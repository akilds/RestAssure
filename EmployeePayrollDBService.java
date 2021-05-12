package FileHandling;

import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.LocalDate;

public class EmployeePayrollDBService {

	private int connectionCounter = 0;
	private static EmployeePayrollDBService employeePayrollDBService;
	private PreparedStatement employeePayrollDataStatement;
	
	public EmployeePayrollDBService()
	{
		
	}
	
	public static EmployeePayrollDBService getInstance()
	{
		if(employeePayrollDBService == null)
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}
	
	//USE CASE 1
	private Connection getConnection() throws SQLException
	{
		connectionCounter++;
		String url = "jdbc:mysql://localhost:3306/payroll_service?characterEncoding=utf8&useSSL=false&useUnicode=true" ;
		String username = "root";
		String password = "Ak@Dd14a";
		Connection connection;
//		System.out.println("Connecting to : "+url);
//		connection = DriverManager.getConnection(url, username, password);
//		System.out.println("Connection success"+connection);
		System.out.println("Processing Thread : " + Thread.currentThread().getName() +
				           " Connecting to database with id : " + connectionCounter);
		connection = DriverManager.getConnection(url, username, password);
		System.out.println("Processing Thread : " + Thread.currentThread().getName() +
		           " id : " + connectionCounter + "Connection is successful" + connection);
		return connection;
	}
	
	//USE CASE 2
	public List<EmployeePayrollData> readData()
	{
		String sql = "select * from employee_payroll2";
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
		    employeePayrollList = this.getEmployeePayrollData(result);
		}	
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) 
		{
			
		}
		return employeePayrollList;
	}
	
	public int updateEmployeeData(String name, double salary)
	{
		return this.updateEmployeeDataUsingStatement(name, salary);
	}
	
	private int updateEmployeeDataUsingStatement(String name,double salary)
	{
		String sql = String.format("update employee_payroll set salary = %.2f where name = '%s';", salary, name);
		try(Connection connection = this.getConnection())
		{
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public List<EmployeePayrollData> getEmployeePayrollData(String name)
	{
		List<EmployeePayrollData> employeePayrollList = null;
		if(this.employeePayrollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try
		{
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return employeePayrollList;
	}
	
	public List<EmployeePayrollData> getEmployeePayrollData(ResultSet result)
	{
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try
		{
			while(result.next())
			{
				int id = result.getInt("id");
				String name = result.getString("name");
				double salary = result.getInt("salary");
				LocalDate startDate = result.getDate("start_date").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return employeePayrollList;
	}
	
	private void prepareStatementForEmployeeData()
	{
		try
		{
			Connection connection = this.getConnection();
			String sql = "select * from employee_payroll where name = ?";
			employeePayrollDataStatement = connection.prepareStatement(sql);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql)
	{
		ResultSet resultSet;
		List<EmployeePayrollData> employeePayrollList = null;
		try(Connection connection = this.getConnection())
		{
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(result);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return employeePayrollList;
	}
	private int updateEmployeeDataUsingPreparedStatement(String name,double salary)
	{
		
		try(Connection connection = this.getConnection())
		{
			String sql = "update employee_payroll set salary = ? where name = ?;";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setDouble(1,salary);
			preparedStatement.setString(2,name);
			return preparedStatement.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate startDate, LocalDate endDate)
	{
		String sql = String.format("select gender, avg(salary) as avg_salary from employee_payroll group by gender;",
				      Date.valueOf(startDate), Date.valueOf(endDate));
		return this.getEmployeePayrollDataUsingDB(sql);
	}
	
	public Map<String, Double> getAverageSalaryByGender()
	{
		String sql = "select gender, avg(salary) as avg_salary from employee_payroll group by gender;";
		Map<String, Double> genderToAverageSalaryMap = new HashMap<>();
		try(Connection connection = this.getConnection())
		{
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			while(result.next())
			{
				String gender = result.getString("gender");
				Double salary = result.getDouble("avg_salary");
				genderToAverageSalaryMap.put(gender, salary);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return genderToAverageSalaryMap;
	}
	
	public EmployeePayrollData addEmployeeToPayroll(String name, double salary, LocalDate date, String gender)
	{
		int employeeId = -1;
		EmployeePayrollData employeePayrollData = null;
		String sql = String.format("insert into employee_payroll(name, gender, salary, start_date)" +
								   "values('%s', '%s', '%s', '%s');",name, gender, salary, Date.valueOf(date));
		try(Connection connection = this.getConnection())
		{
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if(rowAffected == 1)
			{
				ResultSet result = statement.getGeneratedKeys();
				if(result.next()) employeeId = result.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(employeeId, name, salary, date);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return employeePayrollData;
	}
	
	public EmployeePayrollData addEmployeeToPayrollUC8(String name, double salary, LocalDate date, String gender)
	{
		int employeeId = -1;
		Connection connection = null;
		EmployeePayrollData employeePayrollData = null;
		try
		{
			connection = this.getConnection();
			connection.setAutoCommit(false);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		try(Statement statement = connection.createStatement())
		{
			String sql = String.format("insert into employee_payroll(name, gender, salary, start_date)" +
					   "values('%s', '%s', '%s', '%s');",name, gender, salary, Date.valueOf(date));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if(rowAffected == 1)
			{
				ResultSet result = statement.getGeneratedKeys();
				if(result.next()) employeeId = result.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(employeeId, name, salary, date);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			try {
				connection.rollback();
				return employeePayrollData;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try(Statement statement = connection.createStatement())
		{
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format("insert into payroll_details(employeeId, basic_pay, deductions, taxable_pay, tax, net_pay)" +
					   "values(%s, %s, %s, %s, %s, %s);",employeeId, salary, deductions, taxablePay, tax, netPay);
			int rowAffected = statement.executeUpdate(sql);
			if(rowAffected == 1)
			{
				employeePayrollData = new EmployeePayrollData(employeeId, name, salary, date);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return employeePayrollData;
	}
}
