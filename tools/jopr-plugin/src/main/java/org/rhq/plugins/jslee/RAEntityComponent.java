package org.rhq.plugins.jslee;

import java.util.Date;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.slee.management.ResourceAdaptorEntityState;
import javax.slee.management.ResourceManagementMBean;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.ResourceAdaptorID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.slee.container.management.jmx.ActivityManagementMBeanImplMBean;
import org.mobicents.slee.runtime.activity.ActivityContextHandle;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.resource.CreateResourceStatus;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.CreateChildResourceFacet;
import org.rhq.core.pluginapi.inventory.CreateResourceReport;
import org.rhq.core.pluginapi.inventory.DeleteResourceFacet;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;
import org.rhq.plugins.jslee.utils.JainSleeServerUtils;
import org.rhq.plugins.jslee.utils.MBeanServerUtils;
import org.rhq.plugins.jslee.utils.ResourceAdaptorUtils;

public class RAEntityComponent implements ResourceAdaptorUtils, ConfigurationFacet, DeleteResourceFacet,
		CreateChildResourceFacet, OperationFacet, MeasurementFacet {
	private final Log log = LogFactory.getLog(this.getClass());

	private ResourceContext<ResourceAdaptorComponent> resourceContext;
	private String raEntityName;
	private ResourceAdaptorID raId = null;
	private MBeanServerUtils mbeanUtils = null;

	private ConfigProperties configProperties = null;

	private ObjectName resourceManagement;

	public void start(ResourceContext context) throws InvalidPluginConfigurationException, Exception {
		log.info("RAEntityComponent.start");

		this.resourceContext = context;
		this.resourceManagement = new ObjectName(ResourceManagementMBean.OBJECT_NAME);

		this.mbeanUtils = ((JainSleeServerUtils) context.getParentResourceComponent()).getMBeanServerUtils();

		String name = this.resourceContext.getPluginConfiguration().getSimple("name").getStringValue();
		String version = this.resourceContext.getPluginConfiguration().getSimple("version").getStringValue();
		String vendor = this.resourceContext.getPluginConfiguration().getSimple("vendor").getStringValue();

		raEntityName = this.resourceContext.getPluginConfiguration().getSimple("entityName").getStringValue();

		this.raId = new ResourceAdaptorID(name, vendor, version);
	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public AvailabilityType getAvailability() {
		log.info("RAEntityComponent.getAvailability");
		try {
			MBeanServerConnection connection = this.mbeanUtils.getConnection();
			ResourceManagementMBean resourceManagementMBean = (ResourceManagementMBean) MBeanServerInvocationHandler
					.newProxyInstance(connection, this.resourceManagement,
							javax.slee.management.ResourceManagementMBean.class, false);

			configProperties = resourceManagementMBean.getConfigurationProperties(this.raEntityName);

		} catch (Exception e) {
			log.error("getAvailability failed for ResourceAdaptor Entity = " + this.raEntityName);

			return AvailabilityType.DOWN;
		}

		return AvailabilityType.UP;
	}

	public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) throws Exception {
		log.info("RAEntityComponent.getValues() called hurray");
		for (MeasurementScheduleRequest request : metrics) {
			if (request.getName().equals("state")) {
				report.addData(new MeasurementDataTrait(request, this.getState().toString()));
			}
			if (request.getName().equals("activites")) {
				Object[] activities = getActivityContextID();
				report.addData(new MeasurementDataNumeric(request, activities == null ? 0.0
						: (double) activities.length));
			}
		}
	}

	private ResourceAdaptorEntityState getState() throws Exception {
		MBeanServerConnection connection = this.mbeanUtils.getConnection();
		ResourceManagementMBean resourceManagementMBean = (ResourceManagementMBean) MBeanServerInvocationHandler
				.newProxyInstance(connection, this.resourceManagement,
						javax.slee.management.ResourceManagementMBean.class, false);
		return resourceManagementMBean.getState(this.raEntityName);

	}

	private Object[] getActivityContextID() throws Exception {

		ObjectName actMana = new ObjectName("org.mobicents.slee:name=ActivityManagementMBean");
		MBeanServerConnection connection = this.mbeanUtils.getConnection();
		ActivityManagementMBeanImplMBean aciManagMBean = (ActivityManagementMBeanImplMBean) MBeanServerInvocationHandler
				.newProxyInstance(connection, actMana,
						org.mobicents.slee.container.management.jmx.ActivityManagementMBeanImplMBean.class, false);

		Object[] activities = aciManagMBean.retrieveActivityContextIDByResourceAdaptorEntityName(this.raEntityName);

		return activities;

	}

	public Configuration loadResourceConfiguration() throws Exception {
		Configuration config = new Configuration();

		config.put(new PropertySimple("entityName", this.raEntityName));

		PropertyList columnList = new PropertyList("properties");
		for (ConfigProperties.Property confProp : this.configProperties.getProperties()) {
			PropertyMap col = new PropertyMap("propertyDefinition");

			col.put(new PropertySimple("propertyName", confProp.getName()));
			col.put(new PropertySimple("propertyType", confProp.getType()));
			col.put(new PropertySimple("propertyValue", confProp.getValue()));

			columnList.add(col);
		}

		config.put(columnList);

		return config;
	}

	public void updateResourceConfiguration(ConfigurationUpdateReport arg0) {
		// No update is allowed isnt it?

	}

	public void deleteResource() throws Exception {
		MBeanServerConnection connection = this.mbeanUtils.getConnection();
		ResourceManagementMBean resourceManagementMBean = (ResourceManagementMBean) MBeanServerInvocationHandler
				.newProxyInstance(connection, this.resourceManagement,
						javax.slee.management.ResourceManagementMBean.class, false);

		resourceManagementMBean.removeResourceAdaptorEntity(this.raEntityName);

	}

	public ResourceAdaptorID getResourceAdaptorID() {
		return this.raId;
	}

	public MBeanServerUtils getMBeanServerUtils() {
		return this.mbeanUtils;
	}

	public String getRAEntityName() {
		return this.raEntityName;
	}

	public CreateResourceReport createResource(CreateResourceReport report) {
		try {
			Configuration configuration = report.getResourceConfiguration();

			String linkName = configuration.getSimple("linkName").getStringValue();

			MBeanServerConnection connection = this.mbeanUtils.getConnection();
			ResourceManagementMBean resourceManagementMBean = (ResourceManagementMBean) MBeanServerInvocationHandler
					.newProxyInstance(connection, this.resourceManagement,
							javax.slee.management.ResourceManagementMBean.class, false);

			report.setResourceKey(linkName);
			resourceManagementMBean.bindLinkName(this.raEntityName, linkName);

			report.setStatus(CreateResourceStatus.SUCCESS);
			report.setResourceName(linkName);
		} catch (Exception e) {
			log.error("Adding new ResourceAdaptor Entity failed ", e);
			report.setException(e);
			report.setStatus(CreateResourceStatus.FAILURE);

		}
		return report;

	}

	public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException,
			Exception {
		log.info("RAEntityComponent.invokeOperation() with name = " + name);

		OperationResult result = new OperationResult();
		if ("changeRaEntityState".equals(name)) {
			String message = null;
			String action = parameters.getSimple("action").getStringValue();
			MBeanServerConnection connection = this.mbeanUtils.getConnection();
			ResourceManagementMBean resourceManagementMBean = (ResourceManagementMBean) MBeanServerInvocationHandler
					.newProxyInstance(connection, this.resourceManagement,
							javax.slee.management.ResourceManagementMBean.class, false);
			if ("activate".equals(action)) {
				resourceManagementMBean.activateResourceAdaptorEntity(this.raEntityName);
				message = "Successfully Activated Resource Adaptor Entity " + this.raEntityName;
			} else if ("deactivate".equals(action)) {
				resourceManagementMBean.deactivateResourceAdaptorEntity(this.raEntityName);
				message = "Successfully DeActivated Resource Adaptor Entity " + this.raEntityName;
			}

			result.getComplexResults().put(new PropertySimple("result", message));

		} else if ("listActivityContexts".equals(name)) {
			MBeanServerConnection connection = this.mbeanUtils.getConnection();
			ObjectName actMana = new ObjectName("org.mobicents.slee:name=ActivityManagementMBean");

			ActivityManagementMBeanImplMBean aciManagMBean = (ActivityManagementMBeanImplMBean) MBeanServerInvocationHandler
					.newProxyInstance(connection, actMana,
							org.mobicents.slee.container.management.jmx.ActivityManagementMBeanImplMBean.class, false);

			Object[] activities = aciManagMBean.retrieveActivityContextIDByResourceAdaptorEntityName(this.raEntityName);
			// Object[] activities = aciManagMBean.listActivityContexts(true);

			PropertyList columnList = new PropertyList("result");
			if (activities != null) {
				for (Object obj : activities) {
					Object[] tempObjects = (Object[]) obj;
					PropertyMap col = new PropertyMap("element");

					Object tempObj = tempObjects[0];
					PropertySimple activityHandle = new PropertySimple("ActivityHandle",
							tempObj != null ? ((ActivityContextHandle) tempObj).getActivityHandle().toString() : "-");

					col.put(activityHandle);

					col.put(new PropertySimple("Class", tempObjects[1]));

					tempObj = tempObjects[2];
					Date d = new Date(Long.parseLong((String) tempObj));
					col.put(new PropertySimple("LastAccessTime", d));

					tempObj = tempObjects[3];
					col.put(new PropertySimple("ResourceAdaptor", tempObj == null ? "-" : tempObj));

					tempObj = tempObjects[4];
					// PropertyList propertyList = new PropertyList("SbbAttachments");
					String[] strArr = (String[]) tempObj;
					StringBuffer sb = new StringBuffer();
					for (String s : strArr) {
						// PropertyMap SbbAttachment = new PropertyMap("SbbAttachment");
						// SbbAttachment.put(new PropertySimple("SbbAttachmentValue", s));
						// propertyList.add(SbbAttachment);
						sb.append(s).append("<br/>");
					}
					col.put(new PropertySimple("SbbAttachmentValue", sb.toString()));

					tempObj = tempObjects[5];
					// propertyList = new PropertyList("NamesBoundTo");
					sb = new StringBuffer();
					strArr = (String[]) tempObj;
					for (String s : strArr) {
						// PropertyMap NameBoundTo = new PropertyMap("NameBoundTo");
						// NameBoundTo.put(new PropertySimple("NameBoundToValue", s));
						// propertyList.add(NameBoundTo);
						sb.append(s).append("<br/>");
					}
					col.put(new PropertySimple("NameBoundToValue", sb.toString()));

					tempObj = tempObjects[6];
					// propertyList = new PropertyList("Timers");
					sb = new StringBuffer();
					strArr = (String[]) tempObj;
					for (String s : strArr) {
						// PropertyMap Timer = new PropertyMap("Timer");
						// Timer.put(new PropertySimple("TimerValue", s));
						// propertyList.add(Timer);
						sb.append(s).append("<br/>");
					}
					col.put(new PropertySimple("TimerValue", sb.toString()));

					tempObj = tempObjects[7];
					// propertyList = new PropertyList("DataProperties");
					sb = new StringBuffer();
					strArr = (String[]) tempObj;
					for (String s : strArr) {
						// PropertyMap DataProperty = new PropertyMap("DataProperty");
						// DataProperty.put(new PropertySimple("DataPropertyValue", s));
						// propertyList.add(DataProperty);
						sb.append(s).append("<br/>");
					}
					col.put(new PropertySimple("DataPropertyValue", sb.toString()));

					columnList.add(col);

				}
			}

			result.getComplexResults().put(columnList);
		} else {
			throw new UnsupportedOperationException("Operation [" + name + "] is not supported yet.");
		}

		return result;
	}
}
