/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.persister.entity.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.persister.common.spi.PhysicalColumn;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.entity.spi.EntityHierarchy;
import org.hibernate.persister.entity.spi.EntityPersister;
import org.hibernate.persister.entity.spi.RowIdDescriptor;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.SingularAttributeReference;
import org.hibernate.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public class RowIdDescriptorImpl implements RowIdDescriptor {
	private final EntityHierarchy hierarchy;
	// todo : really need to expose AbstractEntityPersister.rowIdName for this to work.
	//		for now we will just always assume a selection name of "ROW_ID"
	private final PhysicalColumn column;

	public RowIdDescriptorImpl(EntityHierarchy hierarchy) {
		this.hierarchy = hierarchy;
		column = new PhysicalColumn(
				hierarchy.getRootEntityPersister().getRootTable(),
				"ROW_ID",
				Integer.MAX_VALUE
		);

	}

	@Override
	public Type getOrmType() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return null;
	}

	@Override
	public String getAttributeName() {
		return "<row_id>";
	}

	@Override
	public EntityPersister getAttributeContainer() {
		return hierarchy.getRootEntityPersister();
	}

	@Override
	public List<Column> getColumns() {
		return Collections.singletonList( column );
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public SingularAttributeReference.SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeReference.SingularAttributeClassification.BASIC;
	}

	@Override
	public String asLoggableText() {
		return "ROW_ID";
	}
}