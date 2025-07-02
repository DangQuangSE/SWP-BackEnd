package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.UserRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    ModelMapper modelMapper;

    public UserDTO updateUserProfile(UserDTO request) {
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Người dùng không tồn tại"));

        user.setFullname(request.getFullname());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setImageUrl(request.getImageUrl());
        user.setDateOfBirth(request.getDateOfBirth());

        User updated = userRepository.save(user);

        return modelMapper.map(updated, UserDTO.class);
    }

    public UserDTO getUserProfile() {
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Người dùng không tồn tại"));
        return modelMapper.map(user, UserDTO.class);
    }
}
