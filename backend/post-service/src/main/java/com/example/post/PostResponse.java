package com.example.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * PostResponse - 글 하나의 응답 JSON
 *
 *   { "id": 1, "user_id": 3, "writer": "민수", "title": "...", "content": "...",
 *     "created_at": "...", "updated_at": "..." }
 *
 * 모놀리식 판과 JSON 모양이 "완전히 같다". 그래서 React 코드를 고치지 않아도 된다.
 * [MSA 개념] 서버 안쪽을 여러 서비스로 쪼개도, 바깥(React)에서 보이는 API 약속은 그대로 지킨다.
 */
public record PostResponse(
		Long id,
		@JsonProperty("user_id") Long userId,
		String writer,
		String title,
		String content,
		@JsonProperty("created_at") LocalDateTime createdAt,
		@JsonProperty("updated_at") LocalDateTime updatedAt
) {

	public static PostResponse from(Post post) {
		return new PostResponse(post.getId(), post.getUserId(), post.getWriter(),
				post.getTitle(), post.getContent(), post.getCreatedAt(), post.getUpdatedAt());
	}
}
