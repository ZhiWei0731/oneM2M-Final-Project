package org.eclipse.om2m.sample.project_ipe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.exceptions.BadRequestException;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.interworking.service.InterworkingService;
import org.eclipse.om2m.sample.project_ipe.constants.project_ipeConstants;
import org.eclipse.om2m.sample.project_ipe.controller.project_ipeController;


public class project_ipeRouter implements InterworkingService {
	private static Log LOGGER = LogFactory.getLog(project_ipeRouter.class);
	
	@Override
	public ResponsePrimitive doExecute(RequestPrimitive request) {
		// handle the user command
		ResponsePrimitive response = new ResponsePrimitive(request);
		if (request.getQueryStrings().containsKey("appID") && request.getQueryStrings().containsKey("category")) {
			String appID = request.getQueryStrings().get("appID").get(0);
			String category = request.getQueryStrings().get("category").get(0);
			String data = request.getQueryStrings().get("data").get(0);
			String unit = request.getQueryStrings().get("unit").get(0);
			LOGGER.info("Received request in project_ipe: appID=" + appID + " ; category=" + category + " ; data=" + data + " ; unit=" + unit);
			switch(category) {
				// can be more complicated
				case "Temperature":
					project_ipeController.createDATA(appID, category, data, unit);
					System.out.println(appID + category + data + unit);
					response.setResponseStatusCode(ResponseStatusCode.OK);
				case "Humidity":
					project_ipeController.createDATA(appID, category, data, unit);
					System.out.println(appID + category + data + unit);
					response.setResponseStatusCode(ResponseStatusCode.OK);
				default:
					throw new BadRequestException();
			}
		}
		if (response.getResponseStatusCode() == null) {
			response.setResponseStatusCode(ResponseStatusCode.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public String getAPOCPath() {
		return project_ipeConstants.POA;
	}
}
