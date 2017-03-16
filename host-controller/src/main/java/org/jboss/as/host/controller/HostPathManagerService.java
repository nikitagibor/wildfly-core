/*
* JBoss, Home of Professional Open Source.
* Copyright 2012, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.host.controller;

import org.jboss.as.controller.CapabilityRegistry;
import org.jboss.as.controller.services.path.PathManagerService;
import org.jboss.as.server.ServerPathManagerService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

/**
 * Service containing the paths for a HC/DC
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class HostPathManagerService extends PathManagerService {

    public static ServiceController<?> addService(ServiceTarget serviceTarget, HostPathManagerService service, HostControllerEnvironment hostEnvironment) {
        ServiceBuilder<?> serviceBuilder = serviceTarget.addService(SERVICE_NAME, service);

        // Add resources and capabilities for the always-present paths
        service.addHardcodedAbsolutePath(serviceTarget, HostControllerEnvironment.HOME_DIR, hostEnvironment.getHomeDir().getAbsolutePath());
        service.addHardcodedAbsolutePath(serviceTarget, HostControllerEnvironment.DOMAIN_CONFIG_DIR, hostEnvironment.getDomainConfigurationDir().getAbsolutePath());
        service.addHardcodedAbsolutePath(serviceTarget, HostControllerEnvironment.DOMAIN_DATA_DIR, hostEnvironment.getDomainDataDir().getAbsolutePath());
        service.addHardcodedAbsolutePath(serviceTarget, HostControllerEnvironment.DOMAIN_LOG_DIR, hostEnvironment.getDomainLogDir().getAbsolutePath());
        service.addHardcodedAbsolutePath(serviceTarget, HostControllerEnvironment.DOMAIN_TEMP_DIR, hostEnvironment.getDomainTempDir().getAbsolutePath());
        service.addHardcodedAbsolutePath(serviceTarget, HostControllerEnvironment.CONTROLLER_TEMP_DIR, hostEnvironment.getDomainTempDir().getAbsolutePath());

        // Add the standard server path capabilities so server config resources can reference them
        ServerPathManagerService.registerDomainServerPathCapabilities(service.localCapRegRef);

        return serviceBuilder.install();
    }

    private final CapabilityRegistry localCapRegRef;

    public HostPathManagerService(CapabilityRegistry capabilityRegistry) {
        super(capabilityRegistry);
        this.localCapRegRef = capabilityRegistry;
    }
}
