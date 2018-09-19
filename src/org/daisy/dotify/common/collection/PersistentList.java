package org.daisy.dotify.common.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

class PersistentList<E> {

	// FIXME: can be simplified if set() is not needed

	private List<Map<Version,Optional<E>>> data = new ArrayList<>(); // LinkedList
	private Map<Version,Integer> size = new TreeMap<>();

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
	 * @see java.util.List#get
	 */
	private E get(Version version, int index) {
		Map<Version,Optional<E>> changes = data.get(index);
		while (version != null) {
			Optional<E> val = changes.get(version);
			if (val != null)
				return val.orElse(null);
			version = version.parent;
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * @see java.util.List#add
	 */
	private Version add(Version version, E e) {
		return set(version, size(version), e); // FIXME: don't call size() twice
	}

	/**
	 * @see java.util.List#set
	 */
	private Version set(Version version, int i, E e) {
		int s = size(version);
		if (s > data.size())
			throw new RuntimeException("coding error");
		if (i > s)
			throw new IndexOutOfBoundsException();
		version = new Version(version);
		Map<Version,Optional<E>> changes;
		if (i < data.size())
			changes = data.get(i);
		else {
			changes = new TreeMap<>();
			data.add(changes);
		}
		if (i == s)
			size.put(version, s + 1);
		changes.put(version, Optional.ofNullable(e));
		return version;
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

	static class View<E> extends AbstractList<E> implements Cloneable { // must be static otherwise can not be extended by other classes

		private final PersistentList<E> list; // = PersistentList.this;
		private PersistentList<E>.Version version;
		protected boolean readonly;

		protected View(PersistentList<E> list, boolean readonly) {
			this.list = list;
			this.version = null;
			this.readonly = readonly;
		}

		protected View(View<E> from, boolean readonly) {
			this.list = from.list;
			this.version = from.version;
			this.readonly = readonly;
		}

		// @Override
		// protected void finalize() throws Throwable { ... }

		public int size() {
			return list.size(version);
		}

		public E get(int index) {
			return list.get(version, index);
		}

		public E set(int index, E element) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			E prev = list.get(version, index);
			version = list.set(version, index, element);
			return prev;
		}

		public void add(int index, E element) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			if (index == size())
				version = list.add(version, element);
			else
				throw new UnsupportedOperationException();
		}

		public E remove(int index) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException();
		}

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError("coding error");
			}
		}
	}
}
