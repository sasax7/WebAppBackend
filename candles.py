import pandas as pd
import requests
from concurrent.futures import ThreadPoolExecutor

# Define URLs and CSV file
candlesticks_url = "http://localhost:9000/candlesticks"
csv_file = "btcusd_1-min_data.csv"

# Read CSV data into a DataFrame
data = pd.read_csv(csv_file)
data.columns = ["Timestamp", "Open", "High", "Low", "Close", "Volume"]

# Convert Timestamp to datetime format
data["Timestamp"] = pd.to_datetime(data["Timestamp"], unit="s")

# Define timeframes in minutes
timeframes = {"1m": 1, "5m": 5, "15m": 15, "1h": 60, "4h": 240, "1d": 1440}
batch_size = 300  # Adjust batch size as needed
days_per_chunk = 1  # Process one day at a time for faster iterations


# Function to aggregate data into different timeframes
def resample_data(df, minutes):
    resampled = (
        df.resample(f"{minutes}min")
        .agg(
            {
                "Open": "first",
                "High": "max",
                "Low": "min",
                "Close": "last",
                "Volume": "sum",
            }
        )
        .dropna()
    )
    resampled["Time"] = resampled.index
    return resampled.reset_index(drop=True)


# Function to send candlestick data in batches to localhost
def send_data_in_batches(candlestick_data):
    headers = {"Content-Type": "application/json"}
    total_sent = 0

    for i in range(0, len(candlestick_data), batch_size):
        batch = candlestick_data[i : i + batch_size]
        response = requests.post(candlesticks_url, json=batch, headers=headers)

        if response.status_code == 201:
            total_sent += len(batch)
            print(
                f"Batch sent successfully! Total candlesticks processed: {total_sent}"
            )
        else:
            print(f"Failed to send batch. Status code: {response.status_code}")
            print(response.text)
            break  # Stop if there's an error to avoid sending more invalid data


# Function to process each timeframe for a single day and send data
def process_timeframe_for_day(daily_data, timeframe, minutes):
    print(f"Processing data for {daily_data['Date'].iloc[0]} and timeframe {timeframe}")
    df_resampled = resample_data(daily_data.set_index("Timestamp"), minutes)
    candlestick_data = [
        {
            "open": row["Open"],
            "high": row["High"],
            "low": row["Low"],
            "close": row["Close"],
            "time": int(row["Time"].timestamp() * 1000),
            "timeframe": timeframe,
            "volume": row["Volume"],
            "pairId": 2,  # Corresponds to BTCUSD pair ID
        }
        for _, row in df_resampled.iterrows()
    ]
    send_data_in_batches(candlestick_data)


# Process data day by day
data["Date"] = data["Timestamp"].dt.date  # Add a date column for daily grouping
unique_dates = data["Date"].unique()

for current_date in unique_dates:
    print(f"Processing data for {current_date}...")
    daily_data = data[data["Date"] == current_date]

    # Process each timeframe in parallel for faster resampling
    with ThreadPoolExecutor() as executor:
        futures = [
            executor.submit(process_timeframe_for_day, daily_data, timeframe, minutes)
            for timeframe, minutes in timeframes.items()
        ]

        # Collect results
        for future in futures:
            future.result()

    print(f"Completed processing for {current_date}.\n")
