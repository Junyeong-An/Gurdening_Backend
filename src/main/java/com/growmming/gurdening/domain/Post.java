package com.growmming.gurdening.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Post extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int viewcount;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String creationdate;

    @Column(nullable = false)
    private int likecount;




}