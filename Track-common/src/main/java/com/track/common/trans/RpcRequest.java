package com.track.common.trans;

/**
 * Rpc请求
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
public class RpcRequest {

    private String id;
    private String interfaceName;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] paramVals;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getParamVals() {
        return paramVals;
    }

    public void setParamVals(Object[] paramVals) {
        this.paramVals = paramVals;
    }
}
