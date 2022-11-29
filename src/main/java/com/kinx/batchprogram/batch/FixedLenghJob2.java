package com.kinx.batchprogram.batch;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.kinx.batchprogram.custom.CustomPassThroughLineAggregator;
import com.kinx.batchprogram.domain.Dept;
import com.kinx.batchprogram.domain.Dept2;
import com.kinx.batchprogram.dto.TwoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FixedLenghJob2 {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private int chunkSize = 5 ;
	
	@Bean
	public Job FixedLengthJob2_batchBuild() throws Exception {
		return jobBuilderFactory.get("FixedLengthJob2")
				.start(FixedLengthJob2_batchStep1())
				.build();
	}
	
	@Bean
	public Step FixedLengthJob2_batchStep1() throws Exception {
		return stepBuilderFactory.get("FixedLengthJob2_batchStep1")
				.<TwoDto, TwoDto>chunk(chunkSize)
				.reader(FixedLengthJob2_FileReader())
				.writer(FixedLengthJob2_FileWrtier(new FileSystemResource("output/fixedLengthJob2_output.txt")))
				.build();
	}
	
	@Bean
	public FlatFileItemReader<TwoDto> FixedLengthJob2_FileReader(){
		FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setResource(new ClassPathResource("sample/fixedLengthJob2_input.txt"));
		flatFileItemReader.setLinesToSkip(1);
		
		DefaultLineMapper<TwoDto> dtoDefaultLineMapper = new DefaultLineMapper<>();
		
		FixedLengthTokenizer fixedLengthTokenizer = new FixedLengthTokenizer();
	
		fixedLengthTokenizer.setNames("one","two");
		fixedLengthTokenizer.setColumns(new Range(1,5), new Range(6,10));
		
		BeanWrapperFieldSetMapper<TwoDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
		beanWrapperFieldSetMapper.setTargetType(TwoDto.class);
		
		dtoDefaultLineMapper.setLineTokenizer(fixedLengthTokenizer);
		dtoDefaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
		flatFileItemReader.setLineMapper(dtoDefaultLineMapper);
		return flatFileItemReader;
		
	}

	@Bean
	public FlatFileItemWriter<TwoDto> FixedLengthJob2_FileWrtier(Resource resource) throws Exception{
		BeanWrapperFieldExtractor<TwoDto> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<>();
		beanWrapperFieldExtractor.setNames(new String[] {"one","two"});
		beanWrapperFieldExtractor.afterPropertiesSet();
		
		FormatterLineAggregator<TwoDto> lineAggregator = new FormatterLineAggregator<>();
		lineAggregator.setFormat("%-5s###%5s");
		lineAggregator.setFieldExtractor(beanWrapperFieldExtractor);
		
		return new FlatFileItemWriterBuilder<TwoDto>().name("FixedLengthJob2_FileWrtier")
				.resource(resource)
				.lineAggregator(lineAggregator)
				.build();
	}

}
