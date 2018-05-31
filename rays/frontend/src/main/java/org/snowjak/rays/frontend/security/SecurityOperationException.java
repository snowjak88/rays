package org.snowjak.rays.frontend.security;

import java.util.Arrays;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

/**
 * An Exception thrown by methods in {@link SecurityOperations}, with an
 * included {@link Reason} enumeration.
 * 
 * @author snowjak88
 *
 */
public class SecurityOperationException extends Exception {
	
	/**
	 * An enumeration which maps onto certain subclasses of
	 * {@link AuthenticationException}. This is intended to allow convenient
	 * error-reporting (especially mapping error-messages onto -conditions).
	 * 
	 * @author snowjak88
	 *
	 */
	public static enum Reason {
		/**
		 * The given user-account is currently disabled.
		 */
		ACCOUNT_DISABLED(DisabledException.class),
		/**
		 * The given user-account has expired.
		 */
		ACCOUNT_EXPIRED(AccountExpiredException.class),
		/**
		 * The given user-account is currently locked.
		 */
		ACCOUNT_LOCKED(LockedException.class),
		/**
		 * The given username/password cannot be authenticated.
		 */
		BAD_CREDENTIALS(BadCredentialsException.class),
		/**
		 * The given user-account's password is expired.
		 */
		CREDENTIALS_EXPIRED(CredentialsExpiredException.class),
		/**
		 * The encountered security-exception is not mapped to any Reason.
		 */
		OTHER(null);
		
		private Class<? extends AuthenticationException> mappedClass;
		
		Reason(Class<? extends AuthenticationException> mappedClass) {
			
			this.mappedClass = mappedClass;
		}
		
		/**
		 * Given an instance of {@link AuthenticationException}, get the Reason that
		 * best fits that exception.
		 * 
		 * @param exception
		 * @return
		 */
		static <E extends AuthenticationException> SecurityOperationException.Reason getByException(E exception) {
			
			return Arrays.stream(Reason.values()).filter(r -> r.mappedClass != null)
					.filter(r -> r.mappedClass.isAssignableFrom(exception.getClass())).findFirst().orElse(Reason.OTHER);
		}
	}
	
	private static final long serialVersionUID = 760609205065852748L;
	
	private final SecurityOperationException.Reason reason;
	
	public SecurityOperationException(AuthenticationException cause) {
		
		super(cause);
		this.reason = Reason.getByException(cause);
	}
	
	public SecurityOperationException.Reason getReason() {
		
		return reason;
	}
	
	@Override
	public String toString() {
		
		return "SecurityOperationException [reason=" + reason.toString() + ", cause="
				+ this.getCause().getClass().getName() + "]";
	}
	
}