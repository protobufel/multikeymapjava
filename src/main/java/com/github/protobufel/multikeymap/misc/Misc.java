/*
 *    Copyright 2017 David Tesler
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.github.protobufel.multikeymap.misc;

import java.util.Optional;
import java.util.function.Supplier;

final class Misc {
    private Misc() {
    }

    @FunctionalInterface
    public interface ChainedSupplier<T> extends Supplier<Optional<Chain<T, ?>>> {
        static <T1> Optional<T1> value(final ChainedSupplier<T1> source) {
            return source.get().flatMap(chain -> Optional.ofNullable(chain.value()));
        }

        @Override
        Optional<Chain<T, ?>> get();
    }

    public interface Chain<T1, T2> extends ChainedSupplier<T2> {
        @Override
        Optional<Chain<T2, ?>> get();

        T1 value();

        default ChainedSupplier<T2> supplier() {
            return this::get;
        }
    }
}
