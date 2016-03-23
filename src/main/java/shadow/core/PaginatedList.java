package shadow.core;

import java.util.ArrayList;
import java.util.List;

public class PaginatedList<T> {

	//private long pointer;
	private int pageSize;
	
	public PaginatedList(int pageSize){
		//TODO
		this.pageSize = pageSize;
	}

	public List<T> nextPage(){
		//TODO
		List<T> result = new ArrayList<T>(this.pageSize);
		return result;
	}
	
}
