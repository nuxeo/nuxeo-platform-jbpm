/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.jbpm.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.jbpm.VirtualTaskInstance;

/**
 * @author Anahide Tchertchian
 */
public interface JbpmActions extends Serializable {

    boolean getCanCreateProcess();

    boolean getCanManageProcess();

    boolean getCanManageParticipants();

    boolean getCanEndTask(TaskInstance taskInstance);

    String createProcessInstance(NuxeoPrincipal principal, String pd, DocumentModel dm, String endLifeCycle);

    ProcessInstance getCurrentProcess();

    String getCurrentProcessInitiator();

    List<TaskInstance> getCurrentTasks(String... taskNames);

    ArrayList<VirtualTaskInstance> getCurrentVirtualTasks();

    boolean getShowAddVirtualTaskForm();

    void toggleShowAddVirtualTaskForm(ActionEvent event);

    VirtualTaskInstance getNewVirtualTask();

    String addNewVirtualTask();

    String removeVirtualTask(int index);

    String moveUpVirtualTask(int index);

    String moveDownVirtualTask(int index);

    /**
     * Returns the list of allowed life cycle state transitions for given document.
     */
    List<String> getAllowedStateTransitions(DocumentRef ref);

    String getUserComment();

    void setUserComment(String comment);

    void validateTaskDueDate(FacesContext context, UIComponent component, Object value);

    boolean isProcessStarted(String startTaskName);

    String startProcess(String startTaskName);

    String validateTask(TaskInstance taskInstance, String transition);

    String rejectTask(TaskInstance taskInstance, String transition);

    String abandonCurrentProcess();

    void resetCurrentData();

    /**
     * Returns true if given document type has process definitions attached to it.
     *
     * @since 5.5
     * @param documentType the document type name
     */
    boolean hasProcessDefinitions(String documentType);

}
