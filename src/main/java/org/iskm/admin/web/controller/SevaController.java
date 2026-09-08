package org.iskm.admin.web.controller;

import java.util.List;
import java.util.UUID;
import org.iskm.admin.web.model.entity.Seva;
import org.iskm.admin.web.model.entity.SevaSubType;
import org.iskm.admin.web.repository.SevaRepository;
import org.iskm.admin.web.repository.SevaSubTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.iskm.admin.web.util.CommonUtil;

import lombok.Data;

@RestController
@RequestMapping("/api/sevas")
public class SevaController {

    private final SevaRepository sevaRepository;
    private final SevaSubTypeRepository sevaSubTypeRepository;

    public SevaController(SevaRepository sevaRepository, SevaSubTypeRepository sevaSubTypeRepository) {
        this.sevaRepository = sevaRepository;
        this.sevaSubTypeRepository = sevaSubTypeRepository;
    }

    @GetMapping
    public ResponseEntity<List<Seva>> getAllSevas() {
        return ResponseEntity.ok(sevaRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Seva> createSeva(@RequestBody SevaRequest request) {
        Seva seva = new Seva();
        seva.setId(CommonUtil.generateUUID());
        seva.setName(request.getName());
        return ResponseEntity.ok(sevaRepository.save(seva));
    }

    @GetMapping("/{sevaId}/subtypes")
    public ResponseEntity<List<SevaSubType>> getSubTypes(@PathVariable String sevaId) {
        return ResponseEntity.ok(sevaSubTypeRepository.findBySevaId(sevaId));
    }

    @PostMapping("/{sevaId}/subtypes")
    public ResponseEntity<SevaSubType> createSubType(@PathVariable String sevaId, @RequestBody SevaSubTypeRequest request) {
        Seva seva = sevaRepository.findById(sevaId).orElseThrow(() -> new RuntimeException("Seva not found"));
        SevaSubType subType = new SevaSubType();
        subType.setId(CommonUtil.generateUUID());
        subType.setSeva(seva);
        subType.setName(request.getName());
        subType.setAmount(request.getAmount());
        subType.setIsGeneralDonation(request.getIsGeneralDonation() != null ? request.getIsGeneralDonation() : false);
        return ResponseEntity.ok(sevaSubTypeRepository.save(subType));
    }

    @Data
    public static class SevaRequest {
        private String name;
    }

    @Data
    public static class SevaSubTypeRequest {
        private String name;
        private Double amount;
        private Boolean isGeneralDonation;
    }
}
