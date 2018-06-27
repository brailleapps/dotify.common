package org.daisy.dotify.common.collection;

import java.util.List;

public class ImmutableList<E> extends PersistentList.View<E> implements List<E> {

	public static <E> ImmutableList<E> empty() {
		return new ImmutableList<E>();
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

	public Builder<E> builder() {
		return new Builder<E>(this);
	}

	// not extending ImmutableList so that Builder can not be accidentally passed when immutable object is expected
	public static class Builder<E> extends PersistentList.View<E> implements List<E>, Cloneable {

		private Builder(ImmutableList<E> from) {
			super(from, false);
		}

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

	public static <E> ImmutableList<E> add(ImmutableList<E> list, E e) {
		// return list.builder().add(e).build();
		return new ImmutableList<E>(list, e);
	}
}
