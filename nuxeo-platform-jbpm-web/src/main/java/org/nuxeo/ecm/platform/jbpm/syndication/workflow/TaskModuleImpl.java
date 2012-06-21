/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     bchaffangeon
 *
 * $Id: TaskModuleImpl.java 25373 2007-09-25 08:03:06Z sfermigier $
 */

package org.nuxeo.ecm.platform.jbpm.syndication.workflow;

import java.util.Date;

import com.sun.syndication.feed.module.ModuleImpl;

/**
 * @author bchaffangeon
 */
@SuppressWarnings("serial")
public class TaskModuleImpl extends ModuleImpl implements TaskModule {

    private Date dueDate;

    private Date startDate;

    private String directive;

    private String description;

    private String name;

    private String comment;

    public TaskModuleImpl() {
        super(TaskModule.class, URI);
    }

    @Override
    public String getDirective() {
        return directive;
    }

    @Override
    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public void setDirective(String directive) {
        this.directive = directive;
    }

    @Override
    public void setDueDate(Date date) {
        dueDate = date;
    }

    @Override
    public void setStartDate(Date date) {
        startDate = date;
    }

    @Override
    public void copyFrom(Object obj) {
        TaskModule tm = (TaskModule) obj;
        dueDate = (Date) tm.getDueDate().clone();
        startDate = (Date) tm.getStartDate().clone();
        directive = tm.getDirective();
    }

    @Override
    public Class<TaskModule> getInterface() {
        return TaskModule.class;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

}
