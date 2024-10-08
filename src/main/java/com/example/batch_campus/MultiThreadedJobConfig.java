package com.example.batch_campus;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
//
//@Configuration
public class MultiThreadedJobConfig {

    @Bean
    public Job job(
        JobRepository jobRepository,
        Step step
    ){
        return new JobBuilder("multiThreadjob",jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step(
        JobRepository jobRepository,
        PlatformTransactionManager platformTransactionManager,
        JpaPagingItemReader<User> jpaPagingItemReader
    ){
        return new StepBuilder("step",jobRepository)
                .<User,User>chunk(5,platformTransactionManager)
                .reader(jpaPagingItemReader)
                .writer(result -> log.info(result.toString()))
                .taskExecutor(new SimpleAsyncTaskExecutor()) //비동기적 실행
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> jpaPagingItemReader(
        EntityManagerFactory entityManagerFactory
    ){
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(5)
                .saveState(false) //비동기적 실행할때 반드시 이걸 넣어야 됌 실패지점 때문에
                .queryString("select u from User u order by u.id")
                .build();

    }

}
