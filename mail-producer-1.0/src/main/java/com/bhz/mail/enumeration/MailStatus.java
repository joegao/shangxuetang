package com.bhz.mail.enumeration;

public enum MailStatus {

	/** 暂存/待发送 */
	DRAFT("0"),
	
	/** 发送中/已经进入Redis队列 */
	SEND_IN("1"),
	
	/** 发送成功 */
	NEED_OK("2"),
	
	/** 发送失败 */
	NEED_ERR("3");

	private String code;

	private MailStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
