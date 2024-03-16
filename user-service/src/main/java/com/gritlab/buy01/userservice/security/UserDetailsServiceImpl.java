package com.gritlab.buy01.userservice.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gritlab.buy01.userservice.model.User;
import com.gritlab.buy01.userservice.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    List<User> users = userRepository.findByName(username);
    if (users == null || users.size() == 0) {
      throw new UsernameNotFoundException("User Not Found with username: " + username);
    }

    return UserDetailsImpl.build(users.get(0));
  }

  @Transactional
  public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + userId));
    return UserDetailsImpl.build(user);
  }
}
