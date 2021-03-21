package cn.edu.xmu.constant;


/**
 * @author zhibin lan
 * @date 2021-03-21
 */
public enum ChatType {
    USER("user"),
    GROUP("group");
    private String type;

    ChatType(String type){
        this.type = type;
    }
    public String getType(){
        return this.type;
    }

}
