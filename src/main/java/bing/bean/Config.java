package bing.bean;

/**
 * 配置文件
 *
 * @author IceWee
 */
public class Config {

    private String startMonth; // yyyy-MM
    private String endMonth; // yyyy-MM

    public Config() {
        super();
    }

    public String getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(String startMonth) {
        this.startMonth = startMonth;
    }

    public String getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(String endMonth) {
        this.endMonth = endMonth;
    }

}
