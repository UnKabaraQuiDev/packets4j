package lu.pcy113.p4j.socket.client;

public class P4JClientException extends RuntimeException {

	public P4JClientException(String msg) {
		super(msg);
	}

	public P4JClientException(Throwable e) {
		super(e);
	}

	public P4JClientException(String string, Throwable e) {
		super(string, e);
	}

}
