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
package org.nuxeo.ecm.platform.jbpm.core.helper;

import java.io.Serializable;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.Assignable;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

// FIXME: this interface has no implementation. Remove?
public interface PublicationHelper extends Serializable {

    String decide(ExecutionContext executionContext) throws Exception;

    void publishDocument(CoreSession session, DocumentModel docToPublish, DocumentModel sectionToPublish);

    void assign(Assignable assignable, ExecutionContext executionContext) throws Exception;

}
