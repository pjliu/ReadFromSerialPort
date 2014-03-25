/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java 
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.logging.Logger;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

/**
 * Read from multiple Serial ports, notifying when data arrives on any.
 * 
 * @version $Id: SerialLogger.java,v 1.4 2004/03/11 04:09:14 ian Exp $
 * @author Ian F. Darwin, http://www.darwinsys.com/
 */
public class SerialLogger {

  public static void main(String[] argv) throws IOException,
      NoSuchPortException, PortInUseException,
      UnsupportedCommOperationException {

    new SerialLogger();
  }

  /* Constructor */
  public SerialLogger() throws IOException, NoSuchPortException,
      PortInUseException, UnsupportedCommOperationException {

    // get list of ports available on this particular computer,
    // by calling static method in CommPortIdentifier.
    Enumeration pList = CommPortIdentifier.getPortIdentifiers();

    // Process the list, processing only serial ports.
    while (pList.hasMoreElements()) {
      CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
      String name = cpi.getName();
      System.out.print("Port " + name + " ");
      if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        System.out.println("is a Serial Port: " + cpi);

        SerialPort thePort;
        try {
          thePort = (SerialPort) cpi.open("Logger", 1000);
        } catch (PortInUseException ev) {
          System.err.println("Port in use: " + name);
          continue;
        }

        // Tell the Comm API that we want serial events.
        thePort.notifyOnDataAvailable(true);
        try {
          thePort
              .addEventListener(new Logger(cpi.getName(), thePort));
        } catch (TooManyListenersException ev) {
          // "CantHappen" error
          System.err.println("Too many listeners(!) " + ev);
          System.exit(0);
        }
      }
    }
  }

  /** Handle one port. */
  public class Logger implements SerialPortEventListener {
    String portName;

    SerialPort thePort;

    BufferedReader ifile;

    public Logger(String name, SerialPort port) throws IOException {
      portName = name;
      thePort = port;
      // Make a reader for the input file.
      ifile = new BufferedReader(new InputStreamReader(thePort
          .getInputStream()));
    }

    public void serialEvent(SerialPortEvent ev) {
      String line;
      try {
        line = ifile.readLine();
        if (line == null) {
          System.out.println("EOF on serial port.");
          System.exit(0);
        }
        System.out.println(portName + ": " + line);
      } catch (IOException ex) {
        System.err.println("IO Error " + ex);
      }
    }
  }
}