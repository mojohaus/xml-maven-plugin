import org.codehaus.plexus.util.FileUtils

File logFile = new File(basedir, "build.log")
assert logFile.isFile()
String logContent = FileUtils.fileRead(logFile, "UTF-8");

assert logContent =~ /\[DEBUG\] No XML formatting violations found in file [^\n]+2-spaces-correct-no-schema\.xml/
assert logContent =~ /\[DEBUG\] No XML formatting violations found in file [^\n]+2-spaces-correct-no-schema-short\.xml/
assert logContent =~ /\[DEBUG\] No XML formatting violations found in file [^\n]+pom\.xml/
assert logContent =~ /\[DEBUG\] Checked the formatting of 3 files/
