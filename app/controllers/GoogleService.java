package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import play.Play;
import play.mvc.Controller;
import play.utils.Properties;

import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleService extends Controller {
	private static final FeedURLFactory urlFactory = FeedURLFactory
			.getDefault();

	public static void report() {

		String mail = params.get("mail");
		String usId = params.get("usId");
		String usName = params.get("usName");

		List<String> report = new LinkedList<String>();

		report.add(mail);
		report.add(usId);
		report.add(usName);

		SpreadsheetService service = getSpreadsheetService();

	    URL listFeedUrl;
		try {
			listFeedUrl = findWorksheet(service).getListFeedUrl();		    
			ListEntry row = new ListEntry();
			
		    row.getCustomElements().setValueLocal("User", mail);
		    row.getCustomElements().setValueLocal("Task", usId);
		    row.getCustomElements().setValueLocal("Description", usName);

		    row = service.insert(listFeedUrl, row);		    
		} catch (IOException | ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static SpreadsheetService getSpreadsheetService() {
		SpreadsheetService service = new SpreadsheetService("TimeCard");
		Properties prop = new Properties();

		try {
			prop.load(Play.classloader.getResourceAsStream("pass.conf"));
			String user = prop.get("googleacco");
			String pass = prop.get("googlepass");

			try {
				service.setUserCredentials(user, pass);
				service.setProtocolVersion(SpreadsheetService.Versions.V3);
			} catch (AuthenticationException e) {
				e.printStackTrace();
				service = null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			service = null;
		}

		return service;
	}

	private static WorksheetEntry findWorksheet(SpreadsheetService service)
			throws IOException, ServiceException {

		String spreadsheetKey = "0AsiyXCOmDCswdHNSUHlLNmtoMkZjdjVZWUJ3bUhMekE";
		String worksheetName = "Polaris";

		SpreadsheetFeed feed = service.getFeed(
				urlFactory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
		for (SpreadsheetEntry se : feed.getEntries()) {
			if (se.getSpreadsheetLink().getHref().endsWith(spreadsheetKey)) {
				for (WorksheetEntry we : se.getWorksheets()) {
					if (we.getTitle().getPlainText()
							.equalsIgnoreCase(worksheetName)) {
						return we;
					}
				}
			}
		}
		throw new RuntimeException("Cannot find worksheet=" + worksheetName);
	}

	private static CellFeed getRow(SpreadsheetService service, int minRow,
			int maxRow, int minCol, int maxCol) {
		CellQuery cellQuery;
		try {
			cellQuery = new CellQuery(findWorksheet(service).getCellFeedUrl());
			cellQuery.setMinimumCol(minCol);
			cellQuery.setMaximumCol(maxCol);
			cellQuery.setMinimumRow(minRow);
			cellQuery.setMaximumRow(maxRow);
			cellQuery.setReturnEmpty(true);
			return service.query(cellQuery, CellFeed.class);
		} catch (ServiceException e) {
			throw new RuntimeException(
					"Service error when getting data to edit", e);
		} catch (IOException e) {
			throw new RuntimeException("Connection with server broken", e);
		}
	}
}
