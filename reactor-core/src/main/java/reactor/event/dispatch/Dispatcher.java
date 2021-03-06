/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package reactor.event.dispatch;

import reactor.event.Event;
import reactor.event.registry.Registry;
import reactor.event.routing.Router;
import reactor.function.Consumer;
import reactor.function.Resource;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * A {@code Dispatcher} is used to {@link Dispatcher#dispatch(Object, Object, Registry, Consumer,
 * reactor.event.routing.Router,
 * Consumer)
 * dispatch} {@link Event}s to {@link Consumer}s. The details of how the dispatching is performed, for example on the
 * same thread or using a different thread, are determined by the implementation.
 *
 * @author Jon Brisbin
 * @author Andy Wilkinson
 * @author Stephane Maldini
 */
public interface Dispatcher extends Executor, Resource {

	/**
	 * Block until all submitted tasks have completed, then do a normal {@link #shutdown()}.
	 */
	boolean awaitAndShutdown();

	/**
	 * Block until all submitted tasks have completed, then do a normal {@link #shutdown()}.
	 */
	boolean awaitAndShutdown(long timeout, TimeUnit timeUnit);

	/**
	 * Instruct the {@code Dispatcher} to dispatch the {@code data} that has the given {@code key}. The {@link Consumer}s
	 * that will receive the event are selected from the {@code consumerRegistry}, and the event is routed to them using
	 * the {@code eventRouter}. In the event of an error during dispatching, the {@code errorConsumer} will be called. In
	 * the event of successful dispatching, the {@code completionConsumer} will be called.
	 *
	 * @param key                The key associated with the event
	 * @param data               The event
	 * @param consumerRegistry   The registry from which consumer's are selected
	 * @param errorConsumer      The consumer that is invoked if dispatch fails. May be {@code null}
	 * @param router             Used to route the event to the selected consumers
	 * @param completionConsumer The consumer that is driven if dispatch succeeds May be {@code null}
	 * @param <E>                type of the event
	 * @throws IllegalStateException If the {@code Dispatcher} is not {@link Dispatcher#alive() alive}
	 */
	<E> void dispatch(Object key,
	                  E data,
	                  Registry<Consumer<?>> consumerRegistry,
	                  Consumer<Throwable> errorConsumer,
	                  Router router,
	                  Consumer<E> completionConsumer);

	/**
	 * Instruct the {@code Dispatcher} to dispatch the given {@code data} using the given {@link Consumer}. This
	 * optimized
	 * route bypasses all selection and routing so provides a significant throughput boost. If an error occurs, the given
	 * {@code errorConsumer} will be invoked.
	 *
	 * @param data          the event
	 * @param router        invokes the {@code Consumer} in the correct thread
	 * @param errorConsumer consumer to invoke if dispatch fails (may be {@code null})
	 * @param <E>           type of the event
	 * @throws IllegalStateException If the {@code Dispatcher} is not {@link Dispatcher#alive() alive}
	 */
	<E> void dispatch(E data,
	                  Router router,
	                  Consumer<E> consumer,
	                  Consumer<Throwable> errorConsumer);

	/**
	 * Request the remaining capacity for the underlying shared state structure.
	 * E.g. {@link reactor.event.dispatch.RingBufferDispatcher} will return
	 * {@link com.lmax.disruptor.RingBuffer#remainingCapacity()}.
	 * <p>
	 *
	 * @return the remaining capacity if supported otherwise it returns a negative value.
	 * @since 2.0
	 */
	long remainingSlots();

	/**
	 * Request the capacity for the underlying shared state structure.
	 * E.g. {@link reactor.event.dispatch.RingBufferDispatcher} will return
	 * {@link com.lmax.disruptor.RingBuffer#getBufferSize()}.
	 * <p>
	 *
	 * @return the remaining capacity if supported otherwise it returns a negative value.
	 * @since 2.0
	 */
	int backlogSize();


	/**
	 * Inspect if the dispatcher supports ordered dispatching:
	 * Single threaded dispatchers naturally preserve event ordering on dispatch.
	 * Multi threaded dispatchers can't prevent a single consumer to receives concurrent notifications.
	 *
	 * @return true if ordering of dispatch is preserved.
	 * * @since 2.0
	 */
	boolean supportsOrdering();


	/**
	 * A dispatcher context can be bound to the thread(s) it runs on. This method allows any caller to detect if he is
	 * actually within this dispatcher scope.
	 *
	 * @return true if within Dispatcher scope (e.g. a thread).
	 * * @since 2.0
	 */
	boolean inContext();


}
