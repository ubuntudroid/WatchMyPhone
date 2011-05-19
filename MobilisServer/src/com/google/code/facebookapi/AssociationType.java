package com.google.code.facebookapi;

/**
 * Describes a DataStore Association type. Decides the navigability
 * that you will have between objects when executing FQL.
 * 
 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.defineAssociation"> Developers Wiki: Data.defineAssociation</a>
 * @author david.j.boden
 */
public enum AssociationType {
	
    ONE_WAY((byte)1),            //where reverse lookup is not needed;
    TWO_WAY_SYMMETRIC((byte)2),  //where a backward association (B to A) is always created when a forward association (A to B) is created.
    TWO_WAY_ASYMMETRIC((byte)3); //where a backward association (B to A) has different meaning than a forward association (A to B).
    
    private byte value;
    AssociationType(byte value) {
        this.value = value;
    }
    
    public byte getValue() {
        return value;
    }  
}
