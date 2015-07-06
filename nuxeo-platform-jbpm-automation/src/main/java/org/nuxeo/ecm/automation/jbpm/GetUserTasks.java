/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Stephane Lacoin
 */
package org.nuxeo.ecm.automation.jbpm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.Comment;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.jbpm.JbpmListFilter;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.JbpmService.TaskVariableName;

/**
 * Returns tasks assigned to current user or one of its groups.
 */
@Operation(id = GetUserTasks.ID, category = Constants.CAT_SERVICES, label = "Get user tasks", since = "5.4", description = "List tasks assigned to this user or one of its group."
        + "Task properties are serialized using JSON and returned in a Blob.")
public class GetUserTasks {

    public static final String ID = "Workflow.GetJBPMTask";

    private static final Log log = LogFactory.getLog(Log.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession repo;

    @Context
    protected JbpmService srv;

    @OperationMethod
    public Blob run() {
        NuxeoPrincipal principal = principal();
        List<TaskInstance> tasks = srv.getCurrentTaskInstances(principal, filter());
        if (tasks == null) {
            return null;
        }
        JSONArray rows = new JSONArray();
        for (TaskInstance task : tasks) {
            DocumentModel doc = null;
            try {
                doc = srv.getDocumentModel(task, principal);
            } catch (Exception e) {
                log.warn("Cannot get doc for task " + task.getId(), e);
            }
            if (doc == null) {
                log.warn(String.format("User '%s' has a task of type '%s' on an " + "unexisting or invisible document",
                        principal.getName(), task.getName()));
                continue;
            }
            JSONObject obj = new JSONObject();
            obj.element("id", task.getId()); // can be one or two (test or
            // suite)
            obj.element("docref", doc.getRef().toString());
            obj.element("name", task.getName());
            obj.element("description", task.getDescription());
            obj.element("startDate", task.getCreate());
            boolean expired = false;
            Date dueDate = task.getDueDate();
            obj.element("dueDate", task.getDueDate());
            if (dueDate != null) {
                expired = dueDate.before(new Date());
            }
            obj.element("expired", expired);
            obj.element("directive", task.getVariableLocally(TaskVariableName.directive.name()));
            @SuppressWarnings("unchecked")
            List<Comment> comments = task.getComments();
            String comment = "";
            if (comments != null && !comments.isEmpty()) {
                comment = comments.get(comments.size() - 1).getMessage();
            }
            obj.element("comment", comment);
            rows.add(obj);
        }
        return new StringBlob(rows.toString(), "application/json");
    }

    protected NuxeoPrincipal principal() {
        return (NuxeoPrincipal) ctx.getPrincipal();
    }

    protected JbpmListFilter filter() {
        return new JbpmListFilter() {
            private static final long serialVersionUID = 1L;

            @Override
            public <T> ArrayList<T> filter(JbpmContext jbpmContext, DocumentModel document, ArrayList<T> list,
                    NuxeoPrincipal principal) {
                return list;
            }
        };
    }
}
