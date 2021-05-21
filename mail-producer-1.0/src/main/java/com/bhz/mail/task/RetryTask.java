package com.bhz.mail.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bhz.mail.entity.MailSend;
import com.bhz.mail.service.MailSendService;

@Component
public class RetryTask {

	private static Logger LOGGER =LoggerFactory.getLogger(RetryTask.class);
	@Autowired
	private MailSendService mailSendService;
	
	
	@Scheduled(initialDelay = 5000, fixedDelay = 10000)
	public void retry(){
		LOGGER.info("----------开始执行重新发送邮件任务------------");
		
		List<MailSend> list = mailSendService.queryDraftList();
		
		//重新发送邮件
		for(MailSend ms : list){
			mailSendService.sendRedis(ms);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
