package parse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Html Parser
 * Created by johnson on 16/3/29.
 */
public class HtmlParser {
  public Set<URL> getLinks(String html, String baseUri) {
    return getLinks(Jsoup.parse(html, baseUri));
  }

  private Set<URL> getLinks(Document document) {
    List<String> outerLinks = new ArrayList<String>() {
      @Override
      public boolean add(String o) {
        return super.add(o);
      }
    };
    for (Element element : document.getAllElements()) {
      /**
       * Since jsoup handles <base> tag automatically, there is no need to handle it in my code
       */
      switch (element.tagName()) {
        case "applet":
          //TODO: I'm not sure what can <applet> do
          break;
        case "audio":
          outerLinks.add(getLinkFromElement(element, "src"));
          break;
        case "object":
          outerLinks.add(getLinkFromElement(element, "data"));
          break;
        case "script":
          outerLinks.add(getLinkFromElement(element, "src"));
          break;
        case "link":
          String rel = element.attr("rel");
          if (!rel.equals("next") && !rel.equals("pre")) {
            outerLinks.add(getLinkFromElement(element, "href"));
          }
          break;
        case "img":
          outerLinks.add(getLinkFromElement(element, "src"));
          break;
        default:
      }
    }
    return verifyUrl(outerLinks);
  }

  private String getLinkFromElement(Element element, String key) {
    // In case that the link is empty, otherwise absUrl would just return the base url
    if (!TextUtils.isEmpty(element.attr(key))) {
      return element.absUrl(key);
    }
    return "";
  }

  private Set<URL> verifyUrl(List<String> candidates) {
    Set<URL> res = new HashSet<>();
    for (String str : candidates) {
      if (StringUtils.isEmpty(str)) {
        continue;
      }
      try {
        URL url = new URL(str);
        res.add(url);
      } catch (MalformedURLException exception) {
        exception.printStackTrace();
      }
    }
    return res;
  }
}
