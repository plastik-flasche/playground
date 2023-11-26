package com.example.javagreeter.greeter;

import com.github.javafaker.Faker;

public class Greeter {
    private final Faker faker;

    public Greeter() {
        this.faker = new Faker();
    }

    public String greet() {
        return "Hello, " + this.faker.name().fullName() + "!";
    }
}
