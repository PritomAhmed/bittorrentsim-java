import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: pritom
 * Date: 4/5/13
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrackedFile {
    private int id;
    private int fileSize;
    private Map<Integer, Peer> seeders;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public Map<Integer, Peer> getSeeders() {
        return seeders;
    }

    public void setSeeders(Map<Integer, Peer> seeders) {
        this.seeders = seeders;
    }

    public void showSeeders() {
        System.out.println("File Id: " + id + " TotalUploadSpeed: " + getTotalUploadSpeed());
        for (int seederId : seeders.keySet()) {
            System.out.println("        Seeder Id: " + seederId);
        }
    }

    public int getTotalUploadSpeed() {
        int totalUploadSpeed = 0;

        for (Peer seeder : seeders.values()) {
            totalUploadSpeed += seeder.getUploadSpeed();
        }

        return totalUploadSpeed;
    }

    public boolean isRare() {
        int rarenessThreshold = Main.MAX_NO_OF_PEERS / Main.PEER_RARENESS_FACTOR;
        int numberOfPeers = seeders.size();
        return numberOfPeers < rarenessThreshold;
    }
}
