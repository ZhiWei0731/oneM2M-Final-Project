package org.eclipse.om2m.sample.project_ipe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.interworking.service.InterworkingService;
import org.eclipse.om2m.sample.project_ipe.constants.project_ipeConstants;
import org.eclipse.om2m.sample.project_ipe.controller.project_ipeController;
import org.eclipse.om2m.sample.project_ipe.controller.project_ipeFunc;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	/** Logger */
	private static Log logger = LogFactory.getLog(Activator.class);
	/** SCL service tracker */
	private ServiceTracker<Object, Object> cseServiceTracker;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		logger.info("Register IpeService..");
		bundleContext.registerService(InterworkingService.class.getName(), new project_ipeRouter(), null);
		logger.info("IpeService is registered.");
		cseServiceTracker = new ServiceTracker<Object, Object>(bundleContext, CseService.class.getName(), null) {
			public void removedService(ServiceReference<Object> reference, Object service) {
				logger.info("CseService removed");
			}

			public Object addingService(ServiceReference<Object> reference) {
				logger.info("CseService discovered");
				CseService cseService = (CseService) this.context.getService(reference);
				project_ipeController.setCse(cseService);
				// create Resource in mn-cse
				project_ipeFunc.createResources(project_ipeConstants.AE_NAME, project_ipeConstants.POA);
				//GUI.init();
				return cseService;
			}
		};
		cseServiceTracker.open();
	}
	
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		try {
			logger.info("Stop test_ipe");
			// do something
			//GUI.stop();
		} catch (Exception e) {
			logger.error("Stop test_ipe error", e);
		}
	}

}