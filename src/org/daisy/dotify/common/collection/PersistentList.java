package org.daisy.dotify.common.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
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
	 * @see java.util.List#listIterator
	 */
	private ListIterator<E> listIterator(Version version) {
		// FIXME: cache values?
		return new ListIterator<E>() {
			private int i = -1;
			private final int size = size(version);
			public boolean hasNext() {
				return i < size - 1;
			}
			public boolean hasPrevious() {
				return i > 0;
			}
			public int nextIndex() {
				return i + 1;
			}
			public int previousIndex() {
				return i == -1 ? -1 : i - 1;
			}
			public E next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return get(version, ++i);
			}
			public E previous() {
				if (!hasPrevious())
					throw new NoSuchElementException();
				return get(version, --i);
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
			public void set(E e) {
				throw new UnsupportedOperationException();
			}
			public void add(E e) {
				throw new UnsupportedOperationException();
			}
		};
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

	static class View<E> implements Cloneable { // must be static otherwise can not be extended by other classes

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

		/* ----------------- */
		/* Read-only methods */
		/* ----------------- */

		public int size() {
			return list.size(version);
		}

		public boolean isEmpty() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean contains(Object o) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public Iterator<E> iterator() {
			return listIterator();
		}

		public Object[] toArray() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public <E> E[] toArray(E[] a) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean containsAll(Collection<?> c) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public E get(int index) {
			return list.get(version, index);
		}

		public int indexOf(Object o) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public int lastIndexOf(Object o) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public ListIterator<E> listIterator() {
			return list.listIterator(version);
		}

		public ListIterator<E> listIterator(int index) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		// FIXME: check for co-modification?
		public List<E> subList(int fromIndex, int toIndex) {
			if (fromIndex < 0)
				throw new IndexOutOfBoundsException();
			if (toIndex > list.size(version))
				throw new IndexOutOfBoundsException();
			if (fromIndex > toIndex)
				throw new IllegalArgumentException();
			return new AbstractList<E>() {
				public int size() {
					return toIndex - fromIndex;
				}
				public E get(int i) {
					return View.this.get(fromIndex + i);
				}
				public E set(int i, E e) {
					return View.this.set(fromIndex + i, e);
				}
			};
		}

		/* ---------------- */
		/* Mutating methods */
		/* ---------------- */

		public boolean add(E e) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			version = list.add(version, e);
			return true;
		}

		public boolean remove(Object o) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean addAll(Collection<? extends E> c) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean addAll(int index, Collection<? extends E> c) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean removeAll(Collection<?> c) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public boolean retainAll(Collection<?> c) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public void clear() {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
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
			throw new UnsupportedOperationException("Not implemented yet");
		}

		public E remove(int index) {
			if (readonly)
				throw new UnsupportedOperationException("Read-only");
			throw new UnsupportedOperationException("Not implemented yet");
		}

		/* ----------------------------------------- */

		// taken from AbstractList

		@Override
		public int hashCode() {
			int hashCode = 1;
			Iterator<E> e = iterator();
			while (e.hasNext()) {
				E o = e.next();
				hashCode = 31*hashCode + (o==null ? 0 : o.hashCode());
			}
			return hashCode;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof List))
				return false;
			ListIterator<E> e1 = listIterator();
			ListIterator e2 = ((List)o).listIterator(); // FIXME: implement listIterator
			while (e1.hasNext() && e2.hasNext()) {
				E o1 = e1.next();
				Object o2 = e2.next();
				if (!(o1==null ? o2==null : o1.equals(o2)))
					return false;
			}
			return !(e1.hasNext() || e2.hasNext());
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
