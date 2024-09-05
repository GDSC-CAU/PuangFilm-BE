package gdsc.cau.puangbe.photo.entity;

import gdsc.cau.puangbe.common.enums.Gender;
import gdsc.cau.puangbe.common.enums.RequestStatus;
import gdsc.cau.puangbe.user.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PhotoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    @Nullable
    private PhotoResult photoResult;

    private RequestStatus status;

    private String email;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private Gender gender;

    @OneToMany(mappedBy = "request", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<PhotoOrigin> photoUrls = new ArrayList<>();

    @Version
    private Long version;

    @Builder
    public PhotoRequest(User user, Gender gender, List<String> urls, String email) {
        this.user = user;
        this.gender = gender;
        this.status = RequestStatus.WAITING;
        this.email = email;
        this.photoUrls = urls.stream().map(PhotoOrigin::new).toList();
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    public void finishStatus() {
        this.status = RequestStatus.FINISHED;
        this.updateDate = LocalDateTime.now();
    }

    public void modifyEmail(String email) {
        this.email = email;
        this.updateDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PhotoRequest{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", version=" + version +
                '}';
    }
}
