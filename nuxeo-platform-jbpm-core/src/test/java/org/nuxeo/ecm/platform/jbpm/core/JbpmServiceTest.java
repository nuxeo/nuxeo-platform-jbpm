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
 *     arussel
 */
package org.nuxeo.ecm.platform.jbpm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.jbpm.JbpmOperation;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.NuxeoJbpmException;
import org.nuxeo.ecm.platform.jbpm.VirtualTaskInstance;
import org.nuxeo.ecm.platform.jbpm.core.service.JbpmServiceImpl;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author arussel
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.directory", //
        "org.nuxeo.ecm.platform.usermanager", //
        "org.nuxeo.ecm.directory.types.contrib", //
        "org.nuxeo.ecm.directory.sql", //
        "org.nuxeo.ecm.platform.jbpm.core", //
        "org.nuxeo.ecm.platform.jbpm.testing", //
})
@LocalDeploy("org.nuxeo.ecm.platform.jbpm.core.test:OSGI-INF/jbpmService-contrib.xml")
public class JbpmServiceTest {

    @Inject
    private JbpmService service;

    @Inject
    private UserManager userManager;

    @Inject
    protected CoreSession session;

    private NuxeoPrincipal administrator;

    private NuxeoPrincipal user1;

    @Before
    public void setUp() throws Exception {
        // clean up previous test.
        JbpmServiceImpl.contexts.set(null);

        administrator = userManager.getPrincipal(SecurityConstants.ADMINISTRATOR);
        user1 = userManager.getPrincipal("myuser1");
    }

    @After
    public void tearDown() throws Exception {
        JbpmServiceImpl.contexts.set(null);
    }

    @Test
    public void testTypeFilter() {
        Map<String, List<String>> typeFilters = service.getTypeFilterConfiguration();
        assertEquals(2, typeFilters.size());
        assertEquals(2, typeFilters.get("Note").size());
        assertTrue(typeFilters.get("Note").contains("review_parallel"));
        assertTrue(typeFilters.get("Note").contains("review_approbation"));
    }

    @Test
    public void testProcessInstanceLifecycle() throws Exception {
        List<String> administratorList = new ArrayList<String>();
        administratorList.add(NuxeoPrincipal.PREFIX + administrator.getName());
        for (String group : administrator.getAllGroups()) {
            administratorList.add(NuxeoGroup.PREFIX + group);
        }
        DocumentModel dm = getDocument();
        assertNotNull(dm);

        // list process definition
        List<ProcessDefinition> pds = service.getProcessDefinitions(administrator, dm, null);
        assertNotNull(pds);
        assertEquals(2, pds.size());

        List<VirtualTaskInstance> participants = new ArrayList<VirtualTaskInstance>();
        participants.add(new VirtualTaskInstance("bob", "dobob", "yobob", null));
        participants.add(new VirtualTaskInstance("trudy", "dotrudy", "yotrudy", null));
        // create process instance
        ProcessInstance pd = service.createProcessInstance(administrator, "review_parallel", dm,
                Collections.singletonMap("participants", (Serializable) participants), null);
        Long pdId = Long.valueOf(pd.getId());
        assertNotNull(pd);
        assertEquals(pd.getContextInstance().getVariable(JbpmService.VariableName.initiator.name()),
                NuxeoPrincipal.PREFIX + SecurityConstants.ADMINISTRATOR);
        assertEquals(pd.getContextInstance().getVariable(JbpmService.VariableName.documentId.name()), dm.getId());
        assertEquals(pd.getContextInstance().getVariable(JbpmService.VariableName.documentRepositoryName.name()),
                dm.getRepositoryName());

        // get process instance
        List<ProcessInstance> pis1 = service.getCurrentProcessInstances(administrator, null);
        assertEquals(1, pis1.size());
        List<ProcessInstance> pis2 = service.getCurrentProcessInstances(administratorList, null);
        assertEquals(1, pis2.size());

        // get tasks
        List<TaskInstance> tasks = service.getTaskInstances(dm, administrator, null);
        List<TaskInstance> tasks2 = service.getTaskInstances(dm, administratorList, null);
        assertEquals(tasks2.size(), tasks.size());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        tasks = service.getCurrentTaskInstances(administrator, null);
        assertEquals(1, tasks.size());
        final long cancelledTi = tasks.get(0).getId();
        service.executeJbpmOperation(new JbpmOperation() {
            private static final long serialVersionUID = 1L;

            public Serializable run(JbpmContext context) throws NuxeoJbpmException {
                TaskInstance ti = context.getTaskInstance(cancelledTi);
                ti.cancel();
                return null;
            }
        });
        tasks = service.getCurrentTaskInstances(administrator, null);
        assertEquals(0, tasks.size());

        service.deleteProcessInstance(administrator, Long.valueOf(pd.getId()));
        pd = service.getProcessInstance(pdId);
        assertNull(pd);

        List<TaskInstance> tis = service.getCurrentTaskInstances(administrator, null);
        assertTrue(tis.isEmpty());
    }

    @Test
    public void testMultipleTaskPerDocument() throws Exception {
        DocumentModel dm = getDocument();
        assertNotNull(dm);

        // list process definition
        List<ProcessDefinition> pds = service.getProcessDefinitions(administrator, dm, null);
        assertNotNull(pds);
        assertEquals(2, pds.size());

        List<VirtualTaskInstance> participants = new ArrayList<VirtualTaskInstance>();
        String prefixedUser1 = NuxeoPrincipal.PREFIX + user1.getName();
        participants.add(new VirtualTaskInstance(prefixedUser1, "dobob1", "yobob1", null));
        participants.add(new VirtualTaskInstance(prefixedUser1, "dobob2", "yobob1", null));

        // create process instance
        service.createProcessInstance(administrator, "review_parallel", dm,
                Collections.singletonMap("participants", (Serializable) participants), null);
        List<TaskInstance> tasks = service.getTaskInstances(dm, administrator, null);
        service.endTask(Long.valueOf(tasks.get(0).getId()), null, null, null, null, null);
        // tasks.get(0).end();
        tasks = service.getTaskInstances(dm, administrator, null);
        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        tasks = service.getTaskInstances(dm, user1, null);
        assertEquals(2, tasks.size());
        List<String> transitions = service.getAvailableTransitions(tasks.get(0).getId(), user1);
        for (String t : transitions) {
            assertNotNull(t);
        }
        assertNotNull(transitions);
    }

    @Test
    public void testTaskManagement() throws Exception {
        DocumentModel dm = getDocument();
        TaskInstance ti = new TaskInstance();
        ti.setName("publication task");
        ti.setActorId("bob");
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(JbpmService.VariableName.documentId.name(), dm.getId());
        variables.put(JbpmService.VariableName.documentRepositoryName.name(), "demo");
        ti.addVariables(variables);
        service.saveTaskInstances(Collections.singletonList(ti));
        List<TaskInstance> lists = service.getTaskInstances(dm, new NuxeoPrincipalImpl("bob"), null);
        assertNotNull(lists);
    }

    @Test
    public void testProcessInstancePersistence() throws Exception {
        DocumentModel dm = getDocument();
        // create process instance
        ProcessInstance pi = service.createProcessInstance(administrator, "review_parallel", dm, null, null);
        Long pid = Long.valueOf(pi.getId());
        // edit
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("foo", "bar");
        pi.getContextInstance().addVariables(variables);
        service.persistProcessInstance(pi);

        ProcessInstance editedPi = service.getProcessInstance(pid);
        assertEquals("bar", editedPi.getContextInstance().getVariable("foo"));
    }

    protected DocumentModel getDocument() throws Exception {
        DocumentModel model = session.createDocumentModel(session.getRootDocument().getPathAsString(), "1", "File");
        DocumentModel doc = session.createDocument(model);
        assertNotNull(doc);

        session.saveDocument(doc);
        session.save();
        return doc;
    }

}
