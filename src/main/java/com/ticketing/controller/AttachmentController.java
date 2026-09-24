package com.ticketing.controller;

import com.ticketing.config.UserContextHolder;
import com.ticketing.model.TicketAttachment;
import com.ticketing.model.User;
import com.ticketing.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(value = "/tickets/{ticketId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketAttachment> uploadAttachment(
            @PathVariable String ticketId,
            @RequestParam("file") MultipartFile file) throws IOException {

        User currentUser = UserContextHolder.get();
        if (currentUser == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String uploader = currentUser.getUsername();

        TicketAttachment attachment = attachmentService.saveAttachment(ticketId, file, uploader);
        return ResponseEntity.ok(attachment);
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) throws MalformedURLException {
        TicketAttachment attachment = attachmentService.getAttachment(id);
        Resource resource = attachmentService.loadAttachmentAsResource(id);

        String contentType = attachment.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/attachments/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
                "maxAttachmentCount", attachmentService.getMaxAttachmentCount(),
                "maxSizeMb", attachmentService.getMaxSizeMb()
        ));
    }
}
