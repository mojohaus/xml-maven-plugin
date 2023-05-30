import groovy.xml.XmlSlurper

File xmlFile = new File(basedir, "/target/generated-resources/xml/xslt/book.xml")
assert xmlFile.isFile()

def book = new XmlSlurper().parse(xmlFile)

assert book instanceof groovy.xml.slurpersupport.GPathResult

assert book.chapter.section == 'Hello World'

