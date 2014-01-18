package com.radiadesign.catalina.session;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class RedisSessionHandlerValve extends ValveBase {
	private final Log log = LogFactory.getLog(RedisSessionManager.class);
	private RedisSessionManager manager;

	public void setRedisSessionManager(RedisSessionManager manager) {
		this.manager = manager;
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		try {
			getNext().invoke(request, response);
		} finally {
			storeOrRemoveSession();
			manager.afterRequest();
		}
	}

	private void storeOrRemoveSession() {
		try {
			RedisSession session = manager.getCurrentSession();
			if (session != null) {

				if (session.isValid()) {
					log.trace("Request with session completed, saving session " + session.getId());
					if (session.getSession() != null) {
						log.trace("HTTP Session present, saving " + session.getId());
						manager.save(session);
					} else {
						log.trace("No HTTP Session present, Not saving " + session.getId());
					}
				} else {
					log.trace("HTTP Session has been invalidated, removing :" + session.getId());
					session = (RedisSession) manager.createSession(session.getId());
					manager.save(session);
				}
			}
		} catch (Exception e) {
			// Do nothing.
		}
	}
}
