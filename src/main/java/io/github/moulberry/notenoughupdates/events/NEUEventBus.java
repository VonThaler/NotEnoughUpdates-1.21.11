/*
 * Copyright (C) 2026 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NEUEventBus {
	public static final NEUEventBus INSTANCE = new NEUEventBus();
	private final Map<Class<?>, List<Consumer<NEUEvent>>> listeners = new ConcurrentHashMap<>();

	public <T extends NEUEvent> void subscribe(Class<T> eventClass, Consumer<T> listener) {
		listeners.computeIfAbsent(eventClass, k -> new ArrayList<>())
						 .add(e -> listener.accept(eventClass.cast(e)));
	}

	public void post(NEUEvent event) {
		List<Consumer<NEUEvent>> eventListeners = listeners.get(event.getClass());
		if (eventListeners != null) {
			for (Consumer<NEUEvent> listener : eventListeners) {
				listener.accept(event);
			}
		}
	}
}
