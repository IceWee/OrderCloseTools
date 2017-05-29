package bing;

/**
 * 常量���
 *
 * @author IceWee
 */
public class Constants {

    /**
     * 软件图标路径��·��
     */
    public static final String ICON_APP_PATH = "/bing/ui/images/app.png";

    /**
     * 复制图标路径
     */
    public static final String ICON_COPY_PATH = "/bing/ui/images/copy.png";

    /**
     * 剪切图标路径
     */
    public static final String ICON_CUT_PATH = "/bing/ui/images/cut.png";

    /**
     * 清空图标路径
     */
    public static final String ICON_CLEAR_PATH = "/bing/ui/images/clear.png";

    public static final String TEXT_COPY = "复制";
    public static final String TEXT_CUT = "剪切";
    public static final String TEXT_CLEAR = "清除";

    public static final String ENCODING_UTF8 = "UTF-8";

    /**
     * 配置文件路径
     */
    public static final String CONFIG_FILE_PATH = "config.ini";

    /**
     * 配置文件-开始月份
     */
    public static final String CONFIG_START_MONTH = "month.start";

    /**
     * 配置文件-终止月份
     */
    public static final String CONFIG_END_MONTH = "month.end";

    /**
     * 配置文件-最小小时
     */
    public static final String CONFIG_MIN_HOUR = "hour.min";

    /**
     * 配置文件-最大小时
     */
    public static final String CONFIG_MAX_HOUR = "hour.max";

    /**
     * 配置文件-最大时间差
     */
    public static final String CONFIG_MAX_SECOND_INTERVAL = "second.interval.max";

    /**
     * 配置文件-订单来源
     */
    public static final String CONFIG_ORDER_SOURCE = "order.source";

    public static final String ORDER_TABLE_NAME = "CRM_WS_ORDER"; // 订单表名
    public static final String COLUMN_ORDER_NO = "ORDER_NO"; // VARCHAR,W20170526100254872730462
    public static final String COLUMN_ORDER_STATUS = "ORDER_STATUS"; // VARCHAR,7
    public static final String COLUMN_PAY_TYPE = "PAY_TYPE"; // VARCHAR,1
    public static final String COLUMN_ORDER_SOURCE = "ORDER_SOURCE"; // sz0528
    public static final String COLUMN_ORDER_DATE = "ORDER_DATE"; // DATETIME
    public static final String COLUMN_ARRIVAL_DATE = "ARRIVAL_DATE"; // DATETIME
    public static final String COLUMN_END_DATE = "END_DATE"; // DATETIME
    public static final String COLUMN_CREATED = "CREATED"; // DATETIME
    public static final String COLUMN_UPDATED = "UPDATED"; // DATETIME

    public static final String ORDER_NO_PREFIX = "W"; // 订单号前缀
    public static final String ORDER_STATUS_CLOSED = "7"; // 订单状态：闭环
    public static final String ORDER_PAY_TYPE = "1"; // 支付方式：现金

    public Constants() {
        super();
    }

}
