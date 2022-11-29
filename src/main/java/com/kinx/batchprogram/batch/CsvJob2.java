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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.kinx.batchprogram.custom.CustomBeanWrapperFieldExtractor;
import com.kinx.batchprogram.custom.CustomPassThroughLineAggregator;
import com.kinx.batchprogram.domain.Dept;
import com.kinx.batchprogram.domain.Dept2;
import com.kinx.batchprogram.dto.TwoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvJob2 {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private int chunkSize = 5 ;
	
	@Bean
	public Job csvJob2_batchBuild() throws Exception {
		return jobBuilderFactory.get("csvJob2")
				.start(csvJob2_batchStep1())
				.build();
	}
	
	@Bean
	public Step csvJob2_batchStep1() throws Exception {
		return stepBuilderFactory.get("csvJob2_batchStep1")
				.<TwoDto, TwoDto>chunk(chunkSize)
				.reader(csvJob2_FileReader())
				.writer(csvJob2_FileWrtier(new FileSystemResource("output/csvJob2_output.csv")))
				.build();
	}
	
	@Bean
	public FlatFileItemReader<TwoDto> csvJob2_FileReader(){
		FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setResource(new ClassPathResource("sample/csvJob1_input2.csv"));
		flatFileItemReader.setLinesToSkip(1);
		
		DefaultLineMapper<TwoDto> dtoDefaultLineMapper = new DefaultLineMapper<>();
		
		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		delimitedLineTokenizer.setNames("one","two");
		delimitedLineTokenizer.setDelimiter(":");
		
		BeanWrapperFieldSetMapper<TwoDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
		beanWrapperFieldSetMapper.setTargetType(TwoDto.class);
		
		dtoDefaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
		dtoDefaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
		flatFileItemReader.setLineMapper(dtoDefaultLineMapper);
		return flatFileItemReader;
		
	}
	
	@Bean
	public FlatFileItemWriter<TwoDto> csvJob2_FileWrtier(Resource resource) throws Exception{
		CustomBeanWrapperFieldExtractor<TwoDto> customBeanWrapperFieldExtractor = new CustomBeanWrapperFieldExtractor<>();
		customBeanWrapperFieldExtractor.setNames(new String[] {"one","two"});
		customBeanWrapperFieldExtractor.afterPropertiesSet();
		
		DelimitedLineAggregator<TwoDto> dtoDelimitedLineAggregator = new DelimitedLineAggregator<>();
		dtoDelimitedLineAggregator.setDelimiter("@");
		dtoDelimitedLineAggregator.setFieldExtractor(customBeanWrapperFieldExtractor);
		return new FlatFileItemWriterBuilder<TwoDto>().name("csvJob2_FileWrtier")
				.resource(resource)
				.lineAggregator(dtoDelimitedLineAggregator)
				.build();
	}

}
