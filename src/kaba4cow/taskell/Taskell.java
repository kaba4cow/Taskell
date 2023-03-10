package kaba4cow.taskell;

import java.util.Scanner;

public class Taskell {

	public static void main(String[] args) {
		System.out.println("-------- TASKELL --------");

		Scanner scanner = new Scanner(System.in);

		Command.processCommand("help");
		while (!Command.processCommand(scanner.nextLine())) {
		}

		scanner.close();
	}

}
