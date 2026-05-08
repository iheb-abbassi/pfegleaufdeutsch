package com.pflege.app.domain.service;

import com.pflege.app.common.ApiException;
import com.pflege.app.domain.dto.DomainDtos;
import com.pflege.app.domain.entity.Domain;
import com.pflege.app.domain.entity.Question;
import com.pflege.app.domain.entity.UserQuestionProgress;
import com.pflege.app.domain.repository.DomainRepository;
import com.pflege.app.domain.repository.QuestionRepository;
import com.pflege.app.domain.repository.UserQuestionProgressRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DomainService {

    private final DomainRepository domainRepository;
    private final QuestionRepository questionRepository;
    private final UserQuestionProgressRepository progressRepository;

    public DomainService(DomainRepository domainRepository, QuestionRepository questionRepository, UserQuestionProgressRepository progressRepository) {
        this.domainRepository = domainRepository;
        this.questionRepository = questionRepository;
        this.progressRepository = progressRepository;
    }

    public List<DomainDtos.DomainResponse> listForUser(Long userId) {
        List<UserQuestionProgress> progressList = progressRepository.findByUserIdWithQuestionAndDomain(userId);
        List<Question> allQuestions = questionRepository.findAllActiveWithOptions();
        return domainRepository.findAll().stream()
                .map(domain -> {
                    long total = allQuestions.stream().filter(question -> question.getDomain().getId().equals(domain.getId())).count();
                    long mastered = progressList.stream()
                            .filter(progress -> progress.isMastered() && progress.getQuestion().getDomain().getId().equals(domain.getId()))
                            .count();
                    int percent = total == 0 ? 0 : (int) ((mastered * 100) / total);
                    return new DomainDtos.DomainResponse(domain.getId(), domain.getName(), domain.getSlug(), total, mastered, percent);
                })
                .toList();
    }

    @Transactional
    public DomainDtos.DomainResponse create(DomainDtos.DomainRequest request) {
        Domain domain = new Domain();
        domain.setName(request.name());
        domain.setSlug(request.slug());
        domainRepository.save(domain);
        return new DomainDtos.DomainResponse(domain.getId(), domain.getName(), domain.getSlug(), 0, 0, 0);
    }

    @Transactional
    public DomainDtos.DomainResponse update(Long id, DomainDtos.DomainRequest request) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Domain not found"));
        domain.setName(request.name());
        domain.setSlug(request.slug());
        return new DomainDtos.DomainResponse(domain.getId(), domain.getName(), domain.getSlug(), 0, 0, 0);
    }

    public List<DomainDtos.DomainResponse> listAdmin() {
        return domainRepository.findAll().stream()
                .map(domain -> new DomainDtos.DomainResponse(domain.getId(), domain.getName(), domain.getSlug(), 0, 0, 0))
                .toList();
    }
}
