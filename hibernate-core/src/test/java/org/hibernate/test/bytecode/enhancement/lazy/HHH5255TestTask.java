/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.bytecode.enhancement.lazy;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import org.hibernate.test.bytecode.enhancement.AbstractEnhancerTestTask;

/**
 * @author Luis Barreiro
 */
public class HHH5255TestTask extends AbstractEnhancerTestTask {


	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {Record.class};
	}

	public void prepare() {
		Configuration cfg = new Configuration();
		cfg.setProperty( Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true" );
		cfg.setProperty( Environment.USE_SECOND_LEVEL_CACHE, "false" );
		super.prepare( cfg );
	}

	public void execute() {
		Session s = getFactory().openSession();
		s.getTransaction().begin();

		Record record = new Record();
		record.setFileContent( new byte[0] );
		s.persist( record );

		s.getTransaction().commit();
		s.close();
		// Detached

		record.setDate( new Date() );

		s = getFactory().openSession();
		s.getTransaction().begin();

		s.merge( record );

		s.getTransaction().commit();
		s.close();
	}

	protected void cleanup() {
	}

}
