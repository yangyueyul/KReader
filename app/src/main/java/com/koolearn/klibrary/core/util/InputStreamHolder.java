package com.koolearn.klibrary.core.util;

import java.io.InputStream;
import java.io.IOException;

public interface InputStreamHolder {
	InputStream getInputStream() throws IOException;
}
