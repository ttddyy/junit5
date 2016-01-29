/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import static org.junit.gen5.api.extension.ExtensionPointRegistry.Position.*;

/**
 * {@code InstancePostProcessor} defines the API for {@link Extension
 * Extensions} that wish to <em>post-process</em> test instances.
 *
 * <p>Common use cases include injecting dependencies into the test
 * instance, invoking custom initialization methods on the test instance,
 * etc.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 */
public interface InstancePostProcessor extends ExtensionPoint {

	ExtensionPointRegistry.Position[] ALLOWED_POSITIONS = { FIRST, DEFAULT, LAST };

	/**
	 * Callback for post-processing the test instance in the supplied
	 * {@link TestExtensionContext}.
	 *
	 * @param context the current test extension context
	 */
	void postProcessTestInstance(TestExtensionContext context) throws Exception;

}
