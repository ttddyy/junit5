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
 * {@code BeforeEachExtensionPoint} defines the API for {@link Extension
 * Extensions} that wish to provide additional behavior to tests before
 * each test method has been invoked.
 *
 * <p>Concrete implementations often implement {@link AfterEachExtensionPoint} as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.BeforeEach
 * @see AfterEachExtensionPoint
 * @see BeforeAllExtensionPoint
 * @see AfterAllExtensionPoint
 */
@FunctionalInterface
public interface BeforeEachExtensionPoint extends ExtensionPoint {

	/**
	 * Configuration for {@code BeforeEachExtensionPoint}
	 */
	ExtensionPointConfiguration CONFIG = new ExtensionPointConfiguration(
		new ExtensionPointRegistry.Position[] { OUTERMOST, OUTSIDE_DEFAULT, DEFAULT, INSIDE_DEFAULT, INNERMOST },
		ExtensionPointRegistry.ApplicationOrder.FORWARD);

	/**
	 * Callback that is invoked <em>before</em> each test method has been invoked.
	 *
	 * @param context the current test extension context
	 */
	void beforeEach(TestExtensionContext context) throws Exception;

}
