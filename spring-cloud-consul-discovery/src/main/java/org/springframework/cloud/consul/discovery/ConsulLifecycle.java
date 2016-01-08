/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;
import org.springframework.cloud.consul.ConsulProperties;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ConsulLifecycle extends AbstractDiscoveryLifecycle {

    class MyService extends NewService {
        @com.google.gson.annotations.SerializedName("Address")
        private String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "NewService{" +
                    "id='" + getId() + '\'' +
                    ", name='" + getName() + '\'' +
                    ", tags=" + getTags() +
                    ", address=" + getAddress() +
                    ", port=" + getPort() +
                    ", check=" + getCheck() +
                    '}';
        }
    }

    class MyCheck extends NewService.Check {

    }

    @Autowired
	private ConsulClient client;

	@Autowired
	private ConsulProperties consulProperties;

	@Autowired
	private TtlScheduler ttlScheduler;

	@Autowired
	private HeartbeatProperties ttlConfig;

	@Override
	protected void register() {
		MyService service = new MyService();

        String address = getEnvironment().getProperty("consul.serviceOverride.address", "");

        // If the value is set to a known value "localhost" - resolve it to the local IP
        if (address == "localhost") {
            try {
                address = Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        service.setAddress(address);

        String appName = getAppName();
		// TODO: move id to properties with context ID as default
		service.setId(getContext().getId());
		service.setName(appName);
		// TODO: support port = 0 random assignment
		Integer port = new Integer(getEnvironment().getProperty("server.port", "8080"));
		service.setPort(port);
		service.setTags(consulProperties.getTags());
		NewService.Check check = new NewService.Check();
		check.setTtl(ttlConfig.getTtl());
		service.setCheck(check);
		register(service);
	}

	@Override
	protected void registerManagement() {
		MyService management = new MyService();
		management.setId(getManagementServiceId());
		management.setName(getManagementServiceName());
		management.setPort(getManagementPort());
		management.setTags(consulProperties.getManagementTags());

		register(management);
	}

	protected void register(MyService service) {
		log.info("Registering service with consul: {}", service.toString());
		client.agentServiceRegister(service);
//        CatalogRegistration catalogRegistration = new CatalogRegistration();
//        catalogRegistration.setAddress(service.getAddress());
//        catalogRegistration.setDatacenter("dev");
//        catalogRegistration.setNode("devhost");
//
//        CatalogRegistration.Service service1 = new CatalogRegistration.Service();
//        service1.setId(service.getId());
//        service1.setPort(service.getPort());
//        service1.setService(service.getName());
//        service1.setTags(service.getTags());
//        catalogRegistration.setService(service1);

//        client.catalogRegister(catalogRegistration);
		ttlScheduler.add(service);
	}

	@Override
	protected Object getConfiguration() {
		return consulProperties;
	}

	@Override
	protected void deregister() {
		deregister(getContext().getId());
	}

	@Override
	protected void deregisterManagement() {
		deregister(getManagementServiceName());
	}

	private void deregister(String serviceId) {
		ttlScheduler.remove(serviceId);
		client.agentServiceDeregister(serviceId);
	}

	@Override
	protected boolean isEnabled() {
		return consulProperties.isEnabled();
	}
}
