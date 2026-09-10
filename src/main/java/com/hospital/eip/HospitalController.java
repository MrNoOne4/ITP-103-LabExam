package com.hospital.eip;

import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class HospitalController {

    private final ProducerTemplate producer;

    public HospitalController(ProducerTemplate producer) {
        this.producer = producer;
    }


    // ================================================================
    // HEALTH CHECK
    // ================================================================

    @GetMapping("/health")
    public ResponseEntity<?> health() {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("status", "ONLINE");
        response.put("system", "Hospital EIP");
        response.put("message", "Apache Camel is running");

        return ResponseEntity.ok(response);
    }


    // ================================================================
    // TASK 1 - MESSAGE CHANNEL
    // ================================================================

    @PostMapping("/task1")
    public ResponseEntity<?> task1(
            @RequestBody Map<String, String> request
    ) {

        String patientId =
                request.getOrDefault("patientId", "");

        String patientName =
                request.getOrDefault("patientName", "");

        String department =
                request.getOrDefault("department", "");

        String message =
                String.format(
                        "{\"patientId\":\"%s\",\"patientName\":\"%s\",\"department\":\"%s\"}",
                        patientId,
                        patientName,
                        department
                );

        producer.sendBody(
                "direct:patientRegistration",
                message
        );

        return success(
                "Task 1 completed",
                "Patient registration sent to JMS queue: hospital.patient",
                message
        );
    }


    // ================================================================
    // TASK 2 - CONTENT BASED ROUTER
    // ================================================================

    @PostMapping("/task2")
    public ResponseEntity<?> task2(
            @RequestBody Map<String, String> request
    ) {

        String patientId =
                request.getOrDefault("patientId", "");

        String department =
                request.getOrDefault("department", "");

        String message =
                String.format(
                        "{\"patientId\":\"%s\",\"department\":\"%s\"}",
                        patientId,
                        department
                );

        producer.sendBody(
                "direct:routePatient",
                message
        );

        String destination;

        if (department.equalsIgnoreCase("Emergency")) {

            destination = "hospital.emergency";

        } else if (department.equalsIgnoreCase("Outpatient")) {

            destination = "hospital.outpatient";

        } else if (department.equalsIgnoreCase("Laboratory")) {

            destination = "hospital.laboratory";

        } else {

            destination = "hospital.manual-review";
        }

        return success(
                "Task 2 completed",
                "Message routed to " + destination,
                message
        );
    }


    // ================================================================
    // TASK 3 - AGGREGATOR
    // ================================================================

    @PostMapping("/task3")
    public ResponseEntity<?> task3(
            @RequestBody Map<String, String> request
    ) {

        String patientId =
                request.getOrDefault(
                        "patientId",
                        "P-2026-001"
                );

        String patientName =
                request.getOrDefault(
                        "patientName",
                        "Unknown Patient"
                );

        String doctor =
                request.getOrDefault(
                        "doctor",
                        "Unknown Doctor"
                );

        String assessment =
                request.getOrDefault(
                        "assessment",
                        "No assessment"
                );

        String test =
                request.getOrDefault(
                        "test",
                        "Unknown Test"
                );

        String labResult =
                request.getOrDefault(
                        "labResult",
                        "Unknown"
                );


        // Message 1 - Patient

        producer.sendBodyAndHeader(
                "direct:aggregatePatient",

                "Patient ID: "
                        + patientId
                        + " | Patient Name: "
                        + patientName,

                "patientId",
                patientId
        );


        // Message 2 - Doctor

        producer.sendBodyAndHeader(
                "direct:aggregatePatient",

                "Doctor: "
                        + doctor
                        + " | Assessment: "
                        + assessment,

                "patientId",
                patientId
        );


        // Message 3 - Laboratory

        producer.sendBodyAndHeader(
                "direct:aggregatePatient",

                "Test: "
                        + test
                        + " | Laboratory Result: "
                        + labResult,

                "patientId",
                patientId
        );


        return success(
                "Task 3 completed",
                "Patient, Doctor and Laboratory messages were submitted to the Aggregator.",
                "Correlation ID: " + patientId
        );
    }


    // ================================================================
    // TASK 4 - XML TO JSON
    // ================================================================

    @PostMapping("/task4")
    public ResponseEntity<?> task4(
            @RequestBody Map<String, String> request
    ) {

        String patientId =
                request.getOrDefault("patientId", "");

        String patientName =
                request.getOrDefault("patientName", "");

        String test =
                request.getOrDefault("test", "");

        String result =
                request.getOrDefault("result", "");


        String xml =
                "<PatientResult>"
                        + "<PatientId>"
                        + escapeXml(patientId)
                        + "</PatientId>"

                        + "<PatientName>"
                        + escapeXml(patientName)
                        + "</PatientName>"

                        + "<Test>"
                        + escapeXml(test)
                        + "</Test>"

                        + "<Result>"
                        + escapeXml(result)
                        + "</Result>"

                        + "</PatientResult>";


        // Ask Camel to translate it

        Object translated =
                producer.requestBody(
                        "direct:translateLab",
                        xml
                );


        return success(
                "Task 4 completed",
                "XML successfully translated to JSON.",
                translated
        );
    }


    // ================================================================
    // TASK 5 - ERROR HANDLING
    // ================================================================

    @PostMapping("/task5")
    public ResponseEntity<?> task5(
            @RequestBody Map<String, String> request
    ) {

        String patientId =
                request.getOrDefault(
                        "patientId",
                        "P-TEST-001"
                );

        String type =
                request.getOrDefault(
                        "type",
                        "transient"
                );


        String message;


        if (type.equalsIgnoreCase("permanent")) {

            message =
                    "PatientId="
                            + patientId
                            + " | PERMANENT_ERROR | permanent outage";

        } else {

            message =
                    "PatientId="
                            + patientId
                            + " | TRANSIENT_ERROR | temporary outage";
        }


        producer.sendBody(
                "direct:processPatient",
                message
        );


        if (type.equalsIgnoreCase("permanent")) {

            return success(
                    "Task 5 completed",
                    "Permanent error was submitted. Camel will retry the message and then send it to hospital.error.",
                    message
            );

        } else {

            return success(
                    "Task 5 completed",
                    "Transient error was submitted. Camel will retry and should eventually succeed.",
                    message
            );
        }
    }


    // ================================================================
    // ARCHITECTURE
    // ================================================================

    @GetMapping("/architecture")
    public ResponseEntity<?> architecture() {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "title",
                "Hospital EIP Architecture"
        );

        response.put(
                "steps",
                new String[]{
                        "Patient Registration",
                        "Apache Camel Producer",
                        "JMS Queue: hospital.patient",
                        "Hospital Processing Consumer",
                        "Content-Based Router",
                        "Emergency / Outpatient / Laboratory",
                        "Aggregator",
                        "XML → JSON Message Translator",
                        "Error Handler",
                        "JMS Queue: hospital.error"
                }
        );

        return ResponseEntity.ok(response);
    }


    // ================================================================
    // SUCCESS RESPONSE
    // ================================================================

    private ResponseEntity<?> success(
            String title,
            String message,
            Object data
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "success",
                true
        );

        response.put(
                "title",
                title
        );

        response.put(
                "message",
                message
        );

        response.put(
                "data",
                data
        );

        return ResponseEntity.ok(response);
    }


    // ================================================================
    // XML ESCAPING
    // ================================================================

    private String escapeXml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}