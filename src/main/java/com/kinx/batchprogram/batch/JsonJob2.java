package com.kinx.batchprogram.batch;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.kinx.batchprogram.dto.CoinMarket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JsonJob2 {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private int chunkSize = 5 ;
	
	@Bean
	public Job jsonJob2_batchBuild() {
		return jobBuilderFactory.get("jsonJob2")
				.start(jsonJob2_batchStep1())
				.build();
	}
	
	@Bean
	public Step jsonJob2_batchStep1() {
		return stepBuilderFactory.get("jsonJob2_batchStep1")
				.<CoinMarket, CoinMarket>chunk(chunkSize)
				.reader(jsonJob2_jsonReader())
				.processor(jsonJob2__processor())
				.writer(jsonJob2_jsonWriter())
				.build();
	}
	
	private ItemProcessor<CoinMarket,CoinMarket> jsonJob2__processor() {
		return coinMarket -> {
			if(coinMarket.getMarket().startsWith("KRW-")) {
				return new CoinMarket(coinMarket.getMarket(), coinMarket.getKorean_name(), coinMarket.getEnglish_name());
			}else {
				return null;
			}
		};
	}

	@Bean
	public JsonItemReader<CoinMarket> jsonJob2_jsonReader(){
		return new JsonItemReaderBuilder<CoinMarket>()
				.jsonObjectReader(new JacksonJsonObjectReader<>(CoinMarket.class))
				.resource(new ClassPathResource("sample/jsonJob1_input.json"))
				.name("jsonJob2_jsonReader")
				.build();
	}
	
	@Bean
	public JsonFileItemWriter<CoinMarket> jsonJob2_jsonWriter(){
		return new JsonFileItemWriterBuilder<CoinMarket>()
				.jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
				.resource(new FileSystemResource("output/jsonJob2_ouput.json"))
				.name("jsonJob2_jsonWriter")
				.build();
	}
}
