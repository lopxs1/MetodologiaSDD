package br.org.amde.digital.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class PostgresDomainTypeContributor implements TypeContributor {

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        typeContributions.getTypeConfiguration()
                .getJdbcTypeRegistry()
                .addDescriptor(SqlTypes.DISTINCT, VarcharJdbcType.INSTANCE);
    }
}
