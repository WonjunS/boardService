package com.example.boardservice.service;

import com.example.boardservice.domain.BoardType;
import com.example.boardservice.domain.Member;
import com.example.boardservice.domain.Posts;
import com.example.boardservice.dto.request.PostsRequestDto;
import com.example.boardservice.dto.response.PostsResponseDto;
import com.example.boardservice.repository.MemberRepository;
import com.example.boardservice.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostsService {

    @Autowired
    private final MemberRepository memberRepository;

    @Autowired
    private final PostsRepository postsRepository;

    // 게시글 올리기
    public Long post(PostsRequestDto postsDto, String boardType, String email) throws Exception {
        Member member = memberRepository.findByEmail(email);

        if(member == null) {
            throw new EntityNotFoundException("User not found.");
        }

        if(boardType.equals("자유게시판")) postsDto.setBoardType(BoardType.FREE);
        if(boardType.equals("공지사항")) postsDto.setBoardType(BoardType.NOTICE);
        if(boardType.equals("질문게시판")) postsDto.setBoardType(BoardType.QUESTION);

        postsDto.setMember(member);

        Posts posts = postsDto.toEntity();
        postsRepository.save(posts);

        return posts.getId();
    }

    // 게시물 1개 불러오기
    public PostsResponseDto getPost(Long postsId) {
        Posts post = postsRepository.findById(postsId)
                .orElseThrow(EntityNotFoundException::new);

        return new PostsResponseDto(post);
    }

    // 게시물 전체 불러오기
    @Transactional(readOnly = true)
    public Page<PostsResponseDto> getAllPosts(Pageable pageable) {
        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1);
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());

        Page<PostsResponseDto> posts = postsRepository.findAll(pageable).map(PostsResponseDto::new);
        return posts;
    }

    // 특정 회원이 작성한 게시물 불러오기
    @Transactional(readOnly = true)
    public Page<PostsResponseDto> getAllPostsByWriter(String email, Pageable pageable) {
        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1);
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());

        Member member = memberRepository.findByEmail(email);

        Page<PostsResponseDto> posts = null;
        if(member != null) {
            posts = postsRepository.findAllByMember(member, pageable).map(PostsResponseDto::new);
        }

        return posts;
    }

    // 검색한 게시물 불러오기
    @Transactional(readOnly = true)
    public Page<PostsResponseDto> searchPosts(String keyword, Pageable pageable) {
        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1);
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());

        Page<PostsResponseDto> posts = postsRepository.findByTitleContaining(keyword, pageable).map(PostsResponseDto::new);
        return posts;
    }

    @Transactional(readOnly = true)
    public Page<PostsResponseDto> getAllFreePosts(Pageable pageable) {
        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1);
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());

        Page<PostsResponseDto> posts =
                postsRepository.findPostsByBoardType(BoardType.FREE, pageable).map(PostsResponseDto::new);

        return posts;
    }

    public Object getAllQuestionPosts(Pageable pageable) {
        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1);
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());

        Page<PostsResponseDto> posts =
                postsRepository.findPostsByBoardType(BoardType.QUESTION, pageable).map(PostsResponseDto::new);

        return posts;
    }

    public Object getAllNoticePosts(Pageable pageable) {
        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1);
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());

        Page<PostsResponseDto> posts =
                postsRepository.findPostsByBoardType(BoardType.NOTICE, pageable).map(PostsResponseDto::new);

        return posts;
    }

    // 게시물 수정하기
    @Transactional
    public void updatePost(Long id, String boardType, PostsRequestDto postsDto) {
        Posts post = postsRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id = " + id));

        if(boardType.equals("자유게시판")) postsDto.setBoardType(BoardType.FREE);
        if(boardType.equals("공지사항")) postsDto.setBoardType(BoardType.NOTICE);
        if(boardType.equals("질문게시판")) postsDto.setBoardType(BoardType.QUESTION);

        post.update(postsDto.getTitle(), postsDto.getBoardType(), postsDto.getContent());
    }

    // 게시물 삭제하기
    @Transactional
    public void deletePost(Long id) {
        postsRepository.deleteById(id);
    }

    // 게시물 조회수 업데이트
    @Transactional
    public int updateView(Long postsId) {
        return postsRepository.updateView(postsId);
    }

    @Transactional
    public int updateLikes(Long postsId) {
        return postsRepository.updateLikes(postsId);
    }

}
