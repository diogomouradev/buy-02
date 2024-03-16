package com.gritlab.buy01.mediaservice.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gritlab.buy01.mediaservice.exception.ForbiddenException;
import com.gritlab.buy01.mediaservice.exception.NotFoundException;
import com.gritlab.buy01.mediaservice.exception.UnexpectedPrincipalTypeException;
import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipRequest;
import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipResponse;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarUpdateRequest;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarUpdateResponse;
import com.gritlab.buy01.mediaservice.model.Media;
import com.gritlab.buy01.mediaservice.payload.response.ErrorMessage;
import com.gritlab.buy01.mediaservice.payload.response.MediaResponse;
import com.gritlab.buy01.mediaservice.security.UserDetailsImpl;
import com.gritlab.buy01.mediaservice.service.KafkaService;
import com.gritlab.buy01.mediaservice.service.MediaService;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class MediaController {
  private final MediaService mediaService;

  private final KafkaService kafkaService;

  @Autowired
  public MediaController(MediaService mediaService, KafkaService kafkaService) {
    this.mediaService = mediaService;
    this.kafkaService = kafkaService;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/media/{productId}")
  public ResponseEntity<MediaResponse> getMediaByProductId(
      @PathVariable("productId") String productId) {
    Optional<List<Media>> media = mediaService.getAllMediaByProductId(productId);
    if (media.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    MediaResponse mediaResponse = new MediaResponse();
    mediaResponse.setProductId(productId);
    mediaResponse.setMedia(media.get());
    return new ResponseEntity<>(mediaResponse, HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/media/{id}")
  public ResponseEntity<?> deleteById(@PathVariable String id) {
    try {
      UserDetailsImpl userDetails = getPrincipal();

      Optional<Media> mediaQuery = mediaService.getMediaById(id);
      Media media = null;

      if (mediaQuery.isPresent()) {
        media = mediaQuery.get();
      } else {
        throw new NotFoundException("Media not found by id");
      }

      if (media.getUserId() != null && !userDetails.getId().equals(media.getUserId())) {
        throw new ForbiddenException("Only the owner can delete user avatar");
      }

      if (media.getProductId() != null) {
        ProductOwnershipRequest ownershipRequest =
            new ProductOwnershipRequest(media.getProductId(), userDetails.getId());
        ProductOwnershipResponse ownershipResponse =
            kafkaService.sendProductOwnershipRequestAndWaitForResponse(ownershipRequest);

        if (!ownershipResponse.isOwner()) {
          throw new ForbiddenException("Media can only be deleted from own products");
        }
      }
    } catch (NotFoundException e) {
      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.NOT_FOUND.value());
      return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    } catch (ForbiddenException e) {
      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    return mediaService.deleteById(id);
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/media")
  public ResponseEntity<?> deleteByUserOrProductId(
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String productId) {

    if (userId != null && productId != null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    try {
      UserDetailsImpl userDetails = getPrincipal();

      if (productId != null) {
        ProductOwnershipRequest ownershipRequest =
            new ProductOwnershipRequest(productId, userDetails.getId());
        ProductOwnershipResponse ownershipResponse =
            kafkaService.sendProductOwnershipRequestAndWaitForResponse(ownershipRequest);

        if (!ownershipResponse.isOwner()) {
          ErrorMessage error =
              new ErrorMessage(
                  "Media can only be deleted from own products", HttpStatus.FORBIDDEN.value());
          return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }
      }

      if (userId != null && (!userDetails.getId().equals(userId))) {
        ErrorMessage error =
            new ErrorMessage(
                "You can only delete avatar belonging to your profile",
                HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
      }
    } catch (Exception e) {
      ErrorMessage error =
          new ErrorMessage(
              "An error was encountered during media upload", HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    return mediaService.deleteMediaById(userId, productId);
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/media")
  public ResponseEntity<?> createMedia(
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String productId,
      @RequestPart MultipartFile image,
      HttpServletRequest request) {

    if (userId != null && productId != null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    try {
      UserDetailsImpl userDetails = getPrincipal();

      if (productId != null) {
        return handleProductMediaUpload(userId, productId, image, userDetails);
      }

      if (userId != null) {
        return handleUserAvatarUpload(userId, productId, image, userDetails);
      }
    } catch (Exception e) {
      ErrorMessage error =
          new ErrorMessage(
              "An error was encountered during media upload", HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/media/product/{productId}")
  public ResponseEntity<?> getProductThumbnail(@PathVariable("productId") String productId) {
    return mediaService.getProductThumbnail(productId);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/media/user/{userId}")
  public ResponseEntity<?> getUserAvatar(@PathVariable("userId") String userId) {
    return mediaService.getUserAvatar(userId);
  }

  private UserDetailsImpl getPrincipal() throws UnexpectedPrincipalTypeException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl userDetailsImpl) {
      return userDetailsImpl;
    } else {
      throw new UnexpectedPrincipalTypeException("Unexpected principal type");
    }
  }

  private ResponseEntity<?> handleUserAvatarUpload(
      String userId, String productId, MultipartFile image, UserDetailsImpl userDetails) {
    if (!userDetails.getId().equals(userId)) {
      ErrorMessage error =
          new ErrorMessage(
              "You can only upload avatar to your user profile", HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    ResponseEntity<?> mediaResponse = mediaService.createMedia(image, userId, productId);
    Object responseBody = mediaResponse.getBody();

    if (responseBody instanceof Media responseMedia) {

      // delete all other images
      mediaService.deletePreviousUserAvatars(userId, responseMedia.getId());

      String avatarId = responseMedia.getId();
      UserAvatarUpdateRequest userAvatarUpdateRequest =
          new UserAvatarUpdateRequest(UUID.randomUUID().toString(), userId, avatarId);
      UserAvatarUpdateResponse userAvatarUpdateResponse =
          kafkaService.sendUserAvatarUpdateRequestAndWaitForResponse(userAvatarUpdateRequest);

      if (userAvatarUpdateResponse == null) {
        ErrorMessage error =
            new ErrorMessage(
                "Timeout while waiting for user avatar update response",
                HttpStatus.REQUEST_TIMEOUT.value());
        mediaService.deleteMediaById(userId, productId);
        return new ResponseEntity<>(error, HttpStatus.REQUEST_TIMEOUT);
      }

      if (!userAvatarUpdateResponse.isAllowed()) {
        ErrorMessage error =
            new ErrorMessage(
                String.format(
                    "An error was encountered during user avatar update: %s",
                    userAvatarUpdateResponse.getMessage()),
                HttpStatus.FORBIDDEN.value());
        mediaService.deleteMediaById(userId, productId);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
      }
      return new ResponseEntity<>(responseMedia, HttpStatus.CREATED);
    } else {
      return mediaResponse;
    }
  }

  private ResponseEntity<?> handleProductMediaUpload(
      String userId, String productId, MultipartFile image, UserDetailsImpl userDetails) {
    ProductOwnershipRequest ownershipRequest =
        new ProductOwnershipRequest(productId, userDetails.getId());
    ProductOwnershipResponse ownershipResponse =
        kafkaService.sendProductOwnershipRequestAndWaitForResponse(ownershipRequest);

    if (!ownershipResponse.isOwner()) {
      ErrorMessage error =
          new ErrorMessage(
              "Media can only be added to your own products", HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    return mediaService.createMedia(image, userId, productId);
  }
}
