package com.kinx.batchprogram.batch;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import com.kinx.batchprogram.domain.Dept;
import com.kinx.batchprogram.dto.TwoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob2 {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	private int chunkSize = 10 ;
	
	// multi file 작업 시 필요
	private final ResourceLoader resourceLoader;
	
	@Bean
	public Job csvToJpaJob2_batchBuild() throws Exception {
		return jobBuilderFactory.get("csvToJpaJob2_batchBuild")
				.start(csvToJpaJob2_batchStep1())
				.build();
	}
	
	@Bean
	public Step csvToJpaJob2_batchStep1() throws Exception {
		return stepBuilderFactory.get("csvToJpaJob2_batchStep1")
				.<TwoDto, Dept>chunk(chunkSize)
				.reader(csvToJpaJob2_FileReader())
				.processor(csvToJpaJob2_processor())
				.writer(csvToJpaJob2_dbItemWriter())
				.faultTolerant() // 에러 처리
				.skip(FlatFileParseException.class) // 넘어갈 에러정의
				.skipLimit(2) // 2개이상 에러 발생 시 못넘어감
				.build();

	}
	
	@Bean
	public MultiResourceItemReader<TwoDto> csvToJpaJob2_FileReader(){
		MultiResourceItemReader<TwoDto> twoDtoMultiResourceIteReader = new MultiResourceItemReader<>();
		try {
			twoDtoMultiResourceIteReader.setResources(
					ResourcePatternUtils.getResourcePatternResolver(this.resourceLoader).getResources(
								"classpath:sample/csvToJpaJob2/*.txt"
					)
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		twoDtoMultiResourceIteReader.setDelegate(multiFileItemReader());
		return twoDtoMultiResourceIteReader;
	}
	
	@Bean
	public JpaItemWriter<Dept> csvToJpaJob2_dbItemWriter(){
		JpaItemWriter<Dept> jpaItemWriter = new JpaItemWriter<>();
		jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
		return jpaItemWriter;
		
	}

	@Bean
	public ItemProcessor<TwoDto, Dept> csvToJpaJob2_processor() {
		return twoDto -> new Dept(Integer.parseInt(twoDto.getOne()), twoDto.getTwo(), "기타");
	}
	
	
	@Bean
	public FlatFileItemReader<TwoDto> multiFileItemReader(){
		FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setLineMapper((line, lineNumber) -> {
			String[] lines= line.split("#");
			return new TwoDto(lines[0], lines[1]);
		});
		return flatFileItemReader;
	}
}
