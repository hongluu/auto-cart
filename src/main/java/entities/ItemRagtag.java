package entities;

public class ItemRagtag extends BaseItem {
	private String productCode;
	private String productId;
	private String productClassId;
	private int quantity;
	private String transactionid;

	public ItemRagtag() {
		this.quantity = 1;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductClassId() {
		return productClassId;
	}

	public void setProductClassId(String productClassId) {
		this.productClassId = productClassId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getTransactionid() {
		return transactionid;
	}

	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}

}
