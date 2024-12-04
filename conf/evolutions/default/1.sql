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
WHERE timeframe = '1m';
CREATE INDEX idx_candlesticks_5min ON candlesticks(time)
WHERE timeframe = '5m';
CREATE INDEX idx_candlesticks_15min ON candlesticks(time)
WHERE timeframe = '15m';
CREATE INDEX idx_candlesticks_30min ON candlesticks(time)
WHERE timeframe = '30m';
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


INSERT INTO pairs (pair_name, base_currency, quote_currency, description, spread)
VALUES ('BTCUSD', 'BTC', 'USD', 'Bitcoin to US Dollar', 0.5);
-- Corrected aggregate_higher_timeframes function


# --- !Downs


DROP TABLE IF EXISTS candlesticks;
DROP TABLE IF EXISTS pairs;
