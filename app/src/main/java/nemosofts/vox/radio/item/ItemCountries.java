package nemosofts.vox.radio.item;

public class ItemCountries {

    private final String id;
    private final String title;
    private final String image;
    private final String image_thumb;
    private final String total_post;

    public ItemCountries(String id, String title, String image, String image_thumb, String total_post) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.image_thumb = image_thumb;
        this.total_post = total_post;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getImageThumb() {
        return image_thumb;
    }

    public String getTotalPost() {
        return total_post;
    }
}
