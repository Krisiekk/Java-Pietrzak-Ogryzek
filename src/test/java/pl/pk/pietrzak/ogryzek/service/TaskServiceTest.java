package pl.pk.pietrzak.ogryzek.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.entity.Task;
import pl.pk.pietrzak.ogryzek.entity.TaskRepository;
import pl.pk.pietrzak.ogryzek.entity.TaskType;
import pl.pk.pietrzak.ogryzek.entity.Users;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    @Test
    @DisplayName("Powinien zwrocic wszystkie zadania")
    void shouldReturnAllTasks() {
        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Task 2");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getName());
        assertEquals("Task 2", result.get(1).getName());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Powinien zwrocic zadanie po id")
    void shouldReturnTaskById() {
        Task task = new Task();
        task.setId(1L);
        task.setName("Task test");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getTaskById(1L);

        assertTrue(result.isPresent());
        assertEquals("Task test", result.get().getName());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Powinien zapisac nowe zadanie")
    void shouldCreateTask() {
        Task task = new Task();
        task.setName("Nowe zadanie");
        task.setDescription("Opis zadania");

        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertEquals("Nowe zadanie", result.getName());
        assertEquals("Opis zadania", result.getDescription());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    @DisplayName("Powinien zaktualizowac zadanie")
    void shouldUpdateTask() {
        Project project = new Project();
        Users user = new Users();

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setName("Stara nazwa");
        existingTask.setDescription("Stary opis");
        existingTask.setTaskType(TaskType.FEATURE);
        existingTask.setProject(project);
        existingTask.setAssignedUser(user);

        Task updatedTask = new Task();
        updatedTask.setName("Nowa nazwa");
        updatedTask.setDescription("Nowy opis");
        updatedTask.setTaskType(TaskType.BUG);
        updatedTask.setProject(project);
        updatedTask.setAssignedUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.updateTask(1L, updatedTask);

        assertEquals("Nowa nazwa", result.getName());
        assertEquals("Nowy opis", result.getDescription());
        assertEquals(TaskType.BUG, result.getTaskType());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    @DisplayName("Powinien usunac zadanie po id")
    void shouldDeleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }
}
