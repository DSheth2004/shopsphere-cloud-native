package com.shopsphere.report;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Batch job run by a Kubernetes CronJob; it starts, writes the daily report line and exits. */
@SpringBootApplication
public class ReportJobApplication {
    private static final Logger log = LoggerFactory.getLogger(ReportJobApplication.class);

    public static void main(String[] args) { System.exit(SpringApplication.exit(SpringApplication.run(ReportJobApplication.class, args))); }

    @Bean CommandLineRunner report() {
        return args -> log.info("Daily sales report generated for {}", LocalDate.now().minusDays(1));
    }
}
