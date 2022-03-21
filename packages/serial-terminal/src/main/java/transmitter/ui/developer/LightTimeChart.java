package transmitter.ui.developer;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import transmitter.message.in.LightReadingMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LightTimeChart {

    @FXML
    private ScrollPane chartScroll;
    @FXML
    private StackPane chartStack;
    @FXML
    private Spinner<Integer> durationSpinner;
    @FXML
    private JFXButton prevButton;
    @FXML
    private JFXButton nextButton;

    private LineChart<Number, Number> lineChart;
    private NumberAxis lightAxis;
    private NumberAxis timeAxis;

    private final LongProperty currentEndTimestamp = new SimpleLongProperty(0);
    private final IntegerProperty currentReceiverPairIndex = new SimpleIntegerProperty(0);

    private final ObservableList<LightReadingMessage> lightReadings = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        try {
            List<String> receiverStrings = Files.readAllLines(Paths.get("/home/abdu/Desktop/test5.rece"));

            for (String receiverString : receiverStrings) {
                lightReadings.add(new LightReadingMessage(receiverString.substring(12)));
            }

            setCurrentEndTimestamp(lightReadings.get(0).getTimestamp());

        } catch (IOException e) {
            e.printStackTrace();
        }

        removeRedundantPairs();

        // int size = lightReadings.size();
        // ReceiverPair middlePair = lightReadings.get(5075);
        // setCurrentEndTimestamp(middlePair.getTimestamp());
        // setCurrentReceiverPairIndex(5075);

        lightAxis = new NumberAxis();
        timeAxis = new NumberAxis();
        timeAxis.setForceZeroInRange(false);
        lineChart = new LineChart<>(timeAxis, lightAxis);
        lineChart.setMaxWidth(Double.MAX_VALUE);
        chartStack.getChildren().add(lineChart);

        lightAxis.setAutoRanging(false);
        lightAxis.setLowerBound(0);
        lightAxis.setUpperBound(600);

        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20, 1000, 160));

        initListeners();

        durationSpinner.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY)
                testLiveShow();
        });
    }

    private void testLiveShow() {
        timeAxis.setAnimated(false);
        lightAxis.setAnimated(false);
        lineChart.setAnimated(false);

        XYChart.Series<Number, Number> showSeries = new XYChart.Series<>();
        lineChart.getData().add(showSeries);

        final int[] currentPairIndex = { 0 };
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                LightReadingMessage reading = lightReadings.get(currentPairIndex[0]);

                Platform.runLater(() -> {
                    if (showSeries.getData().size() >= 40) {
                        showSeries.getData().remove(0);
                    }

                    XYChart.Data<Number, Number> newData = new XYChart.Data<>(reading.getTimestamp(),
                            reading.getLight());
                    showSeries.getData().add(newData);
                });

                currentPairIndex[0]++;
            }
        }, 1000, 200);
    }

    private void initListeners() {
        nextButton.setOnAction(event -> {
            setChartSample(nextSample());
        });

        prevButton.setOnAction(event -> {
            setChartSample(previousSample());
        });
    }

    public void setChartSample(List<LightReadingMessage> lightReadings) {

        lineChart.getData().clear();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        for (LightReadingMessage reading : lightReadings) {

            XYChart.Data<Number, Number> data = new XYChart.Data<>(reading.getTimestamp(), reading.getLight());

            series.getData().add(data);
        }

        timeAxis.setAutoRanging(true);
        lineChart.getData().add(series);
    }

    private ObservableList<LightReadingMessage> nextSample() {

        long estimatedEndTimestamp = getCurrentEndTimestamp() + getByteDuration();

        List<LightReadingMessage> searchIn = this.lightReadings.subList(getCurrentReceiverPairIndex(),
                this.lightReadings.size());

        int startIndex = getCurrentReceiverPairIndex();
        int endIndex = 0;
        long endTimestamp = 0;

        for (LightReadingMessage reading : searchIn) {
            long pairTimestamp = reading.getTimestamp();

            if (pairTimestamp >= estimatedEndTimestamp - 1 &&
                    pairTimestamp <= estimatedEndTimestamp + 1) {

                endIndex = lightReadings.indexOf(reading);
                endTimestamp = pairTimestamp;

                break;
            }
        }

        if (endIndex == 0) {
            return FXCollections.observableArrayList();
        } else {
            setCurrentEndTimestamp(endTimestamp);
            setCurrentReceiverPairIndex(endIndex);
            List<LightReadingMessage> sample = this.lightReadings.subList(startIndex, endIndex);
            return FXCollections.observableArrayList(sample);
        }
    }

    private ObservableList<LightReadingMessage> previousSample() {

        long estimatedStartTimestamp = getCurrentEndTimestamp() - (2 * getByteDuration());

        List<LightReadingMessage> searchIn = this.lightReadings.subList(0, getCurrentReceiverPairIndex());

        int startIndex = 0;
        long startTimestamp = 0;

        for (LightReadingMessage reading : searchIn) {
            long pairTimestamp = reading.getTimestamp();

            if (pairTimestamp >= estimatedStartTimestamp - 1 &&
                    pairTimestamp <= estimatedStartTimestamp + 1) {

                startIndex = lightReadings.indexOf(reading);
                startTimestamp = pairTimestamp;

                break;
            }
        }

        long estimatedEndTimestamp = startTimestamp + getByteDuration();

        int endIndex = 0;
        long endTimestamp = 0;

        for (LightReadingMessage reading : searchIn) {
            long pairTimestamp = reading.getTimestamp();

            if (pairTimestamp >= estimatedEndTimestamp - 1 &&
                    pairTimestamp <= estimatedEndTimestamp + 1) {

                endIndex = lightReadings.indexOf(reading);
                endTimestamp = pairTimestamp;

                break;
            }
        }

        if (startTimestamp == 0 || endIndex == 0 || endTimestamp == 0) {
            return FXCollections.observableArrayList();
        } else {

            setCurrentEndTimestamp(endTimestamp);
            setCurrentReceiverPairIndex(endIndex);
            List<LightReadingMessage> sample = this.lightReadings.subList(startIndex, endIndex);
            return FXCollections.observableArrayList(sample);
        }

    }

    private void removeRedundantPairs() {
        int lastLight = 0;
        long lastStamp = 0;

        ArrayList<LightReadingMessage> toRemove = new ArrayList<>();

        int withSameLight = 0;
        int withDiffLight = 0;

        for (LightReadingMessage readingMessage : lightReadings) {
            int currentLight = readingMessage.getLight();
            long currentStamp = readingMessage.getTimestamp();

            if (currentStamp == lastStamp) {

                if (currentLight != lastLight) {
                    withDiffLight++;
                    toRemove.add(readingMessage);
                } else {
                    withSameLight++;
                }
            }

            lastLight = currentLight;
            lastStamp = currentStamp;
        }

        System.out.println("Overall count: " + (withDiffLight + withSameLight));
        System.out.println("same light count: " + (withSameLight));
        System.out.println("diff light count: " + (withDiffLight));

        lightReadings.removeAll(toRemove);
    }

    public void setByteDuration(Integer duration) {

        durationSpinner.getValueFactory().setValue(duration);
    }

    public long getByteDuration() {
        return durationSpinner.getValue();
    }

    public ReadOnlyObjectProperty<Integer> byteDurationProperty() {
        return durationSpinner.valueProperty();
    }

    public long getCurrentEndTimestamp() {
        return currentEndTimestamp.get();
    }

    public LongProperty currentEndTimestampProperty() {
        return currentEndTimestamp;
    }

    public void setCurrentEndTimestamp(long currentEndTimestamp) {
        this.currentEndTimestamp.set(currentEndTimestamp);
    }

    public int getCurrentReceiverPairIndex() {
        return currentReceiverPairIndex.get();
    }

    public IntegerProperty currentReceiverPairIndexProperty() {
        return currentReceiverPairIndex;
    }

    public void setCurrentReceiverPairIndex(int currentReceiverPairIndex) {
        this.currentReceiverPairIndex.set(currentReceiverPairIndex);
    }
}
