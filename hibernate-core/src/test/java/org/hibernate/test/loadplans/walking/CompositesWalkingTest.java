/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.loadplans.walking;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metamodel.model.domain.spi.EntityTypeImplementor;
import org.hibernate.persister.walking.spi.MetamodelGraphWalker;

import org.junit.Test;

import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.hibernate.test.annotations.collectionelement.TestCourse;

/**
 * @author Steve Ebersole
 */
public class CompositesWalkingTest extends BaseUnitTestCase {
	/**
	 * Test one-level composites defined as part of an entity.
	 */
	@Test
	public void testEntityComposite() {
		final SessionFactory sf = new Configuration()
				.addAnnotatedClass( TestCourse.class )
				.buildSessionFactory();
		try {
			final EntityTypeImplementor ep = (EntityTypeImplementor) sf.getClassMetadata( TestCourse.class );
			MetamodelGraphWalker.visitEntity( new NavigableVisitationStrategyLoggingImpl(), ep );
		}
		finally {
			sf.close();
		}
	}
}
