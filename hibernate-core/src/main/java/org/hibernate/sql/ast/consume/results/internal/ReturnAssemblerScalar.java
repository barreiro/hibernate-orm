/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.consume.results.internal;

import java.sql.SQLException;

import org.hibernate.sql.ast.tree.spi.select.SqlSelection;
import org.hibernate.sql.ast.produce.result.spi.ReturnScalar;
import org.hibernate.sql.ast.consume.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.ast.consume.results.spi.RowProcessingState;
import org.hibernate.sql.ast.consume.results.spi.ReturnAssembler;

/**
 * @author Steve Ebersole
 */
public class ReturnAssemblerScalar implements ReturnAssembler {
	private final SqlSelection sqlSelection;
	private final ReturnScalar returnScalar;

	public ReturnAssemblerScalar(SqlSelection sqlSelection, ReturnScalar returnScalar) {
		this.sqlSelection = sqlSelection;
		this.returnScalar = returnScalar;
	}

	@Override
	public Class getReturnedJavaType() {
		// todo (6.0) : remove the ReturnAssembler#getReturnedJavaType method.
		//		It is only used for resolving dynamic-instantiation arguments which should
		//		not be modeled as Returns anyway...
		return returnScalar.getReturnedJavaType();
	}

	@Override
	public Object assemble(
			RowProcessingState rowProcessingState,
			JdbcValuesSourceProcessingOptions options) throws SQLException {
		// todo (6.0) : apply AttributeConverter?  Or, what calls this?  Maybe there?
		return rowProcessingState.getJdbcValues()[ sqlSelection.getValuesArrayPosition() ];
	}
}
