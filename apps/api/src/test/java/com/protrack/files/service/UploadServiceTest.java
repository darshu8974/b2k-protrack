package com.protrack.files.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.files.domain.DocType;
import com.protrack.files.repository.DocumentRepository;
import com.protrack.files.repository.FileVersionRepository;
import com.protrack.files.service.UploadService.UploadedBlob;
import com.protrack.shared.error.ApiException;
import com.protrack.shared.storage.StoragePort;
import com.protrack.shared.storage.StoragePort.StoredObject;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Unit tests for {@link UploadService} type/size validation (no Spring context, no Docker). The
 * storage port is mocked; validation must reject bad uploads <em>before</em> any storage write.
 */
class UploadServiceTest {

	private static final String DOCX =
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document";

	private StoragePort storagePort;
	private UploadService uploadService;

	@BeforeEach
	void setUp() {
		storagePort = mock(StoragePort.class);
		uploadService = new UploadService(
				mock(DocumentRepository.class), mock(FileVersionRepository.class),
				storagePort, mock(ApplicationEventPublisher.class));
	}

	@Test
	void storesValidDocxManuscriptAndReturnsMetadata() {
		when(storagePort.store(any(InputStream.class)))
				.thenReturn(new StoredObject("blobs/aa/hash", "hash", 12L));
		MultipartFile file = new MockMultipartFile("file", "chapter.docx", DOCX, "content".getBytes());

		UploadedBlob blob = uploadService.store(file, DocType.MANUSCRIPT);

		assertThat(blob.storageKey()).isEqualTo("blobs/aa/hash");
		assertThat(blob.checksumSha256()).isEqualTo("hash");
		assertThat(blob.fileName()).isEqualTo("chapter.docx");
		assertThat(blob.mimeType()).isEqualTo(DOCX);
		verify(storagePort).store(any(InputStream.class));
	}

	@Test
	void acceptsPdfManuscript() {
		when(storagePort.store(any(InputStream.class)))
				.thenReturn(new StoredObject("blobs/bb/h2", "h2", 20L));
		MultipartFile file =
				new MockMultipartFile("file", "chapter.pdf", "application/pdf", "%PDF".getBytes());

		assertThat(uploadService.store(file, DocType.MANUSCRIPT).fileName()).isEqualTo("chapter.pdf");
	}

	@Test
	void rejectsEmptyFile() {
		MultipartFile empty = new MockMultipartFile("file", "empty.docx", DOCX, new byte[0]);

		assertThatThrownBy(() -> uploadService.store(empty, DocType.MANUSCRIPT))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("EMPTY_FILE"));
		verify(storagePort, never()).store(any());
	}

	@Test
	void rejectsUnsupportedTypeForManuscript() {
		MultipartFile txt = new MockMultipartFile("file", "notes.txt", "text/plain", "hi".getBytes());

		assertThatThrownBy(() -> uploadService.store(txt, DocType.MANUSCRIPT))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("UNSUPPORTED_FILE_TYPE"));
		verify(storagePort, never()).store(any());
	}

	@Test
	void rejectsFileExceedingSizeLimit() {
		// Fake an oversized file without allocating 100 MB.
		MultipartFile huge = mock(MultipartFile.class);
		when(huge.isEmpty()).thenReturn(false);
		when(huge.getSize()).thenReturn(101L * 1024 * 1024);

		assertThatThrownBy(() -> uploadService.store(huge, DocType.MANUSCRIPT))
				.isInstanceOfSatisfying(ApiException.class,
						ex -> assertThat(ex.getCode()).isEqualTo("FILE_TOO_LARGE"));
		verify(storagePort, never()).store(any());
	}

	@Test
	void allowsAnyTypeForOtherDocType() {
		when(storagePort.store(any(InputStream.class)))
				.thenReturn(new StoredObject("blobs/cc/h3", "h3", 5L));
		MultipartFile any = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());

		assertThat(uploadService.store(any, DocType.OTHER).fileName()).isEqualTo("notes.txt");
	}
}
