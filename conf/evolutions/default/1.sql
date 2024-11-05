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
  id SERIAL PRIMARY KEY,
  open DOUBLE PRECISION NOT NULL,
  high DOUBLE PRECISION NOT NULL,
  low DOUBLE PRECISION NOT NULL,
  close DOUBLE PRECISION NOT NULL,
  time TIMESTAMP NOT NULL,
  timeframe TEXT NOT NULL,
  volume DOUBLE PRECISION,
  pair_id INTEGER,
  FOREIGN KEY (pair_id) REFERENCES pairs(id)
);

-- CREATE TABLE users (
--   id SERIAL PRIMARY KEY,
--   username TEXT NOT NULL UNIQUE,
--   password TEXT NOT NULL,
--   email TEXT NOT NULL UNIQUE,
--   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

-- CREATE TABLE strategies (
--   id SERIAL PRIMARY KEY,
--   name TEXT NOT NULL
-- );

-- CREATE TABLE userstrategies (
--   user_id INTEGER,
--   strategy_id INTEGER,
--   PRIMARY KEY (user_id, strategy_id),
--   FOREIGN KEY (user_id) REFERENCES users(id),
--   FOREIGN KEY (strategy_id) REFERENCES strategies(id)
-- );

-- CREATE TABLE stoplossesname (
--   id SERIAL PRIMARY KEY,
--   name TEXT NOT NULL
-- );

-- CREATE TABLE strategystoplosses (
--   strategy_id INTEGER,
--   stoploss_id INTEGER,
--   PRIMARY KEY (strategy_id, stoploss_id),
--   FOREIGN KEY (strategy_id) REFERENCES strategies(id),
--   FOREIGN KEY (stoploss_id) REFERENCES stoplossesname(id)
-- );

-- CREATE TABLE takeprofitname (
--   id SERIAL PRIMARY KEY,
--   name TEXT NOT NULL
-- );

-- CREATE TABLE indicatorname (
--   id SERIAL PRIMARY KEY,
--   name TEXT NOT NULL
-- );

-- CREATE TABLE pricepointname (
--   id SERIAL PRIMARY KEY,
--   name TEXT NOT NULL
-- );

-- CREATE TABLE strategytakeprofit (
--   strategy_id INTEGER,
--   takeprofit_id INTEGER,
--   PRIMARY KEY (strategy_id, takeprofit_id),
--   FOREIGN KEY (strategy_id) REFERENCES strategies(id),
--   FOREIGN KEY (takeprofit_id) REFERENCES takeprofitname(id)
-- );

-- CREATE TABLE strategyindicator (
--   strategy_id INTEGER,
--   indicator_id INTEGER,
--   PRIMARY KEY (strategy_id, indicator_id),
--   FOREIGN KEY (strategy_id) REFERENCES strategies(id),
--   FOREIGN KEY (indicator_id) REFERENCES indicatorname(id)
-- );

-- CREATE TABLE strategypricepoint (
--   strategy_id INTEGER,
--   pricepoint_id INTEGER,
--   PRIMARY KEY (strategy_id, pricepoint_id),
--   FOREIGN KEY (strategy_id) REFERENCES strategies(id),
--   FOREIGN KEY (pricepoint_id) REFERENCES pricepointname(id)
-- );

-- CREATE TABLE advancedtrade (
--   id SERIAL PRIMARY KEY,
--   start_date DATE NOT NULL,
--   triggered_date DATE,
--   is_buy BOOLEAN NOT NULL,
--   entry_price DECIMAL(20, 10) NOT NULL,
--   strategy_id INTEGER,
--   pair_id INTEGER,
--   FOREIGN KEY (strategy_id) REFERENCES strategies(id),
--   FOREIGN KEY (pair_id) REFERENCES pairs(id)
-- );

-- CREATE TABLE Indicators (
--   id SERIAL PRIMARY KEY,
--   value DOUBLE PRECISION NOT NULL,
--   time TIMESTAMP,
--   indicator_name_id INTEGER,
--   trade_id INTEGER,
--   FOREIGN KEY (indicator_name_id) REFERENCES indicatorname(id),
--   FOREIGN KEY (trade_id) REFERENCES advancedtrade(id)
-- );

-- CREATE TABLE PricePoint (
--   id SERIAL PRIMARY KEY,
--   value DOUBLE PRECISION NOT NULL,
--   time TIMESTAMP NOT NULL,
--   trade_id INTEGER,
--   pricepoint_name_id INTEGER,
--   FOREIGN KEY (trade_id) REFERENCES advancedtrade(id),
--   FOREIGN KEY (pricepoint_name_id) REFERENCES pricepointname(id)
-- );

-- CREATE TABLE StopLoss (
--   id SERIAL PRIMARY KEY,
--   price DOUBLE PRECISION NOT NULL,
--   timehit TIMESTAMP,
--   highest_price DOUBLE PRECISION,
--   highest_RR DOUBLE PRECISION,
--   hit_1_RR BOOLEAN,
--   trade_id INTEGER,
--   stoploss_name_id INTEGER,
--   FOREIGN KEY (trade_id) REFERENCES advancedtrade(id),
--   FOREIGN KEY (stoploss_name_id) REFERENCES stoplossesname(id)
-- );

-- CREATE TABLE TakeProfit (
--   id SERIAL PRIMARY KEY,
--   price DOUBLE PRECISION NOT NULL,
--   time_hit TIMESTAMP,
--   lowest_price DOUBLE PRECISION,
--   trade_id INTEGER,
--   takeprofit_name_id INTEGER,
--   FOREIGN KEY (trade_id) REFERENCES advancedtrade(id),
--   FOREIGN KEY (takeprofit_name_id) REFERENCES takeprofitname(id)
-- );

-- CREATE TABLE TradeRR (
--   id SERIAL PRIMARY KEY,
--   takeprofit_id INTEGER,
--   stoploss_id INTEGER,
--   RR DOUBLE PRECISION NOT NULL,
--   FOREIGN KEY (takeprofit_id) REFERENCES TakeProfit(id),
--   FOREIGN KEY (stoploss_id) REFERENCES StopLoss(id)
-- );

# --- !Downs

-- DROP TABLE IF EXISTS TradeRR;
-- DROP TABLE IF EXISTS TakeProfit;
-- DROP TABLE IF EXISTS StopLoss;
-- DROP TABLE IF EXISTS PricePoint;
-- DROP TABLE IF EXISTS Indicators;
-- DROP TABLE IF EXISTS advancedtrade;
-- DROP TABLE IF EXISTS strategypricepoint;
-- DROP TABLE IF EXISTS strategyindicator;
-- DROP TABLE IF EXISTS strategytakeprofit;
-- DROP TABLE IF EXISTS pricepointname;
-- DROP TABLE IF EXISTS indicatorname;
-- DROP TABLE IF EXISTS takeprofitname;
-- DROP TABLE IF EXISTS strategystoplosses;
-- DROP TABLE IF EXISTS stoplossesname;
-- DROP TABLE IF EXISTS userstrategies;
-- DROP TABLE IF EXISTS strategies;
-- DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS candlesticks;
DROP TABLE IF EXISTS pairs;
