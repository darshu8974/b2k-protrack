package com.protrack.files.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * An immutable physical version of a {@link Document}. Never mutated after insert except for the
 * {@code isCurrent} flag (rollback/version flip). The bytes live in storage; this row holds only
 * metadata plus the {@code storageKey} and SHA-256 checksum for integrity. {@code createdAt} is set
 * by the database default.
 */
@Entity
@Table(name = "file_versions")
public class FileVersion {

	@Id
	private UUID id;

	@Column(name = "document_id", nullable = false)
	private UUID documentId;

	@Column(name = "version_no", nullable = false)
	private int versionNo;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "mime_type", nullable = false)
	private String mimeType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "storage_key", nullable = false)
	private String storageKey;

	@Column(name = "checksum_sha256")
	private String checksumSha256;

	@Column(name = "is_current", nullable = false)
	private boolean current;

	@Column(name = "uploaded_by")
	private UUID uploadedBy;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected FileVersion() {
	}

	public FileVersion(UUID id, UUID documentId, int versionNo, String fileName, String mimeType,
			long sizeBytes, String storageKey, String checksumSha256, boolean current, UUID uploadedBy) {
		this.id = id;
		this.documentId = documentId;
		this.versionNo = versionNo;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.sizeBytes = sizeBytes;
		this.storageKey = storageKey;
		this.checksumSha256 = checksumSha256;
		this.current = current;
		this.uploadedBy = uploadedBy;
	}

	public UUID getId() {
		return id;
	}

	public UUID getDocumentId() {
		return documentId;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public String getFileName() {
		return fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public String getStorageKey() {
		return storageKey;
	}

	public String getChecksumSha256() {
		return checksumSha256;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public UUID getUploadedBy() {
		return uploadedBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
