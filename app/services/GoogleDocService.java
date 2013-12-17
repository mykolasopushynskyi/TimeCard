package services;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import validation.ReportFormBean;
import validation.ReportFormValidator;
import validation.ValidationResult;

import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

public class GoogleDocService {

	private static final String ITERATION = "Iteration";
	private static final String TEAM = "Team";
	private static final String USER_NAME = "Username";
	private static final String RALYY_ID = "Rally ID";
	private static final String TIME_STAMP = "Timestamp";
	private static final String STORY_NAME = "Story name";
	private static final String HOURS = "Hours";
	private static final String TYPE = "Type";

	private static final String SPREADSHEET_KEY = (String) Play.configuration
			.getProperty("spreadsheet.key", "");	

	private static FeedURLFactory urlFactory = FeedURLFactory.getDefault();
	private static SpreadsheetService service;
	private static String workSheetName;

	public static ValidationResult saveReport(ReportFormBean report,
			String token) throws IOException, ServiceException {

		boolean saveStoryTime;
		ValidationResult saveResponse;
		ReportFormValidator validator;
		workSheetName = getIterationString();

		validator = new ReportFormValidator();

		saveStoryTime = validator.saveStoryTime(report);
		saveResponse = validator.validateFormBean(report);

		if (!saveResponse.hasErrors()) {
			service = getSpreadsheetService(token);
			save(report, saveStoryTime);
		}
		
		return saveResponse;
	}

	//09 December 2013 year begins 78 iteration
	private static String getIterationString() {
		int offsetIteration = 78;
		long day = 24 * 60 * 60 * 1000;
		
		long startPoint = (new Date(2013-1900,11,9)).getTime();
		long now = (new Date()).getTime();
		
		long millisFromStartPoint = now - startPoint;
		long daysFromStartPoint = (millisFromStartPoint - millisFromStartPoint % day) / day;
		long itersFromStartPoint = (daysFromStartPoint - daysFromStartPoint % 14) / 14;
		long currentIteration = itersFromStartPoint + offsetIteration;

		return "i" + currentIteration;
	}

	private static void save(ReportFormBean report, boolean saveStoryTime)
			throws IOException, ServiceException {

		ListEntry row;
		URL workSheetUrl = findOrCreateWorksheet(service).getListFeedUrl();

		// save all parameters
		saveRowEntry(workSheetUrl, report, report.rework, "Rework");
		saveRowEntry(workSheetUrl, report, report.maintenance, "Maintenance");
		saveRowEntry(workSheetUrl, report, report.unplannedActivity, "Unplanned Activity");
		saveRowEntry(workSheetUrl, report, report.sickLeave, "Sick Leave");
		saveRowEntry(workSheetUrl, report, report.vacation, "Vacation");
		saveRowEntry(workSheetUrl, report, report.standUp, "Stand Up");
		saveRowEntry(workSheetUrl, report, report.retrospective, "Retrospective");
		saveRowEntry(workSheetUrl, report, report.projectPlanning, "Project Planning");
		saveRowEntry(workSheetUrl, report, report.demo, "Demo");
		saveRowEntry(workSheetUrl, report, report.estimates, "Estimates");
		saveRowEntry(workSheetUrl, report, report.projectMeetings, "Project Meetings");
		saveRowEntry(workSheetUrl, report, report.trainingAndDevelopment, "Training and development");
		saveRowEntry(workSheetUrl, report, report.management, "Management");

		// save story time parameter
		if (saveStoryTime) {
			row = createStoryTimeEntry(report);
			row = service.insert(workSheetUrl, row);
		}
	}

	private static ListEntry createRowEntry(ReportFormBean report,
			String hoursValue, String activityType) {
		ListEntry row = new ListEntry();

		row.getCustomElements().setValueLocal(headerToGoogleAPI(USER_NAME), report.mail);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(TEAM), report.team);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(TIME_STAMP), report.reportDate);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(HOURS), hoursValue);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(TYPE), activityType);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(ITERATION), workSheetName);

		return row;
	}

	private static ListEntry createStoryTimeEntry(ReportFormBean report) {
		ListEntry row = new ListEntry();

		row.getCustomElements().setValueLocal(headerToGoogleAPI(USER_NAME), report.mail);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(TEAM), report.team);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(TIME_STAMP), report.reportDate);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(HOURS), report.storyTime);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(RALYY_ID), report.usId);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(STORY_NAME), report.usName);
		row.getCustomElements().setValueLocal(headerToGoogleAPI(TYPE), "Story Time");
		row.getCustomElements().setValueLocal(headerToGoogleAPI(ITERATION), workSheetName);
		return row;
	}

	private static SpreadsheetService getSpreadsheetService(String token) {
		SpreadsheetService service = new SpreadsheetService("TimeCard");
		service.setHeader("Authorization", "Bearer " + token);
		service.setProtocolVersion(SpreadsheetService.Versions.V3);
		return service;
	}

	private static WorksheetEntry findOrCreateWorksheet(
			SpreadsheetService service) throws IOException, ServiceException {

		SpreadsheetFeed feed = service.getFeed(
				urlFactory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
		for (SpreadsheetEntry spreadsheet : feed.getEntries()) {
			if (spreadsheet.getSpreadsheetLink().getHref()
					.endsWith(SPREADSHEET_KEY)) {
				for (WorksheetEntry worksheet : spreadsheet.getWorksheets()) {
					if (worksheet.getTitle().getPlainText()
							.equalsIgnoreCase(workSheetName)) {
						//writeHeader(service, worksheet);
						return worksheet;
					}
				}
			    return createReportWorksheet(service, spreadsheet);
			}
		}

		return null;
	}

	private static WorksheetEntry createReportWorksheet(
			SpreadsheetService service, SpreadsheetEntry spreadsheet)
			throws IOException, ServiceException {
		WorksheetEntry worksheet = new WorksheetEntry();
		worksheet.setTitle(new PlainTextConstruct(workSheetName));
		worksheet.setColCount(10);
		worksheet.setRowCount(10);
		
		URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
		worksheet = service.insert(worksheetFeedUrl, worksheet);
	
		writeHeader(service, worksheet);

		return worksheet;
	}

	// For google API query to write data in column with HEADER ROW
	// you must write header of specified column 
	// without white spaces and lower case!!!
	private static String headerToGoogleAPI(String columnHeader) {
		return StringUtils.deleteWhitespace(columnHeader).toLowerCase();
	}
	
	// Writes names of headers in spreadsheet
	// User need to create google spreadsheet with HEADER ROW labeled by the
	// following captions!!!
	private static void writeHeader(SpreadsheetService service,
			WorksheetEntry worksheet) throws IOException, ServiceException {
		URL cellFeedUrl = worksheet.getCellFeedUrl();
		CellFeed cellFeed =  service.getFeed(cellFeedUrl, CellFeed.class);
		
		cellFeed.insert(new CellEntry(1, 1, ITERATION));
		
		cellFeed.insert(new CellEntry(1, 2, TEAM));
		cellFeed.insert(new CellEntry(1, 3, TIME_STAMP));
		cellFeed.insert(new CellEntry(1, 4, USER_NAME));
		cellFeed.insert(new CellEntry(1, 5, RALYY_ID));
		cellFeed.insert(new CellEntry(1, 6, STORY_NAME));
		cellFeed.insert(new CellEntry(1, 7, HOURS));
		cellFeed.insert(new CellEntry(1, 8, TYPE));
	}

	private static boolean saveRowEntry(URL workSheetUrl,
			ReportFormBean report, String hoursValue, String activityType)
			throws IOException, ServiceException {

		ListEntry row;
		if (!StringUtils.isBlank(hoursValue)) {
			row = createRowEntry(report, hoursValue, activityType);
			service.insert(workSheetUrl, row);
			return true;
		} else {
			return false;
		}
	}

}
