package cl.bcs.tesoreria.cloper;

import cl.bcs.utils.BcsPojo;

/**
 * ORACLE SHIM of cl.bcs.tesoreria.cloper.CloperWsBSC (HTTP client of the CLOPER web service; needs org.json and the network): only the
 * connection parameter holder (verbatim, CloperWsBSC.java:41-45). The HTTP call is replaced by the scripted answer of {@link Cloper}.
 */
public class CloperWsBSC {

	// legacy CloperWsBSC.java:41-45 (verbatim)
	public static class ParametrosConexionCloperWS extends BcsPojo{
		String wsdl;
		String profile;
		String password;
	}
}
