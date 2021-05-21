package com.bhz.mail;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.script.ScriptExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bhz.mail.entity.MstDict;
import com.bhz.mail.mapper.MstDictMapper;
import com.bhz.mail.service.MstDictService;
import com.github.pagehelper.PageHelper;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTests {

	@Resource(name="masterDataSource")
	private DataSource masterDataSource;
	
	@Resource(name="slaveDataSource")
	private DataSource slaveDataSource;
	
	@Test
	public void contextLoads()throws Exception {
		Connection c1 = masterDataSource.getConnection("root", "root");
		System.err.println("c1: " + c1.getMetaData().getURL());
		Connection c2 = slaveDataSource.getConnection("root", "root");
		System.err.println("c2: " + c2.getMetaData().getURL());
	}
	
	@Autowired
	private MstDictMapper mstDictMapper;
	
	
	@Test
	public void test1() throws Exception {
		PageHelper.startPage(1, 2);
		List<MstDict> list = mstDictMapper.selectAll();
		for(MstDict md : list){
			
			System.err.println(md.getName());
		}
	}
	
	@Autowired
	private MstDictService mstDictService;
	
	@Test
	public void test2() throws Exception {
		List<MstDict> list =mstDictService.findByStatus("1");
		for(MstDict md : list){
			System.err.println(md.getName());
		}
	}
	
	
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Test
	public void test3() throws Exception {
		
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		
		opsForValue.set("name", "yxxy");
		
		System.err.println("name: " + opsForValue.get("name"));
		//redisTemplate.delete("name");
		
	}
	
	
	
	
	
	
	
	

}
