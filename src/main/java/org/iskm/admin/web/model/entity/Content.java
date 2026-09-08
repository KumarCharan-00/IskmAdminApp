package org.iskm.admin.web.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "content")
public class Content {

    @Id
    private String id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "quote")
    private String quote;

    @Column(name = "short_text", columnDefinition = "TEXT")
    private String shortText;

    @Column(name = "full_text", columnDefinition = "TEXT")
    private String fullText;

    @Column(name = "location")
    private String location;

    @Column(name = "show_donation")
    private Boolean showDonation;

    @Column(name = "status", nullable = false)
    private String status = "draft";

    @Column(name = "show_from_date")
    private LocalDate showFromDate;

    @Column(name = "show_to_date")
    private LocalDate showToDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ToString.Exclude
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "seva_id")
    private Seva seva;

    @ManyToOne
    @JoinColumn(name = "seva_sub_type_id")
    private SevaSubType sevaSubType;

    @Column(name = "button_text")
    private String buttonText;

    @Column(name = "button_href")
    private String buttonHref;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        Content content = (Content) o;
        return getId() != null && Objects.equals(getId(), content.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Content [id=" + id + ", type=" + type + ", title=" + title + ", quote=" + quote + ", shortText=" + shortText + ", fullText=" + fullText + ", location=" + location + ", showDonation=" + showDonation + ", status=" + status + ", showFromDate=" + showFromDate + ", showToDate=" + showToDate + ", createdAt=" + createdAt + ", buttonText=" + buttonText + ", buttonHref=" + buttonHref + "]";
    }
}
