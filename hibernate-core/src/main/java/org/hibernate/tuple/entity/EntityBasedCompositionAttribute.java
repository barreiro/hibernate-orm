/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.tuple.entity;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.EntityTypeImplementor;
import org.hibernate.persister.walking.spi.CompositionDefinition;
import org.hibernate.tuple.BaselineAttributeInformation;
import org.hibernate.tuple.component.AbstractCompositionAttribute;
import org.hibernate.type.spi.EmbeddedType;

/**
 * @author Steve Ebersole
 */
public class EntityBasedCompositionAttribute
		extends AbstractCompositionAttribute
		implements CompositionDefinition {

	public EntityBasedCompositionAttribute(
			EntityTypeImplementor source,
			SessionFactoryImplementor factory,
			int attributeNumber,
			String attributeName,
			EmbeddedType attributeType,
			BaselineAttributeInformation baselineInfo) {
		super( source, factory, attributeNumber, attributeName, attributeType, 0, baselineInfo );
	}

	@Override
	protected EntityTypeImplementor locateOwningPersister() {
		return (EntityTypeImplementor) getSource();
	}
}
