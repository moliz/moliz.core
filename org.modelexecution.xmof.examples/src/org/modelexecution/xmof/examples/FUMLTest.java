package org.modelexecution.xmof.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution;
import org.modelexecution.fumldebug.core.trace.tracemodel.Trace;
import org.modelexecution.fumldebug.core.trace.tracemodel.ValueInstance;
import org.modelexecution.xmof.configuration.ConfigurationObjectMap;
import org.modelexecution.xmof.vm.XMOFInstanceMap;
import org.modelexecution.xmof.vm.XMOFVirtualMachine;
import org.modelexecution.xmof.vm.util.XMOFUtil;

import fUML.Semantics.Classes.Kernel.ExtensionalValue;
import fUML.Semantics.Classes.Kernel.FeatureValue;
import fUML.Semantics.Classes.Kernel.Link;
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.Reference;
import fUML.Semantics.Classes.Kernel.StringValue;
import fUML.Semantics.Classes.Kernel.Value;
import fUML.Semantics.Loci.LociL1.Locus;

public class FUMLTest extends SemanticsTest {

	private static final String FUML_METAMODEL_PATH = "http://www.eclipse.org/uml2/5.0.0/UML";
	private static final String FUML_CONFIGURATION_PATH = "fuml/fuml.xmof";

	private static final String FUML_MODEL_DIR = "test/fuml/";
	private static final String FUML_BEHAVIOR_LIBRARY_FILENAME = "primitiveBehaviorLibrary.uml";
	private static final String FUML_BEHAVIOR_LIBRARY_PATH = FUML_MODEL_DIR
			+ FUML_BEHAVIOR_LIBRARY_FILENAME;
	private static final String FUML_TYPE_LIBRARY_FILENAME = "primitiveTypeLibrary.uml";
	private static final String FUML_TYPE_LIBRARY_PATH = FUML_MODEL_DIR
			+ FUML_TYPE_LIBRARY_FILENAME;
	private static final String FUML_EXEENV_FILENAME = "executionenvironment.xmi";

	private static final String EXECUTION_ENVIRONMENT = "ExecutionEnvironment";
	private static final String FUNCTION_BEHAVIOR = "FunctionBehavior";
	private static final String INTEGER_PLUS_FUNCTION_BEHAVIOR_EXECUTION = "IntegerPlusFunctionBehaviorExecution";

	@BeforeClass
	public static void collectAllActivities() {
		SemanticsTest.collectAllActivities(FUML_CONFIGURATION_PATH);
	}
	
	@Test
	public void test1_setupOfExecutionEnvironment() {
		setupVM("test/fuml/testmodel.uml", "test/fuml/test1parameter.xmi");

		// check presence of ExecutionEnvironment instance
		EObject executionEnvironmentObject = getResourceByFileName(
				FUML_EXEENV_FILENAME).getContents().get(0);
		assertNotNull(executionEnvironmentObject);
		assertEquals(EXECUTION_ENVIRONMENT, executionEnvironmentObject.eClass()
				.getName());
		assertTrue(existsModelElementAtLocus(executionEnvironmentObject));

		// check presence of OpaqueBehaviorExecution instance for primitive
		// behavior and link to corresponding FunctionBehavior
		EObject integerPlusFunctionBehavior = getResourceByFileName(
				FUML_BEHAVIOR_LIBRARY_FILENAME).getContents().get(0)
				.eContents().get(0).eContents().get(0);
		assertNotNull(integerPlusFunctionBehavior);
		assertEquals(FUNCTION_BEHAVIOR, integerPlusFunctionBehavior.eClass()
				.getName());
		assertTrue(existsModelElementAtLocus(integerPlusFunctionBehavior));

		EObject integerPlusFunctionBehaviorExecution = null;
		TreeIterator<EObject> eAllContents = executionEnvironmentObject
				.eAllContents();
		while (eAllContents.hasNext()) {
			EObject next = eAllContents.next();
			if (next.eClass().getName()
					.equals(INTEGER_PLUS_FUNCTION_BEHAVIOR_EXECUTION))
				;
			integerPlusFunctionBehaviorExecution = next;
		}
		assertNotNull(integerPlusFunctionBehaviorExecution);
		assertTrue(existsModelElementAtLocus(integerPlusFunctionBehaviorExecution));

		Object_ integerPlusFunctionBehaviorFUMLObject = getFUMLObjectFromModelElement(integerPlusFunctionBehavior);
		Object_ integerPlusFunctionBehaviorExecutionFUMLObject = getFUMLObjectFromModelElement(integerPlusFunctionBehaviorExecution);
		Link typeLink = null;
		Locus locus = getLocus();
		for (ExtensionalValue extensionalValue : locus.extensionalValues) {
			if (extensionalValue instanceof Link) {
				Link link = (Link) extensionalValue;
				if (link.type.name.equals("types")) {
					List<Object_> linkedObjects = getLinkedObjects(link);
					Object_ linkedObject0 = linkedObjects.get(0);
					Object_ linkedObject1 = linkedObjects.get(1);
					if ((linkedObject0 == integerPlusFunctionBehaviorFUMLObject && linkedObject1 == integerPlusFunctionBehaviorExecutionFUMLObject)
							|| (linkedObject1 == integerPlusFunctionBehaviorFUMLObject && linkedObject0 == integerPlusFunctionBehaviorExecutionFUMLObject)) {
						typeLink = link;
					}
				}
			}
		}
		assertNotNull(typeLink);
		cleanup();
	}
	
	@Test
	public void test2_activityExecution() {
		Trace trace = execute("test/fuml/testmodel.uml", "test/fuml/test1parameter.xmi",
				true);
		assertNotNull(getActivityExecution(trace, "main"));
		assertNotNull(getActivityExecution(trace, "execute_Executor"));
		
		ActivityExecution createExecution = getActivityExecution(trace, "createExecution_ExecutionFactory");
		assertNotNull(createExecution);
		assertEquals(1, createExecution.getActivityOutputs().get(0).getParameterValues().size());
		assertNotNull(createExecution.getActivityOutputs().get(0).getParameterValues().get(0).getValueSnapshot());
		
		assertNotNull(getActivityExecution(trace, "execute_ActivityExecution"));
		assertNotNull(getActivityExecution(trace, "activate_ActivityNodeActivationGroup"));
		assertNotNull(getActivityExecution(trace, "createNodeActivations_ActivityNodeActivationGroup"));
		assertNotNull(getActivityExecution(trace, "createEdgeInstances_ActivityNodeActivationGroup"));
		assertNotNull(getActivityExecution(trace, "run_ActivityNodeActivationGroup"));
		assertNotNull(getActivityExecution(trace, "runNodes_ActivityNodeActivationGroup"));
		assertNotNull(getActivityExecution(trace, "getInitiallyEnabledNodeActivations_ActivityNodeActivationGroup"));		
	}

	@Test
	public void test3_opaqueActionExecution() {
		Trace trace = execute("test/fuml/testmodel.uml", "test/fuml/test2parameter.xmi",
				true);
		Set<ActivityExecution> activityExecutions_doAction = getActivityExecutions(trace, "doAction_OpaqueAction");
		assertEquals(3, activityExecutions_doAction.size());
		
		ActivityExecution opaqueAction1Execution = getActivityExecutionForActionExecution(trace, "OpaqueAction1");
		ActivityExecution opaqueAction2Execution = getActivityExecutionForActionExecution(trace, "OpaqueAction2");
		ActivityExecution opaqueAction3Execution = getActivityExecutionForActionExecution(trace, "OpaqueAction3");
	
		assertTrue(opaqueAction2Execution.isChronologicalSuccessorOf(opaqueAction1Execution));
		assertTrue(opaqueAction3Execution.isChronologicalSuccessorOf(opaqueAction2Execution));
	}
	
	private ActivityExecution getActivityExecutionForActionExecution(Trace trace,
			String actionName) {
		Set<ActivityExecution> activityExecutions_doAction = getActivityExecutions(trace, "doAction_OpaqueAction");
		for(ActivityExecution activityExecution_doAction : activityExecutions_doAction) {
			Object_ semanticVisitor = getContextObject(activityExecution_doAction);
			Object_ runtimeModelElement = getLinkedObject(trace, semanticVisitor, "runtimeModelElement");
			if(((StringValue)getFeatureValue(runtimeModelElement, "name")).value.equals(actionName))
				return activityExecution_doAction;	
		}
		return null;
	}

	private Value getFeatureValue(Object_ object_, String featureName) {
		for(FeatureValue featureValue : object_.featureValues) {
			if(featureValue.feature.name.equals(featureName))
				return featureValue.values.get(0);
		}
		return null;
	}
	
	private Object_ getContextObject(ActivityExecution activityExecution) {
		return (Object_)((ValueInstance)activityExecution.getContextValueSnapshot().eContainer()).getRuntimeValue();
	}
	
	private Object_ getLinkedObject(Trace trace, Object_ object_,
			String associationEnd) {
		Link link = null;
		for (ValueInstance valueInstance : trace.getValueInstances()) {
			Value runtimeValue = valueInstance.getRuntimeValue();
			if (runtimeValue instanceof Link) {
				Link l = (Link) runtimeValue;
				boolean hasAssociationEnd = false;
				boolean linksObject = false;
				for (FeatureValue featureValue : l.featureValues) {
					if (featureValue.feature.name.equals(associationEnd))
						hasAssociationEnd = true;
					for (Value value : featureValue.values) {
						Reference reference = (Reference) value;
						if (reference.referent == object_)
							linksObject = true;
					}
				}
				if (hasAssociationEnd && linksObject) {
					link = l;
					break;
				}
			}
		}
		if (link != null)
			for (FeatureValue featureValue : link.featureValues) {
				if (featureValue.feature.name.equals(associationEnd)) {
					return ((Reference) featureValue.values.get(0)).referent;
				}
			}

		return null;
	}

	private List<Object_> getLinkedObjects(Link link) {
		List<Object_> linkedObjects = new ArrayList<Object_>();
		linkedObjects.add(getLinkedObject(link, 0));
		linkedObjects.add(getLinkedObject(link, 1));
		return linkedObjects;
	}

	private Object_ getLinkedObject(Link link, int featureValuePosition) {
		Reference reference = (Reference) link.featureValues
				.get(featureValuePosition).values.get(0);
		return reference.referent;
	}

	private boolean existsModelElementAtLocus(EObject eObject) {
		Locus locus = getLocus();
		EObject configurationObject = getConfigurationObject(eObject);
		if (configurationObject == null)
			return false;
		Object_ fumlObject = getFUMLObjectFromConfigurationObject(configurationObject);
		if (fumlObject == null)
			return false;
		if (!locus.extensionalValues.contains(fumlObject))
			return false;
		return true;
	}

	private Object_ getFUMLObjectFromModelElement(EObject eObject) {
		EObject configurationObject = getConfigurationObject(eObject);
		Object_ fumlObject = getFUMLObjectFromConfigurationObject(configurationObject);
		return fumlObject;
	}

	private Object_ getFUMLObjectFromConfigurationObject(EObject eObject) {
		XMOFInstanceMap instanceMap = getInstanceMap();
		return instanceMap.getObject(eObject);
	}

	private EObject getConfigurationObject(EObject eObject) {
		ConfigurationObjectMap configurationObjectMap = getConfigurationObjectMap();
		return configurationObjectMap.getConfigurationObject(eObject);
	}

	private Resource getResourceByFileName(String name) {
		ResourceSet resourceSet = getResourceSet();
		for (Resource resource : resourceSet.getResources()) {
			if (resource.getURI().lastSegment().equals(name)) {
				return resource;
			}
		}
		return null;
	}

	XMOFVirtualMachine setupVM(String modelPath,
			String parameterDefinitionPath) {
		return setupVM(modelPath, parameterDefinitionPath, FUML_METAMODEL_PATH,
				FUML_CONFIGURATION_PATH);
	}
	
	private Trace execute(String modelPath, String parameterDefinitionPath,
			boolean cleanup) {
		return execute(modelPath, parameterDefinitionPath, FUML_METAMODEL_PATH,
				FUML_CONFIGURATION_PATH, cleanup);
	}

	@Override
	void loadMetamodel(String metamodelPath) {
	}

	@Override
	ConfigurationObjectMap createConfigurationObjectMap(Resource modelResource,
			Resource configurationResource, Resource parameterDefintionResource) {
		Resource primitiveTypeLibraryPath = loadResource(FUML_TYPE_LIBRARY_PATH);
		Resource primitiveBehaviorLibraryPath = loadResource(FUML_BEHAVIOR_LIBRARY_PATH);
		ConfigurationObjectMap configurationObjectMap = XMOFUtil
				.createConfigurationObjectMap(configurationResource,
						modelResource, parameterDefintionResource,
						primitiveTypeLibraryPath, primitiveBehaviorLibraryPath);
		return configurationObjectMap;
	}
}
