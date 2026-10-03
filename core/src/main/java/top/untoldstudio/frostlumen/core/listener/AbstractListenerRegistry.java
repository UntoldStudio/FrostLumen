/*
 * Copyright 2026 Untold Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.untoldstudio.frostlumen.core.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class AbstractListenerRegistry<T> {
    protected final List<T> listeners = new CopyOnWriteArrayList<>();

    public void register(T listener){
        listeners.add(listener);
    }
    public void unregister(T listener) {
        listeners.remove(listener);
    }

    protected void forListeners(Consumer<T> consumer){
        for (T listener : listeners) {
            consumer.accept(listener);
        }
    }
}
