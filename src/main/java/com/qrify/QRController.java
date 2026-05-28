package com.qrify;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
public class QRController {

    @Autowired
    private QRRepository qrRepository;

    @Autowired
    private jakarta.servlet.http.HttpServletRequest currentRequest;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploaded_assets/";

    // Reads the precise request URL environment domain (local IP or production domain)
    private String getLocalServerBaseUrl() {
        try {
            String requestUrl = currentRequest.getRequestURL().toString();
            String contextPath = currentRequest.getContextPath();
            return requestUrl.replace(currentRequest.getRequestURI(), contextPath);
        } catch (Exception e) {
            return "http://localhost:8080";
        }
    }

    @PostMapping("/process")
    public ResponseEntity<?> compileProductionAsset(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String uniqueSlug = UUID.randomUUID().toString().substring(0, 8);
            QRCodeRecord record = new QRCodeRecord();
            record.setTitle(title);
            record.setAssetType(type);
            record.setCustomUniqueSlug(uniqueSlug);

            if (type.equalsIgnoreCase("FILE")) {
                if (file == null || file.isEmpty()) {
                    return ResponseEntity.badRequest().body("Payload file data block missing.");
                }
                
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) directory.mkdirs();

                String cleanOriginalName = file.getOriginalFilename().replaceAll("\\s+", "_");
                String savedFileName = uniqueSlug + "_" + cleanOriginalName;
                
                Files.write(Paths.get(UPLOAD_DIR + savedFileName), file.getBytes());
                record.setFileContentPath(savedFileName);
            } else {
                if (text == null || text.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("URL dynamic text string target missing.");
                }
                record.setTextContent(text);
            }

            qrRepository.save(record);

            String embeddedTrackingUrl = getLocalServerBaseUrl() + "/api/qr/view/" + uniqueSlug;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(embeddedTrackingUrl, BarcodeFormat.QR_CODE, 350, 350);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(pngOutputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Engine compilation error: " + e.getLocalizedMessage());
        }
    }

    @GetMapping("/view/{slug}")
    public ResponseEntity<?> interceptAndRouteScan(@PathVariable String slug) {
        try {
            QRCodeRecord record = qrRepository.findByCustomUniqueSlug(slug);
            if (record == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("QR Link code has expired or doesn't exist.");
            }

            if (record.getAssetType().equalsIgnoreCase("TEXT")) {
                String target = record.getTextContent().trim();
                if (!target.startsWith("http://") && !target.startsWith("https://") && !target.contains("://")) {
                    target = "https://" + target;
                }
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, target)
                        .build();
            }

            Path filePath = Paths.get(UPLOAD_DIR).resolve(record.getFileContentPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) contentType = "application/octet-stream";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename().substring(9) + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Physical asset asset binary block missing on storage partition.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}