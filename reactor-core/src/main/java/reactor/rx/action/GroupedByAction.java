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
package reactor.rx.action;

import reactor.event.dispatch.Dispatcher;

/**
 * An {@link java.lang.Iterable} wrapper that takes care of lazy subscribing.
 * The Stream will complete or fail whever the parent groupBy action terminates itself or when broadcastXXX is called.
 *
 * Create such stream with the provided factory, E.g.:
 * {@code
 * stream.groupBy(data -> data.id).consume(partitionedStream -> partitionedStream.consume())
 * }
 *
 * @author Stephane Maldini
 */
public final class GroupedByAction<K, T> extends Action<T, T> {
	private final K key;

	public GroupedByAction(K key, Dispatcher dispatcher) {
		super(dispatcher);
		this.key = key;
	}

	public K key() {
		return key;
	}

	@Override
	protected void doNext(T ev) {
		broadcastNext(ev);
	}

	@Override
	public String toString() {
		return super.toString()+"{" +
				"key=" + key +
				'}';
	}
}
