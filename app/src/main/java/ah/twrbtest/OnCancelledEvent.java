package ah.twrbtest;

public class OnCancelledEvent {
    private boolean isSuccess;

    public OnCancelledEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
