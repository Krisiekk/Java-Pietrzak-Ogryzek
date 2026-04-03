package pl.pk.pietrzak.ogryzek.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.entity.ProjectRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.server.ResponseStatusException;

public class ProjectServiceTest {

    private ProjectRepository projectRepository;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        projectService = new ProjectService(projectRepository);
    }

    @Test
    @DisplayName("Powinien zwrocic wszystkie projekty")
    void shouldReturnAllProjects() {
        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("Project 1");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Project 2");

        when(projectRepository.findAll()).thenReturn(Arrays.asList(project1, project2));

        List<Project> result = projectService.getAllProjects();

        assertEquals(2, result.size());
        assertEquals("Project 1", result.get(0).getName());
        assertEquals("Project 2", result.get(1).getName());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Powinien zwrocic projekt po id")
    void shouldReturnProjectById() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Project test");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Optional<Project> result = projectService.getProjectById(1L);

        assertTrue(result.isPresent());
        assertEquals("Project test", result.get().getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Powinien zwrocic pusty Optional gdy projekt nie istnieje")
    void shouldReturnEmptyOptionalWhenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Project> result = projectService.getProjectById(999L);

        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Powinien zapisac nowy projekt")
    void shouldCreateProject() {
        Project project = new Project();
        project.setName("Nowy projekt");
        project.setDescription("Opis projektu");
        project.setUsers(new HashSet<>());

        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectService.createProject(project);

        assertEquals("Nowy projekt", result.getName());
        assertEquals("Opis projektu", result.getDescription());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    @DisplayName("Powinien zaktualizowac projekt")
    void shouldUpdateProject() {
        Project existingProject = new Project();
        existingProject.setId(1L);
        existingProject.setName("Stary projekt");
        existingProject.setDescription("Stary opis");
        existingProject.setUsers(new HashSet<>());

        Project updatedProject = new Project();
        updatedProject.setName("Nowy projekt");
        updatedProject.setDescription("Nowy opis");
        updatedProject.setUsers(new HashSet<>());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(existingProject)).thenReturn(existingProject);

        Project result = projectService.updateProject(1L, updatedProject);

        assertEquals("Nowy projekt", result.getName());
        assertEquals("Nowy opis", result.getDescription());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(existingProject);
    }

    @Test
    @DisplayName("Powinien usunac projekt po id")
    void shouldDeleteProject() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        doNothing().when(projectRepository).deleteById(1L);

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).existsById(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Powinien rzucic blad 404 gdy projekt nie istnieje podczas usuwania")
    void shouldThrowExceptionWhenDeleteProjectNotFound() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            projectService.deleteProject(1L);
        });

        assertEquals(404, exception.getStatusCode().value());
        verify(projectRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Powinien rzucic blad gdy projekt nie istnieje podczas aktualizacji")
    void shouldThrowExceptionWhenUpdateProjectNotFound() {
        Project updatedProject = new Project();
        updatedProject.setName("Updated");

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.updateProject(1L, updatedProject);
        });

        assertEquals("Projekt nie znaleziony", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
    }
}
