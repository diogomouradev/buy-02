package com.gritlab.buy01.mediaservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.gritlab.buy01.mediaservice.model.Media;
import com.gritlab.buy01.mediaservice.payload.response.SingleMediaResponse;
import com.gritlab.buy01.mediaservice.repository.MediaRepository;

public class MediaServiceTests {

  @Mock private MediaRepository mediaRepository;
  @Mock private ThumbnailService thumbnailService;
  @Mock private ImageService imageService;

  @InjectMocks private MediaService mediaService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    try {
      when(imageService.readImage(any(InputStream.class)))
          .thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetMediaById() {
    String mediaId = "123";
    Media expectedMedia = new Media();
    expectedMedia.setId(mediaId);

    when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(expectedMedia));

    Optional<Media> result = mediaService.getMediaById(mediaId);

    assertNotNull(result, "Result should not be null");
    assertEquals(expectedMedia, result.get(), "Returned media should match the expected media");
  }

  @Test
  public void testGetAllMediaByProductId() {
    String productId = "product123";
    List<Media> expectedMediaList = new ArrayList<>();

    when(mediaRepository.findAllByProductId(productId)).thenReturn(Optional.of(expectedMediaList));

    Optional<List<Media>> result = mediaService.getAllMediaByProductId(productId);

    assertNotNull(result, "Result should not be null");
    assertEquals(
        expectedMediaList, result.get(), "Returned media list should match the expected list");
  }

  @Test
  public void testDeleteById() {
    String mediaId = "123";

    ResponseEntity<?> response = mediaService.deleteById(mediaId);

    assertEquals(
        HttpStatus.NO_CONTENT, response.getStatusCode(), "Status code should be 204 NO_CONTENT");
    Mockito.verify(mediaRepository, Mockito.times(1)).deleteById(mediaId);
  }

  @Test
  public void testDeleteMediaById_UserId() {
    String userId = "user123";

    ResponseEntity<?> response = mediaService.deleteMediaById(userId, null);

    assertEquals(
        HttpStatus.NO_CONTENT, response.getStatusCode(), "Status code should be 204 NO_CONTENT");
    Mockito.verify(mediaRepository, Mockito.times(1)).deleteAllByUserId(userId);
  }

  @Test
  public void testDeleteMediaById_ProductId() {
    String productId = "product123";

    ResponseEntity<?> response = mediaService.deleteMediaById(null, productId);

    assertEquals(
        HttpStatus.NO_CONTENT, response.getStatusCode(), "Status code should be 204 NO_CONTENT");
    Mockito.verify(mediaRepository, Mockito.times(1)).deleteAllByProductId(productId);
  }

  @Test
  public void testCreateMedia() throws IOException {
    MultipartFile image = createTestMultipartFile();
    String userId = "user123";
    String productId = "product123";

    Media expectedMedia = new Media();
    when(mediaRepository.save(expectedMedia)).thenReturn(expectedMedia);

    ResponseEntity<?> response = mediaService.createMedia(image, userId, productId);

    assertNotNull(response, "Response is null");
    assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status code should be 201 CREATED");
  }

  @Test
  public void testCreateMedia_TooLargeFile() throws IOException {
    MultipartFile image = createLargeTestMultipartFile();
    String userId = "user123";
    String productId = "product123";

    ResponseEntity<?> response = mediaService.createMedia(image, userId, productId);

    assertEquals(
        HttpStatus.PAYLOAD_TOO_LARGE,
        response.getStatusCode(),
        "Status code should be 413 PAYLOAD_TOO_LARGE");
  }

  @Test
  public void testConvertAndReturnMediaWithMedia() {
    Media media = new Media();
    media.setId("123");
    media.setProductId("product123");
    media.setUserId("user123");
    media.setMimeType("image/jpeg");
    media.setImage(new Binary(new byte[] {0x12, 0x34, 0x56}));

    when(mediaRepository.findFirstByProductId("product123")).thenReturn(Optional.of(media));

    SingleMediaResponse thumbnailSingleMediaResponse =
        new SingleMediaResponse("123", "base64Image", "product123", "user123", "image/jpeg");

    ResponseEntity<?> responseEntity =
        new ResponseEntity<>(thumbnailSingleMediaResponse, HttpStatus.OK);

    when(thumbnailService.getProductThumbnailHelper(anyString(), any(Media.class)))
        .thenAnswer(
            new Answer<ResponseEntity<?>>() {
              @Override
              public ResponseEntity<?> answer(InvocationOnMock invocation) {
                return responseEntity;
              }
            });

    ResponseEntity<?> response = mediaService.getProductThumbnail("product123");

    assertNotNull(response, "Response should not be null");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code should be 200 OK");
    assertTrue(
        response.getBody() instanceof SingleMediaResponse,
        "Response body should be a SingleMediaResponse");

    SingleMediaResponse singleMediaResponse = (SingleMediaResponse) response.getBody();
    assertEquals("123", singleMediaResponse.getId(), "Media ID should match");
    assertEquals("product123", singleMediaResponse.getProductId(), "Product ID should match");
    assertEquals("user123", singleMediaResponse.getUserId(), "User ID should match");
    assertEquals("image/jpeg", singleMediaResponse.getMimeType(), "MIME type should match");
    assertNotNull(singleMediaResponse.getImage(), "Base64 image should not be null");
  }

  @Test
  public void testConvertAndReturnMediaWithNoMedia() {
    when(mediaRepository.findFirstByProductId("product123")).thenReturn(Optional.empty());

    ResponseEntity<?> response = mediaService.getProductThumbnail("product123");

    assertNotNull(response, "Response should not be null");
    assertEquals(
        HttpStatus.NO_CONTENT, response.getStatusCode(), "Status code should be 204 NO_CONTENT");
    assertNull(response.getBody(), "Response body should be null");
  }

  @Test
  public void testDeletePreviousUserAvatars() {
    String userId = "123";

    Media media1 = new Media();
    media1.setId("890");
    media1.setUserId("123");

    Media media2 = new Media();
    media2.setId("456");
    media2.setUserId("123");

    List<Media> mediaList = new ArrayList<>();
    mediaList.add(media1);
    mediaList.add(media2);

    when(mediaRepository.findAllByUserId(userId)).thenReturn(Optional.of(mediaList));
    doNothing().when(mediaRepository).deleteById(mediaList.get(0).getId());

    mediaService.deletePreviousUserAvatars(userId, mediaList.get(1).getId());

    // HACK: i couldnt figure how to verify the correct order and number of calls
    Mockito.verify(mediaRepository, Mockito.times(1)).deleteById(Mockito.anyString());
  }

  private MultipartFile createTestMultipartFile() {
    return new MultipartFile() {
      @Override
      public String getName() {
        return "test-file";
      }

      @Override
      public String getOriginalFilename() {
        return "test.jpg";
      }

      @Override
      public String getContentType() {
        return "image/jpeg";
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public long getSize() {
        return 1024;
      }

      @Override
      public byte[] getBytes() throws IOException {
        return new byte[1024];
      }

      @Override
      public InputStream getInputStream() throws IOException {
        // Create a dummy input stream for testing
        byte[] imageContent = new byte[1024]; // dummy image content
        return new ByteArrayInputStream(imageContent);
      }

      @Override
      public void transferTo(File dest) throws IOException, IllegalStateException {}
    };
  }

  private MultipartFile createLargeTestMultipartFile() {
    return new MultipartFile() {
      @Override
      public String getName() {
        return "test-large-file";
      }

      @Override
      public String getOriginalFilename() {
        return "large.jpg";
      }

      @Override
      public String getContentType() {
        return "image/jpeg";
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public long getSize() {
        return 3 * 1024 * 1024;
      }

      @Override
      public byte[] getBytes() throws IOException {
        return new byte[3 * 1024 * 1024];
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return null;
      }

      @Override
      public void transferTo(File dest) throws IOException, IllegalStateException {}
    };
  }
}
