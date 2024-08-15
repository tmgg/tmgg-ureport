package util;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * js 使用了双语言，看源代码的时候麻烦，这里直接替换成中文
 */
public class ReplaceJsI18n {

    static Map<String, String> finalDict = new TreeMap<>();

    public static void main(String[] args) throws IOException {
        File root = new File(".");

        File src = new File(root, "ureport2-js/src");

        System.out.println(src.getAbsolutePath());


        String designer = FileUtil.readUtf8String(new File(src, "i18n/designer.json"));


        Map<String, Object> designerDict = JsonTool.jsonToMap(designer);


        String preview = FileUtil.readUtf8String(new File(src, "i18n/preview.json"));

        Map<String, Object> previewDict = JsonTool.jsonToMap(preview);
        parseProperties("", designerDict);
        parseProperties("", previewDict);

        System.out.println(JsonTool.toPrettyJsonQuietly(finalDict));

        List<File> jsList = FileUtil.loopFiles(src, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("js");
            }
        });

        for (File file : jsList) {
            replace(file);

        }
    }

    public static void parseProperties(String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            String fullKey = prefix + key;
            if (value instanceof String) {
                finalDict.put(fullKey, (String) value);
            }
            if (value instanceof Map) {
                Map<String, Object> vmap = (Map<String, Object>) value;
                parseProperties(fullKey + ".", vmap);
            }
        }
    }

    // ${window.i18n.dialog.springDS.exist}
    public static void replace(File file) {
        System.out.println(file.getName());

        String content = FileUtil.readUtf8String(file);
        System.out.println(content);

        for (Map.Entry<String, String> e : finalDict.entrySet()) {
            content = content.replace("${window.i18n." + e.getKey() + "}", e.getValue());
        }

        System.err.println(content);

        FileUtil.writeUtf8String(content,file);

    }

}
