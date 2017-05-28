package bing.util;

import bing.Constants;
import bing.bean.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置加载
 *
 * @author IceWee
 */
public class ConfigUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    public static Config getConfig() {
        Config config = null;
        BufferedReader reader = null;
        try {
            config = new Config();
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            String configPath = classLoader.getResource(Constants.CONFIG_FILE_PATH).getFile();
            configPath = URLDecoder.decode(configPath, Constants.ENCODING_UTF8);
            File configFile = new File(configPath);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), Constants.ENCODING_UTF8));
            String line, value;
            String[] array;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug(line);
                if (StringUtils.startsWith(line, "#")) { // #为注释
                    continue;
                }
                array = StringUtils.split(line, "=");
                value = array[1];
                if (StringUtils.startsWith(line, Constants.CONFIG_START_MONTH)) {
                    config.setStartMonth(value);
                } else if (StringUtils.startsWith(line, Constants.CONFIG_END_MONTH)) {
                    config.setEndMonth(value);
                } else if (StringUtils.startsWith(line, Constants.CONFIG_MIN_HOUR)) {
                    config.setMinHour(Integer.parseInt(value));
                } else if (StringUtils.startsWith(line, Constants.CONFIG_MAX_HOUR)) {
                    config.setMaxHour(Integer.parseInt(value));
                } else if (StringUtils.startsWith(line, Constants.CONFIG_MAX_SECOND_INTERVAL)) {
                    config.setMaxSecondInterval(Integer.parseInt(value));
                }
            }
        } catch (IOException e) {
            config = null;
            String error = ExceptionUtils.createExceptionString(e);
            LOGGER.error("加载配置文件[config.ini]时出现了异常...\n{}", error);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return config;
    }

}
