package pl.pk.pietrzak.ogryzek.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import pl.pk.pietrzak.ogryzek.entity.Task;
import pl.pk.pietrzak.ogryzek.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Zadania", description = "Endpointy do zarzadzania zadaniami")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkie zadania", description = "Zwraca liste wszystkich zadan")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    @Operation(summary = "Utworz nowe zadanie", description = "Tworzy nowe zadanie na podstawie przekazanych danych")
    public Task createTask(
            @Parameter(description = "Dane zadania do utworzenia", required = true)
            @RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj zadanie", description = "Aktualizuje istniejace zadanie na podstawie id")
    public Task updateTask(
            @Parameter(description = "Id zadania", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nowe dane zadania", required = true)
            @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usun zadanie", description = "Usuwa zadanie na podstawie id")
    public void deleteTask(
            @Parameter(description = "Id zadania", required = true)
            @PathVariable Long id) {
        taskService.deleteTask(id);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Pobierz zadanie po ID", description = "Zwraca jedno zadanie na podstawie ID")
    public Optional<Task> getTaskById(
            @Parameter(description = "ID zadania", required = true)
            @PathVariable Long id) {
        return taskService.getTaskById(id);
    }


}