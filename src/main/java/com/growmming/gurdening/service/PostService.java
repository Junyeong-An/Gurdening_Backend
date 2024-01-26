package com.growmming.gurdening.service;

import com.growmming.gurdening.domain.dto.PostDTO;
import com.growmming.gurdening.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

//    public Slice<PostDTO.Response> findAll(Pageable pageable) {
//        return postRepository.findAll(pageable)
//    }

}
