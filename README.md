# Bottle Drop
## Summary
The Bottle Drop platform is a distributed system designed to manage beverage supply and empties return at large events, inspired by the c3bottles system used at Chaos Computer Club events. It facilitates the management of a central warehouse, multiple bars, and bottle drop points, ensuring efficient beverage distribution and empties collection. The platform supports various user roles, including planners, warehouse assistants, bartenders, and visitors, each with specific functionalities.

Patterns and Technologies: The system is designed as a microservice architecture using Java and the Spring framework, with plans to incorporate additional patterns and technologies as the project progresses.

## Functionality
### Functional requirements
Functional requirements for the Bottle Drop platform. these requirements represent the intended functionality to be developed in later stages. Each requirement includes a unique identifier, name, short description, priority, and associated actors/roles.

**FR1:** User Account Management
Description: The system shall allow planners to create, update, and delete user accounts, assigning roles such as planner, warehouse assistant, bartender, or visitor.
Priority: High
Actors: Planner
**FR2:** Warehouse Inventory Management
Description: The system shall track the inventory of beverages (by type) and total empties in the central warehouse, updating quantities as stock moves in or out.
Priority: High
Actors: Warehouse assistant
**FR3:** Bar Management
Description: The system shall manage data for each bar, including its name, location, and current stock of beverages (by type).
Priority: High
Actors: Bartender, Warehouse assistant
**FR4:** Automatic Replenishment Request
Description: The system shall automatically generate a replenishment request to the warehouse when a bar’s stock of any beverage type falls below a predefined threshold.
Priority: High
Actors: Bartender (indirectly), Warehouse assistant
**FR5:** Purchase Drinks
Description: Visitors shall be able to purchase drinks from any bar, reducing the bar’s beverage stock accordingly.
Priority: High
Actors: Visitor, Bartender
**FR6:** Return Empties
Description: Visitors shall be able to return empty bottles to any bottle drop point, provided it is not full, increasing the drop point’s empties stock.
Priority: High
Actors: Visitor
**FR7:** Report Full Drop Point
Description: Visitors shall be able to report when a bottle drop point is full, notifying warehouse staff for action.
Priority: Medium
Actors: Visitor
**FR8:** View Warehouse Status
Description: Warehouse assistants shall be able to view the current status of the warehouse, including beverage and empties inventory.
Priority: High
Actors: Warehouse assistant
**FR9:** View Bottle Drop Points Status
Description: Warehouse assistants shall be able to view the status of all bottle drop points, including location, capacity, and current empties stock.
Priority: High
Actors: Warehouse assistant
**FR10:** View Bar Status
Description: Bartenders shall be able to view the current status of their assigned bar, including beverage stock levels.
Priority: High
Actors: Bartender
**FR11:** Empty Bottle Drop Point
Description: The system shall allow warehouse assistants to record the emptying of a bottle drop point, transferring empties to the warehouse and resetting the drop point’s stock.
Priority: High
Actors: Warehouse assistant
**FR12:** Process Replenishment Request
Description: The system shall enable warehouse assistants to process replenishment requests from bars, updating warehouse and bar inventories (simulated via messages).
Priority: High
Actors: Warehouse assistant
**FR13:** Process Drop Point Emptying
Description: The system shall update the warehouse’s empties inventory when a bottle drop point is emptied, reflecting the transferred empties.
Priority: High
Actors: Warehouse assistant
**FR14:** Beverage Type Management
Description: Planners shall be able to define and manage the types of beverages available for the event.
Priority: High
Actors: Planner
**FR15:** Authentication
Description: The system shall allow users to log in using their credentials to access role-specific functionalities.
Priority: High
Actors: Planner, Warehouse assistant, Bartender, Visitor
**FR16:** Authorization
Description: The system shall restrict actions to authorized roles, ensuring users can only perform tasks permitted by their role.
Priority: High
Actors: Planner, Warehouse assistant, Bartender, Visitor

