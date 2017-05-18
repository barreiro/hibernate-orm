/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.relational;

import org.hibernate.HibernateException;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.naming.Identifier;
import org.hibernate.metamodel.model.relational.spi.PhysicalNamingStrategy;

/**
 * Models a database {@code SEQUENCE}.
 *
 * @author Steve Ebersole
 */
public class Sequence implements MappedSequence {

	public static class Name extends QualifiedNameParser.NameParts {
		public Name(
				Identifier catalogIdentifier,
				Identifier schemaIdentifier,
				Identifier nameIdentifier) {
			super( catalogIdentifier, schemaIdentifier, nameIdentifier );
		}
	}

	private final QualifiedSequenceName logicalName;
	private final String exportIdentifier;
	private int initialValue = 1;
	private int incrementSize = 1;

	public Sequence(Identifier catalogName, Identifier schemaName, Identifier sequenceName) {
		this.logicalName = new QualifiedSequenceName( catalogName, schemaName, sequenceName );
		this.exportIdentifier = logicalName.render();
	}

	public Sequence(
			Identifier catalogName,
			Identifier schemaName,
			Identifier sequenceName,
			int initialValue,
			int incrementSize) {
		this( catalogName, schemaName, sequenceName );
		this.initialValue = initialValue;
		this.incrementSize = incrementSize;
	}

	/**
	 * @deprecated Use {@link #getLogicalName()} instead
	 */
	@Deprecated
	public QualifiedSequenceName getName() {
		return logicalName;
	}

	@Override
	public QualifiedSequenceName getLogicalName() {
		return logicalName;
	}

	@Override
	public String getExportIdentifier() {
		return exportIdentifier;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public int getIncrementSize() {
		return incrementSize;
	}

	@Override
	public void validate(int initialValue, int incrementSize) {
		if ( this.initialValue != initialValue ) {
			throw new HibernateException(
					String.format(
							"Multiple references to database sequence [%s] were encountered attempting to" +
									"set conflicting values for 'initial value'.  Found [%s] and [%s]",
							exportIdentifier,
							this.initialValue,
							initialValue
					)
			);
		}
		if ( this.incrementSize != incrementSize ) {
			throw new HibernateException(
					String.format(
							"Multiple references to database sequence [%s] were encountered attempting to" +
									"set conflicting values for 'increment size'.  Found [%s] and [%s]",
							exportIdentifier,
							this.incrementSize,
							incrementSize
					)
			);
		}
	}

	@Override
	public org.hibernate.metamodel.model.relational.spi.Sequence generateRuntimeSequence(
			PhysicalNamingStrategy namingStrategy,
			JdbcEnvironment jdbcEnvironment) {
		final Identifier physicalName = namingStrategy.toPhysicalSequenceName(
				getLogicalName().getSequenceName(),
				jdbcEnvironment
		);

		throw new NotYetImplementedException(  );
	}

	@Override
	public String toLoggableString() {
		return "Sequence(" + logicalName.render() + ")";
	}
}
