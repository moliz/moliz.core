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
import java.util.List;

import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityExitEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeExitEvent;
import org.modelexecution.fumldebug.core.event.BreakpointEvent;
import org.modelexecution.fumldebug.core.event.Event;
import org.modelexecution.fumldebug.core.event.ExtensionalValueEvent;
import org.modelexecution.fumldebug.core.event.ExtensionalValueEventType;
import org.modelexecution.fumldebug.core.event.FeatureValueEvent;
import org.modelexecution.fumldebug.core.event.SuspendEvent;
import org.modelexecution.fumldebug.core.event.impl.ActivityEntryEventImpl;
import org.modelexecution.fumldebug.core.event.impl.ActivityExitEventImpl;
import org.modelexecution.fumldebug.core.event.impl.ActivityNodeEntryEventImpl;
import org.modelexecution.fumldebug.core.event.impl.ActivityNodeExitEventImpl;
import org.modelexecution.fumldebug.core.event.impl.BreakpointEventImpl;
import org.modelexecution.fumldebug.core.event.impl.ExtensionalValueEventImpl;
import org.modelexecution.fumldebug.core.event.impl.FeatureValueEventImpl;
import org.modelexecution.fumldebug.core.event.impl.SuspendEventImpl;

import fUML.Debug;
import fUML.Semantics.Actions.BasicActions.ActionActivation;
import fUML.Semantics.Actions.BasicActions.CallActionActivation;
import fUML.Semantics.Actions.BasicActions.CallBehaviorActionActivation;
import fUML.Semantics.Actions.BasicActions.CallOperationActionActivation;
import fUML.Semantics.Actions.BasicActions.OutputPinActivation;
import fUML.Semantics.Actions.BasicActions.OutputPinActivationList;
import fUML.Semantics.Actions.BasicActions.PinActivation;
import fUML.Semantics.Actions.CompleteActions.ReclassifyObjectActionActivation;
import fUML.Semantics.Actions.IntermediateActions.AddStructuralFeatureValueActionActivation;
import fUML.Semantics.Actions.IntermediateActions.ReadStructuralFeatureActionActivation;
import fUML.Semantics.Actions.IntermediateActions.RemoveStructuralFeatureValueActionActivation;
import fUML.Semantics.Actions.IntermediateActions.StructuralFeatureActionActivation;
import fUML.Semantics.Activities.CompleteStructuredActivities.StructuredActivityNodeActivation;
import fUML.Semantics.Activities.CompleteStructuredActivities.ClauseActivation;
import fUML.Semantics.Activities.CompleteStructuredActivities.ConditionalNodeActivation;
import fUML.Semantics.Activities.CompleteStructuredActivities.ClauseActivationList;
import fUML.Semantics.Activities.CompleteStructuredActivities.LoopNodeActivation;
import fUML.Semantics.Activities.CompleteStructuredActivities.ValuesList;
import fUML.Semantics.Activities.CompleteStructuredActivities.Values;
import fUML.Semantics.Activities.ExtraStructuredActivities.ExpansionActivationGroup;
import fUML.Semantics.Activities.ExtraStructuredActivities.ExpansionActivationGroupList;
import fUML.Semantics.Activities.ExtraStructuredActivities.ExpansionRegionActivation;
import fUML.Semantics.Activities.IntermediateActivities.ActivityEdgeInstance;
import fUML.Semantics.Activities.IntermediateActivities.ActivityExecution;
import fUML.Semantics.Activities.IntermediateActivities.ActivityNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.ActivityNodeActivationGroup;
import fUML.Semantics.Activities.IntermediateActivities.ActivityNodeActivationList;
import fUML.Semantics.Activities.IntermediateActivities.ActivityParameterNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.ActivityParameterNodeActivationList;
import fUML.Semantics.Activities.IntermediateActivities.ControlNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.DecisionNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.ForkNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.ObjectNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.ObjectToken;
import fUML.Semantics.Activities.IntermediateActivities.Token;
import fUML.Semantics.Activities.IntermediateActivities.TokenList;
import fUML.Semantics.Classes.Kernel.BooleanValue;
import fUML.Semantics.Classes.Kernel.CompoundValue;
import fUML.Semantics.Classes.Kernel.ExtensionalValue;
import fUML.Semantics.Classes.Kernel.ExtensionalValueList;
import fUML.Semantics.Classes.Kernel.FeatureValue;
import fUML.Semantics.Classes.Kernel.FeatureValueList;
import fUML.Semantics.Classes.Kernel.Link;
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.Reference;
import fUML.Semantics.Classes.Kernel.Value;
import fUML.Semantics.Classes.Kernel.ValueList;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.Execution;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.OpaqueBehaviorExecution;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValue;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
import fUML.Semantics.Loci.LociL1.Executor;
import fUML.Semantics.Loci.LociL1.Locus;
import fUML.Semantics.Loci.LociL1.SemanticVisitor;
import fUML.Semantics.Loci.LociL1.ChoiceStrategy;
import fUML.Syntax.Actions.BasicActions.CallAction;
import fUML.Syntax.Actions.BasicActions.CallBehaviorAction;
import fUML.Syntax.Actions.BasicActions.OutputPin;
import fUML.Syntax.Actions.BasicActions.OutputPinList;
import fUML.Syntax.Actions.BasicActions.Pin;
import fUML.Syntax.Actions.IntermediateActions.StructuralFeatureAction;
import fUML.Syntax.Activities.CompleteStructuredActivities.Clause;
import fUML.Syntax.Activities.CompleteStructuredActivities.ClauseList;
import fUML.Syntax.Activities.CompleteStructuredActivities.ConditionalNode;
import fUML.Syntax.Activities.CompleteStructuredActivities.ExecutableNode;
import fUML.Syntax.Activities.CompleteStructuredActivities.ExecutableNodeList;
import fUML.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
import fUML.Syntax.Activities.CompleteStructuredActivities.LoopNode;
import fUML.Syntax.Activities.ExtraStructuredActivities.ExpansionNode;
import fUML.Syntax.Activities.ExtraStructuredActivities.ExpansionNodeList;
import fUML.Syntax.Activities.ExtraStructuredActivities.ExpansionRegion;
import fUML.Syntax.Activities.IntermediateActivities.Activity;
import fUML.Syntax.Activities.IntermediateActivities.ActivityEdgeList;
import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
import fUML.Syntax.Activities.IntermediateActivities.ActivityNodeList;
import fUML.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
import fUML.Syntax.Classes.Kernel.Class_;
import fUML.Syntax.Classes.Kernel.Class_List;
import fUML.Syntax.Classes.Kernel.Element;
import fUML.Syntax.Classes.Kernel.Property;
import fUML.Syntax.Classes.Kernel.StructuralFeature;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.Behavior;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;

public aspect EventEmitterAspect implements ExecutionEventListener {

	private ExecutionEventProvider eventprovider = null;
	private List<Event> eventlist = new ArrayList<Event>();

	public EventEmitterAspect() {
		eventprovider = ExecutionContext.getInstance()
				.getExecutionEventProvider();
		eventprovider.addEventListener(this);
	}

	private pointcut activityExecution(ActivityExecution execution) : call (void Execution.execute()) && withincode(ParameterValueList Executor.execute(Behavior, Object_, ParameterValueList)) && target(execution);

	private pointcut inStepwiseExecutionMode() : cflow(execution(void ExecutionContext.executeStepwise(Behavior, Object_, ParameterValueList)));

	private pointcut activityExecutionInStepwiseExecutionMode(
			ActivityExecution execution) :  activityExecution(execution) && inStepwiseExecutionMode();

	private pointcut inExecutionMode() : cflow(execution(void ExecutionContext.execute(Behavior, Object_, ParameterValueList)));

	private pointcut activityExecutionInExecutionMode(
			ActivityExecution execution) :  activityExecution(execution) && inExecutionMode();

	/**
	 * Handling of ActivityEntryEvent in stepwise execution mode
	 * 
	 * @param execution
	 *            Execution object of the executed behavior
	 */
	before(ActivityExecution execution) : activityExecutionInStepwiseExecutionMode(execution) {
		handleNewActivityExecution(execution, null, null);
	}

	/**
	 * Handling of ActivityEntryEvent in execution mode
	 * 
	 * @param execution
	 *            Execution object of the executed behavior
	 */
	before(ActivityExecution execution) : activityExecutionInExecutionMode(execution) {
		ExecutionContext.getInstance()
				.setExecutionInResumeMode(execution, true);
		handleNewActivityExecution(execution, null, null);
	}

	/**
	 * Handling of first resume() call in case of execution mode
	 * 
	 * @param execution
	 */
	after(ActivityExecution execution) : activityExecutionInExecutionMode(execution) {
		boolean hasEnabledNodes = ExecutionContext.getInstance()
				.hasEnabledNodesIncludingCallees(execution);
		if (hasEnabledNodes) {
			ExecutionContext.getInstance().resume(execution.hashCode());
		}
	}

	/**
	 * Execution of the method ActionActivation.fire(TokenList)
	 * 
	 * @param activation
	 *            Activation object of the Action
	 */
	private pointcut fireActionActivationExecution(ActionActivation activation) : execution (void ActionActivation.fire(TokenList)) && target(activation);

	/**
	 * Handling of ActivityNodeEntryEvent for Actions
	 * 
	 * @param activation
	 *            Activation object of the
	 */
	before(ActionActivation activation) : fireActionActivationExecution(activation) {
		handleActivityNodeEntry(activation);
	}

	/**
	 * Execution of the method ControlNodeActivation.fire(TokenList)
	 * 
	 * @param activation
	 *            Activation object of the ControlNode for which fire(TokenList)
	 *            is called
	 */
	private pointcut controlNodeFire(ControlNodeActivation activation) : execution (void ControlNodeActivation.fire(TokenList)) && target(activation);

	/**
	 * Handling of ActivityNodeEntryEvent for ControlNodes
	 * 
	 * @param activation
	 *            Activation object of the ControlNode
	 */
	before(ControlNodeActivation activation) : controlNodeFire(activation) {
		if (activation.node == null) {
			// anonymous fork node
			return;
		}
		handleActivityNodeEntry(activation);
	}

	public void notify(Event event) {
		eventlist.add(event);
	}

	/**
	 * Call of Object_.destroy() within Executor.execute(*)
	 * 
	 * @param o
	 *            Object_ for which destroy() is called
	 */
	private pointcut debugExecutorDestroy(Object_ o) : call (void Object_.destroy()) && withincode(ParameterValueList Executor.execute(Behavior, Object_, ParameterValueList)) && target(o);

	/**
	 * Prevents the method Executor.execute() from destroying the
	 * ActivityExecution This is done after the execution of the Activity has
	 * finished see {@link #handleEndOfActivityExecution(ActivityExecution)}
	 * 
	 * @param o
	 */
	void around(Object_ o) : debugExecutorDestroy(o) {

	}

	private pointcut debugCallBehaviorActionActivationDestroy(Object_ o,
			CallActionActivation activation) : call (void Object_.destroy()) && withincode(void CallActionActivation.doAction()) && this(activation) && target(o);

	/**
	 * Prevents the method CallActionActivation.doAction() from destroying the
	 * Execution of the called Activity This is done when the execution of the
	 * called Activity is finished see
	 * {@link #handleEndOfActivityExecution(ActivityExecution)}
	 * 
	 * @param o
	 *            Execution that should be destroyed
	 * @param activation
	 *            Activation of the CallAction
	 */
	void around(Object o, CallActionActivation activation) : debugCallBehaviorActionActivationDestroy(o, activation) {		
		if (callsOpaqueBehaviorExecution(activation)) {
			proceed(o, activation);
		}
	}

	private pointcut debugRemoveCallExecution(CallActionActivation activation) : call (void CallActionActivation.removeCallExecution(Execution)) && withincode(void CallActionActivation.doAction()) && this(activation);

	/**
	 * Prevents the method CallActionActivation.removeCallExecution from
	 * removing the CallExecution within CallActionActivation.doAction() This is
	 * done when the execution of the called Activity finished see
	 * {@link #handleEndOfActivityExecution(ActivityExecution)}
	 * 
	 * @param activation
	 */
	void around(CallActionActivation activation) : debugRemoveCallExecution(activation) {
		if (callsOpaqueBehaviorExecution(activation)) {
			proceed(activation);
		}
	}

	private pointcut callActionSendsOffers(CallActionActivation activation) : call (void ActionActivation.sendOffers()) && target(activation) && withincode(void ActionActivation.fire(TokenList));

	/**
	 * Prevents the method CallBehaviorActionActivation.fire() from sending
	 * offers (if an Activity was called) This is done when the execution of the
	 * called Activity is finished see
	 * {@link #handleEndOfActivityExecution(ActivityExecution)}
	 */
	void around(CallActionActivation activation) : callActionSendsOffers(activation) {
		if (activation.callExecutions.size() == 0) {
			if (activation.node instanceof CallBehaviorAction) {
				if (((CallBehaviorAction) activation.node).behavior instanceof OpaqueBehavior) {
					// If an OpaqueBehaviorExecution was called, this Execution
					// was already removed in CallActionActivation.doAction()
					proceed(activation);
				}
			}
		}
	}
	
	private pointcut callActionCallIsReady(CallActionActivation activation) : call (boolean ActionActivation.isReady()) && target(activation) && withincode(void ActionActivation.fire(TokenList));

	/**
	 * Ensures that the do-while loop in the Action.fire() method is not called
	 * for a CallBehaviorActionActivation that calls an Activity by returning
	 * false for CallBehaviorActionActiviation.fire() After the execution of the
	 * called Activity, is is checked if the CallBehaviorAction can be executed
	 * again see {@link #handleEndOfActivityExecution(ActivityExecution)}
	 * 
	 * @return false
	 */
	boolean around(CallActionActivation activation) : callActionCallIsReady(activation) {
		if (activation.callExecutions.size() == 0) {
			// If an OpaqueBehaviorExecution was called, this Execution was
			// already removed in CallActionActivation.doAction()
			return proceed(activation);
		} else {
			return false;
		}
	}

	private boolean callsOpaqueBehaviorExecution(CallActionActivation activation) {
		if (activation.callExecutions.size() > 0) {
			if (activation.callExecutions
					.get(activation.callExecutions.size() - 1) instanceof OpaqueBehaviorExecution) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Call of ActivityNodeActivation.fire(TokenList) within void
	 * ActivityNodeActivation.receiveOffer() in the execution flow of
	 * ActivityNodeActivationGroup.run(ActivityNodeActivationList) i.e., call of
	 * ActivityNodeActivation.fire(TokenList) of the initial enabled nodes
	 * 
	 * @param activation
	 *            Activation object of the ActivityNode for which
	 *            fire(TokenList) is called
	 * @param tokens
	 *            Tokens which are the parameters for
	 *            ActivityNodeActivation.fire(TokenList)
	 */
	private pointcut debugActivityNodeFiresInitialEnabledNodes(
			ActivityNodeActivation activation, TokenList tokens) : call (void ActivityNodeActivation.fire(TokenList)) && withincode(void ActivityNodeActivation.receiveOffer()) && cflow(execution(void ActivityNodeActivationGroup.run(ActivityNodeActivationList))) && target(activation) && args(tokens);

	/**
	 * Prevents the call of the method ActivityNodeActivation.fire(TokenList)
	 * for an initial enabled node and adds it to the enabled activity nodes
	 * instead
	 * 
	 * @param activation
	 *            Activation object of the initial enabled activity node
	 * @param tokens
	 *            Tokens which are the parameters for the fire(TokenList) method
	 */
	void around(ActivityNodeActivation activation, TokenList tokens) : debugActivityNodeFiresInitialEnabledNodes(activation, tokens) {
		if (activation instanceof ActivityParameterNodeActivation) {
			proceed(activation, tokens);
		} else {
			addEnabledActivityNodeActivation(0, activation, tokens);
		}
	}

	private void addEnabledActivityNodeActivation(int position,
			ActivityNodeActivation activation, TokenList tokens) {
		ActivityExecution currentActivityExecution = activation
				.getActivityExecution();

		ExecutionStatus exestatus = ExecutionContext.getInstance()
				.getActivityExecutionStatus(currentActivityExecution);

		exestatus.addEnabledActivation(activation, tokens);		
	}

	private void handleSuspension(ActivityExecution execution, Element location) {
		ExecutionStatus executionstatus = ExecutionContext.getInstance()
				.getActivityExecutionStatus(execution);

		ActivityEntryEvent callerevent = executionstatus.getActivityEntryEvent();
		
		List<ActivityNode> allEnabledNodes = ExecutionContext.getInstance().getEnabledNodes(execution.hashCode());
		List<ActivityNode> enabledNodesSinceLastStepForExecution = executionstatus
				.getEnabledNodesSinceLastStep();
		for (int i = 0; i < enabledNodesSinceLastStepForExecution.size(); ++i) {
			if (!allEnabledNodes.contains(enabledNodesSinceLastStepForExecution.get(i))) {
				enabledNodesSinceLastStepForExecution.remove(i);
				--i;
			}
		}

		List<Breakpoint> hitBreakpoints = new ArrayList<Breakpoint>();
		for (int i = 0; i < enabledNodesSinceLastStepForExecution.size(); ++i) {
			ActivityNode node = enabledNodesSinceLastStepForExecution.get(i);
			Breakpoint breakpoint = ExecutionContext.getInstance()
					.getBreakpoint(node);
			if (breakpoint != null) {
				hitBreakpoints.add(breakpoint);
			}
		}

		SuspendEvent event = null;
		if (hitBreakpoints.size() > 0) {
			event = new BreakpointEventImpl(execution.hashCode(), location,
					callerevent);
			((BreakpointEvent) event).getBreakpoints().addAll(hitBreakpoints);
			ExecutionContext.getInstance().setExecutionInResumeMode(execution,
					false);
		} else {
			event = new SuspendEventImpl(execution.hashCode(), location,
					callerevent);
		}

		event.getNewEnabledNodes()
				.addAll(enabledNodesSinceLastStepForExecution);
		eventprovider.notifyEventListener(event);
		executionstatus.clearEnabledNodesSinceLastStep();
	}

	/**
	 * Call of ActivityNodeActivation.fire(TokenList) within
	 * ActivityNodeActivation.receiveOffer() which does not happen in the
	 * execution flow of
	 * ActivityNodeActivationGroup.run(ActivityNodeActivationList) i.e., call of
	 * ActivityNodeActivation.fire(TokenList) of all ActivityNodes other than
	 * initial enabled nodes
	 * 
	 * @param activation
	 *            Activation object of the ActivityNode for which
	 *            fire(TokenList) is called
	 * @param tokens
	 *            Tokens that are the parameter of fire(TokenList)
	 */
	private pointcut debugActivityNodeFiresOtherThanInitialEnabledNodes(
			ActivityNodeActivation activation, TokenList tokens) : call (void ActivityNodeActivation.fire(TokenList)) && withincode(void ActivityNodeActivation.receiveOffer()) && !(cflow(execution(void ActivityNodeActivationGroup.run(ActivityNodeActivationList)))) && target(activation) && args(tokens);

	/**
	 * Prevents the call of the method ActivityNodeActivation.fire(TokenList)
	 * and adds it to enabled activity node list instead
	 * 
	 * @param activation
	 *            ActivityNodeActivation object for which fire(TokenList) is
	 *            called
	 * @param tokens
	 *            Tokens that are the parameter of fire(TokenList)
	 */
	void around(ActivityNodeActivation activation, TokenList tokens) : debugActivityNodeFiresOtherThanInitialEnabledNodes(activation, tokens) {
		if (activation.node == null) {
			// anonymous fork
			proceed(activation, tokens);
			return;
		}

		if (activation instanceof ObjectNodeActivation) {
			proceed(activation, tokens);
			return;
		}

		if (tokens.size() > 0) {
			addEnabledActivityNodeActivation(0, activation, tokens);
		} else {
			if (activation instanceof DecisionNodeActivation) {
				/*
				 * If a decision input flow is provided for a decision node and
				 * this decision node has no other incoming edge no tokens are
				 * provided
				 */
				addEnabledActivityNodeActivation(0, activation, tokens);
			}
			if (activation instanceof ExpansionRegionActivation) {
				/*
				 * ExpansionRegionActiviation.takeOfferedTokens() always returns
				 * an empty list of tokens to
				 * ActivityNodeActivation.receiveOffer() which provices the
				 * tokens to the ActivityNodeActivation.fire() method
				 */
				addEnabledActivityNodeActivation(0, activation, tokens);
			}
		}
	}

	/**
	 * Execution of ActivityNodeActivation.fire(TokenList)
	 * 
	 * @param activation
	 *            Activation object for which fire(TokenList) is called
	 */
	private pointcut debugActivityNodeFiresExecution(
			ActivityNodeActivation activation) : execution (void ActivityNodeActivation.fire(TokenList)) && target(activation);

	/**
	 * Handling of ActivityNodeExitEvent and SuspendEvent for ActivityNodes
	 * 
	 * @param activation
	 *            Activation object for the ActivityNode
	 */
	after(ActivityNodeActivation activation) :  debugActivityNodeFiresExecution(activation) {
		if (activation.node == null) {
			// anonymous fork
			return;
		}

		if (activation instanceof ObjectNodeActivation) {
			return;
		}

		// Handle activity node exit event
		if (!(  (activation instanceof CallOperationActionActivation) || 
				(activation instanceof CallBehaviorActionActivation && ((CallBehaviorAction) activation.node).behavior instanceof Activity) || 
				(activation instanceof StructuredActivityNodeActivation) )) {
			// (1) in the case of a call operation action or a call behavior action
			// which calls an activity, the exit event is issued when the called
			// activity execution ends
			// (2) in the case of a structured activity node the exit event is issued
			// when no contained nodes are enabled
			handleActivityNodeExit(activation);
		}
		
		if(activation instanceof StructuredActivityNodeActivation) { 
			// For an structured activity node this advice is executed right after its execution started
			// and the initially enabled nodes within this structured activity nodes have been determined.
			((StructuredActivityNodeActivation) activation).firing = true; // this is necessary because the doAction method of a structured activity node is interrupted (because it consists of the execution of the contained nodes)
			if(activation instanceof ConditionalNodeActivation) {
				checkStatusOfConditionalNode((ConditionalNodeActivation)activation);
			} else if(activation instanceof LoopNodeActivation) {
				checkStatusOfLoopNode((LoopNodeActivation)activation);
			} else {
				checkStatusOfStructuredActivityNode((StructuredActivityNodeActivation)activation); // this check is necessary for determining if the structured activity node was empty or no contained nodes have been enabled
			}
		}

		if (activation.group instanceof ExpansionActivationGroup) {
			// executed node is contained in expansion region
			ExpansionActivationGroup expansionActivationGroup = (ExpansionActivationGroup) activation.group;
			handleExpansionActivationGroup(expansionActivationGroup);
		}

		// Handle suspension
		if (activation.getActivityExecution().getTypes().size() == 0) {
			// Activity was already destroyed, i.e., the Activity already
			// finished
			// This can happen in the case of existing breakpoints in resume
			// mode
			return;
		}
		boolean hasEnabledNodes = ExecutionContext.getInstance()
				.hasEnabledNodesIncludingCallees(
						activation.getActivityExecution());
		if (!hasEnabledNodes) {
			handleEndOfActivityExecution(activation.getActivityExecution());
		} else {
			if (activation instanceof CallActionActivation) {
				if (((CallActionActivation) activation).callExecutions.size() > 0) {
					return;
				}
			}
			handleSuspension(activation.getActivityExecution(), activation.node);
		}
	}

	/**
	 * Handles the further execution of an expansion region
	 * 
	 * @param expansionActivationGroup
	 */
	private void handleExpansionActivationGroup(
			ExpansionActivationGroup expansionActivationGroup) {
		ExpansionRegionActivation expansionRegionActiviaton = expansionActivationGroup.regionActivation;

		boolean groupHasEnabledNode = hasExpansionActivationGroupEnabledNodes(expansionActivationGroup);

		if (!groupHasEnabledNode) {
			// no enabled node exists in current executed expansion activation
			// group
			if (expansionActivationGroup.index < expansionRegionActiviaton.activationGroups
					.size()) {
				// further expansion activation groups have to be executed
				ExpansionActivationGroup nextExpansionActivationGroup = determineNextExpansionActivationGroup(expansionActivationGroup);
				expansionRegionActiviaton
						.runGroup(nextExpansionActivationGroup);
			} else {
				// execution of expansion region is finished
				finishExpansionRegionActivation(expansionRegionActiviaton);

				// issue ActivityNodeExitEvent
				handleActivityNodeExit(expansionRegionActiviaton);
			}
		}
	}

	/**
	 * Checks if the given expansion activation group has enabled nodes
	 * 
	 * @param expansionActivationGroup
	 * @return true if enabled nodes exist
	 */
	private boolean hasExpansionActivationGroupEnabledNodes(
			ExpansionActivationGroup expansionActivationGroup) {
		ExpansionRegionActivation expansionRegionActiviaton = expansionActivationGroup.regionActivation;
		ActivityExecution activityExecution = expansionRegionActiviaton
				.getActivityExecution();
		ExecutionStatus executionStatus = ExecutionContext.getInstance()
				.getActivityExecutionStatus(activityExecution);

		boolean groupHasEnabledNode = false;

		// Check if expansion activation group contains enabled node
		for (ActivityNodeActivation nodeActivation : expansionActivationGroup.nodeActivations) {
			groupHasEnabledNode = executionStatus.isNodeEnabled(nodeActivation.node);
			if (groupHasEnabledNode) {
				return true;
			}
		}

		// Check if an activity called by an call action contained in the
		// activation group is still executing
		List<ActivityExecution> callees = ExecutionContext.getInstance()
				.getExecutionHierarchy().getCallee(activityExecution);
		for (ActivityExecution callee : callees) {
			ExecutionStatus calleeStatus = ExecutionContext.getInstance()
					.getActivityExecutionStatus(callee);
			ActivityNodeActivation callerActivation = calleeStatus
					.getActivityCall();
			if (expansionActivationGroup.nodeActivations
					.contains(callerActivation)) {
				// Checks if activity was called by a call action contained in
				// the activation group
				if (ExecutionContext.getInstance()
						.hasEnabledNodesIncludingCallees(callee)) {
					// Checks if the activity is still under execution
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Determines the expansion activation group of an expansion region to be
	 * executed next according to its index
	 * 
	 * @param expansionActivationGroup
	 * @return
	 */
	private ExpansionActivationGroup determineNextExpansionActivationGroup(
			ExpansionActivationGroup expansionActivationGroup) {
		ExpansionRegionActivation expansionRegionActiviaton = expansionActivationGroup.regionActivation;

		ExpansionActivationGroup nextExpansionActivationGroup = null;
		for (ExpansionActivationGroup group : expansionRegionActiviaton.activationGroups) {
			if (group.index == expansionActivationGroup.index + 1) {
				nextExpansionActivationGroup = group;
			}
		}
		return nextExpansionActivationGroup;
	}

	/**
	 * Finishes the execution of an expansion region:
	 * <ol>
	 * <li>provide output of expansion activation groups</li>
	 * <li>terminate expansion activation groups</li>
	 * <li>provide output of expansion region</li>
	 * <li>send offers from expansion region</li>
	 * </ol>
	 * 
	 * @param expansionRegionActiviaton
	 */
	private void finishExpansionRegionActivation(
			ExpansionRegionActivation expansionRegionActiviaton) {
		// provide expansion activation group output and terminate activation
		// groups
		for (ExpansionActivationGroup activationGroup : expansionRegionActiviaton.activationGroups) {
			// START duplicate code from
			// ExpansionRegionActivation.runGroup(ExpansionActivationGroup)
			OutputPinActivationList groupOutputs = activationGroup.groupOutputs;
			for (int i = 0; i < groupOutputs.size(); i++) {
				OutputPinActivation groupOutput = groupOutputs.getValue(i);
				groupOutput.fire(groupOutput.takeOfferedTokens());
			}
			activationGroup.terminateAll();
			// END duplicate code from
			// ExpansionRegionActivation.runGroup(ExpansionActivationGroup)
		}

		// provide expansion region output
		ExpansionActivationGroupList activationGroups = expansionRegionActiviaton.activationGroups;
		ExpansionRegion region = (ExpansionRegion) expansionRegionActiviaton.node;
		ExpansionNodeList outputElements = region.outputElement;
		// START duplicate code from
		// ExpansionRegionActivation.doStructuredActivity()
		for (int i = 0; i < activationGroups.size(); i++) {
			ExpansionActivationGroup activationGroup = activationGroups
					.getValue(i);
			OutputPinActivationList groupOutputs = activationGroup.groupOutputs;
			for (int j = 0; j < groupOutputs.size(); j++) {
				OutputPinActivation groupOutput = groupOutputs.getValue(j);
				ExpansionNode outputElement = outputElements.getValue(j);
				// this.getExpansionNodeActivation(outputElement).addTokens(groupOutput.takeTokens());
				expansionRegionActiviaton.getExpansionNodeActivation(
						outputElement).addTokens(groupOutput.takeTokens());
			}
		}
		// END duplicate code from
		// ExpansionRegionActivation.doStructuredActivity()

		// remove expansion activation groups
		expansionRegionActiviaton.activationGroups.clear();

		// send offers
		expansionRegionActiviaton.sendOffers();
	}

	/**
	 * Execution of ActionActivation.sendOffers() in the execution context of
	 * ActionActivation.fire(TokenList)
	 * 
	 * @param activation
	 *            Activation object for which sendOffers() is called
	 */
	private pointcut debugFireActionActivationSendOffers(
			ActionActivation activation) : execution(void ActionActivation.sendOffers()) && target(activation) && cflow (execution(void ActionActivation.fire(TokenList)));

	/**
	 * Handles the do-while loop in the method ActionActivation.fire(TokenList)
	 * (is fireAgain) If the ActionActivation can fire again it is added to the
	 * enabled activity node list and because the token offers are consumed
	 * using the activation.takeOfferedTokens() method, the
	 * activation.fire(TokenList) method does not execute the action's behavior
	 * again
	 * 
	 * @param activation
	 */
	after(ActionActivation activation) : debugFireActionActivationSendOffers(activation) {
		checkIfActionCanFireAgain(activation);
	}

	/**
	 * Call of ActivityNodeActivationGroup.run(ActivityNodeActivationList)
	 */
	private pointcut activityActivationGroupRun(
			ActivityNodeActivationGroup activationgroup) : call (void ActivityNodeActivationGroup.run(ActivityNodeActivationList)) && withincode(void ActivityNodeActivationGroup.activate(ActivityNodeList, ActivityEdgeList)) && target(activationgroup);

	/**
	 * Handling of first SuspendEvent First step is the step were the activity
	 * execution started and the initial enabled nodes are determined.
	 */
	after(ActivityNodeActivationGroup activationgroup) : activityActivationGroupRun(activationgroup) {
		ActivityExecution activityExecution = null;

		if (activationgroup instanceof ExpansionActivationGroup) {
			activityExecution = ((ExpansionActivationGroup) activationgroup).regionActivation.group.activityExecution;
		} else {
			activityExecution = activationgroup.activityExecution;
		}

		ExecutionStatus executionstatus = ExecutionContext.getInstance()
				.getActivityExecutionStatus(activityExecution);

		if (executionstatus != null) {
			if (executionstatus.getEnabledNodes().size() == 0) {
				return;
			}
			Activity activity = (Activity) activityExecution.types.get(0);

			handleSuspension(activityExecution, activity);
		}
	}

	/**
	 * Execution of ActivityNodeActivationList.addValue(*) in the execution flow
	 * of ActivityNodeActivationGroup.run(ActivityNodeActivationList)
	 * 
	 * @param list
	 *            ActivityNodeActivationList for which addValue(*) is called
	 */
	private pointcut valueAddedToActivityNodeActivationList(
			ActivityNodeActivationList list,
			ActivityNodeActivationGroup activationgroup) : execution (void ActivityNodeActivationList.addValue(*))  && target(list) && cflow (execution(void ActivityNodeActivationGroup.run(ActivityNodeActivationList)) && target(activationgroup));

	/**
	 * Execution of Execution.execute()
	 * 
	 * @param execution
	 *            Execution object for which execute() is called
	 */
	private pointcut activityExecutionExecuteExecution(
			ActivityExecution execution) : execution (void Execution.execute()) && target(execution);

	/**
	 * If there are no initial enabled nodes in the activity a ActivityExitEvent
	 * is produced
	 * 
	 * @param behavior
	 *            Behavior which has no initial enabled nodes
	 */
	after(ActivityExecution execution) : activityExecutionExecuteExecution(execution) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance()
				.getActivityExecutionStatus(execution);

		if (executionStatus != null
				&& executionStatus.getEnabledNodes().size() == 0) {
			handleEndOfActivityExecution(execution);
		}
	}

	/**
	 * Handle call of activity by call action
	 * 
	 * @param execution
	 * @param activation
	 */
	private pointcut callActivityExecutionExecute(ActivityExecution execution,
			CallActionActivation activation) : call(void Execution.execute()) && withincode(void CallActionActivation.doAction()) && target(execution) && this(activation);

	before(ActivityExecution execution, CallActionActivation activation) : callActivityExecutionExecute(execution, activation) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance()
				.getActivityExecutionStatus(activation.getActivityExecution());

		ActivityNodeEntryEvent callaentryevent = executionStatus
				.getActivityNodeEntryEvent(activation.node);

		handleNewActivityExecution(execution, activation, callaentryevent);
	}
	
	private void handleNewActivityExecution(ActivityExecution execution,
			ActivityNodeActivation caller, Event parent) {
		ExecutionContext context = ExecutionContext.getInstance();

		Activity activity = (Activity) (execution.getBehavior());
		ActivityEntryEvent event = new ActivityEntryEventImpl(
				execution.hashCode(), activity, parent);

		context.addActivityExecution(execution, caller, event);

		eventprovider.notifyEventListener(event);
	}

	private void handleEndOfActivityExecution(ActivityExecution execution) {
		ExecutionStatus executionstatus = ExecutionContext.getInstance()
				.getActivityExecutionStatus(execution);

		Activity activity = (Activity) (execution.getBehavior());
		ActivityEntryEvent entryevent = executionstatus.getActivityEntryEvent();
		ActivityExitEvent event = new ActivityExitEventImpl(
				execution.hashCode(), activity, entryevent);

		{
			// Produce the output of activity
			// DUPLICATE CODE START from void ActivityExecution.execute()
			ActivityParameterNodeActivationList outputActivations = execution.activationGroup
					.getOutputParameterNodeActivations();
			for (int i = 0; i < outputActivations.size(); i++) {
				ActivityParameterNodeActivation outputActivation = outputActivations
						.getValue(i);

				ParameterValue parameterValue = new ParameterValue();
				parameterValue.parameter = ((ActivityParameterNode) (outputActivation.node)).parameter;

				TokenList tokens = outputActivation.getTokens();
				for (int j = 0; j < tokens.size(); j++) {
					Token token = tokens.getValue(j);
					Value value = ((ObjectToken) token).value;
					if (value != null) {
						parameterValue.values.addValue(value);
						Debug.println("[event] Output activity="
								+ activity.name + " parameter="
								+ parameterValue.parameter.name + " value="
								+ value);
					}
				}

				execution.setParameterValue(parameterValue);
			}
			// DUPLICATE CODE END from void ActivityExecution.execute()
		}

		ActivityNodeActivation caller = executionstatus.getActivityCall();
		if (caller instanceof CallActionActivation) {
			// Get the output from the called activity
			// DUPLICATE CODE START from void CallActionActivation.doAction()
			ParameterValueList outputParameterValues = execution
					.getOutputParameterValues();
			for (int j = 0; j < outputParameterValues.size(); j++) {
				ParameterValue outputParameterValue = outputParameterValues
						.getValue(j);
				OutputPin resultPin = ((CallAction) caller.node).result
						.getValue(j);
				((CallActionActivation) caller).putTokens(resultPin,
						outputParameterValue.values);
			}
			// DUPLICATE CODE END from void CallActionActivation.doAction()
			
			// Destroy execution of the called activity
			execution.destroy();
			((CallActionActivation) caller).removeCallExecution(execution);
						
			// Notify about ActivityExitEvent
			eventprovider.notifyEventListener(event);

			// Notify about Exit of CallAction
			handleActivityNodeExit(caller);			

			// Call sendOffer() from the CallAction
			((CallActionActivation) caller).sendOffers();

			// Check if can fire again
			((CallActionActivation) caller).firing = false;
			if (caller.isReady()) {
				TokenList incomingTokens = caller.takeOfferedTokens();
				if (incomingTokens.size() > 0) {
					addEnabledActivityNodeActivation(0, caller, new TokenList());
				}
			}
			
			checkStatusOfContainingStructuredActivityNode(caller);
			
			if (caller.group instanceof ExpansionActivationGroup) {
				handleExpansionActivationGroup((ExpansionActivationGroup) caller.group);
			}

			boolean hasCallerEnabledNodes = ExecutionContext.getInstance()
					.hasCallerEnabledNodes(execution);

			if (!hasCallerEnabledNodes) {
				handleEndOfActivityExecution(caller.getActivityExecution());
			} else {
				handleSuspension(caller.getActivityExecution(), caller.node);
			}
			return;
		} else {
			// ActivityExecution was triggered by user
			ParameterValueList outputValues = execution.getOutputParameterValues();
			ExecutionContext.getInstance().setActivityExecutionOutput(execution, outputValues);
			execution.destroy();
			eventprovider.notifyEventListener(event);

			this.eventlist.clear();

			ExecutionContext.getInstance().setExecutionInResumeMode(execution, false);

			ExecutionContext.getInstance().removeActivityExecution(execution);
		}
	}

	private void handleActivityNodeEntry(ActivityNodeActivation activation) {
		ExecutionStatus executionstatus = null;
		ActivityEntryEvent activityentry = null;
		int activityExecutionID = -1;

		if (activation.node != null) {
			executionstatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
			activityentry = executionstatus.getActivityEntryEvent();
			activityExecutionID = activation.getActivityExecution().hashCode();
		}
		ActivityNodeEntryEvent event = new ActivityNodeEntryEventImpl(
				activityExecutionID, activation.node, activityentry);

		if (activation.node != null) {
			executionstatus.setActivityNodeEntryEvent(activation.node, event);
		}
 		eventprovider.notifyEventListener(event); 		
	}

	private void handleActivityNodeExit(ActivityNodeActivation activation) {
		if (activation instanceof CallActionActivation) {
			if (((CallActionActivation) activation).callExecutions.size() > 0) {
				return;
			}
		}
		if (activation instanceof ExpansionRegionActivation) {
			if (((ExpansionRegionActivation) activation).activationGroups
					.size() > 0) {
				if (((ExpansionRegionActivation) activation).activationGroups
						.get(0).nodeActivations.get(0).running) {
					return;
				}
			}
		}

		if (activation.node != null) {
			ExecutionStatus executionstatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());			
			if (executionstatus != null) {
				ActivityNodeEntryEvent entry = executionstatus.getActivityNodeEntryEvent(activation.node);
				int activityExecutionID = activation.getActivityExecution().hashCode();
				ActivityNodeExitEvent event = new ActivityNodeExitEventImpl(activityExecutionID, activation.node, entry);
				eventprovider.notifyEventListener(event);
				
				if(!(activation.node instanceof CallAction)) {
					checkStatusOfContainingStructuredActivityNode(activation);
				}
			}
		}
	}

	/**
	 * New extensional value at locus
	 */
	private pointcut locusNewExtensionalValue(ExtensionalValue value) : call (void Locus.add(ExtensionalValue)) && args(value) && !(cflow(execution(Value Value.copy())));

	after(ExtensionalValue value) : locusNewExtensionalValue(value) {
		if (value.getClass() == Object_.class || value.getClass() == Link.class) {
			ExtensionalValueEvent event = new ExtensionalValueEventImpl(value,
					ExtensionalValueEventType.CREATION);
			eventprovider.notifyEventListener(event);
		}
	}

	/**
	 * Extensional value removed from locus
	 */
	private pointcut locusExtensionalValueRemoved() : call (ExtensionalValue ExtensionalValueList.remove(int)) && withincode(void Locus.remove(ExtensionalValue));

	after() returning (Object obj) : locusExtensionalValueRemoved() {
		if (obj.getClass() == Object_.class || obj.getClass() == Link.class) {
			ExtensionalValue value = (ExtensionalValue) obj;
			ExtensionalValueEvent event = new ExtensionalValueEventImpl(value,
					ExtensionalValueEventType.DESTRUCTION);
			eventprovider.notifyEventListener(event);
		}
	}

	/**
	 * Classifier removed/added as type of object
	 */
	private HashMap<ReclassifyObjectActionActivation, Object_> reclassifications = new HashMap<ReclassifyObjectActionActivation, Object_>();

	private pointcut reclassifyObjectAction(
			ReclassifyObjectActionActivation activation) : execution (void ReclassifyObjectActionActivation.doAction()) && target(activation);

	before(ReclassifyObjectActionActivation activation) : reclassifyObjectAction(activation) {
		if (activation.pinActivations.size() > 0) {
			PinActivation pinactivation = activation.pinActivations.get(0);
			if (pinactivation.heldTokens.size() > 0) {
				if (pinactivation.heldTokens.get(0) instanceof ObjectToken) {
					ObjectToken token = (ObjectToken) pinactivation.heldTokens
							.get(0);
					if (token.value instanceof Reference) {
						Reference ref = (Reference) token.value;
						Object_ obj = ref.referent;
						if (obj != null) {
							reclassifications.put(activation, obj);
						}
					}
				}
			}
		}
	}

	after(ReclassifyObjectActionActivation activation) : reclassifyObjectAction(activation) {
		reclassifications.remove(activation);
	}

	private pointcut classifierRemovedAsObjectType(
			ReclassifyObjectActionActivation activation) : call (void Class_List.removeValue(int)) && this(activation) && withincode(void ActionActivation.doAction());

	after(ReclassifyObjectActionActivation activation) : classifierRemovedAsObjectType(activation) {
		Object_ o = reclassifications.get(activation);
		ExtensionalValueEvent event = new ExtensionalValueEventImpl(o,
				ExtensionalValueEventType.TYPE_REMOVED);
		eventprovider.notifyEventListener(event);
	}

	private pointcut classifierAddedAsObjectType(
			ReclassifyObjectActionActivation activation) : call (void Class_List.addValue(Class_)) && this(activation) && withincode(void ActionActivation.doAction());

	after(ReclassifyObjectActionActivation activation) : classifierAddedAsObjectType(activation) {
		Object_ o = reclassifications.get(activation);
		ExtensionalValueEvent event = new ExtensionalValueEventImpl(o,
				ExtensionalValueEventType.TYPE_ADDED);
		eventprovider.notifyEventListener(event);
	}

	/**
	 * Feature values removed from object
	 */

	private pointcut compoundValueRemoveFeatureValue(Object_ o) : call(FeatureValue FeatureValueList.remove(int)) && this(o);

	after(Object_ o) returning(Object value): compoundValueRemoveFeatureValue(o) {
		FeatureValueEvent event = new FeatureValueEventImpl(o,
				ExtensionalValueEventType.VALUE_DESTRUCTION,
				(FeatureValue) value);
		eventprovider.notifyEventListener(event);
	}

	/**
	 * Feature values added to object
	 */

	private pointcut compoundValueAddFeatureValue(Object_ o, FeatureValue value) : call(void FeatureValueList.addValue(FeatureValue)) && this(o) && args(value) && !cflow(execution(Object_ Locus.instantiate(Class_))) && !(cflow(execution(Value Value.copy())));

	after(Object_ o, FeatureValue value) : compoundValueAddFeatureValue(o, value) {
		FeatureValueEvent event = new FeatureValueEventImpl(o,
				ExtensionalValueEventType.VALUE_CREATION, value);
		eventprovider.notifyEventListener(event);
	}

	/**
	 * Value of feature value set
	 */

	private pointcut featureValueSetValue(Object_ obj, FeatureValue value,
			ValueList values) : set(public ValueList FeatureValue.values) && this(obj) && target(value) && args(values) && withincode(void CompoundValue.setFeatureValue(StructuralFeature, ValueList, int)) && !cflow(execution(Object_ Locus.instantiate(Class_))) && !(cflow(execution(void ReclassifyObjectActionActivation.doAction()))) && !(cflow(execution(Value Value.copy())));

	after(Object_ obj, FeatureValue value, ValueList values) : featureValueSetValue(obj, value, values) {
		FeatureValueEvent event = new FeatureValueEventImpl(obj,
				ExtensionalValueEventType.VALUE_CHANGED, value);
		eventprovider.notifyEventListener(event);
	}

	private HashMap<StructuralFeatureActionActivation, Object_> structfeaturevalueactions = new HashMap<StructuralFeatureActionActivation, Object_>();

	private pointcut structuralFeatureValueAction(
			StructuralFeatureActionActivation activation) : execution (void StructuralFeatureActionActivation.doAction()) && target(activation) && if(!(activation instanceof ReadStructuralFeatureActionActivation));

	before(StructuralFeatureActionActivation activation) : structuralFeatureValueAction(activation) {
		PinActivation pinactivation = activation
				.getPinActivation(((StructuralFeatureAction) activation.node).object);
		if (pinactivation != null) {
			if (pinactivation.heldTokens.size() > 0) {
				if (pinactivation.heldTokens.get(0) instanceof ObjectToken) {
					ObjectToken token = (ObjectToken) pinactivation.heldTokens
							.get(0);
					Object_ obj = null;
					if (token.value instanceof Reference) {
						Reference ref = (Reference) token.value;
						obj = ref.referent;
					} else if (token.value instanceof Object_) {
						obj = (Object_) token.value;
					}

					if (obj != null) {
						structfeaturevalueactions.put(activation, obj);
					}
				}
			}
		}
	}

	after(StructuralFeatureActionActivation activation) : structuralFeatureValueAction(activation) {
		structfeaturevalueactions.remove(activation);
	}

	private pointcut valueAddedToFeatureValue(
			AddStructuralFeatureValueActionActivation activation) : (call (void ValueList.addValue(Value)) || call (void ValueList.addValue(int, Value)) ) && this(activation) && withincode(void ActionActivation.doAction()) && !(cflow(execution(Value Value.copy())));

	after(AddStructuralFeatureValueActionActivation activation) : valueAddedToFeatureValue(activation) {
		handleFeatureValueChangedEvent(activation);
	}

	private pointcut valueRemovedFromFeatureValue(
			RemoveStructuralFeatureValueActionActivation activation) : call (Value ValueList.remove(int)) && this(activation) && withincode(void ActionActivation.doAction());

	after(RemoveStructuralFeatureValueActionActivation activation) : valueRemovedFromFeatureValue(activation) {
		handleFeatureValueChangedEvent(activation);
	}

	private void handleFeatureValueChangedEvent(
			StructuralFeatureActionActivation activation) {
		Object_ o = structfeaturevalueactions.get(activation);
		FeatureValue featureValue = o
				.getFeatureValue(((StructuralFeatureAction) activation.node).structuralFeature);
		if (featureValue.feature instanceof Property) {
			Property p = (Property) featureValue.feature;
			if (p.association != null) {
				return;
			}
		}

		FeatureValueEvent event = new FeatureValueEventImpl(o,
				ExtensionalValueEventType.VALUE_CHANGED, featureValue);
		eventprovider.notifyEventListener(event);
	}

	private pointcut valueAddedToLocusBecauseOfCopy() : call (void Locus.add(fUML.Semantics.Classes.Kernel.ExtensionalValue)) && withincode(Value ExtensionalValue.copy());

	/**
	 * Prevent addition of copied value to locus
	 */
	void around() : valueAddedToLocusBecauseOfCopy() {
	}

	private pointcut tokenSendingViaEdge(ActivityEdgeInstance edgeInstance,
			TokenList tokens) : call (void ActivityEdgeInstance.sendOffer(TokenList)) && target(edgeInstance) && args(tokens);

	/**
	 * Store sent tokens 
	 * @param edgeInstance
	 * @param tokens
	 */
	before(ActivityEdgeInstance edgeInstance, TokenList tokens) : tokenSendingViaEdge(edgeInstance, tokens) {
		// store token sendings via edges for trace
		ActivityNodeActivation sourceNodeActivation = edgeInstance.source;
		  
		if(sourceNodeActivation.group == null) { 
			if(sourceNodeActivation instanceof ForkNodeActivation && sourceNodeActivation.node == null) { // anonymous fork node 
				 sourceNodeActivation = sourceNodeActivation.incomingEdges.get(0).source; 
			} else if(sourceNodeActivation instanceof OutputPinActivation &&  sourceNodeActivation.outgoingEdges.get(0).target.node.inStructuredNode != null) { // anonymous output pin activation for expansion region
				sourceNodeActivation = ((ExpansionActivationGroup)sourceNodeActivation.outgoingEdges.get(0).target.group).regionActivation; 
			} 
		}
		  
		ActivityExecution currentActivityExecution = sourceNodeActivation.getActivityExecution(); 
		ExecutionStatus exestatus = ExecutionContext.getInstance().getActivityExecutionStatus(currentActivityExecution);
		 
		if (edgeInstance.group == null) { // anonymous fork node was inserted
			if (edgeInstance.source instanceof ForkNodeActivation) { 
				edgeInstance = edgeInstance.source.incomingEdges.get(0); 
			} else if (edgeInstance.target instanceof ForkNodeActivation) { 
				edgeInstance = edgeInstance.target.outgoingEdges.get(0); 
			} 
		}
		exestatus.addTokenSending(sourceNodeActivation, tokens, edgeInstance.edge);
	}

	private pointcut tokenTransferring(Token tokenOriginal,
			ActivityNodeActivation activation) : call (Token Token.transfer(ActivityNodeActivation)) && target(tokenOriginal) && args(activation);

	/**
	 * Create token copy map 
	 * @param tokenOriginal
	 */
	Token around(Token tokenOriginal, ActivityNodeActivation holder) : tokenTransferring(tokenOriginal, holder){
		// store token copies for trace
		Token tokenCopy = proceed(tokenOriginal, holder);

		if(holder.group == null) { 
			if(holder instanceof ForkNodeActivation && holder.node == null) { //anonymous fork node
				holder = holder.incomingEdges.get(0).source; 
			} else if(holder instanceof OutputPinActivation) { 
				if(holder.outgoingEdges.size() > 0)	{ 
					if(holder.outgoingEdges.get(0).target.node.inStructuredNode != null) { 
						holder = ((ExpansionActivationGroup)holder.outgoingEdges.get(0).target.group).regionActivation; 
					} 
				} else if(holder.incomingEdges.size() > 0) {
					if(holder.incomingEdges.get(0).source.node.inStructuredNode != null) { 
						holder = ((ExpansionActivationGroup)holder.incomingEdges.get(0).source.group).regionActivation; 
					} 
				} 
			} 
		}
		 
		if(holder != null && holder.group != null) { 
			ActivityExecution currentActivityExecution = holder.getActivityExecution();
			ExecutionStatus exestatus = ExecutionContext.getInstance().getActivityExecutionStatus(currentActivityExecution); 
			exestatus.addTokenCopy(tokenOriginal,	tokenCopy); 
		}

		return tokenCopy;
	}

	/**
	 * Call of ActivityNodeActivationGroup.terminateAll() from within
	 * ExpansionRegionActivation.runGroup(ExpansionActivationGroup)
	 * 
	 * @param activationGroup
	 */
	private pointcut debugActivityNodeActivationGroupTerminateAll(
			ActivityNodeActivationGroup activationGroup) : call (void ActivityNodeActivationGroup.terminateAll()) && withincode(void ExpansionRegionActivation.runGroup(ExpansionActivationGroup)) && target(activationGroup);

	/**
	 * Prevents the execution of the method
	 * ActivityNodeActivationGroup.terminateAll() from being executed if it is
	 * called by ExpansionRegionActivation.runGroup(ExpansionActivationGroup)
	 * 
	 * @param activationGroup
	 *            ActivityNodeActivationGroup for which terminateAll() is called
	 */
	void around(ActivityNodeActivationGroup activationGroup) : debugActivityNodeActivationGroupTerminateAll(activationGroup) {
		return;
	}

	/**
	 * Call of ActionActivation.sendOffers() by ActionActivation.fire(TokenList)
	 * 
	 * @param activation
	 */
	private pointcut expansionRegionSendsOffers(
			ExpansionRegionActivation activation) : call (void ActionActivation.sendOffers()) && target(activation) && withincode(void ActionActivation.fire(TokenList));

	/**
	 * Prevents the method ExpansionRegionActivation.fire() from sending offers
	 */
	void around(ExpansionRegionActivation activation) : expansionRegionSendsOffers(activation) {
		return;
	}

	/**
	 * Call of ExpansionRegionActivation.runGroup(ExpansionActivationGroup)
	 * 
	 * @param expansionActivationGroup
	 */
	private pointcut expansionActivationGroupRunGroup(
			ExpansionActivationGroup expansionActivationGroup) : call (void ExpansionRegionActivation.runGroup(ExpansionActivationGroup)) && args(expansionActivationGroup) && withincode(void ExpansionRegionActivation.doStructuredActivity());

	/**
	 * Ensures that ExpansionRegionActivation.runGroup(ExpansionActivationGroup)
	 * is only called for the first ExpansionActiviationGroup
	 * 
	 * @param expansionActivationGroup
	 */
	void around(ExpansionActivationGroup expansionActivationGroup) : expansionActivationGroupRunGroup(expansionActivationGroup) {
		// set running = true for inserted anonymous output pins
		for (OutputPinActivation groupOutput : expansionActivationGroup.groupOutputs) {
			groupOutput.run();
		}
		if (expansionActivationGroup.index == 1) {
			// only start execution of first expansion activation group
			proceed(expansionActivationGroup);
		}
		return;
	}	
		
	private pointcut decisionNodeTakesDecisionInputFlow(ActivityEdgeInstance edgeInstance) : call (TokenList ActivityEdgeInstance.takeOfferedTokens()) && target(edgeInstance) && withincode(Value DecisionNodeActivation.getDecisionInputFlowValue());
	
	/**
	 * Ensures that a decision node only consumes one offered decision input flow value
	 * 
	 * @param edgeInstance
	 */
	TokenList around(ActivityEdgeInstance edgeInstance) : decisionNodeTakesDecisionInputFlow(edgeInstance) {
		TokenList tokens = new TokenList();
		if(edgeInstance.offers.size() > 0) {
			TokenList offeredTokens = edgeInstance.offers.getValue(0).getOfferedTokens();
			tokens.addAll(offeredTokens);
			edgeInstance.offers.removeValue(0);
		}
		return tokens;
	}
	
	private pointcut structuredActivityNodeSendsOffers(
			StructuredActivityNodeActivation activation) : call (void ActionActivation.sendOffers()) && target(activation) && withincode(void ActionActivation.fire(TokenList));

	/**
	 * Prevents the method ExpansionRegionActivation.fire() from sending offers
	 */
	void around(StructuredActivityNodeActivation activation) : structuredActivityNodeSendsOffers(activation) { 
		// this is necessary because the doAction operation of the structured activity node is interrupted 
		// because it consists of the execution of its contained nodes
		return; 
	}
	
	/**
	 * Handling of SuspendEvent for structured activity nodes
	 */

	//private pointcut activityActivationGroupRunForStructuredActivityNode(ActivityNodeActivationGroup activationgroup) : call (void ActivityNodeActivationGroup.run(ActivityNodeActivationList)) && withincode(void StructuredActivityNodeActivation.doStructuredActivity()) && target(activationgroup);	
/*
	after(ActivityNodeActivationGroup activationgroup) : activityActivationGroupRunForStructuredActivityNode(activationgroup) {
		//ActivityExecution activityExecution = getActivityExecution(activationgroup);
		ActivityExecution activityExecution = activationgroup.getActivityExecution();

		ExecutionStatus executionstatus = ExecutionContext.getInstance().getActivityExecutionStatus(activityExecution);

		if (executionstatus != null) {
			if (executionstatus.getEnabledNodes().size() == 0) {
				return;
			}
			Activity activity = (Activity) activityExecution.types.get(0);

			handleSuspension(activityExecution, activity);
		}
	}
*/
/*
	private ActivityExecution getActivityExecution(ActivityNodeActivationGroup activationgroup) {
		// can reuse Activation.getActivityExecution method
		if(activationgroup instanceof ExpansionActivationGroup) {
			// TODO reuse for expansion regions
			return null;
		}
		if(activationgroup.activityExecution != null) {
			return activationgroup.activityExecution;
		}
		if(activationgroup.containingNodeActivation != null) {
			return getActivityExecution(activationgroup.containingNodeActivation.group);
		}
		return null;
	}
*/	
	
	private void checkIfActionCanFireAgain(ActionActivation activation) {
		SemanticVisitor._beginIsolation();
		boolean fireAgain = false;
		activation.firing = false;
		TokenList incomingTokens = new TokenList();
		if (activation.isReady()) {
			incomingTokens = activation.takeOfferedTokens();
			fireAgain = incomingTokens.size() > 0;
			activation.firing = activation.isFiring() & fireAgain;
		}
		SemanticVisitor._endIsolation();

		if (fireAgain) {
			addEnabledActivityNodeActivation(0, activation, incomingTokens);
		}
	}	
	
	private StructuredActivityNodeActivation getContainingStructuredActivityNodeActivation(ActivityNodeActivation activation) {
		if(activation.group == null || activation.group instanceof ExpansionActivationGroup) {
			return null;
		}
		if(activation.group.containingNodeActivation instanceof StructuredActivityNodeActivation) {
			StructuredActivityNodeActivation containingStructuredActivation = (StructuredActivityNodeActivation)activation.group.containingNodeActivation;
			return containingStructuredActivation;
		}	
		return null;
	}
	
	private void checkStatusOfStructuredActivityNode(StructuredActivityNodeActivation activation) {		
		boolean structuredNodeHasEnabledChilds = hasStructuredActivityNodeEnabledChildNodes(activation);
		if(!structuredNodeHasEnabledChilds) {
			handleEndOfStructuredActivityNodeExecution(activation);
		}				
	}		
	
	private boolean hasStructuredActivityNodeEnabledChildNodes(StructuredActivityNodeActivation activation) {
		boolean containedNodeWasEnabled = hasStructuredActivityNodeEnabledDirectChildNodes(activation);
		if(!containedNodeWasEnabled) {
			containedNodeWasEnabled = hasStructuredActivityNodeEnabledCalledNodes(activation);
		}		
		return containedNodeWasEnabled;
	}
	
	private boolean hasStructuredActivityNodeEnabledDirectChildNodes(StructuredActivityNodeActivation activation) { //TODO refactor
		ExecutionStatus executionstatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		List<ActivityNode> containedNodes = getAllContainedNodes((StructuredActivityNode)activation.node); 
		List<ActivityNode> enabledNodes = new ArrayList<ActivityNode>(executionstatus.getEnabledNodes());
		boolean directlyContainedNodeWasEnabled = containedNodes.removeAll(enabledNodes);
		return directlyContainedNodeWasEnabled;
	}
	
	private boolean hasStructuredActivityNodeEnabledCalledNodes(StructuredActivityNodeActivation activation) { //TODO refactor
		for(ActivityNodeActivation childnodeactivation : activation.activationGroup.nodeActivations) {
			if(childnodeactivation instanceof CallActionActivation) {
				for(Execution execution : ((CallActionActivation)childnodeactivation).callExecutions) {
					if(execution instanceof ActivityExecution) {
						ActivityExecution activityexecution = (ActivityExecution)execution;
						if(ExecutionContext.getInstance().hasEnabledNodesIncludingCallees(activityexecution)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private void handleEndOfStructuredActivityNodeExecution(StructuredActivityNodeActivation activation) {		
		activation.sendOffers();		
		handleActivityNodeExit(activation);
		checkIfActionCanFireAgain(activation);
	}
	
	private List<ActivityNode> getAllContainedNodes(StructuredActivityNode node) {		
		List<ActivityNode> containedNodes = new ArrayList<ActivityNode>();
		containedNodes.addAll(node.node);
		
		for(ActivityNode n : node.node) {
			if(n instanceof StructuredActivityNode) {
				containedNodes.addAll(getAllContainedNodes((StructuredActivityNode)n));
			}
		}
		
		return containedNodes;
	}
	
	private void checkStatusOfContainingStructuredActivityNode(ActivityNodeActivation activation) {
		StructuredActivityNodeActivation containingStructuredActivityNodeActivation = getContainingStructuredActivityNodeActivation(activation);
		if(containingStructuredActivityNodeActivation != null) {
			if(containingStructuredActivityNodeActivation instanceof ConditionalNodeActivation) {
				checkStatusOfConditionalNode((ConditionalNodeActivation)containingStructuredActivityNodeActivation);
			} else if(activation instanceof LoopNodeActivation) {
				checkStatusOfLoopNode((LoopNodeActivation)activation);
			} else {
				checkStatusOfStructuredActivityNode(containingStructuredActivityNodeActivation);
			}
		}
	}
	
	/**
	 * Conditional nodes
	 */
	
	/**
	 * Prevents the method ConditionalNodeActivation.doStructuredActivity() from terminating all contained nodes
	 */
	
	private pointcut conditionalNodeTerminatesAll() : call (void ActivityNodeActivationGroup.terminateAll()) && withincode(void ConditionalNodeActivation.doStructuredActivity());
	
	void around() : conditionalNodeTerminatesAll() { 		
		return; 
	}
	
	private pointcut conditionalNodeStarts(ConditionalNodeActivation activation) : call(void StructuredActivityNodeActivation.doStructuredActivity()) && target(activation);
	
	before(ConditionalNodeActivation activation) : conditionalNodeStarts(activation) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.addConditionalNodeExecution(activation);
	}
	
	private pointcut clauseActivationAddedToConditionalNode(ClauseActivation clauseactivation) : call(void ClauseActivationList.addValue(ClauseActivation)) && withincode(void ConditionalNodeActivation.doStructuredActivity()) && args(clauseactivation);
	
	before(ClauseActivation clauseactivation) : clauseActivationAddedToConditionalNode(clauseactivation) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(clauseactivation.conditionalNodeActivation.getActivityExecution());
		executionStatus.addClauseActivation(clauseactivation.conditionalNodeActivation, clauseactivation);
	}
	
	private pointcut conditionalNodeClauseStartsRunningTest(ClauseActivation clauseactivation) : call(void ClauseActivation.runTest()) && target(clauseactivation);
	
	before(ClauseActivation clauseactivation) : conditionalNodeClauseStartsRunningTest(clauseactivation) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(clauseactivation.conditionalNodeActivation.getActivityExecution());
		executionStatus.clauseStartsTest(clauseactivation.conditionalNodeActivation, clauseactivation);
	}
	
	private void checkStatusOfConditionalNode(ConditionalNodeActivation activation) { 
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.updateStatusOfConditionalNode(activation);
		boolean allClauseTestsFinished = executionStatus.areAllClauseTestsFinished(activation);
		boolean anyClauseStartedBody = executionStatus.anyClauseStartedBody(activation);
		boolean anyClauseFinishedBody = executionStatus.anyClauseFinishedBody(activation);
		if(allClauseTestsFinished && !anyClauseStartedBody && !anyClauseFinishedBody) {
			List<ClauseActivation> successorClausesToBeEvaluated = executionStatus.getSuccessorClausesToBeEvaluated(activation);
			if(successorClausesToBeEvaluated.size() > 0) {
				startTestOfClauses(successorClausesToBeEvaluated);
			} else {
				startBodyOfSelectedClause(activation);
			}
		} else if(anyClauseFinishedBody) {
			ClauseActivation selectedClause = executionStatus.getClauseActivationWithExecutedBody(activation);
			finishConditionalNodeExecution(activation, selectedClause);
		}		
	}
	
	private void startTestOfClauses(List<ClauseActivation> clauseActivations) {
		for(ClauseActivation clauseActivation : clauseActivations) {
			clauseActivation.receiveControl();
		}
	}
	
	private void startBodyOfSelectedClause(ConditionalNodeActivation activation) {
		if (activation.selectedClauses.size() > 0 & activation.isRunning()) {
			int i = ((ChoiceStrategy) activation.getExecutionLocus().factory.getStrategy("choice")).choose(activation.selectedClauses.size());
			Clause selectedClause = activation.selectedClauses.getValue(i - 1);
			ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
			executionStatus.setClauseSelectedForExecutingBody(activation, selectedClause);
			
			ClauseList clauses = ((ConditionalNode)activation.node).clause;
			for (int j = 0; j < clauses.size(); j++) {
				Clause clause = clauses.getValue(j);
				if (clause != selectedClause) {
					ExecutableNodeList testNodes = clause.test;
					for (int k = 0; k < testNodes.size(); k++) {
						ExecutableNode testNode = testNodes.getValue(k);
						activation.activationGroup.getNodeActivation(testNode).terminate();
					}
				}
			}
			activation.activationGroup.runNodes(activation.makeActivityNodeList(selectedClause.body));
		}
	}
	
	private void finishConditionalNodeExecution(ConditionalNodeActivation activation, ClauseActivation selectedClause) {
		if(selectedClause != null) {
			OutputPinList resultPins = ((ConditionalNode)activation.node).result;
			OutputPinList bodyOutputPins = selectedClause.clause.bodyOutput;
			for (int k = 0; k < resultPins.size(); k++) {
				OutputPin resultPin = resultPins.getValue(k);
				OutputPin bodyOutputPin = bodyOutputPins.getValue(k);
				activation.putTokens(resultPin, activation.getPinValues(bodyOutputPin));
			}
			activation.activationGroup.terminateAll();
		}
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.removeConditionalNodeExecution(activation);
		handleEndOfStructuredActivityNodeExecution(activation);
	}

	/**
	 * Loop Nodes
	 */
	
	/**
	 * Prevents the method LoopNodeActivation.doStructuredActivity() from terminating all contained nodes
	 */
	
	private pointcut loopNodeTerminatesAll() : call (void ActivityNodeActivationGroup.terminateAll()) && withincode(void LoopNodeActivation.doStructuredActivity());
	
	void around() : loopNodeTerminatesAll() { 		
		return; 
	}
	
	private pointcut loopNodeStarts(LoopNodeActivation activation) : call(void StructuredActivityNodeActivation.doStructuredActivity()) && target(activation);
	
	before(LoopNodeActivation activation) : loopNodeStarts(activation) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.addLoopNodeExecution(activation);
	}
	
	private pointcut loopNodeStartsTestFirst(LoopNodeActivation activation) : call(boolean LoopNodeActivation.runTest()) && withincode(void LoopNodeActivation.doStructuredActivity()) && target(activation);
	
	before(LoopNodeActivation activation) : loopNodeStartsTestFirst(activation) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.loopNodeStartsTest(activation);
	}
	
	private pointcut loopNodeStartsBodyFirst(LoopNodeActivation activation) : call(void LoopNodeActivation.runBody()) && withincode(void LoopNodeActivation.doStructuredActivity()) && target(activation);
	
	before(LoopNodeActivation activation) : loopNodeStartsBodyFirst(activation) {
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.loopNodeStartsBody(activation);
	}
	
	private void checkStatusOfLoopNode(LoopNodeActivation activation) { 
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.updateStatusOfLoopNode(activation);
		
		if(executionStatus.isLoopNodeTestFinished(activation)) { 			
			if(isLoopNodeTestFulfilled(activation)) {
				runLoopNodeBody(activation);				
			} else {
				finishLoopNodeExecution(activation);
			}			
		} else if(executionStatus.isLoopBodyFinished(activation)) {
			finishLoopNodeBody(activation);
			runLoopNodeTest(activation);
		}
	}
	
	private void finishLoopNodeBody(LoopNodeActivation activation) {
		LoopNode loopNode = (LoopNode)activation.node;
		// START code from void LoopNodeActivation.runBody()
		OutputPinList bodyOutputs = loopNode.bodyOutput;
		ValuesList bodyOutputLists = activation.bodyOutputLists;
		for (int i = 0; i < bodyOutputs.size(); i++) {
			OutputPin bodyOutput = bodyOutputs.getValue(i);
			Values bodyOutputList = bodyOutputLists.getValue(i);
			bodyOutputList.values = activation.getPinValues(bodyOutput);
		}
		// END code from void LoopNodeActivation.runBody()
	}
	
	private boolean isLoopNodeTestFulfilled(LoopNodeActivation activation) {
		// START code from boolean LoopNodeActivation.runTest()
		ValueList values = activation.getPinValues(((LoopNode)activation.node).decider);
		boolean decision = false;
		if (values.size() > 0) {
			decision = ((BooleanValue) (values.getValue(0))).value;
		}
		// END code from boolean LoopNodeActivation.runTest()		
		return decision;
	}
	
	private void runLoopNodeBody(LoopNodeActivation activation) {
		if(!((LoopNode)activation.node).isTestedFirst) {
			prepareLoopIteration(activation);
		} else {
			finishLoopIteration(activation);
		}
		activation.runBody();
	}
	
	private void runLoopNodeTest(LoopNodeActivation activation) {
		if(((LoopNode)activation.node).isTestedFirst) {
			prepareLoopIteration(activation);
		} else {
			finishLoopIteration(activation);
		}
		activation.runTest();
	}
	
	private void prepareLoopIteration(LoopNodeActivation activation) {
		// START code from void LoopNodeActivation.doStructuredActivity()
		LoopNode loopNode = (LoopNode) (activation.node);
		OutputPinList loopVariables = loopNode.loopVariable;
		ValuesList bodyOutputLists = activation.bodyOutputLists;
		// Set loop variable values
		activation.runLoopVariables();
		for (int i = 0; i < loopVariables.size(); i++) {
			OutputPin loopVariable = loopVariables.getValue(i);
			Values bodyOutputList = bodyOutputLists.getValue(i);
			ValueList values = bodyOutputList.values;
			activation.putPinValues(loopVariable, values);
			((OutputPinActivation) activation.activationGroup
					.getNodeActivation(loopVariable)).sendUnofferedTokens();
		}

		// Run all the non-executable, non-pin nodes in the conditional
		// node.
		ActivityNodeActivationList nodeActivations = activation.activationGroup.nodeActivations;
		ActivityNodeActivationList nonExecutableNodeActivations = new ActivityNodeActivationList();
		for (int i = 0; i < nodeActivations.size(); i++) {
			ActivityNodeActivation nodeActivation = nodeActivations
					.getValue(i);
			if (!(nodeActivation.node instanceof ExecutableNode | nodeActivation.node instanceof Pin)) {
				nonExecutableNodeActivations.addValue(nodeActivation);
			}
		}
		activation.activationGroup.run(nonExecutableNodeActivations);
		// END code from void LoopNodeActivation.doStructuredActivity()
	}
	
	private void finishLoopIteration(LoopNodeActivation activation) {
		activation.activationGroup.terminateAll();
	}
	
	private void finishLoopNodeExecution(LoopNodeActivation activation) {
		LoopNode loopNode = (LoopNode)activation.node;
		
		// START code void LoopNodeActivation.doStructuredActivity()		
		ValuesList bodyOutputLists = activation.bodyOutputLists;
		OutputPinList resultPins = loopNode.result;
		for (int i = 0; i < bodyOutputLists.size(); i++) {
			Values bodyOutputList = bodyOutputLists.getValue(i);
			OutputPin resultPin = resultPins.getValue(i);
			activation.putTokens(resultPin, bodyOutputList.values);
		}
		// END code void LoopNodeActivation.doStructuredActivity()
		
		ExecutionStatus executionStatus = ExecutionContext.getInstance().getActivityExecutionStatus(activation.getActivityExecution());
		executionStatus.removeLoopNodeExecution(activation);
		handleEndOfStructuredActivityNodeExecution(activation);
	}
}