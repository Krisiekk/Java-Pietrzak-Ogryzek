package pl.pk.pietrzak.ogryzek.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.entity.ProjectRepository;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projekty", description = "Endpointy do zarzadzania projektami")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkie projekty", description = "Zwraca liste wszystkich projektow")
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "Utworz nowy projekt", description = "Tworzy nowy projekt na podstawie przekazanych danych")
    public Project createProject(
            @Parameter(description = "Dane projektu do utworzenia", required = true)
            @RequestBody Project project) {
        return projectRepository.save(project);
    }
}