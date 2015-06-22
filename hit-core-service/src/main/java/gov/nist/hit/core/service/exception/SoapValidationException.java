package gov.nist.hit.core.service.exception;


public class SoapValidationException extends MessageValidationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SoapValidationException(String message) {
		super(message);
	}

	public SoapValidationException(RuntimeException exception) {
		super(exception);
	}

	public SoapValidationException(MessageValidationException e) {
		super(e);
	}
	
	public SoapValidationException(Exception e) {
		super(e);
	}
}
