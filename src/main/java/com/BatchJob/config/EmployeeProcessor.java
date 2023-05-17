package com.BatchJob.config;
import org.springframework.batch.item.ItemProcessor;

import com.BatchJob.model.Employee;


public class EmployeeProcessor implements ItemProcessor<Employee, Employee> {
	

	@Override
	public Employee process(Employee item) throws Exception {
		
		return item;
	}

}
