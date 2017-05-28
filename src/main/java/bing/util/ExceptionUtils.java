package bing.util;

/**
 * 异常工具类
 *
 * @author IceWee
 */
public class ExceptionUtils {

    /**
     * 由异常堆栈信息生成字符串
     *
     * @param e
     * @return
     */
    public static String createExceptionString(Exception e) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(e.toString());
        buffer.append('\t');
        for (int i = 0; i < e.getStackTrace().length; ++i) {
            buffer.append(e.getStackTrace()[i]);
            buffer.append('\n');
        }
        return buffer.toString();
    }

}
