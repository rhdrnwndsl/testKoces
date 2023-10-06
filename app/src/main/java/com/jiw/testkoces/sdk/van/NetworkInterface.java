package com.jiw.testkoces.sdk.van;

public class NetworkInterface {

    public interface ConnectListener {
        /**
         *
         * @param _internetState -1 아예 인터넷이 연결이 안 된 경우
         * @param _bState   true :서버에 연결된 경우,  false 서버에서 끊어진 경우
         * @param _EventMsg     이벤트 메시지
         */
        void onState(int _internetState,boolean _bState,String _EventMsg);
    }
}
