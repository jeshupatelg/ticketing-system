package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    @NotBlank(message = "Comment content cannot be blank")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
