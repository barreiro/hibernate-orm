package org.hibernate.test.bytecode.enhancement.basic;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.Managed;
import org.hibernate.test.bytecode.enhancement.AbstractEnhancerTestTask;
import org.junit.Assert;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Luis Barreiro
 */
public class SuperclassEnhancementTestTask extends AbstractEnhancerTestTask {

	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { BaseEntity.class, SubEntity.class };
	}

	public void prepare() {
		Configuration cfg = new Configuration();
		cfg.setProperty( Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true" );
		cfg.setProperty( Environment.USE_SECOND_LEVEL_CACHE, "false" );
		prepare( cfg );
	}

	public void execute() {
		Assert.assertTrue( "BaseEntity NOT managed", isEnhanced( new BaseEntity() ) );
		Assert.assertTrue( "SubEntity NOT managed", isEnhanced( new SubEntity() ) );
	}

	protected void cleanup() {
	}

	private boolean isEnhanced(Object object) {
		for ( Class localInterface : object.getClass().getInterfaces() ) {
			if ( Managed.class.isAssignableFrom( localInterface ) ) {
				return true;
			}
		}
		return false;
	}

	@Entity private static class BaseEntity {

		@Id
		@GeneratedValue( strategy = GenerationType.IDENTITY )
		private long id;

	}

	@Entity private static class SubEntity extends BaseEntity {

		@ManyToOne( fetch = FetchType.LAZY )
		@LazyToOne( LazyToOneOption.NO_PROXY )
		private BaseEntity otherEntity;

	}

}
