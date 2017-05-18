/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.event.spi;

import org.hibernate.envers.boot.AuditService;
import org.hibernate.envers.internal.synchronization.AuditProcess;
import org.hibernate.envers.internal.synchronization.work.AddWorkUnit;
import org.hibernate.envers.internal.synchronization.work.AuditWorkUnit;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.metamodel.model.domain.spi.EntityTypeImplementor;

/**
 * Envers-specific entity (post) insertion event listener
 *
 * @author Adam Warski (adam at warski dot org)
 * @author HernпїЅn Chanfreau
 * @author Steve Ebersole
 * @author Chris Cranford
 */
public class EnversPostInsertEventListenerImpl extends BaseEnversEventListener implements PostInsertEventListener {
	public EnversPostInsertEventListenerImpl(AuditService auditService) {
		super( auditService );
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		final String entityName = event.getPersister().getEntityName();

		if ( getAuditService().getEntityBindings().isVersioned( entityName ) ) {
			checkIfTransactionInProgress( event.getSession() );

			final AuditProcess auditProcess = getAuditService().getAuditProcess( event.getSession() );

			final AuditWorkUnit workUnit = new AddWorkUnit(
					event.getSession(),
					event.getPersister().getEntityName(),
					getAuditService(),
					event.getId(),
					event.getPersister(),
					event.getState()
			);
			auditProcess.addWorkUnit( workUnit );

			if ( workUnit.containsWork() ) {
				generateBidirectionalCollectionChangeWorkUnits(
						auditProcess,
						event.getPersister(),
						entityName,
						event.getState(),
						null,
						event.getSession()
				);
			}
		}
	}

	@Override
	public boolean requiresPostCommitHanding(EntityTypeImplementor persister) {
		return getAuditService().getEntityBindings().isVersioned( persister.getEntityName() );
	}
}
