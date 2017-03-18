package context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareContext {
	private static Map<String, List<String>> map;
	private static List<String> addedLinkProducts ;

	public static Map<String, List<String>> getMap() {
		return map;
	}

	public static void setMap(Map<String, List<String>> map) {
		ShareContext.map = map;
	}

	public static List<String> getAddedLinkProducts() {
		return addedLinkProducts;
	}

	public static void setAddedLinkProducts(List<String> addedLinkProducts) {
		ShareContext.addedLinkProducts = addedLinkProducts;
	}
}
