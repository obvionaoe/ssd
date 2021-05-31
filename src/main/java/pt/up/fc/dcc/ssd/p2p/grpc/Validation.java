package pt.up.fc.dcc.ssd.p2p.grpc;

import static pt.up.fc.dcc.ssd.p2p.Config.ID_N_BITS;

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

    public static boolean validateRequest(FindItemsRequest request) {
        return validateId(request.getTopic());
    }

    public static boolean validateRequest(LeaveRequest request) {
        return validateId(request.getId());
    }

    private static boolean validateId(String id) {
        return id.length() == ID_N_BITS;
    }

    private static boolean validateConnectionInfo(GrpcConnectionInfo info) {
        return !info.getId().isEmpty() && !info.getAddress().isEmpty() && info.getPort() != 0;
    }
}
