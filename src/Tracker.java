import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pritom
 * Date: 4/5/13
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class Tracker{

    private String URL;
    private Map<Integer, TrackedFile> availableFiles;
    private Map<Integer, TrackedFile> requestedFiles;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public Map<Integer, TrackedFile> getAvailableFiles() {
        return availableFiles;
    }

    public void setAvailableFiles(Map<Integer, TrackedFile> availableFiles) {
        this.availableFiles = availableFiles;
    }

    public Map<Integer, TrackedFile> getRequestedFiles() {
        return requestedFiles;
    }

    public void setRequestedFiles(Map<Integer, TrackedFile> requestedFiles) {
        this.requestedFiles = requestedFiles;
    }

    public void sendMessage(Peer peer, String selectedAction, int fileId){
        /*TODO: this method will be called by peer after a fixed interval of time
         *take action based on message
         *if action = share, add peer to list of seeders of that particular tracked file
         *if action = delete, remove peer from list
         *if action = download, either return or call download method of peer
         */
        if (PeerAction.SHARE.getAction().equalsIgnoreCase(selectedAction)) {
            shareFile(peer, fileId);
        }
        else if (PeerAction.STOP.getAction().equalsIgnoreCase(selectedAction)){
            stopSharingFile(peer, fileId);
        }
        else if (PeerAction.DOWNLOAD.getAction().equalsIgnoreCase(selectedAction)) {
            download(peer, fileId);
        }

    }

    private void stopSharingFile(Peer peer, int fileId) {
        TrackedFile selectedFile = availableFiles.get(fileId);
        selectedFile.getSeeders().remove(peer.getId());
        SharedFile deletedFile = peer.getSharedFiles().get(fileId);
        peer.getSharedFiles().remove(fileId);
        peer.getPreviouslySharedFiles().put(fileId, deletedFile);
    }

    private void shareFile(Peer peer, int fileId) {
        TrackedFile selectedFile = availableFiles.get(fileId);

        while (peer.getCurrentUsedStorage() + selectedFile.getFileSize() > peer.getStorageCapacity()) {
            //System.out.println("Removing unprofitable files");
            peer.convertToList();
            List<SharedFile> sharedFileList = peer.getSharedFileList();
            int idOfFileToBeRemoved = sharedFileList.get(0).getId();
            stopSharingFile(peer, idOfFileToBeRemoved);
        }

        selectedFile.getSeeders().put(peer.getId(), peer);

        SharedFile newSharedFile = new SharedFile();

        newSharedFile.setId(selectedFile.getId());
        newSharedFile.setSize(selectedFile.getFileSize());
        newSharedFile.setUploadedSize(0);

        peer.getSharedFiles().put(fileId, newSharedFile);
        peer.setShareCount(peer.getShareCount() + 1);

        if (requestedFiles.containsKey(fileId)) {
            peer.setRequestHonouredCount(peer.getRequestHonouredCount()+ 1);
            peer.getSharedFiles().get(fileId).setRequested(true);
            requestedFiles.remove(fileId);
        }
    }

    private void download(Peer peer, int fileId) {
        TrackedFile selectedFile = availableFiles.get(fileId);
        peer.downloadFile(selectedFile);
    }

}
