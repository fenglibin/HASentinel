/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.example;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.eeefff.hasentinel.demo.spring.boot.provider.TestBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HelloServiceImpl implements HelloService {
	
	@Value("${test.key:#{null}}")
	private String testKey;
	
	@Autowired
	private TestBean testBean;
	
	public HelloServiceImpl() {
		System.out.println("1111111111111111111111111");
		log.info("22222222222222222222222, aTestKey is:"+testKey);
	}
	
    @Override
    public String sayHello(String name) {
        // System.out.println("async provider received: " + name);
        // return "annotation: hello, " + name;
        //throw new RuntimeException("Exception to show hystrix enabled.");
    	int random  = (int)(10*Math.random());
    	try {
			Thread.sleep(random);
		} catch (InterruptedException e) {
		}
    	return "random is "+random+" hello "+name+", aTestKey is "+testKey+", test test key is:"+testBean.getTestKey();
    }

	public String getTestKey() {
		return testKey;
	}

	public void setTestKey(String testKey) {
		this.testKey = testKey;
	}
    
}
