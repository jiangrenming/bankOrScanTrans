package com.nld.cloudpos.util.print;

import android.content.Context;
import com.nld.logger.LogUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import common.StringUtil;

/**
 * 脚本打印命令工具类
 *
 * @author chenkh
 */
public class PrintUtils {

    static private Element root;

    /**
     * 载入
     *
     * @param context
     * @param fileName 文件路径
     */
    public static void load(Context context, String fileName) {

        Document dom = null;
        try {
            InputStream ins = context.getResources().getAssets().open(fileName);
            if (ins == null) {
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//创建DOM解析工厂
            DocumentBuilder dombuild = factory.newDocumentBuilder();//创建DON解析器
            dom = dombuild.parse(ins);//开始解析XML文档并且得到整个文档的对象模型
        } catch (Exception e) {
            e.printStackTrace();
        }

        root = dom.getDocumentElement();
    }

    /**
     * 组装打印数据
     * 按xml对应的模板格式，组装打印数据
     *
     * @param model
     * @param printerModel
     * @return 打印数据
     */
    public static String getPrintData(String model, Object printerModel) {

        PrinterXmlParse printerXmlParse = PrinterXmlParse.getInstance();

        try {
            printerXmlParse.parse(model, printerModel);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (printerXmlParse.size() == 0) {
            return null;
        }
        StringBuffer cmdData = new StringBuffer();
        Mode mode = null;
        for (Object obj : printerXmlParse) {
            if (obj instanceof Mode) {
                mode = (Mode) obj;
                cmdData.append(PrintUtils.getNLFont(mode.getFontSize()));
                if (StringUtil.isDigital(mode.getLineHeight())) {
                    cmdData.append(PrintUtils.getLineSpacing(Integer
                            .valueOf(mode.getLineHeight())));
                }
            } else if (obj instanceof String) {
                if (((String) obj).startsWith("!image!")) { // 图片数据
                    cmdData.append((String) obj);
                } else {                                    // 字符串数据
                    if (mode == null) {
                        mode = new Mode();
                        mode.setAlign(AlignConst.LEFT);
                    }
                    cmdData.append(PrintUtils.getPrintText(
                            PrintUtils.changeToAlignModel(mode.getAlign()), (String) obj));
                }
            }

            //PRINT_CMD_SPACE_FLAG = "!r!n";
            cmdData.append("!r!n");
        }

        return cmdData.toString();
    }

    /**
     * 组装打印数据,打印结束控制走纸
     * 按xml对应的模板格式，组装打印数据
     *
     * @param model
     * @param printerModel
     * @return
     */
    public static String getPrintDataEndLine(String model, Object printerModel) {

        return getPrintData(model, printerModel) + "!NLPRNOVER\n";
    }

//    public static byte[] getPrintDataBT(Object printOrderModel, String model) {
//        EscCommandParse escCommandParse = new EscCommandParse();
//        byte[] bytes = escCommandParse.parse(model,printOrderModel,root);
//        escCommandParse = null;
//        return bytes;
//    }


//    public static ArrayList<byte[]> getPrintDataBT(Object printerModel) {
//        ArrayList<byte[]> bytes = new ArrayList<byte[]>();
//
//        bytes.add(GPrinterCommand.reset);
//        try {
//            String message = "蓝牙打印测试\n蓝牙打印测试\n蓝牙打印测试\n\n";
//            bytes.add(message.getBytes("gbk"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        bytes.add(GPrinterCommand.print);
//        bytes.add(GPrinterCommand.print);
//        bytes.add(GPrinterCommand.print);
//        return bytes;
//
//
//    }

    /**
     * 将配置文件中mode转为打印指令
     *
     * @return
     */
    private static String getNLFont(String fontSize) {
        if (StringUtil.isEmpty(fontSize)) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("!NLFONT ");
        buffer.append(fontSize + "\n");
        return buffer.toString();
    }

    /**
     * 行间距命令
     *
     * @param spacing 行间距[0，60], 默认为6
     * @return
     */
    private static String getLineSpacing(int spacing) {
        if (spacing < 0 || spacing > 60) {
            spacing = 6;
        }
        return "!yspace " + spacing + "\n";
    }

    /**
     * 条码样式命令
     *
     * @param width  单条条码宽度[1,8], 默认为2
     * @param height 期望高度[1,320], 默认为64, 必须为8的倍数
     * @return
     */
    private static String getBarCodeStyle(int width, int height) {
        if (width < 1 || width > 8) {
            width = 2;
        }

        if (height > 0 && height < 321) {
            height -= height % 8;
        } else {
            height = 64;
        }
        return "!barcode " + width + " " + height + "\n";
    }

    /**
     * 二维码样式命令
     *
     * @param height 期望高度，不超过边界， 默认为100
     * @param level  纠错级别[0, 3] 默认为2
     * @return
     */
    private static String getTwoDimensionCode(int height, int level) {
        if (height <= 0) {
            height = 100;
        }
        if (level > 4 || level < 0) {
            level = 2;
        }
        return "!qrcode " + height + " " + level + "\n";
    }

    /**
     * 打印文字
     *
     * @param alignModel 显示位置（对齐方式）
     * @param text       文字
     * @return
     */
    private static String getPrintText(AlignModel alignModel, String text) {
        return "*text " + getAlignModelCommond(alignModel) + " " + text + "\n";
    }

    /**
     * 打印条码
     *
     * @param alignModel 显示位置（对齐方式）
     * @param data       数据
     * @return
     */
    private static String getPrintBarCode(AlignModel alignModel, String data) {
        return "*barcode " + getAlignModelCommond(alignModel) + " " + data + "\n";
    }

    /**
     * 打印二维码
     *
     * @param alignModel 显示位置（对齐方式）
     * @param data       数据
     * @return
     */
    private static String getPrintTwoDimensionCode(AlignModel alignModel, String data) {
        return "*qrcode " + getAlignModelCommond(alignModel) + " " + data + "\n";
    }

    /**
     * 打印图像，图像大小为width*height，如128*64
     *
     * @param alignModel 显示位置（对齐方式）
     * @param width      宽度 最小为0
     * @param height     高度 最小为0
     * @param data       数据 两种方式传入：
     *                   "#logo" 寻找设备中标识为logo的图像数据
     *                   "data: base64;..." base64数据编码
     * @return
     */
    private static String getPrintImage(AlignModel alignModel, int width, int height, String data) {
        if (width < 0) {
            width = 0;
        }
        if (height < 0) {
            height = 0;
        }
        return "*image " + getAlignModelCommond(alignModel) + " " + width + "*" + height + " " + data + "\n";
    }

    private static String getAlignModelCommond(AlignModel alignModel) {
        String commond = "l"; // 默认居左
        switch (alignModel) {
            case LEFT:
                commond = "l";
                break;
            case CENTER:
                commond = "c";
                break;
            case RIGHT:
                commond = "r";
                break;
            default:
                break;
        }
        return commond;
    }


    /**
     * 将配置文件中对齐参数转化为对齐模式值
     *
     * @param outType
     * @return
     */
    private static AlignModel changeToAlignModel(String outType) {
        if (AlignConst.CENTER.equals(outType)) {
            return AlignModel.CENTER;
        } else if (AlignConst.RIGHT.equals(outType)) {
            return AlignModel.RIGHT;
        } else {
            return AlignModel.LEFT;
        }

    }


    /**
     * 对齐模式
     */
    private enum AlignModel {
        /**
         * 居左
         */
        LEFT,

        /**
         * 居中
         */
        CENTER,

        /**
         * 居右
         */
        RIGHT
    }


    private static class Mode {
        /**
         * 字体的对齐模式
         */
        private String align;
        /**
         * 字体
         */
        private String fontSize;
        /**
         * 行间距
         */
        private String lineHeight;

        public String getAlign() {
            return align;
        }

        public void setAlign(String align) {
            this.align = align;
        }

        public String getLineHeight() {
            return lineHeight;
        }

        public void setLineHeight(String lineHeight) {
            this.lineHeight = lineHeight;
        }

        public String getFontSize() {
            return fontSize;
        }

        public void setFontSize(String fontSize) {
            this.fontSize = fontSize;
        }

    }

    /**
     * 解析打印模版
     *
     * @author linchunhui
     */
    private static class PrinterXmlParse extends LinkedList<Object> {

        public PrinterXmlParse() {

        }

        private static class InnerPrinterXmlParse {
            private static final PrinterXmlParse INSTANCE = new PrinterXmlParse();
        }

        public static PrinterXmlParse getInstance() {
            return InnerPrinterXmlParse.INSTANCE;
        }

        private Object printerModel;

        public <T> void parse(String modelName, Object printerModel) {
            this.printerModel = printerModel;
            Node model = getModelByModelName(root.getChildNodes(), modelName);
            if (model == null) {
                throw new RuntimeException("无此模版数据，打印失败");
            }

            NodeList list = model.getChildNodes();

            this.clear();

            for (int index = 0; index < list.getLength(); index++) {
                Node node = list.item(index);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    controllerTypeParse((Element) node);
                }
            }
        }

        private void controllerTypeParse(Element el) {
            if (el.hasAttribute("showFlag")) {
                String showFlag = getValueFromObjectByPropertyName(el.getAttribute("showFlag"));
                if (showFlag == null || showFlag.equals("0")) {
                    return;
                }
            }

            if (el.getNodeName().equalsIgnoreCase("mode")) {
                this.add(parseMode(el.getChildNodes()));
            } else if (el.getNodeName().equalsIgnoreCase("line")) {
//				this.add(parseLine(el.getChildNodes()));
                String s = parseLine(el.getTextContent());
                if (s != null) {
                    this.add(s);
                }
            } else if (el.getNodeName().equalsIgnoreCase("image")) {
                String s = parseImage(el.getTextContent());
                if (s != null) {
                    this.add("!image!" + s);
                }
            }
        }


        /**
         * 解析Mode
         *
         * @param list
         * @return
         */
        private Mode parseMode(NodeList list) {
            Mode mode = new Mode();
            LogUtils.i("解析Node");
            for (int index = 0; index < list.getLength(); index++) {
                Node node = list.item(index);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element el = (Element) node;
                if (el.getNodeName().equalsIgnoreCase("fontsize")) {
                    mode.setFontSize(el.getTextContent());
                } else if (el.getNodeName().equalsIgnoreCase("align")) {
                    mode.setAlign(el.getTextContent());
                } else if (el.getNodeName().equalsIgnoreCase("lineheight")) {
                    mode.setLineHeight(el.getTextContent());
                }
            }
            return mode;
        }

        private String parseLine(String text) {
            List<String> propertyList = getPropertyFromStringFormat(text);
            return replaceProperty(text, propertyList);
        }

        private String parseImage(String value) {
            List<String> propertyList = getPropertyFromStringFormat(value);
            return replaceProperty(value, propertyList);
        }


        /**
         * 替换字符串中的关键字段
         *
         * @param formatString
         * @param propertyList
         * @return
         */
        private String replaceProperty(String formatString, List<String> propertyList) {
            for (String property : propertyList) {
                //如果这个属性的值==null时，这行文字就不显示
                String value = getValueFromObjectByPropertyName(property);
                if (value == null) {
                    return null;
                }
                formatString = formatString.replace("%" + property + "%", value);
            }
            return formatString;
        }

        /**
         * 从字符串中提取待转换的关键字
         *
         * @param stringFormat
         * @return
         */
        private List<String> getPropertyFromStringFormat(String stringFormat) {
            List<String> list = new ArrayList<String>();
            Pattern digitNumP = Pattern.compile("%([a-z|A-Z|0-9|\\_]*)%");
            Matcher foundDigitNum = digitNumP.matcher(stringFormat);
            while (foundDigitNum.find()) {
                String par = foundDigitNum.group(1);

                list.add(par);
            }
            return list;
        }


        private String getValueFromObjectByPropertyName(String propertyName) {
            Class<?> clazz = printerModel.getClass();
            String methodStr = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1, propertyName.length());
            Method method = null;
            try {
                method = clazz.getMethod(methodStr);
                Object obj = method.invoke(printerModel);
                if (obj != null) {
                    return obj.toString();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("找不到的方法:" + methodStr);
                return null;
            }
        }

        private Node getModelByModelName(NodeList list, String modelName) {
            for (int index = 0; index < list.getLength(); index++) {
                if (list.item(index).getNodeName().equalsIgnoreCase("model")) {
                    String attValue = ((Element) list.item(index)).getAttribute("name");
                    LogUtils.e("printName" + attValue);
                    if (attValue != null && attValue.equalsIgnoreCase(modelName)) {
                        return list.item(index);
                    }
                }
            }
            return null;
        }
    }

    private static class AlignConst {
        /**
         * 居左
         */
        public final static String LEFT = "left";
        /**
         * 居中
         */
        public final static String CENTER = "center";
        /**
         * 居右
         */
        public final static String RIGHT = "right";
    }
}
