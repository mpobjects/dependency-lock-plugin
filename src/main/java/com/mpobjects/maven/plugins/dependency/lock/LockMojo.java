package com.mpobjects.maven.plugins.dependency.lock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

@Mojo(name = "lock", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class LockMojo extends AbstractMojo {
	/**
	 * The current project
	 */
	@Component
	private MavenProject mavenProject;

	@Component
	private ModelBuilder modelBuilder;

	/**
	 * Output file
	 */
	@Parameter(defaultValue = "pom-locked.xml", property = "lockedPom")
	private String outputFilename;

	@Override
	public void execute() throws MojoExecutionException {
		Model model = initializeModel();
		addDependencyManagement(model);

		writeModel(model, new File(mavenProject.getBuild().getDirectory(), outputFilename));
	}

	private void addDependencyManagement(Model aModel) {
		// Based on
		// https://github.com/jboss/bom-builder-maven-plugin/blob/master/src/main/java/org/jboss/maven/plugins/bombuilder/BuildBomMojo.java
		// TODO: append to dep
		// TODO: only add transitive?
		// TODO: do not replace

		List<Artifact> projectArtifacts = new ArrayList<>(mavenProject.getArtifacts());
		Collections.sort(projectArtifacts);

		DependencyManagement depMgmt = new DependencyManagement();
		for (Artifact artifact : projectArtifacts) {
			Dependency dep = new Dependency();
			dep.setGroupId(artifact.getGroupId());
			dep.setArtifactId(artifact.getArtifactId());
			dep.setVersion(artifact.getVersion());
			if (!StringUtils.isEmpty(artifact.getClassifier())) {
				dep.setClassifier(artifact.getClassifier());
			}
			if (!StringUtils.isEmpty(artifact.getType())) {
				dep.setType(artifact.getType());
			}
			depMgmt.addDependency(dep);
		}
		aModel.setDependencyManagement(depMgmt);
		getLog().debug("Added " + projectArtifacts.size() + " dependencies.");
	}

	private Model initializeModel() {
		return null;
	}

	private void writeModel(Model aModel, File aFile) throws MojoExecutionException {
		if (!aFile.getParentFile().exists()) {
			aFile.getParentFile().mkdirs();
		}
		try (FileWriter writer = new FileWriter(aFile)) {
			MavenXpp3Writer mavenWriter = new MavenXpp3Writer();
			mavenWriter.write(writer, aModel);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to write pom file.", e);
		}
	}
}
