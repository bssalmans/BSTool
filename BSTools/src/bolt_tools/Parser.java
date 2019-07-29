package bolt_tools;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Collections.reverseOrder;

/**
 * Created by bssalmans on 9/20/2016.
 */
public class Parser
{
    private static String FIN = "FINISHED";
    private static String ENT = "ENTRY";
    private static String BGIN = "BEGIN";
    private static String STRT = "STARTED";
    private static String EXT = "EXIT";
    private static String END = "END";
    private static String VAR = "VARIABLE";
    private static String SOQL = "SOQL";
    private static String CU = "CODE_UNIT";
    private static String METH = "METHOD";
    private static String CON = "CONSTRUCTOR";
    private static String EXE = "EXECUTION";
    private static String DML = "DML";
    private static String ERR = "ERROR";
    private static String DEBUG = "DEBUG";
	private static BufferedReader br;

    public static void indentFile(File infile) throws IOException
    {
        int tabs = 0;
        String spaces;
        String tabbedLine;
        File ofile = new File("indentedLog.txt");

        PrintWriter writer = new PrintWriter(ofile);

        br = new BufferedReader(new FileReader(infile));
        for(String line = br.readLine(); line != null; line = br.readLine())
        {
            spaces = "";
            if(line.contains(FIN) || line.contains(EXT) || line.contains(END)) { if(tabs > 0) tabs--; }
            for(int i = tabs; i > 0; i--) spaces += "  -  ";
            tabbedLine = spaces + line;
            writer.println(tabbedLine);
            if(line.contains(STRT) || line.contains(ENT) || (line.contains(BGIN) && !line.contains(VAR))) { tabs++; }
        }
        writer.close();

        // TODO: open the file after parsing
        //open(ofile);
    }

    public static void reduceToMethods(File infile) throws IOException
    {
        int tabs = 0;
        String spaces;
        String tabbedLine;
        File ofile = new File("indentedLogEdited.txt");

        PrintWriter writer = new PrintWriter(ofile);

        br = new BufferedReader(new FileReader(infile));
        for(String line = br.readLine(); line != null; line = br.readLine())
        {
            if(line.contains(METH) || line.contains(DEBUG)) 
            {
                spaces = "";
                if(line.contains(FIN) || line.contains(EXT) || line.contains(END)) 
                {
                    if(tabs > 0) tabs--;
                }
                
                for(int i = tabs; i > 0; i--) spaces += "  -  ";
                
                if(line.contains(EXT)) tabbedLine = spaces + " END ";
                else tabbedLine = spaces + line.substring(line.indexOf("]")+1, line.length());
                
                writer.println(tabbedLine);
                
                if (line.contains(STRT) || line.contains(ENT) || (line.contains(BGIN) && !line.contains(VAR))) {
                    tabs++;
                }
            }
        }
        writer.close();

        // TODO: open the file after parsing
        //open(ofile);
    }

    public static void createXML(File infile) throws IOException
    {
        String[] section = new String[5];
        File ofile = new File("xmlLog.xml");

        PrintWriter writer = new PrintWriter(ofile);
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<?xml-stylesheet type=\"text/xsl\" href=\"logStyle.xsl\"?>");
        writer.println("<log>");

        br = new BufferedReader(new FileReader(infile));
        for(String line = br.readLine(); line != null; line = br.readLine())
        {
            // line type
            if(line.contains(CU)) section[0] = "<type>Code</type>";
            else if(line.contains(METH)) section[0] = "<type>Method</type>";
            else if(line.contains(CON)) section[0] = "<type>Constructor</type>";
            else if(line.contains(SOQL)) section[0] = "<type>SOQL</type>";
            else if(line.contains(EXE)) section[0] = "<type>Execution</type>";
            else if(line.contains(DML)) section[0] = "<type>DML</type>";
            else if(line.contains(ERR)) section[0] = "<type>Error</type>";
            else section[0] = "<type>Unknown</type>";
            // section start/end (start=true/end=false/other=null)
            if(line.contains(STRT) || line.contains(ENT) || (line.contains(BGIN))) section[1] = "<start>true</start>";
            else if(line.contains(FIN) || line.contains(EXT) || line.contains(END)) section[1] = "<start>false</start>";
            else section[1] = "<start>null</start>";
            // payload (type dependent)
            if(section[0].contains("Code") && section[1].contains("true"))
            {
                String payload = line.substring(line.lastIndexOf("|")+1, line.length());
                section[2] = "<codesection>"+payload+"</codesection>";
            }
            else if(section[0].contains("Method") && section[1].contains("true"))
            {
                String payload = line.substring(line.lastIndexOf("|")+1, line.length());
                section[2] = "<method>"+payload+"</method>";
            }
            else if(section[0].contains("SOQL") && section[1].contains("true"))
            {
                String payload = line.substring(line.lastIndexOf("|")+1, line.length());
                section[2] = "<query>"+payload+"</query>";
            }
            else if(section[0].contains("SOQL") && section[1].contains("false"))
            {
                String payload = line.substring(line.lastIndexOf(":")+1, line.length());
                section[2] = "<queryRows>"+payload+"</queryRows>";
            }
            else if(section[0].contains("DML") && section[1].contains("true"))
            {
                String[] payload = line.split("[|]");
                section[2] = "<op>"+payload[3].substring(payload[3].lastIndexOf(":")+1,payload[3].length())+"</op>";
                section[3] = "<object>"+payload[4].substring(payload[4].lastIndexOf(":")+1,payload[4].length())+"</object>";
                section[4] = "<rows>"+payload[5].substring(payload[5].lastIndexOf(":")+1,payload[5].length())+"</rows>";
            }

            writer.println("<line>");
            writer.println("    "+section[0]);
            writer.println("    "+section[1]);
            if(section[2]!=null) writer.println("    "+section[2]);
            if(section[3]!=null) writer.println("    "+section[3]);
            if(section[4]!=null) writer.println("    "+section[4]);
            writer.println("</line>");

            for(int i=0;i<5;i++) section[i]=null;
        }
        writer.println("</log>");
        writer.close();

        // TODO: open the file after parsing
        //open(ofile);
    }
    
    public static void queryCounter(File infile) throws IOException
    {
        HashMap<String,Integer> qCount = new HashMap<>();

        //String inPath = infile.getAbsolutePath();
        String query;
        int index;

        File ofile = new File("queryCount.txt");

        PrintWriter writer = new PrintWriter(ofile);

        BufferedReader br = new BufferedReader(new FileReader(infile));
        for(String line = br.readLine(); line != null; line = br.readLine())
        {
            if(line.contains(SOQL))
            {
                index = line.lastIndexOf("|", line.length());
                if(index >= 1 && !line.contains("Row") && !line.contains("RunOnce") && !line.contains("Exception"))
                {
                    query = line.substring(index,line.length()-1);
                    if(qCount.keySet().contains(query)) qCount.put(query,qCount.get(query) + 1);
                    else qCount.put(query, new Integer(1));
                }
            }
        }

        HashMap<String,Integer> newQCount = (HashMap)sortByValue(qCount);
        Iterator it = (Iterator) newQCount.entrySet().iterator();
        Integer ttlLines = 0;
        while(it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            writer.println(pair.getValue() + " time(s): " + pair.getKey());
            ttlLines += (Integer) pair.getValue();
        }
        writer.println("TOTAL QUERIES: "+ttlLines);
        writer.close();
        open(ofile);
    }
    
    public static void methodCounter(File infile) throws IOException
    {
        HashMap<String,Integer> mCount = new HashMap<>();

        //String inPath = infile.getAbsolutePath();
        String method;
        int index;

        File ofile = new File("methodCount.txt");

        PrintWriter writer = new PrintWriter(ofile);

        BufferedReader br = new BufferedReader(new FileReader(infile));
        for(String line = br.readLine(); line != null; line = br.readLine())
        {
            if(line.contains("ENTERED"))
            {
                index = line.lastIndexOf("|", line.length());
                if(index >= 1)
                {
                	method = line.substring(index,line.length()-1);
                    if(mCount.keySet().contains(method)) mCount.put(method,mCount.get(method) + 1);
                    else mCount.put(method, new Integer(1));
                }
            }
        }

        HashMap<String,Integer> newMCount = (HashMap)sortByValue(mCount);
        Iterator it = (Iterator) newMCount.entrySet().iterator();
        Integer ttlLines = 0;
        while(it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            writer.println(pair.getValue() + " time(s): " + pair.getKey());
            ttlLines += (Integer) pair.getValue();
        }
        writer.println("TOTAL METHOD CALLS: "+ttlLines);
        writer.close();
        open(ofile);
    }
    
    public static void workflowCounter(File infile) throws IOException
    {
        HashMap<String,Integer> wCount = new HashMap<>();
        HashMap<String,Integer> wTrueCount = new HashMap<>();

        //String inPath = infile.getAbsolutePath();
        String wf;
        int index;

        File ofile = new File("workflowCount.txt");

        PrintWriter writer = new PrintWriter(ofile);

        BufferedReader br = new BufferedReader(new FileReader(infile));
        String lastLine = null;
        for(String line = br.readLine(); line != null; line = br.readLine())
        {
            if(line.contains("WF_CRITERIA_BEGIN"))
            {
                index = line.lastIndexOf("]", line.length());
                if(index >= 1)
                {
                	wf = line.substring(index,line.length()-1);
                    if(wCount.keySet().contains(wf)) wCount.put(wf,wCount.get(wf) + 1);
                    else wCount.put(wf, new Integer(1));
                    lastLine = wf;
                }
                
            }
            else if(line.contains("WF_CRITERIA_END") && line.contains("true"))
            {
            	index = line.lastIndexOf("|", line.length());
            	if(wTrueCount.keySet().contains(lastLine)) wTrueCount.put(lastLine,wTrueCount.get(lastLine) + 1);
                else wTrueCount.put(lastLine, new Integer(1));
            }
        }

        HashMap<String,Integer> newMCount = (HashMap)sortByValue(wCount);
        Iterator it = (Iterator) newMCount.entrySet().iterator();
        Integer ttlLines = 0;
        while(it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            writer.println(pair.getValue() + " time(s): " + pair.getKey());
            ttlLines += (Integer) pair.getValue();
        }
        writer.println("TOTAL WFs: "+ttlLines);
        
        newMCount = (HashMap)sortByValue(wTrueCount);
        it = (Iterator) newMCount.entrySet().iterator();
        ttlLines = 0;
        while(it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            writer.println(pair.getValue() + " time(s): " + pair.getKey());
            ttlLines += (Integer) pair.getValue();
        }
        writer.println("TOTAL WFtrues: "+ttlLines);
        writer.close();
        open(ofile);
    }
    
    static void parenthesizeFile(File infile) throws IOException
    {
        //String inPath = infile.getAbsolutePath();
        //String abPath;
        File ofile = new File("parenthesizedFile1.txt");

        // TODO: Figure out saving in working directory
        // region working directory code
        /* saving file in working directory is not working
        if(OSDetector.isWindows())
        {
            int index = inPath.lastIndexOf("\\", inPath.length());
            abPath = inPath.substring(0,index);
            ofile = new File(abPath + "parsedLog.txt");
        }
        else if(OSDetector.isMac() || OSDetector.isLinux())
        {
            int index = inPath.lastIndexOf("/", inPath.length());
            abPath = inPath.substring(0,index);
            ofile = new File(abPath + "parsedLog.txt");
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sorry, guy...");
            alert.setContentText("I don't know what operating system you're using and therefore can't open the file.");
            return;
        }
        */
        //endregion

        PrintWriter writer = new PrintWriter(ofile);

        BufferedReader br = new BufferedReader(new FileReader(infile));

        Integer counter = 0;
        Integer totalCounter = 0;
        Integer numFiles = 2;
        for(String line = br.readLine(); line != null; line = br.readLine())
        {
            totalCounter++;
//            if(counter < 100)
//            {
                writer.println("'" + line + "',");
                counter++;
//            }
//            else
//            {
//                writer.println("'" + line + "'");
//                counter = 0;
//                writer.close();
//                ofile = new File("parenthesizedFile"+numFiles+".txt");
//                numFiles++;
//                writer = new PrintWriter(ofile);
//            }
        }
        writer.println("TOTAL NUM: "+totalCounter);
        writer.close();
        open(ofile);
    }

    public static boolean open(File file)
    {
        try
        {
            String os = System.getProperty("os.name").toLowerCase();

            if(os.indexOf("win")>=0)
            {
                Runtime.getRuntime().exec(new String[] {"rundll32", "url.dll,FileProtocolHandler", file.getAbsolutePath()});
                return true;
            } 
            else if(os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0)
            {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", file.getAbsolutePath()});
                return true;
            } 
            else
            {
                //window.alert("Sorry, guy...I don't know what operating system you're using and therefore can't open the file.");
                return false;
            }
        } 
        catch(Exception e)
        {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public static Map<String,Integer> sortByValue(Map<String,Integer> map)
    {
        Map<String,Integer> result = new LinkedHashMap<>();
        Stream<Map.Entry<String,Integer>> st = map.entrySet().stream();

        st.sorted(reverseOrder(Map.Entry.comparingByValue())).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }
}
