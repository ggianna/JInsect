/*
 * Pair.java
 *
 * Created on May 6, 2008, 1:48 PM
 *
 */

package gr.demokritos.iit.jinsect.structs;

/** Represents a pair of elements of any type (as a templated class).
 *
 * @author ggianna
 */
public class Pair<ObjTypeFirst, ObjTypeSecond> {
    protected ObjTypeFirst first;
    protected ObjTypeSecond second;
    
    /** Creates a new instance of Pair, given two objects. 
     *@param oFirst The first object.
     *@param oSecond The second object.
     */
    public Pair(ObjTypeFirst oFirst, ObjTypeSecond oSecond) {
        first = oFirst;
        second = oSecond;
    }

    /** Returns the first object of the pair. 
     *@return The first object. 
     */
    public ObjTypeFirst getFirst() {
        return first;
    }

    /** Returns the second object of the pair. 
     *@return The second object. 
     */
    public ObjTypeSecond getSecond() {
        return second;
    }
}
