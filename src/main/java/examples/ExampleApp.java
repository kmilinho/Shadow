package examples;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import core.PersistentContext;
import core.PersistentContextFactory;
import exceptions.NotValidPersistentClassException;

public class ExampleApp {


	public static void main(String[] args) {
		
		/*
		 * Basic Example of persistor factory instance creator.
		 */
		PersistentContextFactory persistor_factory = new PersistentContextFactory(getDatSource());
		
		PersistentContext<Dog> dog_persistor = persistor_factory.getGenericPersistentContextFor(Dog.class);	

		Dog dog = new Dog();
		dog.setAge(5);
		dog.setName("Ash");

		try {
			dog_persistor.insertAndReturnId(dog);
		} catch (NotValidPersistentClassException e) {
			// TODO
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO 
			e.printStackTrace();
		}
	}
	
	public static DataSource getDatSource(){
		
		String driver = "com.mysql.jdbc.Driver";
		String ip = "localhost";
		String port = "3306";
		String db_name = "persistor_test_db";
		String user = "root";
		String password = "root";
		String url_driver = "mysql";
		
		BasicDataSource data_source = new BasicDataSource();
		
		data_source = new BasicDataSource();
		data_source.setDriverClassName(driver);
		data_source.setUrl("jdbc:"+ url_driver +"://"+ ip + ":" + port + "/" + db_name);
		data_source.setUsername(user);
		data_source.setPassword(password);
		
		return data_source;
	}
}
