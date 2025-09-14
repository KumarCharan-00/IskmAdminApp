package org.iskm.admin.web.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.Cookie;
import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.AddUserResponse;
import org.iskm.admin.web.dto.res.ContentDTO;
import org.iskm.admin.web.dto.res.ContentDTO.ImageDTO;
import org.iskm.admin.web.dto.res.Response;
import org.iskm.admin.web.model.ContentRequest;
import org.iskm.admin.web.model.ContentUpdateRequest;
import org.iskm.admin.web.model.entity.Content;
import org.iskm.admin.web.model.entity.Image;
import org.iskm.admin.web.model.entity.User;
import org.iskm.admin.web.repository.ContentRepository;
import org.iskm.admin.web.repository.UserRepo;
import org.iskm.admin.web.repository.UserRepository;
import org.iskm.admin.web.util.CommonUtil;
import org.iskm.admin.web.util.Constants;
import org.iskm.admin.web.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
	
	private final UserRepo repo;
	 private final CommonUtil commonUtil;
	 private final ContentRepository contentRepository;
	 private final UserRepository userRepository;
	
	@Autowired
    public UserService(UserRepo repo,CommonUtil commonUtil, ContentRepository contentRepository, UserRepository userRepository) {
        this.repo = repo;
        this.commonUtil = commonUtil;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }
	
    public Response saveUser(AddUserDTO addUserDTO) {
		AddUserResponse res;
        try {
            var creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            addUserDTO.setPassword(PasswordUtil.encodePassword(addUserDTO.getPassword()));
            var userEntity = commonUtil.mapEntityData(addUserDTO, creationTime);
            repo.save(userEntity);
            res = new AddUserResponse(true);
        } catch (Exception ex) {
            log.error("Adding user failed with error :: ", ex);
            res = new AddUserResponse(false);
        }
        return res;
    }   
    
    @Transactional
    public void updatePassword(String userName, String newPassword) {
        User user = userRepository.findByUsername(userName)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String hashedPassword = (PasswordUtil.encodePassword(newPassword));
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
    
    public void saveContent(ContentRequest request) {
        Content content = new Content();
        content.setPageTitle(request.getPageTitle());
        content.setPageContent(request.getPageContent());
        content.setStatus(request.getStatus() != null ? request.getStatus() : "draft");

        List<Image> imageList = new ArrayList<>();
        var imageFiles = request.getImages();

        for(MultipartFile imageFile : imageFiles) {
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    Image image = new Image();
                    image.setImageData(imageFile.getBytes());
                    image.setContent(content);
                    imageList.add(image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process image", e);
                }
            }
        }

        content.setImages(imageList);
        contentRepository.save(content);
    }
    
    public List<Content> getAllContent() {
        return contentRepository.findAll();
    }


    public List<Content> getContentByStatus(String status) {
        return contentRepository.findByStatusIgnoreCase(status);
    }
    public ContentDTO toContentDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setPageTitle(content.getPageTitle());
        dto.setPageContent(content.getPageContent());
        dto.setStatus(content.getStatus());
        dto.setCreatedAt(content.getCreatedAt());

        List<ImageDTO> imageDTOs = content.getImages().stream().map(image -> {
            ImageDTO img = new ImageDTO();
            img.setId(image.getId());
            img.setImageData(image.getImageData());
            img.setExpiresAt(image.getExpiresAt());
            img.setCreatedAt(image.getCreatedAt());
            return img;
        }).toList();

        dto.setImages(imageDTOs);
        return dto;
    }
    
    @Transactional
    public Content updateContent(Long id, ContentUpdateRequest request) {
        Content content = contentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Content not found"));

        content.setPageTitle(request.getPageTitle());
        content.setPageContent(request.getPageContent());
        content.setStatus(request.getStatus());

        return contentRepository.save(content);
    }
    
    @Transactional
    public void deleteContent(Long id) {
        if (!contentRepository.existsById(id)) {
            throw new EntityNotFoundException("Content not found");
        }
        contentRepository.deleteById(id);
    }
}
