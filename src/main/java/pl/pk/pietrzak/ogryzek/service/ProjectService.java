package pl.pk.pietrzak.ogryzek.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

    public Project updateProject(Long id, Project projectData) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt o podanym id nie istnieje"));

        existingProject.setName(projectData.getName());
        existingProject.setDescription(projectData.getDescription());
        existingProject.setUsers(projectData.getUsers());

        return projectRepository.save(existingProject);
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt o podanym id nie istnieje");
        }
        projectRepository.deleteById(id);
    }
}