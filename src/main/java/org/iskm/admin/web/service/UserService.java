package org.iskm.admin.web.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.AddUserResponse;
import org.iskm.admin.web.dto.res.ContentDTO;
import org.iskm.admin.web.dto.res.ContentDTO.ImageDTO;
import org.iskm.admin.web.dto.res.ErrorDTO;
import org.iskm.admin.web.dto.res.Response;
import org.iskm.admin.web.model.ContentRequest;
import org.iskm.admin.web.model.ContentUpdateRequest;
import org.iskm.admin.web.model.entity.Content;
import org.iskm.admin.web.model.entity.Image;
import org.iskm.admin.web.model.entity.User;
import org.iskm.admin.web.repository.ContentRepository;
import org.iskm.admin.web.repository.ImageRepository;
import org.iskm.admin.web.repository.UserRepo;
import org.iskm.admin.web.repository.UserRepository;
import org.iskm.admin.web.util.CommonUtil;
import org.iskm.admin.web.util.PasswordUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
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
    private final ImageRepository imageRepository;

    public UserService(UserRepo repo, CommonUtil commonUtil, ContentRepository contentRepository,
            UserRepository userRepository, ImageRepository imageRepository) {
        this.repo = repo;
        this.commonUtil = commonUtil;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    public Response saveUser(@NonNull AddUserDTO addUserDTO) {
        AddUserResponse res;
        try {
            var creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            addUserDTO.setPassword(PasswordUtil.encodePassword(addUserDTO.getPassword()));
            addUserDTO.setUserId(CommonUtil.generateUUID());
            var userEntity = commonUtil.mapEntityData(addUserDTO, creationTime);
            if (Objects.nonNull(userEntity)) {
                repo.save(userEntity);
                res = new AddUserResponse(true);
            } else {
                res = new AddUserResponse(false);
            }
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

    public Response saveContent(ContentRequest request) {
        List<Image> imageList = new ArrayList<>();
        var content = new Content(
                CommonUtil.generateUUID(),
                request.getType(),
                request.getTitle(),
                request.getQuote(),
                request.getShortText(),
                request.getFullText(),
                request.getLocation(),
                request.getShowDonation(),
                request.getStatus() != null ? request.getStatus() : "draft",
                request.getShowFromDate(),
                request.getShowToDate(),
                LocalDateTime.now(),
                imageList
        );
        log.info("Content Request: {}", request);
        if (request.getImages() != null
                && !request.getImages().isEmpty()) {
            savingImageContent(request, imageList, content);
        } else {
            log.info("Images not added");
        }
        try {
            var val = contentRepository.save(content);
            log.info("Content saved to DB");
            return toContentDTO(val);
        } catch (Exception ex) {
            log.error("Saving content to DB failed with the exception :: ", ex);
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setFailureMsg("Saving content to DB failed with the exception :: " + ex.getMessage());
            return errorDTO;
        }
    }

    private void savingImageContent(ContentRequest request, List<Image> imageList, @NonNull Content content) {
        if (null == request.getImages() || request.getImages().isEmpty()) {
            log.info("image should not be null");
            throw new RuntimeException("image should not be null or Empty");
        }
        log.info("Saving images to DB");
        var imageFiles = request.getImages();
        log.info("Image files count: {}", imageFiles.size());
        for (MultipartFile imageFile : imageFiles) {
            if (imageFile != null && !imageFile.isEmpty()) {
                processImage(imageFile, imageList, content);
            }
        }
    }

    private void processImage(MultipartFile imageFile, List<Image> imageList, Content content) {
        try {
            Image image = new Image();
            image.setImageData(imageFile.getBytes());
            image.setContent(content);
            imageList.add(image);
            log.info("Image added to list (Processed)");
        } catch (IOException e) {
            log.error("Failed to process image", e);
            throw new RuntimeException("Failed to process image", e);
        } catch (Exception ex) {
            log.error("Saving content to DB failed with the exception :: ", ex);
        }
    }

    public List<Content> getContent(List<String> types, List<String> byStatus, LocalDateTime from, LocalDateTime to, Integer k) {
        log.info("from :: {} to :: {} byStatus :: {}", from, to, byStatus);
        List<Content> contentList = new ArrayList<>();
        try {
            if (types != null && !types.isEmpty()) {
                types.stream().distinct().forEach(type
                        -> contentList.addAll(fetchContent(type, byStatus, from, to, k))
                );
            } else {
                contentList.addAll(fetchContent(null, byStatus, from, to, k));
            }
            return contentList;
        } catch (Exception ex) {
            log.error("Fetching content failed with exception ::", ex);
            return List.of();
        }
    }

    private List<Content> fetchContent(String type, List<String> byStatus, LocalDateTime from, LocalDateTime to, Integer k) {
        Specification<Content> spec = (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (type != null && !type.isBlank()) {
                predicates.add(builder.equal(builder.upper(root.get("type")), type.toUpperCase()));
            }
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
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        if (k != null && k != 0) {
            return contentRepository.findAll(spec, PageRequest.of(0, k, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        } else {
            return contentRepository.findAll(spec);
        }
    }

    public ContentDTO toContentDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setType(content.getType());
        dto.setTitle(content.getTitle());
        dto.setQuote(content.getQuote());
        dto.setPreviewText(content.getShortText());
        dto.setFullText(content.getFullText());
        dto.setStatus(content.getStatus());
        dto.setShowFromDate(content.getShowFromDate());
        dto.setShowToDate(content.getShowToDate());
        dto.setShowDonation(content.getShowDonation());
        dto.setLocation(content.getLocation());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setImages(mapImages(content.getImages()));
        return dto;
    }

    @Transactional
    public void deleteContent(@NonNull String id) {
        try {
            if (!contentRepository.existsById(id)) {
                throw new EntityNotFoundException("Content not found");
            }
            contentRepository.deleteById(id);
        } catch (EntityNotFoundException enf) {
            log.error("Content with id {} not found :: ", id, enf);
        } catch (Exception ex) {
            log.error("deleting content failed with exception :: ", ex);
        }
    }

    @Transactional
    public ContentDTO partialUpdateById(@NonNull String id, ContentUpdateRequest request) {
        try {
            Content content = contentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Content not found with id: " + id));

            updateContentFields(content, request);

            Objects.requireNonNull(content);
            contentRepository.save(content);
            return toContentDTO(content);

        } catch (Exception ex) {
            log.error("Updating content by id failed with exception :: ", ex);
            return null;
        }
    }

    private void updateContentFields(Content content, ContentUpdateRequest request) {
        updateStringField(content::setType, request.getType());
        updateStringField(content::setTitle, request.getTitle());
        updateStringField(content::setQuote, request.getQuote());
        updateStringField(content::setShortText, request.getPreviewText());
        updateStringField(content::setFullText, request.getFullText());
        updateStringField(content::setStatus, request.getStatus());
        updateStringField(content::setLocation, request.getLocation());

        if (request.getShowFromDate() != null) {
            content.setShowFromDate(request.getShowFromDate());
        }
        if (request.getShowToDate() != null) {
            content.setShowToDate(request.getShowToDate());
        }
        if (request.getShowDonation() != null) {
            content.setShowDonation(request.getShowDonation());
        }
    }

    private void updateStringField(Consumer<String> setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }

    @Transactional(readOnly = true)
    public List<ImageDTO> getImagesByContentId(String contentId) {
        return imageRepository.findByContentId(contentId)
                .stream()
                .map(image -> new ImageDTO(
                image.getId(),
                image.getImageData(),
                image.getExpiresAt(),
                image.getCreatedAt()
        )).toList();
    }

    private List<ImageDTO> mapImages(List<Image> images) {
        return images.stream()
                .map(image -> new ImageDTO(
                image.getId(),
                image.getImageData(),
                image.getExpiresAt(),
                image.getCreatedAt()
        ))
                .toList();
    }
}
