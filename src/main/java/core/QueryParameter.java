package core;


public class QueryParameter<T> {

	public boolean isId;
	public boolean atomicUpdate;
	public T value;
	public String columnName;

	public QueryParameter(boolean atomicUpdate, T value, String tableName, boolean isId) {
		this.isId = isId;
		this.atomicUpdate = atomicUpdate;
		this.value = value;
		this.columnName = tableName;
	}

}
