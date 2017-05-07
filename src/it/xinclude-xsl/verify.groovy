import org.codehaus.plexus.util.FileUtils

File xmlFile = new File(basedir, "/target/generated-resources/xml/xslt/book.xml")
assert xmlFile.isFile()

def book = new XmlSlurper().parse(xmlFile)

assert book instanceof groovy.util.slurpersupport.GPathResult

assert book.chapter.section == 'Hello World'

