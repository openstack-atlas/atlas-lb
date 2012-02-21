package org.openstack.atlas.ctxs.service.domain.pojo;

import org.openstack.atlas.service.domain.pojo.MessageDataContainer;

import java.util.HashMap;

public class CtxsMessageDataContainer extends MessageDataContainer{

    HashMap<String, Object> hashData;

    public HashMap<String, Object> getHashData() {
        return hashData;
    }

    public void setHashData(HashMap<String, Object> hashData) {
        this.hashData = hashData;
    }

}
