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
import org.iskm.admin.web.model.entity.MobileImage;
import org.iskm.admin.web.model.entity.User;
import org.iskm.admin.web.model.entity.WebImage;
import org.iskm.admin.web.repository.ContentRepository;
import org.iskm.admin.web.repository.MobileImageRepository;
import org.iskm.admin.web.repository.UserRepo;
import org.iskm.admin.web.repository.UserRepository;
import org.iskm.admin.web.repository.WebImageRepository;
import org.iskm.admin.web.util.CommonUtil;
import org.iskm.admin.web.util.PasswordUtil;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
    private final WebImageRepository webImageRepository;
    private final MobileImageRepository mobileImageRepository;

    public UserService(UserRepo repo, CommonUtil commonUtil, ContentRepository contentRepository,
            UserRepository userRepository, WebImageRepository webImageRepository,
            MobileImageRepository mobileImageRepository) {
        this.repo = repo;
        this.commonUtil = commonUtil;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.webImageRepository = webImageRepository;
        this.mobileImageRepository = mobileImageRepository;
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

    // @Transactional
    public void updatePassword(String userName, String newPassword) {
        try {
            User user = userRepository.findByUsername(userName)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            String hashedPassword = (PasswordUtil.encodePassword(newPassword));
            user.setPassword(hashedPassword);
            userRepository.save(user);
        } catch (Exception ex) {
            log.info("Update password failed with exception :: ", ex);
        }
    }

    public void saveContent(ContentRequest request, String channel) {
        List<WebImage> webImageList = new ArrayList<>();
        List<MobileImage> mobileImageList = new ArrayList<>();
        Content content = new Content(
                CommonUtil.generateUUID(),
                request.getPageTitle(), request.getPageContent(),
                request.getStatus() != null ? request.getStatus() : "draft",
                LocalDateTime.now(), webImageList, mobileImageList
        );
        if (channel.equalsIgnoreCase("web")) {
            savingWebContent(request, webImageList, content);

        } else {
            savingMobileContent(request, mobileImageList, content);

        }

    }

    private void savingMobileContent(ContentRequest request, List<MobileImage> mobileImageList, Content content) {
        if (null == request.getMobileImages()) {
            log.info("image should not be nulll");
            throw new NullPointerException();
        }
        var imageFiles = request.getMobileImages();
        try {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    processMobileImage(imageFile, mobileImageList, content);
                }
            }
            contentRepository.save(content);
        } catch (Exception ex) {
            log.error("Saving content to DB failed with the exception :: ", ex);
        }
    }

    private void savingWebContent(ContentRequest request, List<WebImage> webImageList, Content content) {
        if (null == request.getWebImages()) {
            log.info("image should not be nulll");
            throw new NullPointerException();
        }
        var imageFiles = request.getWebImages();
        try {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    processWebImage(imageFile, webImageList, content);
                }
            }
            contentRepository.save(content);
        } catch (Exception ex) {
            log.error("Saving content to DB failed with the exception :: ", ex);
        }
    }

    private void processMobileImage(MultipartFile imageFile, List<MobileImage> mobileImageList, Content content) {
        try {
            MobileImage image = new MobileImage();
            image.setImageData(imageFile.getBytes());
            image.setContent(content);
            mobileImageList.add(image);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    private void processWebImage(MultipartFile imageFile, List<WebImage> webImageList, Content content) {
        try {
            WebImage image = new WebImage();
            image.setImageData(imageFile.getBytes());
            image.setContent(content);
            webImageList.add(image);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    public List<Content> getContent(List<String> byStatus, LocalDateTime from, LocalDateTime to) {
        log.info("from :: {} to :: {} byStatus :: {}", from, to, byStatus);
        try {
            Specification<Content> spec = (root, query, builder) -> {
                var predicates = new ArrayList<Predicate>();
                if (byStatus != null && !byStatus.isEmpty()) {
                    var status = root.get("status");
                    var lowerCaseStatus = builder.lower(status.as(String.class));
                    var lowerCaseStatuses = byStatus.stream().map(String::toLowerCase).toList();
                    predicates.add(lowerCaseStatus.in(lowerCaseStatuses));
                }
                if (to != null) {
                    predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), to));
                }
                if (from != null) {
                    predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), from));
                }
                return builder.and(predicates.toArray(new Predicate[0]));
            };
            return contentRepository.findAll(spec);
        } catch (Exception ex) {
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
        return dto;
    }

    // @Transactional
    public void deleteContent(String id) {
        try {
            if (!contentRepository.existsById(id)) {
                throw new EntityNotFoundException("Content not found");
            }
            contentRepository.deleteById(id);
        } catch (Exception ex) {
            log.error("deleting content failed with exception :: ", ex);
        }
    }

    // @Transactional
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

        } catch (Exception ex) {
            log.error("Updating content by id failed with exception :: ", ex);
            return null;
        }
    }

    // @Transactional(readOnly = true)
    public List<ImageDTO> getWebImagesByContentId(String contentId) {
        return webImageRepository.findByContentId(contentId)
                .stream()
                .map(image -> new ImageDTO(
                image.getId(),
                image.getImageData(),
                image.getExpiresAt(),
                image.getCreatedAt()
        ))
                .toList();
    }

    // @Transactional(readOnly = true)
    public List<ImageDTO> getMobileImagesByContentId(String contentId) {
        return mobileImageRepository.findByContentId(contentId)
                .stream()
                .map(image -> new ImageDTO(
                image.getId(),
                image.getImageData(),
                image.getExpiresAt(),
                image.getCreatedAt()
        ))
                .toList();
    }

}
