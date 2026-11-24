package com.example.post;

import com.example.post.common.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostService - 게시글 업무 규칙 (작성자 = 로그인한 사람, 수정·삭제는 본인만)
 *
 * 수정·삭제 검사 순서
 *   1. 글이 있는가?   없으면 404 "글을 찾을 수 없습니다"
 *   2. 내 글인가?     아니면 403 "본인이 쓴 글만 수정·삭제할 수 있습니다"
 *   (로그인 여부 401 은 이 서비스에 오기 전에 게이트웨이가 먼저 검사한다)
 *
 * [MSA 개념] 모놀리식 판에는 UserRepository 가 있었다 (작성자 User 를 찾으려고).
 *   지금은 users 테이블이 다른 서비스의 DB 에 있으므로 UserRepository 가 없다.
 *   글쓴이 번호·닉네임은 Controller 가 게이트웨이 헤더에서 꺼내 넘겨준다.
 */
@Service
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;

	public PostService(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	/** 글 목록. userId 가 있으면 그 사람 글만 */
	public List<PostResponse> findAll(Long userId) {
		List<Post> posts = (userId == null)
				? postRepository.findAllByOrderByIdDesc()
				: postRepository.findAllByUserIdOrderByIdDesc(userId);
		return posts.stream().map(PostResponse::from).toList();
	}

	public PostResponse findOne(Long id) {
		return PostResponse.from(getPost(id));
	}

	@Transactional
	public PostResponse create(PostRequest request, Long userId, String nickname) {
		// 글쓴이 번호와 닉네임을 글과 함께 저장한다 (다른 DB 의 users 를 볼 수 없으니 복사해 둔다)
		Post post = postRepository.save(new Post(userId, nickname, request.title().strip(), clean(request.content())));
		return PostResponse.from(post);
	}

	@Transactional
	public PostResponse update(Long id, PostRequest request, Long userId) {
		Post post = getPost(id); //   1. 있는가? (404)
		checkOwner(post, userId); //  2. 내 글인가? (403)
		post.update(request.title().strip(), clean(request.content()));
		postRepository.flush(); //    UPDATE 를 지금 실행 → updatedAt 이 채워진 값으로 응답
		return PostResponse.from(post);
	}

	@Transactional
	public void delete(Long id, Long userId) {
		Post post = getPost(id);
		checkOwner(post, userId);
		postRepository.delete(post);
	}

	private Post getPost(Long id) {
		return postRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "글을 찾을 수 없습니다"));
	}

	private void checkOwner(Post post, Long userId) {
		if (!post.isWrittenBy(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "본인이 쓴 글만 수정·삭제할 수 있습니다");
		}
	}

	private String clean(String content) {
		return content == null ? "" : content.strip();
	}
}
