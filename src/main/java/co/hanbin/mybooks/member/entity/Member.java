package co.hanbin.mybooks.member.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.CreationTimestamp;

import co.hanbin.mybooks.member.enumerate.MemberRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "Members")
@Getter
@Setter
@ToString
public class Member {
    @Id
    @GeneratedValue
    private Long seq;

    @Column(unique = true)
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private MemberRole role = MemberRole.ROLE_NOT_PERMITTED;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date updatedAt;

    @Transient
	private String accessToken;

	@Transient
	private String accessTokenExpire;

	@Transient
	private String refreshToken;

	@Transient
	private String refreshTokenExpire;
}
