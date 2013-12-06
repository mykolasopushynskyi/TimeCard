package unit;
import java.util.LinkedList;

import org.junit.Test;

import play.test.UnitTest;
import validation.ReportFormBean;
import validation.ReportFormValidator;

public class ValidationTest extends UnitTest {

	@Test
	public void validationReturnResultTest() {

		ReportFormValidator v = new ReportFormValidator();
		ReportFormBean b = new ReportFormBean();
		
		assertNotNull(v.validateFormBean(b));

	}
	
}
