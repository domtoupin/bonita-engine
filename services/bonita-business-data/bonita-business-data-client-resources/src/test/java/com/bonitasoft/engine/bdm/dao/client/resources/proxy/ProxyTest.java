/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.dao.client.resources.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.proxy.assertion.ProxyAssert;
import com.bonitasoft.engine.bdm.proxy.model.TestEntity;

@RunWith(MockitoJUnitRunner.class)
public class ProxyTest {

    @Mock
    private LazyLoader lazyLoader;

    @InjectMocks
    private Proxyfier proxyfier;

    private TestEntity mockLazyLoaderToReturn(TestEntity entity) {
        when(lazyLoader.load(any(Method.class), any(Long.class))).thenReturn(entity);
        return entity;
    }

    @Test
    public void should_load_object_when_method_is_lazy_and_object_is_not_loaded() {
        TestEntity expectedEntity = mockLazyLoaderToReturn(new TestEntity());
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        TestEntity lazyEntity = entity.getLazyEntity();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
        assertThat(lazyEntity).isEqualTo(expectedEntity);
    }

    @Test
    public void should_load_object_when_method_is_lazy_and_object_is_an_empty_list() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getLazyEntityList();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
    }

    @Test
    public void should_return_value_when_it_has_been_already_loaded_before_proxyfication() {
        final String name = "this is a preloaded value";
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity = proxyfier.proxify(entity);

        final String proxyName = entity.getName();

        verifyZeroInteractions(lazyLoader);
        assertThat(proxyName).isEqualTo(name);
    }

    @Test
    public void should_not_load_entity_when_it_has_been_already_loaded_before_proxyfication() {
        final TestEntity alreadySetEntity = new TestEntity();
        alreadySetEntity.setName("aDeepName");
        TestEntity entity = new TestEntity();
        entity.setLazyEntity(alreadySetEntity);
        entity = proxyfier.proxify(entity);

        final TestEntity loadedEntity = entity.getLazyEntity();

        verifyZeroInteractions(lazyLoader);
        assertThat(loadedEntity.getName()).isEqualTo(alreadySetEntity.getName());
    }

    @Test
    public void should_not_load_object_which_has_been_already_lazy_loaded() {
        TestEntity expectedEntity = mockLazyLoaderToReturn(new TestEntity());
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getLazyEntity();
        TestEntity lazyEntity = entity.getLazyEntity();

        verify(lazyLoader, times(1)).load(any(Method.class), any(Long.class));
        assertThat(lazyEntity).isEqualTo(expectedEntity);
    }

    @Test
    public void should_not_load_object_for_a_non_lazy_loading_method() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getEagerEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void should_not_load_object_that_has_been_set_by_a_setter() {
        TestEntity expectedEntity = new TestEntity();
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.setLazyEntity(expectedEntity);
        TestEntity lazyEntity = entity.getLazyEntity();

        verifyZeroInteractions(lazyLoader);
        assertThat(lazyEntity).isEqualTo(expectedEntity);
    }

    @Test
    public void should_return_a_proxy_when_calling_a_getter_returning_an_entity() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final TestEntity eagerEntity = entity.getEagerEntity();

        ProxyAssert.assertThat(eagerEntity).isAProxy();
    }

    @Test
    public void should_not_return_a_proxy_when_calling_a_getter_not_returning_an_entity() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());
        entity.setName("aName");

        final String name = entity.getName();

        ProxyAssert.assertThat(name).isNotAProxy();
    }

    @Test
    public void should_return_a_list_of_proxies_when_calling_a_getter_returning_a_list_of_entities() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final List<TestEntity> entities = entity.getEagerEntities();

        for (final TestEntity e : entities) {
            ProxyAssert.assertThat(e).isAProxy();
        }
    }

    @Test
    public void should_not_return_a_list_of_proxies_when_calling_a_getter_not_returning_a_list_of_entities() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final List<String> strings = entity.getStrings();

        for (final String string : strings) {
            ProxyAssert.assertThat(string).isNotAProxy();
        }
    }
}
