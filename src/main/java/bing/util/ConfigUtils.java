package bing.util;

import bing.Constants;
import bing.bean.Config;
import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
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
    
    @SuppressWarnings("all")
    public static Config getConfig() {
        final Config config = new Config();
        Stream<String> stream = null;
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            String configPath = classLoader.getResource(Constants.CONFIG_FILE_PATH).getFile();
            configPath = URLDecoder.decode(configPath, Constants.ENCODING_UTF8);
            File configFile = new File(configPath);
            configPath = configFile.getAbsolutePath();
            stream = Files.lines(Paths.get(configPath)).filter(line -> !StringUtils.startsWith(line, "#") && StringUtils.isNotBlank(line)).map(String::trim);
            stream.forEach(line -> {           
                LOGGER.debug(line);
                String[] array = StringUtils.split(line, "=");
                String value = array[1];
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
                } else if (StringUtils.startsWith(line, Constants.CONFIG_ORDER_SOURCE)) {
                    config.setOrderSource(value);
                }
            });
        } catch (Exception e) {
            String error = ExceptionUtils.createExceptionString(e);
            LOGGER.error("加载配置文件[config.ini]时出现了异常...\n{}", error);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return config;
    }

}
