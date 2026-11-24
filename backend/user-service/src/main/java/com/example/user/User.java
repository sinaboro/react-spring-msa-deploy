package com.example.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * User - 회원 Entity (userdb 의 users 테이블)
 *
 *   id             bigint        자동 번호
 *   username       varchar(30)   로그인 아이디 (UNIQUE: 중복 불가)
 *   password_hash  varchar(255)  비밀번호가 아니라 "해시" 를 저장한다 (BCrypt)
 *   nickname       varchar(30)   화면에 보일 이름
 *   created_at     timestamp     가입 시각
 *
 * 모놀리식(한 서버) 판과 달라진 점이 없다. 회원 정보는 회원 서비스가 혼자 책임진다.
 * 테이블 이름을 user 가 아니라 users 로 정한 이유: PostgreSQL 에서 user 는 예약어라서.
 */
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String username;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false, length = 30)
	private String nickname;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected User() { // JPA 가 DB 에서 읽어 올 때 쓰는 빈 생성자
	}

	public User(String username, String passwordHash, String nickname) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.nickname = nickname;
	}

	@PrePersist // INSERT 직전에 자동 실행
	void onCreate() {
		createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS); // PostgreSQL 저장 단위(마이크로초)에 맞춘다
	}

	public Long getId() { return id; }
	public String getUsername() { return username; }
	public String getPasswordHash() { return passwordHash; }
	public String getNickname() { return nickname; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
