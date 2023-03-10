package kaba4cow.taskell;

import java.io.File;
import java.util.Scanner;

public class Taskell {

	private static boolean exit = false;

	private static String[] parameters = new String[32];

	private static File directory;
	private static Project project;

	public static void main(String[] args) {
		System.out.println("-------- TASKELL --------\n");

		directory = new File(System.getProperty("user.dir"));

		Scanner scanner = new Scanner(System.in);

		String line, name;
		int numParameters;
		Command command;
		while (true) {
			if (project == null)
				System.out.print(directory.getAbsolutePath() + ": ");
			else
				System.out.print(directory.getAbsolutePath() + " -> " + project.getName() + ": ");
			line = scanner.nextLine();
			System.out.println();
			name = getCommandName(line);
			numParameters = getCommandParameters(name, line);

			command = Command.search(name);

			if (command == null)
				System.out.println("\"" + line + "\" is an unknown command");
			else
				command.execute(parameters, numParameters);
			System.out.println();

			if (exit)
				break;
		}

		scanner.close();
	}

	private static String getCommandName(String string) {
		String name = "";
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c == ' ')
				break;
			else
				name += c;
		}
		return name;
	}

	private static int getCommandParameters(String name, String string) {
		if (name.length() == string.length())
			return 0;

		string = string.substring(name.length()) + " ";
		final int length = string.length();

		int index = 0;
		boolean quotes = false;
		boolean space = false;
		String token = "";

		for (int i = 1; i < length; i++) {
			char c = string.charAt(i);
			if (!space && !quotes && c == ' ') {
				parameters[index++] = token;
				token = "";
				space = true;
			} else if (c == '"') {
				quotes = !quotes;
				space = false;
			} else {
				token += c;
				space = false;
			}

			if (index >= parameters.length)
				break;
		}

		for (int i = index; i < parameters.length; i++)
			parameters[i] = null;

		return index;
	}

	public static void setProject(Project project) {
		Taskell.project = project;
	}

	public static Project getProject() {
		return project;
	}

	public static File getDirectory() {
		return directory;
	}

	public static void setDirectory(File file) {
		directory = file;
	}

	public static void exit() {
		exit = true;
	}

}
