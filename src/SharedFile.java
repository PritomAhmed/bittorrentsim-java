/**
 * Created with IntelliJ IDEA.
 * User: pritom
 * Date: 4/5/13
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class SharedFile implements Comparable {

    private int id;
    private int size;
    private float uploadedSize;
    private boolean requested;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getUploadedSize() {
        return uploadedSize;
    }

    public void setUploadedSize(float uploadedSize) {
        this.uploadedSize = uploadedSize;
    }

    public boolean isRequested() {
        return requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }

    @Override
    public int compareTo(Object o) {
        SharedFile comparedFile = (SharedFile) o;
        float ownUploadRatio = uploadedSize/(float)size;
        float comparedFileUploadRatio = comparedFile.getUploadedSize()/(float)comparedFile.getSize();
        return Float.compare(ownUploadRatio, comparedFileUploadRatio);
    }

    public void showState() {
        System.out.println("Id: " + id +" Size: " + size +" Uploaded: " + uploadedSize + " Ratio: " + uploadedSize/size);
    }
}
