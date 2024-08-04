package gdsc.cau.puangbe.user.entity;

import gdsc.cau.puangbe.photo.entity.PhotoRequest;
import gdsc.cau.puangbe.photo.entity.PhotoResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String userName;

    private LocalDateTime createDate;

    private LocalDateTime requestDate;

    private String kakaoId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<PhotoResult> photoResult = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<PhotoRequest> photoRequest = new ArrayList<>();

    @Builder
    public User(String userName, LocalDateTime createDate, LocalDateTime requestDate, String kakaoId){
        this.userName = userName;
        this.createDate = createDate;
        this.requestDate = requestDate;
        this.kakaoId = kakaoId;
    }

    public void updateRequestDate(LocalDateTime requestDate){
        this.requestDate = requestDate;
    }
}
