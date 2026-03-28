package pl.pk.pietrzak.ogryzek.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import pl.pk.pietrzak.ogryzek.entity.Project;
import pl.pk.pietrzak.ogryzek.entity.ProjectRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }
    public Optional <Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }
    public  void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public Project updateProject(Long id, Project updatedProject) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setName(updatedProject.getName());
                    project.setDescription(updatedProject.getDescription());
                    return projectRepository.save(project);
                })
                .orElseThrow(() -> new RuntimeException("Projekt nie znaleziony"));
    }
}