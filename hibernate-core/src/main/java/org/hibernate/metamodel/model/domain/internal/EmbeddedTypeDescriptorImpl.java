/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.boot.model.domain.EmbeddedMapping;
import org.hibernate.boot.model.domain.EmbeddedValueMapping;
import org.hibernate.boot.model.domain.PersistentAttributeMapping;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.AbstractManagedType;
import org.hibernate.metamodel.model.domain.spi.EmbeddedContainer;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.InheritanceCapable;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.PersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.RepresentationStrategy;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.type.descriptor.java.internal.EmbeddableJavaDescriptorImpl;
import org.hibernate.type.descriptor.java.spi.EmbeddableJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptorRegistry;

/**
 * @author Steve Ebersole
 */
public class EmbeddedTypeDescriptorImpl<T>
		extends AbstractManagedType<T>
		implements EmbeddedTypeDescriptor<T> {
	private final EmbeddedContainer container;
	private final NavigableRole navigableRole;

	private final RepresentationStrategy representationStrategy;

	public EmbeddedTypeDescriptorImpl(
			EmbeddedMapping embeddedMapping,
			EmbeddedContainer container,
			String localName,
			RuntimeModelCreationContext creationContext) {
		super(
				embeddedMapping,
				resolveJtd( creationContext, embeddedMapping ),
				creationContext
		);

		// todo (6.0) : support for specific MutalibilityPlan and Comparator

		this.container = container;
		this.navigableRole = container.getNavigableRole().append( localName );

		this.representationStrategy = creationContext.getMetadata().getMetadataBuildingOptions()
				.getRepresentationStrategySelector()
				.resolveRepresentationStrategy( embeddedMapping, creationContext);
	}

	@SuppressWarnings("unchecked")
	private static <T> EmbeddableJavaDescriptor<T> resolveJtd(RuntimeModelCreationContext creationContext, EmbeddedMapping embeddedMapping) {
		final JavaTypeDescriptorRegistry jtdr = creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry();

		EmbeddableJavaDescriptor<T> jtd = (EmbeddableJavaDescriptor<T>) jtdr.getDescriptor( embeddedMapping.getName() );
		if ( jtd == null ) {
			final Class<T> javaType;
			if ( StringHelper.isEmpty( embeddedMapping.getEmbeddableClassName() ) ) {
				javaType = null;
			}
			else {
				javaType = creationContext.getSessionFactory()
						.getServiceRegistry()
						.getService( ClassLoaderService.class )
						.classForName( embeddedMapping.getEmbeddableClassName() );
			}

			jtd = new EmbeddableJavaDescriptorImpl(
					embeddedMapping.getName(),
					javaType,
					null
			);
			jtdr.addDescriptor( jtd );
		}
		return jtd;
	}

	@Override
	public void finishInstantiation(
			EmbeddedValueMapping embeddedValueMapping,
			RuntimeModelCreationContext creationContext) {
		bindAttributes( embeddedValueMapping, creationContext );
	}

	@SuppressWarnings("AccessStaticViaInstance")
	private void bindAttributes(EmbeddedValueMapping embeddedValueMapping, RuntimeModelCreationContext creationContext) {
		for ( PersistentAttributeMapping attributeMapping : embeddedValueMapping.getDeclaredPersistentAttributes() ) {
			final PersistentAttribute persistentAttribute = attributeMapping.makeRuntimeAttribute(
					this,
					embeddedValueMapping,
					SingularPersistentAttribute.Disposition.NORMAL,
					creationContext
			);

			addAttribute( persistentAttribute );
		}
	}

	@Override
	public void completeInitialization(
			EmbeddedValueMapping embeddedValueMapping,
			RuntimeModelCreationContext creationContext) {
		// todo (6.0) : I think we will want some form of "after all mappings have been 'initialized' (see #finishInstantiation)"
	}

	@Override
	public EmbeddedContainer<?> getContainer() {
		return container;
	}

	@Override
	public EmbeddedTypeDescriptor<T> getEmbeddedDescriptor() {
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EmbeddableJavaDescriptor<T> getJavaTypeDescriptor() {
		return (EmbeddableJavaDescriptor<T>) super.getJavaTypeDescriptor();
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public void visitNavigable(NavigableVisitationStrategy visitor) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public List<Column> collectColumns() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public List<InheritanceCapable<? extends T>> getSubclassTypes() {
		return Collections.emptyList();
	}

	@Override
	public void injectSuperTypeDescriptor(InheritanceCapable<? super T> superType) {
		throw new UnsupportedOperationException( "Embeddable inheritance is not yet implemented" );
	}

	@Override
	public void addSubclassType(InheritanceCapable<? extends T> subclassType) {
		throw new UnsupportedOperationException( "Embeddable inheritance is not yet implemented" );
	}

	@Override
	public void setPropertyValues(Object object, Object[] values) {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public void setPropertyValue(Object object, int i, Object value) {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public Object[] getPropertyValues(Object object) {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public Object getPropertyValue(Object object, int i) throws HibernateException {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public Object getPropertyValue(Object object, String propertyName) {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public boolean[] getPropertyNullability() {
		throw new NotYetImplementedFor6Exception(  );
	}
}