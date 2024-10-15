package org.example.GraduationProject;

import org.springframework.boot.SpringApplication;

public class TestGraduationProjectApplication {

	public static void main(String[] args) {
		SpringApplication.from(GraduationProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
