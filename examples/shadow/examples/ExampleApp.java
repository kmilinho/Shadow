package shadow.examples;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

import shadow.core.Filter;
import shadow.core.PersistentContext;
import shadow.core.PersistentContextFactory;
import shadow.exceptions.NotValidPersistentClassException;

public class ExampleApp {


	public static void main(String[] args) {

		/*
		 * Basic Example of PersistentContextFactory instantiation.
		 */
		PersistentContextFactory persistor_factory = new PersistentContextFactory(getDatSource());

		PersistentContext<Dog> dog_persistor = null;
		try {
			/*
			 * Using the PersistentContextFactory for obtaining a PersistentContext.
			 */
			dog_persistor = persistor_factory.getPersistentContextFor(Dog.class);
		} catch (NotValidPersistentClassException e1) {
			System.err.println("NotValidPersistentClassException abort example");
			return;
		}	


		Dog dog_otto = new Dog("Otto", 11);
		Dog dog_ash = new Dog("Ash", 1);
		Dog dog_borja = new Dog("Borja", 2);
		Dog dog_adam = new Dog("Adam", 12);
		Dog dog_peter = new Dog("Peter", 13);
		Dog dog_wei = new Dog("Wei", 9);

		long id;

		try {

			System.out.println(dog_otto.bark());

			/*
			 * Using the Dog PersistentContext for inserting a new Dog into the DB and obtaining the auto generated primary key.
			 */
			id = dog_persistor.insertAndReturnId(dog_otto);

			/*
			 * Using the Dog PersistentContext for selecting the previously created dog from the DB.
			 */
			dog_otto = dog_persistor.selectById(id);
			System.out.println(dog_otto.bark());

			/*
			 * Using the Dog PersistentContext for inserting a group of Dog instances.
			 */
			dog_persistor.insert(dog_ash, dog_borja, dog_adam, dog_peter, dog_wei);

			/*
			 * Using the Dog PersistentContext for performing a filtered selection. 
			 * This method select all the Dog records and filter the list using a lambda expression.
			 * This methods is experimental, used just as a java 8 integration proof of concept (Not part of the Shadow API).
			 */
			System.out.println("Filter rows using Lambda: ");
			List<Dog> dogListLambda = dog_persistor.filteredSelect(dog -> dog.age > 10);
			dogListLambda.forEach(Dog::print_a_dog);

			System.out.println("Now using Filter: ");
			List<Dog> dogListFilter = dog_persistor.select(new Filter("column_name", "=", "Wei") // A v (B ^ C)
					.or(new Filter("column_age", ">", 10)
							.and(new Filter("column_age", "<=", 12)))
					);
			dogListFilter.forEach(Dog::print_a_dog);

			
		} catch (SQLException e) {
			System.err.println("SQLException abort example");
			return;
		}finally{
			try {
				dog_persistor.deleteAll();
			} catch (SQLException e) {
				System.err.println("SQLException while cleaning");
			}
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
