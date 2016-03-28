package shadow.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Filter {
	
	private String txt;
	private List<Object> values;
	
	
	public Filter(String element, String operator, Object value) {
		this.txt = element.trim() + " " + operator.trim() + " ?";
		this.values = new LinkedList<Object>();
		this.values.add(value);
	}

	private Filter(String txt, List<Object> values) {
		this.txt = txt;
		this.values = values;
	}

	public String getTxt() {
		return txt;
	}

	public List<Object> getValues() {
		return Collections.unmodifiableList(values);
	}

	public static Filter and(Filter... filters) {
		return joinFilters(" AND ", filters);
	}
	
	public static Filter or(Filter... filters) {
		return joinFilters(" OR ", filters);
	}
	
	private static Filter joinFilters(String separator, Filter... filters) {
		
		String text = "";
		List<Object> values = new LinkedList<Object>();
		boolean first = true;
		
		for(Filter filter: filters) {
			
			// Create a list with the values for each paramenter in order
			values.addAll(filter.values);
			
			// Generate text for a prepared statement
			if(!first) {
				text += separator;
			} else {
				first = false;
			}
			
			if(filter.values.size() > 1) {
				text += "(" + filter.txt + ")";
			} else {
				text += filter.txt;
			}
			
		}
		
		return new Filter(text, values);
		
	}
	
}
