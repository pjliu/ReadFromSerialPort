import java.io.*;

/*
Interface class for SimpleSerial.  

If you don't know what an interface is, don't worry.  An interface defines the functions
that can be called by implementors of this class.  Currently there are two implementors, 
SimpleSerialNative, and SimpleSerialJava.  There might be more in the future.

SimpleSerialNative requires the file "SimpleSerialNative.dll" to be in the same folder as your Java
project file.  It's that simple.

SimpleSerialJava requires the correct installation of Sun's Javacomm communication package.  It's much
more powerful, but it can be tricky for the newcomer to configure, and use.

If you have problems with one, why don't you try the other.

A VERY SIMPLE IMPLEMENTATION:

public static void main(String args[]) {
    SimpleSerial        ss;                 // delcare the SimpleSerial object
        
    ss = new SimpleSerialNative(2);         // The argument is the commport number.  There is no Comm0.
        
    ss.writeByte((byte)'a');                // write a byte to the serial port
        
    // Give the PIC chip time to digest the byte to make sure it's ready for the next byte.
    try { Thread.sleep(1); } catch (InterruptedException e) {}
        
    ss.writeByte((byte)'!');                // write another byte to the serial port
        
    String      inputString = ss.readString();      // read any string from the serial port
    System.out.println("I read the string:  " + inputString);
}    

A few important things to note:

1.  When you write data to the serial port, it just writes the data, whether or not the PIC chip is 
    ready to receive data.  There's no handshaking or signaling.  If you send data and the PIC chip 
    isn't ready for it, it will be ignored.  Some PIC chips have a hardware UART which will buffer a 
    few bytes of data for later retrieval.  But this isn't a cure-all.  If the buffer fills up, it 
    creates an error condition which prevents further data from being read.  You need to include 
    custom PIC code to reset the error flag. 

2   In contrast to the PIC chip, the computer has a rather large hardware buffer.  If the PIC chip 
    sends serial data to your computer when you're not around to read it, it gets stored.  

3.  If you make  a call to readByte(), and there's no data to be read, readByte() waits until there 
    is data.  If you want to know how much data (if any) is available, make a call to available().

4.  Conversely, if you make a call to readBytes() or readString() and there's no data to be read, 
    readBytes() returns a byte array of zero length, and readString() returns and empty string.
    
5.  If you want to use Sun's JavaComm package instead of this code, subsitute your calls to
        new SimpleSerialNative(2);        
    with541        new SImpleSerialJava(2);
    

*/

public interface SimpleSerial {

    // These are copied right out of WINBASE.H
    // Most applications should be fine with the defaults.
    
    public static final int NOPARITY            = 0;
    public static final int ODDPARITY           = 1;
    public static final int EVENPARITY          = 2;
    public static final int MARKPARITY          = 3;
    public static final int SPACEPARITY         = 4;

    public static final int ONESTOPBIT          = 0;
    public static final int ONE5STOPBITS        = 1;
    public static final int TWOSTOPBITS         = 2;
      
    /*
    Returns the number of bytes available at the time of this call.
    It's possible there are more bytes by the time readByte() or
    readBytes() is executed.  But there will never be fewer.
    */
    public int available();
        
    /*
    returns TRUE if port is fully operationsal
    returns FALSE if there is a problem with the port
    */
    public boolean isValid();
    
    /*
    Be sure to close your serial port when done.  Note that port is
    automatically closed on exit if you don't close it yourself
    */
    public void close();
        
    /*
    Reads a single byte from the input stream.
    Return value will be from -128 to 127 (inclusive)
    If no data at serial port, waits in routine for data to arrive.
    Use available() to check how much data is available.
    If error, returns 256.
    */
    public int readByte();
    
    /*
    Reads all the bytes in the serial port.
    If no bytes availble, returns array with zero elements. 
    Never waits for data to arrive
    */
    public byte[] readBytes();
    
    /*
    Reads bytes from serial port and converts to a text string.
    DO NOT use this routine to read data.  Char->Byte converstion
    does strange things when the values are negative.  For non-
    text values, use readBytes() above
    */    
    public String readString();
   
    /*
    Writes a single byte to the serial port.
    This writes the data, whether the PIC is ready to receive or not.
    Be careful not to overwhelm the PIC chip with data.
    On pics without a hardware UART, the data will be ignored.
    On pics with a hardware UART, overflowing will loose data AND require
    the UART on the PIC to be reset.  You can reset the UART in PIC code,
    or manually turn the PIC off and then on.
    
    NOTE:  A byte has a value in the range of -128 to 127
    NOTE:  If you want to write a character, you need to cast it to a byte,
            for example:  simpleSerial.writeByte((char)'b');
    */
    public boolean writeByte(byte val);
        boolean writeString(String string); 
    /*
    For more advanced use.  Gets the input stream associated with serial port
    */
    public InputStream getInputStream();   

    /*
    For more advanced use.  Gets the output stream associated with serial port
    */
    public OutputStream getOutputStream();   
    
}