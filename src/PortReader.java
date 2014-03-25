import java.io.*;
import javax.comm.*;
import java.util.*;

public class PortReader implements SerialPortEventListener {
  static Enumeration ports;

  static CommPortIdentifier pID;

  InputStream inStream;

  SerialPort serPort;

  public PortReader() throws Exception{
    serPort = (SerialPort) pID.open("PortReader", 2000);
    inStream = serPort.getInputStream();

    serPort.addEventListener(this);

    serPort.notifyOnDataAvailable(true);

    serPort.setSerialPortParams(9600, SerialPort.DATABITS_7, SerialPort.STOPBITS_1,
        SerialPort.PARITY_EVEN);
  }

  public void serialEvent(SerialPortEvent event) {
    switch (event.getEventType()) {
    case SerialPortEvent.BI:
      System.out.println("SerialPortEvent.BI occurred");
    case SerialPortEvent.OE:
      System.out.println("SerialPortEvent.OE occurred");
    case SerialPortEvent.FE:
      System.out.println("SerialPortEvent.FE occurred");
    case SerialPortEvent.PE:
      System.out.println("SerialPortEvent.PE occurred");
    case SerialPortEvent.CD:
      System.out.println("SerialPortEvent.CD occurred");
    case SerialPortEvent.CTS:
      System.out.println("SerialPortEvent.CTS occurred");
    case SerialPortEvent.DSR:
      System.out.println("SerialPortEvent.DSR occurred");
    case SerialPortEvent.RI:
      System.out.println("SerialPortEvent.RI occurred");
    case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
      System.out.println("SerialPortEvent.OUTPUT_BUFFER_EMPTY occurred");
      break;
    case SerialPortEvent.DATA_AVAILABLE:
      System.out.println("SerialPortEvent.DATA_AVAILABLE occurred");
      byte[] readBuffer = new byte[20];

      try {
        while (inStream.available() > 0) {
          int numBytes = inStream.read(readBuffer);
        }
        System.out.print(new String(readBuffer));
      } catch (IOException ioe) {
        System.out.println("Exception " + ioe);
      }
      break;
    }
  }

  public static void main(String[] args) throws Exception{
    ports = CommPortIdentifier.getPortIdentifiers();

    while (ports.hasMoreElements()) {
      pID = (CommPortIdentifier) ports.nextElement();
      System.out.println("Port " + pID.getName());

      if (pID.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        if (pID.getName().equals("COM1")) {
          PortReader pReader = new PortReader();
          System.out.println("COM1 found");
        }
      }
    }
  }
}