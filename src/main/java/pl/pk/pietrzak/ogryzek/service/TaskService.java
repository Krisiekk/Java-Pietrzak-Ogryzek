package pl.pk.pietrzak.ogryzek.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.pk.pietrzak.ogryzek.entity.Task;
import pl.pk.pietrzak.ogryzek.entity.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task taskData) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zadanie o podanym id nie istnieje"));

        existingTask.setName(taskData.getName());
        existingTask.setDescription(taskData.getDescription());
        existingTask.setTaskType(taskData.getTaskType());
        existingTask.setProject(taskData.getProject());
        existingTask.setAssignedUser(taskData.getAssignedUser());

        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Zadanie o podanym id nie istnieje");
        }
        taskRepository.deleteById(id);
    }
}
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task updateTask(Long id, Task updatedTask) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setName(updatedTask.getName());
                    task.setDescription(updatedTask.getDescription());
                    task.setTaskType(updatedTask.getTaskType());
                    task.setProject(updatedTask.getProject());
                    task.setAssignedUser(updatedTask.getAssignedUser());
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }




}

