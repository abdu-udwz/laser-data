package transmitter.ui.controls.alert;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum InfoAlertType {

    NONE(MaterialDesignB.BOX, Color.TRANSPARENT, 32),
    INFO(MaterialDesignI.INFORMATION, Color.DEEPSKYBLUE, 32),
    CONFIRM(MaterialDesignH.HELP_CIRCLE, Color.DEEPSKYBLUE, 32),
    WARNING(MaterialDesignA.ALERT, Color.DARKORANGE, 32),
    ERROR(MaterialDesignA.ALERT, Color.RED, 32);

    public final Paint fill;
    public final Ikon code;
    public final int glyphSize;

    InfoAlertType(Ikon code, Paint fill, int size) {
        this.fill = fill;
        this.code = code;
        this.glyphSize = size;
    }

}
