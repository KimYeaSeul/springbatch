package com.kinx.batchprogram.batch;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.kinx.batchprogram.domain.Two;
import com.kinx.batchprogram.dto.OneDto;
import com.kinx.batchprogram.dto.TwoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob3 {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	private static final int chunkSize=5;
	
	@Bean
	public Job CsvToJpaJob3_batchBuild() throws Exception {
		return jobBuilderFactory.get("CsvToJpaJob3")
				.start(CsvToJpaJob3_batchStep1())
				.build();
	}
	
	@Bean
	public Step CsvToJpaJob3_batchStep1() throws Exception {
		return stepBuilderFactory.get("CsvToJpaJob3_batchStep1")
				.<TwoDto, Two>chunk(chunkSize)
				.reader(csvToJpaJob3_Reader(null))
				.processor(csvToJpaJob3_processor())
				.writer(csvToJpaJob3_dbItemWirter())
				.build();
	}


	@Bean
	@StepScope
	public FlatFileItemReader<TwoDto> csvToJpaJob3_Reader(@Value("#{jobParameters[inFileName]}")String inFileName) {
		return new FlatFileItemReaderBuilder<TwoDto>()
				.name("csvToJpaJob3_Reader")
				.resource(new FileSystemResource(inFileName))
				.delimited().delimiter(":")
				.names("one","two")
				.targetType(TwoDto.class)
				.recordSeparatorPolicy(new SimpleRecordSeparatorPolicy(){
					@Override
					public String postProcess(String record){
						if(record.indexOf(":") == -1){
							return null;
						}
						return record.trim();
					}
				}).build();
	}
	@Bean
	public JpaItemWriter<Two> csvToJpaJob3_dbItemWirter(){
		JpaItemWriter<Two> jpaItemWriter = new JpaItemWriter<>();
		jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
		return jpaItemWriter;
	}
	@Bean
	public ItemProcessor<TwoDto, Two> csvToJpaJob3_processor(){
		return twoDto -> new Two(twoDto.getOne(), twoDto.getTwo());
	}
	
}
