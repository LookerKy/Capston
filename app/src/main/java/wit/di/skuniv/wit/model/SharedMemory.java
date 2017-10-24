package wit.di.skuniv.wit.model;

/**
 * Created by Ky on 2017-10-25.
 */

public class SharedMemory {
    private static SharedMemory sharedMemory = null;
    public static synchronized SharedMemory getInstance() {
        if (sharedMemory == null) {
            sharedMemory = new SharedMemory();
        }
        return sharedMemory;
    }
    private String resultString;

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }
}
