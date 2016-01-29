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

import static org.junit.gen5.api.extension.ExtensionPointRegistry.Position.DEFAULT;

import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;

/**
 * Super interface for all extension points.
 *
 * <p>An {@code ExtensionPoint} can be registered declaratively via
 * {@link ExtendWith @ExtendWith} or programmatically via an
 * {@link ExtensionRegistrar}.
 *
 * @since 5.0
 * @see ContainerExecutionCondition
 * @see TestExecutionCondition
 * @see InstancePostProcessor
 * @see ExceptionHandlerExtensionPoint
 * @see MethodParameterResolver
 * @see BeforeEachExtensionPoint
 * @see AfterEachExtensionPoint
 * @see BeforeAllExtensionPoint
 * @see AfterAllExtensionPoint
 * @see ExtensionRegistrar
 */
public interface ExtensionPoint extends Extension {

	/**
	 * The set of allowed values for the {@link Position} when registering an {@code ExtensionPoint}
	 * using {@link ExtensionPointRegistry#register(ExtensionPoint, Position)}.
	 *
	 * <p>Field is used through reflection!</p>
	 */
	Position[] ALLOWED_POSITIONS = { DEFAULT };

}
