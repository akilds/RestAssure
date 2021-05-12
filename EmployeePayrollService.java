package FileHandling;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

public class EmployeePayrollService {

	public enum IOService {CONSOLE_IO, FILE_IO, DB_IO, REST_IO}
	private List<EmployeePayrollData> employeePayrollList;
	private EmployeePayrollDBService employeePayrollDBService;
	
	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}
	
	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList){
		this();
		this.employeePayrollList = new ArrayList<>(employeePayrollList);
	}
	
	public static void main(String[] args) {
		ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeePayrollData(consoleInputReader);
		employeePayrollService.writeEmployeePayrollData(IOService.CONSOLE_IO);
	}
	
	public void readEmployeePayrollData(Scanner consoleInputReader)
	{
		System.out.println("Enter Employee ID : ");
		int id = consoleInputReader.nextInt();
		System.out.println("Enter Employee Name : ");
		String name = consoleInputReader.next();
		System.out.println("Enter Employee salary : ");
		double salary = consoleInputReader.nextDouble();
		employeePayrollList.add(new EmployeePayrollData(id, name, salary));
	}

	public void writeEmployeePayrollData(IOService ioService)
	{
		if(ioService.equals(EmployeePayrollService.IOService.CONSOLE_IO))
			System.out.println("\nWriting Employee payroll Roaster to Console\n" + employeePayrollList);
		else if(ioService.equals(EmployeePayrollService.IOService.FILE_IO))
			new EmployeePayrollFileIOService().writeData(employeePayrollList);
	}
	
	public void printData(IOService ioService)
	{
		if(ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().printData();
	}
	
	public long countEntries(IOService ioService)
	{
		if(ioService.equals(IOService.FILE_IO))
			return new EmployeePayrollFileIOService().countEntries();
		return employeePayrollList.size();
	}

	public long readEmployeePayrollData(IOService ioService)
	{
		if(ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		return employeePayrollList.size();
	}
	
	public List<EmployeePayrollData> readEmployeePayrollData1(IOService ioService)
	{
		if(ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBService.readData();
		return this.employeePayrollList;
	}
	
	public void updateEmployeeSalary(String name, double salary)
	{
		EmployeePayrollDBService employeePayrollDBService = new EmployeePayrollDBService();
		int result = employeePayrollDBService.updateEmployeeData(name,salary);
		if(result==0)	return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if(employeePayrollData != null)	employeePayrollData.salary = salary;
	}
	
	private EmployeePayrollData getEmployeePayrollData(String name)
	{
		EmployeePayrollData employeePayrollData;
		employeePayrollData = this.employeePayrollList.stream()
								  .filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name))
								  .findFirst()
								  .orElse(null);
		return employeePayrollData;
	}
	
	public boolean checkEmployeePayrollInSyncWithDB(String name)
	{
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}
	
	public List<EmployeePayrollData> readEmployeePayrollForDateRange(IOService ioService, LocalDate startDate, LocalDate endDate)
	{
		if(ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getEmployeeForDateRange(startDate, endDate);
		return null;
	}
	
	public Map<String, Double> readAverageSalaryByGender(IOService ioService)
	{
		if(ioService.equals(IOService.DB_IO))
			return employeePayrollDBService.getAverageSalaryByGender();
		return null;
	}
	
	public void addEmployeeToPayroll(String name, Double salary, LocalDate date, String gender)
	{
		employeePayrollList.add(employeePayrollDBService.addEmployeeToPayroll(name, salary, date, gender));
	}

	public void addEmployeesToPayroll(List<EmployeePayrollData> employeePayrollDataList) {
		employeePayrollDataList.forEach(employeePayrollData -> {
			System.out.println("Employee being added : " + employeePayrollData.name);
			this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
					                   employeePayrollData.startDate, employeePayrollData.gender);
			System.out.println("Employee added : " + employeePayrollData.name);
		});
		System.out.println(this.employeePayrollList);
	}
	
	public void addEmployeesToPayrollWithThreads(List<EmployeePayrollData> employeePayrollDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<>();
		employeePayrollDataList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
				System.out.println("Employee being added : " + Thread.currentThread().getName());
				this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
						employeePayrollData.startDate, employeePayrollData.gender);
				employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
				System.out.println("Employee added : " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeePayrollData.name);
			thread.start();
		});
		while(employeeAdditionStatus.containsValue(false))
		{
			try {
				Thread.sleep(10);
			}
			catch(InterruptedException e) {
			}
		}
		System.out.println(this.employeePayrollList);
	}

	public void addEmployeesToPayroll(EmployeePayrollData employeePayrollData, IOService ioService) {
		if(ioService.equals(ioService.DB_IO))
			this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.salary,
					                   employeePayrollData.startDate, employeePayrollData.gender);
		else	employeePayrollList.add(employeePayrollData);
	}

	public void updateEmployeeSalary(String string, double d, IOService ioService) {
		if(ioService.equals(IOService.REST_IO)) {
			int result = employeePayrollDBService.updateEmployeeData(name, salary);
			if(result==0)	return;
		}
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if(employeePayrollData != null)	employeePayrollData.salary = salary;
	}
	
	public void deleteEmployeePayroll(String name, IOService ioService)
	{
		if(ioService.equals(IOService.REST_IO)) {
			EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
			employeePayrollList.remove(employeePayrollData);
		}
	}
}
