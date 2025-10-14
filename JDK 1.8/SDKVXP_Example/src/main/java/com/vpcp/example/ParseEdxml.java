package com.vpcp.example;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


import com.vnpt.xml.ed.Ed;
import com.vnpt.xml.ed.parser.EdXmlParser;
import com.vnpt.xml.status.Status;
import com.vnpt.xml.status.parser.StatusXmlParser;


public class ParseEdxml {
  public static void main(String[] args) throws Exception {
   File file = new File("./resources/edoc_replacement.edxml");
    InputStream inputStream = new FileInputStream(file);
    Ed ed =  EdXmlParser.getInstance().parse(inputStream);
    System.out.println(ed.toString());
    
    File file2 = new File("./resources/status_processing_05.edxml");
    InputStream inputStream2 = new FileInputStream(file2);
    Status ed2 =  StatusXmlParser.parse(inputStream2);
    System.out.println(ed2.getHeader());
    
    
  }
}
