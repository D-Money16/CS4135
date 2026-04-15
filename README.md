# CS4135 - E-Library Project
Recommended to use IntelliJ for development, with extensions it can be helpful for both the Java and React code

## Team
Caylum Hurley - 22356363
Vivian Jently - 22079017
Daragh Downes - 22351159
Oscar Zhou    - 22338705
Darragh Quinn - 22359621
Joe Considine - 22344977

## Project Structure
The Springboot backend will go under backend/
The frontend React code will go under frontend/


### Assignment 


#### A1 - Ownership & Division of Bounded Contexts: 
These bounded contexts each have a clear owner: Identity & Access (Joe), Catalogue (Oscar), Lending (Daragh Downes), Notification (Vivian), Book Club (Darragh Quinn), and API Gateway(Caylum). Cross-context dependencies are clearly written down.

#### A2 - DDD Modelling within Each Context (UML)
Entities, Value Objects, Aggregate Roots, Repositories, Domain Services, and Domain Events are used to model each context. A glossary of common terms makes sure that all diagrams and code use the same words.

#### A3 - Aggregate Design & Invariants
Transactional need justifies aggregate boundaries. For example, in LoanIssuanceService, there can be no more than five active loans per user. Cross-aggregate operations use domain events to make sure that things are eventually consistent.

#### A4- Inter‑Context Contracts
Cross-context communication uses versioned REST endpoints for calls that happen at the same time and domain events for calls that happen at different times. Every downstream context has an Anti-Corruption Layer that changes the types of models from the upstream into its own internal types.

#### A5 - Conformance to UML/DDD Notation Guidance
All of the diagrams use the same notation, with stereotypes, multiplicities, and visual differences between aggregate roots, entities, and value objects. We write diagrams in PlantUML, save them in /docs/uml/, and organise them by context.


#### B1. Event Storming Results: 
Found domain events in all bounded contexts, such as BookBorrowed, BookReturned, UserRegistered, NotificationSent, and BookclubCreated. These events set the limits for the services and helped shape the data models for each one.

#### B2. Translation of Insights: 
Results of the event storming were turned into service responsibilities. Commands and aggregates (like Loan, Book, and User) became the most important domain objects in each service. Domain events were linked to REST endpoints and calls between services.

#### B3. Domain Storytelling:  
Considered user journeys such as "student borrows a book" and "staff adds a new title" from start to finish to make sure that the lending service works with the catalogue and notification services correctly.

#### B4. Facilitation and Evidence: 
The team held sessions over Discord calls where they worked together to map out events, commands, and actors on a shared board. They kept notes as a record of the design decisions they made during the project.


#### C1 -Service per Bounded Context
There is the notification, bookclub, identity, lending, catalogue and gateway services, that have tests, the docker-compose file starts them all automatically bringing in all dependencies

#### C2 Service Discovery 
Eureka discovery is implemented, services use this to talk to other services at runtime dynamically

#### C3. Centralized Configuration 
ad-hoc config / hard coded into repo

#### C4. Resilience Patterns
circuit breakers and retry policies implemented on each service,  test-resilience.sh in root can test this

#### C5. Integration & Tests - 
each service has integration tests, mocks DB and actual connections, repo also has bash scripts for testing some services aswell, easies to run the tests in intellij 
h
