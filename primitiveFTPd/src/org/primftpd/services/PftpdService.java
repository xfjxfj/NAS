package org.primftpd.services;

import org.primftpd.events.ClientActionEvent;
import org.primftpd.prefs.PrefsBean;

public interface PftpdService {

	void postClientAction(ClientActionEvent.Storage storage, ClientActionEvent.ClientAction clientAction, String clientIp, String path);

	PrefsBean getPrefsBean();
}
