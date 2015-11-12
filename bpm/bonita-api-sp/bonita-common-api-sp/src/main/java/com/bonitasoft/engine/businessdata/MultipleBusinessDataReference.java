/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata;

import java.util.List;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;

/**
 * A <code>MultipleBusinessDataReference</code> is a reference of a {@link BusinessDataDefinition} which is multiple.
 * 
 * @author Matthieu Chaffotte
 * @deprecated since version 7.0.0, use {@link org.bonitasoft.engine.business.data.MultipleBusinessDataReference}
 */
@Deprecated
public interface MultipleBusinessDataReference extends BusinessDataReference {

    /**
     * Lists the business data identifiers.
     *
     * @return the business data identifiers.
     */
    List<Long> getStorageIds();

}