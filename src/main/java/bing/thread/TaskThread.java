package bing.thread;

import bing.AppUI;
import bing.Constants;
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
    private final String orderPath; // 订单文件路径或目录
    private final String sqlPath; // SQL文件生成路径
    private List<String> allNewOrderNos = new ArrayList<>(); // 全部新订单号（本次处理）

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
            List<String> sqls;
            List<String> orderNos;
            for (File orderFile : orderFiles) {
                LOGGER.info("正在处理文件：{}", orderFile.getAbsolutePath());
                allNewOrderNos = new ArrayList<>();
                orderNos = getOrderNos(orderFile);
                sqls = getOrderCloseSQLs(orderNos, months, config);
                // 1.生成查重SQL
                sqls.add("\n\n\n--本次一共处理订单数：" + allNewOrderNos.size());
                List<String> searchDupSQLs = getSearchDuplicateOrderNoSQLs();
                sqls.add("\n--查询本次生成订单SQL，如结果数与本次处理订单数[" + allNewOrderNos.size() + "]相同说明自动生成的订单号没有重复\n");
                sqls.addAll(searchDupSQLs);
                // 2.写SQL文件
                String sqlFilePath = getSQLFilePath(orderFile.getName());
                writeSQLFile(sqls, sqlFilePath);
            }
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
        builder.append("SELECT * FROM ").append(Constants.ORDER_TABLE_NAME).append(" WHERE ").append(Constants.COLUMN_ORDER_NO).append(" in (");
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
        File orderFile = new File(orderPath);
        if (orderFile.exists()) {
            if (orderFile.isFile()) { // 文件
                return new File[]{orderFile};
            } else { // 目录
                return orderFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile();
                    }
                });
            }
        } else {
            throw new RuntimeException("订单文件路径或目录非法");
        }
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
        builder.append("UPDATE ").append(Constants.ORDER_TABLE_NAME).append(" SET ");
        builder.append(Constants.COLUMN_ORDER_STATUS).append(" = ").append("'").append(Constants.ORDER_STATUS_CLOSED).append("', "); // 状态：闭环
        builder.append(Constants.COLUMN_PAY_TYPE).append(" = ").append("'").append(Constants.ORDER_PAY_TYPE).append("', "); // 支付方式：1
        builder.append(Constants.COLUMN_ORDER_SOURCE).append(" = ").append("'").append(config.getOrderSource()).append("', "); // 来源
        String orderDate = DateUtils.formatDateTime(date);
        builder.append(Constants.COLUMN_ORDER_DATE).append(" = ").append("'").append(orderDate).append("', "); // 订单时间
        long arrivalLong = DateUtils.nextRandomLong(date, config.getMaxSecondInterval() * 1000);
        String arrivalDate = DateUtils.formatDateTime(arrivalLong);
        builder.append(Constants.COLUMN_ARRIVAL_DATE).append(" = ").append("'").append(arrivalDate).append("', "); // 到货时间
        long endLong = DateUtils.nextRandomLong(arrivalLong, config.getMaxSecondInterval() * 1000);
        String endDate = DateUtils.formatDateTime(endLong);
        builder.append(Constants.COLUMN_END_DATE).append(" = ").append("'").append(endDate).append("', "); // 闭环时间
        builder.append(Constants.COLUMN_CREATED).append(" = ").append("'").append(orderDate).append("', "); // 创建时间
        builder.append(Constants.COLUMN_UPDATED).append(" = ").append("'").append(endDate).append("' "); // 修改时间
        builder.append("WHERE ").append(Constants.COLUMN_ORDER_NO).append(" = ").append("'").append(orderNo).append("';");
        // 修改订单号
        String newOrderNo = createNewOrderNo(orderNo, date);
        allNewOrderNos.add(newOrderNo);
        builder.append("\nUPDATE ").append(Constants.ORDER_TABLE_NAME).append(" SET ");
        builder.append(Constants.COLUMN_ORDER_NO).append(" = ").append("'").append(newOrderNo).append("' ");
        builder.append("WHERE ").append(Constants.COLUMN_ORDER_NO).append(" = ").append("'").append(orderNo).append("';");
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
        String suffix = StringUtils.substring(orderNo, 18);
        String dateString = DateUtils.formatOrderDate(date);
        return Constants.ORDER_NO_PREFIX + dateString + suffix;
    }

    /**
     * SQL文件绝对路径
     *
     * @return
     */
    private String getSQLFilePath(String prefix) {
        String sqlFilePath = sqlPath + File.separator + prefix + "-" + System.currentTimeMillis() + ".sql";
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
            writer = new BufferedWriter(new OutputStreamWriter(fos, Constants.ENCODING_UTF8));
            for (String sql : sqls) {
                writer.write(sql);
                writer.newLine();
            }
        } catch (IOException ex) {
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
