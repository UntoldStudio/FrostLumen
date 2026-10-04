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
package top.untold.frostlumen.core.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceReader {
    public static String readString(String path) throws IOException {
        try (InputStream stream = ResourceReader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new FileNotFoundException("Resource not file:" + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    public static byte[] readBytes(String path) throws IOException {
        try (InputStream stream = ResourceReader.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return stream.readAllBytes();
        }
    }
}
