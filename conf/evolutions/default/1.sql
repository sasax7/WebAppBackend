# --- !Ups

CREATE TABLE pairs (
  id SERIAL PRIMARY KEY,
  pair_name TEXT NOT NULL UNIQUE,
  base_currency TEXT,
  quote_currency TEXT,
  description TEXT,
  spread DOUBLE PRECISION
);

CREATE TABLE candlesticks (
  open DOUBLE PRECISION NOT NULL,
  high DOUBLE PRECISION NOT NULL,
  low DOUBLE PRECISION NOT NULL,
  close DOUBLE PRECISION NOT NULL,
  time BIGINT NOT NULL, -- Unix timestamp in milliseconds
  timeframe TEXT NOT NULL,
  volume DOUBLE PRECISION,
  pair_id INTEGER REFERENCES pairs(id),
  CONSTRAINT unique_candlestick UNIQUE (pair_id, timeframe, time),
  PRIMARY KEY (pair_id, timeframe, time)
);

CREATE INDEX idx_candlesticks_pair_timeframe_time
ON candlesticks(pair_id, timeframe, time);

CREATE INDEX idx_candlesticks_time ON candlesticks(time);
CREATE INDEX idx_candlesticks_pair_id ON candlesticks(pair_id);
CREATE INDEX idx_candlesticks_timeframe ON candlesticks(timeframe);

CREATE INDEX idx_candlesticks_1min ON candlesticks(time)
WHERE timeframe = '1min';
CREATE INDEX idx_candlesticks_5min ON candlesticks(time)
WHERE timeframe = '5min';
CREATE INDEX idx_candlesticks_15min ON candlesticks(time)
WHERE timeframe = '15min';
CREATE INDEX idx_candlesticks_30min ON candlesticks(time)
WHERE timeframe = '30min';
CREATE INDEX idx_candlesticks_1h ON candlesticks(time)
WHERE timeframe = '1h';
CREATE INDEX idx_candlesticks_4h ON candlesticks(time)
WHERE timeframe = '4h';
CREATE INDEX idx_candlesticks_1d ON candlesticks(time)
WHERE timeframe = '1d';

-- Create Timeframes Table
CREATE TABLE timeframes (
  id SERIAL PRIMARY KEY,
  name TEXT UNIQUE NOT NULL,
  minutes INTEGER NOT NULL
);

INSERT INTO timeframes (name, minutes) VALUES
('5min', 5),
('15min', 15),
('30min', 30),
('1h', 60),
('4h', 240),
('1d', 1440);

-- Corrected aggregate_higher_timeframes function
CREATE OR REPLACE FUNCTION aggregate_higher_timeframes()
RETURNS TRIGGER AS $$
DECLARE
    tf RECORD;;
    bucket_start TIMESTAMPTZ;;
    aggregated_candle RECORD;;
BEGIN
    -- Only proceed if the inserted candle is 1min
    IF NEW.timeframe <> '1min' THEN
        RETURN NEW;;
    END IF;;

    FOR tf IN SELECT name, minutes FROM timeframes WHERE minutes > 1 ORDER BY minutes ASC LOOP
        -- Convert BIGINT time to TIMESTAMPTZ and calculate the bucket start time
        bucket_start := date_trunc('minute', to_timestamp(NEW.time / 1000)) - 
                        (EXTRACT(minute FROM to_timestamp(NEW.time / 1000)) % tf.minutes) * INTERVAL '1 minute';;

        -- Aggregate candles within the bucket
        SELECT 
            MIN(open) AS open,
            MAX(high) AS high,
            MIN(low) AS low,
            MAX(close) AS close,
            SUM(COALESCE(volume, 0)) AS volume
        INTO aggregated_candle
        FROM candlesticks
        WHERE pair_id = NEW.pair_id
          AND timeframe = '1min'
          AND to_timestamp(time / 1000) >= bucket_start
          AND to_timestamp(time / 1000) < bucket_start + (tf.minutes * INTERVAL '1 minute');;

        -- Upsert the aggregated candle
        INSERT INTO candlesticks (open, high, low, close, time, timeframe, volume, pair_id)
        VALUES (
            aggregated_candle.open,
            aggregated_candle.high,
            aggregated_candle.low,
            aggregated_candle.close,
            EXTRACT(EPOCH FROM bucket_start) * 1000, -- Convert TIMESTAMPTZ back to BIGINT
            tf.name,
            aggregated_candle.volume,
            NEW.pair_id
        )
        ON CONFLICT (pair_id, timeframe, time)
        DO UPDATE SET
            open = EXCLUDED.open,
            high = GREATEST(candlesticks.high, EXCLUDED.high),
            low = LEAST(candlesticks.low, EXCLUDED.low),
            close = EXCLUDED.close,
            volume = candlesticks.volume + EXCLUDED.volume;;
    END LOOP;;

    RETURN NEW;;
END;;
$$ LANGUAGE plpgsql;;

-- Create Trigger
CREATE TRIGGER trg_aggregate_higher_timeframes
AFTER INSERT ON candlesticks
FOR EACH ROW
EXECUTE FUNCTION aggregate_higher_timeframes();;

# --- !Downs
DROP TRIGGER IF EXISTS trg_aggregate_higher_timeframes ON candlesticks;
DROP FUNCTION IF EXISTS aggregate_higher_timeframes;

DROP TABLE IF EXISTS candlesticks;
DROP TABLE IF EXISTS pairs;
DROP TABLE IF EXISTS timeframes;
