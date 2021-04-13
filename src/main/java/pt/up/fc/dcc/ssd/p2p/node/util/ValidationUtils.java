package pt.up.fc.dcc.ssd.p2p.node.util;

import pt.up.fc.dcc.ssd.p2p.grpc.*;

public class ValidationUtils {
    public static boolean validateRequest(PingRequest request) {
        return true;
    }

    public static boolean validateRequest(StoreRequest request) {
        return true;
    }

    public static boolean validateRequest(FindNodeRequest request) {
        return true;
    }

    public static boolean validateRequest(ValueRequest request) {
        return false;
    }

    public static boolean validateRequest(LeaveRequest request) {
        return false;
    }

    private static boolean validateConnectionInfo(GrpcConnectionInfo connectionInfo) {
        System.out.println(connectionInfo.getId());
        return true;
    }
}
