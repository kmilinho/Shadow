package shadow.core;


import javax.sql.DataSource;

import shadow.exceptions.NotValidPersistentClassException;


public class PersistentContextFactory{

	private DataSource dataSource;

	public PersistentContextFactory(DataSource ds){
		this.dataSource = ds;
	}

	public <T> PersistentContext<T> getPersistentContextFor(Class<T> c) throws NotValidPersistentClassException{
		return new PersistentContext<T>(this.dataSource, c);
	}
}
