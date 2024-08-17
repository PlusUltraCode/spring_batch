package com.example.batch_campus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
//@Configuration
public class JobConfiguration {

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job-chunk", jobRepository)
                .start(step)
                .build();
    }

//    @Bean
//    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
//
//        return new StepBuilder("step",jobRepository)
//                .tasklet((contribution, chunkContext) ->{
//                    log.info("step 실행");
//                    return RepeatStatus.FINISHED;
//                },platformTransactionManager)
//                .allowStartIfComplete(true) //성공해도 시작을 허락하는 원래는 이름이 같으면 하나만 작업하는데 이놈 적으면 계속 가능함
//                .startLimit(5)
//                .build();
//    }


    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {

        ItemReader<Integer> itemReader = new ItemReader<>() {

            private int count = 0;

            @Override
            public Integer read() {
                count++;

                log.info("Read {}", count);
                if(count==20)return null;

                return count;
            }
        };

        ItemProcessor<Integer, Integer> itemProcessor = new ItemProcessor<>() {
            @Override
            public Integer process(Integer item) throws Exception {
                if(item==15)
                    throw new IllegalStateException();

                return item;

            }
        };


        return new StepBuilder("step", jobRepository)
                .<Integer,Integer>chunk(10,platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(read -> {})
                .faultTolerant()
                .retry(IllegalStateException.class)
                .retryLimit(5)
                .build();
    }
}
