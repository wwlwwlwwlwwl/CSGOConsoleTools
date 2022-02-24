package cn.wwl.radio.adnmb.bean;

public class AdnmbReply extends AdnmbPost {
    public static final AdnmbReply NO_MORE_REPLY = new AdnmbReply();
    private AdnmbThread targetThread;

    public AdnmbThread getTargetThread() {
        return targetThread;
    }

    public AdnmbReply setTargetThread(AdnmbThread targetThread) {
        this.targetThread = targetThread;
        return this;
    }
}
