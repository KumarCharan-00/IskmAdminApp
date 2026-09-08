package org.iskm.admin.web.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "seva_sub_type")
public class SevaSubType {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "seva_id", nullable = false)
    private Seva seva;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "is_general_donation", nullable = false)
    private Boolean isGeneralDonation = false;
}
