package cn.idev.excel.support;

import cn.idev.excel.exception.ExcelAnalysisException;
import cn.idev.excel.exception.ExcelCommonException;
import cn.idev.excel.read.metadata.ReadWorkbook;
import cn.idev.excel.util.StringUtils;
import lombok.Getter;
import org.apache.poi.EmptyFileException;
import org.apache.poi.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author jipengfei
 */
@Getter
public enum ExcelTypeEnum {
    
    /**
     * csv
     */
    CSV(".csv", new byte[] {-27, -89, -109, -27}),
    
    /**
     * xls
     */
    XLS(".xls", new byte[] {-48, -49, 17, -32, -95, -79, 26, -31}),
    
    /**
     * xlsx
     */
    XLSX(".xlsx", new byte[] {80, 75, 3, 4});
    
    final String value;
    
    final byte[] magic;
    
    ExcelTypeEnum(String value, byte[] magic) {
        this.value = value;
        this.magic = magic;
    }
    
    final static int MAX_PATTERN_LENGTH = 8;
    
    public static ExcelTypeEnum valueOf(ReadWorkbook readWorkbook) {
        ExcelTypeEnum excelType = readWorkbook.getExcelType();
        if (excelType != null) {
            return excelType;
        }
        File file = readWorkbook.getFile();
        InputStream inputStream = readWorkbook.getInputStream();
        if (file == null && inputStream == null) {
            throw new ExcelAnalysisException("File and inputStream must be a non-null.");
        }
        try {
            if (file != null) {
                if (!file.exists()) {
                    throw new ExcelAnalysisException("File " + file.getAbsolutePath() + " not exists.");
                }
                // If there is a password, use the FileMagic first
                if (!StringUtils.isEmpty(readWorkbook.getPassword())) {
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                        return recognitionExcelType(bufferedInputStream);
                    }
                }
                // Use the name to determine the type
                String fileName = file.getName();
                if (fileName.endsWith(XLSX.getValue())) {
                    return XLSX;
                } else if (fileName.endsWith(XLS.getValue())) {
                    return XLS;
                } else if (fileName.endsWith(CSV.getValue())) {
                    return CSV;
                }
                if (StringUtils.isEmpty(readWorkbook.getPassword())) {
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                        return recognitionExcelType(bufferedInputStream);
                    }
                }
            }
            if (!inputStream.markSupported()) {
                inputStream = new BufferedInputStream(inputStream);
                readWorkbook.setInputStream(inputStream);
            }
            return recognitionExcelType(inputStream);
        } catch (ExcelCommonException e) {
            throw e;
        } catch (EmptyFileException e) {
            throw new ExcelCommonException("The supplied file was empty (zero bytes long)");
        } catch (Exception e) {
            throw new ExcelCommonException(
                    "Convert excel format exception.You can try specifying the 'excelType' yourself", e);
        }
    }
    
    private static ExcelTypeEnum recognitionExcelType(InputStream inputStream) throws Exception {
        // Grab the first bytes of this stream
        byte[] data = IOUtils.peekFirstNBytes(inputStream, MAX_PATTERN_LENGTH);
        if (findMagic(XLSX.magic, data)) {
            return XLSX;
        } else if (findMagic(XLS.magic, data)) {
            return XLS;
        }
        // csv has no fixed prefix, if the format is not specified, it defaults to csv
        return CSV;
    }
    
    private static boolean findMagic(byte[] expected, byte[] actual) {
        int i = 0;
        for (byte expectedByte : expected) {
            if (actual[i++] != expectedByte && expectedByte != '?') {
                return false;
            }
        }
        return true;
    }
    
}
