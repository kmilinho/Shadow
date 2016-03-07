package core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import annotations.ColumnMap;
import annotations.PersistentClass;
import exceptions.NotValidPersistentClassException;

public class PersistentContext<T>  {

	private DataSource dataSource;

	public PersistentContext(DataSource datasource){
		this.dataSource = datasource;
	}

	/**
	 * 
	 * @param t Generic object to persist
	 * @throws NotValidPersistentClassException If the class is not annotated as @PersistentClass or
	 * if the T class doesn't have public getters for all the persistent fields.
	 * @throws SQLException If an error occurs while accessing the Data Base.
	 * 
	 */
	public void insert(T t)
			throws NotValidPersistentClassException, SQLException{

		Class<?> c = t.getClass();

		if(!c.isAnnotationPresent(PersistentClass.class)){
			throw new NotValidPersistentClassException("The object must be an instance of a class annotated with @PersistentCLass");
		}

		Annotation annotation = c.getAnnotation(PersistentClass.class);
		PersistentClass table = (PersistentClass) annotation;
		Field[] fields = c.getDeclaredFields();

		PreparedStatement stmt = null;
		try(Connection connection = this.dataSource.getConnection()){
			stmt = generateInsertPrepareStatement(table.table(), fields, t, true, connection);

			if(stmt != null)
				stmt.executeUpdate();
		} 
	}


	/**
	 * 
	 * @param t Generic object to persist
	 * @return Auto-generated key created as a result of executing the insert on the Data Base. 0 if no Auto Gererated Key.
	 * @throws NotValidPersistentClassException If the class is not annotated as @PersistentClass or
	 * if the T class doesn't have public getters for all the persistent fields.
	 * @throws SQLException If an error occurs while accessing the Data Base.
	 */
	public long insertAndReturnId(T t)
			throws NotValidPersistentClassException, SQLException{

		Class<? extends Object> c = t.getClass();

		if(!c.isAnnotationPresent(PersistentClass.class)){
			throw new NotValidPersistentClassException("The object must be an instance of a class annotated with @RgsPersistentCLass");
		}

		Annotation annotation = c.getAnnotation(PersistentClass.class);
		PersistentClass table = (PersistentClass) annotation;
		Field[] fields = c.getDeclaredFields();
		long result = 0;

		try(Connection connection = this.dataSource.getConnection();
				PreparedStatement stmt = generateInsertPrepareStatement(table.table(), fields, t, true, connection)){

			ResultSet rs;
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();

			if (rs != null && rs.next()) {
				result = rs.getLong(1);
				rs.close();
			}	
		}
		return result;
	}


	/*
	 * Private aux method for generating the PreparedStatement object.
	 */
	private PreparedStatement generateInsertPrepareStatement(String tableName, Field[] fields, T t, boolean generatedKeyReturn, Connection connection){

		String sql_insert_front = "INSERT INTO " + tableName + " (";
		String sql_values = ") VALUES (";
		String sql_end = ")";
		ColumnMap column;
		String db_column_name;
		Annotation annotation;

		for(int i = 0; i < fields.length; i++ ){
			if(fields[i].isAnnotationPresent(ColumnMap.class)){
				annotation = fields[i].getAnnotation(ColumnMap.class);
				column = (ColumnMap) annotation;
				db_column_name = column.column();
				sql_insert_front += db_column_name;
				sql_values += "?";

				if(i < fields.length - 1){
					sql_insert_front += ", ";
					sql_values +=",";
				}		
			}
		}

		String sql_query = sql_insert_front + sql_values + sql_end;
		return evalPreparedStatement(generatedKeyReturn, connection, fields, sql_query, t);
	}

	/*
	 * Private aux method for evaluating the PreparedStatement using the object field values.
	 */
	private PreparedStatement evalPreparedStatement(boolean generatedKeyReturn, Connection connection, Field[] fields, String sql, T t){

		PreparedStatement stmt = null;

		try {

			Class<?> returnType;
			if(generatedKeyReturn){

				stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			}else{
				stmt = connection.prepareStatement(sql);
			}

			for (int i = 0; i < fields.length; i++) {

				fields[i].setAccessible(true);
				returnType = fields[i].getType();

				if(returnType.equals(int.class) || returnType.equals(Integer.class)){

					stmt.setInt(i+1, fields[i].getInt(t));
				}
				else if(returnType.equals(String.class)){

					stmt.setString(i+1, (String) fields[i].get(t));
				}
				else if(returnType.equals(java.sql.Timestamp.class)){

					stmt.setTimestamp(i+1, (java.sql.Timestamp) fields[i].get(t));
				}
				else if(returnType.equals(BigDecimal.class)){

					stmt.setBigDecimal(i+1, (BigDecimal) fields[i].get(t));
				}
				else if(returnType.equals(long.class)  || returnType.equals(Long.class) ){

					stmt.setLong(i+1, fields[i].getLong(t));
				}
				else if(returnType.equals(double.class) || returnType.equals(Double.class)){

					stmt.setDouble(i+1, fields[i].getDouble(t));
				}
				else{
					stmt.close();
					System.err.println("The data type: " + returnType + " is not currently supported");
				}
			}

		} catch (SQLException e) {
			System.err.println("Internal SQLException on PrepareStatement: "+sql);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println("Internal IllegalArgumentException. The object passed is not an instance of the @PersistentClass.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("Internal IllegalArgumentException. Field not accessible.");
			e.printStackTrace();
		}

		return stmt;
	}
}
