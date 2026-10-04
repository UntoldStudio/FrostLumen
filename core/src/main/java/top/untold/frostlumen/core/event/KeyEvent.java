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
package top.untold.frostlumen.core.event;

import top.untold.frostlumen.core.data.InputAction;
import top.untold.frostlumen.core.data.InputModifiers;
import top.untold.frostlumen.core.data.Key;

public class KeyEvent extends CancelableEvent {
    private final Key key;
    private final InputAction inputAction;
    private final InputModifiers inputModifiers;

    public KeyEvent(Key key, InputAction action, InputModifiers modifiers) {
        this.key = key;
        this.inputAction = action;
        this.inputModifiers = modifiers;
    }

    public Key getKey() {
        return key;
    }
    public InputAction getInputAction() {
        return inputAction;
    }
    public InputModifiers getInputModifiers() {
        return inputModifiers;
    }
}
