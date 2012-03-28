/*
 * Copyright (c) 2012 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tanja Mayerhofer - initial API and implementation
 */

package org.modelexecution.fumldebug.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modelexecution.fumldebug.core.ExecutionContext;
import org.modelexecution.fumldebug.core.ExecutionEventListener;
import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityExitEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeExitEvent;
import org.modelexecution.fumldebug.core.event.Event;
import org.modelexecution.fumldebug.core.event.StepEvent;
import org.modelexecution.fumldebug.core.test.Return5BehaviorExecution;
import org.modelexecution.fumldebug.core.util.ActivityFactory;

import fUML.Semantics.Actions.BasicActions.CallBehaviorActionActivation;
import fUML.Semantics.Activities.IntermediateActivities.ActivityExecution;
import fUML.Semantics.Classes.Kernel.ExtensionalValueList;
import fUML.Semantics.Classes.Kernel.FeatureValue;
import fUML.Semantics.Classes.Kernel.IntegerValue;
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.PrimitiveValue;
import fUML.Semantics.Classes.Kernel.Reference;
import fUML.Semantics.Classes.Kernel.StringValue;
import fUML.Semantics.Classes.Kernel.Value;
import fUML.Semantics.Classes.Kernel.ValueList;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValue;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
import fUML.Syntax.Actions.BasicActions.CallBehaviorAction;
import fUML.Syntax.Actions.BasicActions.OutputPin;
import fUML.Syntax.Actions.BasicActions.OutputPinList;
import fUML.Syntax.Actions.IntermediateActions.AddStructuralFeatureValueAction;
import fUML.Syntax.Actions.IntermediateActions.CreateObjectAction;
import fUML.Syntax.Actions.IntermediateActions.ValueSpecificationAction;
import fUML.Syntax.Activities.IntermediateActivities.Activity;
import fUML.Syntax.Activities.IntermediateActivities.ActivityFinalNode;
import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
import fUML.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
import fUML.Syntax.Activities.IntermediateActivities.ForkNode;
import fUML.Syntax.Activities.IntermediateActivities.InitialNode;
import fUML.Syntax.Activities.IntermediateActivities.MergeNode;
import fUML.Syntax.Classes.Kernel.Class_;
import fUML.Syntax.Classes.Kernel.Parameter;
import fUML.Syntax.Classes.Kernel.ParameterDirectionKind;
import fUML.Syntax.Classes.Kernel.Property;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;

/**
 * @author Tanja Mayerhofer
 *
 */
public class DebugTest implements ExecutionEventListener{

	private List<Event> eventlist = new ArrayList<Event>();
	private List<ExtensionalValueList> extensionalValueLists = new ArrayList<ExtensionalValueList>();
	
	public DebugTest() {
		ExecutionContext.getInstance().getExecutionEventProvider().addEventListener(this);
		ExecutionContext.getInstance().activityExecutionOutput = new HashMap<ActivityExecution, ParameterValueList>();
		ExecutionContext.getInstance().activityExecutions = new HashMap<Integer, ActivityExecution>();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		eventlist = new ArrayList<Event>();
		extensionalValueLists = new ArrayList<ExtensionalValueList>();
		ExecutionContext.getInstance().reset();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests the execution of an activity without nodes
	 */
	@Test
	public void testActivityWihtoutNodes() {
		Activity activity = ActivityFactory.createActivity("Activity TestActivityWihtoutNodes");
		
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		assertEquals(activity, ((ActivityEntryEvent)eventlist.get(0)).getActivity());
		
		assertTrue(eventlist.get(1) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(1)).getActivity());		
	}
	
	/**
	 * Tests the execution of an activity that has nodes 
	 * but no node can become enabled.
	 * 
	 * Activity:
	 * MergeNode1
	 * MergeNode2
	 * 
	 * Activity ControlFlow:
	 * MergeNode1 --> MergeNode 2
	 * MergeNode2 --> MergeNode 1
	 */
	@Test
	public void testActivityWithoutEnabledNodes() {
		Activity activity = ActivityFactory.createActivity("Activity TestActivityWithoutEnabledNodes");
		MergeNode merge1 = ActivityFactory.createMergeNode(activity, "MergeNode 1");
		MergeNode merge2 = ActivityFactory.createMergeNode(activity, "MergeNode 2");
		
		ActivityFactory.createControlFlow(activity, merge1, merge2);
		ActivityFactory.createControlFlow(activity, merge2, merge1);
		
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		assertEquals(activity, ((ActivityEntryEvent)eventlist.get(0)).getActivity());
		
		assertTrue(eventlist.get(1) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(1)).getActivity());		
	}
	
	/**
	 * Tests the execution of an activity with one single initial node
	 */
	@Test
	public void testActivitySingleInitialNode() {
		Activity activity = ActivityFactory.createActivity("Activity TestActivitySingleInitialNode");
		
		InitialNode initialnode = ActivityFactory.createInitialNode(activity, "InitilNode");
		
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		assertEquals(activity, ((ActivityEntryEvent)eventlist.get(0)).getActivity());
		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		//assertEquals(initialnode, ((StepEvent)eventlist.get(1)).getLocation());
		
		ExecutionContext.getInstance().nextStep();
		
		assertEquals(5, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		assertEquals(initialnode, ((ActivityNodeEntryEvent)eventlist.get(2)).getNode());
		
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initialnode, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		
		assertTrue(eventlist.get(4) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(4)).getActivity());		
	}
	
	/**
	 * Tests the execution of the combination of CreateObjectAction, 
	 * ValueSpecificationAction, and AddStructuralFeatureValueAction, 
	 * whereas the flow of the execution is controlled to accomplish the
	 * following execution order: InitialNode - CreateObjectAction1 - ValueSpecificationAction1 - 
	 * AddStructuralFeatureValueAction - CreateObjectAction2 - ValueSpecificationAction2 - 
	 * AddStructuralFeatureValueAction
	 * 
	 * Activity:
	 * InitialNode
	 * CreateObjectAction1 (class = Person)
	 * ValueSpecificationAction1 (value = "tanja")
	 * CreateObjectAction2 (class = Person)
	 * ValueSpecificationAction2 (value = "philip")
	 * AddStructuralFeatureValueAction (feature = "Name")
	 * 
	 * Activity ControlFlow: 
	 * InitialNode --> CreateObjectAction1 
	 * CreateObjectAction1 --> ValueSpecificationAction1 
	 * ValueSpecificationAction1 --> CreateObjectAction2 
	 * CreateObjectAction2 --> ValueSpecificationAction2
	 * 
	 * Activity ObjectFlow:
	 * CreateObjectAction1.result --> AddStructuralFeatureValueAction.object
	 * CreateObjectAction2.result --> AddStructuralFeatureValueAction.object
	 * ValueSpecificationAction1.result --> AddStructuralFeatureValueAction.value
	 * ValueSpecificationAction2.result --> AddStructuralFeatureValueAction.value
	 */
	@Test
	public void testMultipleAddStructuralFeatureValueActions() {
		Class_ class_person = ActivityFactory.createClass("Person");
		Property property_name = ActivityFactory.createProperty("Name", 0, 1, class_person);
		
		Activity activity = new fUML.Syntax.Activities.IntermediateActivities.Activity();
		activity.setName("TestMultipleAddStructuralFeatureValueActions");
		
		InitialNode initialnode = ActivityFactory.createInitialNode(activity, "InitialNode");
		CreateObjectAction createobject_tanja = ActivityFactory.createCreateObjectAction(activity, "CreateObject Person tanja", class_person);
		CreateObjectAction createobject_philip = ActivityFactory.createCreateObjectAction(activity, "CreateObject Person philip", class_person);
		ValueSpecificationAction valuespec_tanja =  ActivityFactory.createValueSpecificationAction(activity, "ValueSpecification tanja", "tanja");
		ValueSpecificationAction valuespec_philip =  ActivityFactory.createValueSpecificationAction(activity, "ValueSpecification philip", "philip");		
		AddStructuralFeatureValueAction addstructuralfeaturevalue = ActivityFactory.createAddStructuralFeatureValueAction(activity, "AddStructuralFeatureValue Person Name", property_name);				
		
		ActivityFactory.createControlFlow(activity, initialnode, createobject_tanja);
		ActivityFactory.createControlFlow(activity, createobject_tanja, valuespec_tanja);
		ActivityFactory.createControlFlow(activity, valuespec_tanja, createobject_philip);
		ActivityFactory.createControlFlow(activity, createobject_philip, valuespec_philip);
		//ActivityFactory.createControlFlow(activity, valuespec_philip, addstructuralfeaturevalue);

		ActivityFactory.createObjectFlow(activity, createobject_tanja.result, addstructuralfeaturevalue.object);
		ActivityFactory.createObjectFlow(activity, valuespec_tanja.result, addstructuralfeaturevalue.value);
		ActivityFactory.createObjectFlow(activity, createobject_philip.result, addstructuralfeaturevalue.object);
		ActivityFactory.createObjectFlow(activity, valuespec_philip.result, addstructuralfeaturevalue.value);	
		
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		/*
		 * ActivityStart
		 * Step location = null
		 */
		assertEquals(2, eventlist.size());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnode, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		assertEquals(activity, ((ActivityEntryEvent)eventlist.get(0)).getActivity());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(1)).getLocation());	
		
		ExecutionContext.getInstance().nextStep();
	
		/*
		 * ActivityNodeEntry initial
		 * ActivityNodeExit initial
		 * Step location = initial
		 */
		assertEquals(5, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobject_tanja, ExecutionContext.getInstance().getEnabledNodes().get(0));				
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		assertEquals(initialnode, ((ActivityNodeEntryEvent)eventlist.get(2)).getNode());					
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initialnode, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initialnode, ((StepEvent)eventlist.get(4)).getLocation());	
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry create tanja
		 * ActivityNodeExit create tanja
		 * Step location = create tanja
		 */
		assertEquals(8, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(valuespec_tanja, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		assertEquals(createobject_tanja, ((ActivityNodeEntryEvent)eventlist.get(5)).getNode());					
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(createobject_tanja, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(createobject_tanja, ((StepEvent)eventlist.get(7)).getLocation());
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry value tanja
		 * ActivityNodeExit value tanja
		 * Step location = value tanja 
		 */
		assertEquals(11, eventlist.size());
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes().size());	
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(createobject_philip));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(addstructuralfeaturevalue));
		
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		assertEquals(valuespec_tanja, ((ActivityNodeEntryEvent)eventlist.get(8)).getNode());					
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(valuespec_tanja, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertTrue(eventlist.get(10) instanceof StepEvent);
		assertEquals(valuespec_tanja, ((StepEvent)eventlist.get(10)).getLocation());
		
		ExecutionContext.getInstance().nextStep(addstructuralfeaturevalue);
		
		/*
		 * ActivityNodeEntry add tanja
		 * ActivityNodeExit add tanja
		 * Step location = add tanja
		 */
		assertEquals(14, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobject_philip, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertTrue(eventlist.get(11) instanceof ActivityNodeEntryEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeEntryEvent)eventlist.get(11)).getNode());					
		assertTrue(eventlist.get(12) instanceof ActivityNodeExitEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeExitEvent)eventlist.get(12)).getNode());
		assertTrue(eventlist.get(13) instanceof StepEvent);
		assertEquals(addstructuralfeaturevalue, ((StepEvent)eventlist.get(13)).getLocation());
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry create philip
		 * ActivityNodeExit create philip
		 * Step location = create philip
		 */
		assertEquals(17, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(valuespec_philip, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertTrue(eventlist.get(14) instanceof ActivityNodeEntryEvent);
		assertEquals(createobject_philip, ((ActivityNodeEntryEvent)eventlist.get(14)).getNode());					
		assertTrue(eventlist.get(15) instanceof ActivityNodeExitEvent);
		assertEquals(createobject_philip, ((ActivityNodeExitEvent)eventlist.get(15)).getNode());
		assertTrue(eventlist.get(16) instanceof StepEvent);
		assertEquals(createobject_philip, ((StepEvent)eventlist.get(16)).getLocation());
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry value philip
		 * ActivityNodeExit value philip
		 * Step location = value philip
		 */
		assertEquals(20, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(addstructuralfeaturevalue, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertTrue(eventlist.get(17) instanceof ActivityNodeEntryEvent);
		assertEquals(valuespec_philip, ((ActivityNodeEntryEvent)eventlist.get(17)).getNode());					
		assertTrue(eventlist.get(18) instanceof ActivityNodeExitEvent);
		assertEquals(valuespec_philip, ((ActivityNodeExitEvent)eventlist.get(18)).getNode());
		assertTrue(eventlist.get(19) instanceof StepEvent);
		assertEquals(valuespec_philip, ((StepEvent)eventlist.get(19)).getLocation());
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry add philip
		 * ActivityNodeExit add philip
		 * ActivityExit
		 */		
		assertEquals(23, eventlist.size());
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());
		
		assertTrue(eventlist.get(20) instanceof ActivityNodeEntryEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeEntryEvent)eventlist.get(20)).getNode());					
		assertTrue(eventlist.get(21) instanceof ActivityNodeExitEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeExitEvent)eventlist.get(21)).getNode());
		assertTrue(eventlist.get(22) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(22)).getActivity());
					
		List<ActivityNode> nodeorder = new ArrayList<ActivityNode>();
		nodeorder.add(initialnode);
		nodeorder.add(createobject_tanja);
		nodeorder.add(valuespec_tanja);
		nodeorder.add(addstructuralfeaturevalue);
		nodeorder.add(createobject_philip);
		nodeorder.add(valuespec_philip);
		nodeorder.add(addstructuralfeaturevalue);
				
		assertEquals(8, extensionalValueLists.size());
		assertEquals(0, extensionalValueLists.get(0).size());
		assertEquals(0, extensionalValueLists.get(1).size());
		
		for(int i=2;i<4;++i) {
			assertEquals(1, extensionalValueLists.get(i).size());
			assertTrue(extensionalValueLists.get(i).get(0) instanceof Object_);		
			Object_ o = (Object_)(extensionalValueLists.get(i).get(0));
			assertEquals(1, o.types.size());
			assertEquals(class_person, o.types.get(0));
			assertEquals(0, o.featureValues.size());
			//assertEquals(1, o.featureValues.size());
			//assertEquals(property_name, o.featureValues.get(0).feature);
			//assertEquals(0, o.featureValues.get(0).values.size());
		}
		
		assertEquals(1, extensionalValueLists.get(4).size());
		assertTrue(extensionalValueLists.get(4).get(0) instanceof Object_);		
		Object_ o = (Object_)(extensionalValueLists.get(4).get(0));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(1, o.featureValues.size());
		assertEquals(property_name, o.featureValues.get(0).feature);
		assertEquals(1, o.featureValues.get(0).values.size());
		assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
		assertEquals("tanja", ((StringValue)o.featureValues.get(0).values.get(0)).value);
		
		for(int i=5;i<7;++i) {
			assertEquals(2, extensionalValueLists.get(i).size());
			assertTrue(extensionalValueLists.get(i).get(0) instanceof Object_);		
			o = (Object_)(extensionalValueLists.get(i).get(0));
			assertEquals(1, o.types.size());
			assertEquals(class_person, o.types.get(0));
			assertEquals(1, o.featureValues.size());
			assertEquals(property_name, o.featureValues.get(0).feature);
			assertEquals(1, o.featureValues.get(0).values.size());
			assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
			assertEquals("tanja", ((StringValue)o.featureValues.get(0).values.get(0)).value);
			
			assertTrue(extensionalValueLists.get(i).get(1) instanceof Object_);		
			o = (Object_)(extensionalValueLists.get(i).get(1));
			assertEquals(1, o.types.size());
			assertEquals(class_person, o.types.get(0));
			assertEquals(0, o.featureValues.size());
		}
		
		assertEquals(2, extensionalValueLists.get(7).size());
		assertTrue(extensionalValueLists.get(7).get(0) instanceof Object_);		
		o = (Object_)(extensionalValueLists.get(7).get(0));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(1, o.featureValues.size());
		assertEquals(property_name, o.featureValues.get(0).feature);
		assertEquals(1, o.featureValues.get(0).values.size());
		assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
		assertEquals("tanja", ((StringValue)o.featureValues.get(0).values.get(0)).value);
		
		assertTrue(extensionalValueLists.get(7).get(1) instanceof Object_);		
		o = (Object_)(extensionalValueLists.get(7).get(1));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(1, o.featureValues.size());
		assertEquals(property_name, o.featureValues.get(0).feature);
		assertEquals(1, o.featureValues.get(0).values.size());
		assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
		assertEquals("philip", ((StringValue)o.featureValues.get(0).values.get(0)).value);
	}
	
	/**
	 * This test case is a variation of the test case
	 * {@link #testMultipleAddStructuralFeatureValueActions()}, 
	 * whereas the flow of the execution is controlled to accomplish the
	 * following execution order: InitialNode - CreateObjectAction1 - 
	 * ValueSpecificationAction1 - CreateObjectAction2 - ValueSpecificationAction2 - 
	 * AddStructuralFeatureValueAction - AddStructuralFeatureValueAction
	 */
	@Test
	public void testMultipleAddStructuralFeatureValueActions2() {
		Class_ class_person = ActivityFactory.createClass("Person");
		Property property_name = ActivityFactory.createProperty("Name", 0, 1, class_person);
		
		Activity activity = new fUML.Syntax.Activities.IntermediateActivities.Activity();
		activity.setName("TestMultipleAddStructuralFeatureValueActions");
		
		InitialNode initialnode = ActivityFactory.createInitialNode(activity, "InitialNode");
		CreateObjectAction createobject_tanja = ActivityFactory.createCreateObjectAction(activity, "CreateObject Person tanja", class_person);
		CreateObjectAction createobject_philip = ActivityFactory.createCreateObjectAction(activity, "CreateObject Person philip", class_person);
		ValueSpecificationAction valuespec_tanja =  ActivityFactory.createValueSpecificationAction(activity, "ValueSpecification tanja", "tanja");
		ValueSpecificationAction valuespec_philip =  ActivityFactory.createValueSpecificationAction(activity, "ValueSpecification philip", "philip");		
		AddStructuralFeatureValueAction addstructuralfeaturevalue = ActivityFactory.createAddStructuralFeatureValueAction(activity, "AddStructuralFeatureValue Person Name", property_name);				
		
		ActivityFactory.createControlFlow(activity, initialnode, createobject_tanja);
		ActivityFactory.createControlFlow(activity, createobject_tanja, valuespec_tanja);
		ActivityFactory.createControlFlow(activity, valuespec_tanja, createobject_philip);
		ActivityFactory.createControlFlow(activity, createobject_philip, valuespec_philip);

		ActivityFactory.createObjectFlow(activity, createobject_tanja.result, addstructuralfeaturevalue.object);
		ActivityFactory.createObjectFlow(activity, valuespec_tanja.result, addstructuralfeaturevalue.value);
		ActivityFactory.createObjectFlow(activity, createobject_philip.result, addstructuralfeaturevalue.object);
		ActivityFactory.createObjectFlow(activity, valuespec_philip.result, addstructuralfeaturevalue.value);	
		
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		/*
		 * ActivityStart
		 * Step location = null
		 */
		assertEquals(2, eventlist.size());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnode, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		assertEquals(activity, ((ActivityEntryEvent)eventlist.get(0)).getActivity());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(1)).getLocation());	
		
		ExecutionContext.getInstance().nextStep();
	
		/*
		 * ActivityNodeEntry initial
		 * ActivityNodeExit initial
		 * Step location = initial
		 */
		assertEquals(5, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobject_tanja, ExecutionContext.getInstance().getEnabledNodes().get(0));				
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		assertEquals(initialnode, ((ActivityNodeEntryEvent)eventlist.get(2)).getNode());					
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initialnode, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initialnode, ((StepEvent)eventlist.get(4)).getLocation());	
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry create tanja
		 * ActivityNodeExit create tanja
		 * Step location = create tanja
		 */
		assertEquals(8, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(valuespec_tanja, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		assertEquals(createobject_tanja, ((ActivityNodeEntryEvent)eventlist.get(5)).getNode());					
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(createobject_tanja, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(createobject_tanja, ((StepEvent)eventlist.get(7)).getLocation());
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry value tanja
		 * ActivityNodeExit value tanja
		 * Step location = value tanja 
		 */
		assertEquals(11, eventlist.size());
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes().size());	
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(createobject_philip));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(addstructuralfeaturevalue));
		
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		assertEquals(valuespec_tanja, ((ActivityNodeEntryEvent)eventlist.get(8)).getNode());					
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(valuespec_tanja, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertTrue(eventlist.get(10) instanceof StepEvent);
		assertEquals(valuespec_tanja, ((StepEvent)eventlist.get(10)).getLocation());
		
		ExecutionContext.getInstance().nextStep(createobject_philip);
		
		/*
		 * ActivityNodeEntry create philip
		 * ActivityNodeExit create philip
		 * Step location = create philip
		 */
		assertEquals(14, eventlist.size());
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(valuespec_philip, ExecutionContext.getInstance().getEnabledNodes().get(0));
		assertEquals(addstructuralfeaturevalue, ExecutionContext.getInstance().getEnabledNodes().get(1));		
		
		assertTrue(eventlist.get(11) instanceof ActivityNodeEntryEvent);
		assertEquals(createobject_philip, ((ActivityNodeEntryEvent)eventlist.get(11)).getNode());					
		assertTrue(eventlist.get(12) instanceof ActivityNodeExitEvent);
		assertEquals(createobject_philip, ((ActivityNodeExitEvent)eventlist.get(12)).getNode());
		assertTrue(eventlist.get(13) instanceof StepEvent);
		assertEquals(createobject_philip, ((StepEvent)eventlist.get(13)).getLocation());
		
		ExecutionContext.getInstance().nextStep(valuespec_philip);
		
		/*
		 * ActivityNodeEntry value philip
		 * ActivityNodeExit value philip
		 * Step location = value philip
		 */
		assertEquals(17, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(addstructuralfeaturevalue, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertTrue(eventlist.get(14) instanceof ActivityNodeEntryEvent);
		assertEquals(valuespec_philip, ((ActivityNodeEntryEvent)eventlist.get(14)).getNode());					
		assertTrue(eventlist.get(15) instanceof ActivityNodeExitEvent);
		assertEquals(valuespec_philip, ((ActivityNodeExitEvent)eventlist.get(15)).getNode());
		assertTrue(eventlist.get(16) instanceof StepEvent);
		assertEquals(valuespec_philip, ((StepEvent)eventlist.get(16)).getLocation());
		
		ExecutionContext.getInstance().nextStep(addstructuralfeaturevalue);
		
		/*
		 * ActivityNodeEntry add tanja
		 * ActivityNodeExit add tanja
		 * Step location = add tanja
		 */
		assertEquals(20, eventlist.size());
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(addstructuralfeaturevalue, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertTrue(eventlist.get(17) instanceof ActivityNodeEntryEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeEntryEvent)eventlist.get(17)).getNode());					
		assertTrue(eventlist.get(18) instanceof ActivityNodeExitEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeExitEvent)eventlist.get(18)).getNode());
		assertTrue(eventlist.get(19) instanceof StepEvent);
		assertEquals(addstructuralfeaturevalue, ((StepEvent)eventlist.get(19)).getLocation());
		
		ExecutionContext.getInstance().nextStep();
		
		/*
		 * ActivityNodeEntry add philip
		 * ActivityNodeExit add philip
		 * ActivityExit
		 */		
		assertEquals(23, eventlist.size());
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());
		
		assertTrue(eventlist.get(20) instanceof ActivityNodeEntryEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeEntryEvent)eventlist.get(20)).getNode());					
		assertTrue(eventlist.get(21) instanceof ActivityNodeExitEvent);
		assertEquals(addstructuralfeaturevalue, ((ActivityNodeExitEvent)eventlist.get(21)).getNode());
		assertTrue(eventlist.get(22) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(22)).getActivity());
					
		List<ActivityNode> nodeorder = new ArrayList<ActivityNode>();
		nodeorder.add(initialnode);
		nodeorder.add(createobject_tanja);
		nodeorder.add(valuespec_tanja);
		nodeorder.add(createobject_philip);
		nodeorder.add(valuespec_philip);
		nodeorder.add(addstructuralfeaturevalue);
		nodeorder.add(addstructuralfeaturevalue);
				
		assertEquals(8, extensionalValueLists.size());
		assertEquals(0, extensionalValueLists.get(0).size());
		assertEquals(0, extensionalValueLists.get(1).size());
		
		/*
		 * Person object from CreateObjectAction Tanja 
		 */
		for(int i=2;i<4;++i) {
			assertEquals(1, extensionalValueLists.get(i).size());
			assertTrue(extensionalValueLists.get(i).get(0) instanceof Object_);		
			Object_ o = (Object_)(extensionalValueLists.get(i).get(0));
			assertEquals(1, o.types.size());
			assertEquals(class_person, o.types.get(0));
			assertEquals(0, o.featureValues.size());
		}
		
		/*
		 * Person objects from CreateObjectAction Tanja and CreateObjectAction Philip
		 */
		for(int i=4;i<6;++i) {
			assertEquals(2, extensionalValueLists.get(i).size());
			for(int j=0;j<2;++j) {
				assertTrue(extensionalValueLists.get(i).get(j) instanceof Object_);		
				Object_ o = (Object_)(extensionalValueLists.get(i).get(j));
				assertEquals(1, o.types.size());
				assertEquals(class_person, o.types.get(0));
				assertEquals(0, o.featureValues.size());
			}
		}
		
		/*
		 * Name was set for Person object from CreateObjectAction Tanja 
		 */
		assertEquals(2, extensionalValueLists.get(6).size());
		
		assertTrue(extensionalValueLists.get(6).get(0) instanceof Object_);		
		Object_ o = (Object_)(extensionalValueLists.get(6).get(0));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(1, o.featureValues.size());
		assertEquals(property_name, o.featureValues.get(0).feature);
		assertEquals(1, o.featureValues.get(0).values.size());
		assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
		assertEquals("tanja", ((StringValue)o.featureValues.get(0).values.get(0)).value);
		
		assertTrue(extensionalValueLists.get(6).get(1) instanceof Object_);		
		o = (Object_)(extensionalValueLists.get(6).get(1));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(0, o.featureValues.size());
			
		/*
		 * Name was set for Person object from CreateObjectActipn Philip
		 */
		assertEquals(2, extensionalValueLists.get(7).size());
		
		assertTrue(extensionalValueLists.get(7).get(0) instanceof Object_);		
		o = (Object_)(extensionalValueLists.get(7).get(0));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(1, o.featureValues.size());
		assertEquals(property_name, o.featureValues.get(0).feature);
		assertEquals(1, o.featureValues.get(0).values.size());
		assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
		assertEquals("tanja", ((StringValue)o.featureValues.get(0).values.get(0)).value);
		
		assertTrue(extensionalValueLists.get(7).get(1) instanceof Object_);		
		o = (Object_)(extensionalValueLists.get(7).get(1));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(1, o.featureValues.size());
		assertEquals(property_name, o.featureValues.get(0).feature);
		assertEquals(1, o.featureValues.get(0).values.size());
		assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
		assertEquals("philip", ((StringValue)o.featureValues.get(0).values.get(0)).value);
	}

	/**
	 * Tests the execution of an CallBehaviorAction that calls an Activity.
	 * 
	 * Activity 1 (Caller):
	 * InitialNode
	 * CreateObjectAction (class = Class1)
	 * CallBehaviorAction (behavior = Activity 2)
	 * 
	 * Activity 1 ControlFlow:
	 * InitialNode --> CreateObjectAction
	 * CreateObjectAction --> CallBehaviorAction
	 * 
	 * Activity 2 (Callee):
	 * InitialNode
	 * CreateObjectAction (class = Class2)
	 * 
	 * Activity 2 ControlFlow:
	 * InitialNode --> CreateObjectAction
	 */
	@Test
	public void testCallBehaviorAction() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		
		Activity activitycallee = ActivityFactory.createActivity("TestCallBehaviorAction Callee");
		InitialNode initialnodecallee = ActivityFactory.createInitialNode(activitycallee, "InitialNode Callee");
		CreateObjectAction createobjectclass2 = ActivityFactory.createCreateObjectAction(activitycallee, "CreateObjectAction Class2", class2);
		ActivityFactory.createControlFlow(activitycallee, initialnodecallee, createobjectclass2);
		
		Activity activitycaller = ActivityFactory.createActivity("TestCallBehaviorAction Caller");
		InitialNode initialnodecaller = ActivityFactory.createInitialNode(activitycaller, "InitialNode Caller");
		CreateObjectAction createobjectclass1 = ActivityFactory.createCreateObjectAction(activitycaller, "CreateObjectAction Class1", class1);				
		CallBehaviorAction callaction = ActivityFactory.createCallBehaviorAction(activitycaller, "CallBehaviorAction Call ", activitycallee);
		ActivityFactory.createControlFlow(activitycaller, initialnodecaller, createobjectclass1);
		ActivityFactory.createControlFlow(activitycaller, createobjectclass1, callaction);		
		
		// DEBUG
		ExecutionContext.getInstance().debug(activitycaller, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent entrycaller = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activitycaller, entrycaller.getActivity());		
		assertNull(entrycaller.getParent());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnodecaller, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodecallerentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(initialnodecaller, initialnodecallerentry.getNode());		
		assertEquals(entrycaller, eventlist.get(2).getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initialnodecaller, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialnodecallerentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initialnodecaller, ((StepEvent)eventlist.get(4)).getLocation());	
		assertNull(eventlist.get(4).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobjectclass1, ExecutionContext.getInstance().getEnabledNodes().get(0));			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();					
	
		assertEquals(8, eventlist.size());
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl1entry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(createobjectclass1, createcl1entry.getNode());	
		assertEquals(entrycaller, createcl1entry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(createobjectclass1, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(createcl1entry, eventlist.get(6).getParent());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(createobjectclass1, ((StepEvent)eventlist.get(7)).getLocation());	
		assertNull(eventlist.get(7).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(callaction, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		Object_ o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
				
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
		
		assertEquals(11, eventlist.size());
		ActivityNodeEntryEvent callactionentry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		assertEquals(callaction, callactionentry.getNode());
		assertEquals(entrycaller, callactionentry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityEntryEvent);
		ActivityEntryEvent entrycallee = (ActivityEntryEvent)eventlist.get(9);
		assertEquals(activitycallee, entrycallee.getActivity());
		assertEquals(callactionentry, entrycallee.getParent());
		assertTrue(eventlist.get(10) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(10)).getLocation());
		assertNull(eventlist.get(10).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnodecallee, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();				
		
		assertEquals(14, eventlist.size());
		assertTrue(eventlist.get(11) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodecalleeentry =(ActivityNodeEntryEvent)eventlist.get(11); 
		assertEquals(initialnodecallee, initialnodecalleeentry.getNode());	
		assertEquals(entrycallee, initialnodecalleeentry.getParent());
		assertTrue(eventlist.get(12) instanceof ActivityNodeExitEvent);
		assertEquals(initialnodecallee, ((ActivityNodeExitEvent)eventlist.get(12)).getNode());
		assertEquals(initialnodecalleeentry, eventlist.get(12).getParent());
		assertTrue(eventlist.get(13) instanceof StepEvent);
		assertEquals(initialnodecallee, ((StepEvent)eventlist.get(13)).getLocation());	
		assertNull(eventlist.get(13).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobjectclass2, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());
		
		assertEquals(19, eventlist.size());
		assertTrue(eventlist.get(14) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl2entry = (ActivityNodeEntryEvent)eventlist.get(14); 
		assertEquals(createobjectclass2, createcl2entry.getNode());	
		assertEquals(entrycallee, createcl2entry.getParent());
		assertTrue(eventlist.get(15) instanceof ActivityNodeExitEvent);
		assertEquals(createobjectclass2, ((ActivityNodeExitEvent)eventlist.get(15)).getNode());
		assertEquals(createcl2entry, eventlist.get(15).getParent());
		//assertTrue(eventlist.get(16) instanceof StepEvent);
		//assertEquals(createobjectclass2, ((StepEvent)eventlist.get(16)).getLocation());	
		//assertNull(eventlist.get(16).getParent());
		assertTrue(eventlist.get(16) instanceof ActivityExitEvent);
		assertEquals(activitycallee, ((ActivityExitEvent)eventlist.get(16)).getActivity());
		assertEquals(entrycallee, eventlist.get(16).getParent());
		
		assertTrue(eventlist.get(17) instanceof ActivityNodeExitEvent);
		assertEquals(callaction, ((ActivityNodeExitEvent)eventlist.get(17)).getNode());
		assertEquals(callactionentry, eventlist.get(17).getParent());
		assertTrue(eventlist.get(18) instanceof ActivityExitEvent);
		assertEquals(activitycaller, ((ActivityExitEvent)eventlist.get(18)).getActivity());
		assertEquals(entrycaller, eventlist.get(18).getParent());
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(2, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		assertTrue(e.get(1) instanceof Object_);		
		o = (Object_)(e.get(1));
		assertEquals(1, o.types.size());
		assertEquals(class2, o.types.get(0));
	}
	
	/**
	 * This test case is an extension of the test case
	 * {@link #testCallBehaviorAction()}
	 * 
	 * Activity 1 (Caller):
	 * InitialNode
	 * CreateObjectAction1 (class = Class1)
	 * CallBehaviorAction (behavior = Activity 2)
	 * CreateObjectAction2 (class = Class1)
	 * 
	 * Activity 1 ControlFlow:
	 * InitialNode --> CreateObjectAction1
	 * CreateObjectAction1 --> CallBehaviorAction
	 * CallBehaviorAction --> CreateObjectAction2
	 * 
	 * Activity 2 (Callee):
	 * InitialNode
	 * CreateObjectAction (class = Class2)
	 * 
	 * Activity 2 ControlFlow:
	 * InitialNode --> CreateObjectAction
	 */
	@Test
	public void testCallBehaviorAction2() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		
		Activity activitycallee = ActivityFactory.createActivity("TestCallBehaviorAction Callee");
		InitialNode initialnodecallee = ActivityFactory.createInitialNode(activitycallee, "InitialNode Callee");
		CreateObjectAction createobjectclass2 = ActivityFactory.createCreateObjectAction(activitycallee, "CreateObjectAction Class2", class2);
		ActivityFactory.createControlFlow(activitycallee, initialnodecallee, createobjectclass2);
		
		Activity activitycaller = ActivityFactory.createActivity("TestCallBehaviorAction Caller");
		InitialNode initialnodecaller = ActivityFactory.createInitialNode(activitycaller, "InitialNode Caller");
		CreateObjectAction createobjectclass1 = ActivityFactory.createCreateObjectAction(activitycaller, "CreateObjectAction Class1", class1);				
		CallBehaviorAction callaction = ActivityFactory.createCallBehaviorAction(activitycaller, "CallBehaviorAction Call ", activitycallee);
		CreateObjectAction createobjectclass1_2 = ActivityFactory.createCreateObjectAction(activitycaller, "CreateObjectAction Class1 2", class1);
		ActivityFactory.createControlFlow(activitycaller, initialnodecaller, createobjectclass1);
		ActivityFactory.createControlFlow(activitycaller, createobjectclass1, callaction);
		ActivityFactory.createControlFlow(activitycaller, callaction, createobjectclass1_2);		
		
		// DEBUG
		ExecutionContext.getInstance().debug(activitycaller, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent entrycaller = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activitycaller, entrycaller.getActivity());		
		assertNull(entrycaller.getParent());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnodecaller, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodecallerentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(initialnodecaller, initialnodecallerentry.getNode());		
		assertEquals(entrycaller, eventlist.get(2).getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initialnodecaller, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialnodecallerentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initialnodecaller, ((StepEvent)eventlist.get(4)).getLocation());	
		assertNull(eventlist.get(4).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobjectclass1, ExecutionContext.getInstance().getEnabledNodes().get(0));			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();					
	
		assertEquals(8, eventlist.size());
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl1entry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(createobjectclass1, createcl1entry.getNode());	
		assertEquals(entrycaller, createcl1entry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(createobjectclass1, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(createcl1entry, eventlist.get(6).getParent());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(createobjectclass1, ((StepEvent)eventlist.get(7)).getLocation());	
		assertNull(eventlist.get(7).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(callaction, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		Object_ o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
				
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
		
		assertEquals(11, eventlist.size());
		ActivityNodeEntryEvent callactionentry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		assertEquals(callaction, callactionentry.getNode());
		assertEquals(entrycaller, callactionentry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityEntryEvent);
		ActivityEntryEvent entrycallee = (ActivityEntryEvent)eventlist.get(9);
		assertEquals(activitycallee, entrycallee.getActivity());
		assertEquals(callactionentry, entrycallee.getParent());
		assertTrue(eventlist.get(10) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(10)).getLocation());
		assertNull(eventlist.get(10).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnodecallee, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();				
		
		assertEquals(14, eventlist.size());
		assertTrue(eventlist.get(11) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodecalleeentry =(ActivityNodeEntryEvent)eventlist.get(11); 
		assertEquals(initialnodecallee, initialnodecalleeentry.getNode());	
		assertEquals(entrycallee, initialnodecalleeentry.getParent());
		assertTrue(eventlist.get(12) instanceof ActivityNodeExitEvent);
		assertEquals(initialnodecallee, ((ActivityNodeExitEvent)eventlist.get(12)).getNode());
		assertEquals(initialnodecalleeentry, eventlist.get(12).getParent());
		assertTrue(eventlist.get(13) instanceof StepEvent);
		assertEquals(initialnodecallee, ((StepEvent)eventlist.get(13)).getLocation());	
		assertNull(eventlist.get(13).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobjectclass2, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();		
		
		assertEquals(19, eventlist.size());
		assertTrue(eventlist.get(14) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl2entry = (ActivityNodeEntryEvent)eventlist.get(14); 
		assertEquals(createobjectclass2, createcl2entry.getNode());	
		assertEquals(entrycallee, createcl2entry.getParent());
		assertTrue(eventlist.get(15) instanceof ActivityNodeExitEvent);
		assertEquals(createobjectclass2, ((ActivityNodeExitEvent)eventlist.get(15)).getNode());
		assertEquals(createcl2entry, eventlist.get(15).getParent());
		//assertTrue(eventlist.get(16) instanceof StepEvent);
		//assertEquals(createobjectclass2, ((StepEvent)eventlist.get(16)).getLocation());	
		//assertNull(eventlist.get(16).getParent());
		assertTrue(eventlist.get(16) instanceof ActivityExitEvent);
		assertEquals(activitycallee, ((ActivityExitEvent)eventlist.get(16)).getActivity());
		assertEquals(entrycallee, eventlist.get(16).getParent());
		
		assertTrue(eventlist.get(17) instanceof ActivityNodeExitEvent);
		assertEquals(callaction, ((ActivityNodeExitEvent)eventlist.get(17)).getNode());
		assertEquals(callactionentry, eventlist.get(17).getParent());
		assertTrue(eventlist.get(18) instanceof StepEvent);
		assertEquals(callaction, ((StepEvent)eventlist.get(18)).getLocation());
		assertNull(eventlist.get(18).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(createobjectclass1_2, ExecutionContext.getInstance().getEnabledNodes().get(0));
						
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(2, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		assertTrue(e.get(1) instanceof Object_);		
		o = (Object_)(e.get(1));
		assertEquals(1, o.types.size());
		assertEquals(class2, o.types.get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();
		
		assertEquals(22, eventlist.size());
		assertTrue(eventlist.get(19) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl12entry = (ActivityNodeEntryEvent)eventlist.get(19);
		assertEquals(createobjectclass1_2, createcl12entry.getNode());	
		assertEquals(entrycaller, createcl12entry.getParent());
		assertTrue(eventlist.get(20) instanceof ActivityNodeExitEvent);
		assertEquals(createobjectclass1_2, ((ActivityNodeExitEvent)eventlist.get(20)).getNode());
		assertEquals(createcl12entry, eventlist.get(20).getParent());
		assertTrue(eventlist.get(21) instanceof ActivityExitEvent);
		assertEquals(activitycaller, ((ActivityExitEvent)eventlist.get(21)).getActivity());
		assertEquals(entrycaller, eventlist.get(21).getParent());		
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(3, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		assertTrue(e.get(1) instanceof Object_);		
		o = (Object_)(e.get(1));
		assertEquals(1, o.types.size());
		assertEquals(class2, o.types.get(0));
		o = (Object_)(e.get(2));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
	}
	
	/**
	 * Tests the execution of an CallBehaviorAction that calls an 
	 * OpaqueBehavior that produces an IntegerValue with value 5
	 * as Output.
	 * Besides the occurrence of the events it is also tested if the
	 * OutputPin of the CallBehaviorAction actually holds an ObjectToken
	 * with the IntegerValue 5 after its execution.
	 * 
	 * Activity: 
	 * InitialNode
	 * CallBehaviorAction (behavior = RETURN5, outputPin)
	 * ActivityFinalNode 
	 * 
	 * Activity ControlFlow:
	 * InitialNode --> CallBehaviorAction
	 * CallBehaviorAction --> ActivityFinalNode
	 */
	@Test
	public void testCallBehaviorActionCallingOpaqueBehavior() {
		OpaqueBehavior return5behavior = new OpaqueBehavior();
		Parameter output = new Parameter();
		output.setDirection(ParameterDirectionKind.out);
		output.setName("result");
		return5behavior.ownedParameter.add(output);
		
		Return5BehaviorExecution return5execution = new Return5BehaviorExecution();
		return5execution.types.add(return5behavior);
		
		ExecutionContext.getInstance().locus.factory.addPrimitiveBehaviorPrototype(return5execution);		
		ExecutionContext.getInstance().opaqueBehaviors.put("RETURN5", return5behavior);
		
		
		Activity activity = ActivityFactory.createActivity("TestCallBehaviorActionCallingOpaqueBehavior");
		//OpaqueBehavior return5behavior = ExecutionContext.getInstance().getOpaqueBehavior("RETURN5");
		
		CallBehaviorAction callaction = ActivityFactory.createCallBehaviorAction(activity, "Call Behavior RETURN 5", return5behavior);		
		OutputPin outputpin_callaction = new OutputPin();
		outputpin_callaction.setName("OutputPin (Call Behavior RETURN)");
		OutputPinList output_callaction = new OutputPinList();
		output_callaction.add(outputpin_callaction);
		callaction.result = output_callaction;
		callaction.output = output_callaction;
		
		InitialNode initialnode = ActivityFactory.createInitialNode(activity, "Initial Node");
		ActivityFinalNode finalnode = ActivityFactory.createActivityFinalNode(activity, "Activity Final Node");
		
		ActivityFactory.createControlFlow(activity, initialnode, callaction);
		ActivityFactory.createControlFlow(activity, callaction, finalnode);
		
		// DEBUG
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertNull(((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnode, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodeentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(initialnode, initialnodeentry.getNode());		
		assertEquals(activityentry, initialnodeentry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initialnode, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialnodeentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initialnode, ((StepEvent)eventlist.get(4)).getLocation());	
		assertNull(eventlist.get(4).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(callaction, ExecutionContext.getInstance().getEnabledNodes().get(0));			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(8, eventlist.size());
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent callactionentry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(callaction, callactionentry.getNode());		
		assertEquals(activityentry, callactionentry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(callaction, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(callactionentry, eventlist.get(6).getParent());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(callaction, ((StepEvent)eventlist.get(7)).getLocation());	
		assertNull(eventlist.get(7).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(finalnode, ExecutionContext.getInstance().getEnabledNodes().get(0));			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		ActivityExecution activityexe = (ActivityExecution)ExecutionContext.getInstance().getExtensionalValues().get(0);
		CallBehaviorActionActivation callactivation = null;
		for(int i=0;i<activityexe.activationGroup.nodeActivations.size();++i) {
			if(activityexe.activationGroup.nodeActivations.get(i) instanceof CallBehaviorActionActivation) {
				callactivation = (CallBehaviorActionActivation)activityexe.activationGroup.nodeActivations.get(i);
			}
		}
		assertEquals(1, callactivation.pinActivations.get(0).heldTokens.size());
		assertEquals(5, ((IntegerValue)(callactivation.pinActivations.get(0).heldTokens.get(0).getValue())).value);
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(11, eventlist.size());
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent finalnodeentry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(finalnode, finalnodeentry.getNode());		
		assertEquals(activityentry, finalnodeentry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(finalnode, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(finalnodeentry, eventlist.get(9).getParent());
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(10)).getActivity());	
		assertEquals(activityentry, eventlist.get(10).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
	}
	
	/**
	 * This test case is an variant of the test case 
	 * {@link #testCallBehaviorActionCallingOpaqueBehavior()}
	 * 
	 * It is tested, if the execution of an CallBehaviorAction 
	 * which calls an OpaqueBehavior also  works if the 
	 * CallBehaviorAction is the last action of the activity.
	 * Therewith it is primarily tested if after the execution of
	 * the CallBehaviorAction an ActivityExitEvent is fired instead
	 * of an StepEvent.
	 * 
	 * Activity: 
	 * InitialNode
	 * CallBehaviorAction (behavior = RETURN5, outputPin)
	 * 
	 * Activity ControlFlow:
	 * InitialNode --> CallBehaviorAction
	 */
	@Test
	public void testCallBehaviorActionCallingOpaqueBehavior2() {
		OpaqueBehavior return5behavior = new OpaqueBehavior();
		Parameter output = new Parameter();
		output.setDirection(ParameterDirectionKind.out);
		output.setName("result");
		return5behavior.ownedParameter.add(output);
		
		Return5BehaviorExecution return5execution = new Return5BehaviorExecution();
		return5execution.types.add(return5behavior);
		
		ExecutionContext.getInstance().locus.factory.addPrimitiveBehaviorPrototype(return5execution);		
		ExecutionContext.getInstance().opaqueBehaviors.put("RETURN5", return5behavior);		
		
		Activity activity = ActivityFactory.createActivity("TestCallBehaviorActionCallingOpaqueBehavior");
		//OpaqueBehavior return5behavior = ExecutionContext.getInstance().getOpaqueBehavior("RETURN5");
		
		CallBehaviorAction callaction = ActivityFactory.createCallBehaviorAction(activity, "Call Behavior RETURN 5", return5behavior);		
		OutputPin outputpin_callaction = new OutputPin();
		outputpin_callaction.setName("OutputPin (Call Behavior RETURN)");
		OutputPinList output_callaction = new OutputPinList();
		output_callaction.add(outputpin_callaction);
		callaction.result = output_callaction;
		callaction.output = output_callaction;
		
		InitialNode initialnode = ActivityFactory.createInitialNode(activity, "Initial Node");
		
		ActivityFactory.createControlFlow(activity, initialnode, callaction);
		
		// DEBUG
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertNull(((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initialnode, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodeentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(initialnode, initialnodeentry.getNode());		
		assertEquals(activityentry, initialnodeentry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initialnode, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialnodeentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initialnode, ((StepEvent)eventlist.get(4)).getLocation());	
		assertNull(eventlist.get(4).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(callaction, ExecutionContext.getInstance().getEnabledNodes().get(0));			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(8, eventlist.size());
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent callactionentry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(callaction, callactionentry.getNode());		
		assertEquals(activityentry, callactionentry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(callaction, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(callactionentry, eventlist.get(6).getParent());						
		assertTrue(eventlist.get(7) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(7)).getActivity());	
		assertEquals(activityentry, eventlist.get(7).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		//ActivityExecution activityexe = (ActivityExecution)ExecutionContext.getInstance().getExtensionalValues().get(0);
		int activityexecutionID = activityentry.getActivityExecutionID();		
		ActivityExecution activityexe = ExecutionContext.getInstance().activityExecutions.get(activityexecutionID);
		CallBehaviorActionActivation callactivation = null;
		for(int i=0;i<activityexe.activationGroup.nodeActivations.size();++i) {
			if(activityexe.activationGroup.nodeActivations.get(i) instanceof CallBehaviorActionActivation) {
				callactivation = (CallBehaviorActionActivation)activityexe.activationGroup.nodeActivations.get(i);
			}
		}
		assertEquals(1, callactivation.pinActivations.get(0).heldTokens.size());
		assertEquals(5, ((IntegerValue)(callactivation.pinActivations.get(0).heldTokens.get(0).getValue())).value);
	}
	
	/**
	 * This test case tests if the execution works with
	 * multiple initial enabled nodes.
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class2)
	 * (without edges)
	 */
	@Test
	public void testMoreThanOneInitialNode() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		
		Activity activity = ActivityFactory.createActivity("TestMoreThanOneInitialNode");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		// DEBUG
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertNull(((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(3, ExecutionContext.getInstance().getEnabledNodes().size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(create1));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(create2));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(create3));
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(create1);						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, createcl1entry.getNode());		
		assertEquals(activityentry, eventlist.get(2).getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(createcl1entry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(create1, ((StepEvent)eventlist.get(4)).getLocation());	
		assertNull(eventlist.get(4).getParent());
		
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes().size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(create2));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(create3));			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		Object_ o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(create2);					
	
		assertEquals(8, eventlist.size());
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl2entry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(create2, createcl2entry.getNode());	
		assertEquals(activityentry, createcl2entry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(createcl2entry, eventlist.get(6).getParent());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(create2, ((StepEvent)eventlist.get(7)).getLocation());	
		assertNull(eventlist.get(7).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(create3, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(2, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		o = (Object_)(e.get(1));
		assertEquals(1, o.types.size());
		assertEquals(class2, o.types.get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();					
		
		assertEquals(11, eventlist.size());
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent createcl3entry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(create3, createcl3entry.getNode());	
		assertEquals(activityentry, createcl3entry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(createcl3entry, eventlist.get(9).getParent());
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(10)).getActivity());	
		assertEquals(activityentry, eventlist.get(10).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(3, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		o = (Object_)(e.get(1));
		assertEquals(1, o.types.size());
		assertEquals(class2, o.types.get(0));
		o = (Object_)(e.get(2));
		assertEquals(1, o.types.size());
		assertEquals(class3, o.types.get(0));
	}
	
	/**
	 * This tests the execution of an CallBehaviorAction that calls an Activity
	 * and produces output
	 * 
	 * Activity 1 (Caller):
	 * ValueSpecificationAction (value="tanja")
	 * CallBehaviorAction (behavior = Activity2)
	 * AddStructuralFeatureValueAction (feature = Name)
	 * 
	 * Activity 1 ControlFlow:
	 * ValueSpecificationAction --> CallBehaviorAction
	 * 
	 * Activity 1 ObjectFlow:
	 * ValueSpecificationAction.result --> AddStructuralFeatureValueAction.value
	 * CallBehaviorAction.result.get(0) --> AddStructuralFeatureValueAction.object
	 * 
	 * Activity 2 (Callee, 1 parameter):
	 * ActivityParameterNode (direction = out)
	 * CreateObjectAction (class = Person)
	 * 
	 * Activity 2 ObjectFlow:
	 * CreateObjectAction.result --> ActivityParameterNode
	 */
	@Test
	public void testCallBehaviorActionWithActivityOutput() {
		Class_ class_person = ActivityFactory.createClass("Person");
		Property property_name = ActivityFactory.createProperty("Name", 0, 1, class_person);
		
		Activity activity2 = ActivityFactory.createActivity("TestCallBehaviorActionWithActivityOutput Callee Activity 2");
		Parameter param = ActivityFactory.createParameter(activity2, "Result Parameter (" + activity2.name + ")", ParameterDirectionKind.out);				
		ActivityParameterNode paramnode = ActivityFactory.createActivityParameterNode(activity2, "ParameterNode", param);
		CreateObjectAction create = ActivityFactory.createCreateObjectAction(activity2, "CreateObject Person", class_person);		
		ActivityFactory.createObjectFlow(activity2, create.result, paramnode);
		
		Activity activity1 = ActivityFactory.createActivity("TestCallBehaviorActionWithActivityOutput Caller Activity 1");
		ValueSpecificationAction vs = ActivityFactory.createValueSpecificationAction(activity1, "ValueSpecification tanja", "tanja");
		CallBehaviorAction call = ActivityFactory.createCallBehaviorAction(activity1, "Call Activity 2", activity2, 1);		
		AddStructuralFeatureValueAction add = ActivityFactory.createAddStructuralFeatureValueAction(activity1, "AddStructuralFeature name", property_name);
						
		ActivityFactory.createControlFlow(activity1, vs, call);
		ActivityFactory.createObjectFlow(activity1, vs.result, add.value);
		ActivityFactory.createObjectFlow(activity1, call.result.get(0), add.object);
		
		// DEBUG
		ExecutionContext.getInstance().debug(activity1, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent entrycaller = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity1, entrycaller.getActivity());		
		assertNull(entrycaller.getParent());		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertNull(((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(vs, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodecallerentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(vs, initialnodecallerentry.getNode());		
		assertEquals(entrycaller, eventlist.get(2).getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(vs, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialnodecallerentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(vs, ((StepEvent)eventlist.get(4)).getLocation());	
		assertNull(eventlist.get(4).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(call, ExecutionContext.getInstance().getEnabledNodes().get(0));			
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());				
				
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();						
		
		assertEquals(8, eventlist.size());
		ActivityNodeEntryEvent callactionentry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		assertEquals(call, callactionentry.getNode());
		assertEquals(entrycaller, callactionentry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityEntryEvent);
		ActivityEntryEvent entrycallee = (ActivityEntryEvent)eventlist.get(6);
		assertEquals(activity2, entrycallee.getActivity());
		assertEquals(callactionentry, entrycallee.getParent());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(7)).getLocation());
		assertNull(eventlist.get(7).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(create, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();				
		
		assertEquals(13, eventlist.size());
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialnodecalleeentry =(ActivityNodeEntryEvent)eventlist.get(8); 
		assertEquals(create, initialnodecalleeentry.getNode());	
		assertEquals(entrycallee, initialnodecalleeentry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(create, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(initialnodecalleeentry, eventlist.get(9).getParent());				
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		assertEquals(activity2, ((ActivityExitEvent)eventlist.get(10)).getActivity());
		assertEquals(entrycallee, eventlist.get(10).getParent());
		
		assertTrue(eventlist.get(11) instanceof ActivityNodeExitEvent);
		assertEquals(call, ((ActivityNodeExitEvent)eventlist.get(11)).getNode());
		assertEquals(callactionentry, eventlist.get(11).getParent());
		
		assertTrue(eventlist.get(12) instanceof StepEvent);
		assertEquals(call, ((StepEvent)eventlist.get(12)).getLocation());	
		assertNull(eventlist.get(12).getParent());
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		Object_ o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(add, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep();
		
		assertEquals(16, eventlist.size());
		assertTrue(eventlist.get(13) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent addentry =(ActivityNodeEntryEvent)eventlist.get(13); 
		assertEquals(add, addentry.getNode());	
		assertEquals(entrycaller, addentry.getParent());
		assertTrue(eventlist.get(14) instanceof ActivityNodeExitEvent);
		assertEquals(add, ((ActivityNodeExitEvent)eventlist.get(14)).getNode());
		assertEquals(addentry, eventlist.get(14).getParent());				
		assertTrue(eventlist.get(15) instanceof ActivityExitEvent);
		assertEquals(activity1, ((ActivityExitEvent)eventlist.get(15)).getActivity());
		assertEquals(entrycaller, eventlist.get(15).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class_person, o.types.get(0));
		assertEquals(1, o.featureValues.size());
		assertEquals(property_name, o.featureValues.get(0).feature);
		assertEquals(1, o.featureValues.get(0).values.size());
		assertTrue(o.featureValues.get(0).values.get(0) instanceof StringValue);
		assertEquals("tanja", ((StringValue)o.featureValues.get(0).values.get(0)).value);
	}
	
	/**
	 * Tests the retrieving of the output of an activity execution.
	 * 
	 * Activity (1 parameter):
	 * CreateObjectAction (class = Class1)
	 * ActivityParameterNode (direction=out)
	 * 
	 * Activity ObjectFlow:
	 * CreateObjectAction.result --> ActivityParameterNode
	 */
	@Test
	public void testActivityWithOutput() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Activity activity = ActivityFactory.createActivity("TestActivityWithOutput");
		Parameter param = ActivityFactory.createParameter(activity, "OutputParameter", ParameterDirectionKind.out);
		ActivityParameterNode paramnode = ActivityFactory.createActivityParameterNode(activity, "OutputParameterNode", param);
		CreateObjectAction action = ActivityFactory.createCreateObjectAction(activity, "CreateObjectAction", class1);
		ActivityFactory.createObjectFlow(activity, action.result, paramnode);
		
		// DEBUG
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());				
		assertNull(activityentry.getParent());		
		int activityexecutionID = activityentry.getActivityExecutionID();		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertNull(((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(action, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID);						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent actionentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(action, actionentry.getNode());		
		assertEquals(activityentry, eventlist.get(2).getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(action, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(actionentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(4)).getActivity());	
		assertEquals(activityentry, eventlist.get(4).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());		
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		Object_ o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		ParameterValueList output = ExecutionContext.getInstance().getActivityOutput(activityexecutionID);
		assertEquals(1, output.size());
		ParameterValue outputvalue = output.get(0);
		assertEquals(param, outputvalue.parameter);
		ValueList values = output.get(0).values;
		assertEquals(1, values.size());
		Value value = values.get(0);
		assertTrue(value instanceof Reference);
		Object_ valueobject = ((Reference)value).referent;		
		assertEquals(1, valueobject.types.size());
		assertEquals(class1, valueobject.types.get(0));
	}
	
	/**
	 * Tests if an ActivityExecution is destroyed at the end of its execution
	 * 
	 * Activity:
	 * CreateObjectAction
	 */
	@Test
	public void testDestroyingOfActivity() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Activity activity = ActivityFactory.createActivity("TestDestroyingOfActivity");
		CreateObjectAction action = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		
		// DEBUG
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());				
		assertNull(activityentry.getParent());	
		int activityexecutionID = activityentry.getActivityExecutionID();		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertNull(((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(action, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		/*
		 * At the locus only the ActivityExecution object exists
		 * and the type of this object is set properly
		 */
		assertEquals(1, ExecutionContext.getInstance().locus.extensionalValues.size());
		assertTrue(ExecutionContext.getInstance().locus.extensionalValues.get(0) instanceof ActivityExecution);
		ActivityExecution execution = (ActivityExecution)ExecutionContext.getInstance().locus.extensionalValues.get(0);
		assertEquals(activity, (Activity) execution.types.get(0));
				
		assertEquals(1, execution.types.size());
		assertEquals(activity, execution.types.get(0));
		assertEquals(execution, ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID);						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent actionentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(action, actionentry.getNode());		
		assertEquals(activityentry, eventlist.get(2).getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(action, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(actionentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(4)).getActivity());	
		assertEquals(activityentry, eventlist.get(4).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());		
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		Object_ o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		/*
		 * At the locus is only the created object present, the ActivityExecution was removed.
		 * Also the ActivityExecution object was destroyed, i.e., the type was removed
		 */
		assertEquals(1, ExecutionContext.getInstance().locus.extensionalValues.size());
		assertTrue(ExecutionContext.getInstance().locus.extensionalValues.get(0) instanceof Object_);
		o = (Object_)ExecutionContext.getInstance().locus.extensionalValues.get(0);
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		assertEquals(0, ExecutionContext.getInstance().activityExecutions.get(activityexecutionID).types.size());
		assertEquals(0, execution.types.size());
	}	
	
	/**
	 * Tests the clean up of the ExecutionContext after an Activity has been executed, 
	 * i.e., the clean up of attribute HashMap<ActivityExecution, List<ActivationConsumedTokens>> enabledActivations
	 * is tested
	 * 
	 * Activity:
	 * CreateObjectAction
	 */
	@Test
	public void testCleanUpOfExecutionContext() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Activity activity = ActivityFactory.createActivity("TestDestroyingOfActivity");
		CreateObjectAction action = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		
		// DEBUG
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		int activityexecutionID = activityentry.getActivityExecutionID();		

		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(action, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		ExtensionalValueList e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(0, e.size());
		
		/*
		 * The ExecutionContext contains the ActivityExecution object, a list for its enabled nodes, and
		 * no output for this execution
		 */
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));	
		
		ActivityExecution execution = ExecutionContext.getInstance().activityExecutions.get(activityexecutionID);		
		assertTrue(ExecutionContext.getInstance().enabledActivations.containsKey(execution));
		assertNotNull(ExecutionContext.getInstance().enabledActivations.get(execution));
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.get(execution).size());
		
		assertFalse(ExecutionContext.getInstance().activityExecutionOutput.containsKey(execution));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID);						
	
		assertEquals(5, eventlist.size());
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(4)).getActivity());	
		assertEquals(activityentry, eventlist.get(4).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());		
		
		e = extensionalValueLists.get(extensionalValueLists.size()-1);
		assertEquals(1, e.size());
		assertTrue(e.get(0) instanceof Object_);		
		Object_ o = (Object_)(e.get(0));
		assertEquals(1, o.types.size());
		assertEquals(class1, o.types.get(0));
		
		/*
		 * The ExecutionContext still contains the ActivityExecution object, 
		 * contains the output (in this case an empty list), 
		 * but not a list for its enabled nodes
		 */
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));	
		
		execution = ExecutionContext.getInstance().activityExecutions.get(activityexecutionID);
		
		assertFalse(ExecutionContext.getInstance().enabledActivations.containsKey(execution));
		
		assertTrue(ExecutionContext.getInstance().activityExecutionOutput.containsKey(execution));
		assertNotNull(ExecutionContext.getInstance().activityExecutionOutput.get(execution));
		assertEquals(0, ExecutionContext.getInstance().activityExecutionOutput.get(execution).size());		
	}
	
	/**
	 * This test case is an extension of the test case
	 * {@link #testCleanUpOfExecutionContext()}
	 * and tests additionally the clean up if an other activity is called using the
	 * CallBehaviorAction
	 * 
	 * Activity 1 (Caller):
	 * InitialNode
	 * ForkNode
	 * MergeNode
	 * CallBehaviorAction
	 * 
	 * Activity 1 ControlFlow:
	 * InitialNode --> ForkNode
	 * ForkNode --> CallBehaviorAction
	 * ForkNode --> MergeNode
	 * 
	 * Activity 2 (Callee):
	 * MergeNode
	 */
	//@Test
	public void testCleanUpOfExecutionContextWithCallBehaviorAction(){
		Activity activity2 = ActivityFactory.createActivity("TestCleanUpOfExecutionContextWithCallBehaviorAction Callee");
		MergeNode merge2 = ActivityFactory.createMergeNode(activity2, "MergeNode Callee");
		
		Activity activity = ActivityFactory.createActivity("TestCleanUpOfExecutionContextWithCallBehaviorAction Caller");
		InitialNode initial = ActivityFactory.createInitialNode(activity, "InitialNode");
		ForkNode fork = ActivityFactory.createForkNode(activity, "ForkNode"); 
		MergeNode merge = ActivityFactory.createMergeNode(activity, "MergeNode");
		CallBehaviorAction call = ActivityFactory.createCallBehaviorAction(activity, "CallBehaviorAction", activity2);
		ActivityFactory.createControlFlow(activity, initial, fork);
		ActivityFactory.createControlFlow(activity, fork, call);
		ActivityFactory.createControlFlow(activity, fork, merge);
		
		//DEBUG
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
		
		assertEquals(2, eventlist.size());							
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());				
		assertNull(activityentry.getParent());		
		int activityexecutionID = activityentry.getActivityExecutionID();		
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertNull(((StepEvent)eventlist.get(1)).getLocation());	
		assertNull(eventlist.get(1).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(initial, ExecutionContext.getInstance().getEnabledNodes().get(0));	
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		assertEquals(1, ExecutionContext.getInstance().activityExecutions.keySet().size());
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));		
		ActivityExecution executionactivity1 = ExecutionContext.getInstance().activityExecutions.get(activityexecutionID);			
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.keySet().size());
		assertTrue(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity1));
		assertNotNull(ExecutionContext.getInstance().enabledActivations.get(executionactivity1));
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.get(executionactivity1).size());
		assertEquals(0, ExecutionContext.getInstance().activityExecutionOutput.keySet().size());
		assertFalse(ExecutionContext.getInstance().activityExecutionOutput.containsKey(executionactivity1));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID);						
	
		assertEquals(5, eventlist.size());
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(initial, initialentry.getNode());		
		assertEquals(activityentry, eventlist.get(2).getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initial, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialentry, eventlist.get(3).getParent());
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initial, ((StepEvent)eventlist.get(4)).getLocation());	
		assertEquals(null, eventlist.get(4).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(fork, ExecutionContext.getInstance().getEnabledNodes().get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
				
		assertEquals(1, ExecutionContext.getInstance().activityExecutions.keySet().size());
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));		
		assertEquals(executionactivity1, ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.keySet().size());
		assertTrue(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity1));
		assertNotNull(ExecutionContext.getInstance().enabledActivations.get(executionactivity1));
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.get(executionactivity1).size());		
		assertEquals(0, ExecutionContext.getInstance().activityExecutionOutput.keySet().size());
		assertFalse(ExecutionContext.getInstance().activityExecutionOutput.containsKey(executionactivity1));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID);						
	
		assertEquals(8, eventlist.size());
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent forkentry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(fork, forkentry.getNode());		
		assertEquals(activityentry, eventlist.get(5).getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(fork, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(forkentry, eventlist.get(6).getParent());
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(fork, ((StepEvent)eventlist.get(7)).getLocation());	
		assertEquals(null, eventlist.get(7).getParent());
		
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes().size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(call));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(merge));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());

		assertEquals(1, ExecutionContext.getInstance().activityExecutions.keySet().size());
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));		
		assertEquals(executionactivity1, ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.keySet().size());
		assertTrue(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity1));
		assertNotNull(ExecutionContext.getInstance().enabledActivations.get(executionactivity1));
		assertEquals(2, ExecutionContext.getInstance().enabledActivations.get(executionactivity1).size());		
		assertEquals(0, ExecutionContext.getInstance().activityExecutionOutput.keySet().size());
		assertFalse(ExecutionContext.getInstance().activityExecutionOutput.containsKey(executionactivity1));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID, call);						
	
		assertEquals(11, eventlist.size());
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent callentry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(call, callentry.getNode());	
		assertTrue(eventlist.get(9) instanceof ActivityEntryEvent);
		ActivityEntryEvent activity2entry = (ActivityEntryEvent)eventlist.get(9);
		int activity2executionID = activity2entry.getActivityExecutionID();
		assertEquals(activity2, activity2entry.getActivity());
		assertEquals(callentry, activity2entry.getParent());
		assertTrue(eventlist.get(10) instanceof StepEvent);
		assertEquals(null, ((StepEvent)eventlist.get(10)).getLocation());	
		assertEquals(null, eventlist.get(10).getParent());
		
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes().size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(merge));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes().contains(merge2));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		assertEquals(2, ExecutionContext.getInstance().activityExecutions.keySet().size());
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activity2executionID));		
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activity2executionID));
		ActivityExecution executionactivity2 = ExecutionContext.getInstance().activityExecutions.get(activity2executionID);
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.keySet().size());
		assertTrue(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity1));
		assertNotNull(ExecutionContext.getInstance().enabledActivations.get(executionactivity1));
		assertEquals(2, ExecutionContext.getInstance().enabledActivations.get(executionactivity1).size());	
		assertFalse(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity2));
		assertEquals(0, ExecutionContext.getInstance().activityExecutionOutput.keySet().size());
		assertFalse(ExecutionContext.getInstance().activityExecutionOutput.containsKey(executionactivity1));
		
		// NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID, merge);						
		
		assertEquals(14, eventlist.size());
		assertTrue(eventlist.get(11) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent mergeentry = (ActivityNodeEntryEvent)eventlist.get(11);
		assertEquals(merge, mergeentry.getNode());	
		assertEquals(activityentry, mergeentry.getParent());
		assertTrue(eventlist.get(12) instanceof ActivityNodeExitEvent);
		assertEquals(merge, ((ActivityNodeExitEvent)eventlist.get(12)).getNode());
		assertEquals(mergeentry, eventlist.get(12).getParent());
		assertTrue(eventlist.get(13) instanceof StepEvent);
		assertEquals(merge, ((StepEvent)eventlist.get(13)).getLocation());	
		assertEquals(null, eventlist.get(13).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes().size());
		assertEquals(merge2, ExecutionContext.getInstance().getEnabledNodes().get(0));		
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		assertEquals(2, ExecutionContext.getInstance().activityExecutions.keySet().size());
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activity2executionID));		
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activityexecutionID));
		assertNotNull(ExecutionContext.getInstance().activityExecutions.get(activity2executionID));
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.keySet().size());
		assertTrue(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity1));
		assertNotNull(ExecutionContext.getInstance().enabledActivations.get(executionactivity1));
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.get(executionactivity1).size());	
		assertFalse(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity2));
		assertEquals(0, ExecutionContext.getInstance().activityExecutionOutput.keySet().size());
		assertFalse(ExecutionContext.getInstance().activityExecutionOutput.containsKey(executionactivity1));
		
		//NEXT STEP
		ExecutionContext.getInstance().nextStep(activityexecutionID);						
		
		assertEquals(19, eventlist.size());
		assertTrue(eventlist.get(14) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent merge2entry = (ActivityNodeEntryEvent)eventlist.get(14);
		assertEquals(merge2, merge2entry.getNode());	
		assertEquals(activity2entry, merge2entry.getParent());
		assertTrue(eventlist.get(15) instanceof ActivityNodeExitEvent);
		assertEquals(merge2, ((ActivityNodeExitEvent)eventlist.get(15)).getNode());
		assertEquals(merge2entry, eventlist.get(15).getParent());
		assertTrue(eventlist.get(16) instanceof ActivityExitEvent);
		assertEquals(activity2, ((ActivityExitEvent)eventlist.get(16)).getActivity());	
		assertEquals(activity2entry, eventlist.get(16).getParent());
		
		assertTrue(eventlist.get(17) instanceof ActivityNodeExitEvent);
		assertEquals(call, ((ActivityNodeExitEvent)eventlist.get(17)).getNode());
		assertEquals(callentry, eventlist.get(17).getParent());		
		assertTrue(eventlist.get(18) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(18)).getActivity());	
		assertEquals(activityentry, eventlist.get(18).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes().size());		
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());

		assertEquals(1, ExecutionContext.getInstance().activityExecutions.keySet().size());
		assertTrue(ExecutionContext.getInstance().activityExecutions.containsKey(activityexecutionID));
		assertFalse(ExecutionContext.getInstance().activityExecutions.containsKey(activity2executionID));		
		assertEquals(1, ExecutionContext.getInstance().enabledActivations.keySet().size());
		assertTrue(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity1));
		assertNotNull(ExecutionContext.getInstance().enabledActivations.get(executionactivity1));
		assertEquals(0, ExecutionContext.getInstance().enabledActivations.get(executionactivity1).size());	
		assertFalse(ExecutionContext.getInstance().enabledActivations.containsKey(executionactivity2));
		assertEquals(0, ExecutionContext.getInstance().activityExecutionOutput.keySet().size());
		assertFalse(ExecutionContext.getInstance().activityExecutionOutput.containsKey(executionactivity1));
	}
	
	
	
	@Override
	public void notify(Event event) {		
		eventlist.add(event);
		
		if(event instanceof StepEvent || event instanceof ActivityExitEvent) {
			ExtensionalValueList list = new ExtensionalValueList();
			for(int i=0;i<ExecutionContext.getInstance().getExtensionalValues().size();++i) {
				if(ExecutionContext.getInstance().getExtensionalValues().get(i).getClass() == Object_.class) {
					//list.add(ExecutionContext.getInstance().getExtensionalValues().get(i));
					list.add(copyObject((Object_)ExecutionContext.getInstance().getExtensionalValues().get(i)));
				}
			}
			extensionalValueLists.add(list);
		}
	}
		
	private Object_ copyObject(Object_ object) {
		Object_ newObject = new Object_();
		for (int i = 0; i < object.types.size(); i++) {
			newObject.types.addValue(object.types.getValue(i));
		}
		
		for (int i = 0; i < object.featureValues.size(); i++) {
			FeatureValue featureValue = object.featureValues.getValue(i);
			FeatureValue newFeatureValue = new FeatureValue();
			newFeatureValue.feature = featureValue.feature;
			newFeatureValue.position = featureValue.position;
			for(int j=0;j<featureValue.values.size();++j) {
				if(featureValue.values.get(j) instanceof PrimitiveValue) {
					newFeatureValue.values.add(featureValue.values.get(j).copy());
				} else if(featureValue.values.get(j) instanceof Object_) {
					newFeatureValue.values.add(copyObject((Object_)featureValue.values.get(j)));
				} 
			}			
			newObject.featureValues.add(newFeatureValue);						
		}
		
		return newObject;		
	}
	
}
