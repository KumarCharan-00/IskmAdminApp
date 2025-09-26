package org.iskm.admin.web.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
import org.iskm.admin.web.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
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
            addUserDTO.setUserId(CommonUtil.generateUUID());
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
    	try {
        User user = userRepository.findByUsername(userName)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String hashedPassword = (PasswordUtil.encodePassword(newPassword));
        user.setPassword(hashedPassword);
        userRepository.save(user);
    	}catch(Exception ex) {
    		log.info("Update password failed with exception :: ",ex);
    	}
    }
    
    public void saveContent(ContentRequest request) {
        if(null == request.getImages()) {
        	log.info("image should not be nulll");
        	throw new NullPointerException();
        }
        List<Image> imageList = new ArrayList<>();
        Content content = new Content(
                CommonUtil.generateUUID(),
                request.getPageTitle(), request.getPageContent(),
                request.getStatus() != null ? request.getStatus() : "draft",
                LocalDateTime.now(), imageList
        );
        var imageFiles = request.getImages();
        try {
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
            contentRepository.save(content);
        } catch(Exception ex) {
        	log.error("Saving content to DB failed with the exception :: " , ex);
        }
    }


    public List<Content> getContent(String byStatus, LocalDateTime from, LocalDateTime to) {
    	try {
        Specification<Content> spec = (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (byStatus != null && !byStatus.isBlank()) {
                var status = root.get("status");
                var lowerCaseStatus = builder.lower(status.as(String.class));
                predicates.add(builder.equal(lowerCaseStatus, byStatus.toLowerCase()));
            }
            if (to != null) predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), to));
            if (from != null) predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), from));
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        return contentRepository.findAll(spec);
    	} catch(Exception ex) {
    		log.error("Fetching content failed with exception ::", ex);
    		return null;
    	}
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
    public void deleteContent(String id) {
    	try {
        if (!contentRepository.existsById(id)) {
            throw new EntityNotFoundException("Content not found");
        }
        contentRepository.deleteById(id);
    	} catch(Exception ex) {
    		log.error("deleting content failed with exception :: ", ex);
    	}
    }
    
    @Transactional
    public ContentDTO partialUpdateById(String id, ContentUpdateRequest request) {
    	try {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found with id: " + id));

        if (request.getPageTitle() != null && !request.getPageTitle().isBlank()) {
            content.setPageTitle(request.getPageTitle());
        }
        if (request.getPageContent() != null && !request.getPageContent().isBlank()) {
            content.setPageContent(request.getPageContent());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            content.setStatus(request.getStatus());
        }

        contentRepository.save(content);
         return toContentDTO(content);
        
    	} catch(Exception ex) {
    		log.error("Updating content by id failed with exception :: ", ex);
    		return null;
    	}
    }

}
