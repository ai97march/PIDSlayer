package com.azarul.portcleanup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PidSlayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PidSlayerApplication.class, args);
	}
	
	@Bean
	CommandLineRunner run(CommandExecutorService commandExecutorService) {
		return args -> {
			List<String> portList = new ArrayList<String>(Arrays.asList("8088,8097,8090,8099,8079,8095,8086,8082".split(",")));
			
			portList.forEach(portNumber -> {
				String netstatCommand = "netstat -ano | findstr "+portNumber;
				String netstatOutput = commandExecutorService.executeCommand(netstatCommand);
				System.out.println("\n");
				System.out.println(netstatOutput);
				String result = commandExecutorService.extractAndKillPID(netstatOutput, portNumber);
				System.out.println(result);
			});
			
		};
	}

}
