package com.youra.radiofr.item;

public class ItemAbout {

	private final String app_email, app_author, app_contact, app_website, app_description, app_developed_by;

	public ItemAbout(String app_email, String app_author, String app_contact, String app_website, String app_description, String app_developed_by) {
		this.app_email = app_email;
		this.app_author = app_author;
		this.app_contact = app_contact;
		this.app_website = app_website;
		this.app_description = app_description;
		this.app_developed_by = app_developed_by;
	}

	public String getEmail() {
		return app_email;
	}

	public String getAuthor() {
		return app_author;
	}

	public String getContact() {
		return app_contact;
	}

	public String getWebsite() {
		return app_website;
	}

	public String getAppDesc() {
		return app_description;
	}

	public String getDevelopedBY() {
		return app_developed_by;
	}
}