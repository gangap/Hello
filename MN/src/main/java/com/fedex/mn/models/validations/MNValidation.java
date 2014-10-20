package com.fedex.mn.models.validations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fedex.mn.app.Application;
import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.models.impls.ApprovalModel;
import com.fedex.mn.models.impls.BasicDetailModel;
import com.fedex.mn.models.impls.BuildDetailModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.DeploymentDetailModel;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.TestDetailModel;
import com.fedex.mn.utils.Util;

@Component
public class MNValidation
{
	private Map<String, ClientMsg> signValids;
	
	private static Logger log = Logger.getLogger(MNValidation.class);
	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private Application app;
	
	@PostConstruct 
	public void init()
	{
		signValids = new HashMap<String, ClientMsg>();
		
		signValids.put("tm", new ClientMsg("sign", "Signature Required", "Please obtain the approval of the Team Manager"));
		signValids.put("sa", new ClientMsg("sign", "Signature Required", "Please obtain the SQA Accepted approval"));
		signValids.put("st", new ClientMsg("sign", "Signature Required", "Please obtain the SQA Tested/Not Tested approval"));
		signValids.put("pbs", new ClientMsg("sign", "Signature Required", "Please obtain the approval of the PBS MD"));
		signValids.put("orig", new ClientMsg("sign", "Signature Required", "Please obtain the approval of the Originator MD"));
		signValids.put("vp", new ClientMsg("sign", "Signature Required", "Please obtain the approval of the VP"));
		signValids.put("sqa", new ClientMsg("sign", "Signature Required", "Please obtain the approval of the SQA MD"));
		signValids.put("rm", new ClientMsg("sign", "Signature Required", "Please obtain the approval of the Release Manger"));
		signValids.put("pbsO", new ClientMsg("sign", "Optional Signature", "The PBS team's signature is optional"));
	}
	
	public List<ClientMsg> validateToSave(MNModel mn)
	{
		List<ClientMsg> errors = new ArrayList<ClientMsg>();
		
		errors.addAll(validateBasic(mn.getBasicDetail()));
		
		return errors;
	}
	
	public List<ClientMsg> validateToUpdate(MNModel mn)
	{
		List<ClientMsg> errors = new ArrayList<ClientMsg>();
		
		BasicDetailModel basic = mn.getBasicDetail();
		
		String statusName = mem.getName("status_types", basic.getStatus().getId());
		
		//Do not do any validation if the status is 'Open' or 'Cancelled'
		if(statusName.equals("Open") || statusName.equals("Cancelled"))
			return errors;
		
		errors.addAll(validateBasic(basic));			
		
		if(shouldValidateTest(basic.getDestination().getId(), basic.getLoadType().getId(), basic.getStatus().getId()))
			errors.addAll(validateTest(mn.getTestDetail(), basic));
		
		if(shouldValidateBuild(basic.getChangeType().getId()))
			errors.addAll(validateBuild(mn.getBuildDetail(), mn.getBasicDetail()));
		
		errors.addAll(validateDeploy(mn.getDeploymentDetail()));
		
		return errors;
	}
	
	public void removeSignature(MNModel mn, List<ApprovalModel> approvals, int approvalID)
	{
		
	}
	
	public List<ClientMsg> validateSignatures(int destId, int loadId, int statusId, List<ApprovalModel> approvals)
	{
		List<ClientMsg> errors = new ArrayList<ClientMsg>(0);
		
		String dest = Util.getName(mem.getProperties("destination_types"), destId),
			load = Util.getName(mem.getProperties("load_types"), loadId),
			status = Util.getName(mem.getProperties("status_types"), statusId);
		
		if((dest == null || dest.isEmpty()) ||
			(load == null || load.isEmpty()) ||
			(status == null || status.isEmpty()))
		{
			log.error("Invalid destination/load/status for validation");
			return errors;
		}
		
		if(status.equals("Deferred") || status.equals("Test Failed"))
		{
			status = "Cancelled";
		}
		
		if(dest.equals("Production"))
		{
			//Any status that is greater than 'Open' and if the status is not 'Cancelled'
			if(compareStatuses(status, "Open") >= 0 
				&& !status.equals("Cancelled"))
			{
				//All Load Types need to have the Team Manager's Signature
				if(!hasApproval("Team Manager", approvals))
					errors.add(signValids.get("tm"));
				
				//Load Type: Expedited
				if(load.equals("Expedited"))
				{
					if(!hasApproval("Originator's MD", approvals))
						errors.add(signValids.get("orig"));
					if(!hasApproval("SQA MD", approvals))
						errors.add(signValids.get("sqa"));
				}
				
				//Load Type: Peak Expedited
				else if(load.equals("Peak Expedited"))
				{
					if(!hasApproval("Originator's MD", approvals))
						errors.add(signValids.get("orig"));
					if(!hasApproval("Vice President", approvals))
						errors.add(signValids.get("vp"));
					if(!hasApproval("SQA MD", approvals))
						errors.add(signValids.get("sqa"));						
				}
				
				//Load Type: Peak Normal
				else if(load.equals("Peak Normal"))
				{
					if(!hasApproval("Originator's MD", approvals))
						errors.add(signValids.get("orig"));
					if(!hasApproval("Vice President", approvals))
						errors.add(signValids.get("vp"));
				}
			}
			
			if(compareStatuses(status, "Ready for SQA") >= 0
				&& !status.equals("Cancelled")
				&& !load.equals("Emergency"))
			{
				if(!hasApproval("SQA Accepted", approvals))
					errors.add(signValids.get("sa"));
			}
			
			if(compareStatuses(status, "In SQA Test") >= 0
				&& !status.equals("Cancelled")
				&& !load.equals("Emergency"))
			{
				if(!(hasApproval("SQA Tested", approvals) || hasApproval("SQA Not Tested", approvals)))
					errors.add(signValids.get("st"));
			}
			
		}
		else if(dest.contains("L3") &&
			app.getAppState().isL3c5Flag())
		{
			//Any status that is greater than 'Open' and if the status is not 'Cancelled'
			if(compareStatuses(status, "Open") >= 0 
				&& !status.equals("Cancelled"))
			{
				//The load type does not matter as they should always be 'Normal'
				if(!hasApproval("Originator's MD", approvals))
					errors.add(signValids.get("orig"));
				
				if(!hasApproval("SQA MD", approvals))
					errors.add(signValids.get("sqa"));
			}
		}
		
		
		return errors;
	}
	
	public boolean hasNeededSignatures(String dest, String load, String status, List<ApprovalModel> approvals)
	{
		List<ClientMsg> msgs = validateSignatures(Util.getId(mem.getProperties("destination_types"), dest), 
			Util.getId(mem.getProperties("load_types"), load),
			Util.getId(mem.getProperties("status_types"), status),
			approvals);	 
		
		if(msgs.isEmpty())
			return false;
		else if(msgs.size() == 1 && msgs.get(0).equals(signValids.get("pbsO")))
			return false;
		else
			return true;
	}
	
	/**
	 * Gets all of the signatures (required/optional) for the MN
	 * @param dest - The Destination Type
	 * @param load - The Load Type
	 * @param status - The Status Type
	 * @param approvals - The list of approvals the MN already has 
	 * @return - A @List<DBProperty> of signatures that the MN currently still needs
	 */
	public List<DBProperty> getSignatures(String dest, String load, String status, List<ApprovalModel> approvals)
	{
		List<ClientMsg> msgs = validateSignatures(Util.getId(mem.getProperties("destination_types"), dest), 
			Util.getId(mem.getProperties("load_types"), load),
			Util.getId(mem.getProperties("status_types"), status),
			approvals);		
		
		List<DBProperty> signatures = new ArrayList<DBProperty>();		
		List<DBProperty> approvalProps = mem.getProperties("approval_types");
	
		for(ClientMsg msg : msgs)
		{
			if(msg.equals(signValids.get("tm")))
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "Team Manager")));		
			else if(msg.equals(signValids.get("sa")))
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "SQA Accepted")));
			else if(msg.equals(signValids.get("st")))
			{
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "SQA Tested")));
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "SQA Not Tested")));
			}
			else if(msg.equals(signValids.get("orig")))
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "Originator's MD")));
			else if(msg.equals(signValids.get("vp")))
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "Vice President")));
			else if(msg.equals(signValids.get("sqa")))
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "SQA MD")));
			else if(msg.equals(signValids.get("pbsO")))
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "PBS")));
			else if(msg.equals(signValids.get("rm")))
				signatures.add(approvalProps.get(Util.getIndex(approvalProps, "Release Manager")));
	
		}
		
		return signatures;
	}
	
	public List<DBProperty> getAvailableStatuses(String dest, String load, List<ApprovalModel> approvals)
	{
		List<DBProperty> statuses = mem.getProperties("status_types");
		List<DBProperty> validStatuses = new ArrayList<DBProperty>();
		
		validStatuses.add(statuses.get(Util.getIndex(statuses, "Open")));
		validStatuses.add(statuses.get(Util.getIndex(statuses, "Pending Approval")));
		validStatuses.add(statuses.get(Util.getIndex(statuses, "Cancelled")));
		
		if(	dest == null || dest.isEmpty() || 
			load == null || load.isEmpty() ||
			dest.equals("---") || load.equals("---"))
			return validStatuses;
			
		//System.out.println(statuses.iterator())
		if(!dest.equals("Production"))
		{
			if(dest.contains("L3") && 
				app.getAppState().isL3c5Flag() &&
				!hasApproval("Originator's MD", approvals))
			{
				return validStatuses;				
			}
			else if(dest.equals("L2 Only")) {			
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready for Full Build")));			
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready for SQA")));				
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Test Failed")));
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Completed")));
			}
			else {
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready for SQA")));
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Test Failed")));
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Completed")));
			}
			
		}		
		else
		{
		
			validStatuses.add(statuses.get(Util.getIndex(statuses, "Deferred")));
			
			if(!hasApproval("Team Manager", approvals))
				return validStatuses;
			else if(!hasApproval("Originator's MD", approvals) && 
					(load.equals("Expedited") || 
					load.equals("Peak Normal") || 
					load.equals("Peak Expedited")))
				return validStatuses;
			else if(!hasApproval("Vice President", approvals) && 
				(load.equals("Peak Expedited") || load.equals("Peak Normal")))
				return validStatuses;
			else if(!hasApproval("SQA MD", approvals) && 
				load.equals("Peak Expedited"))
				return validStatuses;
			else if(!hasApproval("SQA MD", approvals) && 
				(load.equals("Expedited") || load.equals("Peak Expedited")))
				return validStatuses;
						
				
			//Emergencies can be directly sent to 'Ready for Production'
			if(hasApproval("Team Manager", approvals) 
				&& load.equals("Emergency")) {
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready To Build")));
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Staged For Production")));
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready For Sync")));
			}
			else {
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready for SQA")));
			}
			
			if(hasApproval("SQA Accepted", approvals)
				&& !load.equals("Emergency")) {
			    validStatuses.add(statuses.get(Util.getIndex(statuses, "Accepted for L5 / L6")));
			    validStatuses.add(statuses.get(Util.getIndex(statuses, "In SQA Test")));
			    validStatuses.add(statuses.get(Util.getIndex(statuses, "Test Failed")));
			}
				
			if(hasApproval("SQA Tested", approvals) && 
				!hasApproval("SQA Not Tested", approvals) && 
				!load.equals("Emergency")) {
				validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready For Production")));
			}
			
			if(!hasApproval("SQA Tested", approvals) && 
					hasApproval("SQA Not Tested", approvals) && 
					!load.equals("Emergency")) {
					validStatuses.add(statuses.get(Util.getIndex(statuses, "Ready For Production")));
			}
			
			if(!hasConfirmation(approvals))
				return validStatuses;
			
			validStatuses.add(statuses.get(Util.getIndex(statuses, "Completed")));
		}
		
		return validStatuses;
	}
	
	public int compareStatuses(String compare1, String compare2)
	{
		List<DBProperty> statuses = mem.getProperties("status_types");
				
		int index1 = -1, index2 = -1;
		
		
		for(int index = 0; index < statuses.size(); index++)
		{
			DBProperty prop = statuses.get(index);
			
			if(prop.getName().equals(compare1))
			{
				index1 = index; 
			}
			if(prop.getName().equals(compare2))
			{
				index2 = index;
			}
		}
		
		if(index1 == -1 || index2 == -1)
			throw new UnsupportedOperationException("Invalid status comparision: " + compare1 + " and " + compare2);
		else if(index1 > index2)
			return 1;
		else if(index1 == index2)
			return 0;
		else //if(index1 <  index2)
			return -1;
	}
	
	private boolean hasApproval(String name, List<ApprovalModel> approvals)
	{
		for(ApprovalModel approval : approvals)
		{			
			if(approval.getType() == 0 && approval.getApprovalType().getName().toLowerCase().equals(name.toLowerCase()))
				return true;
		}
		
		return false;
	}
	
	private boolean hasConfirmation(List<ApprovalModel> approvals)
	{
		for(ApprovalModel approval : approvals)
		{			
			if(approval.getType() == 1)
				return true;
		}
		
		return false;
	}
	
	public List<ClientMsg> validateBasic(BasicDetailModel basic)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		if(basic.getArtifact() == null || !basic.getArtifact().matches("^(?!\\s*$).+"))
		{
			msgs.add(new ClientMsg("validation", "basic", "The ITG/ Artifact cannot be empty"));
		}
		
		if(basic.getExpectedLoadDate() == null)
		{
			msgs.add(new ClientMsg("validation", "basic", "The date cannot be empty"));
		}
		
		if(basic.getStatus().getId() < 0)
		{
			msgs.add(new ClientMsg("validation", "basic", "status.id", "Please select a status!"));
		}
		
		if(basic.getDestination().getId() < 0)
		{
			msgs.add(new ClientMsg("validation", "basic", "destination.id", "Please select a destination!"));
		}
		
		if(basic.getSymphonyProfile().getId() < 0)
		{
			msgs.add(new ClientMsg("validation", "basic", "symphonyProfile.id", "Please select a Symphony Profile!"));
		}
		
		if(basic.getRelease().getId() < 0)
		{
			msgs.add(new ClientMsg("validation", "basic", "release.id", "Please select a Release"));
		}
		
		if(basic.getChangeType().getId() < 0)
		{
			msgs.add(new ClientMsg("validation", "basic", "changeType.id", "Please select the type of change"));
		}
		
		if(basic.getLoadType().getId() < 0)
		{
			msgs.add(new ClientMsg("validation", "basic", "loadType.id", "Please select the load type!"));
		}			
		if(!mem.getName("dependency_types", basic.getDependency().getId()).equals("No"))
		{
			if(basic.getDependencyIds().size() == 0)
			{
				msgs.add(new ClientMsg("validation", "basic", "dependencyId", "Please select an MN dependency"));
			}
		}
		else if(basic.getDependencyIds().size() != 0)
		{
			msgs.add(new ClientMsg("validation", "basic", "dependencyId", "Please select With or After for the Dependent MN"));
		}
		
		//Expected Time
		if(basic.getElHour() < 0 || basic.getElHour() > 23)
		{
			msgs.add(new ClientMsg("validation", "basic", "expectedLoadHour", "Please enter a valid hour value (0 - 23)"));
		}
		if(basic.getElMin() < 0 || basic.getElMin() > 59)
		{
			msgs.add(new ClientMsg("validation", "basic", "expectedLoadHour", "Please enter a valid minute value (0 - 59)"));
		}
		if(basic.getElSec() < 0 || basic.getElSec() > 59)
		{
			msgs.add(new ClientMsg("validation", "basic", "expectedLoadHour", "Please enter a valid second value (0 - 59)"));
		}
		
		//Actual Time
		if(basic.getAlHour() < 0 || basic.getAlHour() > 23)
		{
			msgs.add(new ClientMsg("validation", "basic", "actualLoadHour", "Please enter a valid hour value (0 - 23)"));
		}
		if(basic.getAlMin() < 0 || basic.getAlMin() > 59)
		{
			msgs.add(new ClientMsg("validation", "basic", "actualLoadHour", "Please enter a valid minute value (0 - 59)"));
		}
		if(basic.getAlSec() < 0 || basic.getAlSec() > 59)
		{
			msgs.add(new ClientMsg("validation", "basic", "actualLoadHour", "Please enter a valid second value (0 - 59)"));
		}
		
		if(mem.getName("destination_types", basic.getDestination().getId()).equals("Production") &&
			(basic.getSummary() == null || basic.getSummary().isEmpty()))
		{
			msgs.add(new ClientMsg("validation", "basic", "summary", "Please enter a summary of the MN"));
		}
		
		if(basic.getArtifact() != null && !basic.getArtifact().trim().isEmpty())
		{
			if(!basic.getArtifact().matches("^(([aA][rR][tT][fF]|[iI][tT][gG]|[iI][oO][mM]|[rR][tT]|[qQ][cC])\\d+(\\s)*(,|;|\\|)?(\\s)*)*$"))
			{
				msgs.add(new ClientMsg("validation", "basic", "artifact", "Please enter valid reference IDs (artf, qc, iom, rt, itg) separated by any delimiter (, or _ or |)"));
			}
		}
		
		return msgs;
	}
	
	public List<ClientMsg> validateBuild(BuildDetailModel build, BasicDetailModel basic)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();		 		
		
		String status = mem.getName("status_types", basic.getStatus().getId());	
		
		if(status.equals("Open") || status.equals("Cancelled"))
			return msgs;
		
		if(!status.equals("Pending Approval") && !status.equals("Ready for SQA") && !status.equals("Ready for IEAT") &&
			(build.getTrackerNum() == null || build.getTrackerNum().isEmpty()))
			msgs.add(new ClientMsg("validation", "build", "trackerNum", "Must enter a valid Build Tracker Number"));
		if(build.getOs().getId() < 0)
			msgs.add(new ClientMsg("validation", "build", "os.id", "Must specify the Operating System"));
		if(build.getBuildProject().getId() < 0)
			msgs.add(new ClientMsg("validation", "build", "buildProject.id", "Must specify the Build Project"));
		if(build.getScm().getId() < 0)
			msgs.add(new ClientMsg("validation", "build", "scm.id", "Must specify the SCM Tool"));
				
		return msgs;
	}
	
	public List<ClientMsg> validateTest(TestDetailModel test, BasicDetailModel basic)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		String status = mem.getName("status_types", basic.getStatus().getId());	
		
		if(status.equals("Open") || status.equals("Cancelled"))
			return msgs;
		
		if(test.getTestType().getId() > 0 && mem.getProperties("test_types").get(Util.getIndex(mem.getProperties("test_types"), "Not Testable")).getId() == test.getTestType().getId() &&
			(test.getTestableJustification() == null || test.getTestableJustification().isEmpty()))
		{
			msgs.add(new ClientMsg("validation", "test", "testType", "Must input a justification for a Not Testable type!"));
		}
		if(basic.getStatus().getId() > 0 && mem.getName("status_types", basic.getStatus().getId()).equals("Ready for SQA Test") &&
			basic.getDestination().getId() > 0 && mem.getName("destination_types", basic.getDestination().getId()).equals("Production") && 
			basic.getChangeType().getId() > 0 && mem.getName("change_types", basic.getId()).equals("Software") && 
			(test.getTestData() == 2 && (test.getTestDataText() == null || test.getTestDataText().isEmpty()) ||
			(test.getExpectedResults() == null || test.getExpectedResults().isEmpty()) ||
			(test.getSteps() == null || test.getSteps().isEmpty())))
		{
			msgs.add(new ClientMsg("validation", "test", "", "Please complete the Test Data, Expected Results and Step by Step Procedures fields"));
		}
		
		return msgs;
	}
	
	public List<ClientMsg> validateDeploy(DeploymentDetailModel deploy)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		
		return msgs;		
	}
	
	public boolean isTestEnabled(int dest, int load)
	{
		String destName = mem.getName("destination_types", dest);
		String loadName = mem.getName("load_types", load);
		
		
		if(destName.equals("L2 Only") || destName.equals("L3 Only") || destName.equals("L4 Only") || 
			(loadName.equals("Emergency") && destName.equals("Production")))
			return false;
		
		return true;
	}
	
	public boolean isBuildEnabled(int status, int change)
	{
		String changeName = mem.getName("change_types", change);
		String statusName = mem.getName("status_types", status);
			
		if(changeName.equals("Software") || changeName.equals("Software and Data") ||
			statusName.equals("Ready for SQA") || statusName.equals("Ready for IEAT"))
			return true;							
		else
			return false;
	}
	
	public boolean shouldValidateTest(int dest, int load, int status)
	{
		String destName = mem.getName("destination_types", dest);
		String loadName = mem.getName("load_types", load);
		
		
		if(destName.equals("L2 Only") || destName.equals("L3 Only") || destName.equals("L4 Only") || 
			(loadName.equals("Emergency") && destName.equals("Production")))
			return false;
		
		return true;
	}
	
	public boolean shouldValidateBuild(int change)
	{
		String changeName = mem.getName("change_types", change);
			
		if(changeName.equals("Software") || changeName.equals("Software and Data"))
			return true;							
		else
			return false;
	}
	
	public int getStatusLevel(MNModel mn, List<ClientMsg> approvals)
	{		
		if(!mem.getName("destination_types", mn.getBasicDetail().getDestination().getId()).equals("Production"))
		{
			return -1;
		}
		
		return 0;
	}
}
