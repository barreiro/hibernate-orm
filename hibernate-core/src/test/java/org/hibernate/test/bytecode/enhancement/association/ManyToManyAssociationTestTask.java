/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.bytecode.enhancement.association;

import java.util.HashSet;

import org.hibernate.test.bytecode.enhancement.AbstractEnhancerTestTask;
import org.junit.Assert;

/**
 * @author Luis Barreiro
 */
public class ManyToManyAssociationTestTask extends AbstractEnhancerTestTask {

	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {Group.class, User.class};
	}

	public void prepare() {
	}

	public void execute() {
		Group group = new Group();
		Group anotherGroup = new Group();

		User user = new User();
		User anotherUser = new User();

		user.addGroup( group );
		user.addGroup( anotherGroup );
		anotherUser.addGroup( group );

		Assert.assertTrue( group.getUsers().size() == 2 );

		group.setUsers( new HashSet<User>() );

		Assert.assertTrue( user.getGroups().size() == 1 );
	}

	protected void cleanup() {
	}
}
