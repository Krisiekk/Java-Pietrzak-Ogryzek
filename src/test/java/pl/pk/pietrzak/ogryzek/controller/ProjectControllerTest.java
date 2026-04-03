package pl.pk.pietrzak.ogryzek.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.service.ProjectService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProjectControllerTest {

    private MockMvc mockMvc;
    private ProjectService projectService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProjectController(projectService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkie projekty")
    void shouldGetAllProjects() throws Exception {
        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("Project 1");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Project 2");

        when(projectService.getAllProjects()).thenReturn(Arrays.asList(project1, project2));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Project 1"))
                .andExpect(jsonPath("$[1].name").value("Project 2"));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @DisplayName("Powinien zwrócić projekt po ID")
    void shouldGetProjectById() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Project 1");
        project.setDescription("Description");
        project.setUsers(new HashSet<>());

        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Project 1"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    @DisplayName("Powinien utworzyć nowy projekt")
    void shouldCreateProject() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("New Project");
        project.setDescription("New Description");
        project.setUsers(new HashSet<>());

        when(projectService.createProject(any(Project.class))).thenReturn(project);

        mockMvc.perform(post("/api/projects")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Project"));

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    @Test
    @DisplayName("Powinien zaktualizować projekt")
    void shouldUpdateProject() throws Exception {
        Project updatedProject = new Project();
        updatedProject.setId(1L);
        updatedProject.setName("Updated Project");
        updatedProject.setDescription("Updated Description");
        updatedProject.setUsers(new HashSet<>());

        when(projectService.updateProject(eq(1L), any(Project.class))).thenReturn(updatedProject);

        mockMvc.perform(put("/api/projects/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updatedProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"));

        verify(projectService, times(1)).updateProject(eq(1L), any(Project.class));
    }

    @Test
    @DisplayName("Powinien usunąć projekt")
    void shouldDeleteProject() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isOk());

        verify(projectService, times(1)).deleteProject(1L);
    }
}

