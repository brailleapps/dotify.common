package org.daisy.dotify.common.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

class PersistentMap<K,V> {

	private Map<K,Map<Version,Optional<V>>> data = new HashMap<>(); // TreeMap

	/**
	 * @see java.util.Map#get
	 */
	private V get(Version version, Object key) {
		Map<Version,Optional<V>> changes = data.get(key);
		if (changes == null)
			return null;
		while (version != null) {
			Optional<V> val = changes.get(version);
			if (val != null)
				return val.orElse(null);
			version = version.parent;
			// changes = changes.headMap(version, true); // would this actually speed things up?
		}
		return null;
	}

	// public int size(Version version);

	// public Set<Entry<K,V>> entrySet(Version version);

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
		version = new Version(version);
		changes.put(version, Optional.ofNullable(val));
		return version;
	}

	// public Version remove(Version version, K key) { return put(version, key, null); }

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

	static class View<K,V> { // must be static otherwise can not be extended by other classes

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

		/* ----------------- */
		/* Read-only methods */
		/* ----------------- */

		public int size() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean isEmpty() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean containsKey(Object key) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean containsValue(Object value) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public V get(Object key) {
			return map.get(version, key);
		}

		public Set<K> keySet() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public Collection<V> values() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public Set<Map.Entry<K,V>> entrySet() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		/* ---------------- */
		/* Mutating methods */
		/* ---------------- */

		public V put(K key, V value) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			// FIXME: modify version instead of creating new version if it has no children and no other view is based on it
			V prev = map.get(version, key);
			version = map.put(version, key, value); // FIXME: how to combine get and put?
			return prev;
		}

		public V remove(Object key) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public void putAll(Map<? extends K,? extends V> m) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public void clear() {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		/* ----------------------------------------- */
	
		// taken from AbstractMap

		@Override
		public int hashCode() {
			int h = 0;
			Iterator<Map.Entry<K,V>> i = entrySet().iterator();
			while (i.hasNext())
				h += i.next().hashCode();
			return h;
		}
	
		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Map))
				return false;
			@SuppressWarnings("unchecked")
			Map<K,V> t = (Map<K,V>)o;
			if (t.size() != size())
				return false;
			try {
				Iterator<Map.Entry<K,V>> i = entrySet().iterator(); // FIXME: implement entrySet
				while (i.hasNext()) {
					Map.Entry<K,V> e = i.next();
					K key = e.getKey();
					V value = e.getValue();
					if (value == null) {
						if (!(t.get(key)==null && t.containsKey(key)))
							return false;
					} else {
						if (!value.equals(t.get(key)))
							return false;
					}
				}
			} catch(ClassCastException unused) {
				return false;
			} catch(NullPointerException unused) {
				return false;
			}
			return true;
		}
	}
}
