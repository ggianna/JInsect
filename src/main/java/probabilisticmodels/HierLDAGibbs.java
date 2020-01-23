/*
 * HierLDAGibbs.java
 */

package probabilisticmodels;

import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import gr.demokritos.iit.jinsect.events.NotificationListener;
import gr.demokritos.iit.jinsect.threading.PooledThreadList;

/**
 * Estimate word assignements in leaf topics and topic assignements in super topics from 
 * a corpus of documents. The method is based on Gibbs sampling.
 */
public class HierLDAGibbs implements Serializable {
    
    protected Matrix2D documentTermMatrix;
    protected Matrix2D[] documentTopicMatrixPerLevel;
    protected Matrix2D[] topicAboveTopicMatrixPerLevel;
    protected Matrix2D leafTopicTermMatrix;
    HashMap<Point,List<Integer>> wordTopics;
    protected int numOfLevels;
    protected double alpha;
    protected double beta;
    /** A NotificationListener that would expect a double number as oParams object. */
    protected NotificationListener ProgressIndicator = null;
            
    /** 
     * Constructor method:
     * Creates a new instance of HierLDAGibbs
     * @param iNumOfLevels number of levels of the hierarchy
     * @param iaDocTermMatrix the document - term matrix (the input)
     * @param dAlpha the Dirichlet parameter alpha
     * @param dBeta the Dirichlet parameter beta
     */
    public HierLDAGibbs(int iNumOfLevels, int[][] iaDocTermMatrix, double dAlpha, double dBeta) {
        alpha= dAlpha;
        beta = dBeta;
        numOfLevels = iNumOfLevels;
        documentTermMatrix = new Matrix2D(iaDocTermMatrix);
        // For all levels
        documentTopicMatrixPerLevel = new Matrix2D[iNumOfLevels];
        topicAboveTopicMatrixPerLevel = new Matrix2D[iNumOfLevels];
        for (int iLevel=0; iLevel < numOfLevels; iLevel++) {
            // Init doc - level topic matrix
            documentTopicMatrixPerLevel[iLevel] = new Matrix2D(getDocumentCount(), iLevel + 1);
            // Init level - previous level matrix
            if (iLevel > 0)
                topicAboveTopicMatrixPerLevel[iLevel] = new Matrix2D(iLevel + 1, iLevel);
        }
        // Init term - leaf topic matrix
        leafTopicTermMatrix = new Matrix2D(numOfLevels, getVocabularySize());
        wordTopics = new HashMap<Point,List<Integer>>();
    }
    
    /**
     * Get the vocabulary size
     * @return  the vocabulary size
     */
    public final int getVocabularySize() {
        return documentTermMatrix.getColCount();
    }
    
    /**
     * Get the number of documents in the corpus
     * @return  the number of documents
     */
    public final int getDocumentCount() {
        return documentTermMatrix.getRowCount();
    }
    
    /**
     * Initialize the model.
     * Assign observations to topics and then assign topics to super topics.
     * Perform random assignements with equal probabilities.
     * Assign the observations hierarchically bootom - up.
     */
    private void initModelState() {
        // Leaf level
        // For every document
        for (int iDoc=0; iDoc<getDocumentCount(); iDoc++) {
            // For every word
            for (int iWord=0; iWord < getVocabularySize(); iWord++)
                // For every word occurence
                for (int iWordOcc=0; iWordOcc < documentTermMatrix.get(iDoc, iWord); iWordOcc++) {
                    // Think from which topic the word has been chosen (Sample topic)
                    int iTopic = (int)(Math.random() * numOfLevels); // numOfLevels equals to the number of topics in the
                                                              // leaf level
                    // Update matrices
                    leafTopicTermMatrix.inc(iTopic,iWord);
                    documentTopicMatrixPerLevel[numOfLevels - 1].inc(iDoc,iTopic);
                    List<Integer> l;
                    if ((l = wordTopics.get(new Point(iDoc, iWord))) == null) {
                        // Must add new list
                        l = new ArrayList<Integer>();
                        l.add(iTopic);
                        wordTopics.put(new Point(iDoc, iWord), l);
                    }
                    else
                    {
                        // Add to existing list
                        l.add(iTopic);
                    }
                    
                    // For every level of topics up to 1
                    int iCurrentLevelTopic = iTopic;
                    for (int iLevel=numOfLevels-1; iLevel >= 1; iLevel--) {
                        // Think from which topic the subtopic has been chosen (Sample super-topic)
                        int iSuperTopic = (int)(Math.random() * iLevel);
                        // Update matrices
                        topicAboveTopicMatrixPerLevel[iLevel].inc(iCurrentLevelTopic, iSuperTopic);
                        documentTopicMatrixPerLevel[iLevel].inc(iDoc, iSuperTopic);
                        iCurrentLevelTopic = iSuperTopic;
                    }
                }
        }
    }
    
    public void performGibbs(int iIterations, int iBurnIn, int iThreads) {
        initModelState();
        PooledThreadList t = new PooledThreadList(iThreads);
        
        for (int iCurIter=0; iCurIter < iIterations; iCurIter++) {
            // Update progress bar
            if (ProgressIndicator != null)
                ProgressIndicator.Notify(this, (double)iCurIter / iIterations);
            
            // For every document
            for (int iDoc=0; iDoc<getDocumentCount(); iDoc++) {
                // For every word
                for (int iWord=0; iWord < getVocabularySize(); iWord++) {
                    // For every word occurence
                    for (int iWordOcc=0; iWordOcc < documentTermMatrix.get(iDoc, iWord); iWordOcc++) {
                        // Sample leaf topic conditional
                        int iSelectedTopic = sampleLeafTopicFullConditional(numOfLevels - 1, iWord, iDoc);
                        
                        if (iCurIter > iBurnIn) {
                            // Update statistics
                            // Transfer word from one topic to another
                            int iGetOldTopic = wordTopics.get(new Point(iDoc, iWord)).get(iWordOcc);
                            // TODO: Update doc-topic matrix
                            documentTopicMatrixPerLevel[numOfLevels-1].dec(iDoc, iGetOldTopic);
                            documentTopicMatrixPerLevel[numOfLevels-1].inc(iDoc, iSelectedTopic);
                            // TODO: Update word-topic matrices
                            wordTopics.get(new Point(iDoc, iWord)).set(iWordOcc, iSelectedTopic);
                        }
                        
                        final int iWordArg = iWord;
                        final int iDocArg = iDoc;
                        final int iCurIterArg = iCurIter;
                        final int iBurnInArg = iBurnIn;
                        
                        // For all other levels
                        for (int iCurLevel=numOfLevels-1; iCurLevel>0; iCurLevel--) {
                            final int iCurLevelArg = iCurLevel;
                            while (!t.addThreadFor(new Runnable() {
                                public void run() {
                                        // For all topics of the level
                                        for (int iCurTopic=0; iCurTopic < iCurLevelArg; iCurTopic++) {
                                            // Sample supertopic conditional
                                            int iSelectedSuperTopic = 
                                                    sampleSuperTopicFullConditional(iCurLevelArg, 
                                                    iCurTopic + 1, iWordArg, iDocArg, iCurLevelArg);
                                            // Sample supertopic inverse conditional for decreased value
                                            //int iDeSelectedSuperTopic = sampleSuperTopicInverseFullConditional(iCurLevel, 
                                                    //iCurTopic + 1, iWord, 
                                                    //iDoc, iCurLevel);
                                            // TODO: Update topic - supertopic Matrices
                                            if (iCurIterArg > iBurnInArg) {
                                                topicAboveTopicMatrixPerLevel[iCurLevelArg].inc(iCurTopic, 
                                                        iSelectedSuperTopic);
                                                //topicAboveTopicMatrixPerLevel[iCurLevel].dec(iCurTopic, iDeSelectedSuperTopic);
                                            }
                                            // TODO: CHECK if decrease of some kind is required
                                        }

                                }
                            }))
                                Thread.yield();
                        }
                    }
                    
                    try {
                        t.waitUntilCompletion();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
                    
        }
    }

    public String printoutTopicTerms(int iLevel, int iTopicNumber, int iFirstNWords, Map<Integer,String> mTermNumToTerm) {
        StringBuffer sRes = new StringBuffer();
        // Calc word probs
        Distribution dWordDistro = getTopicTermDistro(iLevel, iTopicNumber);
        
        // While we still have words
        while (!dWordDistro.asTreeMap().isEmpty() && (--iFirstNWords > 0)) {
            // Get max prob word
            int iMaxProbWord = ((Integer)dWordDistro.getKeyOfMaxValue()).intValue();
            sRes.append(mTermNumToTerm.get(iMaxProbWord) + " [" + dWordDistro.maxValue() + "]\n");
            // Remove it from the list
            dWordDistro.asTreeMap().remove(iMaxProbWord);
        }
        return sRes.toString();
    }
    
    public Distribution getTopicTermDistro(int iLevel, int iTopicNumber) {
        // Init previous level prob distro
        Distribution dPreviousLvl = new Distribution();
        //dPreviousLvl.setValue(iTopicNumber, calcTopicProbsUnderSuperTopic(iLevel,iTopicNumber).getValue(iTopicNumber));
        dPreviousLvl.setValue(iTopicNumber, 1.0);
        
        // For each level up to numOfLevels
        for (int iCurLevel = iLevel + 1; iCurLevel < numOfLevels; iCurLevel++) {
            Distribution dCurLevel = new Distribution();
            // For every super-topic
            Iterator iSuperTopics=dPreviousLvl.asTreeMap().keySet().iterator();
            while (iSuperTopics.hasNext()) {
                int iCurSuperTopic =  ((Integer)iSuperTopics.next()).intValue();
                Distribution dCurLevelGivenSuperTopic = calcTopicProbsUnderSuperTopic(iCurLevel, iCurSuperTopic);
                // Get the supertopic prob
                double dSuperTopicProb = dPreviousLvl.getValue(iCurSuperTopic);
                
                // For all current level topics
                for (int iCurTopicNo=0; iCurTopicNo <= iCurLevel; iCurTopicNo++) {
                    // Increase prob of current level topic by the prob given the current supertopic.
                    dCurLevel.setValue(iCurTopicNo, dCurLevel.getValue(iCurTopicNo) + 
                            dSuperTopicProb * 
                            dCurLevelGivenSuperTopic.getValue(iCurTopicNo)); 
                }
            }
            dPreviousLvl = dCurLevel;
        }
        
        // Calc word probs
        dPreviousLvl = calcTermProbGivenLeafTopics(dPreviousLvl.getProbabilityDistribution());
        
        return dPreviousLvl;
    }
    
    public String printoutNormalizedTopicTerms(int iLevel, int iTopicNumber, 
            int iFirstNWords, Map<Integer,String> mTermNumToTerm) {
        // Get root topic-word distro
        Distribution dTopTopic = getTopicTermDistro(0,0);
        // Get our level topic-term distro
        Distribution dFocusTopic = getTopicTermDistro(iLevel, iTopicNumber);
        
        Distribution dFinal = new Distribution();
        Iterator iFocusTerms = dFocusTopic.asTreeMap().keySet().iterator();
        while (iFocusTerms.hasNext()) {
            Integer iCurTerm = (Integer)iFocusTerms.next();
            dFinal.setValue(iCurTerm, dFocusTopic.getValue(iCurTerm) / 
                    (dTopTopic.getValue(iCurTerm) == 0.0 ? 1.0 : dTopTopic.getValue(iCurTerm)));
        }
        dFinal = dFinal.getProbabilityDistribution();
        
        StringBuffer sRes = new StringBuffer();
        // While we still have words
        while (!dFinal.asTreeMap().isEmpty() && (--iFirstNWords > 0)) {
            // Get max prob word
            int iMaxProbWord = ((Integer)dFinal.getKeyOfMaxValue()).intValue();
            sRes.append(mTermNumToTerm.get(iMaxProbWord) + " [" + dFinal.maxValue() + "]\n");
            // Remove it from the list
            dFinal.asTreeMap().remove(iMaxProbWord);
        }
        return sRes.toString();
        
    }
    
    public Distribution calcTopicProbsUnderSuperTopic(int iTopicsLevel, int iSuperTopicIndex) {
        Distribution dCurLevelDist = new Distribution();
        
        for (int iTopicCnt = 0; iTopicCnt <= iTopicsLevel; iTopicCnt++) {
            if (iTopicsLevel == 0) {
                dCurLevelDist.setValue(iTopicCnt, 1.0); // Single Topic
                break;
            }
            else
            {
                dCurLevelDist.setValue(iTopicCnt, topicAboveTopicMatrixPerLevel[iTopicsLevel].get(iTopicCnt, iSuperTopicIndex));
            }
        }
        
        // Create a probability distribution and return it
        return dCurLevelDist.getProbabilityDistribution();
    }
    
    private Distribution calcTermProbGivenLeafTopics(Distribution dLeafTopicProbs) {
        Distribution dRes = new Distribution();
        // For all words
        for (int iTermCnt=0; iTermCnt < documentTermMatrix.getColCount(); iTermCnt++) {
            // For all leaf topics
            for (int iLeafTopicCnt=0; iLeafTopicCnt < leafTopicTermMatrix.getRowCount(); iLeafTopicCnt++) {
                // Calc word prob
                dRes.setValue(iTermCnt, dRes.getValue(iTermCnt) + dLeafTopicProbs.getValue(iLeafTopicCnt) * 
                        calcTermProbGivenLeafTopic(iLeafTopicCnt, iTermCnt));
                
                // DEBUG LINES
                //System.out.println(dLeafTopicProbs.getValue(iLeafTopicCnt) * 
                //        calcTermProbGivenLeafTopic(iLeafTopicCnt, iTermCnt));
                //////////////
            }
        }
        
        // Create a probability distribution and return it
        return dRes.getProbabilityDistribution();
    }
    
    private final double calcTermProbGivenLeafTopic(int iTopicNo, int iTermNo) {
        return (double)leafTopicTermMatrix.get(iTopicNo, iTermNo) / leafTopicTermMatrix.getSumOfRow(iTopicNo);
    }
    
    /**
     * Sample leaf topics from the full conditional multinomial distribution
     * @param numberOfTopics the number of topics at the last (leaf) level
     * @param iTerm the observed term
     * @param iDocument the current document
     * @return  the sampled topic
     */
    final private int sampleLeafTopicFullConditional(int numberOfTopics, int iTerm, int iDocument) {
        // do multinomial sampling via cumulative method:
        double[] p = new double[numberOfTopics];
        for (int iTopic = 0; iTopic < numberOfTopics; iTopic++) {
            // p = P(w|t) * P(t|d)
            p[iTopic] = (double)(leafTopicTermMatrix.get(iTopic, iTerm) + beta) / 
                    (leafTopicTermMatrix.getSumOfRow(iTopic) + getVocabularySize() * beta) *
                    (documentTopicMatrixPerLevel[numOfLevels-1].get(iDocument, iTopic) + alpha) /
                    (documentTopicMatrixPerLevel[numOfLevels-1].getSumOfRow(iDocument) + numberOfTopics * alpha);
        }
        
        // cumulate multinomial parameters
        for (int k = 1; k < p.length; k++) {
            p[k] += p[k - 1];
        }
        // scaled sample because of unnormalised p[]
        double u = Math.random() * p[numberOfTopics - 1];
        int topic = 0;
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic])
                return topic;
        }
        return topic;
    }
    
    /**
     * Sample super topics from the full conditional multinomial distribution
     * @param numberOfSuperTopics the number of super topics
     * @param iTopic the current (observed or already sampled) topic
     * @param iTerm
     * @param iDocument the current document
     * @param iCurLevel the current level. This is the level of the already sampled topics, not the level that the super topics
     *        belong. The level of the super topics is iCurLevel-1.
     * @return  the sampled super topic
     */
    final private int sampleSuperTopicFullConditional(int numberOfSuperTopics, int iTopic, int iTerm, int iDocument, int iCurLevel) {
        // do multinomial sampling via cumulative method:
        double[] p = new double[numberOfSuperTopics];
        for (int iSuperTopic = 0; iSuperTopic < numberOfSuperTopics; iSuperTopic++) {
            p[iSuperTopic] = (double)(topicAboveTopicMatrixPerLevel[iCurLevel].get(iTopic, iSuperTopic) + beta) / 
                    (topicAboveTopicMatrixPerLevel[iCurLevel].getSumOfCol(iSuperTopic) + (numberOfSuperTopics+1) * beta) *
                    (documentTopicMatrixPerLevel[iCurLevel].get(iDocument, iSuperTopic) + alpha) /
                    (documentTopicMatrixPerLevel[iCurLevel].getSumOfRow(iDocument) + numberOfSuperTopics * alpha);
        }
        
        // cumulate multinomial parameters
        for (int k = 1; k < p.length; k++) {
            p[k] += p[k - 1];
        }
        // scaled sample because of unnormalised p[]
        double u = Math.random() * p[numberOfSuperTopics - 1];
        int topic = 0;
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic])
                return topic;
        }
        return topic;
    }
    
    /**
     * Sample super topics from the inverse full conditional multinomial distribution
     * @param numberOfSuperTopics the number of super topics
     * @param iTopic the current (observed or already sampled) topic
     * @param iTerm
     * @param iDocument the current document
     * @param iCurLevel the current level. This is the level of the already sampled topics, not the level that the super topics
     *        belong. The level of the super topics is iCurLevel-1.
     * @return  the sampled super topic
     */
    final private int sampleSuperTopicInverseFullConditional(int numberOfSuperTopics, int iTopic, int iTerm, int iDocument, int iCurLevel) {
        // do multinomial sampling via cumulative method:
        double[] p = new double[numberOfSuperTopics];
        for (int iSuperTopic = 0; iSuperTopic < numberOfSuperTopics; iSuperTopic++) {
            p[iSuperTopic] = (double)numberOfSuperTopics - (double)(topicAboveTopicMatrixPerLevel[iCurLevel].get(iTopic, iSuperTopic) + beta) / 
                    (topicAboveTopicMatrixPerLevel[iCurLevel].getSumOfCol(iSuperTopic) + (numberOfSuperTopics+1) * beta) *
                    (documentTopicMatrixPerLevel[iCurLevel].get(iDocument, iSuperTopic) + alpha) /
                    (documentTopicMatrixPerLevel[iCurLevel].getSumOfRow(iDocument) + numberOfSuperTopics * alpha);
        }
        
        // cumulate multinomial parameters
        for (int k = 1; k < p.length; k++) {
            p[k] += p[k - 1];
        }
        // scaled sample because of unnormalised p[]
        double u = Math.random() * p[numberOfSuperTopics - 1];
        int topic = 0;
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic])
                return topic;
        }
        return topic;
    }
 
    
    /** Returns the index of the next leaf topic, following the generative process defined by the model.
     *@return The index of the leaf topic.
     */
    public final int generateNextLeafTopic() {
        int iAboveSelectedTopic = 0; // Init first topic selection
        // For every level
        for (int iLevelCnt=1; iLevelCnt<numOfLevels; iLevelCnt++) {
            // Create the supertopic-topic distro
            Distribution dCurLevel=new Distribution();
            int iLevelTopics=topicAboveTopicMatrixPerLevel[iLevelCnt].getRowCount();
            for (int iTopicCnt=0; iTopicCnt < iLevelTopics; iTopicCnt++) {
                // by getting the supertopic-topic values from the corresponding matrix
                dCurLevel.asTreeMap().put(iTopicCnt, 
                        Double.valueOf(topicAboveTopicMatrixPerLevel[iLevelCnt].get(iTopicCnt, iAboveSelectedTopic)));
            }
            // Get next topic using random selection
            iAboveSelectedTopic = ((Integer)dCurLevel.getProbabilityDistribution().getNextResult()).intValue();
        }
        return iAboveSelectedTopic;
    }
    
    /** Returns the index of the next term, following the generative process defined by the model.
     *@return The index of the term.
     */
    private final int generateNextTerm() {
        int iLeafTopic = generateNextLeafTopic(); // Init first topic selection
        
        // For the leaf level, select a word, given the final selected topic
        // Create the topic-word distro
        Distribution dLeafLevel=new Distribution();
        int iTopicWords=leafTopicTermMatrix.getColCount();
        for (int iWordCnt=0; iWordCnt < iTopicWords; iWordCnt++) {
            // by getting the supertopic-topic values from the corresponding matrix
            dLeafLevel.asTreeMap().put(iWordCnt, 
                    Double.valueOf(leafTopicTermMatrix.get(iLeafTopic, iWordCnt)));
        }
        // Get next topic using random selection
        int iSelectedWord = ((Integer)dLeafLevel.getProbabilityDistribution().getNextResult()).intValue();
        
        return iSelectedWord;
    }
    
    /** Generates a text (i.e. list of term indices), based on the model, given the mean text length.
     *@param iMeanSize The mean text length in terms.
     *@return A list of term indices.
     */
    public List generateText(int iMeanSize) {
        // Sample text size
        int iTextSize = (int)(gr.demokritos.iit.jinsect.algorithms.statistics.statisticalCalculation.getPoissonNumber(iMeanSize));
        Vector vRes = new Vector(iTextSize);
        
        while (iTextSize-- > 0) {
            vRes.add(generateNextTerm());
        }
        return vRes;
    }
    
    public static void main(String[] args) {
        Hashtable hIndexToWord = new Hashtable();
        hIndexToWord.put(0,"this");
        hIndexToWord.put(1,"is");
        hIndexToWord.put(2,"a");
        hIndexToWord.put(3,"test");
        hIndexToWord.put(4,"Ilias");
        
        int[][] d={
            {5,0,0,0,0},
            {5,2,0,0,0},
            {5,2,0,0,1},
            {0,5,5,0,0},
            {0,0,1,5,5},
            {5,0,1,5,5}
        };
        
        HierLDAGibbs h = new HierLDAGibbs(3,d,0.1,0.1);
        h.performGibbs(10000, 1000, Runtime.getRuntime().availableProcessors());
        for (int iLvl=0; iLvl < 3; iLvl++) {
            System.out.println("Level:" + iLvl);
            for (int iTopic=0; iTopic <= iLvl; iTopic++)
                System.out.println("Topic:" + iTopic + "\n" + h.printoutTopicTerms(iLvl,iTopic,10, hIndexToWord) + "--\n");
        }
    }

    public int getNumOfLevels() {
        return numOfLevels;
    }
    
    public void setProgressIndicator(NotificationListener ProgressIndicator) {
        this.ProgressIndicator = ProgressIndicator;
    }

    private void writeObject(java.io.ObjectOutputStream os) throws IOException {
        os.writeObject(documentTermMatrix);
        os.writeObject(documentTopicMatrixPerLevel);
        os.writeObject(topicAboveTopicMatrixPerLevel);
        os.writeObject(leafTopicTermMatrix);
        os.writeObject(wordTopics);
        os.writeInt(numOfLevels);
        os.writeDouble(alpha);
        os.writeDouble(beta);
        
    }
    
    private void readObject(java.io.ObjectInputStream is) throws IOException, ClassNotFoundException {
        documentTermMatrix = (Matrix2D)is.readObject();// Matrix2D        
        documentTopicMatrixPerLevel = (Matrix2D[])is.readObject();//Matrix2D[]
        topicAboveTopicMatrixPerLevel = (Matrix2D[])is.readObject();//Matrix2D[]
        leafTopicTermMatrix = (Matrix2D)is.readObject();// Matrix2D        
        wordTopics=(HashMap<Point,List<Integer>>)is.readObject();
        numOfLevels=is.readInt();
        alpha=is.readDouble();
        beta=is.readDouble();
        
    }
}


