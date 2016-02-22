package examples;

import annotations.ColumnMap;
import annotations.PersistentClass;

@PersistentClass(table = "dog_table")
public class Dog {
	
	@ColumnMap(column = "column_name")
	private String name;
	
	@ColumnMap(column = "column_age")
	private int age;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
}
