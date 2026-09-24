package com.ticketing.service;

import com.ticketing.model.PredefinedPhoto;
import com.ticketing.repository.PredefinedPhotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {

    private final PredefinedPhotoRepository photoRepository;

    @Value("${app.avatars.storage-dir:./data/avatars}")
    private String avatarStorageDir;

    public PhotoService(PredefinedPhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<PredefinedPhoto> getAllPhotos(String category) {
        if (category == null || category.equalsIgnoreCase("ALL")) {
            return photoRepository.findAll();
        }
        return photoRepository.findByCategoryOrCategory(category.toUpperCase(), "BOTH");
    }

    @Transactional
    public PredefinedPhoto uploadCustomPhoto(MultipartFile file, String name, String category) throws IOException {
        if (name == null || name.isBlank()) {
            name = file.getOriginalFilename();
            if (name == null) name = "Custom Photo";
        }
        if (category == null || category.isBlank()) {
            category = "BOTH";
        }

        Path dirPath = Paths.get(avatarStorageDir).toAbsolutePath().normalize();
        Files.createDirectories(dirPath);

        String ext = "";
        String orig = file.getOriginalFilename();
        if (orig != null && orig.contains(".")) {
            ext = orig.substring(orig.lastIndexOf("."));
        }

        String fileName = "photo_" + UUID.randomUUID() + ext;
        Path targetPath = dirPath.resolve(fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "/api/photos/custom/" + fileName;
        PredefinedPhoto photo = new PredefinedPhoto(name, photoUrl, category.toUpperCase(), true);
        return photoRepository.save(photo);
    }

    public Resource loadCustomPhotoAsResource(String filename) throws MalformedURLException {
        Path filePath = Paths.get(avatarStorageDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IllegalArgumentException("Photo file not found: " + filename);
        }
    }
}
