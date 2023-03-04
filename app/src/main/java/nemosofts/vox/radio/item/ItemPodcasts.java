package nemosofts.vox.radio.item;

public class ItemPodcasts {

    private final String id;
    private final String title;
    private final String image;
    private final String image_thumb;
    private final String description;

    public ItemPodcasts(String id, String title, String image, String image_thumb, String description) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.image_thumb = image_thumb;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}
