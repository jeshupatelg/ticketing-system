package com.ticketing.service;

import com.ticketing.model.PredefinedPhoto;
import com.ticketing.repository.PredefinedPhotoRepository;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PhotoService {

    public static final Set<String> SUPPORTED_FORMATS = Set.of("png", "jpg", "jpeg", "webp", "svg");

    private static final boolean HAS_WEBP_WRITER = ImageIO.getImageWritersByFormatName("webp").hasNext();

    private final PredefinedPhotoRepository photoRepository;

    @Value("${app.avatars.storage-dir:./data/avatars}")
    private String avatarStorageDir;

    @Value("${app.avatars.max-size-mb:${app.photos.max-size-mb:2}}")
    private long maxSizeMb;

    @Value("${app.photos.thumbnail-size:256}")
    private int thumbnailSize;

    @Value("${app.photos.thumbnail-quality:0.85}")
    private double thumbnailQuality;

    public PhotoService(PredefinedPhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public long getMaxSizeMb() {
        return maxSizeMb;
    }

    public int getThumbnailSize() {
        return thumbnailSize;
    }

    public double getThumbnailQuality() {
        return thumbnailQuality;
    }

    public Set<String> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    public List<PredefinedPhoto> getAllPhotos(String category) {
        if (category == null || category.equalsIgnoreCase("ALL")) {
            return photoRepository.findAll();
        }
        return photoRepository.findByCategoryOrCategory(category.toUpperCase(), "BOTH");
    }

    public String resolveAndValidateFormat(MultipartFile file) {
        String ext = "";
        String orig = file.getOriginalFilename();
        if (orig != null && orig.contains(".")) {
            ext = orig.substring(orig.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT).trim();
        }

        if (ext.isEmpty() || !SUPPORTED_FORMATS.contains(ext)) {
            String contentType = file.getContentType();
            if (contentType != null) {
                switch (contentType.toLowerCase(Locale.ROOT).trim()) {
                    case "image/png" -> ext = "png";
                    case "image/jpeg", "image/jpg" -> ext = "jpg";
                    case "image/webp" -> ext = "webp";
                    case "image/svg+xml" -> ext = "svg";
                }
            }
        }

        if (!SUPPORTED_FORMATS.contains(ext)) {
            throw new IllegalArgumentException(
                    "Unsupported image format: " + (ext.isEmpty() ? "unknown" : ext) +
                    ". Allowed formats are: PNG, JPG, JPEG, WEBP, SVG."
            );
        }

        return ext;
    }

    @Transactional
    public PredefinedPhoto uploadCustomPhoto(MultipartFile file, String name, String category) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty photo file.");
        }

        long maxSizeBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Photo size exceeds maximum allowed size of " + maxSizeMb + " MB.");
        }

        String ext = resolveAndValidateFormat(file);

        if (name == null || name.isBlank()) {
            name = file.getOriginalFilename();
            if (name == null) name = "Custom Photo";
        }
        if (category == null || category.isBlank()) {
            category = "BOTH";
        }

        Path dirPath = Paths.get(avatarStorageDir).toAbsolutePath().normalize();
        Files.createDirectories(dirPath);

        String outputExt;
        String fileName;
        Path targetPath;

        if ("svg".equals(ext)) {
            // Keep SVG untouched to preserve vector quality
            outputExt = "svg";
            fileName = "photo_" + UUID.randomUUID() + "." + outputExt;
            targetPath = dirPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } else {
            // Raster image: square-crop and downscale using Thumbnailator
            if ("png".equals(ext)) {
                outputExt = "png";
            } else if ("webp".equals(ext)) {
                outputExt = HAS_WEBP_WRITER ? "webp" : "png";
            } else {
                outputExt = "jpg";
            }

            fileName = "photo_" + UUID.randomUUID() + "." + outputExt;
            targetPath = dirPath.resolve(fileName);

            try (InputStream in = file.getInputStream()) {
                var builder = Thumbnails.of(in)
                        .size(thumbnailSize, thumbnailSize)
                        .crop(Positions.CENTER);

                if ("jpg".equals(outputExt)) {
                    builder.outputFormat("jpg")
                            .outputQuality(thumbnailQuality);
                } else if ("png".equals(outputExt)) {
                    builder.outputFormat("png");
                } else if ("webp".equals(outputExt)) {
                    builder.outputFormat("webp")
                            .outputQuality(thumbnailQuality);
                }

                builder.toFile(targetPath.toFile());
            } catch (Exception e) {
                Files.deleteIfExists(targetPath);
                throw new IllegalArgumentException("Failed to process image: " + e.getMessage(), e);
            }
        }

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
