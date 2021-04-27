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

import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.example2.HelloService2;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Service
public class HelloServiceImpl_CallOtherDubboService implements HelloService {

	@Reference
	private HelloService2 helloService2;
	
	public HelloServiceImpl_CallOtherDubboService() {
		System.out.println("1111111111111111111111111");
		log.info("22222222222222222222222");
	}
	
    @Override
    public String sayHello(String name) {
        // System.out.println("async provider received: " + name);
        // return "annotation: hello, " + name;
        //throw new RuntimeException("Exception to show hystrix enabled.");
    	//return "hello "+name;
    	return helloService2.sayHello(name);
    }

}
