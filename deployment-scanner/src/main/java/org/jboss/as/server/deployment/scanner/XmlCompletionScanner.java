/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.as.server.deployment.scanner;

import static org.jboss.as.server.deployment.scanner.logging.DeploymentScannerLogger.ROOT_LOGGER;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.as.server.deployment.scanner.logging.DeploymentScannerLogger;
import org.wildfly.common.xml.SAXParserFactoryUtil;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Determines if an XML document is well formed, to prevent half copied XML files from being deployed
 *
 * @author Stuart Douglas
 */
public class XmlCompletionScanner {


    public static boolean isCompleteDocument(final File file) throws IOException {
        ErrorHandler handler = new ErrorHandler(file.getName());
        try {
            SAXParserFactory factory = SAXParserFactoryUtil.create();
            factory.setValidating(false);
            final SAXParser parser = factory.newSAXParser();
            parser.parse(file, handler);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            DeploymentScannerLogger.ROOT_LOGGER.debugf(e, "Exception parsing scanned XML document %s", file);
            return false;
        }
        return !handler.error;
    }

    private static class ErrorHandler extends DefaultHandler {

        private boolean error = false;
        private final String fileName;

        public ErrorHandler(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void error(final SAXParseException e) throws SAXException {
            traceError(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            traceError(e);
            throw(e);
        }

        private void traceError(SAXParseException e) {
            error = true;
            ROOT_LOGGER.info(ROOT_LOGGER.invalidXmlFileFound(fileName, e.getLineNumber(), e.getColumnNumber()));
        }
    }
}
