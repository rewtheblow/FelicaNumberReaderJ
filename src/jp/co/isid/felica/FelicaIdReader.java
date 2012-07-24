package jp.co.isid.felica;

import java.applet.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jp.co.isid.felica.Felica.FelicaException;




/**
 * FelicaIdReader is main class of the application
 * @author Ryusaburo Tanaka
 */
@SuppressWarnings("serial")
public class FelicaIdReader extends JFrame {
    private Felica felica;
    
    private JPanel idPanel = new JPanel();
    private JTextField idField = new JTextField();
    
    private JPanel msgPanel1 = new JPanel();
    private JPanel msgPanel2 = new JPanel();
    
    private JPanel numPanel = new JPanel();
    private JTextField numField = new JTextField();
    
    private JPanel namePanel = new JPanel();
    private JTextField nameField = new JTextField();

    private JPanel secPanel = new JPanel();
    private JTextField secField = new JTextField();

    private JPanel mailPanel = new JPanel();
    private JTextField mailField = new JTextField();
   
    private JLabel messageLabel	= new JLabel(); 

    boolean active = true;
    
    private Workers	workers = new Workers();
    private ArrayList<String>	readworkers = new ArrayList<String>();
    
	private File file = new File("participants" + new SimpleDateFormat("yyyyMMddHmmss").format(new Date()) + ".csv");
	private PrintWriter pw;

	public FelicaIdReader() {
        super("ISID PARTICIPANTS CHECKER");
        try {
            felica = new Felica();
        }
        catch (FelicaException e) {
            System.err.println("フェリカカードリーダにアクセスできません");
            System.exit(-1);
        }
        
        // 終了時処理の追加
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                active = false;
                synchronized (felica) {
                    felica.close();
                }
                System.exit(0);
            }
        });
        
        try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        this.setLayout(new GridLayout(9, 1));

        msgPanel1.setLayout(new FlowLayout());
        JLabel label1	= new JLabel("社員証をカードリーダーにかざして下さい。");
        label1.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 36));
        label1.setForeground(Color.red);
        msgPanel1.add(label1);
        this.add(msgPanel1);
        
        msgPanel2.setLayout(new FlowLayout());
        JLabel label2	= new JLabel("社員番号・名前等が表示されればOKです。");
        label2.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 36));
        label2.setForeground(Color.red);
        msgPanel2.add(label2);
        this.add(msgPanel2);

        numPanel.setLayout(new FlowLayout());
        numPanel.add(new JLabel("　　番号 "));
        numField.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 36));
        numField.setPreferredSize(new Dimension(750, 36));
        numPanel.add(numField);
        this.add(numPanel);

        namePanel.setLayout(new FlowLayout());
        namePanel.add(new JLabel("　　名前 "));
        nameField.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 36));
        nameField.setPreferredSize(new Dimension(750, 36));
        namePanel.add(nameField);
        this.add(namePanel);

        secPanel.setLayout(new FlowLayout());
        secPanel.add(new JLabel("　　所属 "));
        secField.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 20));
        secField.setPreferredSize(new Dimension(750, 36));
        secPanel.add(secField);
        this.add(secPanel);

        mailPanel.setLayout(new FlowLayout());
        mailPanel.add(new JLabel("アドレス "));
        mailField.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 28));
        mailField.setPreferredSize(new Dimension(750, 36));
        mailPanel.add(mailField);
        this.add(mailPanel);
        
        idPanel.setLayout(new FlowLayout());
        idPanel.add(new JLabel("　　IDm "));
        idField.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 28));
        idField.setPreferredSize(new Dimension(750, 36));
        idPanel.add(idField);
        this.add(idPanel);
       
        this.add(messageLabel);

        JLabel cwLabel	= new JLabel("                Copyright (c) 2012 by  RYUSABURO TANAKA  (Cloud business produce department, ISID)");
        cwLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        this.add(cwLabel);
        this.pack();

        // IDが全部表示されないこともあるため、幅を変更する
        Dimension dim = getPreferredSize();
        if (dim.getWidth() < 300) {
        	this.setPreferredSize(new Dimension(300, (int)dim.getHeight()));
            this.pack();
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        this.setVisible(true);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        
        // Felicaカードの読み取りループ
        String Id = null;
        String before = null;
        while (active) {
            try {
                Id = felica.getID(Felica.WILDCARD);
            }
            catch (FelicaException e) {
                Id = null;
            }
            
            if (Id != null) {
            	if (!Id.equals(before)) {
            		before = new String(Id);
                	idField.setText(Id);
                	if(!workers.isContainWorker(Id)){
                		Toolkit.getDefaultToolkit().beep();
                		messageLabel.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 28));
                		messageLabel.setForeground(Color.red);
                		messageLabel.setText("　　　　　　　　この社員証は社員情報に登録されていません！！");
                	}
                	else
                	{
                		nameField.setText(workers.getWorkerName(Id));
                		numField.setText(workers.getWorkerNumber(Id));
                		secField.setText(workers.getWorkerSection(Id));
                		mailField.setText(workers.getWorkerMail(Id));
                		messageLabel.setText("");
                		
                		if(!readworkers.contains(Id)){
                			readworkers.add(Id);
                			pw.println(	workers.getWorkerNumber(Id).trim()	+ "," + 
                						workers.getWorkerName(Id).trim()	+ "," + 
                						workers.getWorkerSection(Id).trim()	+ "," + 
                						workers.getWorkerMail(Id).trim()	+ "," + 
                						Id);
                			pw.flush();
                		}
                	}
            	}
            }
            else {
            	before = null;
            	idField.setText("");
            	numField.setText("");
        		nameField.setText("");
        		secField.setText("");
        		mailField.setText("");
        		messageLabel.setText("");
            }
            
            try {
                Thread.sleep(500); // 0.5秒おきに読み取り
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

	public static void main(String[] args) {
		new FelicaIdReader();
	}

}
