package com.azarul.portcleanup;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

@Service
public class CommandExecutorService {

	public String executeCommand(String command) {
		StringBuilder output = new StringBuilder();
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("cmd", "/c", command);

			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}

			//Capture any errors
			StringBuilder error = new StringBuilder();
			try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				String lineError;
				while ((lineError = errorReader.readLine()) != null) {
					error.append(lineError).append("\n");
				}
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				return output.toString();
			} else if(exitCode == 1) {
				String portNumber = command.trim().split("\\s+")[command.trim().split("\\s+").length - 1];
				return "Oops!! try checking port number => ["+portNumber+"].\nPerhaps the PID associated with it may be killed earlier or the port is incorrect.\nCommand execution failed with error code: " + exitCode +"\n";
			}
			else {
				return "[Error] : "+error.toString();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return "Error executing command: " + e.getMessage();
		}
	}

	// Extracts PID from the command output and kills it
	public String extractAndKillPID(String netstatOutput, String portNumber) {
		String pid = extractPID(netstatOutput, portNumber);
		if (!pid.equals("PID not found")) {
			String killCommand = "taskkill /F /PID " + pid; // Windows command to kill PID
			return executeCommand(killCommand);
		}
		return "PID not found";
	}

	// Extracts the PID from the output
	private String extractPID(String output, String portNumber) {
		String[] lines = output.split("\n");
		for (String line : lines) {
			if (!line.startsWith("Oops!!") && line.contains(portNumber)) {
				String[] columns = line.trim().split("\\s+");
				if (columns.length > 4) {
					return columns[columns.length - 1]; // PID is the last element
				}
			}
		}
		return "PID not found";
	}
}
