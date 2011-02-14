/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */

/**
 * Provides the classes and annotations used when writing tests with the JMockit mocking APIs.
 * <p/>
 * The {@link mockit.Mockit} class contains a group of {@code setUpMock/setUpMocks} static methods that can be used to
 * set up <em>state-oriented</em> mock implementations.
 * They rely on the {@link mockit.Mock} and {@link mockit.MockClass} annotations.
 * <p/>
 * The {@link mockit.MockUp} class is a convenient wrapper for common uses of the
 * {@link mockit.Mockit#setUpMock(Class, Object)} and {@link mockit.Mockit#setUpMock(Object)} methods, which allows the
 * definition of in-line mocks for individual tests.
 * <p/>
 * The {@link mockit.Expectations} class provides an API for the traditional <em>record-replay</em> model of recording
 * expected invocations which are later replayed and implicitly verified.
 * This API makes use of the {@link mockit.Mocked} annotation.
 * <p/>
 * The {@link mockit.Verifications} class, when combined with the {@link mockit.NonStrictExpectations} class and/or the
 * {@link mockit.NonStrict} annotation, extends the record-replay model to a <em>record-replay-verify</em> model, where
 * the record phase can be empty (or not) and the expected invocations can be verified explicitly <em>after</em>
 * exercising the code under test.
 */
package mockit;
