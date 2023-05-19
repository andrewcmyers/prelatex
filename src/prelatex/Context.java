package prelatex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A namespace that supports pushing and popping other namespaces.
 *  The current namespace can be either immutable or mutable. The initial
 *  state is a single-level mutable namespace to which new bindings can be
 *  added.
 */
public class Context<T> implements Namespace<T> {
	
	interface Node<T> {
		T get(String name) throws LookupFailure;
		void put(String name, T value);
	}
	
	static class FixedNode<T> implements Node<T> {
		Namespace<T> fixed_mappings; // may be null

		public FixedNode(Namespace<T> n) {
			fixed_mappings = n;
		}

		@Override
		public T get(String name) throws LookupFailure {
			return fixed_mappings.lookup(name);
		}

		@Override
		public void put(String name, T value) {
			throw new UnsupportedOperationException();
		}
	}
	
	static class MutableNode<T> implements Node<T> {
		Map<String, T> mappings = new HashMap<>();

		@Override
		public T get(String name) throws LookupFailure {
			if (!mappings.containsKey(name))
				throw lookupFailed;
			return mappings.get(name);
		}

		@Override
		public void put(String name, T value) {
			mappings.put(name, value);			
		}
	}

	private List<Node<T>> nodes;
	{
		nodes = new ArrayList<>();
		nodes.add(new MutableNode<>());
	}

	static public LookupFailure lookupFailed = new LookupFailure();
	
	public T lookup(String name) throws LookupFailure {
		for (int i = nodes.size() - 1; i >= 0; i--) {
			Node<T> n = nodes.get(i);
			try {
				return n.get(name);
			} catch (LookupFailure e) {
				// try the next node up the stack
			}
		}
		throw lookupFailed;		
	}

	public void push() {
		nodes.add(new MutableNode<>());
	}
	
	public void push(Namespace<T> n) {
		nodes.add(new FixedNode<T>(n));
	}

	public void pop() {
		nodes.remove(nodes.size()-1);
	}
	
    // defn may be null
	public void add(String name, T defn) {
		if (defn == null) {
			System.err.println("Warning: " + name + " bound to null");
			return;
		}
		nodes.get(nodes.size()-1).put(name,  defn);
	}

	public int depth() {
		return nodes.size() - 1;
	}
}
