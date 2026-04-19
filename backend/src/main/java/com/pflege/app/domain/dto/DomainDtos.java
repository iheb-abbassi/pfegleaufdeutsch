package com.pflege.app.domain.dto;

public class DomainDtos {

    public record DomainResponse(Long id, String name, String slug, long totalQuestions, long masteredQuestions, int progressPercent) {
    }

    public record DomainRequest(String name, String slug) {
    }
}
