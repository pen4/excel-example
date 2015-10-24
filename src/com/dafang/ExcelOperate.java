package com.dafang;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class ExcelOperate {

	public static void main(String[] args) throws Exception {
		//-----���ÿ�ʼʱ���Լ�ʱ���ʽ----
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS"); 
		TimeZone t = sdf.getTimeZone(); 
		t.setRawOffset(0); 
		sdf.setTimeZone(t); 
		Long startTime = System.currentTimeMillis(); 

		//------�������ݿ�-------
		Connection conn = null;
		PreparedStatement pst = null;
		try {
			//������ƣ��������ݿ�Driver
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("���ݿ����ӳɹ�");
			conn = java.sql.DriverManager.getConnection(  
	                "jdbc:MySQL://192.168.1.3:3306/test", "root", "root"); //���ݿ�database--test,username--root��password--root 
			conn.setAutoCommit(false); 
			//Dictionary_Geology -- ������ 
			//���ݿ��һ���ֶ�id--�����������ô���
			//chinese -- ���ݿ�ڶ����ֶ� 
			//english -- ���ݿ�������ֶ� 
			//content -- ���ݿ���ĸ��ֶ� 
			pst = (PreparedStatement) conn.prepareStatement("insert into Dictionary_Geology(chinese, english, content) values (?,?,?)" );
			
			
			File file = new File("excel/new2.xls");
			String[][] result = getData(file, 1);
			int rowLength = result.length;
			System.out.println(result.length);
			for (int i = 0; i < rowLength; i++) {
						//�������ݿ���ֶεĲ�ͬ�������޸�
						pst.setString(1, result[i][1]);//chinese
						pst.setString(2, result[i][2]);//english
						pst.setString(3, result[i][3]);//content
						pst.addBatch(); //�����������
			}
			//���������ύ
			pst.executeBatch(); 
			conn.commit(); 
			System.out.println("���ݿ�д��ɹ�"); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("���ݿ�����ʧ��");
		} catch (SQLException e) {
			e.printStackTrace();
		}  finally{
			//�ر�PreparedStatement
			if(pst != null) {
				pst.close();
				pst = null;
			}
			//�ر�Connection
			if(conn != null) {
				conn.close();
				pst = null;
			}
		}
		
		//-----����ʱ��------
		Long endTime = System.currentTimeMillis(); 
		System.out.println("��ʱ��" + sdf.format(new Date(endTime - startTime))); 	
	}

	/**
	 * ��ȡExcel�����ݣ���һά����洢����һ���и��е�ֵ����ά����洢���Ƕ��ٸ���
	 * 
	 * @param file
	 *            ��ȡ���ݵ�ԴExcel
	 * @param ignoreRows
	 *            ��ȡ���ݺ��Ե�������������ͷ����Ҫ���� ���Ե�����Ϊ1
	 * @return ������Excel�����ݵ�����
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String[][] getData(File file, int ignoreRows)
			throws FileNotFoundException, IOException {
		List<String[]> result = new ArrayList<String[]>();
		int rowSize = 0;
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				file));
		// ��HSSFWorkbook
		POIFSFileSystem fs = new POIFSFileSystem(in);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFCell cell = null;
		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			HSSFSheet st = wb.getSheetAt(sheetIndex);
			// ��һ��Ϊ���⣬��ȡ
			for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
				HSSFRow row = st.getRow(rowIndex);
				if (row == null) {
					continue;
				}
				int tempRowSize = row.getLastCellNum() + 1;
				if (tempRowSize > rowSize) {
					rowSize = tempRowSize;
				}
				String[] values = new String[rowSize];
				Arrays.fill(values, "");
				boolean hasValue = false;
				for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
					String value = "";
					cell = row.getCell(columnIndex);
					if (cell != null) {
						// ע�⣺һ��Ҫ��������������ܻ��������
						cell.setEncoding(HSSFCell.ENCODING_UTF_16);
						switch (cell.getCellType()) {
						case HSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue();
							break;
						case HSSFCell.CELL_TYPE_NUMERIC:
							if (HSSFDateUtil.isCellDateFormatted(cell)) {
								Date date = cell.getDateCellValue();
								if (date != null) {
									value = new SimpleDateFormat("yyyy-MM-dd")
											.format(date);
								} else {
									value = "";
								}
							} else {
								value = new DecimalFormat("0").format(cell
										.getNumericCellValue());
							}
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							// ����ʱ���Ϊ��ʽ���ɵ���������ֵ
							if (!cell.getStringCellValue().equals("")) {
								value = cell.getStringCellValue();
							} else {
								value = cell.getNumericCellValue() + "";
							}
							break;
						case HSSFCell.CELL_TYPE_BLANK:
							break;
						case HSSFCell.CELL_TYPE_ERROR:
							value = "";
							break;
						case HSSFCell.CELL_TYPE_BOOLEAN:
							value = (cell.getBooleanCellValue() == true ? "Y"
									: "N");
							break;
						default:
							value = "";
						}
					}
					if (columnIndex == 0 && value.trim().equals("")) {
						break;
					}
					values[columnIndex] = rightTrim(value);
					hasValue = true;
				}

				if (hasValue) {
					result.add(values);
				}
			}
		}
		in.close();
		String[][] returnArray = new String[result.size()][rowSize];
		for (int i = 0; i < returnArray.length; i++) {
			returnArray[i] = (String[]) result.get(i);
		}
		return returnArray;
	}

	/**
	 * ȥ���ַ����ұߵĿո�
	 * 
	 * @param str
	 *            Ҫ������ַ���
	 * @return �������ַ���
	 */
	public static String rightTrim(String str) {
		if (str == null) {
			return "";
		}
		int length = str.length();
		for (int i = length - 1; i >= 0; i--) {
			if (str.charAt(i) != 0x20) {
				break;
			}
			length--;
		}
		return str.substring(0, length);
	}
}