package org.eclipse.om2m.sample.project_ipe.controller;

import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.io.ObixEncoder;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.sample.project_ipe.RequestSender;
import org.eclipse.om2m.sample.project_ipe.constants.project_ipeConstants;


public class project_ipeController {
	public static CseService CSE;
	
	public static void createDATA(String appID, String category, String data, String unit) {
		// notify GUI
		//notifyObserver(test_ipeController.getAirConState());
		// Send the information to the CSE
		// String targetID = project_ipeConstants.CSE_PREFIX + "/" + project_ipeConstants.AE_NAME + "/" + project_ipeConstants.DATA;
		String targetID = project_ipeConstants.CSE_PREFIX + "/" + appID + "/" + project_ipeConstants.DATA;
		System.out.println(targetID);
		ContentInstance cin = new ContentInstance();
		Obj obj = new Obj();
		obj.add(new Str("appID", appID));
		obj.add(new Str("category", category));
		obj.add(new Str("data", data));
		obj.add(new Str("unit", unit));
		//obj.add(new Str("appID", test_ipeModel.getAirConValue()));
		//obj.add(new Str("category", String.valueOf(test_ipeModel.getAirConTemp())));
		//obj.add(new Str("data", String.valueOf(test_ipeModel.getAirConFan())));
		//obj.add(new Str("unit", String.valueOf(test_ipeModel.getAirConFan())));
		cin.setContent(ObixEncoder.toString(obj));
		cin.setContentInfo(MimeMediaType.OBIX + ":" + MimeMediaType.ENCOD_PLAIN);
		RequestSender.createContentInstance(targetID, cin);
	}
	
	public static void setCse(CseService cse) {
		CSE = cse;
	}
}