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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kinx.batchprogram.domain.Dept;
import com.kinx.batchprogram.domain.Dept2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob2 {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	
	//	DB를 잘라서 Batch를 순차적으로 돌겠다?
	//  잘라서 가지고 오다가 batch가 달라질 수 도 있기 때문에 정답은 아님.
	private int chunkSize = 10;

	@Bean
	public Job JpaPageJob2_batchBuild() {
		return jobBuilderFactory.get("JpaPageJob2")
				.start(JpaPageJob2_step1())
				.build();
	}

	// 1과 다르게 가공을 해서 넘겨주고 싶을 때 !
	// processor 사용 가능
	@Bean
	public Step JpaPageJob2_step1() {
		return stepBuilderFactory.get("JpaPageJob2_step1")
				.<Dept, Dept2>chunk(chunkSize)
				.reader(jpaPageJob2_dbItemReader())
				.processor(jpaPageJob2_processor())
				.writer(jpaPageJob2_dbItemWriter())
				.build();
	}
	
	private ItemProcessor<Dept, Dept2> jpaPageJob2_processor(){
		return dept -> {
			return new Dept2(dept.getDeptNo(), "NEW222_"+dept.getDName(), "NEW222_"+dept.getLoc());
		};
	}
	
	@Bean(destroyMethod="")
	public JpaPagingItemReader<Dept> jpaPageJob2_dbItemReader(){
		return new JpaPagingItemReaderBuilder<Dept>()
				.name("jpaPageJob2_dbItemReader")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(chunkSize)
				.queryString("SELECT d FROM Dept d order by dept_no asc")
				.build();
	}
	
	@Bean
	public JpaItemWriter<Dept2> jpaPageJob2_dbItemWriter(){
		JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
		jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
		return jpaItemWriter;
		
	}
}
