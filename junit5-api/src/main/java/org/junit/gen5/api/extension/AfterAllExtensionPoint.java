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
 * {@code AfterAllExtensionPoint} defines the API for {@link Extension
 * Extensions} that wish to provide additional behavior to tests after
 * all test methods have been invoked.
 *
 * <p>Concrete implementations often implement {@link BeforeAllExtensionPoint} as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.AfterAll
 * @see BeforeAllExtensionPoint
 * @see BeforeEachExtensionPoint
 * @see AfterEachExtensionPoint
 */
@FunctionalInterface
public interface AfterAllExtensionPoint extends ExtensionPoint {

	/**
	 * Configuration for {@code AfterAllExtensionPoint}
	 */
	ExtensionPointConfiguration CONFIG = new ExtensionPointConfiguration(
		new ExtensionPointRegistry.Position[] { OUTERMOST, OUTSIDE_DEFAULT, DEFAULT, INSIDE_DEFAULT, INNERMOST },
		ExtensionPointRegistry.ApplicationOrder.BACKWARD);

	/**
	 * Callback that is invoked <em>after</em> all test methods have been invoked.
	 *
	 * @param context the current container extension context
	 */
	void afterAll(ContainerExtensionContext context) throws Exception;

}
