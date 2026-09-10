// package com.hospital.eip;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.apache.camel.Exchange;
// import org.apache.camel.LoggingLevel;
// import org.apache.camel.builder.RouteBuilder;
// import org.springframework.stereotype.Component;

// import javax.xml.parsers.DocumentBuilderFactory;
// import java.io.ByteArrayInputStream;
// import java.nio.charset.StandardCharsets;
// import java.util.LinkedHashMap;
// import java.util.Map;

// @Component
// public class HospitalRoutes extends RouteBuilder {

//     private static final String JMS = "activemq6:queue:";
//     private final ObjectMapper mapper = new ObjectMapper();

//     @Override
//     public void configure() {

//         // TASK 5: Error Channel + Retry.
//         // Uses the JMS error queue instead of RabbitMQ.
//         errorHandler(deadLetterChannel(JMS + "hospital.error")
//                 .maximumRedeliveries(3)
//                 .redeliveryDelay(1000)
//                 .retryAttemptedLogLevel(LoggingLevel.WARN)
//                 .onRedelivery(exchange -> {
//                     Integer retry = exchange.getProperty(Exchange.REDELIVERY_COUNTER, Integer.class);
//                     System.out.println("[TASK 5] Retry attempt " + retry + " - processing patient again...");
//                 }));

//         // ============================================================
//         // TASK 1 - MESSAGE CHANNEL (JMS QUEUE)
//         // ============================================================
//         from("direct:patientRegistration")
//                 .routeId("task1-patient-producer")
//                 .log("[TASK 1] Patient Registration System - creating message")
//                 .to(JMS + "hospital.patient")
//                 .log("[TASK 1] Message sent to JMS queue: hospital.patient");

//         from(JMS + "hospital.patient?concurrentConsumers=1")
//                 .routeId("task1-hospital-consumer")
//                 .log("[TASK 1] Hospital Processing System - received from JMS queue: ${body}")
//                 .log("[TASK 1] Message Channel delivery successful.");

//         // ============================================================
//         // TASK 2 - CONTENT-BASED ROUTER
//         // ============================================================
//         from("direct:routePatient")
//                 .routeId("task2-content-based-router")
//                 .choice()
//                     .when(simple("${body} contains 'Emergency'"))
//                         .log("[TASK 2] ROUTER: Emergency -> Emergency Department Queue")
//                         .to(JMS + "hospital.emergency")
//                     .when(simple("${body} contains 'Outpatient'"))
//                         .log("[TASK 2] ROUTER: Outpatient -> Outpatient Department Queue")
//                         .to(JMS + "hospital.outpatient")
//                     .when(simple("${body} contains 'Laboratory'"))
//                         .log("[TASK 2] ROUTER: Laboratory -> Laboratory Queue")
//                         .to(JMS + "hospital.laboratory")
//                     .otherwise()
//                         .log("[TASK 2] ROUTER: Unknown department -> Manual Review Queue")
//                         .to(JMS + "hospital.manual-review")
//                 .end();

//         from(JMS + "hospital.emergency")
//                 .routeId("task2-emergency-consumer")
//                 .log("[TASK 2] [EMERGENCY DEPARTMENT] Message received: ${body}");

//         from(JMS + "hospital.outpatient")
//                 .routeId("task2-outpatient-consumer")
//                 .log("[TASK 2] [OUTPATIENT DEPARTMENT] Message received: ${body}");

//         from(JMS + "hospital.laboratory")
//                 .routeId("task2-laboratory-consumer")
//                 .log("[TASK 2] [LABORATORY] Message received: ${body}");

//         from(JMS + "hospital.manual-review")
//                 .routeId("task2-manual-review-consumer")
//                 .log("[TASK 2] [MANUAL REVIEW] Message received: ${body}");

//         // ============================================================
//         // TASK 3 - AGGREGATOR
//         // ============================================================
//         // Patient ID is the correlation identifier.
//         from("direct:aggregatePatient")
//                 .routeId("task3-patient-aggregator")
//                 .aggregate(header("patientId"), (oldExchange, newExchange) -> {
//                     if (oldExchange == null) {
//                         return newExchange;
//                     }

//                     String oldBody = oldExchange.getIn().getBody(String.class);
//                     String newBody = newExchange.getIn().getBody(String.class);
//                     oldExchange.getIn().setBody(oldBody + System.lineSeparator() + newBody);
//                     return oldExchange;
//                 })
//                 .completionSize(3)
//                 .log("[TASK 3] AGGREGATOR: 3 related messages combined using Patient ID=${header.patientId}")
//                 .log("[TASK 3] COMPLETE PATIENT MEDICAL SUMMARY:\n${body}");

//         // ============================================================
//         // TASK 4 - MESSAGE TRANSLATOR XML -> JSON
//         // ============================================================
//         from("direct:translateLab")
//                 .routeId("task4-message-translator")
//                 .process(exchange -> {
//                     String xml = exchange.getIn().getBody(String.class);
//                     var factory = DocumentBuilderFactory.newInstance();
//                     factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
//                     factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
//                     factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//                     var document = factory.newDocumentBuilder()
//                             .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

//                     Map<String, String> result = new LinkedHashMap<>();
//                     result.put("patientId", text(document, "PatientId"));
//                     result.put("patientName", text(document, "PatientName"));
//                     result.put("test", text(document, "Test"));
//                     result.put("result", text(document, "Result"));

//                     exchange.getIn().setBody(mapper.writeValueAsString(result));
//                 })
//                 .log("[TASK 4] MESSAGE TRANSLATOR: XML converted to JSON")
//                 .log("[TASK 4] [HOSPITAL JSON] ${body}");

//         // ============================================================
//         // TASK 5 - PROCESSING ROUTE
//         // ============================================================
//         from("direct:processPatient")
//                 .routeId("task5-error-handling")
//                 .log("[TASK 5] Hospital Processing System attempting: ${body}")
//                 .process(exchange -> {
//                     String body = exchange.getIn().getBody(String.class);
//                     Integer attempts = exchange.getProperty(Exchange.REDELIVERY_COUNTER, Integer.class);
//                     if (attempts == null) attempts = 0;

//                     // TRANSIENT_ERROR succeeds after the retries demonstrate the retry mechanism.
//                     if (body.contains("TRANSIENT_ERROR") && attempts < 2) {
//                         System.out.println("[TASK 5] Simulated temporary outage. Throwing exception...");
//                         throw new RuntimeException("Temporary hospital processing outage");
//                     }

//                     // PERMANENT_ERROR demonstrates the Error Channel after max retries.
//                     if (body.contains("PERMANENT_ERROR")) {
//                         System.out.println("[TASK 5] Simulated permanent outage. Throwing exception...");
//                         throw new RuntimeException("Permanent hospital processing outage");
//                     }
//                 })
//                 .log("[TASK 5] Processing successful.");

//         from(JMS + "hospital.error")
//                 .routeId("task5-error-channel")
//                 .log("[TASK 5] ERROR CHANNEL: failed message received from JMS error queue -> ${body}")
//                 .log("[TASK 5] Maximum retries reached. Message is now available for manual review.");
//     }

//     private String text(org.w3c.dom.Document document, String tag) {
//         var nodes = document.getElementsByTagName(tag);
//         if (nodes.getLength() == 0) return "";
//         return nodes.item(0).getTextContent();
//     }
// }

package com.hospital.eip;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HospitalRoutes extends RouteBuilder {

    private static final String JMS = "activemq6:queue:";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure() {

        // ============================================================
        // TASK 5 - ERROR HANDLING + RETRY
        // ============================================================

        errorHandler(
                deadLetterChannel(JMS + "hospital.error")
                        .maximumRedeliveries(3)
                        .redeliveryDelay(1000)
                        .retryAttemptedLogLevel(LoggingLevel.WARN)
                        .onRedelivery(exchange -> {

                            Integer retry =
                                    exchange.getProperty(
                                            Exchange.REDELIVERY_COUNTER,
                                            Integer.class
                                    );

                            System.out.println(
                                    "[TASK 5] Retry attempt "
                                            + retry
                                            + " - processing patient again..."
                            );
                        })
        );

        // ============================================================
        // TASK 1 - MESSAGE CHANNEL
        // ============================================================

        from("direct:patientRegistration")
                .routeId("task1-patient-producer")

                .log("[TASK 1] Patient Registration System - creating message")

                .to(JMS + "hospital.patient")

                .log("[TASK 1] Message sent to JMS queue: hospital.patient");


        from(JMS + "hospital.patient?concurrentConsumers=1")
                .routeId("task1-hospital-consumer")

                .log(
                        "[TASK 1] Hospital Processing System - received from JMS queue: ${body}"
                )

                .log(
                        "[TASK 1] Message Channel delivery successful."
                );


        // ============================================================
        // TASK 2 - CONTENT BASED ROUTER
        // ============================================================

        from("direct:routePatient")
                .routeId("task2-content-based-router")

                .choice()

                .when(simple("${body} contains 'Emergency'"))

                .log(
                        "[TASK 2] ROUTER: Emergency -> Emergency Department Queue"
                )

                .to(JMS + "hospital.emergency")


                .when(simple("${body} contains 'Outpatient'"))

                .log(
                        "[TASK 2] ROUTER: Outpatient -> Outpatient Department Queue"
                )

                .to(JMS + "hospital.outpatient")


                .when(simple("${body} contains 'Laboratory'"))

                .log(
                        "[TASK 2] ROUTER: Laboratory -> Laboratory Queue"
                )

                .to(JMS + "hospital.laboratory")


                .otherwise()

                .log(
                        "[TASK 2] ROUTER: Unknown department -> Manual Review Queue"
                )

                .to(JMS + "hospital.manual-review")

                .end();


        // Emergency consumer

        from(JMS + "hospital.emergency")
                .routeId("task2-emergency-consumer")

                .log(
                        "[TASK 2] [EMERGENCY DEPARTMENT] Message received: ${body}"
                );


        // Outpatient consumer

        from(JMS + "hospital.outpatient")
                .routeId("task2-outpatient-consumer")

                .log(
                        "[TASK 2] [OUTPATIENT DEPARTMENT] Message received: ${body}"
                );


        // Laboratory consumer

        from(JMS + "hospital.laboratory")
                .routeId("task2-laboratory-consumer")

                .log(
                        "[TASK 2] [LABORATORY] Message received: ${body}"
                );


        // Manual review consumer

        from(JMS + "hospital.manual-review")
                .routeId("task2-manual-review-consumer")

                .log(
                        "[TASK 2] [MANUAL REVIEW] Message received: ${body}"
                );


        // ============================================================
        // TASK 3 - AGGREGATOR
        // ============================================================

        from("direct:aggregatePatient")
                .routeId("task3-patient-aggregator")

                .aggregate(
                        header("patientId"),
                        (oldExchange, newExchange) -> {

                            if (oldExchange == null) {
                                return newExchange;
                            }

                            String oldBody =
                                    oldExchange.getIn()
                                            .getBody(String.class);

                            String newBody =
                                    newExchange.getIn()
                                            .getBody(String.class);

                            oldExchange.getIn().setBody(
                                    oldBody
                                            + System.lineSeparator()
                                            + newBody
                            );

                            return oldExchange;
                        }
                )

                .completionSize(3)

                .log(
                        "[TASK 3] AGGREGATOR: 3 related messages combined using Patient ID=${header.patientId}"
                )

                .log(
                        "[TASK 3] COMPLETE PATIENT MEDICAL SUMMARY:\n${body}"
                );


        // ============================================================
        // TASK 4 - XML TO JSON MESSAGE TRANSLATOR
        // ============================================================

        from("direct:translateLab")
                .routeId("task4-message-translator")

                .process(exchange -> {

                    String xml =
                            exchange.getIn()
                                    .getBody(String.class);

                    var factory =
                            DocumentBuilderFactory.newInstance();

                    // Security settings for XML parser

                    factory.setFeature(
                            "http://apache.org/xml/features/disallow-doctype-decl",
                            true
                    );

                    factory.setFeature(
                            "http://xml.org/sax/features/external-general-entities",
                            false
                    );

                    factory.setFeature(
                            "http://xml.org/sax/features/external-parameter-entities",
                            false
                    );

                    var document =
                            factory.newDocumentBuilder()
                                    .parse(
                                            new ByteArrayInputStream(
                                                    xml.getBytes(
                                                            StandardCharsets.UTF_8
                                                    )
                                            )
                                    );

                    Map<String, String> result =
                            new LinkedHashMap<>();

                    result.put(
                            "patientId",
                            text(document, "PatientId")
                    );

                    result.put(
                            "patientName",
                            text(document, "PatientName")
                    );

                    result.put(
                            "test",
                            text(document, "Test")
                    );

                    result.put(
                            "result",
                            text(document, "Result")
                    );

                    exchange.getIn().setBody(
                            mapper.writeValueAsString(result)
                    );

                })

                .log(
                        "[TASK 4] MESSAGE TRANSLATOR: XML converted to JSON"
                )

                .log(
                        "[TASK 4] [HOSPITAL JSON] ${body}"
                );


        // ============================================================
        // TASK 5 - PROCESSING ROUTE
        // ============================================================

        from("direct:processPatient")
                .routeId("task5-error-handling")

                .log(
                        "[TASK 5] Hospital Processing System attempting: ${body}"
                )

                .process(exchange -> {

                    String body =
                            exchange.getIn()
                                    .getBody(String.class);

                    Integer attempts =
                            exchange.getProperty(
                                    Exchange.REDELIVERY_COUNTER,
                                    Integer.class
                            );

                    if (attempts == null) {
                        attempts = 0;
                    }

                    // ------------------------------------------------
                    // TRANSIENT ERROR
                    // Fails twice, then succeeds
                    // ------------------------------------------------

                    if (
                            body.contains("TRANSIENT_ERROR")
                                    && attempts < 2
                    ) {

                        System.out.println(
                                "[TASK 5] Simulated temporary outage. Throwing exception..."
                        );

                        throw new RuntimeException(
                                "Temporary hospital processing outage"
                        );
                    }


                    // ------------------------------------------------
                    // PERMANENT ERROR
                    // Keeps failing and eventually goes to error queue
                    // ------------------------------------------------

                    if (body.contains("PERMANENT_ERROR")) {

                        System.out.println(
                                "[TASK 5] Simulated permanent outage. Throwing exception..."
                        );

                        throw new RuntimeException(
                                "Permanent hospital processing outage"
                        );
                    }

                })

                .log(
                        "[TASK 5] Processing successful."
                );


        // ============================================================
        // TASK 5 - ERROR QUEUE
        // ============================================================

        from(JMS + "hospital.error")
                .routeId("task5-error-channel")

                .log(
                        "[TASK 5] ERROR CHANNEL: failed message received from JMS error queue -> ${body}"
                )

                .log(
                        "[TASK 5] Maximum retries reached. Message is now available for manual review."
                );
    }


    // ================================================================
    // XML HELPER
    // ================================================================

    private String text(
            org.w3c.dom.Document document,
            String tag
    ) {

        var nodes =
                document.getElementsByTagName(tag);

        if (nodes.getLength() == 0) {
            return "";
        }

        return nodes.item(0).getTextContent();
    }
}