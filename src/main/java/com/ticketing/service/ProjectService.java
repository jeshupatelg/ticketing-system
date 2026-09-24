package com.ticketing.service;

import com.ticketing.dto.CreateProjectRequest;
import com.ticketing.dto.ProjectResponse;
import com.ticketing.model.Project;
import com.ticketing.model.ProjectSequence;
import com.ticketing.repository.ProjectRepository;
import com.ticketing.repository.ProjectSequenceRepository;
import com.ticketing.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectSequenceRepository sequenceRepository;
    private final TicketRepository ticketRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectSequenceRepository sequenceRepository,
                          TicketRepository ticketRepository) {
        this.projectRepository = projectRepository;
        this.sequenceRepository = sequenceRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Project getProjectByCode(String code) {
        return projectRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with code: " + code));
    }

    public ProjectResponse getProjectResponse(String code) {
        return toResponse(getProjectByCode(code));
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (code.length() != 3 || !code.matches("^[A-Z0-9]{3}$")) {
            throw new IllegalArgumentException("Project code must be exactly 3 alphanumeric characters");
        }

        if (projectRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Project with code '" + code + "' already exists");
        }

        String photoUrl = request.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) {
            photoUrl = "/api/photos/default/project-1.svg";
        }

        Project project = new Project(code, request.getName().trim(), request.getDescription(), photoUrl);
        Project saved = projectRepository.save(project);

        // Initialize sequence
        sequenceRepository.save(new ProjectSequence(code, 0));

        return toResponse(saved);
    }

    @Transactional
    public String getNextTicketId(String projectCode) {
        String code = projectCode.toUpperCase();
        ProjectSequence sequence = sequenceRepository.findByProjectCodeForUpdate(code)
                .orElseGet(() -> new ProjectSequence(code, 0));

        long nextVal = sequence.getLastSequence() + 1;
        sequence.setLastSequence(nextVal);
        sequenceRepository.save(sequence);

        return String.format("%s-%05d", code, nextVal);
    }

    private ProjectResponse toResponse(Project project) {
        long count = ticketRepository.countByProjectCode(project.getCode());
        return new ProjectResponse(
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getPhotoUrl(),
                count,
                project.getCreatedAt()
        );
    }
}
