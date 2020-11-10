package transmitter.source.util;

import com.jfoenix.controls.JFXListCell;
import javafx.util.Callback;

import java.util.Locale;
import java.util.ResourceBundle;

public class Utils {

    public static final ResourceBundle DEFAULT_PROPERTIES =
            ResourceBundle.getBundle(Res.DEFAULT_PROPERTIES);

    public static ResourceBundle getBundle() {
        //TODO: use settings
        return ResourceBundle.getBundle(Res.LANGUAGE_PATH, Locale.ENGLISH);
    }

    public static String getI18nString(String key){
        return getBundle().getString(key);
    }

    public static <T> Callback<Boolean, JFXListCell<T>> customCellFactory(Callback<T, String> callback){

        return new Callback<Boolean, JFXListCell<T>>() {

            @Override
            public JFXListCell<T> call(Boolean isButtonCell) {

                return new  JFXListCell<T>(){
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);

                        if (! empty) {
                            setText(callback.call(item));
                        }
                        else{
                            setText(null);
                            setGraphic(null);
                        }

                        if (isButtonCell){
                            setVisible(! empty);
                        }
                    }
                };
            }
        };
    }
}
