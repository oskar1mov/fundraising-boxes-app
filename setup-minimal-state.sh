#!/bin/bash

echo "Setting up minimal application state..."
BASE_URL="http://localhost:8080/api"

# Create fundraising event
curl -s -X POST "$BASE_URL/events" \
  -H "Content-Type: application/json" \
  -d '{"name": "Community Charity Drive", "currency": "USD"}'

# Register collection boxes
curl -s -X POST "$BASE_URL/boxes" \
  -H "Content-Type: application/json" \
  -d '{"boxIdentifier": "MAIN-001"}'

curl -s -X POST "$BASE_URL/boxes" \
  -H "Content-Type: application/json" \
  -d '{"boxIdentifier": "MAIN-002"}'

# Assign first box to event
curl -s -X PUT "$BASE_URL/boxes/1/assign/1"

# Add money to assigned box
curl -s -X POST "$BASE_URL/boxes/1/money" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "currency": "USD"}'

curl -s -X POST "$BASE_URL/boxes/1/money" \
  -H "Content-Type: application/json" \
  -d '{"amount": 50.00, "currency": "EUR"}'

curl -s -X POST "$BASE_URL/boxes/1/money" \
  -H "Content-Type: application/json" \
  -d '{"amount": 25.00, "currency": "GBP"}'

echo "Setup complete. Current state:"
echo "Events:"
curl -s -X GET "$BASE_URL/events"
echo -e "\nBoxes:"
curl -s -X GET "$BASE_URL/boxes"
