/* Under LGPL licence.
 */

package probabilisticmodels;

import java.io.IOException;
import java.io.Serializable;

/**
 * This class implements the functionality of a two dimensional matrix of integers.
 */
class Matrix2D implements Serializable {
    /** The 2D matrix of integers */
    private int[][] matrix;
    private int[] rowSums;
    private int[] colSums;
    private int colCount;
    private int rowCount;
    
    /**
     * Constructor method:
     * Initialize matrix with zeros.
     */
    public Matrix2D(int iRows, int iCols) {
        // Init matrix
        matrix = new int[iRows][iCols];
        rowSums = new int[iRows];
        colSums = new int[iCols];
        
        // Copy data
        for (int iRow=0; iRow < iRows; iRow++) {
            for (int iCol=0; iCol < iCols; iCol++) {
                matrix[iRow][iCol] = 0;
            }
        }
        
        colCount = iCols;
        rowCount = iRows;
        resetSums();
    }
    
    private void resetSums() {
        for (int iCnt=0; iCnt<getRowCount(); iCnt++)
            rowSums[iCnt] = 0;
        for (int iCnt=0; iCnt<getColCount(); iCnt++)
            colSums[iCnt] = 0;
    }
    
    /**
     * Constructor method:
     * Set the matrix equal to the given data.
     * @param iaSourceMatrix the input matrix
     */    
    public Matrix2D(int[][] iaSourceMatrix) {
        // Init matrix
        matrix = new int[iaSourceMatrix.length][iaSourceMatrix[0].length];
        rowSums = new int[iaSourceMatrix.length];
        colSums = new int[iaSourceMatrix[0].length];
        rowCount = iaSourceMatrix.length;
        colCount = iaSourceMatrix[0].length;
        
        // Copy data
        for (int iRow=0; iRow < iaSourceMatrix.length; iRow++) {
            for (int iCol=0; iCol < iaSourceMatrix[iRow].length; iCol++) {
                set(iRow, iCol, iaSourceMatrix[iRow][iCol]);
            }
        }
    }
    
    /**
     * Get a specific value of the matrix.
     * @param iRow the specific row of the matrix
     * @param iCol the specific column of the matrix
     * @return  the value of the element [iRow][iCol]
     */
    public final int get(int iRow, int iCol) {
        return matrix[iRow][iCol];
    }
    
    /**
     * Set a specific value to the matrix.
     * @param iRow the specific row of the matrix
     * @param iCol the specific column of the matrix
     * @param iVal the calue to set to the matrix
     */
    public final void set(int iRow, int iCol, int iVal) {
        int iOldVal = matrix[iRow][iCol]; // Get previous value
        // Decrease sums
        rowSums[iRow] -= iOldVal;
        colSums[iRow] -= iOldVal;
        
        matrix[iRow][iCol] = iVal;
        
        // Update sums
        rowSums[iRow] += iVal;
        colSums[iRow] += iVal;
    }
    
    /**
     * Increse the value of a specific element of the matrix.
     * @param iRow the specific row of the matrix
     * @param iCol the specific column of the matrix
     */
    public final void inc(int iRow, int iCol) {
        matrix[iRow][iCol]++;
        rowSums[iRow]++;
        colSums[iCol]++;
    }
    
    /**
     * Decrease the value of a specific element of the matrix.
     * @param iRow the specific row of the matrix
     * @param iCol the specific column of the matrix
     */
    public final void dec(int iRow, int iCol) {
        matrix[iRow][iCol]++;
        rowSums[iRow]--;
        colSums[iCol]--;
    }
    
    /**
     * Get the number of rows of the matrix
     * @return  the number of rows
     */
    public final int getRowCount() {
        return rowCount;
    }
    
    /**
     * Get the number of columns of the matrix
     * @return  the number of columns
     */
    public final int getColCount() {
        return colCount;
    }
    
    /**
     * Get the sum of the elements of a specific row
     * @param iRow the specific row
     * @return  the sum of the elements of the row iRow
     */
    public final int getSumOfRow(int iRow) {
        /* OBSOLETE
        int iSum=0;
        for (int iCnt=0; iCnt<getColCount(); iCnt++) {
            iSum += matrix[iRow][iCnt];
        }
        return iSum;
         */
        return rowSums[iRow];
    }

    /**
     * Get the sum of the elements of a specific column
     * @param iCol the specific column
     * @return  the sum of the elements of the column iCol
     */
    public final int getSumOfCol(int iCol) {
        /* OBSOLETE
        int iSum=0;
        for (int iCnt=0; iCnt<getRowCount(); iCnt++) {
            iSum += matrix[iCnt][iCol];
        }
        return iSum;
         */
        return colSums[iCol];
    }
    
    private void writeObject(java.io.ObjectOutputStream os) throws IOException {
        os.writeObject(matrix);
        os.writeObject(rowSums);
        os.writeObject(colSums);
        os.writeInt(colCount);
        os.writeInt(rowCount);        
    }
    
    private void readObject(java.io.ObjectInputStream is) throws IOException, ClassNotFoundException {
        matrix = (int[][])is.readObject();
        rowSums = (int[])is.readObject();
        colSums = (int[])is.readObject();
        colCount = is.readInt();
        rowCount=is.readInt();
    }
}