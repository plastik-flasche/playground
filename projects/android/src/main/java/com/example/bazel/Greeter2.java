package com.example.bazel;

import com.example.javagreeter.greeter.Greeter;

/**
 * A tiny Greeter library for the Bazel Android "Hello, World" app.
 */
public class Greeter2 {
  public String sayHello() {
    //return "Hello Bazel! \uD83D\uDC4B\uD83C\uDF31"; // Unicode for 👋🌱
    return new Greeter().greet();
  }
}
