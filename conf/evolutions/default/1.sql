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

CREATE TABLE users (
  id SERIAL PRIMARY KEY, -- Internal user ID
  firebase_uid TEXT NOT NULL UNIQUE, -- UID from Firebase Authentication
  username TEXT, -- Optional custom username
  email TEXT, -- Email for convenience/reference
  role TEXT DEFAULT 'user', -- Role: 'admin', 'user', etc.
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Account creation date
  last_login TIMESTAMP -- Last login time
);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_last_login ON users (last_login);
CREATE INDEX idx_users_role_created_at ON users (role, created_at);

CREATE TABLE strategies (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE userstrategies (
  user_id INTEGER,
  strategy_id INTEGER,
  PRIMARY KEY (user_id, strategy_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (strategy_id) REFERENCES strategies(id)
);


CREATE TABLE stoplossesname (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE strategystoplosses (
  strategy_id INTEGER,
  stoploss_id INTEGER,
  PRIMARY KEY (strategy_id, stoploss_id),
  FOREIGN KEY (strategy_id) REFERENCES strategies(id),
  FOREIGN KEY (stoploss_id) REFERENCES stoplossesname(id)
);

CREATE TABLE takeprofitname (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE indicatorname (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE pricepointname (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE strategytakeprofit (
  strategy_id INTEGER,
  takeprofit_id INTEGER,
  PRIMARY KEY (strategy_id, takeprofit_id),
  FOREIGN KEY (strategy_id) REFERENCES strategies(id),
  FOREIGN KEY (takeprofit_id) REFERENCES takeprofitname(id)
);

CREATE TABLE strategyindicator (
  strategy_id INTEGER,
  indicator_id INTEGER,
  PRIMARY KEY (strategy_id, indicator_id),
  FOREIGN KEY (strategy_id) REFERENCES strategies(id),
  FOREIGN KEY (indicator_id) REFERENCES indicatorname(id)
);

CREATE TABLE strategypricepoint (
  strategy_id INTEGER,
  pricepoint_id INTEGER,
  PRIMARY KEY (strategy_id, pricepoint_id),
  FOREIGN KEY (strategy_id) REFERENCES strategies(id),
  FOREIGN KEY (pricepoint_id) REFERENCES pricepointname(id)
);

CREATE TABLE advancedtrade (
  id SERIAL PRIMARY KEY,
  start_date TIMESTAMP NOT NULL,
  triggered_date TIMESTAMP,
  is_buy BOOLEAN NOT NULL,
  entry_price DECIMAL(20, 10) NOT NULL,
  strategy_id INTEGER,
  pair_id INTEGER,
  FOREIGN KEY (strategy_id) REFERENCES strategies(id),
  FOREIGN KEY (pair_id) REFERENCES pairs(id)
);

CREATE TABLE Indicators (
  id SERIAL PRIMARY KEY,
  value DOUBLE PRECISION NOT NULL,
  time TIMESTAMP,
  indicator_name_id INTEGER,
  trade_id INTEGER,
  FOREIGN KEY (indicator_name_id) REFERENCES indicatorname(id),
  FOREIGN KEY (trade_id) REFERENCES advancedtrade(id)
);

CREATE TABLE PricePoint (
  id SERIAL PRIMARY KEY,
  value DOUBLE PRECISION NOT NULL,
  time TIMESTAMP NOT NULL,
  trade_id INTEGER,
  pricepoint_name_id INTEGER,
  FOREIGN KEY (trade_id) REFERENCES advancedtrade(id),
  FOREIGN KEY (pricepoint_name_id) REFERENCES pricepointname(id)
);

CREATE TABLE StopLoss (
  id SERIAL PRIMARY KEY,
  price DOUBLE PRECISION NOT NULL,
  timehit TIMESTAMP,
  highest_price DOUBLE PRECISION,
  highest_RR DOUBLE PRECISION,
  hit_1_RR BOOLEAN,
  trade_id INTEGER,
  stoploss_name_id INTEGER,
  FOREIGN KEY (trade_id) REFERENCES advancedtrade(id),
  FOREIGN KEY (stoploss_name_id) REFERENCES stoplossesname(id)
);

CREATE TABLE TakeProfit (
  id SERIAL PRIMARY KEY,
  price DOUBLE PRECISION NOT NULL,
  time_hit TIMESTAMP,
  lowest_price DOUBLE PRECISION,
  trade_id INTEGER,
  takeprofit_name_id INTEGER,
  FOREIGN KEY (trade_id) REFERENCES advancedtrade(id),
  FOREIGN KEY (takeprofit_name_id) REFERENCES takeprofitname(id)
);

CREATE TABLE TradeRR (
  id SERIAL PRIMARY KEY,
  takeprofit_id INTEGER,
  stoploss_id INTEGER,
  RR DOUBLE PRECISION NOT NULL,
  FOREIGN KEY (takeprofit_id) REFERENCES TakeProfit(id),
  FOREIGN KEY (stoploss_id) REFERENCES StopLoss(id)
);


INSERT INTO pairs (pair_name, base_currency, quote_currency, description, spread)
VALUES ('BTCUSD', 'BTC', 'USD', 'Bitcoin to US Dollar', 0.5);

INSERT INTO users (firebase_uid, username, email, role, created_at, last_login)
VALUES ('uid12345', 'JohnDoe', 'johndoe@example.com', 'user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO strategies (name)
VALUES ('Sample Strategy');
INSERT INTO userstrategies (user_id, strategy_id)
VALUES (1, 1);

INSERT INTO stoplossesname (name)
VALUES ('Standard Stop Loss');
INSERT INTO takeprofitname (name)
VALUES ('Standard Take Profit');
INSERT INTO indicatorname (name)
VALUES ('Moving Average');
INSERT INTO pricepointname (name)
VALUES ('Key Resistance Level');
INSERT INTO strategystoplosses (strategy_id, stoploss_id)
VALUES (1, 1);
INSERT INTO strategytakeprofit (strategy_id, takeprofit_id)
VALUES (1, 1);
INSERT INTO strategyindicator (strategy_id, indicator_id)
VALUES (1, 1);
INSERT INTO strategypricepoint (strategy_id, pricepoint_id)
VALUES (1, 1);
INSERT INTO advancedtrade (start_date, triggered_date, is_buy, entry_price, strategy_id, pair_id)
VALUES ('2023-12-01', '2023-12-02', TRUE, 50000.00, 1, 1);
INSERT INTO indicators (value, time, indicator_name_id, trade_id)
VALUES (50500, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO pricepoint (value, time, trade_id, pricepoint_name_id)
VALUES (51000, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO stoploss (price, timehit, highest_price, highest_rr, hit_1_rr, trade_id, stoploss_name_id)
VALUES (49500, NULL, 50500, 1.5, FALSE, 1, 1);
INSERT INTO takeprofit (price, time_hit, lowest_price, trade_id, takeprofit_name_id)
VALUES (52000, NULL, 50000, 1, 1);
INSERT INTO traderr (takeprofit_id, stoploss_id, rr)
VALUES (1, 1, 2.0);
# --- !Downs


DROP TABLE IF EXISTS TradeRR;
DROP TABLE IF EXISTS TakeProfit;
DROP TABLE IF EXISTS StopLoss;
DROP TABLE IF EXISTS PricePoint;
DROP TABLE IF EXISTS Indicators;
DROP TABLE IF EXISTS advancedtrade;
DROP TABLE IF EXISTS strategypricepoint;
DROP TABLE IF EXISTS strategyindicator;
DROP TABLE IF EXISTS strategytakeprofit;
DROP TABLE IF EXISTS strategystoplosses;
DROP TABLE IF EXISTS userstrategies;
DROP TABLE IF EXISTS stoplossesname;
DROP TABLE IF EXISTS takeprofitname;
DROP TABLE IF EXISTS indicatorname;
DROP TABLE IF EXISTS pricepointname;
DROP TABLE IF EXISTS strategies;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS candlesticks;
DROP TABLE IF EXISTS pairs;

