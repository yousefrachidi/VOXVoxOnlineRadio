package nemosofts.vox.radio.item;

public class ItemBanner {

	private final String bid;
	private final String title;
	private final String info;
	private final String image;

	public ItemBanner(String bid, String title, String info, String image) {
		this.bid = bid;
		this.title = title;
		this.info = info;
		this.image = image;
	}

	public String getId() {
		return bid;
	}

	public String getTitle() {
		return title;
	}

	public String getInfo() {
		return info;
	}

	public String getImage() {
		return image;
	}
}