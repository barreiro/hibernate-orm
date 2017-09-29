/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.boot.model.domain.EntityMapping;
import org.hibernate.boot.model.domain.MappedTableJoin;
import org.hibernate.boot.model.relational.MappedTable;
import org.hibernate.bytecode.internal.BytecodeEnhancementMetadataNonPojoImpl;
import org.hibernate.bytecode.internal.BytecodeEnhancementMetadataPojoImpl;
import org.hibernate.bytecode.spi.BytecodeEnhancementMetadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.LoadQueryInfluencers.InternalFetchProfileType;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FilterHelper;
import org.hibernate.loader.internal.StandardSingleIdEntityLoader;
import org.hibernate.loader.spi.EntityLocker;
import org.hibernate.loader.spi.MultiIdEntityLoader;
import org.hibernate.loader.spi.SingleIdEntityLoader;
import org.hibernate.loader.spi.SingleUniqueKeyEntityLoader;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.RepresentationMode;
import org.hibernate.metamodel.model.domain.internal.EntityIdentifierCompositeAggregatedImpl;
import org.hibernate.metamodel.model.domain.internal.EntityIdentifierSimpleImpl;
import org.hibernate.metamodel.model.domain.internal.SqlAliasStemHelper;
import org.hibernate.metamodel.model.relational.spi.ForeignKey;
import org.hibernate.metamodel.model.relational.spi.JoinedTableBinding;
import org.hibernate.metamodel.model.relational.spi.PhysicalColumn;
import org.hibernate.metamodel.model.relational.spi.PhysicalTable;
import org.hibernate.metamodel.model.relational.spi.Table;
import org.hibernate.query.NavigablePath;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.sql.ast.JoinType;
import org.hibernate.sql.ast.produce.metamodel.spi.TableGroupInfo;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.RootTableGroupContext;
import org.hibernate.sql.ast.produce.spi.SqlAliasBase;
import org.hibernate.sql.ast.produce.spi.TableGroupContext;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.ast.tree.spi.from.EntityTableGroup;
import org.hibernate.sql.ast.tree.spi.from.TableReference;
import org.hibernate.sql.ast.tree.spi.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.spi.predicate.Junction;
import org.hibernate.sql.ast.tree.spi.predicate.Predicate;
import org.hibernate.sql.ast.tree.spi.predicate.RelationalPredicate;
import org.hibernate.sql.results.internal.EntityQueryResultImpl;
import org.hibernate.sql.results.internal.EntitySqlSelectionMappingsImpl;
import org.hibernate.sql.results.spi.EntitySqlSelectionMappings;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.type.descriptor.java.spi.EntityJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.IdentifiableJavaDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractEntityDescriptor<T>
		extends AbstractIdentifiableType<T>
		implements Lockable<T> {
	private static final CoreMessageLogger log = CoreLogging.messageLogger( AbstractEntityDescriptor.class );

	private final SessionFactoryImplementor factory;

	private final NavigableRole navigableRole;

	private final Table rootTable;
	private final List<JoinedTableBinding> secondaryTableBindings;

	private final BytecodeEnhancementMetadata bytecodeEnhancementMetadata;

	private final String sqlAliasStem;

	private final Dialect dialect;

	@SuppressWarnings("UnnecessaryBoxing")
	public AbstractEntityDescriptor(
			EntityMapping bootMapping,
			RuntimeModelCreationContext creationContext) throws HibernateException {
		super( bootMapping,  resolveJavaTypeDescriptor( creationContext, bootMapping ), creationContext );

		this.factory = creationContext.getSessionFactory();

		this.navigableRole = new NavigableRole( bootMapping.getEntityName() );

		this.rootTable = resolveRootTable( bootMapping, creationContext );
		this.secondaryTableBindings = resolveSecondaryTableBindings( bootMapping, creationContext );

		final RepresentationMode representation = getRepresentationStrategy().getMode();
		if ( representation == RepresentationMode.POJO ) {
			this.bytecodeEnhancementMetadata = BytecodeEnhancementMetadataPojoImpl.from( bootMapping );
		}
		else {
			this.bytecodeEnhancementMetadata = new BytecodeEnhancementMetadataNonPojoImpl( bootMapping.getEntityName() );
		}

		log.debugf(
				"Instantiated persister [%s] for entity [%s (%s)]",
				this,
				bootMapping.getEntityName(),
				bootMapping.getJpaEntityName()
		);

		this.sqlAliasStem = SqlAliasStemHelper.INSTANCE.generateStemFromEntityName( bootMapping.getEntityName() );
		this.dialect = factory.getServiceRegistry().getService( JdbcServices.class ).getDialect();
	}

	// todo (6.0) : the root-table may not need to be phyically stored here
	// 		table structures vary by inheritance type
	//
	private Table resolveRootTable(EntityMapping entityMapping, RuntimeModelCreationContext creationContext) {
		final MappedTable rootMappedTable = entityMapping.getRootTable();
		return resolveTable( rootMappedTable, creationContext );
	}

	private Table resolveTable(MappedTable mappedTable, RuntimeModelCreationContext creationContext) {
		return creationContext.getDatabaseObjectResolver().resolveTable( mappedTable );
	}

	private List<JoinedTableBinding> resolveSecondaryTableBindings(
			EntityMapping entityMapping,
			RuntimeModelCreationContext creationContext) {
		final Collection<MappedTableJoin> secondaryTables = entityMapping.getSecondaryTables();
		if ( secondaryTables.size() <= 0 ) {
			return Collections.emptyList();
		}

		if ( secondaryTables.size() == 1 ) {
			return Collections.singletonList(
					generateJoinedTableBinding( secondaryTables.iterator().next(), creationContext )
			);
		}

		return secondaryTables.stream().map( m ->generateJoinedTableBinding( m, creationContext ) ).collect( Collectors.toList() );
	}

	private JoinedTableBinding generateJoinedTableBinding(MappedTableJoin mappedTableJoin, RuntimeModelCreationContext creationContext) {
		final Table joinedTable = resolveTable( mappedTableJoin.getMappedTable(), creationContext );

		// todo (6.0) : resolve "join predicate" as ForeignKey.ColumnMappings
		//		see note on MappedTableJoin regarding what to expose there

		return new JoinedTableBinding(
				joinedTable,
				getPrimaryTable(),
				null,
				mappedTableJoin.isOptional()
		);
	}

	private static <T> IdentifiableJavaDescriptor<T> resolveJavaTypeDescriptor(
			RuntimeModelCreationContext creationContext,
			EntityMapping entityMapping) {
		return (EntityJavaDescriptor<T>) creationContext.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry()
				.getDescriptor( entityMapping.getEntityName() );
	}

	@Override
	public void finishInitialization(
			EntityHierarchy entityHierarchy,
			IdentifiableTypeDescriptor<? super T> superType,
			EntityMapping mappingDescriptor,
			RuntimeModelCreationContext creationContext) {
		super.finishInitialization( entityHierarchy, superType, mappingDescriptor, creationContext );

		log.debugf(
				"Completed initialization of persister [%s] for entity [%s (%s)]",
				this,
				getJavaTypeDescriptor().getEntityName(),
				getJavaTypeDescriptor().getJpaEntityName()
		);
	}

	@Override
	public SessionFactoryImplementor getFactory() {
		return factory;
	}

	@Override
	public EntityJavaDescriptor<T> getJavaTypeDescriptor() {
		return (EntityJavaDescriptor<T>) super.getJavaTypeDescriptor();
	}

	@Override
	public String getEntityName() {
		return getJavaTypeDescriptor().getEntityName();
	}

	@Override
	public String getJpaEntityName() {
		return getJavaTypeDescriptor().getJpaEntityName();
	}

	@Override
	public String getName() {
		return getJpaEntityName();
	}

	@Override
	public NavigableContainer getContainer() {
		return null;
	}

	@Override
	public Table getPrimaryTable() {
		return rootTable;
	}

	@Override
	public List<JoinedTableBinding> getSecondaryTableBindings() {
		return secondaryTableBindings;
	}

	@Override
	public Class<T> getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public Class<T> getBindableJavaType() {
		return getJavaType();
	}

	@Override
	public BytecodeEnhancementMetadata getBytecodeEnhancementMetadata() {
		return bytecodeEnhancementMetadata;
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public String getNavigableName() {
		return navigableRole.getNavigableName();
	}

	@Override
	public EntityDescriptor<T> getEntityDescriptor() {
		return this;
	}

	@Override
	public <Y> SingularAttribute<? super T, Y> getId(Class<Y> type) {
		return getHierarchy().getIdentifierDescriptor();
	}

	@Override
	public <Y> SingularAttribute<T, Y> getDeclaredId(Class<Y> type) {
		return getHierarchy().getIdentifierDescriptor();
	}

	@Override
	public <Y> SingularAttribute<? super T, Y> getVersion(Class<Y> type) {
		return getHierarchy().getVersionDescriptor();
	}

	@Override
	public <Y> SingularAttribute<T, Y> getDeclaredVersion(Class<Y> type) {
		return getHierarchy().getVersionDescriptor();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return getIdentifierDescriptor() instanceof EntityIdentifierSimpleImpl
				|| getIdentifierDescriptor() instanceof EntityIdentifierCompositeAggregatedImpl;
	}

	@Override
	public boolean hasVersionAttribute() {
		return getHierarchy().getVersionDescriptor() != null;
	}

	@Override
	public Set<SingularAttribute<? super T, ?>> getIdClassAttributes() {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public Type<?> getIdType() {
		return getHierarchy().getIdentifierDescriptor().getType();
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.ENTITY_TYPE;
	}

	private final SingleIdEntityLoader customQueryLoader = null;
	private Map<LockMode,SingleIdEntityLoader> loaders;
	private Map<InternalFetchProfileType,SingleIdEntityLoader> internalCascadeLoaders;

	private final FilterHelper filterHelper = null;
	private final Set<String> affectingFetchProfileNames = new HashSet<>();


	@Override
	public SingleIdEntityLoader getSingleIdLoader(LockOptions lockOptions, LoadQueryInfluencers loadQueryInfluencers) {
		if ( 	customQueryLoader != null ) {
			// if the user specified that we should use a custom query for loading this entity, we need
			// 		to always use that custom loader.
			return customQueryLoader;
		}


		if ( isAffectedByEnabledFilters( loadQueryInfluencers ) ) {
			// special case of not-cacheable based on enabled filters effecting this load.
			//
			// This case is special because the filters need to be applied in order to
			// 		properly restrict the SQL/JDBC results.  For this reason it has higher
			// 		precedence than even ""internal" fetch profiles.
			return createLoader( lockOptions, loadQueryInfluencers );
		}

		final boolean useInternalFetchProfile = loadQueryInfluencers.getEnabledInternalFetchProfileType() != null
				&& LockMode.UPGRADE.greaterThan( lockOptions.getLockMode() );
		if ( useInternalFetchProfile ) {
			return internalCascadeLoaders.computeIfAbsent(
					loadQueryInfluencers.getEnabledInternalFetchProfileType(),
					internalFetchProfileType -> createLoader( lockOptions, loadQueryInfluencers )
			);
		}

		// otherwise see if the loader for the requested load can be cached (which
		// 		also means we should look in the cache).

		final boolean cacheable = ! isAffectedByEnabledFetchProfiles( loadQueryInfluencers )
				&& ! isAffectedByEntityGraph( loadQueryInfluencers )
				&& lockOptions.getTimeOut() != LockOptions.WAIT_FOREVER;


		SingleIdEntityLoader loader = null;
		if ( cacheable ) {
			if ( loaders == null ) {
				loaders = new ConcurrentHashMap<>();
			}
			else {
				loader = loaders.get( lockOptions.getLockMode() );
			}
		}

		if ( loader == null ) {
			loader = createLoader( lockOptions, loadQueryInfluencers );
		}

		if ( cacheable ) {
			assert loaders != null;
			loaders.put( lockOptions.getLockMode(), loader );
		}

		return loader;
	}

	protected boolean isAffectedByEnabledFilters(LoadQueryInfluencers loadQueryInfluencers) {
		return loadQueryInfluencers.hasEnabledFilters()
				&& filterHelper.isAffectedBy( loadQueryInfluencers.getEnabledFilters() );
	}

	protected boolean isAffectedByEnabledFetchProfiles(LoadQueryInfluencers loadQueryInfluencers) {
		for ( String s : loadQueryInfluencers.getEnabledFetchProfileNames() ) {
			if ( affectingFetchProfileNames.contains( s ) ) {
				return true;
			}
		}
		return false;
	}

	protected boolean isAffectedByEntityGraph(LoadQueryInfluencers loadQueryInfluencers) {
		return loadQueryInfluencers.getFetchGraph() != null
				|| loadQueryInfluencers.getLoadGraph() != null;
	}

	protected SingleIdEntityLoader createLoader(LockOptions lockOptions, LoadQueryInfluencers loadQueryInfluencers) {
		// todo (6.0) : determine when the loader can be cached....

		// for now, always create a new one
		return new StandardSingleIdEntityLoader<>(
				this,
				lockOptions,
				loadQueryInfluencers
		);
	}

	@Override
	public SingleUniqueKeyEntityLoader getSingleUniqueKeyLoader(Navigable navigable, LoadQueryInfluencers loadQueryInfluencers) {
		return null;
	}

	@Override
	public MultiIdEntityLoader getMultiIdLoader(LoadQueryInfluencers loadQueryInfluencers) {
		// todo (6.0) : disallow against entities for which the user has defined a custom "loader query".
		return null;
	}

	private Map<LockMode,EntityLocker> lockers;

	@Override
	public EntityLocker getLocker(LockOptions lockOptions, LoadQueryInfluencers loadQueryInfluencers) {
		EntityLocker entityLocker = null;
		if ( lockers == null ) {
			lockers = new ConcurrentHashMap<>();
		}
		else {
			entityLocker = lockers.get( lockOptions.getLockMode() );
		}

		if ( entityLocker == null ) {
			throw new NotYetImplementedFor6Exception(  );
//			entityLocker = new EntityLocker() {
//				final LockingStrategy strategy = getFactory().getJdbcServices()
//						.getJdbcEnvironment()
//						.getDialect()
//						.getLockingStrategy( ... );
//				@Override
//				public void lock(
//						Serializable id,
//						Object version,
//						Object object,
//						SharedSessionContractImplementor session,
//						Options options) {
//					strategy.lock( id, version, object, options.getTimeout(), session );
//				}
//			};
//			lockers.put( lockOptions.getLockMode(), entityLocker );
		}
		return entityLocker;
	}

	@Override
	public String getSqlAliasStem() {
		return sqlAliasStem;
	}

	@Override
	public EntityTableGroup createRootTableGroup(TableGroupInfo info, RootTableGroupContext tableGroupContext) {
		final SqlAliasBase sqlAliasBase = tableGroupContext.getSqlAliasBaseGenerator().createSqlAliasBase( getSqlAliasStem() );

		final TableReference primaryTableReference = resolvePrimaryTableReference( sqlAliasBase );

		final List<TableReferenceJoin> joins = new ArrayList<>(  );
		resolveTableReferenceJoins( primaryTableReference, sqlAliasBase, tableGroupContext, joins::add );

		final EntityTableGroup group = new EntityTableGroup(
				info.getUniqueIdentifier(),
				tableGroupContext.getTableSpace(),
				this,
				this,
				new NavigablePath( getEntityName() ),
				primaryTableReference,
				joins
		);

		// todo (6.0) - apply filters - which needs access to Session, or at least its LoadQueryInfluencers
		//		the filter conditions would be added to the SQL-AST's where-clause via tableGroupContext
		//		for now, add null, this is just here as a placeholder
		tableGroupContext.addRestriction( null );

		return group;
	}

	private TableReference resolvePrimaryTableReference(SqlAliasBase sqlAliasBase) {
		return new TableReference( getPrimaryTable(), sqlAliasBase.generateNewAlias() );
	}

	private void resolveTableReferenceJoins(
			TableReference rootTableReference,
			SqlAliasBase sqlAliasBase,
			TableGroupContext context,
			Consumer<TableReferenceJoin> collector) {
		getSecondaryTableBindings()
				.stream()
				.map( joinedTableBinding -> createTableReferenceJoin( joinedTableBinding, rootTableReference, sqlAliasBase, context ) )
				.forEach( collector );
	}

	private TableReferenceJoin createTableReferenceJoin(
			JoinedTableBinding joinedTableBinding,
			TableReference rootTableReference,
			SqlAliasBase sqlAliasBase,
			TableGroupContext context) {
		final TableReference joinedTableReference = new TableReference(
				joinedTableBinding.getTargetTable(),
				sqlAliasBase.generateNewAlias()
		);

		return new TableReferenceJoin(
				joinedTableBinding.isOptional()
						? JoinType.LEFT
						: context.getTableReferenceJoinType(),
				joinedTableReference,
				generateJoinPredicate( rootTableReference, joinedTableReference, joinedTableBinding.getJoinPredicateColumnMappings() )
		);
	}

	private Predicate generateJoinPredicate(
			TableReference rootTableReference,
			TableReference joinedTableReference,
			ForeignKey.ColumnMappings joinPredicateColumnMappings) {
		assert rootTableReference.getTable() == joinPredicateColumnMappings.getTargetTable();
		assert joinedTableReference.getTable() == joinPredicateColumnMappings.getReferringTable();
		assert !joinPredicateColumnMappings.getColumnMappings().isEmpty();

		final Junction conjunction = new Junction( Junction.Nature.CONJUNCTION );

		for ( ForeignKey.ColumnMapping columnMapping : joinPredicateColumnMappings.getColumnMappings() ) {
			conjunction.add(
					new RelationalPredicate(
							RelationalPredicate.Operator.EQUAL,
							rootTableReference.resolveColumnReference( columnMapping.getTargetColumn() ),
							joinedTableReference.resolveColumnReference( columnMapping.getReferringColumn() )
					)
			);
		}

		return conjunction;
	}

	@Override
	public void applyTableReferenceJoins(
			ColumnReferenceQualifier lhs,
			JoinType joinType,
			SqlAliasBase sqlAliasBase,
			TableReferenceJoinCollector joinCollector,
			TableGroupContext tableGroupContext) {
		final TableReference root = resolvePrimaryTableReference( sqlAliasBase );
		joinCollector.addRoot( root );
		resolveTableReferenceJoins( root, sqlAliasBase, tableGroupContext, joinCollector::collectTableReferenceJoin );
	}

	@Override
	public QueryResult createQueryResult(
			NavigableReference navigableReference,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		assert navigableReference.getNavigable() instanceof EntityValuedNavigable;

		return new EntityQueryResultImpl(
				(EntityValuedNavigable) navigableReference.getNavigable(),
				resultVariable,
				buildSqlSelectionMappings( navigableReference, creationContext ),
				navigableReference.getNavigablePath(),
				creationContext
		);
	}

	// todo (6.0) : rather than SqlSelection, I kind of think that these contracts should capture the Expressions
	//		an "(Sql)ExpressionGroup" if you will.  This is because the entity may be
	// 		used in clauses other than the select.
	//
	// note : we could also cache mappings between a NavigableReference and its "(Sql)Expression"
	//		group mappings

	private EntitySqlSelectionMappings buildSqlSelectionMappings(
			NavigableReference selectedExpression,
			QueryResultCreationContext resolutionContext) {
		final EntitySqlSelectionMappingsImpl.Builder mappings = new EntitySqlSelectionMappingsImpl.Builder();

		mappings.applyRowIdSqlSelection(
				resolutionContext.getSqlSelectionResolver().resolveSqlSelection(
						resolutionContext.getSqlSelectionResolver().resolveSqlExpression(
								selectedExpression.getSqlExpressionQualifier(),
								getHierarchy().getRowIdDescriptor().getColumns().get( 0 )
						)
				)
		);

		mappings.applyDiscriminatorSqlSelection(
				resolutionContext.getSqlSelectionResolver().resolveSqlSelection(
						resolutionContext.getSqlSelectionResolver().resolveSqlExpression(
								selectedExpression.getSqlExpressionQualifier(),
								getHierarchy().getDiscriminatorDescriptor().getColumns().get( 0 )
						)
				)
		);

		mappings.applyTenantDiscriminatorSqlSelection(
				resolutionContext.getSqlSelectionResolver().resolveSqlSelection(
						resolutionContext.getSqlSelectionResolver().resolveSqlExpression(
								selectedExpression.getSqlExpressionQualifier(),
								getHierarchy().getTenantDiscrimination().getColumn()
						)
				)
		);

		mappings.applyIdSqlSelectionGroup(
				getHierarchy().getIdentifierDescriptor().resolveSqlSelectionGroup(
						selectedExpression.getSqlExpressionQualifier(),
						resolutionContext
				)
		);

		getPersistentAttributes().forEach(
				persistentAttribute -> {
					mappings.applyAttributeSqlSelectionGroup(
							persistentAttribute,
							persistentAttribute.resolveSqlSelectionGroup(
									selectedExpression.getSqlExpressionQualifier(),
									resolutionContext
							)
					);
				}
		);

		return mappings.create();
	}

	@Override
	public String getRootTableName() {
		return ( (PhysicalTable) rootTable ).getTableName().render( dialect );
	}

	@Override
	public String[] getRootTableIdentifierColumnNames() {
		final List<PhysicalColumn> columns = rootTable.getPrimaryKey().getColumns();
		String[] columnNames = new String[columns.size()];
		int i = 0;
		for ( PhysicalColumn column : columns ) {
			columnNames[i] = column.getName().render( dialect );
			i++;
		}
		return columnNames;
	}

	@Override
	public String getVersionColumnName() {
		return ( (PhysicalColumn) getHierarchy().getVersionDescriptor().getColumns().get( 0 ) )
				.getName()
				.render( dialect );
	}

	@Override
	public boolean hasNaturalIdentifier() {
		return getHierarchy().getNaturalIdDescriptor() != null;
	}
}