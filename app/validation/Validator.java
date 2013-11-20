package validation;

import java.lang.reflect.Field;

public interface Validator {
	boolean validateField(Field field);
}
