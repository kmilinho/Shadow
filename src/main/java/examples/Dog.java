package examples;

import annotations.ColumnMap;
import annotations.PersistentClass;

@PersistentClass(table = "dog_table")
public class Dog {

	@ColumnMap(column = "column_name")
	private String name;

	@ColumnMap(column = "column_age")
	private int age;
	
	public Dog(String name, int age){
		this.name = name;
		this.age = age;
	}

	public String bark(){
		return "My name is " + this.name + " and I'm " + this.age + " years old, woof."; 
	}
}