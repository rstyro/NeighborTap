package com.lrs.common.exception;


import com.lrs.common.constant.ApiResultEnum;
import lombok.Data;

/**
 * 自定义的api异常
 * @author rstyro
 *
 */
@Data
public class ServiceException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	private Integer status;
	private String message;
	private Object data;
	private Exception exception;
	public ServiceException() {
		super();
	}

	public ServiceException(Integer status, String message, Object data, Exception exception) {
		this.status = status;
		this.message = message;
		this.data = data;
		this.exception = exception;
	}
	public ServiceException(String message) {
		this(ApiResultEnum.ERROR.getCode(),message,null,null);
	}
	public ServiceException(ApiResultEnum apiResultEnum) {
		this(apiResultEnum.getCode(),apiResultEnum.getMessage(),null,null);
	}
	public ServiceException(ApiResultEnum apiResultEnum, Object data) {
		this(apiResultEnum.getCode(),apiResultEnum.getMessage(),data,null);
	}
	public ServiceException(ApiResultEnum apiResultEnum, Object data, Exception exception) {
		this(apiResultEnum.getCode(),apiResultEnum.getMessage(),data,exception);
	}


}
