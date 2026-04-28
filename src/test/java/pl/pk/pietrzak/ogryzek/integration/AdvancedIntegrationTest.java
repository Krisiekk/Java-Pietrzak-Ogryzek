package pl.pk.pietrzak.ogryzek.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
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
 * Zaawansowane testy integracyjne obejmujące skomplikowane scenariusze.
 * Testują interakcje między wszystkimi warstwami aplikacji i wszystkimi encjami.
 */
@SpringBootTest
@Transactional
// @ActiveProfiles("integration")
@DisplayName("Zaawansowane testy integracyjne - scenariusze biznesowe")
class AdvancedIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

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

    // =============== SCENARIUSZE BIZNESOWE ===============

    @Test
    @DisplayName("Scenariusz: Tworzenie projektu z zespołem i zadaniami")
    void shouldCreateCompleteProjectWithTeamAndTasks() throws Exception {
        // Krok 1: Utwórz użytkowników
        Users dev1 = new Users();
        dev1.setUsername("developer1");
        Users savedDev1 = userRepository.save(dev1);

        Users dev2 = new Users();
        dev2.setUsername("developer2");
        Users savedDev2 = userRepository.save(dev2);

        Users qa = new Users();
        qa.setUsername("qa_engineer");
        Users savedQA = userRepository.save(qa);

        // Krok 2: Utwórz projekt
        Project project = new Project();
        project.setName("E-Commerce Platform");
        project.setDescription("Platform handlu elektronicznego");

        String projectJson = objectMapper.writeValueAsString(project);

        mockMvc.perform(post("/api/projects")
                .contentType("application/json")
                .content(projectJson))
                .andDo(print())
                .andExpect(status().isOk());

        Project savedProject = projectRepository.findAll().stream()
                .filter(p -> p.getName().equals("E-Commerce Platform"))
                .findFirst()
                .orElseThrow();

        // Krok 3: Przypisz zespół do projektu
        mockMvc.perform(post("/api/projects/{id}/users/{userId}", 
                    savedProject.getId(), 
                    savedDev1.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/{id}/users/{userId}", 
                    savedProject.getId(), 
                    savedDev2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/{id}/users/{userId}", 
                    savedProject.getId(), 
                    savedQA.getId()))
                .andExpect(status().isOk());

        // Krok 4: Utwórz zadania dla projektu
        Task task1 = new Task();
        task1.setName("Implementacja API");
        task1.setDescription("Stwórz REST API");
        task1.setTaskType(TaskType.FEATURE);
        task1.setProject(savedProject);
        task1.setAssignedUser(savedDev1);

        String task1Json = objectMapper.writeValueAsString(task1);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(task1Json))
                .andExpect(status().isOk());

        Task task2 = new Task();
        task2.setName("Testy jednostkowe");
        task2.setDescription("Napisz testy dla API");
        task2.setTaskType(TaskType.IMPROVEMENT);
        task2.setProject(savedProject);
        task2.setAssignedUser(savedDev2);

        String task2Json = objectMapper.writeValueAsString(task2);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(task2Json))
                .andExpect(status().isOk());

        Task task3 = new Task();
        task3.setName("Bug: Błąd logowania");
        task3.setDescription("Użytkownicy nie mogą się zalogować");
        task3.setTaskType(TaskType.BUG);
        task3.setProject(savedProject);
        task3.setAssignedUser(savedQA);

        String task3Json = objectMapper.writeValueAsString(task3);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(task3Json))
                .andExpect(status().isOk());

        // Krok 5: Weryfikacja kompletnego scenariusza
        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("E-Commerce Platform"))
                .andExpect(jsonPath("$.users", hasSize(3)))
                .andExpect(jsonPath("$.users[*].username", 
                        hasItems("developer1", "developer2", "qa_engineer")));

        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", 
                        hasItems("Implementacja API", "Testy jednostkowe", "Bug: Błąd logowania")))
                .andExpect(jsonPath("$[*].taskType", 
                        hasItems("FEATURE", "IMPROVEMENT", "BUG")));
    }

    @Test
    @DisplayName("Scenariusz: Przypisanie użytkownika do projektu i weryfikacja relacji")
    void shouldVerifyUserProjectRelationshipIntegrity() throws Exception {
        // Przygotowanie
        Users user = new Users();
        user.setUsername("john_doe");
        Users savedUser = userRepository.save(user);

        Project project1 = new Project();
        project1.setName("Project Alpha");
        Project savedProject1 = projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("Project Beta");
        Project savedProject2 = projectRepository.save(project2);

        // Test: Przypisz użytkownika do obu projektów
        mockMvc.perform(post("/api/projects/{id}/users/{userId}",
                    savedProject1.getId(),
                    savedUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/{id}/users/{userId}",
                    savedProject2.getId(),
                    savedUser.getId()))
                .andExpect(status().isOk());

        // Weryfikacja: Sprawdź że użytkownik ma dwa projekty
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects", hasSize(2)))
                .andExpect(jsonPath("$.projects[*].name", hasItems("Project Alpha", "Project Beta")));

        // Weryfikacja: Sprawdź że oba projekty mają użytkownika
        mockMvc.perform(get("/api/projects/{id}", savedProject1.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].username").value("john_doe"));

        mockMvc.perform(get("/api/projects/{id}", savedProject2.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].username").value("john_doe"));
    }

    @Test
    @DisplayName("Scenariusz: Zarządzanie zadaniami w projekcie z wieloma użytkownikami")
    void shouldManageTasksInMultiUserProject() throws Exception {
        // Przygotowanie: Utwórz użytkowników
        Users user1 = new Users();
        user1.setUsername("alice");
        Users savedUser1 = userRepository.save(user1);

        Users user2 = new Users();
        user2.setUsername("bob");
        Users savedUser2 = userRepository.save(user2);

        // Przygotowanie: Utwórz projekt i przypisz użytkowników
        Project project = new Project();
        project.setName("Collaborative Project");
        project.getUsers().add(savedUser1);
        project.getUsers().add(savedUser2);
        Project savedProject = projectRepository.save(project);

        // Krok 1: Utwórz zadania przypisane do różnych użytkowników
        Task task1 = new Task();
        task1.setName("Frontend Development");
        task1.setTaskType(TaskType.FEATURE);
        task1.setProject(savedProject);
        task1.setAssignedUser(savedUser1);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setName("Backend Development");
        task2.setTaskType(TaskType.FEATURE);
        task2.setProject(savedProject);
        task2.setAssignedUser(savedUser2);
        taskRepository.save(task2);

        Task task3 = new Task();
        task3.setName("Database Setup");
        task3.setTaskType(TaskType.IMPROVEMENT);
        task3.setProject(savedProject);
        taskRepository.save(task3);

        // Krok 2: Pobierz wszystkie zadania dla projektu
        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", 
                        hasItems("Frontend Development", "Backend Development", "Database Setup")));

        // Krok 3: Sprawdź że zadania są przypisane do poprawnych użytkowników
        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Frontend Development')].assignedUser.username", 
                        hasItem("alice")))
                .andExpect(jsonPath("$[?(@.name=='Backend Development')].assignedUser.username", 
                        hasItem("bob")));
    }

    @Test
    @DisplayName("Scenariusz: Aktualizacja projektu i propagacja zmian")
    void shouldUpdateProjectAndPropagateChanges() throws Exception {
        // Przygotowanie
        Project project = new Project();
        project.setName("Original Name");
        project.setDescription("Original Description");
        Project savedProject = projectRepository.save(project);

        Users user1 = new Users();
        user1.setUsername("user1");
        Users savedUser1 = userRepository.save(user1);

        Users user2 = new Users();
        user2.setUsername("user2");
        Users savedUser2 = userRepository.save(user2);

        // Przypisz użytkowników
        savedProject.getUsers().add(savedUser1);
        savedProject.getUsers().add(savedUser2);
        projectRepository.save(savedProject);

        // Aktualizuj projekt
        Project updatedProject = new Project();
        updatedProject.setName("Updated Name");
        updatedProject.setDescription("Updated Description");

        String updateJson = objectMapper.writeValueAsString(updatedProject);

        mockMvc.perform(put("/api/projects/{id}", savedProject.getId())
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        // Weryfikacja że zmiany są widoczne
        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.users", hasSize(2)));
    }

    @Test
    @DisplayName("Scenariusz: Kaskadowe operacje - usunięcie projektu i jego zadań")
    void shouldHandleProjectDeletionWithTasks() throws Exception {
        // Przygotowanie
        Project project = new Project();
        project.setName("Project to Delete");
        Project savedProject = projectRepository.save(project);

        Task task1 = new Task();
        task1.setName("Task 1");
        task1.setTaskType(TaskType.FEATURE);
        task1.setProject(savedProject);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setName("Task 2");
        task2.setTaskType(TaskType.IMPROVEMENT);
        task2.setProject(savedProject);
        taskRepository.save(task2);

        // Weryfikacja że projekt i zadania istnieją
        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Task 1", "Task 2")));

        // Usuń projekt
        mockMvc.perform(delete("/api/projects/{id}", savedProject.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Scenariusz: Wiele typów zadań w projekcie")
    void shouldHandleMultipleTaskTypesInProject() throws Exception {
        // Przygotowanie
        Project project = new Project();
        project.setName("Feature Rich Project");
        Project savedProject = projectRepository.save(project);

        // Utwórz zadania różnych typów
        Task bug = new Task();
        bug.setName("Critical Bug");
        bug.setTaskType(TaskType.BUG);
        bug.setProject(savedProject);
        taskRepository.save(bug);

        Task feature = new Task();
        feature.setName("New Feature");
        feature.setTaskType(TaskType.FEATURE);
        feature.setProject(savedProject);
        taskRepository.save(feature);

        Task task = new Task();
        task.setName("Regular Task");
        task.setTaskType(TaskType.IMPROVEMENT);
        task.setProject(savedProject);
        taskRepository.save(task);

        // Weryfikacja
        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].taskType", hasItems("BUG", "FEATURE", "IMPROVEMENT")))
                .andExpect(jsonPath("$[*].name", hasItems("Critical Bug", "New Feature", "Regular Task")));
    }

    @Test
    @DisplayName("Scenariusz: Kompleksowy workflow - od projektowania do wykonania")
    void shouldSupportCompleteWorkflow() throws Exception {
        // 1. Utwórz zespół
        Users pm = new Users();
        pm.setUsername("project_manager");
        Users savedPM = userRepository.save(pm);

        Users dev = new Users();
        dev.setUsername("developer");
        Users savedDev = userRepository.save(dev);

        // 2. Utwórz projekt
        Project project = new Project();
        project.setName("Workflow Project");
        project.setDescription("Project for workflow testing");

        String projectJson = objectMapper.writeValueAsString(project);
        mockMvc.perform(post("/api/projects")
                .contentType("application/json")
                .content(projectJson))
                .andExpect(status().isOk());

        Project savedProject = projectRepository.findAll().stream()
                .filter(p -> p.getName().equals("Workflow Project"))
                .findFirst()
                .orElseThrow();

        // 3. Przypisz zespół
        mockMvc.perform(post("/api/projects/{id}/users/{userId}", 
                    savedProject.getId(), 
                    savedPM.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/{id}/users/{userId}", 
                    savedProject.getId(), 
                    savedDev.getId()))
                .andExpect(status().isOk());

        // 4. Utwórz zadania
        Task task = new Task();
        task.setName("Workflow Task");
        task.setDescription("Test task");
        task.setTaskType(TaskType.FEATURE);
        task.setProject(savedProject);
        task.setAssignedUser(savedDev);

        String taskJson = objectMapper.writeValueAsString(task);
        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content(taskJson))
                .andExpect(status().isOk());

        // 5. Weryfikacja pełnego workflow
        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Workflow Project"))
                .andExpect(jsonPath("$.users", hasSize(2)));

        mockMvc.perform(get("/api/users/{id}", savedPM.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects", hasSize(1)))
                .andExpect(jsonPath("$.projects[0].name").value("Workflow Project"));

        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Workflow Task")))
                .andExpect(jsonPath("$[*].assignedUser.username", hasItem("developer")));
    }
}

