/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.tool.schema.extract.spi;

import org.hibernate.Incubating;
import org.hibernate.boot.model.relational.NamespaceName;
import org.hibernate.naming.Identifier;
import org.hibernate.boot.model.relational.MappedNamespace;
import org.hibernate.boot.model.relational.QualifiedSequenceName;
import org.hibernate.boot.model.relational.QualifiedTableName;

/**
 * Provides access to information about existing schema objects (tables, sequences etc) of existing database.
 *
 * @author Christoph Sturm
 * @author Teodor Danciu
 * @author Steve Ebersole
 */
@Incubating
public interface DatabaseInformation {
	/**
	 * Check to see if the given schema already exists.
	 *
	 * @param schema The schema name
	 *
	 * @return {@code true} indicates a schema with the given name already exists
	 */
	boolean schemaExists(NamespaceName schema);

	/**
	 * Obtain reference to the named TableInformation
	 *
	 * @param catalogName The name of the catalog which contains the schema which the table belongs to
	 * @param schemaName The name of the schema the table belongs to
	 * @param tableName The table name
	 *
	 * @return The table information.  May return {@code null} if not found.
	 */
	TableInformation getTableInformation(Identifier catalogName, Identifier schemaName, Identifier tableName);

	/**
	 * Obtain reference to the named TableInformation
	 *
	 * @param schemaName The name of the schema the table belongs to
	 * @param tableName The table name
	 *
	 * @return The table information.  May return {@code null} if not found.
	 */
	TableInformation getTableInformation(NamespaceName schemaName, Identifier tableName);

	/**
	 * Obtain reference to the named TableInformation
	 *
	 * @param tableName The qualfied table name
	 *
	 * @return The table information.  May return {@code null} if not found.
	 */
	TableInformation getTableInformation(QualifiedTableName tableName);

	/**
	 * Obtain reference to all the {@link TableInformation) for a given {@link MappedNamespace }
	 *
	 * @param namespace The {@link MappedNamespace } which contains the {@link TableInformation)
	 *
	 * @return a {@link NameSpaceTablesInformation}
	 */
	NameSpaceTablesInformation getTablesInformation(MappedNamespace namespace);

	/**
	 * Obtain reference to the named SequenceInformation
	 *
	 * @param catalogName The name of the catalog which contains the schema which the sequence belongs to
	 * @param schemaName The name of the schema the sequence belongs to
	 * @param sequenceName The sequence name
	 *
	 * @return The sequence information.  May return {@code null} if not found.
	 */
	SequenceInformation getSequenceInformation(
			Identifier catalogName,
			Identifier schemaName,
			Identifier sequenceName);

	/**
	 * Obtain reference to the named SequenceInformation
	 *
	 * @param namespaceName The name of the namespace the table belongs to
	 * @param sequenceName The sequence name
	 *
	 * @return The sequence information.  May return {@code null} if not found.
	 */
	SequenceInformation getSequenceInformation(NamespaceName namespaceName, Identifier sequenceName);

	/**
	 * Obtain reference to the named SequenceInformation
	 *
	 * @param sequenceName The qualified sequence name
	 *
	 * @return The sequence information.  May return {@code null} if not found.
	 */
	SequenceInformation getSequenceInformation(QualifiedSequenceName sequenceName);

	/**
	 * Check to see if the given catalog already exists.
	 *
	 * @param catalog The catalog name
	 *
	 * @return {@code true} indicates a catalog with the given name already exists
	 */
	boolean catalogExists(Identifier catalog);

	void cleanup();
}
