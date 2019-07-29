package bolt_tools;

import javax.swing.*;

import org.apache.commons.text.StringEscapeUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by bssalmans on 9/20/2016.
 */
public class debugtools
{
    static File selectedFile;
    
    static String CONNECTION_URL = StringEscapeUtils.escapeJava("jdbc:sqlserver://DESKTOP-0P1SU82\\SQLEXPRESS;user=INFORMATION_SCHEMA");

    public static void main(String[] args)
    {
    	JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Boring ol' Debug Parser for the Elderly and Infirm");
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JCheckBox indentFile = new JCheckBox("Indent File",false);
        JCheckBox methodsOnlyCB = new JCheckBox("Methods Only",false);
        JCheckBox soqlCountCB = new JCheckBox("SOQL Count",false);
        //JCheckBox CSVtoBDCB = new JCheckBox("CSVtoDB",false);
        JCheckBox parenthesize = new JCheckBox("Parenthesize",false);
        JCheckBox methodCount = new JCheckBox("Method Count",false);
        JCheckBox wfCount = new JCheckBox("Workflow Count",false);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	try
                {	
            		if(indentFile.isSelected())
            		{
            			Parser.indentFile(selectedFile);
            		}
            		else if(methodsOnlyCB.isSelected())
                	{
                		Parser.reduceToMethods(selectedFile);
                	}
                	else if(soqlCountCB.isSelected())
                	{
                		Parser.queryCounter(selectedFile);
                	}
//                	else if(CSVtoBDCB.isSelected())
//                	{
//                		CSVtoDB.runCSVtoDB(CONNECTION_URL, selectedFile);
//                	}
                	else if(parenthesize.isSelected())
                	{
                		Parser.parenthesizeFile(selectedFile);
                	}
                	else if(methodCount.isSelected())
                	{
                		Parser.methodCounter(selectedFile);
                	}
                	else if(wfCount.isSelected())
                	{
                		Parser.workflowCounter(selectedFile);
                	}
                } 
                catch(Exception exc) 
                { 
                	System.out.println(exc.getMessage()); 
                }
            }
        });
        runButton.setEnabled(false);

        JButton selectButton = new JButton("Select File");
        selectButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = fileChooser.getSelectedFile();
                    System.out.println(selectedFile.getName());
                }
                runButton.setEnabled(true);
            }
        });

        frame.add(selectButton);
        frame.add(runButton);
        frame.add(methodsOnlyCB);
        frame.add(soqlCountCB);
        frame.add(parenthesize);
        frame.add(methodCount);
        frame.add(wfCount);
        frame.add(indentFile);
        //frame.add(CSVtoBDCB);
        frame.pack();
        frame.setSize(400,400);
        frame.setVisible(true);
    }
}
