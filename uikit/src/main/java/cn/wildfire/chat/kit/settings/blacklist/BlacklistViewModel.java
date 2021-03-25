/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.settings.blacklist;

import java.util.List;

import androidx.lifecycle.ViewModel;
import cn.wildfirechat.remote.ChatManager;

public class BlacklistViewModel extends ViewModel {

    public BlacklistViewModel() {
        super();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public List<String> getBlacklists() {
        return ChatManager.Instance().getBlackList(true);
    }

}
