package com.sc.chatserver;

import java.util.Date;

public class MessageFromClient {
	private String text;
	private String id;
	private Date datetime;
	private String from;
	private String to;

	private String chatId;

	public MessageFromClient() {
	}

	public MessageFromClient(String text, String id, Date datetime, String from, String to, String chatId) {
		this.text = text;
		this.id = id;
		this.datetime = datetime;
		this.from = from;
		this.to = to;
		this.chatId = chatId;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}
}
