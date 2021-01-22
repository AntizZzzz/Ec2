package test.common;

import java.io.File;

import jxl.Workbook;
import jxl.biff.DisplayFormat;
import jxl.format.Colour;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @Description 描述：使用JXL 创建Excel 样例
 *
 * @author  JRH
 * @date    2014年7月16日 下午5:54:38
 * @version v1.0.0
 */
public class ExcelJXL {

	private static final DisplayFormat JXL_PERCENT_FMT = NumberFormats.PERCENT_FLOAT;
	private static final DisplayFormat JXL_NUMBER_FMT = NumberFormats.INTEGER;
	private static final String[] VISIBLE_HEADER_ARR = {"姓名", "年龄", "拥戴率"};
	
	public static void main(String[] args) {
		
		try {
			
			File file = new File("D://高级报表.xls");
//			File file = new File( new String(("D://高级报表.xls" ).getBytes("UTF-8" ), "ISO-8859-1" ));

			WritableWorkbook writableWorkbook = Workbook.createWorkbook(file);

			int sheetIndex = 1;
			WritableSheet sheet = writableWorkbook.createSheet ("表" + sheetIndex, sheetIndex);

			// 设置excel 表格列头的 列样式居中
			WritableCellFormat headerCellFormat = new WritableCellFormat();
			headerCellFormat.setAlignment(jxl.format.Alignment.CENTRE);	// 居中
			headerCellFormat.setBackground(Colour. LIGHT_GREEN);        // 设置单元格背景颜色为浅绿色

			for ( int ch = 0; ch <VISIBLE_HEADER_ARR.length; ch++) {    // 设置excel列名
			    sheet.addCell( new jxl.write.Label(ch, 0, VISIBLE_HEADER_ARR[ch], headerCellFormat));
			}

			int row = sheet.getRows();		// 获取当前行数
			int col = 0;					// 设置起始列号
			
			sheet.addCell(new jxl.write.Label(col, row, "老蒋"));       // 创建文本单元格

			WritableCellFormat numberCellFormat = new WritableCellFormat(JXL_NUMBER_FMT );
			numberCellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			jxl.write.Number numberCell = new jxl.write.Number(++col, row, 24, numberCellFormat);
			sheet.addCell(numberCell);     							  // 创建数值单元格

			WritableCellFormat percentCellFormat = new WritableCellFormat(JXL_PERCENT_FMT );
			percentCellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			jxl.write.Number percentCell = new jxl.write.Number(++col, row, 0.9580D, percentCellFormat);
			sheet.addCell(percentCell);     						  // 创建百分比单元格

			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
			writableWorkbook.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
