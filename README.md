# oneM2M Final Project Demo
[原始 README 連結](https://hackmd.io/@ZhiWei0731/oneM2M-Final-Project)
[GitHub 連結](https://github.com/ZhiWei0731/oneM2M-Final-Project) 

## 環境版本
* Ubuntu 18.04.2 LTS
* Eclipse 2019-12(4.14.0)
* Java JDK 1.8.0_275
* Maven 3.6.0
* OM2M 1.4 ([commit](https://git.eclipse.org/c/om2m/org.eclipse.om2m.git/commit/?id=9cd538e7a2a6e3633983b605c32a1d5507294f2f))
* Python 3.6.9
    * request 2.25.0

## 步驟
1. 在 bash 先啟動 in-cse， 再啟動 mn-cse。
2. 在 mn-cse 的 bash 裡面，start 47，手動啟動 project_ipe 這個 semantic，此時 MN 上也會多了 Sensor 這個 AE，以及底下的 DESCRIPTION。
3. 在 bash 啟動 node-red，避免 MN 上的 AE (例如Temp_Sensor1) 建立訂閱失敗。
4. 在 bash 啟動 jupyter notebook，依序執行 create_AE.ipynb 裡面的 block cell，目的是依序建立 IN 上的 AE 和其 DATA container，然後建立 MN 上的 AE 和其 STATE 跟 DATA container。
5. 開啟 node-red 的 dashboard，輸入 http://127.0.0.1:1880/ui (要記得先安裝 dashboard 插件)
6. 點擊開啟或關閉不同感測器，即可看到右邊感測器數值的變化。

:::info
安裝 node-red dashboard
node-red 頁面右上方 -> Manage palette -> Palette -> install -> 搜尋 dashboard -> 找到 node-red-dashboard 並安裝
:::


## 架構圖
![Project_Flow](https://hackmd.io/_uploads/SJ5_ueaP6.png)
1. 使用者在 node-red 的 dashboard 點選開關，決定是否要獲取資料。
2. 在 mn 對應的 sensor AE 的 STATE container 下，將開關資訊以 content instance 的形式儲存。
3. node-red 根據 content instance 的資訊，去決定要不要獲取最新資料。
4. 若要獲取資料，則每隔一秒發出 HTTP GET request。
5. node-red 將最新的資料以 HTTP POST request 的形式傳給 mn 上的 Sensor(AE)，request 的形式如下面。
```
http://127.0.0.1:8282/~/mn-cse/mn-name/Sensor?appID=Temp_Sensor1&category=Temperature&data=27.25&unit=Celsius
```
6. Sensor 透過 poa 將 POST request 轉給 IPE 處理。
7. IPE 處理封包後，在 mn 下建立對應的 content instance。
8. 當 mn 的 DATA 底下有新建資料，透過 sub 通知 IN。
9. 將通知轉交給 node-red 處理。
10. 在 IN 下建立對應的 content instance，並呈現在 dashboard 上。




## dashboard
![dashboard](https://hackmd.io/_uploads/Bygza5gfwT.png)

## oneM2M platform
![oneM2M_MN](https://hackmd.io/_uploads/BkAs9ezvp.png)
![oneM2M_IN](https://hackmd.io/_uploads/SJCi9lfPp.png)

## node-red 
Flow 請看 pic 目錄
對應的 JSON 檔在 node_red 資料夾

:::spoiler node-red 流程截圖
* node-red_NB-IoT
![node-red_NB-IoT](https://hackmd.io/_uploads/rkN85xfDa.png)
* node-red_MN(1)
![node-red_MN(1)](https://hackmd.io/_uploads/ryfPqeMvp.png)
* node-red_MN(2)
![node-red_MN(2)](https://hackmd.io/_uploads/rJGwqxMv6.png)
* node-red_IN(1)
![node-red_IN(1)](https://hackmd.io/_uploads/SyKD9gzwa.png)
* node-red_IN(2)
![node-red_IN(2)](https://hackmd.io/_uploads/SkYv9eGv6.png)
:::

## python 建 AE
程式碼在 jupyter_notebook 資料夾下的 create_AE.ipynb

:::spoiler click to show python code
```python=
import requests, base64, time
OM2M_URL = "http://127.0.0.1:8080/~"
CSE_ID = "/in-cse/"
CSE_NAME = "in-name"
LOGIN="admin"
PSWD="admin"
OM2M_BASE = OM2M_URL+CSE_ID
auth_headers = {"X-M2M-ORIGIN":LOGIN+":"+PSWD}
# The other accepted value is application/xml
common_headers = {"Accept": "application/json"}
    
def create_AE(name, poa):
    header_ae = {"Content-Type":"application/xml;ty=2"}
    name_ae = name
    body_ae = """
    <m2m:ae xmlns:m2m="http://www.onem2m.org/xml/protocols" rn="{0}">
        <api>{0}</api>
        <lbl>Location/Cloud_Platform</lbl>
        <poa>{1}</poa>
        <rr>True</rr>
    </m2m:ae>
    """.format(name_ae, poa)
    response = requests.delete(OM2M_BASE+CSE_NAME+"/"+name_ae, headers={**auth_headers, **common_headers})
    print(response)
    response = requests.post(OM2M_BASE, data=body_ae, headers={**auth_headers, **common_headers, **header_ae})
    print(response)
    
def create_CNT(name_ae, name_cnt):
    header_cnt = {"Content-Type":"application/xml;ty=3"}
    body_cnt = """<m2m:cnt xmlns:m2m="http://www.onem2m.org/xml/protocols" rn="{0}"></m2m:cnt>""".format(name_cnt)
    print(body_cnt)
    response = requests.post(OM2M_BASE+CSE_NAME+"/"+name_ae, data=body_cnt, headers={**auth_headers, **common_headers, **header_cnt})
    print(response)

# def create_CIN(name_ae, name_cnt, mode, cin):
#     header_cin = {"Content-Type":"application/xml;ty=4"}
#     body_cin = """<m2m:cin xmlns:m2m="http://www.onem2m.org/xml/protocols"><cnf>{0}</cnf><con>{1}</con></m2m:cin>""".format(mode, base64.b64encode(cin).decode("utf-8"))
#     print(body_cin)
#     response = requests.post(OM2M_BASE+CSE_NAME+"/"+name_ae+"/"+name_cnt, data=body_cin, headers={**auth_headers, **common_headers, **header_cin})
#     print(response)
```

```python=
# create AE and container in IN
create_AE("Temp_Sensor1", "http://127.0.0.1:1880/temp_sensor1_data")
time.sleep(2)
create_CNT("Temp_Sensor1", "DATA")
time.sleep(1)

create_AE("Temp_Sensor2", "http://127.0.0.1:1880/temp_sensor2_data")
time.sleep(2)
create_CNT("Temp_Sensor2", "DATA")
time.sleep(1)

create_AE("Humi_Sensor1", "http://127.0.0.1:1880/humi_sensor1_data")
time.sleep(2)
create_CNT("Humi_Sensor1", "DATA")
time.sleep(1)

create_AE("Humi_Sensor2", "http://127.0.0.1:1880/humi_sensor2_data")
time.sleep(2)
create_CNT("Humi_Sensor2", "DATA")
time.sleep(1)
```

```python=
import requests, base64, time
OM2M_URL = "http://127.0.0.1:8282/~"
CSE_ID = "/mn-cse/"
CSE_NAME = "mn-name"
LOGIN="admin"
PSWD="admin"
OM2M_BASE = OM2M_URL+CSE_ID
auth_headers = {"X-M2M-ORIGIN":LOGIN+":"+PSWD}
# The other accepted value is application/xml
common_headers = {"Accept": "application/json"}
    
def create_AE(name):
    header_ae = {"Content-Type":"application/xml;ty=2"}
    name_ae = name
    body_ae = """
    <m2m:ae xmlns:m2m="http://www.onem2m.org/xml/protocols" rn="{0}">
        <api>{0}</api>
        <lbl>Location/Device</lbl>
        <rr>True</rr>
    </m2m:ae>
    """.format(name_ae)
    response = requests.delete(OM2M_BASE+CSE_NAME+"/"+name_ae, headers={**auth_headers, **common_headers})
    print(response)
    response = requests.post(OM2M_BASE, data=body_ae, headers={**auth_headers, **common_headers, **header_ae})
    print(response)
    
def create_CNT(name_ae, name_cnt):
    header_cnt = {"Content-Type":"application/xml;ty=3"}
    body_cnt = """<m2m:cnt xmlns:m2m="http://www.onem2m.org/xml/protocols" rn="{0}"></m2m:cnt>""".format(name_cnt)
    print(body_cnt)
    response = requests.post(OM2M_BASE+CSE_NAME+"/"+name_ae, data=body_cnt, headers={**auth_headers, **common_headers, **header_cnt})
    print(response)

# def create_CIN(name_ae, name_cnt, mode, cin):
#     header_cin = {"Content-Type":"application/xml;ty=4"}
#     body_cin = """<m2m:cin xmlns:m2m="http://www.onem2m.org/xml/protocols"><cnf>{0}</cnf><con>{1}</con></m2m:cin>""".format(mode, base64.b64encode(cin).decode("utf-8"))
#     print(body_cin)
#     response = requests.post(OM2M_BASE+CSE_NAME+"/"+name_ae+"/"+name_cnt, data=body_cin, headers={**auth_headers, **common_headers, **header_cin})
#     print(response)

def create_SUB(name_ae, name_cnt, name_sub, nu):
    header_sub = {"Content-Type":"application/xml;ty=23"}
    body_sub = """
    <m2m:sub xmlns:m2m="http://www.onem2m.org/xml/protocols" rn="{0}">
        <nu>{1}</nu>
        <nct>2</nct>
    </m2m:sub>
    """.format(name_sub, nu)
    print(body_sub)
    response = requests.post(OM2M_BASE+CSE_NAME+"/"+name_ae+"/"+name_cnt, data=body_sub, headers={**auth_headers, **common_headers, **header_sub})
    print(response)
```


```python=    
# create AE, container, SUB in MN
create_AE("Temp_Sensor1")
time.sleep(2)
create_CNT("Temp_Sensor1", "STATE")
time.sleep(1)
create_CNT("Temp_Sensor1", "DATA")
time.sleep(1)
create_SUB("Temp_Sensor1", "STATE", "SUBSCRIPTION", "http://127.0.0.1:1880/temp_sensor1_state")
time.sleep(1)
create_SUB("Temp_Sensor1", "DATA", "SUBSCRIPTION", "http://127.0.0.1:8080/~/in-cse/in-name/Temp_Sensor1")
time.sleep(1)

create_AE("Temp_Sensor2")
time.sleep(2)
create_CNT("Temp_Sensor2", "STATE")
time.sleep(1)
create_CNT("Temp_Sensor2", "DATA")
time.sleep(1)
create_SUB("Temp_Sensor2", "STATE", "SUBSCRIPTION", "http://127.0.0.1:1880/temp_sensor2_state")
time.sleep(1)
create_SUB("Temp_Sensor2", "DATA", "SUBSCRIPTION", "http://127.0.0.1:8080/~/in-cse/in-name/Temp_Sensor2")
time.sleep(1)


create_AE("Humi_Sensor1")
time.sleep(2)
create_CNT("Humi_Sensor1", "STATE")
time.sleep(1)
create_CNT("Humi_Sensor1", "DATA")
time.sleep(1)
create_SUB("Humi_Sensor1", "STATE", "SUBSCRIPTION", "http://127.0.0.1:1880/humi_sensor1_state")
time.sleep(1)
create_SUB("Humi_Sensor1", "DATA", "SUBSCRIPTION", "http://127.0.0.1:8080/~/in-cse/in-name/Humi_Sensor1")
time.sleep(1)


create_AE("Humi_Sensor2")
time.sleep(2)
create_CNT("Humi_Sensor2", "STATE")
time.sleep(1)
create_CNT("Humi_Sensor2", "DATA")
time.sleep(1)
create_SUB("Humi_Sensor2", "STATE", "SUBSCRIPTION", "http://127.0.0.1:1880/humi_sensor2_state")
time.sleep(1)
create_SUB("Humi_Sensor2", "DATA", "SUBSCRIPTION", "http://127.0.0.1:8080/~/in-cse/in-name/Humi_Sensor2")
time.sleep(1)
```
:::

## project_ipe
### 程式碼目錄
![螢幕擷取畫面 2023-12-21 022600](https://hackmd.io/_uploads/Hk0ztgMDa.png)

### Activate.java
:::spoiler Click to show Activate.java
```java=
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

```
:::


### project_ipeRouter.java
:::spoiler Click to show project_ipeRouter.java
```java=
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
```
:::



### RequsetSender.java
:::spoiler Click to show RequsetSender.java
```java=
package org.eclipse.om2m.sample.project_ipe;

import java.math.BigInteger;

import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.Operation;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.Resource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.sample.project_ipe.controller.project_ipeController;

public class RequestSender {
	
	/**
	 * Private constructor to avoid creation of this object
	 */
	private RequestSender() {
	}

	public static ResponsePrimitive createResource(String targetId, Resource resource, int resourceType) {
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTo(targetId);
		request.setResourceType(BigInteger.valueOf(resourceType));
		request.setRequestContentType(MimeMediaType.OBJ);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setContent(resource);
		request.setOperation(Operation.CREATE);
		return project_ipeController.CSE.doRequest(request);
	}

	public static ResponsePrimitive createAE(AE resource) {
		return createResource("/" + Constants.CSE_ID, resource, ResourceType.AE);
	}

	public static ResponsePrimitive createContainer(String targetId, Container resource) {
		return createResource(targetId, resource, ResourceType.CONTAINER);
	}

	public static ResponsePrimitive createContentInstance(String targetId, ContentInstance resource) {
		return createResource(targetId, resource, ResourceType.CONTENT_INSTANCE);
	}

	public static ResponsePrimitive getRequest(String targetId) {
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTo(targetId);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setOperation(Operation.RETRIEVE);
		request.setRequestContentType(MimeMediaType.OBJ);
		return project_ipeController.CSE.doRequest(request);
	}
}
```
:::


### project_ipeConstants.java
:::spoiler Click to show project_ipeConstants.java
```java=
package org.eclipse.om2m.sample.project_ipe.constants;

import org.eclipse.om2m.commons.constants.Constants;

public class project_ipeConstants {
	
	private project_ipeConstants() {
	}

	public static final String POA = "project_ipe";
	public static final String DATA = "DATA";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String AE_NAME = "Sensor";
	//public static final String QUERY_STRING_OP = "op";

	public static String CSE_ID = "/" + Constants.CSE_ID;
	public static String CSE_PREFIX = CSE_ID + "/" + Constants.CSE_NAME;
}
```
:::



### project_ipeController.java
:::spoiler Click to show project_ipeController.java
```java=
package org.eclipse.om2m.sample.project_ipe.controller;

import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.obix.Bool;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.io.ObixEncoder;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.sample.project_ipe.RequestSender;
import org.eclipse.om2m.sample.project_ipe.constants.project_ipeConstants;


public class project_ipeController {
	public static CseService CSE;
	// protected static String AE_ID;
	
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
```
:::


### project_ipeFunc.java
:::spoiler Click to show project_ipeFunc.java
```java=
package org.eclipse.om2m.sample.project_ipe.controller;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.sample.project_ipe.RequestSender;
import org.eclipse.om2m.sample.project_ipe.constants.project_ipeConstants;
import org.eclipse.om2m.sample.project_ipe.controller.project_ipeFunc;

public class project_ipeFunc {
	private static Log LOGGER = LogFactory.getLog(project_ipeFunc.class);

	public static void createResources(String appId, String poa) {
		// Create the Application resource
		Container container = new Container();
		container.getLabels().add("sensor");
		container.setMaxNrOfInstances(BigInteger.valueOf(0));
		
		AE ae = new AE();
		ae.setRequestReachability(true);
		ae.getPointOfAccess().add(poa);
		ae.setAppID(appId);
		ae.setName(appId);

		ResponsePrimitive response = RequestSender.createAE(ae);
		// Create Application sub-resources only if application not yet created
		if(response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			container = new Container();
			container.setMaxNrOfInstances(BigInteger.valueOf(10));
			// Create STATE container sub-resource
			container.setName(project_ipeConstants.DESCRIPTION);
			LOGGER.info(RequestSender.createContainer(response.getLocation(), container));
		}
	}
}
```
:::



## Attemp but not work
### 查看 cmd
[Linux查看及刪除執行中的進程(PID)](https://codes.bobi.tw/archives/60)
查看不是root執行的相關進程：
* ps -U root -u root -N
刪除執行中進程可以用kill 然後加上PID數字即可，如果要刪除進程名稱，則使用killall加上程式名稱。
* kill 10000


### 對背景console下命令
echo "ss \n" > /proc/$(pgrep -f "start_mn.sh")/fd/0

echo "start 47\n" > /proc/$(pgrep -f "start_mn.sh")/fd/0

echo $(pgrep -f "start_mn.sh")

