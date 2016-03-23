package shadow.test;

import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;

public class PersistorTest {
	
	@BeforeClass 
	public static void init(){
		
	}
	
	@Test 
	public void test_insert(){
		Assert.assertEquals("fails basic add: ", 4, 2+2);
	}

}
