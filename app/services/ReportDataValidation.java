package services;

import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import controllers.Security;

public class ReportDataValidation {

	public boolean validateReportHours(LinkedList<String> values) {
		double sum = 0;
		boolean isValid = true;

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
		return isValid;
	}
	
	public boolean validateStoryTime(String storyTime, String usId,
			String usName) {

		boolean isValid = true;

		if (!StringUtils.isBlank(storyTime)) {
			if (isValidUserStoryId(usId)) {
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
		return reallyId.matches("(([uU][sS])|([dD][eE]))[0-9]+");
	}

	public boolean isValidCredentials(String mail) {
		boolean isValid = true;

		// is user logged in app
		isValid = isValid && Security.isLogged();
		// is email matches
		isValid = isValid && Security.getUserInfo().equals(mail);
		// is mail is GlobalLogic email
		isValid = isValid
				&& mail.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@globallogic.com");

		return isValid;
	}
}
