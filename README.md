# Fundraising Boxes Application

A Spring Boot application for managing collection boxes during fundraising events for charity organizations.

## Features

- **Collection Box Management**: Register, unregister, and list collection boxes
- **Fundraising Event Management**: Create and manage fundraising events
- **Box Assignment**: Assign boxes to fundraising events (only when empty)
- **Money Management**: Add money to boxes and transfer to events
- **Currency Support**: Supports USD, EUR, and GBP with automatic conversion
- **Financial Reporting**: View balances across all fundraising events

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building and Running

1. **Build the application:**
```bash
mvn clean package
```

2. **Run the application:**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

3. **Set up minimal working state:**
```bash
./setup-minimal-state.sh
```

## API Endpoints

### Collection Boxes

- `POST /api/boxes` - Register a new collection box
  ```json
  {
    "boxIdentifier": "BOX-001"
  }
  ```

- `GET /api/boxes` - List all collection boxes
- `DELETE /api/boxes/{id}` - Unregister a collection box
- `PUT /api/boxes/{boxId}/assign/{eventId}` - Assign box to fundraising event
- `PUT /api/boxes/{boxId}/unassign` - Unassign box from event
- `POST /api/boxes/{boxId}/money` - Add money to a box
  ```json
  {
    "amount": 100.50,
    "currency": "USD"
  }
  ```
- `POST /api/boxes/{boxId}/empty` - Empty box (transfer money to assigned event)

### Fundraising Events

- `POST /api/events` - Create a new fundraising event
  ```json
  {
    "name": "Charity Event 2025",
    "currency": "EUR"
  }
  ```

- `GET /api/events` - List all fundraising events
- `GET /api/events/{id}` - Get specific fundraising event
- `GET /api/events/report` - Get financial report

## Testing

The application includes comprehensive JUnit tests covering all major functionality.

### Running All Tests
```bash
# Run all tests
mvn test

```

### Running Specific Test Classes
```bash
# Run specific test class
mvn test -Dtest=BoxServiceTest
mvn test -Dtest=CurrencyConversionServiceTest
mvn test -Dtest=BoxControllerTest
mvn test -Dtest=FundraisingEventControllerTest
```

### Running Individual Test Methods
```bash
# Run specific test method
mvn test -Dtest=BoxServiceTest#shouldRegisterBoxSuccessfully
mvn test -Dtest=BoxControllerTest#shouldCreateBoxSuccessfully
```

## Currency Exchange Rates

The application uses direct exchange rates between all currency pairs:
- USD ↔ EUR: 1 USD = 0.85 EUR | 1 EUR = 1.18 USD
- USD ↔ GBP: 1 USD = 0.75 GBP | 1 GBP = 1.33 USD  
- EUR ↔ GBP: 1 EUR = 0.88 GBP | 1 GBP = 1.13 EUR

When money is transferred from a box to a fundraising event, it's automatically converted to the event's currency using direct conversion (single operation) for efficiency.

**Rounding:** All currency conversions are rounded to 2 decimal places using HALF_UP rounding mode, but the rounding strategy is debatable depending on product requirements.

## Start the application
./mvnw spring-boot:run

## Create events
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"name": "Animal Rescue Fund", "currency": "USD"}'

## Register boxes  
curl -X POST http://localhost:8080/api/boxes \
  -H "Content-Type: application/json" \
  -d '{"boxIdentifier": "BOX-001"}'

## List all boxes (per requirement - shows assigned/empty status)
curl -X GET http://localhost:8080/api/boxes

## Assign box to event (only works if box is empty)
curl -X PUT http://localhost:8080/api/boxes/1/assign/1

## Unassign box from event
curl -X PUT http://localhost:8080/api/boxes/1/unassign

## List all events (financial report)
curl -X GET http://localhost:8080/api/events

## Delete a box (unregister)
curl -X DELETE http://localhost:8080/api/boxes/1

# Contact
oskarkarimovoskar@gmail.com
