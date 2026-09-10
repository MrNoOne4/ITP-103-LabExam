package com.hospital.eip;

import org.apache.camel.ProducerTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class HospitalRouteTester implements CommandLineRunner {

    private final ProducerTemplate producer;

    public HospitalRouteTester(ProducerTemplate producer) {
        this.producer = producer;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("              HOSPITAL EIP LAB");
        System.out.println("==============================================================");

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> task1(scanner);
                case "2" -> task2(scanner);
                case "3" -> task3();
                case "4" -> task4();
                case "5" -> task5();
                case "6" -> showArchitecture();
                case "0" -> {
                    System.out.println("Exiting Hospital EIP Tester...");
                    return;
                }
                default -> System.out.println("[SYSTEM] Invalid choice. Please enter 0-6.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                 HOSPITAL EIP ROUTE TESTER");
        System.out.println("==============================================================");
        System.out.println("[1] Task 1: Message Channel (JMS Queue)");
        System.out.println("[2] Task 2: Content-Based Router");
        System.out.println("[3] Task 3: Aggregator (Patient + Doctor + Lab)");
        System.out.println("[4] Task 4: Message Translator (XML -> JSON)");
        System.out.println("[5] Task 5: Error Handling & Retry");
        System.out.println("[6] Show EIP Architecture");
        System.out.println("[0] Exit");
        System.out.println("==============================================================");
        System.out.print("Select a task number (0-6): ");
    }

    private void task1(Scanner scanner) {
        System.out.println();
        System.out.println("--- [Task 1: Message Channel - JMS] ---");

        System.out.print("Enter Patient ID (e.g. P-2026-001): ");
        String patientId = scanner.nextLine();
        System.out.print("Enter Patient Name: ");
        String patientName = scanner.nextLine();
        System.out.print("Enter Department (Emergency/Outpatient/Laboratory): ");
        String department = scanner.nextLine();

        String message = String.format(
                "{\"patientId\":\"%s\",\"patientName\":\"%s\",\"department\":\"%s\"}",
                patientId, patientName, department);

        System.out.println();
        System.out.println("[PATIENT REGISTRATION] Patient registration created.");
        System.out.println("[PATIENT REGISTRATION] Message: " + message);
        System.out.println("[MESSAGE CHANNEL] Sending to JMS queue: hospital.patient");

        producer.sendBody("direct:patientRegistration", message);

        System.out.println("[CHECKPOINT] JMS Message Channel test completed.");
        System.out.println("[CHECKPOINT] No RabbitMQ or external broker is required.");
    }

    private void task2(Scanner scanner) {
        System.out.println();
        System.out.println("--- [Task 2: Content-Based Router] ---");

        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine();
        System.out.print("Enter Department (Emergency/Outpatient/Laboratory): ");
        String department = scanner.nextLine();

        String message = String.format(
                "{\"patientId\":\"%s\",\"department\":\"%s\"}",
                patientId, department);

        System.out.println();
        System.out.println("[ROUTER] Incoming patient message: " + message);
        producer.sendBody("direct:routePatient", message);
        System.out.println("[CHECKPOINT] Content-Based Router test completed.");
    }

    private void task3() {
        System.out.println();
        System.out.println("--- [Task 3: Aggregator Pattern] ---");
        String patientId = "P-2026-001";

        System.out.println("[PATIENT SYSTEM] Patient Information received.");
        producer.sendBodyAndHeader("direct:aggregatePatient",
                "Patient ID: P-2026-001 | Patient Name: Juan Dela Cruz",
                "patientId", patientId);

        System.out.println("[DOCTOR SYSTEM] Doctor Assignment received.");
        producer.sendBodyAndHeader("direct:aggregatePatient",
                "Doctor: Dr. Santos | Assessment: Fever",
                "patientId", patientId);

        System.out.println("[LAB SYSTEM] Laboratory Result received.");
        producer.sendBodyAndHeader("direct:aggregatePatient",
                "Test: CBC | Laboratory Result: Normal",
                "patientId", patientId);

        System.out.println("[CHECKPOINT] Three related messages submitted using Patient ID as correlation key.");
    }

    private void task4() {
        System.out.println();
        System.out.println("--- [Task 4: Message Translator] ---");

        String xml = "<PatientResult>"
                + "<PatientId>P-2026-001</PatientId>"
                + "<PatientName>Juan Dela Cruz</PatientName>"
                + "<Test>Complete Blood Count</Test>"
                + "<Result>Normal</Result>"
                + "</PatientResult>";

        System.out.println("[LABORATORY SYSTEM - XML INPUT]");
        System.out.println(xml);
        producer.sendBody("direct:translateLab", xml);
        System.out.println("[CHECKPOINT] XML-to-JSON translation completed.");
    }

    private void task5() {
        System.out.println();
        System.out.println("--- [Task 5: Error Handling & Retry] ---");
        System.out.println("A. TRANSIENT ERROR: fails twice, then succeeds.");
        producer.sendBody("direct:processPatient",
                "PatientId=P-RETRY-001 | TRANSIENT_ERROR | temporary outage");

        System.out.println();
        System.out.println("B. PERMANENT ERROR: retries 3 times, then goes to JMS error queue.");
        producer.sendBody("direct:processPatient",
                "PatientId=P-FAIL-001 | PERMANENT_ERROR | permanent outage");

        System.out.println();
        System.out.println("[CHECKPOINT] Error Handling and Retry test completed.");
    }

    private void showArchitecture() {
        System.out.println();
        System.out.println("==================== EIP ARCHITECTURE ========================");
        System.out.println("Patient Registration");
        System.out.println("        |");
        System.out.println("        v");
        System.out.println("Apache Camel Producer");
        System.out.println("        |");
        System.out.println("        v");
        System.out.println("JMS Queue: hospital.patient");
        System.out.println("        |");
        System.out.println("        v");
        System.out.println("Hospital Processing Consumer");
        System.out.println("        |");
        System.out.println("        v");
        System.out.println("Content-Based Router");
        System.out.println("   /        |        \\");
        System.out.println("Emergency Outpatient Laboratory");
        System.out.println("        |");
        System.out.println("        v");
        System.out.println("Aggregator -> XML to JSON Translator");
        System.out.println("        |");
        System.out.println("        v");
        System.out.println("Error Handler -> JMS hospital.error Queue");
        System.out.println("==============================================================");
    }
}
