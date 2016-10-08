/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.bytecode.enhancement.lazy;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.test.bytecode.enhancement.AbstractEnhancerTestTask;
import org.junit.Assert;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * @author Gail Badner
 */
public class MultipleLazyCollectionTestTask extends AbstractEnhancerTestTask {

	private Long entityId;

	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {Entity1.class, Entity2.class};
	}

	public void prepare() {
		Configuration cfg = new Configuration();
		cfg.setProperty( Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true" );
		cfg.setProperty( Environment.USE_SECOND_LEVEL_CACHE, "false" );
		super.prepare( cfg );

		Session s = getFactory().openSession();
		s.beginTransaction();

		// step 1: insert entity1
		Entity1 entity1 = new Entity1();
		entity1.id = 1L;
		s.persist( entity1 );

		s.getTransaction().commit();
		s.clear();
		s.close();
	}

	public void execute() {
		Session s = getFactory().openSession();
		s.beginTransaction();

		// step 2: read entity1 and update
		Entity1 entity1 = s.find(Entity1.class, 1L);
		Assert.assertNull( entity1.text );
		entity1.text = "xxx";

		// IMPORTANT: uncommenting the next line makes the test pass under hibernate 5.2.2
		// entity1.children.size();

		// Barreiro: uncommenting makes hasUninitializedLazyProperties( object ) return false, so that
		// AbstractEntityPersister#getUpdateStrings returns SQLUpdateStrings instead of SQLLazyUpdateStrings

		s.getTransaction().commit();
		s.close();

		// --- //

		s = getFactory().openSession();
		s.beginTransaction();

		// step 3: verify entity1
		entity1 = s.find(Entity1.class, 1L);
		Assert.assertNotNull("field text should have been updated", entity1.text );

		s.getTransaction().commit();
		s.close();
	}

	protected void cleanup() {
	}

	@Entity private static class Entity1 {

		@Id private long id;

		@Basic(fetch = FetchType.LAZY)
		private String text;

		@OneToMany(mappedBy="parent")
		private List<Entity2> children;
	}

	@Entity private static class Entity2 {

		@Id
		private Long id;

		@ManyToOne
		private Entity1 parent;

	}
}
