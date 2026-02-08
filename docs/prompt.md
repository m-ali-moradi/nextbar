
Now clean up BarService and Warehouse anything related to FeignClient and circuit breaker. We have implemented the communication between Bar and Event Planner via RabbitMQ. for now, we only have the following communication between Bar and Warehouse:
- Bar sends a message to Warehouse to request supply.
- Warehouse sends a message to Bar to update the supply status.
these two Inter-service communication are implemented via RabbitMQ (cloud stream).

how these messages are pushed to Frontend is done via WebSocket through the API Gateway. The API Gateway listens to the RabbitMQ messages and pushes the updates to the Frontend via WebSocket.