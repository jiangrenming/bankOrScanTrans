package com.nld.cloudpos.payment.security;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;

public class CustomImageDownloader extends BaseImageDownloader {
	public CustomImageDownloader(Context context) {
		super(context);
	}

	public static HashMap<String, String> header = new HashMap<String, String>();

	@Override
	protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
		HttpURLConnection connection = super.createConnection(url, extra);

		if (header != null) {
			Iterator<String> iterator = header.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = header.get(key);
				connection.addRequestProperty(key, value);
			}
		}

		return connection;
	}
}