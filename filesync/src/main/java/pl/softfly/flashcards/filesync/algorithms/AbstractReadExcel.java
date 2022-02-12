package pl.softfly.flashcards.filesync.algorithms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;

/**
 * Determines the column indexes for terms and definitions.
 *
 * @author Grzegorz Ziemski
 */
public abstract class AbstractReadExcel {

    /** The column index of term. */
    private int termIndex = -1;

    /** The column index of definition. */
    private int definitionIndex = -1;

    /** The row where cards data begins. */
    private int skipHeaderRows = -1;

    // @todo Only for testing
    public AbstractReadExcel() {}

    /**
     * Determines the column indexes for terms and definitions.
     * Possible scenarios:
     * <p>
     * 1. The spreadsheet does not include a header.
     * IE_01 2 columns - Terms and Definitions
     * IE_02 1 column - Terms
     * <p>
     * 2. The spreadsheet includes the header with:
     * IE_03 2 columns - Term and Definition
     * IE_04 1 column - Term
     * IE_05 1 column - Definition
     * <p>
     * 3. The spreadsheet has 2 columns, but only the header for:
     * IE_06 Term
     * IE_07 Definition
     */
    protected void findColumnIndexes(@NonNull Sheet datatypeSheet) {
        Iterator<Row> rowIt = datatypeSheet.iterator();
        int termIndex = -1;
        // IE_03 IE_04 IE_05 Check the header only on the first no empty row.
        boolean lookForHeader = true;
        // IE_04
        int onlyTermRowIndex = -1;
        int onlyTermColIndex = -1;
        // IE_05
        int onlyDefinitionRowIndex = -1;
        int onlyDefinitionColIndex = -1;
        // IE_06 Term header from the previous row.
        int pTermHeaderIndex = -1;
        // IE_07 Definition header from the previous row.
        int pDefinitionHeaderIndex = -1;

        for (int rowNum = 0; rowIt.hasNext(); rowNum++) {
            Row currentRow = rowIt.next();
            Iterator<Cell> cellIt = currentRow.iterator();
            int cTermHeaderIndex = -1;
            int cDefinitionHeaderIndex = -1;
            int cTermIndex = -1;
            if (lookForHeader) {
                lookForHeader = termIndex == -1;
            }

            for (int colNum = 0; cellIt.hasNext(); colNum++) {
                Cell currentCell = cellIt.next();
                String value = currentCell.getStringCellValue().trim();
                if (nonEmpty(value)) {
                    if (lookForHeader && value.toLowerCase().startsWith("term")) {
                        cTermHeaderIndex = colNum;
                        onlyTermRowIndex = rowNum;
                        onlyTermColIndex = colNum;
                        if (cTermHeaderIndex != -1 && cDefinitionHeaderIndex != -1) {
                            // IE_01 2 columns - Terms and Definitions
                            this.termIndex = cTermHeaderIndex;
                            this.definitionIndex = cDefinitionHeaderIndex;
                            skipHeaderRows = rowNum;
                            return;
                        }
                    } else if (lookForHeader && value.toLowerCase().startsWith("definition")) {
                        cDefinitionHeaderIndex = colNum;
                        onlyDefinitionRowIndex = rowNum;
                        onlyDefinitionColIndex = colNum;
                        if (cTermHeaderIndex != -1 && cDefinitionHeaderIndex != -1) {
                            // IE_01 2 columns - Terms and Definitions
                            this.termIndex = cTermHeaderIndex;
                            this.definitionIndex = cDefinitionHeaderIndex;
                            skipHeaderRows = rowNum;
                            return;
                        }
                    } else if (pTermHeaderIndex != -1 && pTermHeaderIndex != colNum) {
                        // IE_06 The spreadsheet has 2 columns, but only the Term in the header.
                        this.termIndex = pTermHeaderIndex;
                        this.definitionIndex = colNum;
                        skipHeaderRows = rowNum - 1;
                        return;
                    } else if (pDefinitionHeaderIndex != -1 && pDefinitionHeaderIndex != colNum) {
                        // IE_07 The spreadsheet has 2 columns, but only the Definition in the header.
                        this.termIndex = colNum;
                        this.definitionIndex = pDefinitionHeaderIndex;
                        skipHeaderRows = rowNum - 1;
                        return;
                    } else if (cTermIndex == -1) {
                        // IE_01 IE_02 IE_04 IE_05 1 column
                        // The first column is a term if a header is missing.
                        cTermIndex = colNum;
                        termIndex = colNum;
                    } else {
                        // IE_01 IE_02 The spreadsheet does not include a header.
                        // The second column is an definition if a header is missing.
                        this.termIndex = cTermIndex;
                        this.definitionIndex = colNum;
                        return;
                    }
                }
            }
            pTermHeaderIndex = cTermHeaderIndex;
            pDefinitionHeaderIndex = cDefinitionHeaderIndex;
        }

        if (onlyTermColIndex != -1) {
            // IE_04 1 column - Term
            this.termIndex = termIndex;
            skipHeaderRows = onlyTermRowIndex;
        } else if (onlyDefinitionColIndex != -1) {
            // IE_05 Only 1 column - Definition
            this.termIndex = -1;
            this.definitionIndex = onlyDefinitionColIndex;
            skipHeaderRows = onlyDefinitionRowIndex;
        } else {
            // IE_02 1 column - Terms
            this.termIndex = termIndex;
        }
    }

    protected boolean nonEmpty(@Nullable String str) {
        return str != null && !str.isEmpty();
    }

    protected boolean empty(@Nullable String str) {
        return str == null || str.isEmpty();
    }

    protected int getTermIndex() {
        return termIndex;
    }

    protected void setTermIndex(int termIndex) {
        this.termIndex = termIndex;
    }

    protected int getDefinitionIndex() {
        return definitionIndex;
    }

    protected void setDefinitionIndex(int definitionIndex) {
        this.definitionIndex = definitionIndex;
    }

    protected int getSkipHeaderRows() {
        return skipHeaderRows;
    }

    protected void setSkipHeaderRows(int skipHeaderRows) {
        this.skipHeaderRows = skipHeaderRows;
    }
}
