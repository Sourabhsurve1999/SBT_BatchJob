package com.BatchJob.config;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.BatchJob.model.Employee;
import com.BatchJob.repository.EmployeeRepository;


@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private EmployeeRepository employeeRepository;


    @Bean
    public FlatFileItemReader<Employee>reader(){
        FlatFileItemReader<Employee>reader=new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("employees.csv"));
        reader.setName("csvReader");
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper());
        return reader;
    }

    private LineMapper<Employee> lineMapper() {
        DefaultLineMapper<Employee>lineMapper=new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "eid", "name","experience");

        BeanWrapperFieldSetMapper<Employee> fieldSetMapper=new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Employee.class);
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return  lineMapper;

    }

    @Bean
    public EmployeeProcessor processor(){
        return  new EmployeeProcessor();
    }


    @Bean
    public RepositoryItemWriter<Employee> writer(){
        RepositoryItemWriter<Employee>writer=new RepositoryItemWriter<>();
        writer.setRepository(employeeRepository);
        writer.setMethodName("save");
        return writer;

    }

    @Bean
    public Step step1(){
        return stepBuilderFactory.get("csv-step").<Employee,Employee>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecuter())
                .build();
    }

    @Bean
    public Job runJob(){
        return jobBuilderFactory.get("importEmployees")
                .flow(step1()).end().build();
    }


    private TaskExecutor taskExecuter() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor=new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(10);
        return simpleAsyncTaskExecutor;
    }

}
