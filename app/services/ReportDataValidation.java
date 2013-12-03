package services;

import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import play.mvc.Scope.Params;
import controllers.Security;

public class ReportDataValidation {

	public boolean validateFields(Params HTTPparams, String[] paramList) {
		boolean isValid = true;

		for (int i = 0; i < paramList.length; i++) {
			if (!isValidField(HTTPparams.get(paramList[i]))) {
				isValid = false;
			}
		}

		return isValid;
	}

	public boolean isValidField(String val) {
		boolean result = true;

		if (val != null) {
			if (val.matches("[0-9]{0,2}(\\.[0-9]{0,2}){0,1}")) {
				if (!StringUtils.isBlank(val)) {
					try {
						if (Double.parseDouble(val) > 0
								&& Double.parseDouble(val) <= 24) {
							result = true;
						} else {
							result = false;
						}
					} catch (NumberFormatException e) {
						result = false;
					}
				}
			} else {
				result = false;
			}
		} else {
			result = false;
		}

		return result;

	}

	public boolean validateReportHours(LinkedList<String> values) {
		double sum = 0;
		boolean isValid = true;
		if (values != null) {
			for (int i = 0; i < values.size(); i++) {
				if (!StringUtils.isBlank(values.get(i))) {
					if (NumberUtils.isNumber(values.get(i))) {
						sum += Double.parseDouble(values.get(i));
					} else {
						isValid = false;
						break;
					}
				}
			}

			if (sum <= 0 || sum > 24) {
				isValid = false;
			}
		} else {
			isValid = false;
		}
		return isValid;
	}

	public boolean validateStoryTime(String storyTime, String storyId,
			String storyName) {

		boolean isValid = true;

		if (!StringUtils.isBlank(storyTime)) {
			if (isValidUserStoryId(storyId)) {
				try {
					if (Double.parseDouble(storyTime) <= 0
							|| Double.parseDouble(storyTime) > 24) {
						isValid = false;
					}
				} catch (NumberFormatException e) {
					isValid = false;
				}
			} else {
				isValid = false;
			}
		} else {
			isValid = false;
		}

		return isValid;
	}

	public boolean isValidUserStoryId(String reallyId) {
		if (reallyId != null) {
			return reallyId.matches("(([uU][sS])|([dD][eE]))[0-9]+");
		} else {
			return false;
		}

	}

	public static boolean isNullNumber(String val) {
		boolean result = false;
		try {
			if (Double.parseDouble(val) == 0d) {
				result = true;
			}
		} catch (NumberFormatException e) {
			result = true;
		}

		return result;
	}

	public boolean isValidMail(String mail) {
		boolean isValid = true;
		// is email matches
		isValid = isValid && Security.getUserInfo().equals(mail);
		// is mail is GlobalLogic email
		isValid = isValid
				&& mail.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@globallogic.com");

		return isValid;
	}
}
