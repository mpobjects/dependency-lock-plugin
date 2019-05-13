package com.mpobjects.maven.plugins.dependency.lock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
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

/**
 * Generate a <code>pom.xml</code> with dependencies, including transitive, explicitly locked to a specific version by
 * generating <code>&lt;dependencyManagemenet&gt;</code> entries. This mostly affects dependencies using version ranges.
 */
@Mojo(
		name = "lock",
		defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
		requiresDependencyResolution = ResolutionScope.TEST,
		requiresDependencyCollection = ResolutionScope.TEST)
public class LockMojo extends AbstractMojo {

	@Component
	private MavenProject mavenProject;

	/**
	 * Output file.
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

		List<Artifact> projectArtifacts = new ArrayList<>(mavenProject.getArtifacts());
		if (projectArtifacts.isEmpty()) {
			getLog().debug("No dependencies to manage.");
			return;
		}

		Collections.sort(projectArtifacts);

		DependencyManagement depMgmt = aModel.getDependencyManagement();
		if (depMgmt == null) {
			depMgmt = new DependencyManagement();
			aModel.setDependencyManagement(depMgmt);
		}
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
			getLog().debug("Registering artifact " + artifact);
			depMgmt.addDependency(dep);
		}
	}

	private Model initializeModel() {
		return mavenProject.getOriginalModel().clone();
	}

	private void writeModel(Model aModel, File aFile) throws MojoExecutionException {
		if (!aFile.getParentFile().exists()) {
			if (!aFile.getParentFile().mkdirs()) {
				throw new MojoExecutionException("Unable to write pom file. Cannot create parent directory " + aFile.getParentFile());
			}
		}
		try (FileOutputStream out = new FileOutputStream(aFile);
				Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);) {
			MavenXpp3Writer mavenWriter = new MavenXpp3Writer();
			mavenWriter.write(writer, aModel);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to write pom file.", e);
		}
	}
}
