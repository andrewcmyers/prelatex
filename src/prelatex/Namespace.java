package prelatex;
public interface Namespace<T> {
	static class LookupFailure extends Exception {
		@Override public Exception fillInStackTrace() { return this; }
	}

	/** Return the */
	T lookup(String name) throws LookupFailure;
}