package com.ticketing.service;

import com.ticketing.model.TicketAttachment;
import com.ticketing.repository.TicketAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final TicketAttachmentRepository attachmentRepository;

    @Value("${app.attachments.max-count:10}")
    private int maxAttachmentCount;

    @Value("${app.attachments.max-size-mb:20}")
    private long maxSizeMb;

    @Value("${app.attachments.storage-dir:./data/attachments}")
    private String storageDir;

    public AttachmentService(TicketAttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public List<TicketAttachment> getAttachmentsForTicket(String ticketId) {
        return attachmentRepository.findByTicketIdOrderByUploadedAtAsc(ticketId);
    }

    @Transactional
    public TicketAttachment saveAttachment(String ticketId, MultipartFile file, String uploadedBy) throws IOException {
        long currentCount = attachmentRepository.countByTicketId(ticketId);
        if (currentCount >= maxAttachmentCount) {
            throw new IllegalArgumentException("Maximum attachment count (" + maxAttachmentCount + ") reached for this ticket.");
        }

        long maxSizeBytes = maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxSizeMb + " MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "attachment";
        }

        Path dirPath = Paths.get(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(dirPath);

        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        Path targetPath = dirPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        TicketAttachment attachment = new TicketAttachment(
                ticketId,
                originalFilename,
                file.getSize(),
                file.getContentType(),
                targetPath.toString(),
                uploadedBy
        );

        return attachmentRepository.save(attachment);
    }

    public Resource loadAttachmentAsResource(Long attachmentId) throws MalformedURLException {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));

        Path filePath = Paths.get(attachment.getFilePath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IllegalArgumentException("File not found on storage: " + attachment.getFileName());
        }
    }

    public TicketAttachment getAttachment(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + id));
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        attachmentRepository.findById(attachmentId).ifPresent(att -> {
            try {
                Files.deleteIfExists(Paths.get(att.getFilePath()));
            } catch (IOException ignored) {}
            attachmentRepository.delete(att);
        });
    }

    public int getMaxAttachmentCount() { return maxAttachmentCount; }
    public long getMaxSizeMb() { return maxSizeMb; }
}
