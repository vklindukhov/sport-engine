package utils

@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7')
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovyx.net.http.HTTPBuilder
import org.cyberneko.html.parsers.SAXParser

class XmlUtils {
    static GPathResult findTagWithAttributeValue(rootElement, String tagName, String attrName, String attrValue) {
        rootElement."**".find {
            isTagWithAttributeValue(it as NodeChild, tagName, attrName, attrValue)
        } as GPathResult
    }

    static boolean isTagWithName(NodeChild node, String tagName) {
        !isBlank(tagName) && node.name().toLowerCase() == tagName
    }

    static boolean isTagWithAttributeValue(NodeChild node, String tagName, String attrName, String attrValue) {
        try {
            if (!isBlank(tagName)) false
            def isTag = node.name().toLowerCase() == tagName
            if (!isBlank(attrName) && !isBlank(attrValue)) {
                if (isTag && (node.attributes().get(attrName) as String)?.contains(attrValue)) {
                    return true
                }
                return false
            }
            return isTag
        } catch (e) {
            System.err.println('isTagWithAttributeValue - ' + e.getMessage())
            return false
        }
    }

    static boolean isBlank(String str) {
        str == null || str.isEmpty()
    }

    static GPathResult getHtmlByPath(String path, Map headers) {
        GPathResult html = null
        HTTPBuilder http = null
        try {
            def url = new URL(path)
            http = new HTTPBuilder(path)
            if(headers != null) http.setHeaders(headers)
            html = http.get([:]) as GPathResult
        } catch (MalformedURLException e) {
            html = new XmlSlurper(new SAXParser()).parseText(this.getClass().getResource(path).text)
        } finally {
            if(http != null) http.shutdown()
        }
        html
    }

    static GPathResult getHtmlByUrl(URL url, Map headers) {
        GPathResult html = null
        HTTPBuilder http = null
        try {
            http = new HTTPBuilder(url)
            http.setHeaders(headers)
            html = http.get([:]) as GPathResult
        } catch (MalformedURLException e) {
        } finally {
            if(http != null) http.shutdown()
        }
        html
    }

}
