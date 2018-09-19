package org.daisy.dotify.common.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

class PersistentMap<K,V> {

	private Map<K,Map<Version,Optional<V>>> data = new HashMap<>(); // TreeMap
	private Map<Version,Integer> size = new TreeMap<>();

	private Optional<V> get(Map<Version,Optional<V>> changes, Version version) {
		if (changes == null)
			return null;
		while (version != null) {
			Optional<V> val = changes.get(version);
			if (val != null)
				return val;
			version = version.parent;
			// changes = changes.headMap(version, true); // would this actually speed things up?
		}
		return null;
	}

	/**
	 * @see java.util.Map#get
	 */
	private V get(Version version, Object key) {
		if (version == null)
			return null;
		Optional<V> val = get(data.get(key), version);
		if (val != null)
			return val.orElse(null);
		return null;
	}

	/**
	 * @see java.util.List#size
	 */
	private int size(Version version) {
		while (version != null) {
			Integer i = size.get(version);
			if (i != null)
				return i;
			version = version.parent;
		}
		return 0;
	}
	
	/**
	 * @see java.util.Map#containsKey
	 */
	private boolean containsKey(Version version, Object key) {
		if (version == null)
			return false;
		return get(data.get(key), version) != null;
	}

	// FIXME: only update if val != changes.get(version) ?
	/**
	 * @see java.util.Map#put
	 */
	private Version put(Version version, K key, V val) {
		Map<Version,Optional<V>> changes = data.get(key);
		if (changes == null) {
			changes = new TreeMap<>();
			data.put(key, changes);
		}
		Version newVersion = new Version(version);
		if (changes.isEmpty()) {
			size.put(newVersion, size(version) + 1);
		}
		changes.put(newVersion, Optional.ofNullable(val));
		return newVersion;
	}

	/**
	 * @see java.util.Map#entrySet
	 */
	private Set<Map.Entry<K,V>> entrySet(Version version) {
		return new AbstractSet<Map.Entry<K,V>>() {
			public Iterator<Map.Entry<K,V>> iterator() {
				return new Iterator<Map.Entry<K,V>>() {
					private Iterator<Map.Entry<K,Map<Version,Optional<V>>>> iterator = PersistentMap.this.data.entrySet().iterator();
					private Optional<Map.Entry<K,V>> next = null;
					private void computeNext() {
						while (next == null) {
							if (version == null) {
								next = Optional.empty();
							}
							try {
								Map.Entry<K,Map<Version,Optional<V>>> entry = iterator.next();
								Optional<V> val = get(entry.getValue(), version);
								if (val != null) {
									next = Optional.of(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), val.orElse(null)));
								}
							} catch (NoSuchElementException e) {
								next = Optional.empty();
							}
						}
					}
					public boolean hasNext() {
						computeNext();
						return next.isPresent();
					}
					public Entry<K,V> next() {
						computeNext();
						if (next.isPresent())
							return next.get();
						else
							throw new NoSuchElementException();
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
			public int size() {
				return PersistentMap.this.size(version);
			}
		};
	}

	private int versions = 0;

	private class Version implements Comparable<Version> {

		private final Version parent;
		private final Integer i;

		Version(Version parent) {
			this.parent = parent;
			this.i = versions++;
		}

		@Override
		public int compareTo(Version that) {
			if (that == null)
				throw new NullPointerException();
			return this.i.compareTo(that.i);
		}
	}

	static class View<K,V> extends AbstractMap<K,V> { // must be static otherwise can not be extended by other classes

		private final PersistentMap<K,V> map; // = PersistentMap.this;
		private PersistentMap<K,V>.Version version;
		protected boolean readonly;

		protected View(PersistentMap<K,V> map, boolean readonly) {
			this.map = map;
			this.version = null;
			this.readonly = readonly;
		}

		protected View(View<K,V> from, boolean readonly) {
			this.map = from.map;
			this.version = from.version;
			this.readonly = readonly;
		}

		// @Override
		// protected void finalize() throws Throwable { ... }

		@Override
		public int size() {
			return map.size(version);
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(version, key);
		}

		@Override
		public V get(Object key) {
			return map.get(version, key);
		}

		public Set<Map.Entry<K,V>> entrySet() {
			return map.entrySet(version);
		}

		public V put(K key, V value) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			// FIXME: modify version instead of creating new version if it has no children and no other view is based on it
			V prev = map.get(version, key);
			version = map.put(version, key, value); // FIXME: how to combine get and put?
			return prev;
		}
		
		@Override
		public V remove(Object key) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException();
		}
	}
}
