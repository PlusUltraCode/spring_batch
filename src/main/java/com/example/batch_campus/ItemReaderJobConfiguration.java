package com.example.batch_campus;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLOutput;

//@Configuration
public class ItemReaderJobConfiguration {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ){
        return new JobBuilder("itemReaderJob",jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            ItemReader<User> jpaCursorItemReader
    ){
        return new StepBuilder("step",jobRepository)
                .<User,User>chunk(2,platformTransactionManager)
                .reader(jpaCursorItemReader)
                .writer(System.out::println)
                .build();
    }

    @Bean
    public FlatFileItemReader<User> flatFileItemReader(){
        return new FlatFileItemReaderBuilder<User>()
                .name("flatFileItemReader")
                .resource(new ClassPathResource("users.txt"))
                .linesToSkip(2)
                .delimited().delimiter(",")
                .names("name","age","region","telephone")
                .targetType(User.class)
                .strict(true)

                .build();
    }


    @Bean
    public FlatFileItemReader<User> fixedLengthflatFileItemReader(){
        return new FlatFileItemReaderBuilder<User>()
                .name("fixedLengthflatFileItemReader")
                .resource(new ClassPathResource("usersFixedLength.txt"))
                .linesToSkip(2)
                .fixedLength()
                .columns(new Range[]{new Range(1,2), new Range(3,4), new Range(5,6), new Range(7,19)})
                .names("name","age","region","telephone")
                .targetType(User.class)
                .strict(true)

                .build();
    }


    @Bean
    public JsonItemReader<User> jsonItemReader(){
        return new JsonItemReaderBuilder<User>()
                .name("jsonItemReader")
                .resource(new ClassPathResource("users.json"))
                .jsonObjectReader(new JacksonJsonObjectReader<>(User.class))
                .build();
    }

    @Bean
    public ItemReader<User> jpaPagingItemReader(
            EntityManagerFactory entityManagerFactory
    ){
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(3)
                .queryString("SELECT u FROM User u Order by u.id")
                .build();

    }

    @Bean
    public ItemReader<User> jpaCursorItemReader(
            EntityManagerFactory entityManagerFactory
    ){
        return new JpaPagingItemReaderBuilder<User>()
                .name("japCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User u Order by u.id")
                .build();
    }
}
