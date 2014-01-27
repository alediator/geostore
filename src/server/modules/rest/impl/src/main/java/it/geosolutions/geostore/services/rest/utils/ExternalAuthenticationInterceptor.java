/*
 *  Copyright (C) 2007 - 2014 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 * 
 *  GPLv3 + Classpath exception
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geostore.services.rest.utils;

import it.geosolutions.geostore.core.model.User;
import it.geosolutions.geostore.core.model.enums.Role;

import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.message.Message;

/**
 * 
 * Class ExternalAuthenticationInterceptor. Geostore authentication interceptor
 * that allows get the user and the role from an external authenticator. If you
 * need to check the user session, you may override validUser method
 * 
 * @author adiaz (alejandro.diaz at geo-solutions.it)
 */
public class ExternalAuthenticationInterceptor extends
		AutoUserCreateGeostoreAuthenticationInterceptor {

	/**
	 * User name header for the message
	 */
	private String userNameHeader;

	/**
	 * User role header for the message
	 */
	private String userRoleHeader;

	/**
	 * Obtain the user name from the header
	 * 
	 * @param message
	 * 
	 * @return user name in the request header
	 */
	private String getUserName(Message message) {
		return getHeader(message, userNameHeader);
	}

	/**
	 * Obtain the user sole from the header
	 * 
	 * @param message
	 * 
	 * @return user role in the request header
	 */
	private String getUserRole(Message message) {
		return getHeader(message, userRoleHeader);
	}

	/**
	 * Obtain a parameter from the header
	 * 
	 * @param message
	 * 
	 * @return parameter <code>name</code> value in the request header
	 */
	private String getHeader(Message message, String name) {
		@SuppressWarnings("unchecked")
		Map<String, List<String>> headers = (Map<String, List<String>>) message
				.get(Message.PROTOCOL_HEADERS);
		if (headers.containsKey(name)) {
			return headers.get(name).get(0);
		} else {
			return null;
		}
	}

	/**
	 * Obtain user an role from configured headers on the message
	 * 
	 * @param message
	 * 
	 * @return User with the user name and the role from the header parameters
	 */
	protected synchronized User getUserFromHeader(Message message) {
		User user = new User();
		String username = getUserName(message);
		String userRole = getUserRole(message);
		user.setName(username);
		user.setRole(Role.ADMIN.name().equals(userRole) ? Role.ADMIN
				: Role.USER);
		return user;
	}

	/**
	 * Check if the user generated from the request is valid in the external
	 * authentication system
	 * 
	 * @param user
	 *            created from the request headers
	 * @param message
	 * 
	 * @return true if the user is valid or false otherwise
	 */
	protected synchronized boolean validUser(User user, Message message) {
		// TODO: Override it to check if the user is valid on the externalized
		// authentication provider
		return true;
	}

	/**
	 * Obtain an user from his user name
	 * 
	 * @param username
	 *            of the user
	 * @param message
	 *            intercepted
	 * 
	 * @return user identified with the user name
	 */
	protected synchronized User getUser(String username, Message message) {
		User user = null;
		try {
			user = getUserFromHeader(message);
			if (validUser(user, message)) {
				this.setNewUsersRole(user.getRole());
				user = super.getUser(user.getName(), message);
				return user;
			} else {
				throw new AccessDeniedException(
						"The user is not valid in the external authentication");
			}
		} catch (Exception e) {
			if (LOGGER.isInfoEnabled()) {
				String reason = "Requested user not found: " + username;
				LOGGER.info(reason);
				throw new AccessDeniedException(reason);
			}
		}

		return user;

	}

	/**
	 * @return the userNameHeader
	 */
	public String getUserNameHeader() {
		return userNameHeader;
	}

	/**
	 * @param userNameHeader
	 *            the userNameHeader to set
	 */
	public void setUserNameHeader(String userNameHeader) {
		this.userNameHeader = userNameHeader;
	}

	/**
	 * @return the userRoleHeader
	 */
	public String getUserRoleHeader() {
		return userRoleHeader;
	}

	/**
	 * @param userRoleHeader
	 *            the userRoleHeader to set
	 */
	public void setUserRoleHeader(String userRoleHeader) {
		this.userRoleHeader = userRoleHeader;
	}

}