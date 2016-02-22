package core;


import javax.sql.DataSource;


public class PersistentContextFactory{

	private DataSource dataSource;

	public PersistentContextFactory(DataSource ds){
		this.dataSource = ds;
	}

	public <T> PersistentContext<T> getGenericPersistentContextFor(Class<T> c){
		return new PersistentContext<T>(this.dataSource);
	}
}
