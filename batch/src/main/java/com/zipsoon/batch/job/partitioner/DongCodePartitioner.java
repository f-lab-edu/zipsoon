package com.zipsoon.batch.job.partitioner;

import com.zipsoon.batch.domain.DongCode;
import com.zipsoon.batch.service.DongCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class DongCodePartitioner implements Partitioner {
    private final DongCodeService dongCodeService;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<DongCode> dongCodes = dongCodeService.getAllDongCodes();
        if (dongCodes.isEmpty()) {
            throw new IllegalStateException("No dong codes available for processing");
        }

        Map<String, ExecutionContext> partitions = new HashMap<>();
        int totalCodes = dongCodes.size();
        int chunkSize = calculateChunkSize(totalCodes, gridSize);

        createPartitions(dongCodes, chunkSize, partitions);
        log.info("Created {} partitions with chunk size {}", partitions.size(), chunkSize);

        return partitions;
    }

    private int calculateChunkSize(int totalItems, int gridSize) {
        return (int) Math.ceil((double) totalItems / gridSize);
    }

    private void createPartitions(List<DongCode> dongCodes, int chunkSize,
                                Map<String, ExecutionContext> partitions) {
        for (int i = 0; i < dongCodes.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, dongCodes.size());
            List<DongCode> partitionCodes = new ArrayList<>(dongCodes.subList(i, end));

            ExecutionContext context = new ExecutionContext();
            context.put("dongCodes", partitionCodes);
            context.put("partitionId", i / chunkSize);

            partitions.put("partition" + (i / chunkSize), context);
        }
    }
}