package com.youra.radiofr.item;

import java.io.Serializable;


public class ItemRecorder implements Serializable{

	private final String id;
	private final String mp3;
	private final String title;
	private final String duration;
	long filesets;

	public ItemRecorder(String id, String mp3, String title, String duration, long filesets) {
		this.id = id;
		this.mp3 = mp3;
		this.title = title;
		this.duration = duration;
		this.filesets = filesets;
	}

	public String getId() {
		return id;
	}

	public String getMp3() {
		return mp3;
	}

	public String getTitle() {
		return title;
	}

	public String getDuration(){
		return duration;
	}

	public long getFilesets(){
		return filesets;
	}

}