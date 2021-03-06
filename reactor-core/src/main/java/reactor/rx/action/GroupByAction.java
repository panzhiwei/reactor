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
import reactor.function.Consumer;
import reactor.function.Function;
import reactor.rx.Stream;
import reactor.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage a dynamic registry of substreams for a given key extracted from the incoming data. Each non-existing key
 * will result in a new stream to be signaled
 *
 * @param <T>
 * @param <K>
 * @since 2.0
 */
public class GroupByAction<T, K> extends Action<T, Stream<T>> {

	private final Function<? super T, ? extends K> fn;
	private final Map<K, GroupedByAction<K, T>> groupByMap = new HashMap<K, GroupedByAction<K, T>>();

	public GroupByAction(Function<? super T, ? extends K> fn, Dispatcher dispatcher) {
		super(dispatcher);
		Assert.notNull(fn, "Key mapping function cannot be null.");
		this.fn = fn;
	}

	public Map<K, ? extends Stream<T>> groupByMap() {
		return groupByMap;
	}

	public Action<T, Stream<T>> cancel(K key) {
		dispatch(key, new Consumer<K>() {
			@Override
			public void accept(K k) {
				Action<T, T> s = groupByMap.remove(k);
				if (s != null) {
					s.cancel();
				}
			}
		});
		return this;
	}

	public Action<T, Stream<T>> complete(K key) {
		dispatch(key, new Consumer<K>() {
			@Override
			public void accept(K k) {
				Action<T, T> s = groupByMap.remove(k);
				if (s != null) {
					s.broadcastComplete();
				}
			}
		});
		return this;
	}

	@Override
	protected void doNext(T value) {
		final K key = fn.apply(value);
		GroupedByAction<K, T> stream = groupByMap.get(key);
		if (stream == null) {
			stream = new GroupedByAction<K, T>(key, dispatcher);
			stream.capacity(capacity).env(environment);
			groupByMap.put(key, stream);
			broadcastNext(stream.onOverflowBuffer());
		}
		stream.broadcastNext(value);
	}

	@Override
	protected void doError(Throwable ev) {
		super.doError(ev);
		for (Action<T, T> stream : groupByMap.values()) {
			stream.broadcastError(ev);
		}
	}

	@Override
	protected void doComplete() {
		super.doComplete();
		for (Action<T, T> stream : groupByMap.values()) {
			stream.broadcastComplete();
		}
		groupByMap.clear();
	}

}
