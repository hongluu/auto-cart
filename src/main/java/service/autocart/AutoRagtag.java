package service.autocart;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import context.ShareContext;
import entities.Brand;
import entities.ItemRagtag;
import main.MainSettingDialog;
import service.crawler.JsoupCrawler;
import service.crawler.RagtagCrawler;

public class AutoRagtag {

	private  String cookie = "";
	private String transantionId ="";
	private MainSettingDialog mainSettingDialog;
	final static Logger logger = Logger.getLogger(AutoRagtag.class);
	
	public AutoRagtag(MainSettingDialog mainSettingDialog) {
		this.mainSettingDialog = mainSettingDialog;
	}

	public String getTransantionId() {
		return transantionId;
	}

	public void setTransantionId(String transantionId) {
		this.transantionId = transantionId;
	}
	public void setTransactionIdAndCookie(){
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("http://www.ragtag.jp");
		WebElement transactionEl = driver.findElement(By.xpath("//*[@id='header_login_form']/input[2]"));
		transantionId = transactionEl.getAttribute("value");
		
		StringBuilder cookieBuilder = new StringBuilder(); 
		Set<Cookie> cookies = driver.manage().getCookies();
		cookies.forEach(a->{
			cookieBuilder.append(a.getName()+"="+a.getValue()+";");
		 });
		cookie= cookieBuilder.toString();
		AutoRagtag.logger.info("=================TRANSACTIONID===================");
		AutoRagtag.logger.info(transantionId);
		AutoRagtag.logger.info("=================COOKIE===================");
		AutoRagtag.logger.info(cookie);
	}
	public LinkedHashMap<String, ArrayList<Brand>> getAllBrandMap() {
		try {
			LinkedHashMap<String, ArrayList<Brand>> ret = new LinkedHashMap<String, ArrayList<Brand>>();
			Document document = JsoupCrawler.getDoc("http://www.ragtag.jp/products/brandlist.php");
			Elements elements = document.select(".open");
			elements.addAll(document.select(".close"));
			Elements areas = document.select(".brandlist-area");
			for (int ii = 0; ii < elements.size(); ii++) {
				String alpha = elements.get(ii).text();
				Element area = areas.get(ii);
				Elements brands = area.select("a");
				ArrayList<Brand> brandList = new ArrayList<Brand>();
				for (Element aElement : brands) {
					String brandCode = aElement.attr("href").split("=")[1];
					String brandName = aElement.select("a span").first().text();
					Brand brand = new Brand(brandName, brandCode);
					brandList.add(brand);
				}
				ret.put(alpha, brandList);
			}

			return ret;
		} catch (Exception e) {
			return new LinkedHashMap<String, ArrayList<Brand>>();
		} finally {

		}

	}

	private RagtagCrawler ragCrawler;
	private boolean isExit = false;

	public void run(ArrayList<Brand> selectedBrands) {
		if(selectedBrands ==null || selectedBrands.size() ==0){
			return;
		}
		this.ragCrawler = new RagtagCrawler();
		
		List<String> brandNames = selectedBrands.stream().map(x-> x.getBrandName()).collect(Collectors.toList());
		// uncomment to test
		ShareContext.setAddedLinkProducts(new ArrayList<>());
		while (!isExit) {
			mainSettingDialog.appendLog("Start find products");
			List<ItemRagtag> allItemsRagtag = ragCrawler.getProductToAddCart(brandNames, this.transantionId);
			
			
			if(allItemsRagtag!= null){
				mainSettingDialog.appendLog("Found " +allItemsRagtag.size()+ " products");
				for (ItemRagtag itemRagtag : allItemsRagtag) {
					this.addCart(itemRagtag);
					mainSettingDialog.appendLog("Add products : "+itemRagtag.getProductCode());
				}
				mainSettingDialog.appendLog("COMPLETE STEP !!!");
			}else{
				mainSettingDialog.appendLog("No news products !!!");
			}
		}
	}

	public void addCart(ItemRagtag itemRagtag) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		String url = "http://www.ragtag.jp/products/detail.php?";
		HttpPost post = new HttpPost(url);
		post.setHeader("Cookie", this.getCookie());
		
		post.setHeader("X-Requested-With", "XMLHttpRequest");
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("undefined", itemRagtag.getProductCode()));
		urlParameters.add(new BasicNameValuePair("mode", "cart_ajax"));
		urlParameters.add(new BasicNameValuePair("product_id", itemRagtag.getProductId()));
		urlParameters.add(new BasicNameValuePair("product_class_id", itemRagtag.getProductClassId()));
		urlParameters.add(new BasicNameValuePair("quantity", itemRagtag.getQuantity() + ""));
		urlParameters.add(new BasicNameValuePair("favorite_product_id", ""));
		urlParameters.add(new BasicNameValuePair("transactionid", itemRagtag.getTransactionid()));
		
		AutoRagtag.logger.info("Add product :" + itemRagtag.getProductCode());
		try {
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			httpClient.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exit() {
		this.isExit= true;
		ragCrawler.quitDriver();
	}

	public RagtagCrawler getRagCrawler() {
		return ragCrawler;
	}

	public void setRagCrawler(RagtagCrawler ragCrawler) {
		this.ragCrawler = ragCrawler;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

}
