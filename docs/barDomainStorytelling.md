Domain Storytelling for Bar Context
Story 1: Create Bar and initialize stock
Actors: Bartender, System, Event planner
The system checks whether the event planner has defined any bar and stock.
If an initial bar plan exists:
The system retrieves the predefined bar details and its stock.
The system registers the incoming bars and their stock details in the local DB.
If no initial bar plan exists:
The system adds locally predefined bars , and stocks.
Bartender also can create new bar and stock.
Story 2: Serve drink
Actors: Bartender, System
A bartender serves a drink to a customer.
The system decreases the corresponding product quantity from the bar’s stock.
The system logs the served drink in the usage log.
The system updates the log with the timestamp, product, and quantity.
The system calculates and displays the total served quantity per drink type.
Story 3: Auto supply request
Actors: System, Bartender
After stock is updated, the system checks if the quantity for any product is less than or equal to 5.
If true:
The system creates a supply request for that product.
The system displays a toast with timer of 10 seconds to the bartender , allowing them to cancel or change the quantity of the request.
The request is marked with status REQUESTED.
After the request is created and if it is still in REQUESTED status. The bartender can cancel the request.
Story 4: Supply request process
Actors: Warehouse Staff, System
Warehouse staff views all REQUESTED supply requests.
Warehouse staff updates the request status to IN_PROGRESS once fulfillment begins.
After delivery:
Warehouse staff marks the request as DELIVERED.
The system adds the delivered product quantities to the bar’s stock.