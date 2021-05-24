package pt.up.fc.dcc.ssd.common;

import java.io.*;

public class SerializableMockup implements Serializable {
    public final long serialVersionUID = 1L;
    String test;

    public SerializableMockup(String test) {
        this.test = test;
    }

    public byte[] toByteArray() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SerializableMockup toObject(byte[] arr) {
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (SerializableMockup) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SerializableMockup sm = new SerializableMockup("teste");

        System.out.println(sm.test);

        System.out.println(SerializableMockup.toObject(sm.toByteArray()).test);
    }
}
