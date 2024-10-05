package edu.eci.cvds.Task.services;

import edu.eci.cvds.Task.TaskManagerException;
import edu.eci.cvds.Task.models.Difficulty;
import edu.eci.cvds.Task.models.Task;
import org.springframework.stereotype.Component;
import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The TaskAnalysis class is responsible for providing analytical operations and generating random task data
 * for the system. This class interacts with the TaskPersistence component to save, update, and retrieve data,
 * offering a series of methods that perform statistical analysis and metrics on tasks.
 *
 * @version 1.0
 * @since 04-10-2024
 */
@Component
public class TaskAnalysis {

    private final TaskPersistence taskPersistence;

    /**
     * Constructor for the TaskAnalysis class. It injects the TaskPersistence component to interact with the data layer.
     *
     * @param taskPersistence The persistence component used to save and retrieve task information.
     */
    public TaskAnalysis(TaskPersistence taskPersistence) {
        this.taskPersistence = taskPersistence;
    }

    /**
     * Generates a specified number of random tasks if no tasks exist in the system.
     *
     * @param counter The number of random tasks to generate.
     * @throws TaskManagerException If there is an error while interacting with the persistence layer.
     */
    public void randomData(int counter) throws TaskManagerException {
        if (isEmpty()) {
            generateAnalysis(counter);
        }
    }

    /**
     * Generates a list of random tasks using the Faker library.
     *
     * @param numberOfTasks The number of random tasks to generate.
     * @return A list of randomly generated Task objects.
     * @throws TaskManagerException If there is an error while generating tasks.
     */
    public List<Task> getRandomTasks(int numberOfTasks) throws TaskManagerException {
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfTasks; i++) {
            Faker faker = new Faker();
            boolean state = faker.bool().bool();
            String id = null;
            String name = faker.name().fullName();
            String description = faker.animal().name();
            int priority = faker.number().numberBetween(1, 6);
            int estimatedTime = faker.number().numberBetween(1, 100);
            Difficulty difficulty = generateDifficulty(faker);
            LocalDateTime dateTime = generateDate(faker);
            Task task1 = new Task(id, name, description, state, priority, estimatedTime, difficulty, dateTime);
            tasks.add(task1);
        }
        return tasks;
    }

    /**
     * Generates a random Difficulty level based on a random number generated by Faker.
     *
     * @param faker The Faker object used to generate random values.
     * @return A randomly generated Difficulty level.
     */
    private Difficulty generateDifficulty(Faker faker) {
        int number = faker.number().numberBetween(0, 3);
        if (number == 1) {
            return Difficulty.BAJA;
        } else if (number == 2) {
            return Difficulty.MEDIA;
        } else {
            return Difficulty.ALTA;
        }
    }

    /**
     * Generates a random LocalDateTime using a random Date generated by Faker.
     *
     * @param faker The Faker object used to generate a random Date.
     * @return A LocalDateTime representation of the random Date.
     */
    private LocalDateTime generateDate(Faker faker) {
        Date date = faker.date().birthday();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Checks if there are any tasks in the persistence layer.
     *
     * @return True if there are no tasks; false otherwise.
     * @throws TaskManagerException If there is an error while checking the task list.
     */
    private boolean isEmpty() throws TaskManagerException {
        return taskPersistence.findAll().isEmpty();
    }

    /**
     * Generates a specified number of tasks and saves them to the persistence layer.
     *
     * @param counter The number of tasks to generate.
     * @throws TaskManagerException If there is an error while saving the tasks.
     */
    private void generateAnalysis(int counter) throws TaskManagerException {
        List<Task> tasks = getRandomTasks(counter);
        for (Task task : tasks) {
            taskPersistence.save(task);
        }
    }

    /**
     * Generates a histogram of the number of tasks grouped by difficulty.
     *
     * @return A map where the keys are difficulties and the values are the count of tasks for each difficulty.
     * @throws TaskManagerException If there is an error while retrieving data from the persistence layer.
     */
    public Map<Difficulty, Long> getHistogram() throws TaskManagerException {
        long difficultyALTA = taskPersistence.findByDifficulty(Difficulty.ALTA).size();
        long difficultyMEDIA = taskPersistence.findByDifficulty(Difficulty.MEDIA).size();
        long difficultyBAJA = taskPersistence.findByDifficulty(Difficulty.BAJA).size();
        return Map.of(Difficulty.ALTA, difficultyALTA, Difficulty.MEDIA, difficultyMEDIA, Difficulty.BAJA, difficultyBAJA);
    }

    /**
     * Retrieves the number of finished tasks grouped by their estimated time to complete.
     *
     * @return A map where the keys are the estimated time and the values are the count of finished tasks for each time.
     * @throws TaskManagerException If there is an error while retrieving data from the persistence layer.
     */
    public Map<Integer, Long> getFinishedTasks() throws TaskManagerException {
        return taskPersistence.findByState(true).stream()
                .collect(Collectors.groupingBy(
                        Task::getEstimatedTime,
                        Collectors.counting()
                ));
    }

    /**
     * Calculates the average number of tasks grouped by priority.
     * Note: The average is calculated by dividing the total count by 3 since there are three priorities (1, 2, and 3).
     *
     * @return A map where the keys are priorities and the values are the average number of tasks for each priority.
     * @throws TaskManagerException If there is an error while retrieving data from the persistence layer.
     */
    public Map<Integer, Double> getConsolidatedPriority() throws TaskManagerException {
        Map<Integer, Double> res = new HashMap<>();
        List<Task> totalTasks = taskPersistence.findAll();
        Map<Integer, Long> tasksGrouped = totalTasks.stream()
                .collect(Collectors.groupingBy(
                        Task::getPriority,
                        Collectors.counting()
                ));

        for (Map.Entry<Integer, Long> entry : tasksGrouped.entrySet()) {
            res.put(entry.getKey(), (double) entry.getValue());
        }

        return res;
    }

    /**
     * Calculates the total time spent on tasks grouped by their difficulty.
     *
     * @return A map where the keys are difficulties and the values are the total estimated time spent on tasks of each difficulty.
     * @throws TaskManagerException If there is an error while retrieving data from the persistence layer.
     */
    public Map<Difficulty, Double> getTotalTimeSpentByDifficulty() throws TaskManagerException {
        List<Task> allTasks = taskPersistence.findByState(true);
        return allTasks.stream().collect(Collectors.groupingBy(
                Task::getDifficulty,
                Collectors.summingDouble(Task::getEstimatedTime)
        ));
    }

    /**
     * Deletes all tasks from the system.
     */
    public void deleteAllTasks() {
        taskPersistence.deleteAll();
    }
}
