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


# Read and process data in chunks
print(f"Processing and sending data in batches of {batch_size}...")
total_sent = 0

for chunk in pd.read_csv(csv_file, chunksize=batch_size):
    print(f"Preparing batch {total_sent // batch_size + 1}...")

    # Prepare candlestick data for the current chunk
    batch_data = [
        {
            "open": row["Open"],
            "high": row["High"],
            "low": row["Low"],
            "close": row["Close"],
            "time": int(row["Timestamp"]) * 1000,  # Convert to milliseconds
            "timeframe": "1min",
            "volume": row["Volume"] if pd.notna(row["Volume"]) else 0.0,
            "pair_id": 1,  # Corresponds to BTCUSD pair ID
        }
        for _, row in chunk.iterrows()
    ]

    # Send the current batch
    if send_batch(batch_data):
        total_sent += len(batch_data)
    else:
        print("Stopping due to an error.")
        break  # Stop processing if there's an error

print(f"Finished processing. Total candlesticks sent: {total_sent}")
