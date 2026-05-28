package com.qrify;

import jakarta.persistence.*;

@Entity
@Table(name = "qr_codes")
public class QRCodeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String assetType; // Tracks "TEXT" or "FILE" structures natively
    
    @Column(columnDefinition = "TEXT")
    private String textContent; // Stores text definitions or website routing links
    
    private String fileContentPath; // Points directly to the location on your storage drive
    private String customUniqueSlug; // A short random string mapped inside the QR matrix pixels

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public String getFileContentPath() { return fileContentPath; }
    public void setFileContentPath(String fileContentPath) { this.fileContentPath = fileContentPath; }

    public String getCustomUniqueSlug() { return customUniqueSlug; }
    public void setCustomUniqueSlug(String customUniqueSlug) { this.customUniqueSlug = customUniqueSlug; }
}