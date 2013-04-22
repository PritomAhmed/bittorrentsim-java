import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: pritom
 * Date: 4/5/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public static final int MAX_FILE_SIZE = 5000;
    public static final int MAX_NO_OF_FILES = 1000;
    public static final int MAX_NO_OF_PEERS = 50;
    public static final int FILE_AVAILABILITY_MODULO = 100;
    public static final int MAX_NO_OF_ACTIONS = 3;
    public static final int MAX_NO_OF_ROUNDS_PER_PEER = 2000;
    public static final float MIN_SHARE_RATIO = 0.5f;
    public static final int FILE_SHARE_PROBABILITY = 50;
    public static final int RESEED_BONUS_FACTOR = 5;

    public static final Map<Integer, Integer> bandwidthMap;
    public static final Map<Integer, Integer> storageCapMap;

    static {
        Map bandMap = new HashMap<Integer, Integer>();
        bandMap.put(0, 128);
        bandMap.put(1, 256);
        bandMap.put(2, 512);
        bandMap.put(3, 768);
        bandMap.put(4, 1024);
        bandMap.put(5, 1536);
        bandMap.put(6, 2048);
        bandwidthMap = Collections.unmodifiableMap(bandMap);
    }

    static {
        Map storageMap = new HashMap<Integer, Integer>();

        storageMap.put(0,100000);
        storageMap.put(1,200000);
        storageMap.put(2,500000);
        storageMap.put(3,1000000);
        storageMap.put(4,2000000);
        storageMap.put(5,5000000);
        storageMap.put(6,10000000);


        storageCapMap = Collections.unmodifiableMap(storageMap);
    }



    public static void main(String[] args) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Map<Integer, TrackedFile> trackedFileMap = TorrentSimUtils.initializeTrackedFiles();
        Map<Integer, Peer> mapOfPeers = TorrentSimUtils.createPeerMap();
        TorrentSimUtils.distributeFilesAmongPeers(trackedFileMap, mapOfPeers);
        //displayFileSeeders(trackedFileMap);

        Tracker tracker = new Tracker();
        tracker.setAvailableFiles(trackedFileMap);
        tracker.setRequestedFiles(new ConcurrentHashMap<Integer, TrackedFile>());

        for (Peer peer : mapOfPeers.values()) {
            peer.setTracker(tracker);
        }

        for (Peer peer : mapOfPeers.values()) {
            peer.getThread().start();
        }

        for (Peer peer : mapOfPeers.values()) {
            try {
                peer.getThread().join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (Peer peer : mapOfPeers.values()) {
            peer.showFinalState();
        }

        float totalSatisfaction =  TorrentSimUtils.calculateTotalSatisfaction(mapOfPeers);

        System.out.println("Total Satisfaction: " + totalSatisfaction);

        TorrentSimUtils.writeResultToFile(totalSatisfaction);
    }




}
