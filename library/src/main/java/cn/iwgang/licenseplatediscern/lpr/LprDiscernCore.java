package cn.iwgang.licenseplatediscern.lpr;

public class LprDiscernCore {

    static {
        System.loadLibrary("lprcore");
    }


    public static native long init(String casacde_detection,
                                   String finemapping_prototxt, String finemapping_caffemodel,
                                   String segmentation_prototxt, String segmentation_caffemodel,
                                   String charRecognization_proto, String charRecognization_caffemodel,
                                   String segmentation_free_prototxt, String segmentation_free_caffemodel);

    public static native String discern(long inputMat, long object);

}
