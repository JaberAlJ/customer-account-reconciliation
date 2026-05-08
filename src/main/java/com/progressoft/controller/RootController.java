package com.progressoft.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String home() {
        return """
                <h1>Customer Account Reconciliation</h1>
                <p>Starter project for the Customer Account Reconciliation assignment</p>
                """;
    }
}
