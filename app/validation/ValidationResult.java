package validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.hibernate.dialect.FirebirdDialect;

public class ValidationResult {

	private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();
	private List<String> globalErrors = new ArrayList<String>();

	public boolean hasErrors() {
		return !fieldErrors.isEmpty() || !globalErrors.isEmpty();
	}

	public void addFieldError(String fieldName, String errorMessage) {
		ensureFieldNameErrorBucketInitialized(fieldName);
		fieldErrors.get(fieldName).add(errorMessage);
	}

	public void addGlobalError(String errorMessage) {
		globalErrors.add(errorMessage);
	}

	private void ensureFieldNameErrorBucketInitialized(String fieldName) {
		if (fieldErrors.get(fieldName) == null) {
			fieldErrors.put(fieldName, new ArrayList<String>());
		}
	}

	public String toString() {
		String result = "";
		for (String msg : globalErrors) {
			result += msg;
		}
		return result;
	}

	/*
	 * private String message; private boolean isError;
	 * 
	 * public String getMessage() { return message; } public void
	 * setMessage(String message) { this.message = message; } public boolean
	 * isError() { return isError; } public void setError(boolean isError) {
	 * this.isError = isError; }
	 */
}
