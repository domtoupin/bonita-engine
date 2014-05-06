package com.bonitasoft.engine.bdm;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Romain Bioteau
 */
public class BDMQueryUtilTest {

    @Test
    public void should_createQueryNameForUniqueConstraint_return_queryname() throws Exception {
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));
        final String queryNameForUniqueConstraint = BDMQueryUtil.createQueryNameForUniqueConstraint(uniqueConstraint);
        assertThat(queryNameForUniqueConstraint).isEqualTo("findByName");

    }

    @Test
    public void should_createQueryContentForUniqueConstraint_return_query_content_with_parameters() throws Exception {
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));
        final String queryContentForUniqueConstraint = BDMQueryUtil.createQueryContentForUniqueConstraint("org.bonita.Employee", uniqueConstraint);
        assertThat(queryContentForUniqueConstraint).isNotEmpty().isEqualTo("SELECT e\nFROM Employee e\nWHERE e.name= :name\n");
    }

    @Test
    public void createProvidedQueriesForBOShouldNotGenerateGetAllForUniqueConstraint() throws Exception {
        // given:
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("com.corp.Arrival");
        final Field field = new Field();
        field.setName("people");
        field.setType(FieldType.INTEGER);
        bo.addField(field);
        bo.addUniqueConstraint("someName", "people");

        // when:
        final List<Query> queries = BDMQueryUtil.createProvidedQueriesForBusinessObject(bo);

        // then:
        assertThat(getQueryNames(queries)).doesNotContain("getAllArrivalByPeople");
    }

    @Test
    public void createProvidedQueriesForBOWithOneUniqueAndOneNonUniqueFieldShouldGenerate3Queries() throws Exception {
        // given:
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("com.corp.Arrival");
        bo.addField(aStringField("unikAttr"));
        bo.addField(aStringField("unconstrainedAttr"));
        bo.addUniqueConstraint("someConstraintName", "unikAttr");

        // when:
        final List<Query> queries = BDMQueryUtil.createProvidedQueriesForBusinessObject(bo);

        // then:
        assertThat(queries).hasSize(3);
    }

    protected Field aStringField(final String name) {
        final Field field = new Field();
        field.setName(name);
        field.setType(FieldType.STRING);
        return field;
    }

    private List<String> getQueryNames(final List<Query> queries) {
        return extract(queries, on(Query.class).getName());
    }

    @Test
    public void should_createQuerForUniqueConstraint_return_query_with_parameters() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final Field field = new Field();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));

        final Query query = BDMQueryUtil.createQueryForUniqueConstraint(bo, uniqueConstraint);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).isEqualTo("SELECT e\nFROM Employee e\nWHERE e.name= :name\n");
        assertThat(query.getName()).isEqualTo("findByName");
        assertThat(query.getReturnType()).isEqualTo(bo.getQualifiedName());
        assertThat(query.getQueryParameters()).hasSize(1);
    }

    @Test
    public void should_createQueryForField_return_query_with_parameters() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final Field field = new Field();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        final Query query = BDMQueryUtil.createQueryForField(bo, field);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).contains("SELECT e\nFROM Employee e\nWHERE e.name= :name\nORDER BY e.persistenceId");
        assertThat(query.getName()).isEqualTo("findByName");
        assertThat(query.getReturnType()).isEqualTo(List.class.getName());
        assertThat(query.getQueryParameters()).hasSize(1);
    }

    @Test
    public void should_createSelectAllQueryreturn_query_without_parameters() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final Field field = new Field();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        final Query query = BDMQueryUtil.createSelectAllQueryForBusinessObject(bo);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).contains("SELECT e\nFROM Employee e");
        assertThat(query.getContent()).doesNotContain("WHERE");
        assertThat(query.getName()).isEqualTo("find");
        assertThat(query.getReturnType()).isEqualTo(List.class.getName());
        assertThat(query.getQueryParameters()).isEmpty();
    }

    @Test
    public void createSelectAllQueryShouldGenerateOrderByPersistenceId() throws Exception {
        // when:
        final String queryContent = BDMQueryUtil.createSelectAllQueryContent("MyBizObject");
        // then:
        assertThat(queryContent).contains("ORDER BY m.persistenceId");
    }

    @Test
    public void createDefaultQueryForFieldShouldGenerateOrderByPersistenceId() throws Exception {
        // when:
        final String queryContent = BDMQueryUtil.createQueryContentForField("NerfSurvey", new Field());
        // then:
        assertThat(queryContent).contains("ORDER BY n.persistenceId");
    }

}
