package bing.bean;

/**
 * 配置文件
 *
 * @author IceWee
 */
public class Config {

    private String startMonth; // yyyy-MM
    private String endMonth; // yyyy-MM
    private int minHour;
    private int maxHour;
    private int maxSecondInterval;

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

    public int getMinHour() {
        return minHour;
    }

    public void setMinHour(int minHour) {
        this.minHour = minHour;
    }

    public int getMaxHour() {
        return maxHour;
    }

    public void setMaxHour(int maxHour) {
        this.maxHour = maxHour;
    }

    public int getMaxSecondInterval() {
        return maxSecondInterval;
    }

    public void setMaxSecondInterval(int maxSecondInterval) {
        this.maxSecondInterval = maxSecondInterval;
    }

}
