package pl.softfly.flashcards.filesync.algorithms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;

/**
 * Determines the column indexes for questions and answers.
 *
 * @author Grzegorz Ziemski
 */
public abstract class AbstractReadExcel {

    /** The column index of question. */
    private int questionIndex = -1;

    /** The column index of answer. */
    private int answerIndex = -1;

    /** The row where cards data begins. */
    private int skipHeaderRows = -1;

    // @todo Only for testing
    public AbstractReadExcel() {}

    /**
     * Determines the column indexes for questions and answers.
     * Possible scenarios:
     * <p>
     * 1. The spreadsheet does not include a header.
     * IE_01 2 columns - Questions and Answers
     * IE_02 1 column - Questions
     * <p>
     * 2. The spreadsheet includes the header with:
     * IE_03 2 columns - Question and Answer
     * IE_04 1 column - Question
     * IE_05 1 column - Answer
     * <p>
     * 3. The spreadsheet has 2 columns, but only the header for:
     * IE_06 Question
     * IE_07 Answer
     */
    protected void findColumnIndexes(@NonNull Sheet datatypeSheet) {
        Iterator<Row> rowIt = datatypeSheet.iterator();
        int questionIndex = -1;
        // IE_03 IE_04 IE_05 Check the header only on the first no empty row.
        boolean lookForHeader = true;
        // IE_04
        int onlyQuestionRowIndex = -1;
        int onlyQuestionColIndex = -1;
        // IE_05
        int onlyAnswerRowIndex = -1;
        int onlyAnswerColIndex = -1;
        // IE_06 Question header from the previous row.
        int pQuestionHeaderIndex = -1;
        // IE_07 Answer header from the previous row.
        int pAnswerHeaderIndex = -1;

        for (int rowNum = 0; rowIt.hasNext(); rowNum++) {
            Row currentRow = rowIt.next();
            Iterator<Cell> cellIt = currentRow.iterator();
            int cQuestionHeaderIndex = -1;
            int cAnswerHeaderIndex = -1;
            int cQuestionIndex = -1;
            if (lookForHeader) {
                lookForHeader = questionIndex == -1;
            }

            for (int colNum = 0; cellIt.hasNext(); colNum++) {
                Cell currentCell = cellIt.next();
                String value = currentCell.getStringCellValue().trim();
                if (nonEmpty(value)) {
                    if (lookForHeader && value.toLowerCase().startsWith("question")) {
                        cQuestionHeaderIndex = colNum;
                        onlyQuestionRowIndex = rowNum;
                        onlyQuestionColIndex = colNum;
                        if (cQuestionHeaderIndex != -1 && cAnswerHeaderIndex != -1) {
                            // IE_01 2 columns - Questions and Answers
                            this.questionIndex = cQuestionHeaderIndex;
                            this.answerIndex = cAnswerHeaderIndex;
                            skipHeaderRows = rowNum;
                            return;
                        }
                    } else if (lookForHeader && value.toLowerCase().startsWith("answer")) {
                        cAnswerHeaderIndex = colNum;
                        onlyAnswerRowIndex = rowNum;
                        onlyAnswerColIndex = colNum;
                        if (cQuestionHeaderIndex != -1 && cAnswerHeaderIndex != -1) {
                            // IE_01 2 columns - Questions and Answers
                            this.questionIndex = cQuestionHeaderIndex;
                            this.answerIndex = cAnswerHeaderIndex;
                            skipHeaderRows = rowNum;
                            return;
                        }
                    } else if (pQuestionHeaderIndex != -1 && pQuestionHeaderIndex != colNum) {
                        // IE_06 The spreadsheet has 2 columns, but only the Question in the header.
                        this.questionIndex = pQuestionHeaderIndex;
                        this.answerIndex = colNum;
                        skipHeaderRows = rowNum - 1;
                        return;
                    } else if (pAnswerHeaderIndex != -1 && pAnswerHeaderIndex != colNum) {
                        // IE_07 The spreadsheet has 2 columns, but only the Answer in the header.
                        this.questionIndex = colNum;
                        this.answerIndex = pAnswerHeaderIndex;
                        skipHeaderRows = rowNum - 1;
                        return;
                    } else if (cQuestionIndex == -1) {
                        // IE_01 IE_02 IE_04 IE_05 1 column
                        // The first column is a question if a header is missing.
                        cQuestionIndex = colNum;
                        questionIndex = colNum;
                    } else {
                        // IE_01 IE_02 The spreadsheet does not include a header.
                        // The second column is an answer if a header is missing.
                        this.questionIndex = cQuestionIndex;
                        this.answerIndex = colNum;
                        return;
                    }
                }
            }
            pQuestionHeaderIndex = cQuestionHeaderIndex;
            pAnswerHeaderIndex = cAnswerHeaderIndex;
        }

        if (onlyQuestionColIndex != -1) {
            // IE_04 1 column - Question
            this.questionIndex = questionIndex;
            skipHeaderRows = onlyQuestionRowIndex;
        } else if (onlyAnswerColIndex != -1) {
            // IE_05 Only 1 column - Answer
            this.questionIndex = -1;
            this.answerIndex = onlyAnswerColIndex;
            skipHeaderRows = onlyAnswerRowIndex;
        } else {
            // IE_02 1 column - Questions
            this.questionIndex = questionIndex;
        }
    }

    protected boolean nonEmpty(@Nullable String str) {
        return str != null && !str.isEmpty();
    }

    protected boolean empty(@Nullable String str) {
        return str == null || str.isEmpty();
    }

    protected int getQuestionIndex() {
        return questionIndex;
    }

    protected int getAnswerIndex() {
        return answerIndex;
    }

    protected int getSkipHeaderRows() {
        return skipHeaderRows;
    }

}
