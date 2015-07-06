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
package org.nuxeo.ecm.platform.jbpm;

import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * @author arussel
 */
public class NuxeoJbpmException extends NuxeoException {

    private static final long serialVersionUID = 1L;

    public NuxeoJbpmException(String message, Throwable e) {
        super(message, e);
    }

    public NuxeoJbpmException(Throwable e) {
        super(e);
    }

    public NuxeoJbpmException(String message) {
        super(message);
    }

}
