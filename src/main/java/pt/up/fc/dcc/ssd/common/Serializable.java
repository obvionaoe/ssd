package pt.up.fc.dcc.ssd.common;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.io.*;

public class Serializable {
    public static byte[] toByteArray(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object toObject(byte[] arr) {
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // Example
        Id test = new Id();

        System.out.println(test);

        Id d = (Id) toObject(toByteArray(test));

        System.out.println(d);
    }
}