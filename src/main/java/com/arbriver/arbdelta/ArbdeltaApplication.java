package com.arbriver.arbdelta;

import com.arbriver.arbdelta.app.container.DefaultAppContainer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArbdeltaApplication implements CommandLineRunner {
	private final DefaultAppContainer container;

	public ArbdeltaApplication(DefaultAppContainer container) {
		this.container = container;
	}

	public static void main(String[] args) {
		SpringApplication.run(ArbdeltaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		container.start();
	}
}
