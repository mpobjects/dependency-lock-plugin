package com.mpobjects.maven.plugins.dependency.lock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

public class LockMojoTest {
	@Rule
	public MojoRule rule = new MojoRule() {
		@Override
		protected void after() {
		}

		@Override
		protected void before() throws Throwable {
		}
	};

	@Test
	public void testLockMojo() throws Exception {
		// TODO: artifacts are not resolved

		File pom = new File("target/test-classes/project-to-test/");
		assertNotNull(pom);
		assertTrue(pom.exists());

		LockMojo myMojo = (LockMojo) rule.lookupConfiguredMojo(pom, "lock");
		assertNotNull(myMojo);
		myMojo.execute();

		String outputFilename = (String) rule.getVariableValueFromObject(myMojo, "outputFilename");
		assertNotNull(outputFilename);

		File file = new File("target/test-classes/project-to-test/target", outputFilename);
		assertTrue(file.exists());
	}

}
