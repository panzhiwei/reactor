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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.event.dispatch.Dispatcher;
import reactor.event.registry.Registration;
import reactor.function.Consumer;
import reactor.rx.subscription.PushSubscription;
import reactor.rx.subscription.support.WrappedSubscription;
import reactor.timer.Timer;
import reactor.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * @author Stephane Maldini
 * @since 2.0
 */
public class ThrottleAction<T> extends Action<T, T> {

	private final Timer timer;
	private final long  period;
	private final Consumer<Long> periodTask = new Consumer<Long>() {
		@Override
		public void accept(Long aLong) {
			if (upstreamSubscription != null) {
				dispatch(-1l, upstreamSubscription);
			}
		}
	};

	protected final Consumer<Long> throttledConsumer = new Consumer<Long>() {
		@Override
		public void accept(Long n) {
			upstreamSubscription.updatePendingRequests(n);
		}
	};

	private final long delay;

	private Registration<? extends Consumer<Long>> timeoutRegistration;

	@SuppressWarnings("unchecked")
	public ThrottleAction(Dispatcher dispatcher,
	                      Timer timer, long period, long delay) {
		super(dispatcher, 1);
		Assert.state(timer != null, "Timer must be supplied");
		this.timer = timer;
		this.period = period;
		this.delay = delay;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected PushSubscription<T> createTrackingSubscription(Subscription subscription) {
		return new TimeoutTracker<>(subscription, this);
	}

	@Override
	protected void doSubscribe(Subscription subscription) {
		super.doSubscribe(subscription);
		timeoutRegistration = timer.schedule(periodTask, period, TimeUnit.MILLISECONDS, delay);
	}

	@Override
	protected void doNext(T ev) {
		broadcastNext(ev);
	}

	@Override
	protected void onRequest(long n) {
		trySyncDispatch(n, throttledConsumer);
	}

	@Override
	public Action<T, T> cancel() {
		timeoutRegistration.cancel();
		return super.cancel();
	}

	@Override
	public Action<T, T> pause() {
		timeoutRegistration.pause();
		return super.pause();
	}

	@Override
	public Action<T, T> resume() {
		timeoutRegistration.resume();
		return super.resume();
	}

	@Override
	public void doComplete() {
		timeoutRegistration.cancel();
		super.doComplete();
	}

	static class TimeoutTracker<T> extends WrappedSubscription<T> {

		public TimeoutTracker(Subscription subscription, Subscriber<T> subscriber) {
			super(subscription, subscriber);
		}

		@Override
		public void request(long n) {
			if(n == -1){
				super.request(1l);
			} else {
				super.request(n);
			}
		}

		@Override
		public long clearPendingRequest() {
			return -1l;
		}
	}
}
