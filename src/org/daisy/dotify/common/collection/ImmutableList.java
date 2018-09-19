package org.daisy.dotify.common.collection;

import java.util.List;

/**
 * An immutable list, i.e. a list of which the content is guaranteed not to change.
 *
 * All the "destructive" methods ({@link List#set}, {@link java.util.Collection#add}, {@link
 * java.util.Collection#remove}, etc.) in this class will throw an UnsupportedOperationException.
 *
 * Instances of this class that are derived from each other are implemented via a common persistent
 * data structure, i.e. a data structure that preserves previous versions of itself.
 */
public class ImmutableList<E> extends PersistentList.View<E> implements List<E> {

	@SuppressWarnings("rawtypes")
	private static final ImmutableList EMPTY = new ImmutableList<>();

	/**
	 * Get an empty instance of ImmutableList.
	 */
	public static <E> ImmutableList<E> empty() {
		return (ImmutableList<E>)EMPTY;
	}

	private ImmutableList() {
		super(new PersistentList<E>(), true);
	}

	/* ----------------- */
	/* Builder (mutable) */
	/* ----------------- */

	private ImmutableList(Builder<E> from) {
		super(from, true);
	}

	/**
	 * Get a ImmutableList.Builder with the same initial contents as this list, in other words, a
	 * mutable version of this list.
	 */
	public Builder<E> builder() {
		return new Builder<E>(this);
	}

	/**
	 * A builder of an ImmutableList. Does not support removing elements, or adding elements at
	 * other positions than at the end of the list.
	 */
	// not extending ImmutableList so that Builder can not be accidentally passed when immutable object is expected
	public static class Builder<E> extends PersistentList.View<E> implements List<E>, Cloneable {

		private Builder(ImmutableList<E> from) {
			super(from, false);
		}

		/**
		 * Build an immutable version of this list.
		 */
		public ImmutableList<E> build() {
			return new ImmutableList<E>(this);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E> clone() {
			return (Builder<E>)super.clone();
		}
	}

	/* ----------------------------------- */
	/* Static versions of mutating methods */
	/* ----------------------------------- */

	private ImmutableList(ImmutableList<E> from, E e) {
		super(from, false);
		add(e);
		readonly = true;
	}

	/**
	 * Non-destructive version of {@link java.util.Collection#add}. Instead of returning a boolean
	 * value (which is always true), this method will return a new List object with the given
	 * element added.
	 */
	public static <E> ImmutableList<E> add(ImmutableList<E> list, E e) {
		// return list.builder().add(e).build();
		return new ImmutableList<E>(list, e);
	}
}
