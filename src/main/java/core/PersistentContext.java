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
	 * @throws NotValidPersistentClassException If the class is not annotated as PersistentClass.
	 * @throws SQLException
	 */
	public void insert(T t) throws NotValidPersistentClassException, SQLException, UnsupportedDataMappingException{

		Class<?> c = t.getClass();

		if(!c.isAnnotationPresent(PersistentClass.class)){
			throw new NotValidPersistentClassException("The object must be an instance of a class annotated with @RgsPersistentCLass");
		}

		Annotation annotation = c.getAnnotation(PersistentClass.class);
		PersistentClass table = (PersistentClass) annotation;
		Field[] fields = c.getDeclaredFields();

		PreparedStatement stmt = null;

		try(Connection connection = this.dataSource.getConnection()){

			stmt = generateInsertPrepareStatement(table.table(), fields, t, true, connection);
			if(stmt != null)
				stmt.executeUpdate();

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			//TODO 
			e.printStackTrace();
		}

	}


	/**
	 * 
	 * @param t Generic object to persist
	 * @return Auto-generated key created as a result of executing the insert on the Data Base. 0 if no key.
	 * @throws NotValidPersistentClassException
	 * @throws SQLException
	 */
	public long insertAndReturnId(T t) throws NotValidPersistentClassException, SQLException, UnsupportedDataMappingException{

		//TODO: Test behavior on missing auto generated key.

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

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			//TODO 
			e.printStackTrace();
		}

		return result;
	}


	/*
	 * Private aux method for generating the PreparedStatement object.
	 */
	private PreparedStatement generateInsertPrepareStatement(String tableName, Field[] fields, T t, boolean generatedKeyReturn, Connection connection) throws SQLException, 
	IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnsupportedDataMappingException{

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

				if(i < fields.length - 1){
					sql_insert_front += ", ";
					sql_values +=",";
				}

				PropertyDescriptor pd;
				try {
					pd = new PropertyDescriptor(fields[i].getName(), t.getClass());
					types.add(pd.getReadMethod());

				} catch (IntrospectionException e) {
					e.printStackTrace();
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

		for(Method m : types){

			returnType = m.getReturnType();

			if(returnType.equals(int.class) || returnType.equals(Integer.class)){
				stmt.setInt(index, (int) m.invoke(t));
			}
			else if(returnType.equals(String.class)){
				stmt.setString(index, (String) m.invoke(t));
			}
			else if(returnType.equals(java.sql.Timestamp.class)){
				stmt.setTimestamp(index, (java.sql.Timestamp) m.invoke(t));
			}
			else if(returnType.equals(BigDecimal.class)){
				stmt.setBigDecimal(index, (BigDecimal) m.invoke(t));
			}
			else if(returnType.equals(long.class)  || returnType.equals(Long.class) ){
				stmt.setLong(index, (long) m.invoke(t));
			}
			else if(returnType.equals(double.class) || returnType.equals(Double.class)){
				stmt.setDouble(index, (double) m.invoke(t));
			}
			else{
				stmt.close();
				throw new UnsupportedDataMappingException("The data type: " + returnType + " is not currently supported");
			}
			index++;
		}

		return stmt;
	}
}
