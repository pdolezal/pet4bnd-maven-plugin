/*
 * Copyright 2016 Yetamine
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.yetamine.pet4bnd.testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.yetamine.pet4bnd.model.Persistable;

/**
 * Utilities for output testing.
 */
public class TestOutput {

    /**
     * Persists an object to a byte array.
     *
     * @param persistable
     *            the object to persist. It must not be {@code null}.
     *
     * @return the persisted data
     *
     * @throws IOException
     *             if the output could not be written
     */
    public static byte[] toBytes(Persistable persistable) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        persistable.persist(result);
        return result.toByteArray();
    }

    private TestOutput() {
        throw new AssertionError();
    }
}
