/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.consume.results.internal.instantiation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.sql.ast.consume.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.ast.consume.results.spi.RowProcessingState;
import org.hibernate.sql.ast.consume.results.spi.QueryResultAssembler;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class QueryResultAssemblerListImpl implements QueryResultAssembler {
	private static final Logger log = Logger.getLogger( QueryResultAssemblerListImpl.class );

	private final List<ArgumentReader> argumentReaders;

	public QueryResultAssemblerListImpl(List<ArgumentReader> argumentReaders) {
		this.argumentReaders = argumentReaders;
	}

	@Override
	public Class getReturnedJavaType() {
		return List.class;
	}

	@Override
	public Object assemble(RowProcessingState rowProcessingState, JdbcValuesSourceProcessingOptions options) throws SQLException {
		final ArrayList<Object> result = new ArrayList<>();
		for ( ArgumentReader argumentReader : argumentReaders ) {
			result.add( argumentReader.assemble( rowProcessingState, options ) );
		}
		return result;
	}
}