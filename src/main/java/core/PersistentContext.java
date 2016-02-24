package core;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import annotations.ColumnMap;
import annotations.PersistentClass;
import exceptions.NotValidPersistentClassException;
import exceptions.UnsupportedDataMappingException;


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
	 * @throws UnsupportedDataMappingException If one of the data type of the fields annotated with @ColumnMap is currently not supported on this application.
	 * 
	 */
	public void insert(T t) throws NotValidPersistentClassException, SQLException, UnsupportedDataMappingException{

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
	 * @return Auto-generated key created as a result of executing the insert on the Data Base. 0 if no key.
	 * @throws NotValidPersistentClassException If the class is not annotated as @PersistentClass or
	 * if the T class doesn't have public getters for all the persistent fields.
	 * @throws SQLException If an error occurs while accessing the Data Base.
	 * @throws UnsupportedDataMappingException If one of the data type of the fields annotated with @ColumnMap is currently not supported on this application.
	 */
	public long insertAndReturnId(T t) throws NotValidPersistentClassException, SQLException, UnsupportedDataMappingException{

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
	private PreparedStatement generateInsertPrepareStatement(String tableName, Field[] fields, T t, boolean generatedKeyReturn, Connection connection)
			throws SQLException, UnsupportedDataMappingException, NotValidPersistentClassException{

		String sql_insert_front = "INSERT INTO " + tableName + " (";
		String sql_values = ") VALUES (";
		String sql_end = ")";
		ColumnMap column;
		String db_column_name;
		Annotation annotation;
		List<Method> types = new ArrayList<Method>();

		for(int i = 0; i < fields.length; i++ ){
			if(fields[i].isAnnotationPresent(ColumnMap.class)){
				annotation = fields[i].getAnnotation(ColumnMap.class);
				column = (ColumnMap) annotation;
				db_column_name = column.column();
				sql_insert_front += db_column_name;
				sql_values += "?";
				PropertyDescriptor pd;

				if(i < fields.length - 1){
					sql_insert_front += ", ";
					sql_values +=",";
				}		
				try {
					pd = new PropertyDescriptor(fields[i].getName(), t.getClass());

					types.add(pd.getReadMethod());
				} catch (IntrospectionException e) {
					System.err.println("IntrospectionException on field: " + fields[i].getName());
					throw new NotValidPersistentClassException("Classes annotated with @PersistentCLass must have a public getter for all the fields annotated with @ColumnMap.");
				}
			}
		}

		String sql_query = sql_insert_front + sql_values + sql_end;
		PreparedStatement stmt = null;

		if(generatedKeyReturn){
			stmt = connection.prepareStatement(sql_query, Statement.RETURN_GENERATED_KEYS);
		}else{
			stmt = connection.prepareStatement(sql_query);
		}

		Class<?> returnType;
		int index = 1;
		String data_type = null;

		try{
			for(Method m : types){
				returnType = m.getReturnType();

				if(returnType.equals(int.class) || returnType.equals(Integer.class)){
					data_type = "int";
					
					stmt.setInt(index, (int) m.invoke(t));
				}
				else if(returnType.equals(String.class)){
					data_type = "String";
					
					stmt.setString(index, (String) m.invoke(t));
				}
				else if(returnType.equals(java.sql.Timestamp.class)){
					data_type = "java.sql.Timestamp";

					stmt.setTimestamp(index, (java.sql.Timestamp) m.invoke(t));
				}
				else if(returnType.equals(BigDecimal.class)){
					data_type = "BigDecimal";

					stmt.setBigDecimal(index, (BigDecimal) m.invoke(t));
				}
				else if(returnType.equals(long.class)  || returnType.equals(Long.class) ){
					data_type = "long";

					stmt.setLong(index, (long) m.invoke(t));
				}
				else if(returnType.equals(double.class) || returnType.equals(Double.class)){
					data_type = "double";

					stmt.setDouble(index, (double) m.invoke(t));
				}
				else{
					stmt.close();
					throw new UnsupportedDataMappingException("The data type: " + returnType + " is not currently supported");
				}
				index++;
			}
		}catch (IllegalAccessException e){
			System.err.println("IllegalAccessException while invoking a getter with return type: " + data_type);
			throw new NotValidPersistentClassException("Classes annotated with @PersistentCLass must have a public getter for all the fields annotated with @ColumnMap.");
		} catch (InvocationTargetException e) {
			System.err.println("InvocationTargetException while invoking a getter with return type: " + data_type);
			throw new NotValidPersistentClassException("The execution of a getter method in the Persistent Class has failed.");
		}
		return stmt;
	}
}
