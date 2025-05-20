package ecart.exception;

public class InventoryProcessingException extends RuntimeException {
	private final int httpStatus;
	public InventoryProcessingException(String message,int httpStatus)
	{
		super(message);
		this.httpStatus=httpStatus;
		
	}
	public int getHttpStatus()
	{
		return httpStatus;
	}

}
