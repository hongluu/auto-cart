package entities;

public class Brand {
	private String brandName;
	private String brandCode;
	public Brand(String brandName, String brandCode) {
		this.brandName = brandName;
		this.brandCode = brandCode;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getBrandCode() {
		return brandCode;
	}
	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}
	@Override
	public String toString(){
		return brandName;
	}
	public String getBrandNameForCrawl() {
		return brandName.replaceAll(" ", "+");
	}
	
}
