import org.codehaus.plexus.util.FileUtils

File logFile = new File(basedir, "build.log")
assert logFile.isFile()
String logContent = FileUtils.fileRead(logFile, "UTF-8");

assert logContent =~ /\[ERROR\] [^ \n]*2-spaces-broken-no-schema-short\.xml:5,14: Delete 1 spaces. Expected 4 found 5 spaces before start element <text-1>/
assert logContent =~ /\[DEBUG\] No XML formatting violations found in file [^\n]+pom\.xml/
assert logContent =~ /\[DEBUG\] Checked the formatting of 2 files/
