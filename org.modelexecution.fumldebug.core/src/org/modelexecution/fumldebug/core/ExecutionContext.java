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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.modelexecution.fumldebug.core.impl.ExecutionEventProviderImpl;

import fUML.Library.IntegerFunctions;
import fUML.Semantics.Actions.BasicActions.ActionActivation;
import fUML.Semantics.Activities.IntermediateActivities.ActivityExecution;
import fUML.Semantics.Activities.IntermediateActivities.ActivityNodeActivation;
import fUML.Semantics.Classes.Kernel.ExtensionalValueList;
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.RedefinitionBasedDispatchStrategy;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
import fUML.Semantics.CommonBehaviors.Communications.FIFOGetNextEventStrategy;
import fUML.Semantics.Loci.LociL1.Executor;
import fUML.Semantics.Loci.LociL1.FirstChoiceStrategy;
import fUML.Semantics.Loci.LociL1.Locus;
import fUML.Semantics.Loci.LociL3.ExecutionFactoryL3;
import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
import fUML.Syntax.Classes.Kernel.PrimitiveType;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.Behavior;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.FunctionBehavior;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;

public class ExecutionContext {

	private static ExecutionContext instance = new ExecutionContext();
	
	private ExecutionEventProvider eventprovider;
	
	protected Locus locus = null;
		
	private PrimitiveType typeBoolean = null;
	private PrimitiveType typeInteger = null;
	
	protected Hashtable<String, OpaqueBehavior> opaqueBehaviors = new Hashtable<String, OpaqueBehavior>();
	
	private boolean isDebugMode = false;
	
	protected HashMap<ActivityExecution, List<ActivationConsumedTokens>> enabledActivations = new HashMap<ActivityExecution, List<ActivationConsumedTokens>>(); 
	
	protected HashMap<ActivityExecution, ParameterValueList> activityExecutionOutput = new HashMap<ActivityExecution, ParameterValueList>();
	
	protected HashMap<Integer, ActivityExecution> activityExecutions = new HashMap<Integer, ActivityExecution>(); 
	
	protected ExecutionContext()
	{
		/*
		 * Locus initialization
		 */
		this.locus = new Locus();
		this.locus.setFactory(new ExecutionFactoryL3());  // Uses local subclass for ExecutionFactory
		this.locus.setExecutor(new Executor());

		this.locus.factory.setStrategy(new RedefinitionBasedDispatchStrategy());
		this.locus.factory.setStrategy(new FIFOGetNextEventStrategy());
		this.locus.factory.setStrategy(new FirstChoiceStrategy());
	
		typeBoolean = this.createPrimitiveType("Boolean");
		this.createPrimitiveType("String");
		typeInteger = this.createPrimitiveType("Integer");
		this.createPrimitiveType("UnlimitedNatural");
		
		/*
		 * Initialization of primitive behaviors 
		 */
		IntegerFunctions integerFunctions = new IntegerFunctions(typeInteger, typeBoolean, this.locus.factory);
		addFunctionBehavior(integerFunctions.integerGreater);
	}	
	
	public static ExecutionContext getInstance(){
		return instance;
	}
	
	private PrimitiveType createPrimitiveType(String name) {
		PrimitiveType type = new PrimitiveType();
		type.name = name;
		this.locus.factory.addBuiltInType(type);
		return type;
	}
		
	/**
	 * TODO
	 * Does it make sense to create one event provider per activity execution
	 * and to maintain a dictionary or something for active executions?
	 * @return
	 */
	public ExecutionEventProvider getExecutionEventProvider(){
		if(this.eventprovider == null) {
			this.eventprovider = new ExecutionEventProviderImpl();
		}
		return this.eventprovider;
	}
		
	public ParameterValueList execute(Behavior behavior, Object_ context, ParameterValueList inputs) {
		isDebugMode = false;		
		return this.locus.executor.execute(behavior, context, inputs);
	}
	
	public void debug(Behavior behavior, Object_ context, ParameterValueList inputs) {
		isDebugMode = true;
		this.locus.executor.execute(behavior, context, inputs);
	}
	
	/**
	 * This method only exists because of test cases
	 */
	protected void nextStep() {
		//TODO remove
		nextStep(this.activityExecutions.keySet().iterator().next());
	}
	
	/**
	 * This method only exists because of test cases
	 * @param node
	 */
	protected void nextStep(ActivityNode node) {
		//TODO remove
		nextStep(this.activityExecutions.keySet().iterator().next(), node);
	}

	public void nextStep(int executionID) {
		nextStep(executionID, StepDepth.STEP_NODE);
	}
	
	public void nextStep(int executionID, ActivityNode node) {
		nextStep(executionID, StepDepth.STEP_NODE, node);
	}
	
	public void nextStep(int executionID, StepDepth depth) {						
		nextStep(executionID, depth, null);
	}
	
	public void nextStep(int executionID, StepDepth depth, ActivityNode node) {	
		ActivationConsumedTokens nextnode = getNextNode(executionID, node);
		nextStep(nextnode);		
	}			
					
	private ActivationConsumedTokens getNextNode(int executionID, ActivityNode node) {
		ActivityExecution activityExecution = activityExecutions.get(executionID);
		List<ActivationConsumedTokens> activationConsumedTokens = enabledActivations.get(activityExecution);
		
		if(activationConsumedTokens.size() == 0) {
			return null;
		}
		
		ActivationConsumedTokens nextnode = null;
		
		if(node == null) {
			nextnode = activationConsumedTokens.remove(0);
		} else {
			for(int i=0; i<activationConsumedTokens.size(); ++i) {
				if(activationConsumedTokens.get(i).getActivation().node == node) {
					nextnode = activationConsumedTokens.remove(i);
				}
			}			
			if(nextnode == null) {
				nextnode = activationConsumedTokens.remove(0);
			}					
		}
		return nextnode;
	}
	
	private void nextStep(ActivationConsumedTokens nextnode) {
		ActivityNodeActivation activation = nextnode.getActivation();
		if(activation instanceof ActionActivation) {
			((ActionActivation)activation).firing = true;
		}	
		activation.fire(nextnode.getTokens());
	}
	
	private void addFunctionBehavior(FunctionBehavior behavior) { 
		opaqueBehaviors.put(behavior.name, behavior);
	}
	
	public OpaqueBehavior getOpaqueBehavior(String name) {
		if(opaqueBehaviors.containsKey(name)) {
			return opaqueBehaviors.get(name);
		}
		return null;
	}
	
	public ExtensionalValueList getExtensionalValues() {
		return locus.extensionalValues;
	}
	
	public void reset() {
		locus.extensionalValues = new ExtensionalValueList();
	}
	
	protected boolean isDebugMode() {
		return isDebugMode;
	}
	
	/**
	 * This function is only present because of test cases that use the
	 * getEnabledNodes() function without arguments
	 * @return
	 */
	protected List<ActivityNode> getEnabledNodes() {
		//TODO remove
		return getEnabledNodes(this.activityExecutions.keySet().iterator().next());
	}
	
	public List<ActivityNode> getEnabledNodes(int executionID) {
		ActivityExecution activityExecution = activityExecutions.get(executionID);
		List<ActivationConsumedTokens> activationConsumedTokens = enabledActivations.get(activityExecution);		
	
		List<ActivityNode> nodes = new ArrayList<ActivityNode>();
		
		if(activationConsumedTokens != null) {
			for(int i=0;i<activationConsumedTokens.size();++i) {
				ActivityNode node = activationConsumedTokens.get(i).getActivation().node;
				if(node != null) {					
					nodes.add(node);
				}
			}
		}
		return nodes;
	}
	
	public ParameterValueList getActivityOutput(int executionID) {
		ActivityExecution execution = this.activityExecutions.get(executionID);
		ParameterValueList output = this.activityExecutionOutput.get(execution);
		return output;
	}
}
