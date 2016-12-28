/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.spi.descriptor.java.managed;

import javax.persistence.metamodel.MappedSuperclassType;

/**
 * @author Steve Ebersole
 */
public interface JavaTypeDescriptorMappedSuperclassImplementor
		extends JavaTypeDescriptorIdentifiableImplementor, MappedSuperclassType {
	@Override
	default PersistenceType getPersistenceType() {
		return PersistenceType.MAPPED_SUPERCLASS;
	}

	@Override
	JavaTypeDescriptorIdentifiableImplementor getSupertype();
}