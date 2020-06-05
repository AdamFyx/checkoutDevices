window.jsHandlersMap = {
    functionInJs: function(bridge, data, responseCallback) {
        if (responseCallback) {
            try {
//                window.getConfigInfo({}, function(resData) {
//                    responseCallback(resData);
//                });
                window.outerTest();
            } catch (e) {
                console.error(e);
            }
        }
    },
    initHandlers: function(bridge, data, responseCallback) {
        var handlerArr = JSON.parse(data);
        for (var index in handlerArr) {
            var handle = handlerArr[index];
            window[handle.fnName] = function(handle) {
                return function(params, callback) {
                    WebViewJavascriptBridge.callHandler(
                        handle.fnName,
                        params,
                        function(resData) {
                          callback(resData);
                        }
                    );
                }
            }(handle);
        }
    },
    refreshOuterHandlers: function(bridge, data, responseCallback) {
        console.log("refreshOuterHandlers ============== begin");
        for (var key in window) {
            if (new RegExp("^jsHandler_").test(key)) {
                console.log("==========" + key)
                bridge.registerHandler(key, window[key]);
            }
        }
        console.log("refreshOuterHandlers ============== end");
        responseCallback("success");
    }
};