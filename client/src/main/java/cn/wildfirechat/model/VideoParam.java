package cn.wildfirechat.model;

/**
 * @ClassName VideoParam
 * @Description 小视频的参数 ，宽 高，时长
 * @Author dhl
 * @Date 2021/1/7 13:59
 * @Version 1.0
 */
public class VideoParam {

    private final int width ;
    private final int height;
    private final long duration ;
    private final byte[] thumbnailBytes ;
        public VideoParam(int width, int height, long duration, byte[] thumbnailBytes ){
            this.width = width ;
            this.height = height;
            this.duration = duration ;
            this.thumbnailBytes = thumbnailBytes ;
        }


    public long getDuration() {
        return duration;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public byte[] getThumbnailBytes() {
        return thumbnailBytes;
    }


}
