import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pritom
 * Date: 4/5/13
 * Time: 10:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class Peer implements Runnable {

    private int id;
    private float amountDownloaded;
    private float amountUploaded;
    private float shareRatio;
    private float satisfaction;
    private int satisfactionCount;
    private int disappointmentCount;
    private int storageCapacity;
    private int downloadSpeed;
    private int uploadSpeed;
    private Tracker tracker;
    private Map<Integer, SharedFile> sharedFiles;
    private Map<Integer, SharedFile> previouslySharedFiles;
    private List<SharedFile> sharedFileList;
    private Thread thread;
    private int downloadCount;
    private int shareCount;
    private int uploadCount;
    private int requestHonouredCount;

    private static final int DISSATISFACTION_PENALTY = 5;

    public Peer() {
        thread = new Thread(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public int getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(int uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public float getAmountDownloaded() {
        return amountDownloaded;
    }

    public void setAmountDownloaded(float amountDownloaded) {
        this.amountDownloaded = amountDownloaded;
    }

    public float getAmountUploaded() {
        return amountUploaded;
    }

    public void setAmountUploaded(float amountUploaded) {
        this.amountUploaded = amountUploaded;
    }

    public float getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(float satisfaction) {
        this.satisfaction = satisfaction;
    }

    public float getShareRatio() {
        return shareRatio;
    }

    public void setShareRatio(float shareRatio) {
        this.shareRatio = shareRatio;
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(int storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    public Map<Integer, SharedFile> getSharedFiles() {
        return sharedFiles;
    }

    public void setSharedFiles(Map<Integer, SharedFile> sharedFiles) {
        this.sharedFiles = sharedFiles;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public int getSatisfactionCount() {
        return satisfactionCount;
    }

    public void setSatisfactionCount(int satisfactionCount) {
        this.satisfactionCount = satisfactionCount;
    }

    public int getDisappointmentCount() {
        return disappointmentCount;
    }

    public void setDisappointmentCount(int disappointmentCount) {
        this.disappointmentCount = disappointmentCount;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(int uploadCount) {
        this.uploadCount = uploadCount;
    }

    public List<SharedFile> getSharedFileList() {
        return sharedFileList;
    }

    public void setSharedFileList(List<SharedFile> sharedFileList) {
        this.sharedFileList = sharedFileList;
    }

    public int getRequestHonouredCount() {
        return requestHonouredCount;
    }

    public void setRequestHonouredCount(int requestHonouredCount) {
        this.requestHonouredCount = requestHonouredCount;
    }

    public Map<Integer, SharedFile> getPreviouslySharedFiles() {
        return previouslySharedFiles;
    }

    public void setPreviouslySharedFiles(Map<Integer, SharedFile> previouslySharedFiles) {
        this.previouslySharedFiles = previouslySharedFiles;
    }

    public void simulatePeer() {

        Random random = new Random();
        int selectedFileId = random.nextInt(Main.MAX_NO_OF_FILES) + 1;
        int selectedActionIndex = random.nextInt(Main.MAX_NO_OF_ACTIONS);
        PeerAction selectedPeerAction = PeerAction.values()[selectedActionIndex];
        String selectedAction = selectedPeerAction.getAction();

        if (shareRatio == Main.MIN_SHARE_RATIO) {
            selectedPeerAction = PeerAction.SHARE;
            selectedAction = PeerAction.SHARE.getAction();
        }

        if (selectedPeerAction == PeerAction.DOWNLOAD && sharedFiles.containsKey(selectedFileId)) {
            return;
        } else if (selectedPeerAction == PeerAction.SHARE && sharedFiles.containsKey(selectedFileId)) {
            return;
        } else if (selectedPeerAction == PeerAction.STOP && !sharedFiles.containsKey(selectedFileId)) {
            return;
        }

        if (selectedPeerAction == PeerAction.SHARE) {
            //System.out.println("Honouring request");
            selectedFileId = chooseRequestedFileToShare(selectedFileId);
        }

        tracker.sendMessage(this, selectedAction, selectedFileId);

    }

    private int chooseRequestedFileToShare(int selectedFileId) {
        if (!tracker.getRequestedFiles().isEmpty()) {
            for (int requestedFileId : tracker.getRequestedFiles().keySet()) {
                if (previouslySharedFiles.containsKey(requestedFileId)) {
                    selectedFileId = requestedFileId;
                    previouslySharedFiles.remove(requestedFileId);
                    break;
                }
            }
        }
        return selectedFileId;
    }

    public void downloadFile(TrackedFile fileToBeDownloaded) {
        Map<Integer, Peer> seeders = fileToBeDownloaded.getSeeders();
        if (seeders.isEmpty()) {
            satisfaction -= DISSATISFACTION_PENALTY;
            ++disappointmentCount;
            tracker.getRequestedFiles().put(fileToBeDownloaded.getId(), fileToBeDownloaded);
        } else {
            int totalUploadSpeedOfFile = fileToBeDownloaded.getTotalUploadSpeed();
            int fileDownloadSpeed = Math.min(downloadSpeed, totalUploadSpeedOfFile);
            int downloadedFileSize = fileToBeDownloaded.getFileSize();

            if (fileDownloadSpeed == 0) {                       //peers have suddenly stopped sharing the file
                satisfaction -= DISSATISFACTION_PENALTY;
                ++disappointmentCount;
                tracker.getRequestedFiles().put(fileToBeDownloaded.getId(), fileToBeDownloaded);
                return;
            }

            float probableShareRatio = calculateProbableShareRatio(downloadedFileSize);
            if (probableShareRatio < Main.MIN_SHARE_RATIO) {
                //System.out.println("Share Ratio: " + shareRatio);
                return;
            }
            amountDownloaded += downloadedFileSize;

            float downloadCompletionTime = downloadedFileSize / fileDownloadSpeed;
            satisfaction += downloadCompletionTime;
            ++satisfactionCount;
            shareRatio = probableShareRatio;
            ++downloadCount;

            for (Peer seeder : seeders.values()) {
<<<<<<< HEAD
                uploadFile(fileToBeDownloaded, totalUploadSpeedOfFile, downloadedFileSize, seeder);
=======
                uploadWithHonouredRequestsAndRareFileBonus(fileToBeDownloaded, totalUploadSpeedOfFile, downloadedFileSize, seeder);
>>>>>>> be4fd7463fbefa6a952d27a987e2d3cbf0a2d46c
            }

        }

    }

<<<<<<< HEAD
    private void uploadFile(TrackedFile fileToBeDownloaded, int totalUploadSpeedOfFile, int downloadedFileSize, Peer seeder) {
=======
    private void upload(TrackedFile fileToBeDownloaded, int totalUploadSpeedOfFile, int downloadedFileSize, Peer seeder) {
>>>>>>> be4fd7463fbefa6a952d27a987e2d3cbf0a2d46c
        SharedFile copyOfFileOfSeeder = seeder.getSharedFiles().get(fileToBeDownloaded.getId());
        if (copyOfFileOfSeeder != null) {
            float amountUploadedBySeeder = (uploadSpeed * downloadedFileSize) / totalUploadSpeedOfFile;
            //System.out.println("Upload Speed: "+ uploadSpeed + " , TotalUploadSpeed: " + totalUploadSpeedOfFile + " , FileSize: " + downloadedFileSize +" , Amount uploaded by seeder: " + amountUploadedBySeeder);
            seeder.setAmountUploaded(seeder.getAmountUploaded() + amountUploadedBySeeder);
            seeder.updateShareRatio();
            seeder.setUploadCount(seeder.getUploadCount() + 1);
            copyOfFileOfSeeder.setUploadedSize(copyOfFileOfSeeder.getUploadedSize() + amountUploadedBySeeder);
        }
    }

    private void updateShareRatio() {
        //TODO: should add our proposed non-linear SRE logic instead of direct calculation in future
        shareRatio = amountUploaded / amountDownloaded;
    }

    private void uploadWithHonouredRequestsAndRareFileBonus(TrackedFile fileToBeDownloaded, int totalUploadSpeedOfFile, int downloadedFileSize, Peer seeder) {
        SharedFile copyOfFileOfSeeder = seeder.getSharedFiles().get(fileToBeDownloaded.getId());
        if (copyOfFileOfSeeder != null) {
            float amountUploadedBySeeder = (uploadSpeed * downloadedFileSize) / totalUploadSpeedOfFile;
            if (copyOfFileOfSeeder.isRequested()) {
                amountUploadedBySeeder*=Main.RESEED_BONUS_FACTOR;
            }
            if (fileToBeDownloaded.isRare()) {
                amountUploadedBySeeder*=Main.RARE_FILE_BONUS_FACTOR;
            }
            //System.out.println("Upload Speed: "+ uploadSpeed + " , TotalUploadSpeed: " + totalUploadSpeedOfFile + " , FileSize: " + downloadedFileSize +" , Amount uploaded by seeder: " + amountUploadedBySeeder);
            seeder.setAmountUploaded(seeder.getAmountUploaded() + amountUploadedBySeeder);
            seeder.updateShareRatio();
            seeder.setUploadCount(seeder.getUploadCount() + 1);
            copyOfFileOfSeeder.setUploadedSize(copyOfFileOfSeeder.getUploadedSize() + amountUploadedBySeeder);
        }
    }

    private float calculateProbableShareRatio(float downloadedFileSize) {
        float probableDownloadedFileSize = amountDownloaded + downloadedFileSize;
        return amountUploaded / probableDownloadedFileSize;
    }

    @Override
    public void run() {
        for (int i = 0; i < Main.MAX_NO_OF_ROUNDS_PER_PEER; i++) {
            simulatePeer();
        }
    }

    public void showFinalState() {
        System.out.println("Share Ratio: " + shareRatio + " , Satisfaction: " + satisfaction + "(" + satisfactionCount + "/" + disappointmentCount
                + ") , AmountDownloaded: " + amountDownloaded + " , AmountUploaded: " + amountUploaded + " , DownloadCount: " + downloadCount
                + " , UploadCount: " + uploadCount + " , Requests Honored: " + requestHonouredCount);
    }

    public void showSharedFiles() {
        System.out.println("TOTAL FILES : " + sharedFiles.size());
        for (SharedFile sharedFile : sharedFileList) {
            sharedFile.showState();
        }
    }

    public void convertToList() {
        sharedFileList = Collections.synchronizedList(new ArrayList<SharedFile>(sharedFiles.values()));
    }

    public int getCurrentUsedStorage() {
        int currentUsedStorage = 0;

        for (SharedFile sharedFile : sharedFiles.values()) {
            currentUsedStorage+=sharedFile.getSize();
        }
        return currentUsedStorage;
    }
}
