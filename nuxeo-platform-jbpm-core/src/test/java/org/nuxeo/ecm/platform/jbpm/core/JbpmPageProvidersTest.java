/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.ecm.platform.jbpm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.JbpmTaskService;
import org.nuxeo.ecm.platform.jbpm.VirtualTaskInstance;
import org.nuxeo.ecm.platform.jbpm.core.service.JbpmServiceImpl;
import org.nuxeo.ecm.platform.jbpm.dashboard.DashBoardItem;
import org.nuxeo.ecm.platform.jbpm.dashboard.DocumentProcessItem;
import org.nuxeo.ecm.platform.jbpm.providers.UserTaskPageProvider;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @since 5.4.2
 */
@RunWith(FeaturesRunner.class)
@Features({ TransactionalFeature.class, CoreFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.directory", //
        "org.nuxeo.ecm.platform.usermanager", //
        "org.nuxeo.ecm.directory.types.contrib", //
        "org.nuxeo.ecm.directory.sql", //
        "org.nuxeo.ecm.platform.query.api", //
        "org.nuxeo.ecm.platform.jbpm.core", //
        "org.nuxeo.ecm.platform.jbpm.testing", //
})
@LocalDeploy({ "org.nuxeo.ecm.platform.jbpm.core.test:OSGI-INF/jbpmService-contrib.xml",
        "org.nuxeo.ecm.platform.jbpm.core.test:OSGI-INF/pageproviders-contrib.xml" })
public class JbpmPageProvidersTest {

    @Inject
    private JbpmService service;

    @Inject
    protected JbpmTaskService taskService;

    @Inject
    protected PageProviderService ppService;

    @Inject
    protected UserManager userManager;

    @Inject
    protected CoreSession session;

    protected NuxeoPrincipal administrator;

    protected DocumentModel document;

    @Before
    public void setUp() throws Exception {
        // clean up previous test.
        JbpmServiceImpl.contexts.set(null);

        administrator = userManager.getPrincipal(SecurityConstants.ADMINISTRATOR);
        document = getDocument();

        // create process instance
        List<VirtualTaskInstance> participants = new ArrayList<VirtualTaskInstance>();
        participants.add(new VirtualTaskInstance("bob", "dobob", "yobob", null));
        service.createProcessInstance(administrator, "review_parallel", document,
                Collections.singletonMap("participants", (Serializable) participants), null);
        // create isolated task
        List<String> actors = new ArrayList<String>();
        actors.add(NuxeoPrincipal.PREFIX + administrator.getName());
        actors.add(NuxeoGroup.PREFIX + SecurityConstants.MEMBERS);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2006, 6, 6);
        // create one task
        taskService.createTask(session, administrator, document, "Test Task Name", actors, false, "test directive",
                "test comment", calendar.getTime(), null);
        // create another task to check pagination
        taskService.createTask(session, administrator, document, "Test Task Name 2", actors, false, "test directive",
                "test comment", calendar.getTime(), null);
    }

    @After
    public void tearDown() throws Exception {
        JbpmServiceImpl.contexts.set(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTaskPageProvider() throws Exception {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put(UserTaskPageProvider.CORE_SESSION_PROPERTY, (Serializable) session);
        PageProvider<DashBoardItem> taskProvider = (PageProvider<DashBoardItem>) ppService.getPageProvider(
                "current_user_tasks", null, null, null, properties, (Object[]) null);
        List<DashBoardItem> tasks = taskProvider.getCurrentPage();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        // check process task
        DashBoardItem task = tasks.get(0);
        assertEquals("choose-participant", task.getName());
        assertNull(task.getComment());
        assertNull(task.getDescription());
        assertNull(task.getDirective());
        assertEquals(document.getRef(), task.getDocRef());
        assertEquals(document, task.getDocument());
        assertNull(task.getDueDate());
        assertNotNull(task.getStartDate());
        // check first single task
        task = tasks.get(1);
        assertEquals("Test Task Name", task.getName());
        assertEquals("test comment", task.getComment());
        assertNull(task.getDescription());
        assertEquals("test directive", task.getDirective());
        assertEquals(document.getRef(), task.getDocRef());
        assertEquals(document, task.getDocument());
        assertNotNull(task.getDueDate());
        assertNotNull(task.getStartDate());
        assertFalse(taskProvider.isPreviousPageAvailable());
        assertTrue(taskProvider.isNextPageAvailable());
        taskProvider.nextPage();
        tasks = taskProvider.getCurrentPage();
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        // check second single task
        task = tasks.get(0);
        assertEquals("Test Task Name 2", task.getName());
        assertEquals("test comment", task.getComment());
        assertNull(task.getDescription());
        assertEquals("test directive", task.getDirective());
        assertEquals(document.getRef(), task.getDocRef());
        assertEquals(document, task.getDocument());
        assertNotNull(task.getDueDate());
        assertNotNull(task.getStartDate());

        assertTrue(taskProvider.isPreviousPageAvailable());
        assertFalse(taskProvider.isNextPageAvailable());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessPageProvider() throws Exception {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put(UserTaskPageProvider.CORE_SESSION_PROPERTY, (Serializable) session);
        PageProvider<DocumentProcessItem> processProvider = (PageProvider<DocumentProcessItem>) ppService.getPageProvider(
                "current_user_processes", null, null, null, properties, (Object[]) null);
        List<DocumentProcessItem> processes = processProvider.getCurrentPage();
        assertNotNull(processes);
        assertEquals(1, processes.size());
        assertFalse(processProvider.isPreviousPageAvailable());
        assertFalse(processProvider.isNextPageAvailable());
        DocumentProcessItem process = processes.get(0);
        assertEquals("1", process.getDocTitle());
        assertEquals(document, process.getDocumentModel());
        assertEquals("review_parallel", process.getProcessInstanceName());
        assertNotNull(process.getProcessInstanceStartDate());
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
