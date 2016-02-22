package core;


public class QueryParameter<T> {

	private boolean atomicUpdate;
	private T value;
	private String columnName;
	
	
	public QueryParameter(boolean atomicUpdate, T value, String tableName) {
		this.atomicUpdate = atomicUpdate;
		this.value = value;
		this.columnName = tableName;
	}
	
	public boolean isAtomicUpdate() {
		return atomicUpdate;
	}
	
	public void setAtomicUpdate(boolean atomicUpdate) {
		this.atomicUpdate = atomicUpdate;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String tableName) {
		this.columnName = tableName;
	}
	
	
}
