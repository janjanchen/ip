package Duke;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * A Parser that deals with the user commands on the Task List.
 */
public class Parser {
    private TaskList tasks;
    private Storage storage;

    /**
     * A public constructor to create a parser that takes in a TaskList
     * and a file of which the TaskList is stored.
     * @param tasks The task list of the user.
     * @param storage The file where the task list is stored.
     */
    public Parser(TaskList tasks, Storage storage) {
        this.tasks = tasks;
        this.storage = storage;
    }


    /**
     * Returns the storage where the task list is saved.
     * @return The storage where the task list is saved
     */
    public Storage getStorage() {
        return this.storage;
    }


    /**
     * Ends the user's session with Duke with a farewell message.
     * @return Farewell message to the user.
     */
    public String endSession() {
        return "Bye. Hope to see you again soon!";
    }


    /**
     * Gives summary of the available commands for Duke.
     * @return summary of Duke's command to the user.
     */
    public String helpMessage() {
        return "Hello! I'm Duke\n" +
                "To add a Todo, type -> todo <Description> \n" +
                "To add a Deadline, type -> deadline <Description> /by <deadline>\n" +
                "To add an Event, type -> event <Description> /at <details>\n" +
                "To mark as done, type -> done <task list index>\n" +
                "To see all of your tasks, type -> list\n" +
                "To end session, type -> bye\n" +
                "What can I do for you today?";
    }


    /**
     * Filtes the tasks that includes the keyword.
     * @param input The keyword.
     * @throws InvalidFormatException Thrown when the format of the query is invalid.
     */
    public String find(String input) throws InvalidFormatException {
        if (input.length() <= 5) {
            throw new InvalidFormatException("Please specify the keyword to be searched!");
        }

        String keyword = input.substring(5);
        ArrayList<Task> original = tasks.inArrayList();
        ArrayList<Task> filtered = new ArrayList<>();
        int taskSize = tasks.arrSize();

        for (int i = 0; i < taskSize; i++) {
            String[] target = original.get(i).toString().split("--");
            if (target[0].substring(7).contains(keyword)) {
                filtered.add(original.get(i));
            }
        }

        if (filtered.size() < 1) {
            return "There are no matching results!";
        } else {
            String result = "Here are the matching tasks in your list:\n";
            int filteredLen = filtered.size();
            for (int i = 1; i < filteredLen + 1; i++) {
                String task = i + ". " + filtered.get(i - 1).toString() + " \n";
                result = result + task;
            }
            return result;
        }
    }

    /**
     * Marks a specified task as completed.
     * @param input The task index.
     * @throws InvalidTaskIndexException Thrown when the task index is invalid.
     * @return Returns the string representation of the response from Duke.
     */
    public String done(String input) throws InvalidTaskIndexException {
        if (input.length() <= 5 || !Character.isDigit(input.charAt(5))) {
            throw new InvalidTaskIndexException("Please specify the task index to be marked as done!");
        }

        try {
            int pos = Integer.parseInt(input.substring(5)) - 1;
            int taskLen = tasks.arrSize();
            if (pos < 0) {
                throw new InvalidTaskIndexException("Task list index starts from 1!");
            }

            if (pos >= taskLen) {
                throw new InvalidTaskIndexException("There are only " + taskLen + " tasks!");
            }

            String response = tasks.markDone(pos);
            storage.writeToFile(tasks.inArrayList());
            return response;

        } catch (NumberFormatException nfe) {
            throw new InvalidTaskIndexException("A task index should only contain numbers!");
        } catch (IOException e) {
            return "Something went wrong: " + e.getMessage();
        }
    }

    /**
     * Adds a new task to the task list.
     * @param input The details of the user's input.
     * @throws InvalidFormatException Thrown when the input format is invalid.
     * @throws DukeException Thrown when Duke encounters errors.
     * @throws InvalidTaskIndexException Thrown when the task index is invalid.
     */
    public String add(String input) throws InvalidFormatException, DukeException, InvalidTaskIndexException {
        try {
            if (input.startsWith("todo ") || input.equals("todo")) {
                if (input.length() <= 5) {
                    throw new InvalidFormatException("OOPS!!! The description of a todo cannot be empty.");
                }

                String response = tasks.addTodo(input.substring(5));
                storage.writeToFile(tasks.inArrayList());
                return response;


            } else if (input.startsWith("deadline ") || input.equals("deadline")) {
                if (!input.contains(" /by ") || !input.contains("/by")) {
                    throw new InvalidFormatException("OOPS!! To add a Deadline, " +
                            "type -> deadline <Description> /by <deadline>!");
                }
                if (input.length() <= 12) {
                    throw new InvalidFormatException("OOPS!!! The descriptions of a deadline cannot be empty.");
                }

                String[] spl = input.substring(9).split("/");
                if (spl[0].length() < 2 || spl[1].length() < 4) {
                    throw new InvalidFormatException("OOPS!!! The descriptions of a deadline cannot be empty.");
                }

                LocalDate date = LocalDate.parse(spl[1].substring(3));
                if (date.isBefore(LocalDate.now())) {
                    throw new DukeException("The deadline was in the past!");
                }

                String response = tasks.addDeadline(spl[0], date);
                storage.writeToFile(tasks.inArrayList());
                return response;


            } else if (input.startsWith("event ") || input.equals("event")) {
                if (!input.contains(" /at ") || !input.contains("/at")) {
                    throw new InvalidTaskIndexException("OOPS!! To add an Duke.Event, " +
                            "type -> event <Description> /at <details>!");
                }
                if (input.length() <= 9) {
                    throw new InvalidFormatException("OOPS!!! The descriptions of an event cannot be empty.");
                }

                String[] spl = input.substring(6).split("/");
                if (spl[0].length() < 2 || spl[1].length() < 4) {
                        throw new InvalidFormatException("OOPS!!! The descriptions of an event cannot be empty.");
                }

                String response = tasks.addEvent(spl[0], spl[1].substring(3));
                storage.writeToFile(tasks.inArrayList());
                return response;


            } else {
                throw new DukeException("OOPS!!! I'm sorry, but I don't know what that means :-(");
            }
        } catch (IOException e) {
            return "Something went wrong: " + e.getMessage();
        } catch (DateTimeParseException e) {
            return "Please enter the date in the format of yyyy-mm-dd!";
        }
    }

    /**
     * Deletes a specified task from the task list.
     * @param input The task index.
     * @throws InvalidTaskIndexException Thrown when the task index is invalid.
     */
    public String delete(String input) throws InvalidTaskIndexException {
        if (input.length() <= 7 || !Character.isDigit(input.charAt(7))) {
            throw new InvalidTaskIndexException("Please specify the task index to be deleted!");
        }

        try {
            int pos = Integer.parseInt(input.substring(7)) - 1;
            int taskLen = tasks.arrSize();

            if (pos < 0) {
                throw new InvalidTaskIndexException("Task list index starts from 1!");
            }
            if (pos >= taskLen) {
                throw new InvalidTaskIndexException("There are only " + taskLen + " tasks!");
            }

            String response = tasks.deleteTask(pos);
            storage.writeToFile(tasks.inArrayList());
            return response;

        } catch (NumberFormatException nfe) {
            throw new InvalidTaskIndexException("A task index should only contain numbers!");
        } catch (IOException e) {
            return "Something went wrong: " + e.getMessage();
        }
    }

    /**
     * Lists out all the tasks in the task list.
     */
    public String list() {
        return tasks.listTasks();
    }
}
