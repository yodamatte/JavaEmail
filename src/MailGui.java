
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.mail.*;
import javax.swing.*;


public class MailGui extends JFrame{
	private MailServer mailServer;
	private JPanel cardSingleMail, cardMailList, cardPanel, topPanel, cardWriteMail;
	private JTextArea jta, jta2;
	private JTextField from, subject, to, subjectTo;
	private JList<String> list;
	private MailListener listener;
	private JScrollPane listScroll;
	private final static int WIDTH = 1000;
	private final static int HEIGHT = 1000;
	public MailGui(){
		mailServer = new MailServer();
		listener = new MailListener(this);
		cardPanel = new JPanel(new CardLayout());
		topPanel = new JPanel(new BorderLayout());
		JButton btn = new JButton("Send");
		btn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent ae) {
				jta2.setText("");
				to.setText("");
				subjectTo.setText("");
				((CardLayout) cardPanel.getLayout()).show(cardPanel, "WRITE");
			}
			
		});
		topPanel.add(btn,BorderLayout.CENTER);
		
		this.setSize(HEIGHT, WIDTH);
		this.setLayout(new BorderLayout());
		this.add(loadFolderPanel(), BorderLayout.WEST);
		initCard();
		this.add(topPanel, BorderLayout.NORTH);
		this.add(cardPanel);
		this.setTitle("Email");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	
	}

	/**
	 * Initialize the cardlayout
	 */
	public void initCard(){
		//read
		cardSingleMail = new JPanel(new BorderLayout());
		jta = new JTextArea();
		jta.setEditable(false);
		from = new JTextField("FROM");
		from.setEditable(false);
		subject = new JTextField("SUBJECT");
		subject.setEditable(false);
		JButton getAtta = new JButton("Get attatchments");
		getAtta.addActionListener(listener);
		JPanel jp = new JPanel();
		jp.add(from);
		jp.add(subject);
		JScrollPane mailPane = new JScrollPane(jta);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(jp, BorderLayout.WEST);
		panel.add(getAtta,BorderLayout.EAST);
		mailPane.setPreferredSize(new Dimension(WIDTH-200,HEIGHT-150));
		cardSingleMail.add(panel, BorderLayout.NORTH);
		cardSingleMail.add(mailPane, BorderLayout.CENTER);
		
		
		//send
		cardWriteMail = new JPanel(new BorderLayout());
		jta2 = new JTextArea();
		to = new JTextField("TO");
		to.setPreferredSize(new Dimension(100,30));
		subjectTo = new JTextField("SUBJECT");
		subjectTo.setPreferredSize(new Dimension(100,30));
		JButton addAtta = new JButton("Add attatchments");
		addAtta.addActionListener(listener);
		jp = new JPanel();
		jp.add(to);
		jp.add(subjectTo);
		JScrollPane mailTo = new JScrollPane(jta2);
		mailTo.setPreferredSize(new Dimension(WIDTH-200,HEIGHT-150));
		JPanel sasd = new JPanel(new BorderLayout());
		sasd.add(jp, BorderLayout.WEST);
		sasd.add(addAtta,BorderLayout.EAST);
		cardWriteMail.add(sasd, BorderLayout.NORTH);
		cardWriteMail.add(mailTo, BorderLayout.CENTER);
		JButton btn = new JButton("SKICKA");
		btn.addActionListener(listener);
		cardWriteMail.add(btn, BorderLayout.SOUTH);

		//list
		cardMailList = new JPanel();
		try {
			list = createMailList(mailServer.getFolderContent(mailServer.getCurrentFolder()));
			listScroll = new JScrollPane(list);
			listScroll.setPreferredSize(new Dimension(WIDTH-200,HEIGHT-150));
			cardMailList.add(listScroll);
		} catch (MessagingException e) {}
		
		cardPanel.add(cardMailList, "LIST");
		cardPanel.add(cardSingleMail, "SINGLE");
		cardPanel.add(cardWriteMail, "WRITE");

	}
	/**
	 * Creating the Jlist which all the mails should be showed in
	 * @param messages - The messages whom shall be showed.
	 * @return	A JList cotaning all messages.
	 * @throws MessagingException
	 */
	public JList<String> createMailList(Message[] messages) throws MessagingException{
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(Message msg : messages){
			model.addElement(msg.getFrom()[0] + "      " + msg.getSubject());
		}
		JList<String> list = new JList<String>(model);
		this.list = list;
		list.setBackground(Color.WHITE);
		list.addListSelectionListener(listener);
		return list;
	}
	
	/**
	 * Swaps cardlayout to single mail for reading purposes.
	 * @param from - Sets the graphic JTextField from.
	 * @param subject - Sets the graphic JTextField subject.
	 * @param message - Sets the JTextArea to show the message.
	 */
	public void swapToSingleMail(String from, String subject, String message){
		((CardLayout) cardPanel.getLayout()).show(cardPanel, "SINGLE");
		jta.setText(message);
		jta.setCaretPosition(0);
		this.from.setText(from);
		this.subject.setText(subject);

	}
	/**
	 * Creates the folder Panel
	 * @return The folder panel.
	 */
	public JScrollPane loadFolderPanel(){
		JPanel jp = new JPanel();
		jp.setLayout(new GridBagLayout());
		jp.setBackground(Color.white);
		for (javax.mail.Folder folder : mailServer.getAllFolders()) {
			GridBagConstraints gbc = new GridBagConstraints();
	        gbc.gridwidth = GridBagConstraints.REMAINDER;
	        gbc.insets = new Insets(3,3,3,3);
	        JLabel jl = new JLabel(folder.getFullName());
		    jl.addMouseListener(listener);
		    jl.setOpaque(true);
		    jl.setBackground(Color.white);
		    jp.add(jl, gbc);
	    }
		return new JScrollPane(jp);
	}

	
	/**
	 * Swaps the folder to show another folders content.
	 * @param msg - Array with all the messages.
	 */
	public void swapFolder(Message[] msg){
		cardMailList.remove(listScroll);
		listScroll.remove(list);
		try {
			list = createMailList(msg);
			listScroll = new JScrollPane(list);
			listScroll.setPreferredSize(new Dimension(WIDTH-200,HEIGHT-150));
		} catch (MessagingException e) {}
		cardMailList.add(listScroll);
		this.setVisible(false);
		this.setVisible(true);
		((CardLayout) cardPanel.getLayout()).show(cardPanel, "LIST");
	}
	public ArrayList<String> getSendData(){
		ArrayList<String> al = new ArrayList<String>();
		al.add(to.getText());
		al.add(subjectTo.getText());
		al.add(jta2.getText());
		return al;
	}

	public static void main(String[] args) {
		String adress = JOptionPane.showInputDialog("Email Adress");
		String password = JOptionPane.showInputDialog("Password");
		if(MailServer.login(adress, password))
			new MailGui();
		else{
			adress = JOptionPane.showInputDialog("Email Adress");
			password = JOptionPane.showInputDialog("Password");
		}
	}
}

