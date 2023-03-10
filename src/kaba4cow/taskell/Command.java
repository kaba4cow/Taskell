package kaba4cow.taskell;

import java.io.File;

public enum Command {

	EXIT("exit", "", "Closes the program") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			Taskell.exit();
		}
	},
	HELP("help", "", "Shows list of all available commands") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			for (Command command : values()) {
				System.out.println(command.name + " " + command.parameters);
				System.out.println("\t" + command.description);
			}
		}
	},
	ECHO("echo", "[message]", "Prints a message") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			for (int i = 0; i < numParameters; i++)
				System.out.print(parameters[i] + " ");
			System.out.println();
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
				if (Taskell.getDirectory().getParentFile() != null)
					Taskell.setDirectory(Taskell.getDirectory().getParentFile());
			} else {
				file = new File(path);
				if (file.isDirectory())
					Taskell.setDirectory(file);
				else {
					path = Taskell.getDirectory().getAbsolutePath() + "\\" + path;
					file = new File(path);
					if (file.isDirectory())
						Taskell.setDirectory(file);
					else
						System.out.println(path + " is not a directory");
				}
			}
		}
	},
	DIR("dir", "", "Lists all files in current directory") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			File[] files = Taskell.getDirectory().listFiles();
			for (File file : files)
				System.out.println("-> " + file.getName());
		}
	},
	CREATE("proj-create", "[name]", "Creates a project with specified name") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			Project project = new Project(parameters[0]);
			Taskell.setProject(project);
		}
	},
	OPEN("proj-open", "[name]", "Opens a project with specified name") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			Project project = Project.load(parameters[0]);
			if (project == null)
				System.out.println();
			else
				Taskell.setProject(project);
		}
	},
	SAVE("proj-save", "", "Saves current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
			else if (!project.save())
				System.out.println("Could not save the project");
		}
	},
	PROJ_RENAME("proj-rename", "[name]", "Renames current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
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

			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
			else {
				String description = parameters[0];
				project.setDescription(description);
			}
		}
	},
	PROJ_INFO("proj-info", "", "Lists all tasks in the current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
			else {
				System.out.println("Project: " + project.getName());
				System.out.println("Description: " + project.getDescription());
				if (project.getNumberOfTasks() == 0) {
					System.out.println("Tasks: none");
					return;
				}
				System.out.println("Tasks (" + project.getNumberOfTasks() + "):\n");
				System.out.printf("%5s | %8s | %11s | %s ", "Index", "Priority", "Status", "Description");
				System.out.println();
				for (int i = 0; i < 45; i++)
					System.out.print("-");
				System.out.println();
				for (int i = 0; i < project.getNumberOfTasks(); i++) {
					Task task = project.getTask(i);
					System.out.printf("%5s | %8s | %11s | %s ", i, Task.priorities[task.getPriority()],
							task.getStatus().getName(), task.getDescription());
					System.out.println();
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

			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
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
			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
			else if (!project.addTask(parameters[0]))
				System.out.println("This task already exists");
		}
	},
	TASK_REMOVE("task-remove", "[index]", "Removes a task from project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
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

			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
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

			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
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

			Project project = Taskell.getProject();
			if (project == null)
				System.out.println("No project selected");
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

	private Command(String name, String parameters, String description) {
		this.name = name;
		this.parameters = parameters;
		this.description = description;
	}

	public abstract void execute(String[] parameters, int numParameters);

	private static boolean invalidParameters(int numParameters1, int numParameters2) {
		if (numParameters1 == numParameters2)
			return false;
		invalidParameters();
		return true;
	}

	private static void invalidParameters() {
		System.out.println("Invalid parameters");
	}

	public static Command search(String name) {
		for (Command command : values())
			if (command.name.equalsIgnoreCase(name))
				return command;
		return null;
	}

}
