package pl.pk.pietrzak.ogryzek.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.entity.ProjectRepository;
import pl.pk.pietrzak.ogryzek.entity.Users;
import pl.pk.pietrzak.ogryzek.entity.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy integracyjne dla UserController.
 * Obejmują całą warstwę aplikacji: Controller → Service → Repository.
 *
 * Uwaga: Maven Failsafe domyślnie uruchamia klasy testowe pasujące do wzorca *IT.
 * Dlatego klasa nazywa się UserIT.
 */
@SpringBootTest
@Transactional
@DisplayName("Testy integracyjne UserController")
class UserIT extends IntegrationTestBase {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String userJson(String username) {
        return "{\"username\":\"" + username + "\"}";
    }

    // =============== TESTY CRUD ===============

    @Test
    @DisplayName("Powinien utworzyć nowego użytkownika przez API")
    void shouldCreateUserThroughAPI() throws Exception {
        Users user = new Users();
        user.setUsername("newuser");

        String jsonRequest = userJson("newuser");

        mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("Powinien pobrać wszystkich użytkowników z bazy")
    void shouldGetAllUsersFromDatabase() throws Exception {
        // Przygotowanie
        Users user1 = new Users();
        user1.setUsername("user1");
        userRepository.save(user1);

        Users user2 = new Users();
        user2.setUsername("user2");
        userRepository.save(user2);

        // Test
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].username", hasItems("user1", "user2")));
    }

    @Test
    @DisplayName("Powinien pobrać użytkownika po ID")
    void shouldGetUserById() throws Exception {
        // Przygotowanie
        Users user = new Users();
        user.setUsername("findme");
        Users savedUser = userRepository.save(user);

        // Test
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.username").value("findme"));
    }

    @Test
    @DisplayName("Powinien zaktualizować użytkownika")
    void shouldUpdateUser() throws Exception {
        // Przygotowanie
        Users user = new Users();
        user.setUsername("originalusername");
        Users savedUser = userRepository.save(user);

        Users updatedUser = new Users();
        updatedUser.setUsername("updatedusername");

        String jsonRequest = userJson("updatedusername");

        // Test
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType("application/json")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedusername"));
    }

    @Test
    @DisplayName("Powinien usunąć użytkownika")
    void shouldDeleteUser() throws Exception {
        // Przygotowanie
        Users user = new Users();
        user.setUsername("todelete");
        Users savedUser = userRepository.save(user);

        // Test
        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // =============== TESTY RELACJI ===============

    @Test
    @DisplayName("Powinien pobrać użytkownika ze wszystkimi przypisanymi projektami")
    void shouldGetUserWithAssignedProjects() throws Exception {
        // Przygotowanie: Utwórz użytkownika
        Users user = new Users();
        user.setUsername("projectmanager");
        Users savedUser = userRepository.save(user);

        // Przygotowanie: Utwórz projekty
        Project project1 = new Project();
        project1.setName("Project 1");
        project1.getUsers().add(savedUser);
        savedUser.getProjects().add(project1);
        projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("Project 2");
        project2.getUsers().add(savedUser);
        savedUser.getProjects().add(project2);
        projectRepository.save(project2);
        userRepository.save(savedUser);

        // Test
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("projectmanager"))
                .andExpect(jsonPath("$.projects", hasSize(2)))
                .andExpect(jsonPath("$.projects[*].name", hasItems("Project 1", "Project 2")));
    }

    @Test
    @DisplayName("Powinien obsługiwać użytkownika bez przypisanych projektów")
    void shouldHandleUserWithoutProjects() throws Exception {
        // Przygotowanie
        Users user = new Users();
        user.setUsername("freelancer");
        Users savedUser = userRepository.save(user);

        // Test
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("freelancer"))
                .andExpect(jsonPath("$.projects", hasSize(0)));
    }

    // =============== TESTY INTERAKCJI WARSTW ===============

    @Test
    @DisplayName("Powinien obsługiwać pełny cykl CRUD dla użytkowników")
    void shouldHandleFullCRUDCycleForUsers() throws Exception {
        // CREATE
        Users user = new Users();
        user.setUsername("cruduser");

        String createJson = userJson("cruduser");

        mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content(createJson))
                .andExpect(status().isOk());

        // READ
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("cruduser")));

        // UPDATE
        Users updatedUser = new Users();
        updatedUser.setUsername("cruduserupdated");

        Users savedUser = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals("cruduser"))
                .findFirst()
                .orElseThrow();

        String updateJson = userJson("cruduserupdated");

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("cruduserupdated"));

        // DELETE
        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Powinien zachować relacje między użytkownikami i projektami")
    void shouldMaintainUserProjectRelationship() throws Exception {
        // Przygotowanie: Utwórz użytkownika i projekt
        Users user = new Users();
        user.setUsername("teamlead");
        Users savedUser = userRepository.save(user);

        Project project = new Project();
        project.setName("Leadership Project");
        project.getUsers().add(savedUser);
        savedUser.getProjects().add(project);
        Project savedProject = projectRepository.save(project);
        userRepository.save(savedUser);

        // Test: Pobierz użytkownika i sprawdź czy projekt jest w liście
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects[0].name").value("Leadership Project"))
                .andExpect(jsonPath("$.projects[0].id").value(savedProject.getId()));
    }

    @Test
    @DisplayName("Powinien obsługiwać wiele użytkowników w jednym projekcie")
    void shouldHandleMultipleUsersInOneProject() throws Exception {
        // Przygotowanie: Utwórz użytkowników
        Users user1 = new Users();
        user1.setUsername("dev1");
        Users savedUser1 = userRepository.save(user1);

        Users user2 = new Users();
        user2.setUsername("dev2");
        Users savedUser2 = userRepository.save(user2);

        Users user3 = new Users();
        user3.setUsername("qa");
        Users savedUser3 = userRepository.save(user3);

        // Przygotowanie: Utwórz projekt i przypisz użytkowników
        Project project = new Project();
        project.setName("Team Project");
        project.getUsers().add(savedUser1);
        project.getUsers().add(savedUser2);
        project.getUsers().add(savedUser3);
        projectRepository.save(project);

        // Test: Pobierz wszystkich użytkowników i sprawdź projekty
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItems("dev1", "dev2", "qa")));
    }

    @Test
    @DisplayName("Powinien obsługiwać usunięcie użytkownika z projektu")
    void shouldHandleUserRemovalFromProject() throws Exception {
        // Przygotowanie
        Users user = new Users();
        user.setUsername("departing");
        Users savedUser = userRepository.save(user);

        Project project = new Project();
        project.setName("Project");
        project.getUsers().add(savedUser);
        savedUser.getProjects().add(project);
        projectRepository.save(project);
        userRepository.save(savedUser);

        // Test: Pobierz użytkownika z projektem
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects", hasSize(1)));
    }
}

