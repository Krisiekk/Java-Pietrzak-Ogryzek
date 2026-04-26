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
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.entity.ProjectRepository;
import pl.pk.pietrzak.ogryzek.entity.Users;
import pl.pk.pietrzak.ogryzek.entity.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy integracyjne dla ProjectController.
 * Obejmują całą warstwę aplikacji: Controller → Service → Repository
 */
@SpringBootTest
@Transactional
@DisplayName("Testy integracyjne ProjectController")
class ProjectIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =============== TESTY CRUD ===============

    @Test
    @DisplayName("Powinien utworzyć nowy projekt przez API")
    void shouldCreateProjectThroughAPI() throws Exception {
        Project project = new Project();
        project.setName("Nowy projekt");
        project.setDescription("Opis nowego projektu");

        String jsonRequest = objectMapper.writeValueAsString(project);

        mockMvc.perform(post("/api/projects")
                .contentType("application/json")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Nowy projekt"))
                .andExpect(jsonPath("$.description").value("Opis nowego projektu"));
    }

    @Test
    @DisplayName("Powinien pobrać wszystkie projekty z bazy danych")
    void shouldGetAllProjectsFromDatabase() throws Exception {
        // Przygotowanie danych
        Project project1 = new Project();
        project1.setName("Projekt 1");
        project1.setDescription("Opis 1");
        projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("Projekt 2");
        project2.setDescription("Opis 2");
        projectRepository.save(project2);

        // Test
        mockMvc.perform(get("/api/projects"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItems("Projekt 1", "Projekt 2")));
    }

    @Test
    @DisplayName("Powinien pobrać projekt po ID")
    void shouldGetProjectById() throws Exception {
        Project project = new Project();
        project.setName("Projekt do pobrania");
        project.setDescription("Opis");
        Project savedProject = projectRepository.save(project);

        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProject.getId()))
                .andExpect(jsonPath("$.name").value("Projekt do pobrania"));
    }

    @Test
    @DisplayName("Powinien zaktualizować projekt")
    void shouldUpdateProject() throws Exception {
        Project project = new Project();
        project.setName("Oryginalny projekt");
        project.setDescription("Oryginalny opis");
        Project savedProject = projectRepository.save(project);

        Project updatedProject = new Project();
        updatedProject.setName("Zaktualizowany projekt");
        updatedProject.setDescription("Zaktualizowany opis");

        String jsonRequest = objectMapper.writeValueAsString(updatedProject);

        mockMvc.perform(put("/api/projects/{id}", savedProject.getId())
                .contentType("application/json")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zaktualizowany projekt"))
                .andExpect(jsonPath("$.description").value("Zaktualizowany opis"));
    }

    @Test
    @DisplayName("Powinien usunąć projekt")
    void shouldDeleteProject() throws Exception {
        Project project = new Project();
        project.setName("Projekt do usunięcia");
        Project savedProject = projectRepository.save(project);

        mockMvc.perform(delete("/api/projects/{id}", savedProject.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Weryfikacja że projekt został usunięty
        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andExpect(status().isNotFound());
    }

    // =============== TESTY RELACJI ===============

    @Test
    @DisplayName("Powinien przypisać użytkownika do projektu")
    void shouldAssignUserToProject() throws Exception {
        // Przygotowanie: Utwórz użytkownika
        Users user = new Users();
        user.setUsername("testuser");
        Users savedUser = userRepository.save(user);

        // Przygotowanie: Utwórz projekt
        Project project = new Project();
        project.setName("Projekt dla użytkownika");
        project.setDescription("Opis");
        Project savedProject = projectRepository.save(project);

        // Test: Przypisz użytkownika do projektu
        mockMvc.perform(post("/api/projects/{id}/users/{userId}",
                    savedProject.getId(),
                    savedUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProject.getId()))
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Powinien pobrać projekt ze wszystkimi przypisanymi użytkownikami")
    void shouldGetProjectWithAssignedUsers() throws Exception {
        // Przygotowanie: Utwórz użytkowników
        Users user1 = new Users();
        user1.setUsername("user1");
        Users savedUser1 = userRepository.save(user1);

        Users user2 = new Users();
        user2.setUsername("user2");
        Users savedUser2 = userRepository.save(user2);

        // Przygotowanie: Utwórz projekt i przypisz użytkowników
        Project project = new Project();
        project.setName("Projekt zespołowy");
        project.setDescription("Projekt z zespołem");
        project.getUsers().add(savedUser1);
        project.getUsers().add(savedUser2);
        Project savedProject = projectRepository.save(project);

        // Test
        mockMvc.perform(get("/api/projects/{id}", savedProject.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andExpect(jsonPath("$.users[*].username", hasItems("user1", "user2")));
    }

    @Test
    @DisplayName("Powinien zwrócić 404 przy przypisywaniu nieistniejącego użytkownika")
    void shouldThrowErrorWhenAssigningNonexistentUser() throws Exception {
        Project project = new Project();
        project.setName("Projekt");
        Project savedProject = projectRepository.save(project);

        mockMvc.perform(post("/api/projects/{id}/users/{userId}",
                    savedProject.getId(),
                    999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Powinien zwrócić 404 przy operacji na nieistniejącym projekcie")
    void shouldThrowErrorForNonexistentProject() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // =============== TESTY INTERAKCJI WARSTW ===============

    @Test
    @DisplayName("Powinien zapisać projekt w bazie i pobrać go z powrotem")
    void shouldSaveProjectAndRetrieveIt() throws Exception {
        Project project = new Project();
        project.setName("Projekt do zapisania");
        project.setDescription("Testowy projekt");

        String jsonRequest = objectMapper.writeValueAsString(project);

        // Utwórz projekt
        mockMvc.perform(post("/api/projects")
                .contentType("application/json")
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Pobierz wszystkie projekty i weryfikuj że nowy projekt istnieje
        mockMvc.perform(get("/api/projects"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Projekt do zapisania")));
    }

    @Test
    @DisplayName("Powinien obsługiwać pełny cykl CRUD operacji")
    void shouldHandleFullCRUDCycle() throws Exception {
        // CREATE
        Project project = new Project();
        project.setName("CRUD Test");
        project.setDescription("Test CRUD");

        String createJson = objectMapper.writeValueAsString(project);

        mockMvc.perform(post("/api/projects")
                .contentType("application/json")
                .content(createJson))
                .andExpect(status().isOk());

        // READ - pobierz listę
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("CRUD Test")));

        // UPDATE
        Project updatedProject = new Project();
        updatedProject.setName("CRUD Test Updated");
        updatedProject.setDescription("Test CRUD Updated");

        Project savedProject = projectRepository.findAll().stream()
                .filter(p -> p.getName().equals("CRUD Test"))
                .findFirst()
                .orElseThrow();

        String updateJson = objectMapper.writeValueAsString(updatedProject);

        mockMvc.perform(put("/api/projects/{id}", savedProject.getId())
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CRUD Test Updated"));

        // DELETE
        mockMvc.perform(delete("/api/projects/{id}", savedProject.getId()))
                .andExpect(status().isOk());
    }
}

