package service.crawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupCrawler {
	public static Document getDoc(String pageUrl) {
		try {
			return Jsoup.connect(pageUrl).header("Accept-Encoding", "gzip, deflate")
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
					.maxBodySize(0).timeout(600000).get();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
