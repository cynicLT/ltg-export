package org.cynic.ltg_export;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Supplier;

@Component
public class Runner implements CommandLineRunner {

    private final Supplier<String> token;

    public Runner(Supplier<String> token) {
        this.token = token;
    }

    @Override
    public void run(String... args) {

        System.out.println(Arrays.toString(args));
        System.out.println(token.get());
    }
}
