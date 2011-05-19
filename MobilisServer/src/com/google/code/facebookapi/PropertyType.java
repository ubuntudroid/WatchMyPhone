package com.google.code.facebookapi;

/**
 * Encapsulates the property types used in the DataStore API.
 * 
 * @author david.j.boden
 * @see http://wiki.developers.facebook.com/index.php/Data.defineObjectProperty
 */
public enum PropertyType {
    INTEGER((byte)1),
    STRING((byte)2),
    TEXT_BLOB((byte)3);
    
    private byte value;
    PropertyType(byte value) {
        this.value = value;
    }
    
    public byte getValue() {
        return value;
    }
}