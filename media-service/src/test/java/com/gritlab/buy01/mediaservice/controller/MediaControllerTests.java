package com.gritlab.buy01.mediaservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipRequest;
import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipResponse;
import com.gritlab.buy01.mediaservice.model.Media;
import com.gritlab.buy01.mediaservice.security.UserDetailsImpl;
import com.gritlab.buy01.mediaservice.service.KafkaService;
import com.gritlab.buy01.mediaservice.service.MediaService;

public class MediaControllerTests {

  @Mock private MediaService mediaService;

  @Mock private RestTemplate restTemplate;

  @Mock private KafkaService kafkaService;

  @Mock private MultipartFile multipartFile;

  @InjectMocks private MediaController mediaController;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private void simulateSecurityContext(String userId, String name, String... roles) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
    }
    UserDetailsImpl userDetails = new UserDetailsImpl(userId, name, authorities);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  public void testGetMediaByProductId() {
    Media media = new Media();
    when(mediaService.getAllMediaByProductId(anyString()))
        .thenReturn(Optional.of(Collections.singletonList(media)));

    ResponseEntity<?> response = mediaController.getMediaByProductId("testProduct");

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testDeleteProductMediaById() {
    simulateSecurityContext("456", "testUser", "SELLER");
    ProductOwnershipResponse ownershipResponse = new ProductOwnershipResponse();
    ownershipResponse.setOwner(true);
    when(kafkaService.sendProductOwnershipRequestAndWaitForResponse(
            any(ProductOwnershipRequest.class)))
        .thenReturn(ownershipResponse);

    Media media = new Media();
    media.setProductId("234");
    when(mediaService.getMediaById("123")).thenReturn(Optional.of(media));

    when(mediaService.deleteById(anyString()))
        .thenReturn((ResponseEntity) ResponseEntity.noContent().build());

    ResponseEntity<?> response = mediaController.deleteById("123");

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  public void testDeleteByUserOrProductId() {
    simulateSecurityContext("456", "testUser", "SELLER");

    when(mediaService.deleteMediaById("456", null))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

    ResponseEntity<?> response = mediaController.deleteByUserOrProductId("456", null);

    assertNotNull(response, "Response should not be null");
    assertEquals(
        HttpStatus.NO_CONTENT, response.getStatusCode(), "Status code should be 204 NO_CONTENT");
  }

  @Test
  public void testCreateMedia() {
    simulateSecurityContext("123", "testUser", "SELLER");

    ProductOwnershipResponse ownershipResponse = new ProductOwnershipResponse();
    ownershipResponse.setOwner(true);
    when(kafkaService.sendProductOwnershipRequestAndWaitForResponse(
            any(ProductOwnershipRequest.class)))
        .thenReturn(ownershipResponse);

    Media mockMedia = new Media();
    when(mediaService.createMedia(eq(multipartFile), isNull(), eq("testProduct")))
        .thenAnswer(
            invocation -> {
              return ResponseEntity.status(HttpStatus.CREATED).body(mockMedia);
            });

    ResponseEntity<?> response =
        mediaController.createMedia(null, "testProduct", multipartFile, null);

    assertNotNull(response, "Response is null");
    assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Unexpected status code");
  }

  @Test
  public void testGetProductThumbnail() {
    Media media = new Media();
    when(mediaService.getProductThumbnail(anyString()))
        .thenReturn((ResponseEntity) ResponseEntity.ok(media));

    ResponseEntity<?> response = mediaController.getProductThumbnail("testProduct");

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetUserAvatar() {
    Media media = new Media();
    when(mediaService.getUserAvatar(anyString()))
        .thenReturn((ResponseEntity) ResponseEntity.ok(media));

    ResponseEntity<?> response = mediaController.getUserAvatar("testUser");

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetMediaByProductId_NoMediaFound() {
    when(mediaService.getAllMediaByProductId(anyString())).thenReturn(Optional.empty());

    ResponseEntity<?> response = mediaController.getMediaByProductId("testProduct");

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  public void testCreateMedia_OwnershipDenied() {
    simulateSecurityContext("123", "testUser", "SELLER");

    ProductOwnershipResponse ownershipResponse = new ProductOwnershipResponse();
    ownershipResponse.setOwner(false);
    when(kafkaService.sendProductOwnershipRequestAndWaitForResponse(
            any(ProductOwnershipRequest.class)))
        .thenReturn(ownershipResponse);

    ResponseEntity<?> response =
        mediaController.createMedia(null, "testProduct", multipartFile, null);

    assertNotNull(response, "Response is null");
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Unexpected status code");
  }

  @Test
  public void testGetProductThumbnail_NoThumbnailFound() {
    when(mediaService.getProductThumbnail(anyString()))
        .thenReturn((ResponseEntity) ResponseEntity.notFound().build());

    ResponseEntity<?> response = mediaController.getProductThumbnail("testProduct");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void testGetUserAvatar_NoAvatarFound() {
    when(mediaService.getUserAvatar(anyString()))
        .thenReturn((ResponseEntity) ResponseEntity.notFound().build());

    ResponseEntity<?> response = mediaController.getUserAvatar("testUser");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
