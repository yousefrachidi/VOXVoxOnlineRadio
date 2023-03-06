package com.youra.radiofr.item;

import java.io.Serializable;

public class ItemRadio implements Serializable{

	private final String id;
	private final String cat_id;
	private final String radio_title;
	private final String radio_url;
	private final String image;
	private final String image_thumb;
	private String averageRating;
	private String totalRate;
	private final String total_views;
	private final String category_name;
	private Boolean isFavourite;
	private final Boolean isPremium;
	private String userRating="";
	private String userMessage="" ;

	public ItemRadio(String id, String cat_id, String radio_title, String radio_url, String image, String image_thumb, String averageRating, String totalRate, String total_views, String category_name, Boolean isPremium, Boolean isFavourite) {
		this.id = id;
		this.cat_id = cat_id;
		this.radio_title = radio_title;
		this.radio_url = radio_url;
		this.image = image;
		this.image_thumb = image_thumb;
		this.averageRating = averageRating;
		this.totalRate = totalRate;
		this.total_views = total_views;
		this.category_name = category_name;
		this.isPremium = isPremium;
		this.isFavourite = isFavourite;
	}

	public String getId() {
		return id;
	}

	public String getCatID() {
		return cat_id;
	}

	public String getRadioTitle() {
		return radio_title;
	}

	public String getRadioUrl() {
		return radio_url;
	}

	public String getImage() {
		return image;
	}

	public String getImageThumb() {
		return image_thumb;
	}

	public String getAverageRating() {
		return averageRating;
	}

	public String getTotalRate() {
		return totalRate;
	}

	public void setAverageRating(String averageRating) {
		this.averageRating = averageRating;
	}

	public void setTotalRate(String totalRate) {
		this.totalRate = totalRate;
	}

	public String getTotalViews() {
		return total_views;
	}

	public String getCategoryName() {
		return category_name;
	}

	public Boolean IsFav() {
		return isFavourite;
	}
	public void setIsFav(Boolean favourite) {
		isFavourite = favourite;
	}

	public Boolean IsPremium() {
		return isPremium;
	}

	public String getUserRating() {
		return userRating;
	}
	public void setUserRating(String userRating) {
		this.userRating = userRating;
	}


	public String getUserMessage() {
		return userMessage;
	}
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
}