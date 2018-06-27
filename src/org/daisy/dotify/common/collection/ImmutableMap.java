package org.daisy.dotify.common.collection;

import java.util.Map;

public final class ImmutableMap<K,V> extends PersistentMap.View<K,V> implements Map<K,V> {

	public static <K,V> ImmutableMap<K,V> empty() {
		return new ImmutableMap<K,V>(new PersistentMap<K,V>());
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

	public Builder<K,V> builder() {
		return new Builder<K,V>(this);
	}

	// not extending ImmutableMap so that Builder can not be accidentally passed when immutable object is expected
	public final static class Builder<K,V> extends PersistentMap.View<K,V> implements Map<K,V> {

		private Builder(ImmutableMap<K,V> from) {
			super(from, false);
		}

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

	public static <K,V> ImmutableMap<K,V> put(ImmutableMap<K,V> map, K key, V value) {
		// return map.builder().put(key, value).build();
		return new ImmutableMap<K,V>(map, key, value);
	}
}
