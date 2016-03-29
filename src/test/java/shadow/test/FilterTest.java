package shadow.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import shadow.core.Filter;

public class FilterTest {

	@Test
	public void testFilterConstructor() {
		Filter fil = new Filter("name", "=", "Carl");
		assertNotNull(fil);
	}

	@Test
	public void testGetTxt() {
		Filter fil = new Filter("name", "=", "Carl");
		assertEquals("name = ?", fil.getTxt());
	}

	@Test
	public void testGetValues() {
		Filter fil = new Filter("name", "=", "Carl");
		List<Object> vals = fil.getValues();
		assertEquals(1, vals.size());
		assertEquals("Carl", vals.get(0));
	}

	@Test
	public void testStaticAnd() {
		Filter fil = Filter.AND(
				new Filter("name", "=", "Carl"),
				new Filter("surname", "=", "Shadow"),
				new Filter("age", ">=", 18)
				);
		
		assertEquals("name = ? AND surname = ? AND age >= ?", fil.getTxt());
		
		List<Object> vals = fil.getValues();
		assertEquals(3, vals.size());
		assertEquals("Carl", vals.get(0));
		assertEquals("Shadow", vals.get(1));
		assertEquals(18, vals.get(2));
	}
	
	@Test
	public void testStaticAndOfOne() {
		Filter fil = Filter.AND(
				new Filter("name", "=", "Carl")
				);
		
		assertEquals("name = ?", fil.getTxt());
	}
	
	@Test
	public void testStaticAndEmpty() {
		Filter fil = Filter.AND();
		
		assertEquals("", fil.getTxt());
	}
	
	@Test
	public void testStaticAndEmptyWithNotEmpty() {
		Filter fil = Filter.AND(Filter.AND(), new Filter("name", "=", "Carl"));
		
		assertEquals("name = ?", fil.getTxt());
		List<Object> vals = fil.getValues();
		assertEquals(1, vals.size());
		assertEquals("Carl", vals.get(0));
	}

	@Test
	public void testStaticOr() {
		Filter fil = Filter.OR(
				new Filter("name", "=", "Carl"),
				new Filter("surname", "=", "Shadow"),
				new Filter("age", ">=", 18)
				);
		
		assertEquals("name = ? OR surname = ? OR age >= ?", fil.getTxt());
		
		List<Object> vals = fil.getValues();
		assertEquals(3, vals.size());
		assertEquals("Carl", vals.get(0));
		assertEquals("Shadow", vals.get(1));
		assertEquals(18, vals.get(2));
	}
	
	@Test
	public void testStaticOrWithStaticAnd() {
		Filter fil = Filter.OR(
				new Filter("name", "=", "Carl"),
				Filter.AND(
					new Filter("surname", "=", "Shadow"),
					new Filter("age", ">=", 18)
					)
				);
		
		assertEquals("name = ? OR (surname = ? AND age >= ?)", fil.getTxt());
		
		List<Object> vals = fil.getValues();
		assertEquals(3, vals.size());
		assertEquals("Carl", vals.get(0));
		assertEquals("Shadow", vals.get(1));
		assertEquals(18, vals.get(2));
	}
	
	@Test
	public void testOrWithAnd() {
		Filter fil = new Filter("name", "=", "Carl")
				.or( new Filter("surname", "=", "Shadow")
						.and(new Filter("age", ">=", 18))
					);

		
		assertEquals("name = ? OR (surname = ? AND age >= ?)", fil.getTxt());
		
		List<Object> vals = fil.getValues();
		assertEquals(3, vals.size());
		assertEquals("Carl", vals.get(0));
		assertEquals("Shadow", vals.get(1));
		assertEquals(18, vals.get(2));
	}

}
