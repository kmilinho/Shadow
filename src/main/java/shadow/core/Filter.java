package shadow.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Filter {
	
	private String txt;
	private List<Object> values;
	
	/**
	 * Create a basic query filter that can be combined with other filters.
	 * @param element Element to filter by.
	 * @param operator Operator of the filter: =, !=, >, >=, etc
	 * @param value Value of the element
	 */
	public Filter(String element, String operator, Object value) {
		this.txt = element.trim() + " " + operator.trim() + " ?";
		this.values = new LinkedList<Object>();
		this.values.add(value);
	}

	/**
	 * Creates a filter specifying the text and values of the elements
	 * @param txt text of the filter for a prepared statement (with the values replaced by "?")
	 * @param values List of the values of this filter, that is the values that will have to replace the "?"
	 */
	private Filter(String txt, List<Object> values) {
		this.txt = txt;
		this.values = values;
	}

	/**
	 * Get the text of the filter for a prepared statement (with the values replaced by "?")
	 * @return String with the text
	 */
	public String getTxt() {
		return txt;
	}

	/**
	 * Get a list of the values of this filter, that is the values that will have to replace the "?" 
	 * in the prepared statement.
	 * @return An unmodifiable list with the values
	 */
	public List<Object> getValues() {
		return Collections.unmodifiableList(values);
	}
	
	/**
	 * Combine multiple filters with an AND operator
	 * @param filters
	 * @return Filter with the combination
	 */
	public static Filter AND(Filter... filters) {
		return joinFilters(" AND ", filters);
	}
	
	/**
	 * Combine multiple filters with an OR operator
	 * @param filters
	 * @return Filter with the combination
	 */
	public static Filter OR(Filter... filters) {
		return joinFilters(" OR ", filters);
	}

	/**
	 * Combine multiple filters with an AND operator
	 * @param filters
	 * @return Filter with the combination
	 */
	public Filter and(Filter... filters) {
		return joinFilters(" AND ", combineFiltersInList(this, filters));
	}
	
	/**
	 * Combine multiple filters with an OR operator
	 * @param filters
	 * @return Filter with the combination
	 */
	public Filter or(Filter... filters) {
		return joinFilters(" OR ", combineFiltersInList(this, filters));
	}
	
	/**
	 * Auxiliary method to add a filter to the begining of list of filters
	 * @param f Filter to add
	 * @param filters List of filters
	 * @return List of filters
	 */
	private Filter[] combineFiltersInList(Filter f, Filter... filters) {
		Filter[] res = new Filter[filters.length + 1];
		res[0] = f;
		for(int i = 0; i < filters.length; ++i) {
			res[i+1] = filters[i];
		}
		return res;
	}
	
	/**
	 * This method joins multiple filters using a separator generating a new filter (more complex)
	 * @param separator Separator to use (" AND " or " OR ")
	 * @param filters List of filter to join
	 * @return A new filter
	 */
	private static Filter joinFilters(String separator, Filter... filters) {
		
		String text = "";
		List<Object> values = new LinkedList<Object>();
		boolean first = true;
		
		for(Filter filter: filters) {
			
			if(filter.values.size() == 0 || filter.txt.length() == 0) {
				continue;
			}
			
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
