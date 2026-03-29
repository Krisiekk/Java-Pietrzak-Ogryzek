package pl.pk.pietrzak.ogryzek.controller;
 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.service.ProjectService;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projekty", description = "Endpointy do zarzadzania projektami")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkie projekty", description = "Zwraca liste wszystkich projektow")
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PostMapping
    @Operation(summary = "Utworz nowy projekt", description = "Tworzy nowy projekt na podstawie przekazanych danych")
    public Project createProject(
            @Parameter(description = "Dane projektu do utworzenia", required = true)
            @RequestBody Project project) {
        return projectService.createProject(project);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj projekt", description = "Aktualizuje istniejacy projekt na podstawie id")
    public Project updateProject(
            @Parameter(description = "Id projektu", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nowe dane projektu", required = true)
            @RequestBody Project project) {
        return projectService.updateProject(id, project);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usun projekt", description = "Usuwa projekt na podstawie id")
    public void deleteProject(
            @Parameter(description = "Id projektu", required = true)
            @PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz projekt po ID", description = "Zwraca projekt o podanym ID")
    public Project getProjectById(
            @Parameter(description = "ID projektu do pobrania", required = true)
            @PathVariable Long id) {
        return projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("Projekt nie znaleziony"));
    }

}

