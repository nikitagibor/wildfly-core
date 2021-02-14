/*
* JBoss, Home of Professional Open Source.
* Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.server;

import org.jboss.as.controller.ExpressionResolver;
import org.jboss.as.controller.ExpressionResolverImpl;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.VaultReader;
import org.jboss.as.controller.logging.ControllerLogger;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class RuntimeExpressionResolver extends ExpressionResolverImpl {

    private static final Logger log = Logger.getLogger(RuntimeExpressionResolver.class);

    private static final String EXPRESSION_RESOLVER_CAPABILITY = "org.wildfly.controller.expression-resolver";

    private final VaultReader vaultReader;

    public RuntimeExpressionResolver(VaultReader vaultReader) {
        this.vaultReader = vaultReader;
    }

    @Override
    protected void resolvePluggableExpression(ModelNode node, OperationContext context) throws OperationFailedException {
        String expression = node.asString();
        if (expression.length() > 3) {
            String expressionValue = expression.substring(2, expression.length() -1);

            /*
             * Step 1 - Attempt to resolve using the VaultReader.
             */

            if (vaultReader == null) {
                // No VaultReader was configured or could be loaded given the modules on the classpath
                // This is common in WildFly Core itself as the org.picketbox module is not present
                // to allow loading the standard RuntimeVaultReader impl

                // Just check for a picektbox vault pattern and if present reject
                // We don't want to let vault expressions pass as other resolvers will treat the ":'
                // as a system property name vs default value delimiter
                if (VaultReader.STANDARD_VAULT_PATTERN.matcher(expressionValue).matches()) {
                    log.tracef("Cannot resolve %s -- it is in the default vault format but no vault reader is available", expressionValue);
                    throw ControllerLogger.ROOT_LOGGER.cannotResolveExpression(expression);
                }
                log.tracef("Not resolving %s -- no vault reader available and not in default vault format", expressionValue);
            } else if (vaultReader.isVaultFormat(expressionValue)) {
                try {
                    String retrieved = vaultReader.retrieveFromVault(expressionValue);
                    log.tracef("Retrieved %s from vault for %s", retrieved, expressionValue);
                    node.set(retrieved);
                    return;
                } catch (VaultReader.NoSuchItemException nsie) {
                    throw ControllerLogger.ROOT_LOGGER.cannotResolveExpression(expression);
                }
            } else {
                log.tracef("Not resolving %s -- not in vault format", expressionValue);
            }

            /*
             * Step 2 - Use ExpressionResolver capability if available.
             */

            // TODO The second condition can be removed.
            if (context != null && expressionValue.startsWith("ENC")) {
                try {
                    ExpressionResolver expressionResolver = context.getCapabilityRuntimeAPI(EXPRESSION_RESOLVER_CAPABILITY,
                                                                                            ExpressionResolver.class);
                    ModelNode result = expressionResolver.resolveExpressions(node);
                    if (result != null) {
                        node.set(result.asString());
                    }
                } catch (IllegalStateException | IllegalArgumentException e) {
                    // TODO Clean up error handling.
                    System.out.println("Capability not available " + e.getMessage());
                }
            }
        }
    }
}
