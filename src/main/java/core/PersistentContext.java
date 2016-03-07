package core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;
import annotations.ColumnMap;
import annotations.PersistentClass;
import exceptions.NotValidPersistentClassException;

public class PersistentContext<T>  {

	private DataSource dataSource;
	private Field[] _fields;
	private String _table;
	private int _fields_length;
	private String[] _column_names;


	/**
	 * 
	 * @param datasource
	 * @param _class
	 * @throws NotValidPersistentClassException If the class is not annotated as @PersistentClass or
	 * if the T class doesn't have public getters for all the persistent fields.
	 */
	public PersistentContext(DataSource datasource, Class<T> _class)
			throws NotValidPersistentClassException{

		if(!_class.isAnnotationPresent(PersistentClass.class)){
			throw new NotValidPersistentClassException("The object must be an instance of a class annotated with @PersistentCLass");
		}

		this.dataSource = datasource;
		this._fields = _class.getDeclaredFields();
		Annotation class_an = _class.getAnnotation(PersistentClass.class);
		this._table = ((PersistentClass) class_an).table();

		this._column_names = new String[this._fields_length];

		Annotation annotation = null;
		for(int i = 0; i < this._fields_length; i++ ){
			if(this._fields[i].isAnnotationPresent(ColumnMap.class)){
				annotation = this._fields[i].getAnnotation(ColumnMap.class);
				this._column_names[i] = ((ColumnMap) annotation).column();	
			}
		}

	}

	/**
	 * 
	 * @param t Generic object to persist
	 * @throws SQLException If an error occurs while accessing the Data Base.
	 * 
	 */
	public void insert(T t)
			throws SQLException{

		try(Connection connection = this.dataSource.getConnection();
				PreparedStatement	stmt = generateInsertPrepareStatement(t, true, connection)){

			if(stmt != null)
				stmt.executeUpdate();
		} 
	}


	/**
	 * 
	 * @param t Generic object to persist
	 * @return Auto-generated key created as a result of executing the insert on the Data Base. 0 if no Auto Gererated Key.
	 * @throws SQLException If an error occurs while accessing the Data Base.
	 */
	public long insertAndReturnId(T t)
			throws SQLException{

		long result = 0;

		try(Connection connection = this.dataSource.getConnection();
				PreparedStatement stmt = generateInsertPrepareStatement(t, true, connection)){

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

	//TODO
	public T selectById(long id){
		return null;
	}

	//TODO
	public T selectById(QueryParameter<?> id){
		return null;
	}

	//TODO
	public List<T> select(Filter filter){
		return null;
	}

	//TODO
	public void update(long id){

	}

	//TODO
	public void update(){

	}

	//TODO
	public void delete(){

	}

	//TODO
	public List<T> delete(QueryParameter<?> ... params){
		return null;
	}


	/*
	 * Private aux method for generating the PreparedStatement object.
	 */
	private PreparedStatement generateInsertPrepareStatement(T t, boolean generatedKeyReturn, Connection connection){

		String sql_insert_front = "INSERT INTO " + this._table + " (";
		String sql_values = ") VALUES (";
		String sql_end = ")";

		for(int i = 0; i < this._fields_length; i++ ){

			sql_insert_front += this._column_names[i];
			sql_values += "?";

			if(i < this._fields_length- 1){
				sql_insert_front += ", ";
				sql_values +=",";
			}		

		}

		String sql_query = sql_insert_front + sql_values + sql_end;
		return evalPreparedStatement(generatedKeyReturn, connection, sql_query, t);
	}

	/*
	 * Private aux method for evaluating the PreparedStatement using the object field values.
	 */
	private PreparedStatement evalPreparedStatement(boolean generatedKeyReturn, Connection connection, String sql, T t){

		PreparedStatement stmt = null;

		try {

			Class<?> returnType;
			if(generatedKeyReturn){

				stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			}else{
				stmt = connection.prepareStatement(sql);
			}

			for (int i = 0; i < this._fields_length; i++) {

				this._fields[i].setAccessible(true);
				returnType = this._fields[i].getType();

				if(returnType.equals(int.class) || returnType.equals(Integer.class)){

					stmt.setInt(i+1, this._fields[i].getInt(t));
				}
				else if(returnType.equals(String.class)){

					stmt.setString(i+1, (String) this._fields[i].get(t));
				}
				else if(returnType.equals(java.sql.Timestamp.class)){

					stmt.setTimestamp(i+1, (java.sql.Timestamp) this._fields[i].get(t));
				}
				else if(returnType.equals(BigDecimal.class)){

					stmt.setBigDecimal(i+1, (BigDecimal) this._fields[i].get(t));
				}
				else if(returnType.equals(long.class)  || returnType.equals(Long.class) ){

					stmt.setLong(i+1, this._fields[i].getLong(t));
				}
				else if(returnType.equals(double.class) || returnType.equals(Double.class)){

					stmt.setDouble(i+1, this._fields[i].getDouble(t));
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
