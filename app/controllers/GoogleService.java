package controllers;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.inject.Inject;

import play.mvc.Catch;
import play.mvc.Controller;
import services.GoogleDocService;
import validation.ReportFormBean;
import validation.ValidationResult;

import com.google.gdata.util.ServiceException;

public class GoogleService extends Controller {

	@Inject
	public static GoogleDocService service;

	public static void report() throws IOException, ServiceException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchFieldException, SecurityException {

		ValidationResult validationResponse = new ValidationResult();

		String responceMessage = "Server: Saving is successfull";
		response.status = 200;

		if (Security.isLogged()) {
			ReportFormBean report = getReport();
			validationResponse = service
					.saveReport(report, Security.getToken());

			if (validationResponse.hasErrors()) {
				responceMessage = validationResponse.toString();
				response.status = 400;
			}

		} else {
			validationResponse.addGlobalError("Server: User is not logged.");
			response.status = 403;
			responceMessage = validationResponse.toString();
		}
		renderText(responceMessage);
	}

	private static ReportFormBean getReport() throws IllegalAccessException,
			NoSuchFieldException, SecurityException {
		ReportFormBean report = new ReportFormBean();

		Class<?> reportClass = ReportFormBean.class;
		Field parField;

		String paramValue;
		for (String paramName : params.all().keySet()) {

			paramValue = params.get(paramName);

			if (!paramName.equals("body")) {
				parField = reportClass.getDeclaredField(paramName);
				parField.set(report, paramValue);
			}
		}

		return report;
	}

	@Catch({ IllegalArgumentException.class, IllegalAccessException.class,
			NoSuchFieldException.class, SecurityException.class })
	public static void catchBadData(Throwable t) {
		response.status = 400;
		renderText("Server: Invalid request.");
	}

	@Catch({ IOException.class, ServiceException.class })
	public static void catchServiceUnavailable(Throwable t) {
		t.printStackTrace();
		response.status = 503;
		renderText("Server: Error during writing to google doc.");
	}
}