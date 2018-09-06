package org.snowjak.rays.frontend.messages.backend.commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author snowjak88
 *
 * @param <C>
 *            an object containing the context upon which this command operates
 * @param <V>
 *            denotes the kind of return-value this command yields which can be
 *            passed on to the next chained command. {@link Void} if no
 *            return-value possible.
 */
public abstract class AbstractChainableCommand<C, V> {
	
	private AbstractChainableCommand<?, ?> nextInChain = null;
	private C context;
	
	public static AbstractChainableCommand<?, ?> chain(AbstractChainableCommand<?, ?>... commands) {
		
		final var list = new LinkedList<AbstractChainableCommand<?, ?>>();
		list.addAll(Arrays.asList(commands));
		return chain(list);
	}
	
	public static AbstractChainableCommand<?, ?> chain(List<AbstractChainableCommand<?, ?>> commands) {
		
		if (commands == null)
			return null;
		if (commands.isEmpty())
			return null;
		
		final var currentCommand = commands.remove(0);
		currentCommand.nextInChain = chain(commands);
		
		return currentCommand;
	}
	
	public AbstractChainableCommand() {
		
		this(null, null);
	}
	
	public AbstractChainableCommand(C context) {
		
		this(context, null);
	}
	
	public AbstractChainableCommand(C context, AbstractChainableCommand<V, ?> nextInChain) {
		
		this.context = context;
		this.nextInChain = nextInChain;
	}
	
	public void setContext(C context) {
		
		this.context = context;
	}
	
	public C getContext() {
		
		return context;
	}
	
	public boolean hasNextInChain() {
		
		return (nextInChain != null);
	}
	
	public void setNextInChain(AbstractChainableCommand<V, ?> nextInChain) {
		
		this.nextInChain = nextInChain;
	}
	
	@SuppressWarnings("unchecked")
	public AbstractChainableCommand<V, ?> getNextInChain() {
		
		return (AbstractChainableCommand<V, ?>) nextInChain;
	}
}
