/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.cache.jcache.access;

import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.jcache.JCacheCollectionRegion;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionMetadata;

/**
 * @author Alex Snaps
 */
public class ReadWriteCollectionRegionAccessStrategy
		extends AbstractReadWriteRegionAccessStrategy<JCacheCollectionRegion>
		implements CollectionRegionAccessStrategy {

	public ReadWriteCollectionRegionAccessStrategy(JCacheCollectionRegion jCacheCollectionRegion) {
		super( jCacheCollectionRegion );
	}

	@Override
	public Object generateCacheKey(Object id, PersistentCollectionMetadata persister, SessionFactoryImplementor factory, String tenantIdentifier) {
		return DefaultCacheKeysFactory.createCollectionKey( id, persister, factory, tenantIdentifier );
	}

	@Override
	public Object getCacheKeyId(Object cacheKey) {
		return DefaultCacheKeysFactory.getCollectionId( cacheKey );
	}

}
