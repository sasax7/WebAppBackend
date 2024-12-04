import pandas as pd
import requests

# Define URLs and CSV file
candlesticks_url = "http://localhost:9000/candlesticks"
csv_file = "btcusd_1-min_data.csv"

batch_size = 300  # Number of rows per batch


# Function to send a single batch to localhost
def send_batch(batch_data):
    headers = {"Content-Type": "application/json"}
    print(batch_data[:1])
    response = requests.post(candlesticks_url, json=batch_data, headers=headers)

    if response.status_code == 201:
        print(f"Batch sent successfully! Processed {len(batch_data)} candlesticks.")
        return True
    else:
        print(f"Failed to send batch. Status code: {response.status_code}")
        print(response.text)
        return False


# Function to aggregate and send candles for different timeframes
def aggregate_and_send(chunk, frequency, timeframe_label):
    try:
        chunk["Timestamp"] = pd.to_datetime(chunk["Timestamp"], unit="s")
        resampled = (
            chunk.resample(frequency, on="Timestamp")
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
            .reset_index()
        )
        print(f"Resampled data for frequency '{frequency}':")
        print(resampled.head())  # Debugging line

        batch_data = [
            {
                "open": row["Open"],
                "high": row["High"],
                "low": row["Low"],
                "close": row["Close"],
                "time": int(row["Timestamp"].timestamp())
                * 1000,  # Convert to milliseconds
                "timeframe": timeframe_label,
                "volume": row["Volume"],
                "pair_id": 1,  # Corresponds to BTCUSD pair ID
            }
            for _, row in resampled.iterrows()
        ]
        send_batch(batch_data)
    except Exception as e:
        print(f"Error in aggregate_and_send with frequency '{frequency}': {e}")


# Read and process data in chunks
print(f"Processing and sending data in batches of {batch_size}...")
total_sent = 0

freq_mapping = {"5T": "5m", "1H": "1h", "1D": "1d"}

for chunk_number, chunk in enumerate(
    pd.read_csv(csv_file, chunksize=batch_size), start=1
):
    print(f"Preparing batch {chunk_number}...")
    print(f"Columns in chunk: {chunk.columns.tolist()}")  # Debugging line

    # Prepare candlestick data for the current chunk
    try:
        batch_data = [
            {
                "open": row["Open"],
                "high": row["High"],
                "low": row["Low"],
                "close": row["Close"],
                "time": int(row["Timestamp"]) * 1000,  # Convert to milliseconds
                "timeframe": "1m",
                "volume": row["Volume"] if pd.notna(row["Volume"]) else 0.0,
                "pair_id": 1,
            }
            for _, row in chunk.iterrows()
        ]
    except KeyError as e:
        print(f"KeyError while preparing 1m batch: {e}")
        print("Available columns:", chunk.columns.tolist())
        continue

    # Send the current batch
    if send_batch(batch_data):
        total_sent += len(batch_data)
    else:
        print("Stopping due to an error.")
        break  # Stop processing if there's an error

    # Aggregate and send additional timeframes
    for frequency, timeframe_label in freq_mapping.items():
        aggregate_and_send(chunk, frequency, timeframe_label)

print(f"Finished processing. Total candlesticks sent: {total_sent}")
