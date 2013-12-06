package validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.google.gdata.client.spreadsheet.SpreadsheetService;

import controllers.Security;
import play.mvc.Scope.Params;

public class ReportFormValidator extends FormValidator<ReportFormBean> {

	public boolean saveStoryTime(ReportFormBean form) {
		ReportFormBean report = form;

		boolean saveStoryTime;

		if (!StringUtils.isBlank(report.storyTime)) {
			if (validateStoryTime(report.storyTime, report.usId, report.usName)) {
				saveStoryTime = true;
			} else {

				saveStoryTime = false;
			}
		} else {
			saveStoryTime = false;
		}

		return saveStoryTime;
	}

	@Override
	public ValidationResult validateFormBean(ReportFormBean report) {

		ValidationResult saveResult = new ValidationResult();

		String resultText = null;

		if (isValidMail(report.mail)) {
			if (validateFields(report)) {
				if (validateReportHours(report)) {
					if (!StringUtils.isBlank(report.storyTime)) {
						if (!validateStoryTime(report.storyTime, report.usId,
								report.usName)) {
							resultText = "Server: Can't save story time";
						}
					}
				} else {
					resultText = "Server: Hours sum validation failed.";
				}
			} else {
				resultText = "Server: Not all fiels is valid.";
			}
		} else {
			resultText = "Server: Email is invalid";
		}
		if (resultText != null)
			saveResult.addGlobalError(resultText);
		return saveResult;
	}

	private boolean validateFields(ReportFormBean report) {
		boolean isValid = true;

		for (String val : getReportFields(report)) {
			isValid = isValid && isValidField(val);
		}
		return isValid;
	}

	private boolean isValidField(String val) {

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

	private boolean validateReportHours(ReportFormBean report) {

		boolean isValid = true;
		double sum = 0;

		for (String val : getReportFields(report)) {

			if (!StringUtils.isBlank(val)) {
				if (NumberUtils.isNumber(val)) {
					sum += Double.parseDouble(val);
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

	private boolean validateStoryTime(String storyTime, String storyId,
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

	private boolean isValidUserStoryId(String reallyId) {
		if (reallyId != null) {
			return reallyId.matches("(([uU][sS])|([dD][eE]))[0-9]+");
		} else {
			return false;
		}
	}

	private static boolean isNullNumber(String val) {
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

	private boolean isValidMail(String mail) {
		boolean isValid = true;
		// is email matches
		isValid = isValid && Security.getUserInfo().equals(mail);
		// is mail is GlobalLogic email
		isValid = isValid
				&& mail.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@globallogic.com");

		return isValid;
	}

	private List<String> getReportFields(ReportFormBean report) {
		List<String> vals = new ArrayList<String>();
		vals.add(report.storyTime);
		vals.add(report.rework);
		vals.add(report.maintenance);
		vals.add(report.unplannedActivity);
		vals.add(report.sickLeave);
		vals.add(report.vacation);
		vals.add(report.standUp);
		vals.add(report.retrospective);
		vals.add(report.projectPlanning);
		vals.add(report.demo);
		vals.add(report.estimates);
		vals.add(report.projectMeetings);
		vals.add(report.trainingAndDevelopment);
		vals.add(report.management);
		return vals;
	}
}
