package pl.pk.pietrzak.ogryzek.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.pk.pietrzak.ogryzek.entity.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy integracyjne dla TaskController.
 * Obejmują całą warstwę aplikacji: Controller → Service → Repository
 */
@SpringBootTest
@Transactional
@DisplayName("Testy integracyjne TaskController")
class TaskIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =============== TESTY CRUD ===============

    @Test
    @DisplayName("Powinien utworzyć nowe zadanie przez API")
    void shouldCreateTaskThroughAPI() throws Exception {
        // Przygotowanie: Utwórz projekt
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        // Test: Utwórz zadanie
        Task task = new Task();
        task.setName("Nowe zadanie");
        task.setDescription("Opis zadania");
        task.setTaskType(TaskType.IMPROVEMENT);
        task.setProject(savedProject);

        String jsonRequest = objectMapper.writeValueAsString(task);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Nowe zadanie"))
                .andExpect(jsonPath("$.description").value("Opis zadania"));
    }

    @Test
    @DisplayName("Powinien pobrać wszystkie zadania z bazy")
    void shouldGetAllTasksFromDatabase() throws Exception {
        // Przygotowanie
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        Task task1 = new Task();
        task1.setName("Zadanie 1");
        task1.setDescription("Opis 1");
        task1.setTaskType(TaskType.BUG);
        task1.setProject(savedProject);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setName("Zadanie 2");
        task2.setDescription("Opis 2");
        task2.setTaskType(TaskType.FEATURE);
        task2.setProject(savedProject);
        taskRepository.save(task2);

        // Test
        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItems("Zadanie 1", "Zadanie 2")));
    }

    @Test
    @DisplayName("Powinien pobrać zadanie po ID")
    void shouldGetTaskById() throws Exception {
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setName("Zadanie do pobrania");
        task.setDescription("Opis");
        task.setTaskType(TaskType.IMPROVEMENT);
        task.setProject(savedProject);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/{id}", savedTask.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.name").value("Zadanie do pobrania"));
    }

    @Test
    @DisplayName("Powinien zaktualizować zadanie")
    void shouldUpdateTask() throws Exception {
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setName("Oryginalne zadanie");
        task.setDescription("Oryginalny opis");
        task.setTaskType(TaskType.FEATURE);
        task.setProject(savedProject);
        Task savedTask = taskRepository.save(task);

        Task updatedTask = new Task();
        updatedTask.setName("Zaktualizowane zadanie");
        updatedTask.setDescription("Zaktualizowany opis");
        updatedTask.setTaskType(TaskType.IMPROVEMENT);
        updatedTask.setProject(savedProject);

        String jsonRequest = objectMapper.writeValueAsString(updatedTask);

        mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                .contentType("application/json")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zaktualizowane zadanie"))
                .andExpect(jsonPath("$.taskType").value("IMPROVEMENT"));
    }

    @Test
    @DisplayName("Powinien usunąć zadanie")
    void shouldDeleteTask() throws Exception {
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setName("Zadanie do usunięcia");
        task.setTaskType(TaskType.FEATURE);
        task.setProject(savedProject);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/{id}", savedTask.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // =============== TESTY RELACJI ===============

    @Test
    @DisplayName("Powinien przypisać użytkownika do zadania")
    void shouldAssignUserToTask() throws Exception {
        // Przygotowanie
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        Users user = new Users();
        user.setUsername("testuser");
        Users savedUser = userRepository.save(user);

        Task task = new Task();
        task.setName("Zadanie do przypisania");
        task.setTaskType(TaskType.IMPROVEMENT);
        task.setProject(savedProject);
        task.setAssignedUser(savedUser);
        Task savedTask = taskRepository.save(task);

        // Test
        mockMvc.perform(get("/api/tasks/{id}", savedTask.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUser.username").value("testuser"));
    }

    @Test
    @DisplayName("Powinien pobrać wszystkie zadania dla projektu")
    void shouldGetAllTasksForProject() throws Exception {
        // Przygotowanie
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        Task task1 = new Task();
        task1.setName("Zadanie 1");
        task1.setTaskType(TaskType.IMPROVEMENT);
        task1.setProject(savedProject);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setName("Zadanie 2");
        task2.setTaskType(TaskType.BUG);
        task2.setProject(savedProject);
        taskRepository.save(task2);

        // Test
        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Zadanie 1", "Zadanie 2")));
    }

    // =============== TESTY INTERAKCJI WARSTW ===============

    @Test
    @DisplayName("Powinien obsługiwać pełny cykl CRUD dla zadań")
    void shouldHandleFullCRUDCycleForTasks() throws Exception {
        // Przygotowanie: Utwórz projekt
        Project project = new Project();
        project.setName("CRUD Test Project");
        Project savedProject = projectRepository.save(project);

        // CREATE
        Task task = new Task();
        task.setName("CRUD Test Task");
        task.setDescription("Test CRUD");
        task.setTaskType(TaskType.FEATURE);
        task.setProject(savedProject);

        String createJson = objectMapper.writeValueAsString(task);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(createJson))
                .andExpect(status().isOk());

        // READ
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("CRUD Test Task")));

        // UPDATE
        Task updatedTask = new Task();
        updatedTask.setName("CRUD Test Task Updated");
        updatedTask.setDescription("Test CRUD Updated");
        updatedTask.setTaskType(TaskType.IMPROVEMENT);
        updatedTask.setProject(savedProject);

        Task savedTask = taskRepository.findAll().stream()
                .filter(t -> t.getName().equals("CRUD Test Task"))
                .findFirst()
                .orElseThrow();

        String updateJson = objectMapper.writeValueAsString(updatedTask);

        mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CRUD Test Task Updated"));

        // DELETE
        mockMvc.perform(delete("/api/tasks/{id}", savedTask.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Powinien zachować relacje między zadaniem a projektem")
    void shouldMaintainTaskProjectRelationship() throws Exception {
        // Przygotowanie
        Project project = new Project();
        project.setName("Project with Tasks");
        project.setDescription("Project Description");
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setName("Related Task");
        task.setTaskType(TaskType.FEATURE);
        task.setProject(savedProject);
        Task savedTask = taskRepository.save(task);

        // Test
        mockMvc.perform(get("/api/tasks/{id}", savedTask.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.id").value(savedProject.getId()))
                .andExpect(jsonPath("$.project.name").value("Project with Tasks"));
    }

    @Test
    @DisplayName("Powinien zaakceptować różne typy zadań (IMPROVEMENT, BUG, FEATURE)")
    void shouldAcceptDifferentTaskTypes() throws Exception {
        Project project = new Project();
        project.setName("Test Project");
        Project savedProject = projectRepository.save(project);

        // Test BUG
        Task bugTask = new Task();
        bugTask.setName("Bug Task");
        bugTask.setTaskType(TaskType.BUG);
        bugTask.setProject(savedProject);

        String bugJson = objectMapper.writeValueAsString(bugTask);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(bugJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskType").value("BUG"));

        // Test FEATURE
        Task featureTask = new Task();
        featureTask.setName("Feature Task");
        featureTask.setTaskType(TaskType.FEATURE);
        featureTask.setProject(savedProject);

        String featureJson = objectMapper.writeValueAsString(featureTask);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(featureJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskType").value("FEATURE"));

        // Test IMPROVEMENT
        Task improvementTask = new Task();
        improvementTask.setName("Improvement Task");
        improvementTask.setTaskType(TaskType.IMPROVEMENT);
        improvementTask.setProject(savedProject);

        String improvementJson = objectMapper.writeValueAsString(improvementTask);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(improvementJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskType").value("IMPROVEMENT"));
    }
}
