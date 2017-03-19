package service.crawler;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import context.ShareContext;
import entities.ItemRagtag;

public class RagtagCrawler extends BaseCrawlerWithServer {

	public RagtagCrawler() {
		try {
			this.configure();
			this.prepareDriver();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<ItemRagtag> getProductToAddCart(List<String> brandNames, String transantionId) {
		List<ItemRagtag> output = new ArrayList<>();
		RagtagCrawler.logger.info("Start find link!");
		List<String> allLinks = this.getLinkProductBy(brandNames);
		RagtagCrawler.logger.info("End find link!");
		
		if(allLinks ==null || allLinks.size() ==0){
			RagtagCrawler.logger.info("No products to add cart !");
			return null;
		}
		RagtagCrawler.logger.info("FOUND "+allLinks.size()+" NEW PRODUCTS");
		ExecutorService executor = Executors.newFixedThreadPool(50);
		for (String link : allLinks) {
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					output.add(getProductToAddCart(link, transantionId));
				}
			};
			executor.execute(worker);
		}
		executor.shutdown();
        while (!executor.isTerminated()) {
        }
        
		return output;
	}

	private List<String> getLinkProductBy(List<String> brandNames) {
		List<String> currentLinks = new ArrayList<>();
		List<String> output = new ArrayList<>();
		
		for (String brandName : brandNames) {
			currentLinks.addAll(getLinkProductBy(brandName));
		}
		List<String> addedList = ShareContext.getAddedLinkProducts();
		
		
		if (addedList == null) {
			ShareContext.setAddedLinkProducts(currentLinks);
			return null;
		}
		output= currentLinks.stream().filter(link -> !addedList.contains(link)).collect(Collectors.toList());
		if(output != null && !output.isEmpty()){
			ShareContext.setAddedLinkProducts(currentLinks);
		}
		return output;
		
	}

	private List<String> getLinkProductBy(String brandName) {
		int limit = 100;
		int offset = 0;
		TreeMap<Integer, List<String>> mapListLink = new TreeMap<Integer, List<String>>();
		mapListLink = this.getLinkProductBy(mapListLink, brandName, limit, offset);
		int totalPage = mapListLink.firstKey();

		if (totalPage > limit) {
			int nJump = totalPage / limit;
			for (int i = 1; i <= nJump; i++) {
				mapListLink = this.getLinkProductBy(mapListLink, brandName, limit, i * 100);
			}
		}

		return mapListLink.firstEntry().getValue();
	}

	private TreeMap<Integer, List<String>> getLinkProductBy(TreeMap<Integer, List<String>> outputs, String brandName,
			int limit, int offset) {

		WebDriver driver = this.getDriver();
		String url = "http://www.ragtag.jp/products/search.php?limit=" + limit + "&o=" + offset + "&s2%5B%5D="
				+ brandName.replaceAll(" ", "+");
		driver.get(url);
		if (offset == 0) {
			int totalPage = Integer.parseInt(driver.findElement(By.xpath("//*[@id='title_area']/span")).getText().replaceAll("[^0-9]+", ""));
			outputs.put(totalPage, new ArrayList<String>());
		}

		WebElement parent = driver.findElement(By.id("js_naviplus_search_disp"));
		List<WebElement> productsList = parent.findElements(By.className("listphoto"));
		for (WebElement productEl : productsList) {
			WebElement porductLinkEl = productEl.findElement(By.cssSelector("a"));
			outputs.firstEntry().getValue().add(porductLinkEl.getAttribute("href"));
		}
		return outputs;
	}

	private ItemRagtag getProductToAddCart(String link, String transantionId) {
		Document doc = JsoupCrawler.getDoc(link);
		ItemRagtag item = new ItemRagtag();
		item.setProductCode(doc.select("#vs-product-id").attr("value"));
		item.setProductClassId(doc.select("#product_class_id").attr("value"));
		item.setProductId(doc.select("#detail_cart_area input[name=product_id]").attr("value"));
		item.setTransactionid(transantionId);
		return item;

	}

	public void testGetListNotContains() {
		List<String> addedList = Arrays.asList("a", "as", "ads", "ad", "ac", "b");
		List<String> currentLinks = Arrays.asList("a", "as", "ads", "ad", "ac", "b", "asdsad");

		List<String> output = currentLinks.stream().filter(link -> !addedList.contains(link))
				.collect(Collectors.toList());
		System.out.println(output);

	}

}
