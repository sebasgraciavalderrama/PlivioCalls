/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.co.plivo;

import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.call.BulkCall;
import com.plivo.helper.api.response.call.CDR;
import com.plivo.helper.api.response.call.Call;
import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.xml.elements.Speak;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


/**
 *
 * @author kcastrop
 */
public class PlivoCall{
   

    public static void main(String[] args) throws Exception{
       
        ArrayList<String> numbers = new ArrayList<>();
        
        numbers.add("573157959868");
        numbers.add("573102703851");
        
        //Test alert --------------------------------------------------------------
        Random rand = new Random();
        int  n = rand.nextInt(99) + 1;
        String alerta = "Cliente Coomeva. Alerta, espacio en " + Integer.toString(n) + " porciento";
        //-------------------------------------------------------------------------
        
        call(numbers, alerta);
               
    }
    
    public static void call(ArrayList<String> numbers, String alerta) throws Exception{
        
        ArrayList<String> call_ids = new ArrayList<>(); 
        
        String answer_url = buildCall(alerta);
        
        for(String number: numbers){
            call_ids.add(makeCall(number, answer_url));
        }
        
        System.out.println("Esperando n segundos para obtener informacion de la(s) llamadas(s)");
        Thread.sleep(50*1000);
        
        getCallDetails(call_ids);
    
    }
    
    public static void getCallDetails(ArrayList<String> calls){
        
        String auth_id = "MAMZEXNTAWNGE2YJC3OT";
        String auth_token = "OGNmOWM4ZjFmNjgxMTMyMDU0OWQ2NDFlYmZiY2Ew";
        
        System.out.println("");
        System.out.println("--------------------- Detalles de la(s) llamada(s) ---------------------");
        
        for(String call: calls){
            
            RestAPI api = new RestAPI(auth_id, auth_token, "v1");
            LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
            parameters.put("record_id", call); // The ID of the call

            try {
                CDR cdr = api.getCDR(parameters);
                System.out.println("Numero llamado: " + cdr.toNumber);
                System.out.println("Duracion de la llamada: " + cdr.callDuration);
                System.out.println("Costo de la llamada: " + cdr.totalRate);
                System.out.println("Hora de la llamada: " + cdr.endTime);
                System.out.println("---------------------------------------------------------");
            } catch (PlivoException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
        
    }
    
    public static String buildCall(String alerta) throws Exception{
        //XML file creation -------------------------------------------------------
        Element root=new Element("Response");
        Document doc=new Document();

        Element child=new Element("Speak");
        child.setAttribute("loop", "3");
        child.setAttribute("language", "es-US");
        child.setAttribute("voice", "MAN");
        child.addContent(alerta);

        root.addContent(child);

        doc.setRootElement(root);

        XMLOutputter outter=new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat());
        outter.output(doc, new FileWriter(new File("alerta.xml")));
        
        String answer_url = "";
        ProcessBuilder pb = new ProcessBuilder(
            "curl", "-i", "-F", "name=alerta.xml", 
            "-F", "file=@/home/kcastrop/NetBeansProjects/PlivoNoMaven/alerta.xml",
            "https://uguu.se/api.php?d=upload-tool");
        
        pb.redirectErrorStream(true); 
        Process p = pb.start();
        try(InputStream response = p.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(response);
            BufferedReader br = new BufferedReader(reader);
            StringBuilder responseStrBuilder = new StringBuilder();

            String line = new String();
            

            while ((line = br.readLine()) != null) {
                if(line.toLowerCase().contains("alerta.xml")){
                    answer_url = line;
                }
            }
        }
        
        System.out.println("Link xml: " + answer_url);
        System.out.println("");
        return answer_url;
    
    }
    
    public static String makeCall(String number, String answer_url){
        
        String auth_id = "MAMZEXNTAWNGE2YJC3OT";
        String auth_token = "OGNmOWM4ZjFmNjgxMTMyMDU0OWQ2NDFlYmZiY2Ew";
        
        RestAPI call = new RestAPI(auth_id, auth_token, "v1");  
        RestAPI details = new RestAPI(auth_id, auth_token, "v1"); 
          
        LinkedHashMap<String, String> call_params = new LinkedHashMap<String, String>();

        call_params.put("to", number);
        call_params.put("from", "14154847489");
        call_params.put("answer_url",answer_url);
        call_params.put("ring_timeout","20");
        call_params.put("time_limit","59");
        call_params.put("answer_method","GET"); // method to invoke the answer_url

        String call_id = "";
        
        try
        {
            System.out.println("Llamando al numero: " + number);
            Call resp = call.makeCall(call_params);      
            call_id = resp.requestUUID;
        }
        catch (PlivoException e)
        {
            System.out.println(e.getLocalizedMessage());
        }
        
        return call_id;
    }
}
