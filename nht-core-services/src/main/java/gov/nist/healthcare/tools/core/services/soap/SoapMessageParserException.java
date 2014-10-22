package gov.nist.healthcare.tools.core.services.soap;

import gov.nist.healthcare.tools.core.services.message.MessageParserException;

public class SoapMessageParserException extends MessageParserException {

	private static final long serialVersionUID = -5030971870248560876L;

	public SoapMessageParserException(String message) {
		super(message);
	}

	public SoapMessageParserException(RuntimeException exception) {
		super(exception);
	}
 
	public SoapMessageParserException(Exception e) {
		super(e);
	}
}