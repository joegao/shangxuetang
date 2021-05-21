package com.bhz.mail.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.script.ScriptExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bhz.mail.config.database.ReadOnlyConnection;
import com.bhz.mail.entity.MailSend;
import com.bhz.mail.enumeration.MailStatus;
import com.bhz.mail.enumeration.RedisPriorityQueue;
import com.bhz.mail.mapper.MailSend1Mapper;
import com.bhz.mail.mapper.MailSend2Mapper;
import com.bhz.mail.utils.FastJsonConvertUtil;

@Service
public class MailSendService {

	private static Logger LOGGER = LoggerFactory.getLogger(MailSendService.class);
	
	@Autowired
	private MailSend1Mapper mailSend1Mapper;
	
	@Autowired
	private MailSend2Mapper mailSend2Mapper;
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;		//集群模式[链接池]
	
	
	//1.	lua 
	
	//2. 	zookeeper ---> Curator lock  *** 
	
	/**
	 * @param mailSend
	 * @throws Exception
	 */
	public void insert(MailSend mailSend) throws Exception {
		//KeyUtil ==> uuid 有顺序的
		int hashCode = mailSend.getSendId().hashCode();
		if(hashCode % 2 == 0){
			mailSend2Mapper.insert(mailSend);
		} else {
			mailSend1Mapper.insert(mailSend);
		}
	}

	public void sendRedis(MailSend mailSend) {
		
		//mailSend 
		
		int hashCode = mailSend.getSendId().hashCode();
		if(hashCode % 2 == 0){
			mailSend = mailSend2Mapper.selectByPrimaryKey(mailSend.getSendId());
		} else {
			mailSend = mailSend1Mapper.selectByPrimaryKey(mailSend.getSendId());
		}
		
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		
		Long priority = mailSend.getSendPriority();
		
		Long ret = 0L;
		Long size = 0L;
		
		if(priority < 4L){
			//进入延迟队列 1 2 3  //返回的结果是最新的容器长度
			//
			ret = opsForList.rightPush(RedisPriorityQueue.DEFER_QUEUE.getCode(), FastJsonConvertUtil.convertObjectToJSON(mailSend));
			size = opsForList.size(RedisPriorityQueue.DEFER_QUEUE.getCode());
		} 
		else if( priority > 3 && priority < 7L) {
			//进入普通队列4 5 6 
			ret = opsForList.rightPush(RedisPriorityQueue.NORMAL_QUEUE.getCode(), FastJsonConvertUtil.convertObjectToJSON(mailSend));
			size = opsForList.size(RedisPriorityQueue.NORMAL_QUEUE.getCode());			
		} 
		else {
			//进入紧急队列7 8 9
			ret = opsForList.rightPush(RedisPriorityQueue.FAST_QUEUE.getCode(), FastJsonConvertUtil.convertObjectToJSON(mailSend));
			size = opsForList.size(RedisPriorityQueue.FAST_QUEUE.getCode());
		}
		
		//只要进行消息投递 则count + 1
		mailSend.setSendCount(mailSend.getSendCount() + 1);
		
		
		//成功:
		if(ret == size) {
			mailSend.setSendStatus(MailStatus.SEND_IN.getCode());
			
			if(mailSend.getSendId().hashCode() % 2 == 0){
				mailSend2Mapper.updateByPrimaryKeySelective(mailSend);
			} else{
				mailSend1Mapper.updateByPrimaryKeySelective(mailSend);
			}
			LOGGER.info("-----进入队列成功, id : {}-----", mailSend.getSendId());
			
		} 
		//失败:
		else {
			//投递失败的时候 
			if(mailSend.getSendId().hashCode() % 2 == 0){
				mailSend2Mapper.updateByPrimaryKeySelective(mailSend);
			} else{
				mailSend1Mapper.updateByPrimaryKeySelective(mailSend);
			}
			
			LOGGER.info("-----进入队列失败, 等待轮训机制重新投递, id : {}-----", mailSend.getSendId());
			
			
		}
	}

	
	/**
	 * 链接从数据库 读取暂存的邮件消息内容
	 * @return
	 */
	@ReadOnlyConnection
	public List<MailSend> queryDraftList() {
		List<MailSend> list = new ArrayList<MailSend>();
		list.addAll(mailSend1Mapper.queryDraftList());
		list.addAll(mailSend2Mapper.queryDraftList());
		return list;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
