package com.gritlab.buy01.mediaservice.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.gritlab.buy01.mediaservice.event.DeleteMediaEvent;
import com.gritlab.buy01.mediaservice.repository.MediaRepository;

@Component
public class DeleteMediaEventListener {

  @Autowired private MediaRepository mediaRepository;

  @EventListener
  public void handleDeleteMediaEvent(DeleteMediaEvent event) {
    if (event.getUserId() != null) {
      mediaRepository.deleteAllByUserId(event.getUserId());
    } else if (event.getProductId() != null) {
      mediaRepository.deleteAllByProductId(event.getProductId());
    }
  }
}
