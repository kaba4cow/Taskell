package kaba4cow.taskell;

import java.util.Scanner;

public class Taskell {

	public static void main(String[] args) {
		System.out.print("-------- TASKELL --------");

		Scanner scanner = new Scanner(System.in);

		Command.processCommand("");
		do {
			System.out.print(Command.getOutput());
		} while (!Command.processCommand(scanner.nextLine()));

		scanner.close();
	}

}
