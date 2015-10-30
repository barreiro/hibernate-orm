/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.event.spi;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;

/**
 * Enumeration of the recognized types of events, including meta-information about each.
 *
 * @author Steve Ebersole
 */
public enum EventType {
	LOAD( "load", LoadEventListener.class ),
	RESOLVE_NATURAL_ID( "resolve-natural-id", ResolveNaturalIdEventListener.class ),

	INIT_COLLECTION( "load-collection", InitializeCollectionEventListener.class ),

	SAVE_UPDATE( "save-update", SaveOrUpdateEventListener.class ),
	UPDATE( "update", SaveOrUpdateEventListener.class ),
	SAVE( "save", SaveOrUpdateEventListener.class ),
	PERSIST( "create", PersistEventListener.class ),
	PERSIST_ONFLUSH( "create-onflush", PersistEventListener.class ),

	MERGE( "merge", MergeEventListener.class ),

	DELETE( "delete", DeleteEventListener.class ),

	REPLICATE( "replicate", ReplicateEventListener.class ),

	FLUSH( "flush", FlushEventListener.class ),
	AUTO_FLUSH( "auto-flush", AutoFlushEventListener.class ),
	DIRTY_CHECK( "dirty-check", DirtyCheckEventListener.class ),
	FLUSH_ENTITY( "flush-entity", FlushEntityEventListener.class ),

	CLEAR( "clear", ClearEventListener.class ),
	EVICT( "evict", EvictEventListener.class ),

	LOCK( "lock", LockEventListener.class ),

	REFRESH( "refresh", RefreshEventListener.class ),

	PRE_LOAD( "pre-load", PreLoadEventListener.class ),
	PRE_DELETE( "pre-delete", PreDeleteEventListener.class ),
	PRE_UPDATE( "pre-update", PreUpdateEventListener.class ),
	PRE_INSERT( "pre-insert", PreInsertEventListener.class ),

	POST_LOAD( "post-load", PostLoadEventListener.class ),
	POST_DELETE( "post-delete", PostDeleteEventListener.class ),
	POST_UPDATE( "post-update", PostUpdateEventListener.class ),
	POST_INSERT( "post-insert", PostInsertEventListener.class ),

	POST_COMMIT_DELETE( "post-commit-delete", PostDeleteEventListener.class ),
	POST_COMMIT_UPDATE( "post-commit-update", PostUpdateEventListener.class ),
	POST_COMMIT_INSERT( "post-commit-insert", PostInsertEventListener.class ),

	PRE_COLLECTION_RECREATE( "pre-collection-recreate", PreCollectionRecreateEventListener.class ),
	PRE_COLLECTION_REMOVE( "pre-collection-remove", PreCollectionRemoveEventListener.class ),
	PRE_COLLECTION_UPDATE( "pre-collection-update", PreCollectionUpdateEventListener.class ),

	POST_COLLECTION_RECREATE( "post-collection-recreate", PostCollectionRecreateEventListener.class ),
	POST_COLLECTION_REMOVE( "post-collection-remove", PostCollectionRemoveEventListener.class ),
	POST_COLLECTION_UPDATE( "post-collection-update", PostCollectionUpdateEventListener.class );

	/**
	 * Maintain a map of {@link EventType} instances keyed by name for lookup by name as well as {@link #values()}
	 * resolution.
	 */
	private static final Map<String,EventType> EVENT_TYPE_BY_NAME_MAP = AccessController.doPrivileged(
			new PrivilegedAction<Map<String, EventType>>() {
				@Override
				public Map<String, EventType> run() {
					final Map<String, EventType> typeByNameMap = new HashMap<String, EventType>();
					for ( Field field : EventType.class.getDeclaredFields() ) {
						if ( EventType.class.isAssignableFrom( field.getType() ) ) {
							try {
								final EventType typeField = (EventType) field.get( null );
								typeByNameMap.put( typeField.eventName(), typeField );
							}
							catch (Exception t) {
								throw new HibernateException( "Unable to initialize EventType map", t );
							}
						}
					}
					return typeByNameMap;
				}
			}
	);

	/**
	 * Find an {@link EventType} by its name
	 *
	 * @param eventName The name
	 *
	 * @return The {@link EventType} instance.
	 *
	 * @throws HibernateException If eventName is null, or if eventName does not correlate to any known event type.
	 */
	public static EventType resolveEventTypeByName(final String eventName) {
		if ( eventName == null ) {
			throw new HibernateException( "event name to resolve cannot be null" );
		}
		final EventType eventType = EVENT_TYPE_BY_NAME_MAP.get( eventName );
		if ( eventType == null ) {
			throw new HibernateException( "Unable to locate proper event type for event name [" + eventName + "]" );
		}
		return eventType;
	}


	private final String eventName;
	private final Class baseListenerInterface;

	EventType(String eventName, Class baseListenerInterface) {
		this.eventName = eventName;
		this.baseListenerInterface = baseListenerInterface;
	}

	public String eventName() {
		return eventName;
	}

	public Class baseListenerInterface() {
		return baseListenerInterface;
	}

	@Override
	public String toString() {
		return eventName();
	}
}
