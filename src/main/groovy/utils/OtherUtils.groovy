package utils
@Grab(group = 'com.googlecode.soundlibs', module = 'jlayer', version = '1.0.1-1')
import javazoom.jl.player.Player

class OtherUtils {
    public static void playSound(String fileName) {
        new Player(new BufferedInputStream(Class.getResourceAsStream(fileName))).play()
    }

    public static void playMoney() {
        playSound("/Money.mp3")
    }

    public static void playEmergency() {
        playSound('/emergency.mp3')
    }

    public static double roundWithPrecision(double value, int precision) {
        Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision)
    }

    public static File getFile(String fileName) {
        def defLocationFileName = '/path.txt'
        def pathToResourceFolder = this.getResource(defLocationFileName).getPath().split(defLocationFileName)[0]
        def outputFile = new File(pathToResourceFolder + fileName)
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }
        outputFile
    }

}
