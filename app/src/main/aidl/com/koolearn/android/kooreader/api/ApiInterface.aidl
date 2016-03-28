/*
 * This code is in the public domain.
 */

package com.koolearn.android.kooreader.api;

import com.koolearn.android.kooreader.api.ApiObject;

interface ApiInterface {
	ApiObject request(int method, in ApiObject[] parameters);
	List<ApiObject> requestList(int method, in ApiObject[] parameters);
	Map requestMap(int method, in ApiObject[] parameters);
}
