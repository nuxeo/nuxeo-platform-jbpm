/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nicolas Ulrich
 *
 */

package org.nuxeo.ecm.platform.jbpm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.jbpm.JbpmTaskListService;
import org.nuxeo.ecm.platform.jbpm.TaskList;
import org.nuxeo.ecm.platform.jbpm.VirtualTaskInstance;
import org.nuxeo.ecm.platform.jbpm.core.service.JbpmServiceImpl;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.transaction.TransactionHelper;

@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.directory", //
        "org.nuxeo.ecm.directory.api", //
        "org.nuxeo.ecm.directory.sql", //
        "org.nuxeo.ecm.platform.usermanager", //
        "org.nuxeo.ecm.platform.usermanager.api", //
        "org.nuxeo.ecm.directory.types.contrib", //
        "org.nuxeo.ecm.platform.content.template", //
        "org.nuxeo.ecm.platform.userworkspace.api", //
        "org.nuxeo.ecm.platform.userworkspace.types", //
        "org.nuxeo.ecm.platform.jbpm.core", //
        "org.nuxeo.ecm.platform.jbpm.testing", //
})
@LocalDeploy({ "org.nuxeo.ecm.platform.userworkspace.core:OSGI-INF/userworkspace-framework.xml",
        "org.nuxeo.ecm.platform.userworkspace.core:OSGI-INF/userWorkspaceImpl.xml",
        "org.nuxeo.ecm.platform.jbpm.core.test:OSGI-INF/jbpmService-contrib.xml" })
public class JbpmTaskListServiceTest {

    public static String userWorkspacePath = "/default-domain/UserWorkspaces/Administrator";

    @Inject
    protected CoreFeature coreFeature;

    @Inject
    protected EventService eventService;

    @Inject
    protected CoreSession session;

    @Before
    public void setUp() throws Exception {
        // clean up previous test.
        JbpmServiceImpl.contexts.set(null);
    }

    @After
    public void tearDown() throws Exception {
        JbpmServiceImpl.contexts.set(null);
    }

    @Test
    public void testUserWorkspaceService() throws Exception {
        DocumentModel userWorkspace = getUserWorkspace(session);
        assertNotNull(userWorkspace);
    }

    @Test
    public void testAdapter() {
        DocumentModel doc = session.createDocumentModel("/", "list1", "TaskList");
        doc = session.createDocument(doc);
        assertNotNull(doc);

        TaskList list = doc.getAdapter(TaskList.class);
        assertNotNull(list);

        VirtualTaskInstance task = new VirtualTaskInstance();
        Date date = new GregorianCalendar().getTime();
        task.setActors(Arrays.asList("user1", "user2"));
        task.setDirective("directive1");
        task.setComment("comment1");
        task.getParameters().put("right", "Read");
        task.setDueDate(date);

        list.addTask(task);

        assertEquals(list.getTasks().get(0).getActors(), Arrays.asList("user1", "user2"));
        assertEquals("directive1", list.getTasks().get(0).getDirective());
        assertEquals("comment1", list.getTasks().get(0).getComment());
        assertEquals("Read", list.getTasks().get(0).getParameters().get("right"));
        assertEquals(list.getTasks().get(0).getDueDate(), date);
    }

    @Test
    public void testAdapterFail() {
        try {
            session.getRootDocument().getAdapter(TaskList.class);
            fail("Should throw exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void testTaskListService() throws Exception {

        // Retrieve the service
        JbpmTaskListService service = Framework.getService(JbpmTaskListService.class);
        assertNotNull(service);

        // Create a task list
        TaskList list = service.createTaskList(session, "List");
        assertNotNull(list);
        assertEquals("List", list.getName());

        // Add a task
        VirtualTaskInstance task = new VirtualTaskInstance();
        Date date = new GregorianCalendar().getTime();
        task.setActors(Arrays.asList("user1", "user2"));
        task.setDirective("directive1");
        task.setComment("comment1");
        task.getParameters().put("right", "Read");
        task.setDueDate(date);

        list.addTask(task);

        assertEquals(1, list.getTasks().size());
        assertEquals(list.getTasks().get(0).getActors(), Arrays.asList("user1", "user2"));
        assertEquals("directive1", list.getTasks().get(0).getDirective());
        assertEquals("comment1", list.getTasks().get(0).getComment());
        assertEquals(list.getTasks().get(0).getDueDate(), date);
        assertEquals("Read", list.getTasks().get(0).getParameters().get("right"));

        // Save the list
        service.saveTaskList(session, list);

        reOpenSession();

        // Try to load unknown list
        TaskList listFake = service.getTaskList(session, "ListFake");
        assertNull(listFake);
        assertEquals(1, service.getTaskLists(session).size());

        // Load the list
        TaskList list2 = service.getTaskList(session, list.getUUID());
        assertNotNull(list2);

        assertEquals(list.getTasks().size(), list2.getTasks().size());

        assertEquals(list.getTasks().get(0).getActors(), Arrays.asList("user1", "user2"));
        assertEquals("directive1", list.getTasks().get(0).getDirective());
        assertEquals("comment1", list.getTasks().get(0).getComment());
        assertEquals(list.getTasks().get(0).getDueDate(), date);
        assertEquals("Read", list.getTasks().get(0).getParameters().get("right"));

        // Try to delete an unknown it
        service.deleteTaskList(session, "ListFake");

        // Delete it
        service.deleteTaskList(session, list.getUUID());

        reOpenSession();

        // Check it is deleted
        TaskList list3 = service.getTaskList(session, list.getUUID());
        assertNull(list3);
    }

    private static DocumentModel getUserWorkspace(CoreSession session) {
        UserWorkspaceService uws = Framework.getLocalService(UserWorkspaceService.class);
        return uws.getCurrentUserPersonalWorkspace(session, null);
    }

    protected void reOpenSession() {
        session = coreFeature.reopenCoreSession();
    }

    protected void waitForAsyncCompletion() {
        if (TransactionHelper.isTransactionActiveOrMarkedRollback()) {
            TransactionHelper.commitOrRollbackTransaction();
            TransactionHelper.startTransaction();
        }
        eventService.waitForAsyncCompletion();
    }

}
