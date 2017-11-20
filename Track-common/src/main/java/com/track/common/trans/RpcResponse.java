package com.track.common.trans;

/**
 * Rpc响应
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcResponse {

    private String id;
    private Throwable error;
    private Object result;

    public boolean isError() {
        return null != this.error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
