/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.loadplans.walking;

import org.hibernate.metamodel.model.domain.spi.EntityTypeImplementor;
import org.hibernate.persister.walking.spi.MetamodelGraphWalker;

import org.junit.Test;

import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.hibernate.test.onetoone.formula.Address;

/**
 * @author Steve Ebersole
 */
public class KeyManyToOneWalkingTest extends BaseCoreFunctionalTestCase {
	@Override
	protected String[] getMappings() {
		return new String[] { "onetoone/formula/Person.hbm.xml" };
	}

	@Test
	public void testWalkingKeyManyToOneGraphs() {
		// Address has a composite id with a bi-directional key-many to Person
		final EntityTypeImplementor ep = (EntityTypeImplementor) sessionFactory().getClassMetadata( Address.class );

		MetamodelGraphWalker.visitEntity( new NavigableVisitationStrategyLoggingImpl(), ep );
	}
}
