

import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.mail.Message;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MailListener implements ActionListener, MouseListener, ListSelectionListener{
	private MailGui gui;
	private MailServer server;
	private JFileChooser fc;
	private File currentAtta = null;
	private Message currentMessage = null;
	public MailListener(MailGui gui){
		this.gui = gui;
		this.server = new MailServer();
		fc = new JFileChooser();
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		JLabel jl = (JLabel) me.getSource();
		String folderName = jl.getText();
		for(Component c : jl.getParent().getComponents()){
			if(c instanceof JLabel){
				jl.setOpaque(false);
			}
		}
		jl.setOpaque(true);
		jl.setBackground(Color.black);
		try {
			gui.swapFolder(server.getFolderContent(server.getFolder(folderName)));
			gui.setTitle("Email/"+ folderName);
		} catch (Exception e) {}
			
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		JLabel jl = (JLabel) me.getSource();
		jl.setBackground(Color.LIGHT_GRAY);
	}

	@Override
	public void mouseExited(MouseEvent me) {
		JLabel jl = (JLabel) me.getSource();
		jl.setBackground(Color.WHITE);
	}

	@Override
	public void mousePressed(MouseEvent me) {}

	@Override
	public void mouseReleased(MouseEvent me) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		gui.setTitle("Email/writing");
		if(e.getActionCommand() == "Add attatchments"){
			int returnVal = fc.showOpenDialog(gui);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            currentAtta = fc.getSelectedFile();
	            System.out.println(currentAtta.getName());
	        } else {
	            System.out.println("inte vald fil");
	        }
		}
		if(e.getActionCommand() == "Get attatchments"){
			if (fc.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
				try {
					
					File f = server.getAttachments(currentMessage).get(0);
					System.out.println(fc.getSelectedFile().getParentFile().getAbsolutePath() + "\\"+f.getName());
					PrintWriter textFileWriter = new PrintWriter(new FileWriter(fc.getSelectedFile().getParentFile().getAbsolutePath() + "\\"+f.getName()));
					textFileWriter.close();
				} catch (Exception e1) {}
			
			}
		}
		else if ((e.getActionCommand() == "SKICKA")){
			ArrayList<String> al = gui.getSendData();
			server.sendMail(al.get(0),al.get(2),al.get(1),currentAtta);
			currentAtta = null;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()){
			JList<String> jl = (JList<String>) e.getSource();
			int selectedIndex = jl.getSelectedIndex();
			ArrayList<String> al = server.getMessageInfo(server.getCurrentFolder(), selectedIndex+1);
			gui.setTitle("Email/" + al.get(0));
			gui.swapToSingleMail(al.get(1), al.get(0), al.get(2));

			try {
				currentMessage = server.getMessage(server.getCurrentFolder(), selectedIndex+1);
				if(server.getAttachments(server.getMessage(server.getCurrentFolder(), selectedIndex+1)) != null){
					System.out.println(server.getAttachments(server.getMessage(server.getCurrentFolder(), selectedIndex+1)).get(0).getAbsolutePath());
				}
			} catch (Exception e1) {}
		}
	}
}
