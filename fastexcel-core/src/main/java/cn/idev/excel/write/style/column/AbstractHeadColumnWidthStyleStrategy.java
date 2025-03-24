package cn.idev.excel.write.style.column;

import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

/**
 * Returns the column width according to each column header
 *
 * @author Jiaju Zhuang
 */
public abstract class AbstractHeadColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {
    
    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList, Cell cell,
            Head head, Integer relativeRowIndex, Boolean isHead) {
        boolean needSetWidth = relativeRowIndex != null && (isHead || relativeRowIndex == 0);
        if (!needSetWidth) {
            return;
        }
        Integer width = columnWidth(head, cell.getColumnIndex());
        if (width != null) {
            width = width * 256;
            writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), width);
        }
    }
    
    /**
     * Returns the column width corresponding to each column head.
     *
     * <p>
     * if return null, ignore
     *
     * @param head        Nullable.
     * @param columnIndex Not null.
     * @return
     */
    protected abstract Integer columnWidth(Head head, Integer columnIndex);
    
}
