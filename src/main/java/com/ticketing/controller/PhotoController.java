package com.ticketing.controller;

import com.ticketing.model.PredefinedPhoto;
import com.ticketing.service.PhotoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
                "maxSizeMb", photoService.getMaxSizeMb()
        ));
    }

    @GetMapping
    public ResponseEntity<List<PredefinedPhoto>> getPhotos(
            @RequestParam(required = false, defaultValue = "ALL") String category) {
        return ResponseEntity.ok(photoService.getAllPhotos(category));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PredefinedPhoto> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false, defaultValue = "BOTH") String category) throws IOException {

        PredefinedPhoto saved = photoService.uploadCustomPhoto(file, name, category);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/custom/{filename}")
    public ResponseEntity<Resource> serveCustomPhoto(@PathVariable String filename) throws MalformedURLException {
        Resource resource = photoService.loadCustomPhotoAsResource(filename);
        String contentType = MediaType.IMAGE_JPEG_VALUE;
        if (filename.endsWith(".png")) contentType = MediaType.IMAGE_PNG_VALUE;
        if (filename.endsWith(".svg")) contentType = "image/svg+xml";
        if (filename.endsWith(".webp")) contentType = "image/webp";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(resource);
    }

    @GetMapping(value = "/default/{name}", produces = "image/svg+xml")
    public ResponseEntity<String> serveDefaultSvg(@PathVariable String name) {
        String svg;
        switch (name.toLowerCase()) {
            case "adhocs.svg":
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"20\" fill=\"#f59e0b\"/>" +
                        "<path d=\"M55 15 L25 55 L48 55 L45 85 L75 45 L52 45 Z\" fill=\"#ffffff\" stroke=\"#d97706\" stroke-width=\"2\"/>" +
                        "</svg>";
                break;
            case "project-1.svg":
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"20\" fill=\"#3b82f6\"/>" +
                        "<circle cx=\"50\" cy=\"50\" r=\"25\" fill=\"none\" stroke=\"#ffffff\" stroke-width=\"6\"/>" +
                        "<circle cx=\"50\" cy=\"50\" r=\"10\" fill=\"#ffffff\"/>" +
                        "</svg>";
                break;
            case "project-2.svg":
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"20\" fill=\"#8b5cf6\"/>" +
                        "<path d=\"M30 65 A15 15 0 0 1 45 40 A20 20 0 0 1 70 45 A15 15 0 0 1 70 65 Z\" fill=\"#ffffff\"/>" +
                        "</svg>";
                break;
            case "project-3.svg":
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"20\" fill=\"#10b981\"/>" +
                        "<path d=\"M50 20 L75 35 L75 65 L50 80 L25 65 L25 35 Z\" fill=\"none\" stroke=\"#ffffff\" stroke-width=\"6\"/>" +
                        "</svg>";
                break;
            case "avatar-2.svg":
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"50\" fill=\"#059669\"/>" +
                        "<circle cx=\"50\" cy=\"40\" r=\"18\" fill=\"#a7f3d0\"/>" +
                        "<circle cx=\"50\" cy=\"90\" r=\"32\" fill=\"#a7f3d0\"/>" +
                        "</svg>";
                break;
            case "avatar-3.svg":
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"50\" fill=\"#ea580c\"/>" +
                        "<circle cx=\"50\" cy=\"40\" r=\"18\" fill=\"#fed7aa\"/>" +
                        "<circle cx=\"50\" cy=\"90\" r=\"32\" fill=\"#fed7aa\"/>" +
                        "</svg>";
                break;
            case "avatar-4.svg":
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"50\" fill=\"#7c3aed\"/>" +
                        "<circle cx=\"50\" cy=\"40\" r=\"18\" fill=\"#ddd6fe\"/>" +
                        "<circle cx=\"50\" cy=\"90\" r=\"32\" fill=\"#ddd6fe\"/>" +
                        "</svg>";
                break;
            case "avatar-1.svg":
            default:
                svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100\" height=\"100\">" +
                        "<rect width=\"100\" height=\"100\" rx=\"50\" fill=\"#2563eb\"/>" +
                        "<circle cx=\"50\" cy=\"40\" r=\"18\" fill=\"#bfdbfe\"/>" +
                        "<circle cx=\"50\" cy=\"90\" r=\"32\" fill=\"#bfdbfe\"/>" +
                        "</svg>";
                break;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/svg+xml"))
                .body(svg);
    }
}
