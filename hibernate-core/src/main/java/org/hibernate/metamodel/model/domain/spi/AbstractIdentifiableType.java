/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.boot.model.domain.IdentifiableTypeMapping;
import org.hibernate.boot.model.domain.PersistentAttributeMapping;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.type.descriptor.java.spi.IdentifiableJavaDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifiableType<T> extends AbstractManagedType<T> implements IdentifiableTypeDescriptor<T> {
	private EntityHierarchy entityHierarchy;
	private IdentifiableTypeDescriptor<? super T> superclassType;

	public AbstractIdentifiableType(
			IdentifiableTypeMapping bootMapping,
			IdentifiableJavaDescriptor<T> javaTypeDescriptor,
			RuntimeModelCreationContext creationContext) {
		super( bootMapping, javaTypeDescriptor, creationContext );
	}

	@Override
	public IdentifiableJavaDescriptor<T> getJavaTypeDescriptor() {
		return (IdentifiableJavaDescriptor<T>) super.getJavaTypeDescriptor();
	}

	@SuppressWarnings("unchecked")
	public IdentifiableTypeDescriptor<? super T> getSuperclassType() {
		return superclassType;
	}

	@Override
	public EntityHierarchy getHierarchy() {
		return entityHierarchy;
	}

	@Override
	public void visitDeclaredNavigables(NavigableVisitationStrategy visitor) {
		getHierarchy().getIdentifierDescriptor().visitNavigable( visitor );
		super.visitDeclaredNavigables( visitor );
	}

	public void finishInitialization(
			EntityHierarchy entityHierarchy,
			IdentifiableTypeDescriptor<? super T> superType,
			IdentifiableTypeMapping mappingDescriptor,
			RuntimeModelCreationContext creationContext) {
		this.entityHierarchy = entityHierarchy;
		this.superclassType = superType;

		for ( PersistentAttributeMapping attributeMapping : mappingDescriptor.getDeclaredPersistentAttributes() ) {
			final PersistentAttribute persistentAttribute = attributeMapping.makeRuntimeAttribute(
					this,
					mappingDescriptor,
					SingularPersistentAttribute.Disposition.NORMAL,
					creationContext
			);
			addAttribute( persistentAttribute );
		}
	}
}