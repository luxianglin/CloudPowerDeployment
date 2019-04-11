package newcloud;


import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class GenExcel {

    private HSSFSheet sheet;
    private HSSFWorkbook hwb;
    private HSSFRow row;

    private static String New_List_File = "Q.csv";
    private static GenExcel instance = new GenExcel();

    public static GenExcel getInstance() { // 返回实例
        if (instance == null) {
            instance = new GenExcel();
        }
        return instance;
    }

    public void init() {
        //文档对象
        hwb = new HSSFWorkbook();
        //excel sheet
        sheet = hwb.createSheet();
    }

    // 与Q值表同步，Q值表更新，Excel表跟着更新
    public void fillData(Map<String, Map<Integer, Double>> QList, String state_idx, int action_idx, double QValue) {
        int rows = -1;
        // Step1:计算出状态行的行号
        for (Map.Entry me : QList.entrySet()) {
            rows++;

            if (state_idx.equals(me.getKey())) {
                break;
            }
        }
        row = sheet.createRow(rows);
        row.createCell(0).setCellValue(state_idx);
        row.createCell(action_idx + 1).setCellValue(QValue);
    }

    // 将完整的Q值表直接Copy生成Excel表
    public void copyQList() {

    }

    public void genExcel() {
        try {
            hwb.write(new FileOutputStream(new File(New_List_File), true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
