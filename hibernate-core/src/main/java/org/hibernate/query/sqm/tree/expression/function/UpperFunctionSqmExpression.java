/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import org.hibernate.metamodel.queryable.spi.BasicValuedExpressableType;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class UpperFunctionSqmExpression extends AbstractFunctionSqmExpression {
	public static final String NAME = "upper";

	private SqmExpression expression;

	public UpperFunctionSqmExpression(BasicValuedExpressableType resultType, SqmExpression expression) {
		super( resultType );
		this.expression = expression;

		assert expression != null;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUpperFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "UPPER(" + getExpression().asLoggableText() + ")";
	}
}
