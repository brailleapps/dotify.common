package org.daisy.dotify.common.splitter;

import java.util.List;

/**
 * Provides a default splitter result.
 * @author Joel Håkansson
 *
 * @param <T> the type of units
 */
public class DefaultSplitResult<T extends SplitPointUnit,U extends SplitPointDataSource<T,U>> implements SplitResult<T,U> {
	private final List<T> head;
	private final SplitPointDataSource<T,U> tail;
	
	/**
	 * Creates a new result.
	 * @param head the head of the result
	 * @param tail the tail of the result
	 */
	public DefaultSplitResult(List<T> head, SplitPointDataSource<T,U> tail) {
		this.head = head;
		this.tail = tail;
	}
	
	@Override
	public List<T> head() {
		return head;
	}

	@Override
	public SplitPointDataSource<T,U> tail() {
		return tail;
	}
}
