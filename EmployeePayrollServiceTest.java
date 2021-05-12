package FileHandling;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EmployeePayrollServiceTest {

	@Test
	public void employeePayrollCheck()
	{
		EmployeePayrollData[] arrayOfEmps = {
				new EmployeePayrollData(1,"Jeff Bezos",10000.0),
				new EmployeePayrollData(2,"Bill Gates",20000.0),
				new EmployeePayrollData(3,"Mark Zuckerburg",30000.0)
		};
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		employeePayrollService.writeEmployeePayrollData(EmployeePayrollService.IOService.FILE_IO);
		employeePayrollService.printData(EmployeePayrollService.IOService.FILE_IO);
		long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.FILE_IO);
		Assert.assertEquals(3,entries);
	}
	
	@Test
	public void employeePayrollCheck2()
	{
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		long entries = employeePayrollService.readEmployeePayrollData(EmployeePayrollService.IOService.FILE_IO);
		Assert.assertEquals(3,entries);
	}
	
	@Test
	public void matchCount()
	{
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeePayrollData1(EmployeePayrollService.IOService.DB_IO);
		Assert.assertEquals(3, employeePayrollData.size());
	}
	
	@Test
	public void testUpdation()
	{
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeePayrollData1(EmployeePayrollService.IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Terisa",3000000.00);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Terisa");
		Assert.assertTrue(result);
	}
	
	@Test
	public void testRetrieval()
	{
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData1(EmployeePayrollService.IOService.DB_IO);
		LocalDate startDate = LocalDate.of(2018, 01, 01);
		LocalDate endDate = LocalDate.now();
		List<EmployeePayrollData> employeePayrollData = 
				employeePayrollService.readEmployeePayrollForDateRange(EmployeePayrollService.IOService.DB_IO, startDate, endDate);
		Assert.assertEquals(3, employeePayrollData.size());
	}
	
	@Test
	public void testRetrieval2()
	{
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData1(EmployeePayrollService.IOService.DB_IO);
		Map<String, Double> averageSalaryByGender = employeePayrollService.readAverageSalaryByGender(EmployeePayrollService.IOService.DB_IO);
		Assert.assertTrue(averageSalaryByGender.get("M").equals(2000000.00) &&
				          averageSalaryByGender.get("F").equals(3000000.00) );
	}
	
	public void testAdd()
	{
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData1(EmployeePayrollService.IOService.DB_IO);
		employeePayrollService.addEmployeeToPayroll("Mark",5000000.00,LocalDate.now(),"M");
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Mark");
		Assert.assertTrue(result);
	}
	
	public void testMultiThread()
	{
		EmployeePayrollData[] arrayOfEmps = {
				new EmployeePayrollData(0,"Jeff Bezos","M",100000.0,LocalDate.now()),
				new EmployeePayrollData(0,"Bill Gates","M",200000.0,LocalDate.now()),
				new EmployeePayrollData(0,"Mark Zuckerburg","M",300000.0,LocalDate.now()),
				new EmployeePayrollData(0,"Sunder","M",600000.0,LocalDate.now()),
				new EmployeePayrollData(0,"Mukesh","M",1000000.0,LocalDate.now()),
				new EmployeePayrollData(0,"Anil","M",200000.0,LocalDate.now())
		};
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollData(EmployeePayrollService.IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.addEmployeesToPayroll(Arrays.asList(arrayOfEmps));
		Instant end = Instant.now();
		System.out.println("Duration without Thread : " + Duration.between(start, end));
		Instant threadStart = Instant.now();
		employeePayrollService.addEmployeesToPayrollWithThreads(Arrays.asList(arrayOfEmps));
		Instant threadEnd = Instant.now();
		System.out.println("Duration with Thread : " + Duration.between(threadStart, threadEnd));
		Assert.assertEquals(13, employeePayrollService.countEntries(EmployeePayrollService.IOService.DB_IO));
	}
	
	@Before
	public void setup()
	{
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}
	
	public EmployeePayrollData[] getEmployeeList()
	{
		Response response = RestAssured.get("/employee_payroll");
		System.out.println("Employee Payroll entries in JSON:Server " + response.asString());
		EmployeePayrollData[] arrayOfEmps = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmps;
	}
	
	public Response addEmployeeToJsonServer(EmployeePayrollData employeePayrollData)
	{
		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employee_payroll");
	}
	
	@Test
	public void restMatchCount()
	{
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(2, entries);
	}
	
	@Test
	public void restResponse()
	{
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		EmployeePayrollData employeePayrollData = new EmployeePayrollData(0,"Mark","M",3000000.0,LocalDate.now());
		Response response = addEmployeeToJsonServer(employeePayrollData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(201, statusCode);
		
		employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		employeePayrollService.addEmployeesToPayroll(employeePayrollData, EmployeePayrollService.IOService.REST_IO);
		long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(3, entries);
	}
	
	@Test
	public void restResponse2()
	{
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		EmployeePayrollData[] arrayOfEmpPayrolls = {
				new EmployeePayrollData(0,"Sundar",6000000.0,LocalDate.now()),
				new EmployeePayrollData(0,"Mukesh",10000000.0,LocalDate.now()),
				new EmployeePayrollData(0,"Anil",2000000.0,LocalDate.now())
		};
		for(EmployeePayrollData employeePayrollData : arrayOfEmpPayrolls)
		{
			Response response = addEmployeeToJsonServer(employeePayrollData);
			int statusCode = response.getStatusCode();
			Assert.assertEquals(201, statusCode);
			
			employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
			employeePayrollService.addEmployeesToPayroll(employeePayrollData, EmployeePayrollService.IOService.REST_IO);
		}
		long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(6, entries);
	}
	
	@Test
	public void updateRestJson()
	{
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		
		employeePayrollService.updateEmployeeSalary("Anil", 3000000.0,EmployeePayrollService.IOService.REST_IO);
		EmployeePayrollData employeePayrollData = employeePayrollService.getEmployeePayroll();
		

		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		Response response = request.put("/employee_payroll/" + employeePayrollData.id);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
	}
	
	public void deleteRestJson()
	{
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		EmployeePayrollData employeePayrollData = employeePayrollService.getEmployeePayroll();
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		Response response = request.put("/employee_payroll/" + employeePayrollData.id);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
		
		employeePayrollService.deleteEmployeePayroll(employeePayrollData.name, EmployeePayrollService.IOService.REST_IO);
		long entries = employeePayrollService.countEntries(EmployeePayrollService.IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}
}
