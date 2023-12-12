package com.example.javagreeter.greeter;

import com.github.javafaker.Faker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.io.IOException;

public class Greeter {
    private final Faker faker;

    public Greeter() {
        this.faker = new Faker();
    }

    public String greet() {
        String beginning;
        try (InputStream is = this.getClass().getResourceAsStream("/projects/projectA/foo.txt")) {
            if (is == null) {
                throw new RuntimeException("Could not find resource");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                beginning = reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return beginning + this.faker.name().fullName() + "!";
    }
}
