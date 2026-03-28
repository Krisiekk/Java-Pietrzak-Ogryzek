package pl.pk.pietrzak.ogryzek.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
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

