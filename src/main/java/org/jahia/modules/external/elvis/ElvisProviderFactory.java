/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.external.elvis;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.modules.external.ExternalContentStoreProvider;
import org.jahia.modules.external.elvis.admin.MountPointFactory;
import org.jahia.modules.external.elvis.communication.ElvisCacheManager;
import org.jahia.modules.external.elvis.communication.ElvisSession;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.ProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * This is the provider for Elvis API
 *
 * @author Damien GAILLARD
 */
public class ElvisProviderFactory implements ProviderFactory, ApplicationContextAware, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ElvisProviderFactory.class);

    private ApplicationContext applicationContext;
    private List<String> reservedNodes;
    private ElvisCacheManager elvisCacheManager;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {}

    @Override
    public String getNodeTypeName() {
        return "elvisnt:mountPoint";
    }

    @Override
    public JCRStoreProvider mountProvider(JCRNodeWrapper mountPoint) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.info("Elvis external provider module initialization: mountPoint.getPath() - " + mountPoint.getPath() + ", name - " + mountPoint.getName());
        }

        // Define the provider basing on the mount point parameters
        ExternalContentStoreProvider externalContentStoreProvider = (ExternalContentStoreProvider) SpringContextSingleton.getBean("ExternalStoreProviderPrototype");
        externalContentStoreProvider.setKey(mountPoint.getIdentifier());
        externalContentStoreProvider.setMountPoint(mountPoint.getPath());

        // Define the datasource using the credentials defined in the mount point
        ElvisDataSource dataSource = new ElvisDataSource();
        // Set configuration and elvisSession in the datasource
        dataSource.setConfiguration((ElvisConfiguration) applicationContext.getBean("elvisConfiguration"));
        dataSource.setElvisSession(new ElvisSession(mountPoint.getPropertyAsString(MountPointFactory.URL),
                                                    mountPoint.getPropertyAsString(MountPointFactory.USER_NAME),
                                                    mountPoint.getPropertyAsString(MountPointFactory.PASSWORD),
                                                    mountPoint.getPropertyAsString(MountPointFactory.FILE_LIMIT),
                                                    mountPoint.getProperty(MountPointFactory.USE_PREVIEW).getBoolean(),
                                                    mountPoint.getPropertyAsString(MountPointFactory.PREVIEW_SETTINGS),
                                                    mountPoint.getPropertyAsString(MountPointFactory.FIELD_TO_WRITE_USAGE),
                                                    mountPoint.getProperty(MountPointFactory.TRUST_ALL_CERTIFICATE).getBoolean(),
                                                    mountPoint.getPath(), elvisCacheManager));
        // Start the datasource
        dataSource.start();
        // Finalize the provider setup with datasource and some JCR parameters
        externalContentStoreProvider.setDataSource(dataSource);
        externalContentStoreProvider.setDynamicallyMounted(true);
        externalContentStoreProvider.setSessionFactory(JCRSessionFactory.getInstance());
        externalContentStoreProvider.setReservedNodes(reservedNodes);
        try {
            externalContentStoreProvider.start();
        } catch (JahiaInitializationException e) {
            logger.error(e.getMessage(), e);
        }

        if (logger.isDebugEnabled()) {
            logger.info("Elvis external provider module initialized");
        }

        return externalContentStoreProvider;
    }

    public void setReservedNodes(List<String> reservedNodes) {
        this.reservedNodes = reservedNodes;
    }

    public void setElvisCacheManager(ElvisCacheManager elvisCacheManager) {
        this.elvisCacheManager = elvisCacheManager;
    }
}
