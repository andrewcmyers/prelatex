package prelatex;

import java.util.*;

/** A namespace that supports pushing and popping other namespaces.
 *  The current namespace can be either immutable or mutable. The initial
 *  state is a single-level mutable namespace to which new bindings can be
 *  added.
 */
public class Context<T> implements Namespace<T> {


    interface Node<T> {
		T get(String name) throws LookupFailure;
		void put(String name, T value);

		Iterable<String> keys();
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

		@Override
		public Iterable<String> keys() {
			return mappings.keySet();
		}
	}

	private final List<Node<T>> nodes;
	{
		nodes = new ArrayList<>();
		nodes.add(new MutableNode<>());
	}

	static public LookupFailure lookupFailed = new LookupFailure();
	
	public T lookup(String key) throws LookupFailure {
		for (int i = nodes.size() - 1; i >= 0; i--) {
			Node<T> n = nodes.get(i);
			try {
				return n.get(key);
			} catch (LookupFailure e) {
				// try the next node up the stack
			}
		}
		throw lookupFailed;		
	}

	@Override
	public Iterable<String> keys() {
		Set<String> result = new HashSet<>();
		for (Node<T> node : nodes) {
			for (String k : node.keys()) result.add(k);
		}
		return result;
	}

	public void push() {
		nodes.add(new MutableNode<>());
	}
	
	public void pop() {
		nodes.remove(nodes.size()-1);
	}
	
	public void add(String name, T definition) {
		assert definition != null;
		nodes.get(nodes.size() - 1).put(name,  definition);
	}

	public void setGlobal(String name, T m) {
		nodes.get(0).put(name, m);
	}

	public int depth() {
		return nodes.size() - 1;
	}
}
