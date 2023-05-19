package prelatex;
public interface Namespace<T> {
	static class LookupFailure extends Exception {}

	/** Return the */
	T lookup(String name) throws LookupFailure;
}
