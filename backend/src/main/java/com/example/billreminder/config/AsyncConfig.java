package com.example.billreminder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "otpEmailExecutor")
    public Executor otpEmailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("otp-email-");
        executor.setRejectedExecutionHandler((r, e) -> {
            // Log rejection - email will not be sent but OTP is already generated
            System.err.println("OTP email task rejected: queue is full. Consider scaling up.");
        });
        executor.initialize();
        return executor;
    }
}
