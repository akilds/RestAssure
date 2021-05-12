package FileHandling;

import java.time.LocalDate;

public class EmployeePayrollData {
	public int id;
	public String name;
	public String gender;
	public Double salary;
	public LocalDate startDate;
	
	public EmployeePayrollData(int id, String name, Double salary)
	{
		this.id = id;
		this.name = name;
		this.salary = salary;
	}
	public EmployeePayrollData(int id, String name, Double salary, LocalDate startDate)
	{
		this.id = id;
		this.name = name;
		this.salary = salary;
		this.startDate = startDate;
	}
	
	public EmployeePayrollData(int id, String name, String gender, double salary, LocalDate startDate) {
		this.id = id;
		this.name = name;
		this.salary = salary;
		this.startDate = startDate;
		this.gender = gender;
	}
	public String toString()
	{
		return "id=" + id + ", name=" + name + ", salary=" + salary;
	}
}
