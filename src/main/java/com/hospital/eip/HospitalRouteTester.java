package com.hospital.eip;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class HospitalRouteTester implements CommandLineRunner {

    @Override
    public void run(String... args) {

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("              HOSPITAL EIP SYSTEM");
        System.out.println("==============================================================");
        System.out.println("Apache Camel + Spring Boot + JMS + ActiveMQ");
        System.out.println("Hospital EIP Web Interface is now running.");
        System.out.println();
        System.out.println("Open your browser:");
        System.out.println("http://localhost:8080");
        System.out.println();
        System.out.println("Available EIP Tasks:");
        System.out.println("1. Message Channel");
        System.out.println("2. Content-Based Router");
        System.out.println("3. Aggregator");
        System.out.println("4. XML to JSON Message Translator");
        System.out.println("5. Error Handling and Retry");
        System.out.println("6. EIP Architecture");
        System.out.println("==============================================================");
        System.out.println();
    }
}
