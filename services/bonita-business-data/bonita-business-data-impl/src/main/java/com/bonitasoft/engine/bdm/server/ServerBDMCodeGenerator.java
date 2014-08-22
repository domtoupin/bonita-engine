/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.server;

import static com.bonitasoft.engine.bdm.validator.rule.QueryParameterValidationRule.FORBIDDEN_PARAMETER_NAMES;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;

import com.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import com.bonitasoft.engine.bdm.BDMQueryUtil;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.Query;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;

/**
 * @author Romain Bioteau
 * @author Emmanuel Duchastenier
 */
public class ServerBDMCodeGenerator extends AbstractBDMCodeGenerator {

    private static final String SERVER_DAO_PACKAGE_NAME = "server.";

    public ServerBDMCodeGenerator() {
        super();
    }

    @Override
    protected void addDAO(final BusinessObject bo, final JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        final JDefinedClass daoInterface = createDAOInterface(bo, entity);
        createDAOImpl(bo, entity, daoInterface);
    }

    private void createDAOImpl(final BusinessObject bo, final JDefinedClass entity, final JDefinedClass daoInterface) throws JClassAlreadyExistsException,
    ClassNotFoundException {
        final String daoImplClassName = toDaoImplClassname(bo);
        final JDefinedClass implClass = addClass(daoImplClassName);
        implClass._implements(daoInterface);

        final JFieldVar businessDataRepository = addConstructor(implClass);

        // Add method for provided queries
        for (final Query q : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            final JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(entity, method, q.getName(), entity.fullName(), businessDataRepository);
        }

        // Add method for queries
        for (final Query q : bo.getQueries()) {
            final JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(entity, method, q.getName(), entity.fullName(), businessDataRepository);
        }

        final JMethod method = createMethodForNewInstance(bo, entity, implClass);
        addNewInstanceMethodBody(method, entity);
    }

    // TODO unused ?
    protected JFieldVar addConstructor(final JDefinedClass implClass) {
        final JClass serviceClass = getModel().ref("com.bonitasoft.engine.business.data.BusinessDataRepository");
        final JFieldVar service = addField(implClass, "businessDataRepository", serviceClass);
        final JMethod constructor = implClass.constructor(JMod.PUBLIC);
        constructor.param(serviceClass, "businessDataRepository");
        final JBlock body = constructor.body();
        body.assign(JExpr.refthis("businessDataRepository"), JExpr.ref("businessDataRepository"));
        return service;
    }

    private void addQueryMethodBody(final JDefinedClass entity, final JMethod method, final String queryName, final String returnType,
            final JFieldVar businessDataRepository) throws ClassNotFoundException {
        final String entityName = entity.name();

        final JTryBlock tryBlock = method.body()._try();
        final JBlock tryBody = tryBlock.body();

        JClass queryParameterMapClass = getModel().ref(Map.class);
        queryParameterMapClass = queryParameterMapClass.narrow(String.class, Serializable.class);
        JClass hashMapClass = getModel().ref(HashMap.class);
        hashMapClass = hashMapClass.narrow(String.class, Serializable.class);
        final JVar queryParameterMap = tryBody.decl(queryParameterMapClass, "queryParameters", JExpr._new(hashMapClass));

        for (final JVar param : method.params()) {
            if (!FORBIDDEN_PARAMETER_NAMES.contains(param.name())) {
                tryBody.invoke(queryParameterMap, "put").arg(JExpr.lit(param.name())).arg(param);
            }
        }

        boolean isCollection = false;
        final JClass collectionClass = getModel().ref(Collection.class);
        if (method.type() instanceof JClass) {
            isCollection = collectionClass.isAssignableFrom((JClass) method.type());
        }
        if (isCollection) {
            final JVar[] listParams = method.listParams();
            final JVar startIndex = getMethodParam(listParams, BDMQueryUtil.START_INDEX_PARAM_NAME);
            final JVar maxResults = getMethodParam(listParams, BDMQueryUtil.MAX_RESULTS_PARAM_NAME);
            if (startIndex == null || maxResults == null) {
                throw new IllegalArgumentException("Neither 'startIndex' nor 'maxResults' parameters should be null");
            }
            tryBody._return(businessDataRepository.invoke("findListByNamedQuery").arg(JExpr.lit(entityName + "." + queryName)).arg(JExpr.dotclass(entity))
                    .arg(queryParameterMap).arg(startIndex).arg(maxResults));
        } else {
            final JClass returnTypeClass = entity.fullName().equals(method.type().fullName()) ? entity : getModel().ref(method.type().fullName());
            tryBody._return(businessDataRepository.invoke("findByNamedQuery").arg(JExpr.lit(entityName + "." + queryName)).arg(JExpr.dotclass(returnTypeClass))
                    .arg(queryParameterMap));
        }

        final JClass exceptionClass = getModel().ref(Exception.class);
        final JCatchBlock catchBlock = tryBlock._catch(exceptionClass);
        final JVar param = catchBlock.param("e");
        final JBlock catchBody = catchBlock.body();
        final JClass iaeClass = getModel().ref(SBonitaRuntimeException.class);
        catchBody._throw(JExpr._new(iaeClass).arg(JExpr.ref(null, param)));
    }

    protected JVar getMethodParam(final JVar[] params, final String paramName) {
        for (final JVar jVar : params) {
            if (paramName.equals(jVar.name())) {
                return jVar;
            }
        }
        return null;
    }

    protected String toDaoImplClassname(final BusinessObject bo) {
        final String boName = bo.getQualifiedName();
        final int pointIdx = boName.lastIndexOf('.');
        return boName.substring(0, pointIdx + 1) + SERVER_DAO_PACKAGE_NAME + boName.substring(pointIdx + 1) + DAO_IMPL_SUFFIX;
    }

}