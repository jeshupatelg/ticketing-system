package com.ticketing.config;

import com.ticketing.model.PredefinedPhoto;
import com.ticketing.model.Project;
import com.ticketing.model.User;
import com.ticketing.repository.PredefinedPhotoRepository;
import com.ticketing.repository.ProjectRepository;
import com.ticketing.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PredefinedPhotoRepository photoRepository;

    @Value("${app.attachments.storage-dir:./data/attachments}")
    private String attachmentStorageDir;

    @Value("${app.avatars.storage-dir:./data/avatars}")
    private String avatarStorageDir;

    public DataInitializer(ProjectRepository projectRepository,
                           UserRepository userRepository,
                           PredefinedPhotoRepository photoRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
    }

    @Override
    public void run(String... args) {
        // Ensure storage directories exist
        new File(attachmentStorageDir).mkdirs();
        new File(avatarStorageDir).mkdirs();

        // 1. Ensure default project 'Adhocs' exists
        if (!projectRepository.existsByCodeIgnoreCase("ADH")) {
            Project adhocProject = new Project(
                    "ADH",
                    "Adhocs",
                    "Default catch-all project for ad-hoc tasks, investigations, and rapid requests",
                    "/api/photos/default/adhocs.svg"
            );
            projectRepository.save(adhocProject);
            log.info("Initialized default project: Adhocs (ADH)");
        }

        // 2. Ensure initial users exist
        if (userRepository.count() == 0) {
            userRepository.saveAll(List.of(
                    new User("admin", "admin", "System Administrator", "admin@ticketing.local", "/api/photos/default/avatar-1.svg"),
                    new User("alex", "alex", "Alex Mercer", "alex@ticketing.local", "/api/photos/default/avatar-2.svg"),
                    new User("sarah", "sarah", "Sarah Connor", "sarah@ticketing.local", "/api/photos/default/avatar-3.svg"),
                    new User("marcus", "marcus", "Marcus Wright", "marcus@ticketing.local", "/api/photos/default/avatar-4.svg")
            ));
            log.info("Initialized default users");
        }

        // 3. Ensure predefined photos exist
        if (photoRepository.count() == 0) {
            photoRepository.saveAll(List.of(
                    new PredefinedPhoto("Blue Nebula", "/api/photos/default/avatar-1.svg", "USER", false),
                    new PredefinedPhoto("Emerald Forest", "/api/photos/default/avatar-2.svg", "USER", false),
                    new PredefinedPhoto("Sunset Amber", "/api/photos/default/avatar-3.svg", "USER", false),
                    new PredefinedPhoto("Purple Pulse", "/api/photos/default/avatar-4.svg", "USER", false),
                    new PredefinedPhoto("Adhocs Lightning", "/api/photos/default/adhocs.svg", "PROJECT", false),
                    new PredefinedPhoto("Project Core", "/api/photos/default/project-1.svg", "PROJECT", false),
                    new PredefinedPhoto("Project Cloud", "/api/photos/default/project-2.svg", "PROJECT", false),
                    new PredefinedPhoto("Project Shield", "/api/photos/default/project-3.svg", "PROJECT", false)
            ));
            log.info("Initialized predefined photos");
        }
    }
}
