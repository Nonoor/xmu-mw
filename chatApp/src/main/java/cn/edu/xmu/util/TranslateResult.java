package cn.edu.xmu.util;

public class TranslateResult {String src;
    String dst;

    public String getSrc() {
        return src;
    }
    public void setSrc(String src) {
        this.src = src;
    }
    public String getDst() {
        return dst;
    }
    public void setDst(String dst) {
        this.dst = dst;
    }
    @Override
    public String toString() {
        return "TransalateResult [src=" + src + ", dst=" + dst + "]";
    }
}
