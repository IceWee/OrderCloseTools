package bing.thread;

import bing.AppUI;
import bing.bean.Config;
import bing.util.ConfigUtils;
import bing.util.DateUtils;
import bing.util.ExceptionUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务执行线程
 *
 * @author IceWee
 */
public class TaskThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskThread.class);

    private final AppUI app; // 用于回调
    private final String orderPath; // 订单文件路径
    private final String sqlPath; // SQL文件生成路径
    private List<String> allNewOrderNos = new ArrayList<>(); // 全部新订单号（本次处理）

    private static final String TABLE_NAME = "CRM_WS_ORDER";
    private static final String COLUMN_ORDER_NO = "ORDER_NO"; // VARCHAR,W20170526100254872730462
    private static final String COLUMN_ORDER_STATUS = "ORDER_STATUS"; // VARCHAR,7
    private static final String COLUMN_PAY_TYPE = "PAY_TYPE"; // VARCHAR,7
    private static final String COLUMN_ORDER_SOURCE = "ORDER_SOURCE"; // sz0528
    private static final String COLUMN_ORDER_DATE = "ORDER_DATE"; // DATETIME
    private static final String COLUMN_ARRIVAL_DATE = "ARRIVAL_DATE"; // DATETIME
    private static final String COLUMN_END_DATE = "END_DATE"; // DATETIME
    private static final String COLUMN_CREATED = "CREATED"; // DATETIME
    private static final String COLUMN_UPDATED = "UPDATED"; // DATETIME

    public TaskThread(AppUI app, String orderPath, String sqlPath) {
        this.app = app;
        this.orderPath = orderPath;
        this.sqlPath = sqlPath;
    }

    @Override
    public void run() {
        try {
            // 1.读取配置文件
            Config config = ConfigUtils.getConfig();
            if (config == null) {
                this.app.buttonsEnabled();
                return;
            }
            List<String> months = DateUtils.getMonthList(config.getStartMonth(), config.getEndMonth());
            LOGGER.info("起止月份解析完成...");
            // 2.遍历订单文件列表
            File[] orderFiles = getOrderFiles();
            List<String> sqls = new ArrayList<>();
            List<String> orderNos;
            for (File orderFile : orderFiles) {
                LOGGER.info("正在处理文件：{}", orderFile.getAbsolutePath());
                orderNos = getOrderNos(orderFile);
                sqls.addAll(getOrderCloseSQLs(orderNos, months, config));
            }
            // 3.生成查重SQL
            List<String> searchDupSQLs = getSearchDuplicateOrderNoSQLs();
            sqls.addAll(searchDupSQLs);
            sqls.add("--本次一共处理订单数：" + allNewOrderNos.size());
            // 4.写SQL文件
            String sqlFilePath = getSQLFilePath();
            writeSQLFile(sqls, sqlFilePath);
            this.app.completed();
        } catch (IOException | ParseException ex) {
            LOGGER.error("运行出错\n{}", ExceptionUtils.createExceptionString(ex));
            this.app.buttonsEnabled();
        }
    }

    /**
     * 获取查询本次产生的重复订单号查询语句
     *
     * @return
     */
    private List<String> getSearchDuplicateOrderNoSQLs() {
        List<String> sqls = new ArrayList<>();
        final int MAX_SIZE = 500;
        int total = allNewOrderNos.size();
        int times = total / MAX_SIZE;
        if (total % MAX_SIZE != 0) {
            times++;
        }
        int fromIndex = 0;
        int toIndex = 0;
        List<String> subOrderNos;
        for (int i = 0; i < times; i++) {
            toIndex = fromIndex + MAX_SIZE;
            if (toIndex >= total) {
                toIndex = total;
            }
            subOrderNos = allNewOrderNos.subList(fromIndex, toIndex);
            sqls.add(getSearchDuplicateOrderNoSQL(subOrderNos));
            fromIndex = fromIndex + MAX_SIZE;
        }
        return sqls;
    }

    private String getSearchDuplicateOrderNoSQL(List<String> orderNos) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ").append(COLUMN_ORDER_NO).append(" in (");
        String sql = StringUtils.join(orderNos.toArray(), "','");
        sql = "'" + sql + "'";
        builder.append(sql);
        builder.append(");\n");
        return builder.toString();
    }

    /**
     * 获取订单文件列表
     *
     * @return
     */
    private File[] getOrderFiles() {
        File orderDir = new File(orderPath);
        return orderDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        });
    }

    /**
     * 解析订单文件获得订单号列表
     *
     * @return
     */
    private List<String> getOrderNos(File orderFile) throws FileNotFoundException, IOException {
        List<String> orderNos = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(orderFile));
        String line = reader.readLine();
        while (line != null) {
            if (StringUtils.isNotBlank(line)) {
                orderNos.add(StringUtils.trim(line));
            }
            line = reader.readLine();
        }
        return orderNos;
    }

    /**
     * 根据订单号生成UPDATE语句列表
     *
     * @param orderNos
     * @param months
     * @param config
     * @return
     */
    private List<String> getOrderCloseSQLs(List<String> orderNos, List<String> months, Config config) throws ParseException {
        List<String> sqls = new ArrayList<>();
        int monthIndex = 0;
        int maxMonthIndex = months.size() - 1;
        String yyyyMM;
        for (String orderNo : orderNos) {
            if (monthIndex > maxMonthIndex) {
                monthIndex = 0;
            }
            yyyyMM = months.get(monthIndex);
            long date = DateUtils.randomLong(yyyyMM, config.getMinHour(), config.getMaxHour());
            sqls.add(getOrderCloseSQL(orderNo, date, config));
            monthIndex++;
        }
        return sqls;
    }

    /**
     * 获取订单闭环更新SQL
     *
     * @param orderNo
     * @param date
     * @param config
     * @return
     */
    private String getOrderCloseSQL(String orderNo, long date, Config config) {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ").append(TABLE_NAME).append(" SET ");
        builder.append(COLUMN_ORDER_STATUS).append(" = ").append("'7', "); // 状态：闭环
        builder.append(COLUMN_PAY_TYPE).append(" = ").append("'1', "); // 支付方式：1
        builder.append(COLUMN_ORDER_SOURCE).append(" = ").append("'sz0528', "); // 来源
        String orderDate = DateUtils.formatDateTime(date);
        builder.append(COLUMN_ORDER_DATE).append(" = ").append("'").append(orderDate).append("', "); // 订单时间
        long arrivalLong = DateUtils.nextRandomLong(date, config.getMaxSecondInterval() * 1000);
        String arrivalDate = DateUtils.formatDateTime(arrivalLong);
        builder.append(COLUMN_ARRIVAL_DATE).append(" = ").append("'").append(arrivalDate).append("', "); // 到货时间
        long endLong = DateUtils.nextRandomLong(arrivalLong, config.getMaxSecondInterval() * 1000);
        String endDate = DateUtils.formatDateTime(endLong);
        builder.append(COLUMN_END_DATE).append(" = ").append("'").append(endDate).append("', "); // 闭环时间
        builder.append(COLUMN_CREATED).append(" = ").append("'").append(orderDate).append("', "); // 创建时间
        builder.append(COLUMN_UPDATED).append(" = ").append("'").append(endDate).append("' "); // 修改时间
        builder.append("WHERE ").append(COLUMN_ORDER_NO).append(" = ").append("'").append(orderNo).append("';");
        // 修改订单号
        String newOrderNo = createNewOrderNo(orderNo, date);
        allNewOrderNos.add(newOrderNo);
        builder.append("\nUPDATE ").append(TABLE_NAME).append(" SET ");
        builder.append(COLUMN_ORDER_NO).append(" = ").append("'").append(newOrderNo).append("' ");
        builder.append("WHERE ").append(COLUMN_ORDER_NO).append(" = ").append("'").append(orderNo).append("';");
        return builder.toString();
    }

    /**
     * 替换订单号中日期部分生成新订单号
     *
     * @param orderNo
     * @param date
     * @return
     */
    private String createNewOrderNo(String orderNo, long date) {
        String prefix = "W";
        String suffix = StringUtils.substring(orderNo, 18);
        String dateString = DateUtils.formatOrderDate(date);
        return prefix + dateString + suffix;
    }

    /**
     * SQL文件绝对路径
     *
     * @return
     */
    private String getSQLFilePath() {
        String sqlFilePath = sqlPath + File.separator + "update-" + System.currentTimeMillis() + ".sql";
        LOGGER.debug("sql path : {}", sqlFilePath);
        return sqlFilePath;
    }

    /**
     * 生成SQL文件
     *
     * @param sqls
     * @param sqlFilePath
     */
    private void writeSQLFile(List<String> sqls, String sqlFilePath) {
        File file = new File(sqlFilePath);
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            for (String sql : sqls) {
                writer.write(sql);
                writer.newLine();
            }
        } catch (Exception ex) {
            LOGGER.error("生成SQL文件时出现了异常\n{}", ExceptionUtils.createExceptionString(ex));
            throw new RuntimeException(ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
        }
    }

}
