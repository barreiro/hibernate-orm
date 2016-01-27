/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.bytecode.enhancement.lazy;


import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import org.hibernate.testing.bytecode.enhancement.EnhancerTestUtils;
import org.hibernate.test.bytecode.enhancement.AbstractEnhancerTestTask;
import org.junit.Assert;

/**
 * @author Gail Badner
 */
public class LazyInEmbeddedPropertyTestTask extends AbstractEnhancerTestTask {

	private Long entityId;

	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {Record.class, RecordData.class};
	}

	public void prepare() {
		Configuration cfg = new Configuration();
		cfg.setProperty( Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true" );
		cfg.setProperty( Environment.USE_SECOND_LEVEL_CACHE, "false" );
		super.prepare( cfg );

		Session s = getFactory().openSession();
		s.beginTransaction();

		Record record = new Record();
		record.setDescription( "desc" );
		record.setData( new RecordData( "eager.txt", "text/plain", "lazy content".getBytes( StandardCharsets.UTF_8 ) ) );
		s.persist( record );
		entityId = record.getId();

		s.getTransaction().commit();
		s.clear();
		s.close();
	}

	public void execute() {
		Session s = getFactory().openSession();
		s.beginTransaction();

		Record record = s.get( Record.class, entityId );

		Assert.assertFalse( Hibernate.isPropertyInitialized( record, "description" ) );
		Assert.assertEquals( "desc", record.getDescription() );
		EnhancerTestUtils.checkDirtyTracking( record );
		Assert.assertTrue( Hibernate.isPropertyInitialized( record, "description" ) );

		Assert.assertTrue( Hibernate.isPropertyInitialized( record, "data" ) );
		RecordData data = record.getData();
		Assert.assertNotNull( data );
		Assert.assertEquals( "eager.txt", data.getName() );

		Assert.assertFalse( Hibernate.isPropertyInitialized( data, "content" ) );
		Assert.assertEquals( "lazy content".getBytes( StandardCharsets.UTF_8 ) , data.getContent() );
		Assert.assertTrue( Hibernate.isPropertyInitialized( data, "content" ) );

		s.getTransaction().commit();
		s.close();
	}

	protected void cleanup() {
	}

	@Entity
	public static class Record {

		@Id
		@GeneratedValue
		private Long id;

		@Basic(fetch = FetchType.LAZY)
		private String description;

		@Embedded
		private RecordData data;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public RecordData getData() {
			return data;
		}

		public void setData(RecordData data) {
			this.data = data;
		}
	}

	@Embeddable
	public static class RecordData implements Serializable {

		private String name;

		private String mimeType;

		@Basic(fetch = FetchType.LAZY)
		@Lob
		private byte[] content;

		public RecordData() {
		}

		public RecordData(String name, String mimeType, byte[] content) {
			this.name = name;
			this.mimeType = mimeType;
			this.content = content;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMimeType() {
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}
	}

}
