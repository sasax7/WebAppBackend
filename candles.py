import requests
import time
from datetime import datetime

# Binance Kline Endpoint
binance_kline_url = "https://api.binance.com/api/v3/klines"
symbol = "BTCUSDT"
interval = "1m"
limit = 1000  # Max limit per request

candlesticks_url = "http://localhost:9000/candlesticks/aggregate"
batch_size = 300


def fetch_binance_klines(symbol, interval, limit=1000, startTime=None, endTime=None):
    params = {
        "symbol": symbol,
        "interval": interval,
        "limit": limit,
    }
    if startTime is not None:
        params["startTime"] = startTime
    if endTime is not None:
        params["endTime"] = endTime

    response = requests.get(binance_kline_url, params=params)
    response.raise_for_status()
    return response.json()


def send_batch(batch_data):
    headers = {"Content-Type": "application/json"}
    print("First candlestick in this batch:", batch_data[0])
    resp = requests.post(candlesticks_url, json=batch_data, headers=headers)
    if resp.status_code == 200:
        print(f"Batch sent successfully! Processed {len(batch_data)} candlesticks.")
        return True
    else:
        print(f"Failed to send batch. Status code: {resp.status_code}")
        print(resp.text)
        return False


# Define start time: Jan 1, 2018 00:00:00 UTC
start_dt = datetime(2022, 10, 26)
startTime = int(start_dt.timestamp() * 1000)  # in ms

# Current time in ms
endTime = int(time.time() * 1000)

total_sent = 0
request_count = 0

print("Fetching klines from Binance starting from 2018 to now...")

while True:
    # If startTime is beyond current time, break
    if startTime > endTime:
        print("Reached current time. Stopping.")
        break

    request_count += 1
    print(f"Request #{request_count}: startTime={startTime}")

    klines = fetch_binance_klines(symbol, interval, limit=limit, startTime=startTime)

    if not klines:
        # No more data returned
        print("No more klines returned by Binance. Stopping.")
        break

    # Convert klines to desired format
    candlesticks = []
    for k in klines:
        open_time = k[0]  # already ms
        open_price = float(k[1])
        high_price = float(k[2])
        low_price = float(k[3])
        close_price = float(k[4])
        volume = float(k[5])
        # trades = k[8] # if needed

        candlesticks.append(
            {
                "open": open_price,
                "high": high_price,
                "low": low_price,
                "close": close_price,
                "time": open_time,
                "timeframe": "1m",
                "volume": volume,
                "pair_id": 1,  # Assuming BTCUSD pair ID is 1
            }
        )

    # Send data in batches of 300
    for i in range(0, len(candlesticks), batch_size):
        batch = candlesticks[i : i + batch_size]
        if send_batch(batch):
            total_sent += len(batch)
        else:
            print("Error sending batch. Stopping.")
            break

    # Update startTime for the next request
    # The last kline's open_time is k[-1][0], we add 1 minute (60,000 ms)
    last_open_time = klines[-1][0]
    startTime = last_open_time + 60_000  # move to next minute

    # Optional: small delay to not hit rate limits too hard
    time.sleep(0.5)

print(f"Finished processing. Total candlesticks sent: {total_sent}")
