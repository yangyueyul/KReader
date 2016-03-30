package test;

import android.test.AndroidTestCase;



import java.util.HashMap;

/**
 * Created by leixun on 2016/3/29.
 */
public class ApiTest extends AndroidTestCase{
    String responseJson =
            "";
    public void testGetBooks() throws Exception{
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("page","1");
        params.put("categoryid", "1");
        params.put("pageSize", "1");
    }
}
