# ITP103 Hospital EIP

This version keeps the Hospital Enterprise Integration Patterns project **without RabbitMQ**.
Instead, Task 1 and the error channel use a real **JMS queue backed by an embedded Apache ActiveMQ 6 broker**.
No external broker installation or background service is required.

## Technology
- Java 17
- Spring Boot 3.5.5
- Apache Camel 4.10.5
- Apache ActiveMQ 6.1.5 (embedded)
- JMS queues
- Maven
- Jackson JSON

## Five EIP Tasks

### Task 1 - Message Channel
Patient Registration -> Apache Camel -> `hospital.patient` JMS Queue -> Hospital Processing Consumer.

The queue is real JMS messaging; it is not a Java List or a `direct:`/`seda:` simulation.

### Task 2 - Content-Based Router
The router inspects the patient department and sends the message to:
- `hospital.emergency`
- `hospital.outpatient`
- `hospital.laboratory`
- `hospital.manual-review` for unknown values

### Task 3 - Aggregator
Three related hospital messages are combined:
1. Patient Information
2. Doctor Assignment
3. Laboratory Result

The **Patient ID is used as the correlation identifier**.

### Task 4 - Message Translator
A hospital laboratory result is received as XML and converted to JSON using a real XML parser and Jackson.

### Task 5 - Error Handling and Retry
The project demonstrates both:
- A transient error that fails twice and succeeds on retry.
- A permanent error that is retried 3 times and then moved to the `hospital.error` JMS queue.

## How to Run

1. Open the project in IntelliJ IDEA.
2. Make sure Java 17 is selected.
3. Allow Maven to download dependencies.
4. Run:
   `src/main/java/com/hospital/eip/HospitalEipApplication.java`
5. Use the interactive menu.

No RabbitMQ installation is needed.
No external ActiveMQ installation is needed because the broker is embedded for this lab.

## Suggested Screenshots

- Task 1: show patient input, JMS queue send, and JMS consumer receipt.
- Task 2: test Emergency, Outpatient, and Laboratory and show the different destination queues.
- Task 3: show the three messages and the final aggregated medical summary.
- Task 4: show XML input and JSON output.
- Task 5: show retry attempts, successful retry, and permanent failure going to the error queue.

