/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public interface ApplicationService {

    String APPLICATION = "APPLICATION";

    String APPLICATION_PAGE = "APPLICATION_PAGE";

    SApplication createApplication(SApplication application) throws SObjectCreationException, SObjectAlreadyExistsException, SInvalidNameException;

    SApplication getApplication(long applicationId) throws SBonitaReadException, SObjectNotFoundException;

    void deleteApplication(long applicationId) throws SObjectModificationException, SObjectNotFoundException;

    SApplication updateApplication(long applicationId, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException;

    long getNumberOfApplications(QueryOptions options) throws SBonitaReadException;

    List<SApplication> searchApplications(QueryOptions options) throws SBonitaSearchException;

    SApplicationPage createApplicationPage(SApplicationPage applicationPage) throws SObjectCreationException, SObjectAlreadyExistsException, SInvalidNameException;

    SApplicationPage getApplicationPage(String applicationName, String applicationPageName) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage getApplicationPage(long applicationPageId) throws SBonitaReadException, SObjectNotFoundException;

    SApplicationPage getApplicationHomePage(long applicationId) throws SBonitaReadException, SObjectNotFoundException;

    void deleteApplicationPage(long applicationpPageId) throws SObjectModificationException, SObjectNotFoundException;

    long getNumberOfApplicationPages(final QueryOptions options) throws SBonitaReadException;

    List<SApplicationPage> searchApplicationPages(final QueryOptions options) throws SBonitaSearchException;
}