package com.mpobjects.maven.plugins.dependency.lock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
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

	/**
	 * If true, do not lock dependencies of SNAPSHOTs.
	 */
	@Parameter(defaultValue = "true", property = "lockedPom.ignoreSnapshot")
	private boolean ignoreSnapshot = true;

	@Parameter(defaultValue = "${project}", readonly = true)
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

	public String getOutputFilename() {
		return outputFilename;
	}

	public boolean isIgnoreSnapshot() {
		return ignoreSnapshot;
	}

	public void setIgnoreSnapshot(boolean aIgnoreSnapshots) {
		ignoreSnapshot = aIgnoreSnapshots;
	}

	public void setOutputFilename(String aOutputFilename) {
		outputFilename = aOutputFilename;
	}

	private void addDependencyManagement(Model aModel) {
		List<Artifact> projectArtifacts = new ArrayList<>(mavenProject.getArtifacts());
		if (projectArtifacts.isEmpty()) {
			getLog().debug("No dependencies to manage.");
			return;
		}
		Collections.sort(projectArtifacts);

		DependencyManagement depMgmt = aModel.getDependencyManagement();
		Set<String> managedArtifacts;
		if (depMgmt == null) {
			depMgmt = new DependencyManagement();
			aModel.setDependencyManagement(depMgmt);
			managedArtifacts = Collections.emptySet();
		} else {
			managedArtifacts = collectManagedArtifacts(depMgmt);
		}

		for (Artifact artifact : projectArtifacts) {
			if (skipArtifact(managedArtifacts, artifact)) {
				continue;
			}
			Dependency dep = createDependency(artifact);
			getLog().debug("Registering artifact " + artifact);
			depMgmt.addDependency(dep);
		}
	}

	private Set<String> collectManagedArtifacts(DependencyManagement aDepMgmt) {
		Set<String> result = new HashSet<>();
		for (Dependency dependency : aDepMgmt.getDependencies()) {
			result.add(dependency.getManagementKey());
		}
		return result;
	}

	private Dependency createDependency(Artifact aArtifact) {
		Dependency dep = new Dependency();
		dep.setGroupId(aArtifact.getGroupId());
		dep.setArtifactId(aArtifact.getArtifactId());
		dep.setVersion(aArtifact.getVersion());
		if (!StringUtils.isEmpty(aArtifact.getClassifier())) {
			dep.setClassifier(aArtifact.getClassifier());
		}
		if (!StringUtils.isEmpty(aArtifact.getType())) {
			dep.setType(aArtifact.getType());
		}
		return dep;
	}

	private Model initializeModel() {
		return mavenProject.getOriginalModel().clone();
	}

	private boolean skipArtifact(Set<String> aManagedArtifacts, Artifact aArtifact) {
		return aManagedArtifacts.contains(aArtifact.getDependencyConflictId()) || isIgnoreSnapshot() && aArtifact.isSnapshot();
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
