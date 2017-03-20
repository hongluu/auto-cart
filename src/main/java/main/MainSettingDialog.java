package main;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import entities.Brand;
import service.autocart.AutoRagtag;


public class MainSettingDialog extends JFrame{
	/**
	 * 
	 */
	final static Logger logger = Logger.getLogger(MainSettingDialog.class);
	private static final long serialVersionUID = 6445315957007559638L;
	private JTextField textField;
	private JList<String> alphaList;
	private JTextArea logArea;
	private boolean running = false;
	int count = 0;
//	AutoAddCart autoCart = new AutoAddCart(this);
	public void appendLog(String text){
		logArea.append(text);
	}
	
	public void clear(){
		logArea.setText("");
	}
	
	public MainSettingDialog(){
		
		MainSettingDialog.logger.info("========================START==========================");
		AutoRagtag auto = new AutoRagtag(this);
		auto.setTransactionIdAndCookie();
		
		setTitle("カートイン- Ragtag");
		getContentPane().setLayout(null);
		setResizable(false);
		setIconImage(new ImageIcon("./icon.png").getImage());
		
		final JLabel lblNewLabel = new JLabel("<html>ブランド<br>（複数選択可）");
		lblNewLabel.setBounds(12, 10, 95, 33);
		getContentPane().add(lblNewLabel);
		JScrollPane sc = new JScrollPane();
		sc.setBounds(119, 20, 150, 150);
		getContentPane().add(sc);
		final LinkedHashMap<String, ArrayList<Brand>> map = auto.getAllBrandMap();
		alphaList = new JList(map.keySet().toArray());
		sc.setViewportView(alphaList);
		alphaList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final JPanel branPanel = new JPanel();
		branPanel.setBounds(326, 20, 331, 150);
		branPanel.setLayout(new CardLayout());
		
		for (Entry<String, ArrayList<Brand>> entry: map.entrySet()){
			branPanel.add(entry.getKey(), new BrandPane(entry.getKey(), entry.getValue()));
		}
//		((CardLayout)branPanel.getLayout()).show(branPanel, "");
		alphaList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				String alpha = (String)alphaList.getSelectedValue();
				((CardLayout)branPanel.getLayout()).show(branPanel, alpha);
			}
		});
		
		getContentPane().add(branPanel);
		final JButton btnNewButton = new JButton("実行");
		btnNewButton.setMargin(new Insets(0, 0, 0, 0));
		btnNewButton.setBounds(562, 471, 95, 21);
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
//				if (running) return;
//				running = true;
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						List<String> alphas = (ArrayList<String>)alphaList.getSelectedValuesList();
						if (alphas == null || alphas.isEmpty()){
							JOptionPane.showMessageDialog(null, "ブランドの接頭辞を選択してください。!");
							return;
						}
						List<BrandPane> visiblePaneList = new ArrayList<BrandPane>();
						for (Component comp: branPanel.getComponents()){
							if (comp instanceof BrandPane){
								BrandPane panel = (BrandPane)comp;
								if (alphas.contains(panel.getName())){
									visiblePaneList.add(panel);
								}
							}
						}
						ArrayList<Brand> selectedBrands = new ArrayList<Brand>();
						for (BrandPane pane: visiblePaneList){
							selectedBrands.addAll((ArrayList<Brand>)pane.getSelectedBrands());
						}
						if (selectedBrands == null || selectedBrands.isEmpty()){
							JOptionPane.showMessageDialog(null, "ブランドを選択してください。!");
							return;
						}

						btnNewButton.setText("実行中...");
						btnNewButton.setEnabled(false);
						String brandString = "<html>このブランドの新規出品商品をカートに入れる。<br>";
						int ii = 0;
						for (Brand brand: selectedBrands){
							ii++;
							if (ii > 1){
								brandString += "<br>";
							}
							brandString += "・" + brand.toString();
						}
						int ret = JOptionPane.showConfirmDialog(null, brandString);
						if (ret == JOptionPane.OK_OPTION){
							new javax.swing.Timer(1, new ActionListener() {
								int i=0;
								@Override
								public void actionPerformed(ActionEvent e) {
								logArea.append("\nNo." + (i++) + ": ");
								auto.run(selectedBrands);
								}
							}).start();
						}
					}
				});
				
			}
		});
		getContentPane().add(btnNewButton);
		
		logArea = new JTextArea();
		logArea.setRows(20);
		logArea.setColumns(20);
		
		logArea.setBorder(new LineBorder(Color.GRAY));
		logArea.setEditable(false);
		JScrollPane pane = new JScrollPane(logArea);
		pane.setBounds(119, 253, 538, 202);
		getContentPane().add(pane);
		
		JLabel lblLog = new JLabel("ログ");
		lblLog.setBounds(12, 253, 95, 13);
		getContentPane().add(lblLog);
		
		JButton btnSaveSetting = new JButton("設定保存");
		btnSaveSetting.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Properties pros = new Properties();
				try {
					pros.store(new FileWriter(new File("./setting.properties")),"");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnSaveSetting.setMargin(new Insets(0, 0, 0, 0));
		btnSaveSetting.setBounds(431, 471, 112, 21);
		getContentPane().add(btnSaveSetting);
		
		
		JLabel label = new JLabel("    >>");
		label.setBounds(274, 87, 50, 13);
		getContentPane().add(label);
		

		setSize(685, 550);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private class BrandPane extends JScrollPane{
		JList list;
		public BrandPane(String name, ArrayList<Brand> brandList){
			list = new JList<>(brandList.toArray());
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			setViewportView(list);
			setName(name);
		}
		
		public List getSelectedBrands(){
			return list.getSelectedValuesList();
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				File file = new File("./icon.png");
				if (!file.exists()){
					JOptionPane.showMessageDialog(null, "[icon.png]ファイルが見つかりませんでした。");
				}else{
					File settingFile = new File("./setting.properties");
					if (!settingFile.exists()){
						JOptionPane.showMessageDialog(null, "[setting.properties]ファイルが見つかりませんでした。");
					}
					else{
						try{
//							String password = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
//
//					        MessageDigest md = MessageDigest.getInstance("MD5");
//					        md.update(password.getBytes());
//
//					        byte byteData[] = md.digest();
//
//					        //convert the byte to hex format method 1
//					        StringBuffer sb = new StringBuffer();
//					        for (int i = 0; i < byteData.length; i++) {
//					         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
//					        }
//					        
//					        String input = JOptionPane.showInputDialog("パスワードを入力してください。");
//					        if (!sb.toString().equals(input)){
//					        	JOptionPane.showMessageDialog(null, "パスワードが間違いました。開発者に連絡してください。");
//					        	return;
//					        }
					        
					        new MainSettingDialog().setVisible(true);
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	public JTextField getTextField() {
		return textField;
	}

	public void setTextField(JTextField textField) {
		this.textField = textField;
	}



	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
