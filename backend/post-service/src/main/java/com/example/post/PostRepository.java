package com.example.post;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PostRepository - posts 테이블 창구
 *
 *   findAllByOrderByIdDesc()           전체 글, 최근 글이 위로        → ORDER BY id DESC
 *   findAllByUserIdOrderByIdDesc(3)    3번 회원의 글만 ("내 글" 보기) → WHERE user_id = 3
 *   findById(5)                        글 하나 (JpaRepository 기본 기능)
 *
 * 모놀리식 판에 있던 @EntityGraph(작성자 JOIN)가 없다.
 * 닉네임이 posts 테이블 안(writer)에 있으니 JOIN 할 필요가 없다 → 쿼리가 오히려 단순해졌다.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

	List<Post> findAllByOrderByIdDesc();

	List<Post> findAllByUserIdOrderByIdDesc(Long userId);
}
