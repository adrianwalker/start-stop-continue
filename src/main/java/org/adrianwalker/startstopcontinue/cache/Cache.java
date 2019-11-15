package org.adrianwalker.startstopcontinue.cache;

import java.util.function.Function;

public interface Cache<T, R> {

  R readThrough(final T key, final Function<T, R> f);

}
