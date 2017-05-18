/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.event.spi;

import java.io.Serializable;

import org.hibernate.metamodel.model.domain.spi.EntityTypeImplementor;

/**
 * Occurs afterQuery deleting an item from the datastore
 * 
 * @author Gavin King
 */
public class PostDeleteEvent extends AbstractEvent {
	private Object entity;
	private EntityTypeImplementor persister;
	private Serializable id;
	private Object[] deletedState;
	
	public PostDeleteEvent(
			Object entity, 
			Serializable id,
			Object[] deletedState,
			EntityTypeImplementor persister,
			EventSource source
	) {
		super(source);
		this.entity = entity;
		this.id = id;
		this.persister = persister;
		this.deletedState = deletedState;
	}
	
	public Serializable getId() {
		return id;
	}
	public EntityTypeImplementor getPersister() {
		return persister;
	}
	public Object getEntity() {
		return entity;
	}
	public Object[] getDeletedState() {
		return deletedState;
	}
}
