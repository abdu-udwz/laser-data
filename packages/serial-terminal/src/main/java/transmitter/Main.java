package transmitter.source;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transmitter.source.connection.data.Packet;
import transmitter.source.connection.data.Packets;
import transmitter.source.connection.protocol.LaserProtocol;
import transmitter.source.connection.receiver.VirtualReceiver;
import transmitter.source.message.in.LightReadingMessage;
import transmitter.source.socket.SocketManager;
import transmitter.source.ui.controller.ConnectionAlert;
import transmitter.source.util.Res;
import transmitter.source.util.Threading;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    @Override
    public void stop() throws Exception {
        super.stop();
        Threading.shutdownAll(true);
        SocketManager.disconnect();
    }

    @Override
    public void start(Stage primaryStage){
        new ConnectionAlert().showAndWait();
//        testVirtualReceiver();
//        clearReadDuplicates();
//        transmitPackets();
    }

    public static void main(String[] args){
        launch(args);
    }

    private static void testVirtualReceiver(){
        new Thread(() -> {
            ObservableList<String> sample = FXCollections.observableArrayList();

            try {
                sample.addAll(Files.readAllLines(Paths.get("/home/abdu/Desktop/test.rece")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            VirtualReceiver virtualReceiver = new VirtualReceiver(LaserProtocol.getFromSettings());
            for (String receiverPair : sample) {
                LightReadingMessage pair = new LightReadingMessage(receiverPair.substring(12));
                virtualReceiver.setLightReading(pair);
            }

        }).start();
    }

    private static void clearReadDuplicates(){
        new Thread(() -> {

            ObservableList<String> sample = FXCollections.observableArrayList();

            try {
                sample.addAll(Files.readAllLines(Paths.get("/home/abdu/Desktop/test.rece")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            long lastStamp = 0;

            ObservableList<String> toWrite = FXCollections.observableArrayList();

            for (String line : sample) {
                LightReadingMessage message = new LightReadingMessage(line);
                long currentStamp = message.getTimestamp();

                if (currentStamp != lastStamp) {
                    toWrite.add(line);
                }
                lastStamp = currentStamp;
            }

            Path dest = Paths.get("/home/abdu/Desktop/no_duplicate");
            try {
                Files.write(dest, toWrite);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private static void transmitPackets(){

        String text = "الحمد لله رب العالمين الرحمن الرحيم ملك يوم الدين إياك نعبد وإياك نستعين ا";

        Charset charset = LaserProtocol.getFromSettings().getTextEncodingCharset();
        List<Packet> packets = Packets.forText(text, charset);
        System.out.println("=== A total of " + packets.size() + " packet(s) ===");

        int dataSize = 0;
        for (Packet packet : packets) {
            dataSize += packet.dataSize();
            System.out.printf("packet %d, size %d, data size %d%n", packet.index(), packet.size(), packet.dataSize());
        }

        System.out.println("=== read data of size " + dataSize + " byte(s) ===");

        System.out.println("=== testing creating receiver instances ====");

        List<Packet> receiverPackets = new ArrayList<>();

        for (Packet packet : packets) {
            Packet receiverInstance = Packet.getReceiverInstance(packet.getBytes(), packet.dataStartIndex, packet.dataEndIndex);
            receiverPackets.add(receiverInstance);
        }
        System.out.println(Packets.toText(receiverPackets, charset));
    }

    private static void showLightTimeChart(){

        Stage chartStage = new Stage();
        VBox root = null;
        try {
            root = FXMLLoader.load(Res.Fxml.LIGHT_TIME_CHART.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        chartStage.setScene(scene);
        chartStage.show();
    }
}
