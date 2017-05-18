/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.tuple.entity;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.EntityTypeImplementor;
import org.hibernate.tuple.AbstractNonIdentifierAttribute;
import org.hibernate.tuple.BaselineAttributeInformation;
import org.hibernate.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractEntityBasedAttribute extends AbstractNonIdentifierAttribute {
	protected AbstractEntityBasedAttribute(
			EntityTypeImplementor source,
			SessionFactoryImplementor sessionFactory,
			int attributeNumber,
			String attributeName,
			Type attributeType,
			BaselineAttributeInformation attributeInformation) {
		super( source, sessionFactory, attributeNumber, attributeName, attributeType, attributeInformation );
	}

	@Override
	public EntityTypeImplementor getSource() {
		return (EntityTypeImplementor) super.getSource();
	}
}
