package util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

/**
 * js 使用了双语言，看源代码的时候麻烦，这里直接替换成中文
 */
public class ReplaceReact {


    public static void main(String[] args) throws IOException {

        File src = new File("D:\\ws2\\ureport2-react\\src");



        List<File> jsList = FileUtil.loopFiles(src, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getAbsolutePath().contains("test")) {
                    return false;
                }
                if (pathname.getAbsolutePath().contains("bundle")) {
                    return false;
                }
                return pathname.getName().endsWith("js");
            }
        });

        for (File file : jsList) {
              replaceContent(file);

        }

        System.out.println(replaceContent("<ul className=\"dropdown-menu\" role=\"menu\" style=\"padding: 1px\" ></ul>`"));

        System.out.println(replaceContent("<div className=\"tree\" style=\"margin-left: 10px\" focus></div>"));
        System.out.println(replaceContent(" <i className=\"glyphicon glyphicon-circle-arrow-down\"\n" +
                                          "                style=\"color:#9E9E9E;font-size: 16px;vertical-align: middle;cursor: pointer;float: right;margin: 10px 10px 0px 0px;\"\n" +
                                          "                title=\"点击显示/隐藏属性面板\">\n" +
                                          "             </i>"));

    }


    // ${window.i18n.dialog.springDS.exist}
    public static void replaceContent(File file) {
        System.out.println(file.getName());

        String str = FileUtil.readUtf8String(file);


        str = replaceContent(str);


        FileUtil.writeUtf8String(str, file);

    }

    private static String replaceContent(String str) {
        StringBuilder sb = new StringBuilder();


        char[] charArray = str.toCharArray();
        int beginIndex = -1;
        int endIndex = -1;

        char beginChar = 0;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            sb.append(c);

            if (sb.toString().endsWith(" style=\"") || sb.toString().endsWith("style=‘")) {
                beginIndex = i;
                beginChar = c;
                continue;
            }

            if (beginIndex > 0) {
                if (c == beginChar) {
                    endIndex = i;

                    break;
                }
            }
        }

        if (beginIndex > 0 && endIndex > 0) {
            String style = str.substring(beginIndex, endIndex + 1);

            if(!style.contains("${")){
                String pureStyle = style.substring(1, style.length() - 1);
                String newStyle = replaceStyle(pureStyle);
                str = str.replace(style, "{{" + newStyle + "}}");

                str = replaceContent(str);
            }
        }


        return str;
    }


    // color:#9E9E9E;font-size: 16px;vertical-align: middle;cursor: pointer;float: right;margin: 10px 10px 0px 0px;
    private static String replaceStyle(String style) {
        StringBuilder sb = new StringBuilder();

        String[] arr = style.split(";");

        for (String kv : arr) {
            System.out.println(kv);
            String[] kvArr = kv.split(":");
            String k = kvArr[0].trim();
            String v = kvArr[1].trim();

            // 驼峰
            k = StrUtil.toCamelCase(k, '-');

            sb.append(k).append(":");

            sb.append('"').append(v).append('"');
            sb.append(",");
        }

        sb.deleteCharAt(sb.length() - 1);

        String rs = sb.toString();
        return rs;


    }


}
