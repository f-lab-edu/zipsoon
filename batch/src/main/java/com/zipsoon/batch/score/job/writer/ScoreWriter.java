package com.zipsoon.batch.score.job.writer;

import com.zipsoon.batch.score.model.EstateScore;
import com.zipsoon.batch.score.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScoreWriter implements ItemWriter<List<EstateScore>> {
    private final ScoreRepository scoreRepository;

    @Override
    public void write(Chunk<? extends List<EstateScore>> chunk) {
        List<EstateScore> scores = chunk.getItems().stream()
            .flatMap(List::stream)
            .toList();

        scoreRepository.saveAll(scores);
    }
}