package eu.jacquet80.rds.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import eu.jacquet80.rds.input.GroupReader;
import eu.jacquet80.rds.input.HexFileGroupReader;
import eu.jacquet80.rds.input.TCPTunerGroupReader;

public class NetworkOpenDialog extends JFrame {
	private static final long serialVersionUID = 8761399836097550548L;

	private final JTextField 
			txtHost = new JTextField(""), 
			txtPort = new JTextField("8750"),
			txtURL = new JTextField();
	
	private final JRadioButton
			radTCP = new JRadioButton("Network connection to a local device (TCP)"),
			radHTTP = new JRadioButton("Use log file published on a web site (HTTP)", true);
	
	private final ButtonGroup buttons = new ButtonGroup();
	
	private final Semaphore choiceDone = new Semaphore(0);
	
	private GroupReader source;
	
	private NetworkOpenDialog() {
		super("Select network source");
		
		buttons.add(radHTTP);
		buttons.add(radTCP);
		
		final JLabel
			lblHost = new JLabel("Host:"),
			lblPort = new JLabel("Port:"),
			lblURL = new JLabel("URL:");
		
		JPanel contents = new JPanel();
		
		contents.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 5;
		contents.add(radHTTP, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.insets = new Insets(0, 50, 0, 0);
		c.anchor = GridBagConstraints.LINE_END;
		contents.add(lblURL, c);

		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 3;
		c.weightx = 1;
		c.insets = new Insets(0, 0, 0, 0);
		c.ipadx = 400;
		contents.add(txtURL, c);

		c.ipadx = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 5;
		c.weightx = .5;
		c.insets = new Insets(20, 0, 0, 0);
		c.anchor = GridBagConstraints.PAGE_END;
		contents.add(radTCP, c);
		
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.weightx = 0;
		c.insets = new Insets(0, 50, 0, 0);
		c.anchor = GridBagConstraints.LINE_END;
		contents.add(lblHost, c);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 2;
		c.weightx = 1;
		contents.add(txtHost, c);
		c.gridx = 3;
		c.weightx = 0;
		contents.add(lblPort, c);
		c.gridx = 4;
		c.weightx = .3;
		contents.add(txtPort, c);
		
		contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(contents, BorderLayout.CENTER);
		
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		final JButton btnCancel = new JButton("Cancel");
		final JButton btnOK = new JButton("OK");
		buttonPanel.add(btnCancel);
		buttonPanel.add(btnOK);
		add(buttonPanel, BorderLayout.SOUTH);
		
		final ActionListener l = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				if(evt.getSource() == btnOK) {
					if(radHTTP.isSelected()) {
						try {
							source = new HexFileGroupReader(new URL(txtURL.getText()));
						} catch (MalformedURLException e) {
							showError("Bad URL.");
							return;
						} catch (IOException e) {
							showError("I/O error white opening HTTP connection: " + e);
							return;
						}
					}
					
					if(radTCP.isSelected()) {
						try {
							source = new TCPTunerGroupReader(txtHost.getText(), Integer.parseInt(txtPort.getText()));
						} catch (NumberFormatException e) {
							showError("Invalid value for port number.");
							return;
						} catch (IOException e) {
							showError("I/O error white opening TCP connection: " + e);
							return;
						}
					}
				} else {
					source = null;
				}
				
				choiceDone.release();
			}
		};
		
		btnCancel.addActionListener(l);
		btnOK.addActionListener(l);
		
		pack();
		
		setLocationRelativeTo(null);
	}
	
	private final void showError(String msg) {
		JOptionPane.showMessageDialog(NetworkOpenDialog.this, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public static GroupReader dialog() {
		NetworkOpenDialog dialog = new NetworkOpenDialog();
		dialog.setVisible(true);
		dialog.choiceDone.acquireUninterruptibly();
		
		return dialog.source;
	}
}