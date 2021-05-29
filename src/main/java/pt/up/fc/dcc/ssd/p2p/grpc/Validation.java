package pt.up.fc.dcc.ssd.p2p.grpc;

// TODO: finish this properly
public class Validation {
    public static boolean validateRequest(PingRequest request) {
        return validateConnectionInfo(request.getOriginConnectionInfo());
    }

    public static boolean validateRequest(StoreRequest request) {
        return true;
    }

    public static boolean validateRequest(BidRequest request) {
        return true;
    }

    public static boolean validateRequest(GossipRequest request) {
        return true;
    }

    public static boolean validateRequest(FindNodeRequest request) {
        return true;
    }

    public static boolean validateRequest(FindValueRequest request) {
        return true;
    }

    public static boolean validateRequest(LeaveRequest request) {
        return false;
    }

    private static boolean validateConnectionInfo(GrpcConnectionInfo info) {
        return !info.getId().isEmpty() && !info.getAddress().isEmpty() && info.getPort() != 0;
    }
}
