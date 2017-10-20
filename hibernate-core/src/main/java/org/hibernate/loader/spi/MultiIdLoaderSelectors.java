/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.loader.spi;

import org.hibernate.LockOptions;

/**
 * Information that can influence the loader instance returned.
 *
 * @author Steve Ebersole
 */
public interface MultiIdLoaderSelectors {
	boolean isOrderReturnEnabled();
	Integer getBatchSize();
	LockOptions getLockOptions();
}