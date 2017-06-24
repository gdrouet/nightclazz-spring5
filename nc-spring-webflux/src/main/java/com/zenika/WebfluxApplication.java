package com.zenika;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring reactive application bootstrap class.
 *
 * @author Guillaume DROUET
 */
@SpringBootApplication
public class WebfluxApplication {

    public static void main(final String[] args) {
        SpringApplication.run(WebfluxApplication.class, args);
    }

}
