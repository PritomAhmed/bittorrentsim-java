import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: pritom
 * Date: 4/19/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TorrentSimUtils {

    public static void displayFileSeeders(Map<Integer, TrackedFile> trackedFileMap) {
        for (int filedId : trackedFileMap.keySet()) {
            TrackedFile trackedFile = trackedFileMap.get(filedId);
            trackedFile.showSeeders();
        }
    }

    public static void distributeFilesAmongPeers(Map<Integer, TrackedFile> trackedFileMap, Map<Integer, Peer> mapOfPeers) {
        Random moduloGenerator = new Random();

        for (int fileId = 1; fileId <= Main.MAX_NO_OF_FILES; fileId++) {

            int outerCoinToss = moduloGenerator.nextInt(Main.FILE_AVAILABILITY_MODULO);
            if (outerCoinToss >= Main.FILE_SHARE_PROBABILITY) {
                int numberOfSharingPeers = moduloGenerator.nextInt(Main.MAX_NO_OF_PEERS);

                for (int counter = 0; counter < numberOfSharingPeers; counter++) {
                    Integer peerId = moduloGenerator.nextInt(Main.MAX_NO_OF_PEERS) + 1;

                    Peer peer = mapOfPeers.get(peerId);
                    int innerCoinToss = moduloGenerator.nextInt(Main.FILE_AVAILABILITY_MODULO);
                    if (innerCoinToss >= Main.FILE_SHARE_PROBABILITY && peer != null) {
                        TrackedFile trackedFile = trackedFileMap.get(fileId);
                        if (trackedFile != null && trackedFile.getSeeders() != null) {

                            int possibleStorageUsed = peer.getCurrentUsedStorage() + trackedFile.getFileSize();
                            if (possibleStorageUsed <= peer.getStorageCapacity()) {
                                trackedFile.getSeeders().put(peer.getId(), peer);

                                SharedFile file = new SharedFile();
                                file.setId(fileId);
                                file.setSize(trackedFile.getFileSize());
                                file.setUploadedSize(0);
                                peer.getSharedFiles().put(fileId, file);
                            }


                        }
                    }
                }
            }
        }

        /*for (Peer peer : mapOfPeers.values()) {
            for (int fileId = 1; fileId <= MAX_NO_OF_FILES; fileId++) {
                if (fileId % (moduloGenerator.nextInt(FILE_AVAILABILITY_MODULO) + 1) == 0 && peer != null) {
                    TrackedFile trackedFile = trackedFileMap.get(fileId);
                    if (trackedFile != null && trackedFile.getSeeders() != null) {
                        trackedFile.getSeeders().put(peer.getId(), peer);

                        SharedFile file = new SharedFile();
                        file.setId(fileId);
                        file.setSize(trackedFile.getFileSize());
                        file.setUploadedSize(0);
                        peer.getSharedFiles().put(fileId, file);
                    }
                }
            }
        }*/
    }

    public static Map<Integer, Peer> createPeerMap() {
        Map<Integer, Peer> mapOfPeers = new LinkedHashMap<Integer, Peer>();
        Random bandwidthChooser = new Random();
        Random storageCapChooser = new Random();

        for (int peerId = 1; peerId <= Main.MAX_NO_OF_PEERS; peerId++) {
            Peer peer = new Peer();
            peer.setId(peerId);
            peer.setSharedFiles(new ConcurrentHashMap<Integer, SharedFile>());

            int bandwidthMapIndex = bandwidthChooser.nextInt(Main.bandwidthMap.size());
            int storageMapCapIndex = storageCapChooser.nextInt(Main.storageCapMap.size());

            peer.setUploadSpeed(Main.bandwidthMap.get(bandwidthMapIndex));
            peer.setDownloadSpeed(Main.bandwidthMap.get(bandwidthMapIndex));
            peer.setAmountDownloaded(Main.MAX_FILE_SIZE / 5);
            peer.setAmountUploaded(Main.MAX_FILE_SIZE);
            peer.setShareRatio(5.0f);

            peer.setStorageCapacity(Main.storageCapMap.get(storageMapCapIndex));

            mapOfPeers.put(peerId, peer);
        }
        return mapOfPeers;
    }

    public static Map<Integer, TrackedFile> initializeTrackedFiles() {
        Random fileSizeGenerator = new Random();
        Map<Integer, TrackedFile> trackedFileMap = new ConcurrentHashMap<Integer, TrackedFile>();

        for (int fileId = 1; fileId <= Main.MAX_NO_OF_FILES; fileId++) {
            int fileSize = fileSizeGenerator.nextInt(Main.MAX_FILE_SIZE) + 1;

            TrackedFile trackedFile = new TrackedFile();
            trackedFile.setId(fileId);
            trackedFile.setFileSize(fileSize);
            trackedFile.setSeeders(new ConcurrentHashMap<Integer, Peer>());

            trackedFileMap.put(fileId, trackedFile);
        }
        return trackedFileMap;
    }

    public static float calculateTotalSatisfaction(Map<Integer, Peer> mapOfPeers) {
        float totalSatisfaction = 0.0f;

        for (Peer peer : mapOfPeers.values()) {
            totalSatisfaction += peer.getSatisfaction();
        }

        return totalSatisfaction;
    }

    public static void writeResultToFile(float totalSatisfaction) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("resultWithHonourRequest.csv", true));
            writer.newLine();
            StringBuilder resultString = new StringBuilder();
            resultString.append(new Date().toString() + ",");
            resultString.append(Main.MAX_NO_OF_FILES + ",");
            resultString.append(Main.MAX_NO_OF_PEERS + ",");
            resultString.append(Main.MIN_SHARE_RATIO + ",");
            resultString.append(Main.MAX_NO_OF_ROUNDS_PER_PEER + ",");
            resultString.append(totalSatisfaction);

            writer.append(resultString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
