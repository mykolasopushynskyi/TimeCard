package validation;

public class ReportFormBean implements FormBean{
	
	public String team;
	public String mail;

	public String storyTime;
	public String usId;
	public String usName;
	public String reportDate;
	
	public String rework;
	public String maintenance;
	public String unplannedActivity;
	public String sickLeave;
	public String vacation;
	public String standUp;
	public String retrospective;
	public String projectPlanning;
	public String demo;
	public String estimates;
	public String projectMeetings;
	public String trainingAndDevelopment;
	public String management;

	public String toString() {
		String rez = "";
		rez += "reportDate " + reportDate + " \n";
		rez += "storyTime " + storyTime + " \n";
		rez += "usId " + usId  + " \n";
		rez += "usName " + usName  + " \n";
		rez += "mail " + mail  + " \n";
		rez += "team " + team  + " \n";
		rez += "rework " + rework  + " \n";
		rez += "maintenance " + maintenance  + " \n";
		rez += "unplannedActivity " + unplannedActivity + " \n";
		rez += "sickLeave " + sickLeave  + " \n";
		rez += "vacation " + vacation  + " \n";
		rez += "standUp " + standUp  + " \n";
		rez += "retrospective " + retrospective + " \n";
		rez += "projectPlanning " + projectPlanning + " \n";
		rez += "demo " + demo + " \n";
		rez += "estimates " + estimates + " \n";
		rez += "projectMeetings " + projectMeetings + " \n";
		rez += "trainingAndDevelopment " + trainingAndDevelopment + " \n";
		rez += "management " + management + " \n";

		return rez;
	}
}
