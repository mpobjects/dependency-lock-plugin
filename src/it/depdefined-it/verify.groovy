 def expected = new XmlSlurper().parse(new File(basedir, "expected/pom-locked.xml"))
 def actual = new XmlSlurper().parse(new File(basedir, "target/pom-locked.xml"))
 
 def isDifferent = !expected.dependencyManagement.toString().equals(actual.dependencyManagement.toString());
 if (isDifferent) {
    System.err.println("Generated " + file.absolutePath + " differs from expected " + expectedFile.absolutePath)
    return false
}
