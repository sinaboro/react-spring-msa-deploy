package com.example.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Post - 게시글 Entity (postdb 의 posts 테이블)
 *
 *   id          bigint        자동 번호
 *   user_id     bigint        글쓴이 회원 번호   ← 외래 키(FK)가 아닌 "그냥 숫자"
 *   writer      varchar(30)   글쓴이 닉네임      ← 글을 쓸 때 복사해 둔 값
 *   title       varchar(100)  제목
 *   content     text          내용
 *   created_at  timestamp     쓴 시각
 *   updated_at  timestamp     고친 시각
 *
 * [MSA 개념] 모놀리식 판과 가장 크게 달라진 곳
 *   모놀리식 : @ManyToOne User author  → posts.user_id 가 users.id 를 가리키는 FK, 닉네임은 JOIN 으로 읽음
 *   MSA      : Long userId + String writer
 *     - users 테이블은 다른 데이터베이스(userdb)에 있어서 FK 도 JOIN 도 불가능하다.
 *     - 그래서 글을 쓸 때 게이트웨이가 알려 준 번호와 닉네임을 이 테이블에 "복사해서" 저장한다.
 *       이렇게 다른 서비스의 데이터를 조금 복사해 두는 것을 "데이터 복제(비정규화)" 라고 한다.
 *     - 대신 닉네임을 바꿔도 예전 글의 writer 는 그대로다 (이 과정에는 닉네임 변경 기능이 없다).
 *       회원이 지워져도 DB 가 글을 함께 지워 주지 않는다 (ON DELETE CASCADE 를 쓸 수 없다).
 *       → 필요하면 "회원 탈퇴" 소식을 게시글 서비스에 알려 주는 기능을 따로 만들어야 한다.
 */
@Entity
@Table(name = "posts")
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, updatable = false)
	private Long userId; // 글쓴이 번호 (처음 쓴 사람으로 고정, 수정 불가)

	@Column(nullable = false, updatable = false, length = 30)
	private String writer; // 글쓴이 닉네임 (복사본)

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected Post() { // JPA 가 DB 에서 읽어 올 때 쓰는 빈 생성자
	}

	public Post(Long userId, String writer, String title, String content) {
		this.userId = userId;
		this.writer = writer;
		this.title = title;
		this.content = content;
	}

	@PrePersist // INSERT 직전에 자동 실행
	void onCreate() {
		createdAt = now();
		updatedAt = createdAt;
	}

	@PreUpdate // UPDATE 직전에 자동 실행
	void onUpdate() {
		updatedAt = now();
	}

	private static LocalDateTime now() {
		return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS); // PostgreSQL 저장 단위(마이크로초)에 맞춘다
	}

	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}

	/** 이 글을 쓴 사람이 userId 인가? (소유권 검사) */
	public boolean isWrittenBy(Long userId) {
		return this.userId.equals(userId);
	}

	public Long getId() { return id; }
	public Long getUserId() { return userId; }
	public String getWriter() { return writer; }
	public String getTitle() { return title; }
	public String getContent() { return content; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
}
