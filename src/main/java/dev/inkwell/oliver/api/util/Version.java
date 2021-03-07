/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.inkwell.oliver.api.util;

import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.NotNull;

public final class Version implements Comparable<Version> {
    private final int major, minor, patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static Version parse(String string) throws VersionParsingException {
        try {
            String[] split = string.split("\\.");

            return new Version(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        } catch (Exception e) {
            throw new VersionParsingException(e);
        }
    }

    @Override
    public int compareTo(@NotNull Version o) {
        return this.major != o.major
                ? Integer.compare(this.major, o.major)
                : this.minor != o.minor
                ? Integer.compare(this.minor, o.minor)
                : Integer.compare(this.patch, o.patch);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", this.major, this.minor, this.patch);
    }

    public int getVersionComponent(int i) {
        if (i < 0 || i > 2) {
            throw new RuntimeException();
        }

        return i == 0 ? major : i == 1 ? minor : patch;
    }
}
