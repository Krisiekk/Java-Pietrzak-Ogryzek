package pl.pk.pietrzak.ogryzek.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.pk.pietrzak.ogryzek.entity.Task;
import pl.pk.pietrzak.ogryzek.entity.TaskType;
import pl.pk.pietrzak.ogryzek.service.TaskService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TaskControllerTest {

    private MockMvc mockMvc;
    private TaskService taskService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkie zadania")
    void shouldGetAllTasks() throws Exception {
        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Task 2");

        when(taskService.getAllTasks()).thenReturn(Arrays.asList(task1, task2));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Task 1"))
                .andExpect(jsonPath("$[1].name").value("Task 2"));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @DisplayName("Powinien zwrócić zadanie po ID")
    void shouldGetTaskById() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setName("Task 1");
        task.setDescription("Description");
        task.setTaskType(TaskType.FEATURE);

        when(taskService.getTaskById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Task 1"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    @DisplayName("Powinien zwrócić pusty wynik gdy zadanie nie istnieje")
    void shouldReturnEmptyWhenTaskNotFound() throws Exception {
        when(taskService.getTaskById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isOk());

        verify(taskService, times(1)).getTaskById(999L);
    }

    @Test
    @DisplayName("Powinien utworzyć nowe zadanie")
    void shouldCreateTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setName("New Task");
        task.setDescription("New Description");
        task.setTaskType(TaskType.FEATURE);

        when(taskService.createTask(any(Task.class))).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Task"));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("Powinien zaktualizować zadanie")
    void shouldUpdateTask() throws Exception {
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setName("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setTaskType(TaskType.BUG);

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Task"));

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    @DisplayName("Powinien usunąć zadanie")
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk());

        verify(taskService, times(1)).deleteTask(1L);
    }
}

