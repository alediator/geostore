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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import it.geosolutions.geostore.core.model.User;
import it.geosolutions.geostore.services.exception.NotFoundServiceEx;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.junit.Test;

/**
 * 
 * Test for ExternalAuthenticationInterceptor. Test the user external
 * authentication with the interceptor
 * 
 * @author adiaz (alejandro.diaz at geo-solutions.it)
 */
public class ExternalAuthenticationInterceptorTest extends
		BaseAuthenticationInterceptorTest {

	/**
	 * Header for the user name for the test requests
	 */
	private static String USER_NAME_HEADER = "_user";

	/**
	 * Header for the user role for the test requests
	 */
	private static String ROLE_HEADER = "_role";

	/**
	 * Access denied for a new user if the interceptor doesn't create new users
	 * and the headers can't
	 */
	@Test(expected = AccessDeniedException.class)
	public void testNotCreateUsers() {
		ExternalAuthenticationInterceptor interceptor = new ExternalAuthenticationInterceptor();
		interceptor.setAutoCreateUsers(false);
		interceptor.setUserService(userService);
		interceptor.setUserNameHeader(USER_NAME_HEADER);
		interceptor.setUserRoleHeader(ROLE_HEADER);
		interceptor.handleMessage(getMockedMessage("test", "", null));
	}

	/**
	 * Create a user with the credentials from an external authentication
	 * provider
	 */
	@Test
	public void testCreateUsersStrategyFromHeader() {
		ExternalAuthenticationInterceptor interceptor = new ExternalAuthenticationInterceptor();
		interceptor.setAutoCreateUsers(true);
		interceptor.setNewUsersPassword(NewPasswordStrategy.NONE);
		interceptor.setUserService(userService);
		interceptor.setUserNameHeader(USER_NAME_HEADER);
		interceptor.setUserRoleHeader(ROLE_HEADER);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(USER_NAME_HEADER, "test3");
		headers.put(ROLE_HEADER, "ADMIN");
		interceptor.handleMessage(getMockedMessage("test3", "", headers));
		try {
			User user = userService.get("test3");
			assertNotNull(user);
			assertEquals("ADMIN", user.getRole().name());
		} catch (NotFoundServiceEx e) {
			fail("Couldn't found user");
		}
	}

}