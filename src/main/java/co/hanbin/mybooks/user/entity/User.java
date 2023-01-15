package co.hanbin.mybooks.user.entity;

import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;

import lombok.Data;

@Entity
@Data
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NO", unique = true, nullable = false)
	private Integer userNo;

	@Column(name = "JOIN_MTHD_NO", unique = true, nullable = false)
	private Integer joinMthdNo;

	@Column(name = "USER_PW", unique = false, nullable = false)
	private String userPw;

	@Column(name = "USER_JOIN_DT", unique = false, nullable = false)
	private String userJoinDt;

	@Column(name = "USER_MOD_DT", unique = false, nullable = false)
	private String userModDt;

	@Column(name = "USER_ID", unique = true, nullable = false)
	private String userId;

	@Column(name = "USER_CLS", unique = false, nullable = false)
	private Integer userCls;
	
    @Column(name = "USER_NM", unique = false, nullable = true)
	private String userNm;

	@Column(name = "USER_IMG", unique = false, nullable = true)
	private String userImg;

	@Transient
	private List<String> auths;

	@Transient
	private Collection<GrantedAuthority> roles;

	@Transient
	private String accessToken;

	@Transient
	private String accessTokenExpire;

	@Transient
	private String refreshToken;

	@Transient
	private String refreshTokenExpire;

}
