package com.example.post;

import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * PostController - 게시글 API (게시글 서비스, 포트 8082)
 *
 *   주소                  방식     로그인   하는 일
 *   /api/posts            GET      -        글 목록 (?user_id=3 이면 그 사람 글만)
 *   /api/posts/{id}       GET      -        글 하나
 *   /api/posts            POST     필요     새 글 (작성자 = 토큰 주인)
 *   /api/posts/{id}       PUT      본인     글 수정
 *   /api/posts/{id}       DELETE   본인     글 삭제 (204)
 *
 * [MSA 개념] 게이트웨이가 붙여 주는 헤더 두 개
 *   X-User-Id       : 로그인한 사람의 번호       예) 3
 *   X-User-Nickname : 로그인한 사람의 닉네임     예) %EB%AF%BC%EC%88%98  (= "민수" 를 URL 인코딩한 글자)
 *     HTTP 헤더에는 영문·숫자만 안전하게 실을 수 있어서, 게이트웨이가 한글을 %xx 모양으로 바꿔 보낸다.
 *     여기서 URLDecoder 로 다시 "민수" 로 되돌린다.
 *   모놀리식 판의 @AuthenticationPrincipal LoginUser 대신, 평범한 @RequestHeader 로 받는다.
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public List<PostResponse> list(@RequestParam(name = "user_id", required = false) Long userId) {
		return postService.findAll(userId); // ?user_id=3 (없어도 된다)
	}

	@GetMapping("/{id}")
	public PostResponse get(@PathVariable Long id) {
		return postService.findOne(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PostResponse create(@Valid @RequestBody PostRequest request,
			@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-User-Nickname") String encodedNickname) {
		String nickname = URLDecoder.decode(encodedNickname, StandardCharsets.UTF_8); // %EB%AF%BC... → 민수
		return postService.create(request, userId, nickname);
	}

	@PutMapping("/{id}")
	public PostResponse update(@PathVariable Long id, @Valid @RequestBody PostRequest request,
			@RequestHeader("X-User-Id") Long userId) {
		return postService.update(id, request, userId);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
		postService.delete(id, userId);
	}
}
