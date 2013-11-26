import java.util.LinkedList;

import org.junit.Test;

import play.test.UnitTest;
import services.ReportDataValidation;

public class ValidationTest extends UnitTest {
	/**
     * 
     */
	@Test
	public void isValidFieldTest() {

		ReportDataValidation v = new ReportDataValidation();

		assertFalse(v.isValidField("0"));
		assertFalse(v.isValidField(null));
		assertFalse(v.isValidField("25"));
		assertFalse(v.isValidField("-1212"));
		assertFalse(v.isValidField("df sdfsdf"));

		assertTrue(v.isValidField(""));
		assertTrue(v.isValidField("10"));
		assertTrue(v.isValidField("0.5"));
		assertTrue(v.isValidField(".25"));
		// Fixtures.loadModels("initial-data.yml");
	}

	@Test
	public void isValidHoursTest() {

		ReportDataValidation v = new ReportDataValidation();

		String[] s0 = { "9", "1", ".05", "-3" };
		String[] s1 = { "0", "0", "0", "0" };
		String[] s2 = { "12", "12", "12", "12" };
		String[] s3 = { "2", "2", "2.5", ".5" };
		LinkedList<String> l0 = new LinkedList<String>();
		LinkedList<String> l1 = new LinkedList<String>();
		LinkedList<String> l2 = new LinkedList<String>();
		LinkedList<String> l3 = new LinkedList<String>();
		for (String j : s0) {
			l0.add(j);
		}
		for (String j : s1) {
			l1.add(j);
		}
		for (String j : s2) {
			l2.add(j);
		}
		for (String j : s3) {
			l3.add(j);
		}
		assertFalse(v.validateReportHours(l1));
		assertFalse(v.validateReportHours(l2));
		assertFalse(v.validateReportHours(null));
		
		assertTrue(v.validateReportHours(l0));
		assertTrue(v.validateReportHours(l3));
	}

	@Test
	public void isValidUserStoryIdTest() {

		ReportDataValidation v = new ReportDataValidation();

		assertFalse(v.isValidUserStoryId(null));
		assertFalse(v.isValidUserStoryId(""));
		assertFalse(v.isValidUserStoryId("us1asd"));
		assertFalse(v.isValidUserStoryId("US 112"));
		assertFalse(v.isValidUserStoryId("asfddasf"));
		assertFalse(v.isValidUserStoryId("de121xcv"));

		assertTrue(v.isValidUserStoryId("uS1212"));
		assertTrue(v.isValidUserStoryId("de1212"));
		assertTrue(v.isValidUserStoryId("De11"));
		assertTrue(v.isValidUserStoryId("dE121"));
		assertTrue(v.isValidUserStoryId("DE1234"));
	}

	@Test
	public void validateStoryTimeTest() {

		ReportDataValidation v = new ReportDataValidation();

		assertFalse(v.validateStoryTime("25", "DE1234", "Ololo"));
		assertFalse(v.validateStoryTime("-1", "as1234", "Ololo"));
		assertFalse(v.validateStoryTime("", "as1234", "Ololo"));
		assertFalse(v.validateStoryTime("", "", ""));
		assertFalse(v.validateStoryTime(null, null, null));

		assertTrue(v.validateStoryTime("10", "DE1234", "Ololo"));
		assertTrue(v.validateStoryTime("8", "us1234", "Ololo"));
		assertTrue(v.validateStoryTime("10", "De1234", "Ololo"));
	}
}
