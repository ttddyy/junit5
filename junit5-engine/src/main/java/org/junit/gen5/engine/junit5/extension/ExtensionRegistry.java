/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.gen5.api.extension.AfterAllExtensionPoint;
import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeAllExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.ExceptionHandlerExtensionPoint;
import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPointConfiguration;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.ExtensionPointRegistry.ApplicationOrder;
import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExecutionCondition;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * An {@code ExtensionRegistry} holds all registered extensions (i.e.
 * instances of {@link ExtensionPoint}) for a given
 * {@link org.junit.gen5.engine.support.hierarchical.Container} or
 * {@link org.junit.gen5.engine.support.hierarchical.Leaf}.
 *
 * <p>A registry has a reference to its parent registry, and all lookups are
 * performed first in the current registry itself and then in its parent and
 * thereby all its ancestors.
 *
 * @since 5.0
 * @see ExtensionPointRegistry
 * @see ExtensionRegistrar
 */
public class ExtensionRegistry {

	/**
	 * Register all subtypes of {@link ExtensionPoint} that have a different configuration
	 * than {@link ExtensionPointConfiguration#DEFAULT}
	 */
	static {
		//TODO: Replace static block by a different solution. Or maybe not.
		ExtensionPointConfiguration.registerType(BeforeEachExtensionPoint.class, BeforeEachExtensionPoint.CONFIG);
		ExtensionPointConfiguration.registerType(BeforeAllExtensionPoint.class, BeforeAllExtensionPoint.CONFIG);
		ExtensionPointConfiguration.registerType(AfterEachExtensionPoint.class, AfterEachExtensionPoint.CONFIG);
		ExtensionPointConfiguration.registerType(AfterAllExtensionPoint.class, AfterAllExtensionPoint.CONFIG);
		ExtensionPointConfiguration.registerType(InstancePostProcessor.class, InstancePostProcessor.CONFIG);
		ExtensionPointConfiguration.registerType(ExceptionHandlerExtensionPoint.class,
			ExceptionHandlerExtensionPoint.CONFIG);
		ExtensionPointConfiguration.registerType(ContainerExecutionCondition.class,
			ExtensionPointConfiguration.DEFAULT);
		ExtensionPointConfiguration.registerType(TestExecutionCondition.class, ExtensionPointConfiguration.DEFAULT);
		ExtensionPointConfiguration.registerType(MethodParameterResolver.class, ExtensionPointConfiguration.DEFAULT);
	}

	private static ExtensionPointConfiguration getConfigurationFor(Class<? extends ExtensionPoint> extensionPointType) {
		return ExtensionPointConfiguration.getConfigurationForType(extensionPointType);
	}

	/**
	 * Factory for creating and populating a new registry from a list of
	 * extension types and a parent registry.
	 *
	 * @param parentRegistry the parent registry to be used
	 * @param extensionTypes the types of extensions to be registered in
	 * the new registry
	 * @return a new {@code ExtensionRegistry}
	 */
	public static ExtensionRegistry newRegistryFrom(ExtensionRegistry parentRegistry,
			List<Class<? extends Extension>> extensionTypes) {

		ExtensionRegistry newExtensionRegistry = new ExtensionRegistry(parentRegistry);
		extensionTypes.forEach(newExtensionRegistry::registerExtension);
		return newExtensionRegistry;
	}

	private static final Logger LOG = Logger.getLogger(ExtensionRegistry.class.getName());

	private static final List<Class<? extends Extension>> defaultExtensionTypes = Collections.unmodifiableList(
		Arrays.asList(DisabledCondition.class, TestInfoParameterResolver.class, TestReporterParameterResolver.class));

	/**
	 * @return the list of all extension types that are added by default to all root registries
	 */
	static List<Class<? extends Extension>> getDefaultExtensionTypes() {
		return defaultExtensionTypes;
	}

	private final Set<Class<? extends Extension>> registeredExtensionTypes = new LinkedHashSet<>();

	private final List<RegisteredExtensionPoint<?>> registeredExtensionPoints = new ArrayList<>();

	private final Optional<ExtensionRegistry> parent;

	public ExtensionRegistry() {
		this(null);
	}

	ExtensionRegistry(ExtensionRegistry parent) {
		this.parent = Optional.ofNullable(parent);
		if (!this.parent.isPresent()) {
			addDefaultExtensions();
		}
	}

	private void addDefaultExtensions() {
		getDefaultExtensionTypes().stream().forEach(this::registerExtension);
	}

	/**
	 * @return all extension types registered in this registry or one of its ancestors
	 */
	Set<Class<? extends Extension>> getRegisteredExtensionTypes() {
		Set<Class<? extends Extension>> allRegisteredExtensionTypes = new LinkedHashSet<>();
		this.parent.ifPresent(
			parentRegistry -> allRegisteredExtensionTypes.addAll(parentRegistry.getRegisteredExtensionTypes()));
		allRegisteredExtensionTypes.addAll(this.registeredExtensionTypes);
		return Collections.unmodifiableSet(allRegisteredExtensionTypes);
	}

	@SuppressWarnings("unchecked")
	private <E extends ExtensionPoint> List<RegisteredExtensionPoint<E>> getRegisteredExtensionPoints(
			Class<E> extensionType) {

		List<RegisteredExtensionPoint<E>> allExtensionPoints = new ArrayList<>();
		this.parent.ifPresent(
			parentRegistry -> allExtensionPoints.addAll(parentRegistry.getRegisteredExtensionPoints(extensionType)));

		// @formatter:off
		this.registeredExtensionPoints.stream()
				.filter(registeredExtensionPoint -> extensionType.isAssignableFrom(registeredExtensionPoint.getExtensionPoint().getClass()))
				.forEach(extensionPoint -> allExtensionPoints.add((RegisteredExtensionPoint<E>) extensionPoint));
		// @formatter:on

		return allExtensionPoints;
	}

	/**
	 * Return a stream for iterating over all registered extension points
	 * of the specified type.
	 *
	 * @param extensionPointType the type of {@link ExtensionPoint} to stream
	 */
	public <E extends ExtensionPoint> Stream<RegisteredExtensionPoint<E>> stream(Class<E> extensionPointType) {

		List<RegisteredExtensionPoint<E>> registeredExtensionPoints = getRegisteredExtensionPoints(extensionPointType);

		Position[] allowedPositions = getConfigurationFor(extensionPointType).getAllowedPositions();
		ApplicationOrder order = getConfigurationFor(extensionPointType).getApplicationOrder();

		new ExtensionPointSorter().sort(registeredExtensionPoints, allowedPositions);
		if (order == ApplicationOrder.BACKWARD) {
			Collections.reverse(registeredExtensionPoints);
		}
		return registeredExtensionPoints.stream();
	}

	/**
	 * Instantiate an extension of the given type using its default constructor,
	 * and potentially register it in this registry.
	 *
	 * <p>If the extension is an {@link ExtensionPoint}, it will be registered
	 * in this registry, unless an extension of the given type already exists
	 * in this registry.
	 *
	 * <p>If the extension is an {@link ExtensionRegistrar},
	 * its {@link ExtensionRegistrar#registerExtensions registerExtensions()}
	 * method will be invoked to register extensions from the registrar in
	 * this registry.
	 *
	 * @param extensionType the type extension to register
	 */
	void registerExtension(Class<? extends Extension> extensionType) {

		boolean extensionAlreadyRegistered = getRegisteredExtensionTypes().stream().anyMatch(
			registeredType -> registeredType.equals(extensionType));

		if (!extensionAlreadyRegistered) {
			Extension extension = ReflectionUtils.newInstance(extensionType);
			registerExtensionPoint(extension);
			registerExtensionPointsFromRegistrar(extension);
			this.registeredExtensionTypes.add(extensionType);
		}
	}

	private void registerExtensionPoint(Extension extension) {
		if (extension instanceof ExtensionPoint) {
			registerExtensionPoint((ExtensionPoint) extension, extension);
		}
	}

	public void registerExtensionPoint(ExtensionPoint extension, Object source) {
		registerExtensionPoint(extension, source, Position.DEFAULT);
	}

	private void registerExtensionPoint(ExtensionPoint extension, Object source, Position position) {
		LOG.finer(() -> String.format("Registering extension point [%s] from source [%s] with position [%s].",
			extension, source, position));

		RegisteredExtensionPoint<ExtensionPoint> registeredExtensionPoint = new RegisteredExtensionPoint<>(extension,
			source, position);

		// @formatter:off
		ExtensionPointConfiguration.getAllExtensionPointTypes().stream()
			.filter(extensionPointType -> extensionPointType.isInstance(extension))
			.forEach(extensionPointType -> {
				Position[] allowedPositions = getConfigurationFor(extensionPointType).getAllowedPositions();
				checkOnlyAllowedPositions(registeredExtensionPoint, allowedPositions);
			});
		// @formatter:on

		this.registeredExtensionPoints.add(registeredExtensionPoint);
	}

	private <T extends ExtensionPoint> void checkOnlyAllowedPositions(RegisteredExtensionPoint<T> rep,
			Position[] allowedPositions) {

		if (notIn(rep.getPosition(), allowedPositions)) {
			String exceptionMessage = String.format("'%s' not allowed: %s", rep.getPosition(), rep.getSource());
			throw new ExtensionConfigurationException(exceptionMessage);
		}

	}

	private boolean notIn(Position position, Position[] allowedPositions) {
		return !Arrays.asList(allowedPositions).contains(position);
	}

	private void registerExtensionPointsFromRegistrar(Extension extension) {
		if (extension instanceof ExtensionRegistrar) {
			ExtensionRegistrar extensionRegistrar = (ExtensionRegistrar) extension;
			extensionRegistrar.registerExtensions(new DelegatingExtensionPointRegistry(extensionRegistrar));
		}
	}

	/**
	 * A {@code DelegatingExtensionPointRegistry} enables an
	 * {@link ExtensionRegistrar} to populate an {@link ExtensionRegistry}
	 * via the simpler {@link ExtensionPointRegistry} API.
	 *
	 * <p>Furthermore, a {@code DelegatingExtensionPointRegistry} internally
	 * delegates to the enclosing {@code ExtensionRegistry} to {@linkplain
	 * ExtensionRegistry#registerExtensionPoint(ExtensionPoint, Object, Position)
	 * register} extension points with the {@link RegisteredExtensionPoint#getSource() source}
	 * set to the {@code ExtensionRegistrar} supplied to this registry's
	 * constructor.
	 */
	private class DelegatingExtensionPointRegistry implements ExtensionPointRegistry {

		private final ExtensionRegistrar extensionRegistrar;

		DelegatingExtensionPointRegistry(ExtensionRegistrar extensionRegistrar) {
			this.extensionRegistrar = extensionRegistrar;
		}

		@Override
		public void register(ExtensionPoint extensionPoint, Position position) {
			registerExtensionPoint(extensionPoint, this.extensionRegistrar.getClass(), position);
		}

	}

}
