/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.bytecode.enhancement.lazy.basic;


import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import org.hibernate.test.bytecode.enhancement.AbstractEnhancerTestTask;
import org.hibernate.test.bytecode.enhancement.join.Vehicle;
import org.hibernate.testing.bytecode.enhancement.EnhancerTestUtils;
import org.junit.Assert;

/**
 * @author Luis Barreiro
 */
public class HHH10533TestTask extends AbstractEnhancerTestTask {

	private Long entityId;

	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Person.class, Team.class, PersonRelation.class, PersonTeamRelation.class};
	}

	public void prepare() {
		Configuration cfg = new Configuration();
		cfg.setProperty( Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true" );
		cfg.setProperty( Environment.USE_SECOND_LEVEL_CACHE, "false" );
		super.prepare( cfg );

		Session s = getFactory().openSession();
		s.beginTransaction();

		s.getTransaction().commit();
		s.clear();
		s.close();
	}

	public void execute() {
		Session s = getFactory().openSession();
		s.beginTransaction();

		s.getTransaction().commit();
		s.close();
	}

	protected void cleanup() {
	}

	@Entity
	public class Person implements Serializable {
		@Id
		@GeneratedValue
		private Integer id;

		private String name;

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Person() {
		}

		public Person(String name) {
			this.name = name;
		}
	}

	@Entity
	public class Team implements Serializable {
		@Id
		@GeneratedValue
		private Integer id;

		private String name;

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Team() {
		}

		public Team(String name) {
			this.name = name;
		}
	}

	@MappedSuperclass
	public abstract class PersonRelation<OtherType extends Serializable> implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long id;
		private Person person;
		private OtherType otherEntity;

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "id", unique = true, nullable = false, updatable = false)
		public Long getId() {
			return this.id;
		}
		public void setId(final Long id) {
			this.id = id;
		}

		@ManyToOne(optional = false)
		@JoinColumn(name = "person", nullable = false)
		public Person getPerson() {
			return this.person;
		}
		public void setPerson(final Person person) {
			this.person = person;
		}

		@Transient
		protected OtherType getOtherEntity() {
			return this.otherEntity;
		}
		protected void setOtherEntity(final OtherType otherEntity) {
			this.otherEntity = otherEntity;
		}
	}

	@Entity
	@Table(name = "persons_teams")
	public class PersonTeamRelation extends PersonRelation<Team> {
		private static final long serialVersionUID = 1L;
		private boolean administrator;

		@ManyToOne(optional = false)
		@JoinColumn(name = "team", nullable = false)
		public Team getTeam() {
			return getOtherEntity();
		}

		public void setTeam(final Team team) {
			setOtherEntity(team);
		}

		@Column(name = "administrator", nullable = false)
		public boolean isAdministrator() {
			return this.administrator;
		}
		public void setAdministrator(final boolean isAdministrator) {
			this.administrator = isAdministrator;
		}
	}
}
