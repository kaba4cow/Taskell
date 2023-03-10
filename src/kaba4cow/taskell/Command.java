package kaba4cow.taskell;

import java.io.File;

public enum Command {

	EXIT("exit", "", "Closes the program") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			exit = true;
		}
	},
	HELP("help", "", "Shows list of all available commands") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			for (Command command : values()) {
				output.append(command.name + " " + command.parameters + "\n");
				output.append("\t" + command.description + "\n\n");
			}
		}
	},
	ECHO("echo", "[message]", "Prints a message") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			for (int i = 0; i < numParameters; i++)
				output.append(parameters[i] + " ");
			output.append('\n');
		}
	},
	CD("cd", "[path]", "Changes current directory") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			String path = parameters[0];
			File file;
			if (path.equals("..")) {
				if (directory.getParentFile() != null)
					directory = directory.getParentFile();
			} else {
				file = new File(path);
				if (file.isDirectory())
					directory = file;
				else {
					path = directory.getAbsolutePath() + "\\" + path;
					file = new File(path);
					if (file.isDirectory())
						directory = file;
					else
						output.append(path + " is not a directory\n");
				}
			}
		}
	},
	MD("md", "[name]", "Creates new directory") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			String name = parameters[0];
			File file = new File(directory.getAbsolutePath() + "/" + name);
			if (!file.mkdirs())
				output.append("Could not create directory\n");
		}
	},
	DIR("dir", "", "Lists all files in current directory") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			File[] files = directory.listFiles();
			for (File file : files)
				output.append("-> " + file.getName() + "\n");
		}
	},
	PROJ_CREATE("proj-create", "[name]", "Creates a project with specified name") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			project = new Project(parameters[0]);
		}
	},
	PROJ_OPEN("proj-open", "[name]", "Opens a project with specified name") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			Project project = Project.load(directory.getAbsolutePath(), parameters[0]);
			if (project == null)
				output.append("File not found\n");
			else
				Command.project = project;
		}
	},
	PROJ_SAVE("proj-save", "", "Saves current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (project == null)
				output.append("No project selected\n");
			else if (!project.save(directory.getAbsolutePath()))
				output.append("Could not save the project\n");
		}
	},
	PROJ_RENAME("proj-rename", "[name]", "Renames current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else {
				String name = parameters[0];
				project.setName(name);
			}
		}
	},
	PROJ_DESCRIPTION("proj-desc", "[description]", "Adds description to the current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else {
				String description = parameters[0];
				project.setDescription(description);
			}
		}
	},
	PROJ_INFO("proj-info", "", "Lists all tasks in the current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (project == null)
				output.append("No project selected\n");
			else {
				output.append("Project: " + project.getName() + "\n");
				output.append("Description: " + project.getDescription() + "\n");
				if (project.getNumberOfTasks() == 0) {
					output.append("Tasks: none\n");
					return;
				}
				output.append("Tasks (" + project.getNumberOfTasks() + "):\n\n");
				output.append(String.format("%5s | %8s | %11s | %s ", "Index", "Priority", "Status", "Description"));
				output.append('\n');
				for (int i = 0; i < 45; i++)
					output.append('-');
				output.append('\n');
				for (int i = 0; i < project.getNumberOfTasks(); i++) {
					Task task = project.getTask(i);
					output.append(String.format("%5s | %8s | %11s | %s ", i, Task.priorities[task.getPriority()],
							task.getStatus().getName(), task.getDescription()));
					output.append('\n');
				}
			}
		}
	},
	PROJ_SORT("proj-sort", "[parameter]",
			"Sorts tasks in the current project (s - by status, p - by priority, d - by description)") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else if (parameters[0].equalsIgnoreCase("s"))
				project.sortByStatus();
			else if (parameters[0].equalsIgnoreCase("p"))
				project.sortByPriority();
			else if (parameters[0].equalsIgnoreCase("d"))
				project.sortByDescription();
			else
				invalidParameters();
		}
	},
	TASK_ADD("task-add", "[name]", "Creates new task in the current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (project == null)
				output.append("No project selected\n");
			else if (!project.addTask(parameters[0]))
				output.append("This task already exists\n");
		}
	},
	TASK_REMOVE("task-remove", "[index]", "Removes a task from project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else
				try {
					int index = Integer.parseInt(parameters[0]);
					project.removeTask(index);
				} catch (Exception e) {
					invalidParameters();
				}
		}
	},
	TASK_STATUS("task-status", "[index] [status]",
			"Sets a status of specified task (none - n, in progress - p, done - d)") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 2))
				return;

			if (project == null)
				output.append("No project selected\n");
			else
				try {
					int index = Integer.parseInt(parameters[0]);
					Status status;
					if (parameters[1].equalsIgnoreCase("n"))
						status = Status.NONE;
					else if (parameters[1].equalsIgnoreCase("p"))
						status = Status.PROGRESS;
					else if (parameters[1].equalsIgnoreCase("d"))
						status = Status.DONE;
					else {
						invalidParameters();
						return;
					}
					project.getTask(index).setStatus(status);
				} catch (Exception e) {
					invalidParameters();
				}
		}
	},
	TASK_PRIORITY("task-priority", "[index] [priority]", "Sets priority of specified task (low, normal, high)") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 2))
				return;

			if (project == null)
				output.append("No project selected\n");
			else
				try {
					int index = Integer.parseInt(parameters[0]);
					for (int priority = 0; priority < Task.priorities.length; priority++)
						if (Task.priorities[priority].equalsIgnoreCase(parameters[1]))
							project.getTask(index).setPriority(priority);
				} catch (Exception e) {
					invalidParameters();
				}
		}
	},
	TASK_DESCRIPTION("task-desc", "[index] [description]", "Adds description to the specified task") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 2))
				return;

			if (project == null)
				output.append("No project selected\n");
			else
				try {
					int index = Integer.parseInt(parameters[0]);
					String description = parameters[1];
					project.getTask(index).setDescription(description);
				} catch (Exception e) {
					invalidParameters();
				}
		}
	};

	private final String name;
	private final String parameters;
	private final String description;

	private static String[] parameterArray = new String[32];
	private static boolean exit = false;
	private static Project project = null;
	private static File directory = new File(System.getProperty("user.dir"));

	private static StringBuilder output = new StringBuilder();

	private Command(String name, String parameters, String description) {
		this.name = name;
		this.parameters = parameters;
		this.description = description;
	}

	public abstract void execute(String[] parameters, int numParameters);

	public static boolean processCommand(String line) {
		output.append('\n');
		String name = getCommandName(line);
		int numParameters = getCommandParameters(name, line);

		Command command = Command.search(name);

		if (line.isEmpty())
			output.append('\n');
		else if (command == null)
			output.append("\"" + line + "\" is an unknown command\n");
		else
			command.execute(parameterArray, numParameters);
		output.append('\n');

		if (exit)
			return true;

		if (project == null)
			output.append(directory.getAbsolutePath() + ": ");
		else
			output.append(directory.getAbsolutePath() + " -> " + project.getName() + ": ");

		return false;
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
		boolean backslash = false;
		boolean space = false;
		String token = "";

		for (int i = 1; i < length; i++) {
			char c = string.charAt(i);
			if (!space && !backslash && c == ' ') {
				parameterArray[index++] = token;
				token = "";
				space = true;
			} else if (c == '\\') {
				backslash = true;
				space = false;
			} else {
				token += c;
				space = false;
				backslash = false;
			}

			if (index >= parameterArray.length)
				break;
		}

		for (int i = index; i < parameterArray.length; i++)
			parameterArray[i] = null;

		return index;
	}

	public static String getOutput() {
		String string = output.toString();
		output = new StringBuilder();
		return string;
	}

	public static Command search(String name) {
		for (Command command : values())
			if (command.name.equalsIgnoreCase(name))
				return command;
		return null;
	}

	private static boolean invalidParameters(int numParameters1, int numParameters2) {
		if (numParameters1 == numParameters2)
			return false;
		invalidParameters();
		return true;
	}

	private static void invalidParameters() {
		output.append("Invalid parameters\n");
	}

}
