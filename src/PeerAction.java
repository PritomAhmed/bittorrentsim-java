/**
 * Created with IntelliJ IDEA.
 * User: pritom
 * Date: 4/5/13
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public enum PeerAction {
    SHARE("share"), STOP("stop"), DOWNLOAD("download");

    private String action;

    private PeerAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
