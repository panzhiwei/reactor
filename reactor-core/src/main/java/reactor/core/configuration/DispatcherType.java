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

package reactor.core.configuration;

import reactor.core.dynamic.annotation.Dispatcher;
import reactor.jarjar.com.lmax.disruptor.RingBuffer;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * An enumeration of supported types of {@link Dispatcher}.
 *
 * @author Andy Wilkinson
 * @author Stephane Maldini
 */
public enum DispatcherType {

	/**
	 * A grouped {@link Dispatcher}
	 */
	RING_BUFFER_GROUP,

	/**
	 * A {@link Dispatcher} which uses a {@link RingBuffer} for dispatching
	 */
	RING_BUFFER,

	/**
	 * A {@link Dispatcher} which uses the current thread for dispatching
	 */
	SYNCHRONOUS,

	/**
	 * A {@link Dispatcher} which uses a {@link ThreadPoolExecutor} for dispatching
	 */
	THREAD_POOL_EXECUTOR,

	/**
	 * A {@link Dispatcher} which uses a multi-threaded {@literal RingBuffer} for dispatching
	 */
	WORK_QUEUE

}
