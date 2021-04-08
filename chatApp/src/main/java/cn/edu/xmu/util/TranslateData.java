package cn.edu.xmu.util;

import java.util.List;

public class TranslateData {
    String from;
    String to;
    List <TranslateResult> trans_result;

    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public List<TranslateResult> getTrans_result() {
        return trans_result;
    }
    public void setTrans_result(List<TranslateResult> trans_result) {
        this.trans_result = trans_result;
    }

    @Override
    public String toString() {
        return "TranslateData [from=" + from + ", to=" + to + ", trans_result=" + trans_result + "]";
    }

}