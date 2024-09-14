


# Inventory Management Service

This is an Inventory Management Service built using Spring Boot, Kafka, and JPA, designed to handle stock management, automatic restocking, and low inventory alerts. This service integrates with a product catalog and listens to product-related events via Kafka for automatic updates to inventory.

## Features

- **Create Inventory**: Allows the creation of new inventory entries for products.
- **Adjust Stock**: Adjust the stock (increase or decrease) based on external events or manual input.
- **Low Stock Monitoring**: Monitors inventory for products that fall below a threshold and logs them in a separate table.
- **Scheduled Restocking**: A scheduled task that checks the low stock table every minute and restocks products that fall below the threshold.
- **Kafka Integration**: Listens to product-related events (`CREATE` and `DELETE`) from Kafka and updates the inventory accordingly.

## Project Structure

### Controllers

`InventoryController` exposes REST API endpoints to manage inventory.

- **POST /inventory**: Create a new inventory entry.
- **GET /inventory/product/{productId}**: Get inventory details for a specific product by ID.
- **POST /inventory/adjust**: Adjust stock for a given product.

### Services

- **InventoryServiceImplementation**: Implements the core logic for inventory management, including handling stock adjustments and listening to Kafka product events.
- **InventorySchedulingService**: Contains a scheduled task to automatically restock products whose quantity has fallen below the threshold. This task runs every minute.

### Kafka Integration

The service listens to the `product_events` topic and responds to the following events:

- **CREATE**: Automatically creates an inventory entry with an initial stock of 1000 units when a new product is created.
- **DELETE**: Automatically deletes the inventory entry when a product is deleted.

### Entities

- **Inventory**: Represents the inventory for a product, containing fields for `productId`, `quantity`, and `lastUpdated`.
- **BelowThresholdProductQuantity**: Stores products whose stock has fallen below the predefined threshold.

## Scheduling

The `InventorySchedulingService` contains a scheduled task that runs every minute (`0 * * * * *`). It performs the following operations:

- Fetches products from the `BelowThresholdProductQuantity` table that are below the stock threshold.
- Increases their stock by 1000 units.
- Deletes the processed entries from the low stock table.

## Configuration

The application can be configured using the following properties in `application.properties`:

```properties
spring.datasource.url=jdbc:your_database_url
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.kafka.bootstrap-servers=your_kafka_broker
spring.kafka.consumer.group-id=inventory_group
```

## Running the Application

To run the application:

1. Clone the repository.
2. Configure the database and Kafka in the `application.properties`.
3. Start the Kafka broker.
4. Run the application using Maven or your preferred IDE.

```bash
mvn spring-boot:run
```

## API Endpoints

| HTTP Method | Endpoint                        | Description                            |
|-------------|---------------------------------|----------------------------------------|
| POST        | /inventory                      | Create a new inventory entry.          |
| GET         | /inventory/product/{productId}  | Get inventory for a specific product.  |
| POST        | /inventory/adjust               | Adjust stock for a specific product.   |

## Kafka Topics

- **product_events**: Listens for product creation and deletion events to automatically update inventory.

## Scheduled Tasks

- **Low Stock Restocking**: Runs every minute and increases stock by 1000 for products below the threshold.

## Error Handling

- If a product is not found in the inventory, a `RuntimeException` is thrown with a relevant error message.
- Insufficient stock while decreasing will result in an error response.
