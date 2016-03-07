package core;


public class QueryParameter<T> {

	public boolean atomicUpdate;
	public T value;
	public String columnName;


	public QueryParameter(boolean atomicUpdate, T value, String tableName) {
		this.atomicUpdate = atomicUpdate;
		this.value = value;
		this.columnName = tableName;
	}

}
