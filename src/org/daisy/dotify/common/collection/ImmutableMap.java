package org.daisy.dotify.common.collection;

import java.util.Map;

/**
 * An immutable map, i.e. a map of which the content is guaranteed not to change.
 *
 * All the "destructive" methods ({@link Map#put}, {@link Map#remove}, {@link Map#clear}, etc.) in
 * this class will throw an UnsupportedOperationException.
 *
 * Instances of this class that are derived from each other are implemented via a common persistent
 * data structure, i.e. a data structure that preserves previous versions of itself.
 */
public final class ImmutableMap<K,V> extends PersistentMap.View<K,V> implements Map<K,V> {

	@SuppressWarnings("rawtypes")
	private static final ImmutableMap EMPTY = new ImmutableMap<>(new PersistentMap<>());

	/**
	 * Get an empty instance of ImmutableList.
	 */
	public static <K,V> ImmutableMap<K,V> empty() {
		return (ImmutableMap<K,V>)EMPTY;
	}

	private ImmutableMap(PersistentMap<K,V> map) {
		super(map, true);
	}

	/* ----------------- */
	/* Builder (mutable) */
	/* ----------------- */

	private ImmutableMap(Builder<K,V> from) {
		super(from, true);
	}

	/**
	 * Get a ImmutableMap.Builder with the same initial contents as this map, in other words, a
	 * mutable version of this map.
	 */
	public Builder<K,V> builder() {
		return new Builder<K,V>(this);
	}

	/**
	 * A builder of an ImmutableMap. Does not support removing elements.
	 */
	// not extending ImmutableMap so that Builder can not be accidentally passed when immutable object is expected
	public final static class Builder<K,V> extends PersistentMap.View<K,V> implements Map<K,V> {

		private Builder(ImmutableMap<K,V> from) {
			super(from, false);
		}

		/**
		 * Build an immutable version of this map.
		 */
		// FIXME: set readonly to true (permanently) and cast instead of creating new object?
		// -> one object creation less
		// -> Builder needs to have a clone method then
		public ImmutableMap<K,V> build() {
			// if (readonly)
			// 	throw new UnsupportedOperationException("Already built"); // or create new ImmutableMap
			// readonly = true;
			// return (ImmutableMap<K,V>)this;
			return new ImmutableMap<K,V>(this);
		}

		// FIXME: return Builder? (and don't implement Map?)
		// @Override
		// public V put(K key, V value) {
		// 	if (readonly)
		// 		throw new UnsupportedOperationException("Already built"); // or create new Builder
		// 	super.put(key, value);
		// 	return this;
		// }

		// FIXME: return Builder? (and don't implement Map?)
		// @Override
		// public V remove(Object key) {
		// 	if (readonly)
		// 		throw new UnsupportedOperationException("Already built"); // or create new Builder
		// 	super.remove(key);
		// 	return this;
		// }

		// FIXME: return Builder? (and don't implement Map?)
		// @Override
		// public void putAll(Map<? extends K,? extends V> m) {
		// 	if (readonly)
		// 		throw new UnsupportedOperationException("Already built"); // or create new Builder
		// 	super.putAll(m);
		// 	return this;
		// }

		// FIXME: return Builder? (and don't implement Map?)
		// @Override
		// public void clear() {
		// 	if (readonly)
		// 		throw new UnsupportedOperationException("Already built"); // or create new Builder
		// 	super.clear();
		// 	return this;
		// }
	}

	/* ----------------------------------- */
	/* Static versions of mutating methods */
	/* ----------------------------------- */

	private ImmutableMap(ImmutableMap<K,V> from, K key, V value) {
		super(from, false);
		put(key, value);
		readonly = true;
	}

	/**
	 * Non-destructive version of {@link Map#put}. Instead of returning the previous value, this
	 * method will return a new Map object with the given value associated with the given key.
	 */
	public static <K,V> ImmutableMap<K,V> put(ImmutableMap<K,V> map, K key, V value) {
		// return map.builder().put(key, value).build();
		return new ImmutableMap<K,V>(map, key, value);
	}
}
