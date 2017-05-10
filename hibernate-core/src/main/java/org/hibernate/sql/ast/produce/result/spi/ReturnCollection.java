/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.result.spi;

/**
 * Models the a persistent collection as root {@link Return}.  Pertinent to collection initializers only.
 *
 * @author Steve Ebersole
 */
public interface ReturnCollection extends CollectionReference, Return {
}
