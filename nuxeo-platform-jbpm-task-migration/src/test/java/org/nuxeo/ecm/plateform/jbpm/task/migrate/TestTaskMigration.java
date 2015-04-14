package org.nuxeo.ecm.plateform.jbpm.task.migrate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jbpm.taskmgmt.exe.TaskInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.UserPrincipal;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.JbpmTaskService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

@RunWith(FeaturesRunner.class)
@Features({ TransactionalFeature.class, CoreFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.query.api", //
        "org.nuxeo.ecm.platform.task.api", //
        "org.nuxeo.ecm.platform.task.core", //
        "org.nuxeo.ecm.platform.jbpm.api", //
        "org.nuxeo.ecm.platform.jbpm.core", //
        "org.nuxeo.ecm.platform.jbpm.testing", //
})
@LocalDeploy("org.nuxeo.ecm.platform.jbpm.task.migration:OSGI-INF/task-provider-contrib.xml")
public class TestTaskMigration {

    @Inject
    protected JbpmService jbpmService;

    @Inject
    protected JbpmTaskService jbpmTaskService;

    @Inject
    protected TaskService taskService;

    @Inject
    protected CoreSession session;

    protected DocumentModel doc;

    protected NuxeoPrincipal principal;

    protected SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    protected static final int NB_TASKS = 500;

    List<String> prefixedActorIds = new ArrayList<String>();

    @Before
    public void setUp() throws Exception {
        doc = session.createDocumentModel("/", "doc", "File");
        doc.setPropertyValue("dc:title", "MytestDoc");
        doc = session.createDocument(doc);
        session.save();

        principal = new UserPrincipal("toto", null, false, false);

        prefixedActorIds.add("user:tit'i");
    }

    protected void createJBPMTask(String taskName) throws Exception {
        Date dueDate = sdf.parse("12/25/2012");

        Map<String, Serializable> taskVariables = new HashMap<String, Serializable>();
        taskVariables.put("v1", "value1");
        taskVariables.put("v2", "value2");

        jbpmTaskService.createTask(session, principal, doc, taskName, prefixedActorIds, true, "directive", "comment",
                dueDate, taskVariables);

    }

    @Test
    public void testTaskMigration() throws Exception {
        // create JBPM Tasks
        for (int i = 0; i < NB_TASKS; i++) {
            createJBPMTask("TestTask-" + i);
        }

        // verify that the tasks are created
        List<TaskInstance> tis = jbpmService.getCurrentTaskInstances(prefixedActorIds, null);
        assertEquals(NB_TASKS, tis.size());

        DocumentModelList taskDocs = session.query("select * from TaskDoc");
        assertEquals(0, taskDocs.size());

        // call the wrapper service to triger migration
        long t0 = System.currentTimeMillis();
        List<Task> tasks = taskService.getCurrentTaskInstances(prefixedActorIds, session);
        assertEquals(NB_TASKS, tasks.size());
        long t1 = System.currentTimeMillis();
        long deltaS = (t1 - t0) / 1000;

        System.out.println("Migrated " + NB_TASKS + " tasks in " + deltaS + "s");
        System.out.println((NB_TASKS / deltaS) + " tasks/s");

        // check that there are no more JBPM tasks
        tis = jbpmService.getCurrentTaskInstances(prefixedActorIds, null);
        assertEquals(0, tis.size());

        // check that the Task docs were indeed created
        taskDocs = session.query("select * from TaskDoc");
        assertEquals(NB_TASKS, taskDocs.size());

        // check tasks attributes
        Task task = tasks.get(0);
        assertTrue(task.getName().startsWith("TestTask-"));
        assertEquals("directive", task.getDirective());
        assertEquals("comment", task.getComments().get(0).getText());
        assertEquals("toto", task.getInitiator());
        assertEquals("user:tit'i", task.getActors().get(0));
        assertTrue(task.getVariables().keySet().contains("v1"));
        assertTrue(task.getVariables().keySet().contains("v2"));
        assertEquals(doc.getId(), task.getTargetDocumentId());

    }

}
