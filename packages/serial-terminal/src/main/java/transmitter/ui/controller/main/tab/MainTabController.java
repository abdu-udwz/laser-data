package transmitter.source.ui.controller.main.tab;

import javafx.beans.property.ReadOnlyObjectProperty;
import transmitter.source.connection.receiver.VirtualReceiver;
import transmitter.source.ui.controller.main.CommState;
import transmitter.source.ui.controller.main.MainController;

public class MainTabController {

    public MainController getMainController(){
        return MainController.getCurrent();
    }

    public void setMainControllerState(CommState state) {
        MainController.getCurrent().setState(state);
    }

    public CommState getMainControllerState() {
        return MainController.getCurrent().getState();
    }

    public ReadOnlyObjectProperty<CommState> stateProperty(){
        return MainController.getCurrent().stateProperty();
    }

    public VirtualReceiver getVirtualReceiver(){
        return getMainController().getVirtualReceiver();
    }
}
