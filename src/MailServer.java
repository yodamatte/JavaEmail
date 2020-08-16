import java.io.*;
import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailServer {
	public final static String incMailServer = "imap-mail.outlook.com";
	public final static String serverType = "imaps";
	private static Session session;
	private static Store store;
	private static String user;
	private Folder currentFolder;
	
	/**
	 * This method connects to the javax.mail.store for the purpose of accessing all the email and folder information.
	 * As well as the javax.mail.session for sending email purposes.
	 * @param adress - the email-address to connect to.
	 * @param password - the email-address respective password.
	 * @return true if connection successful, false if authentication failed.
	 */
	public static boolean login(String adress, String password){
		user = adress;
		Properties props = System.getProperties();
		props.setProperty(incMailServer, serverType);
		session = Session.getInstance(props,new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(adress, password);
            }
         });
		try {
			store = session.getStore(serverType);
			store.connect(incMailServer, adress, password);
		} catch (MessagingException e) {} 
		props = new Properties();
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.host", "smtp-mail.outlook.com");
	    props.put("mail.smtp.port", "587");
	    // Get the Session object.
	    session = Session.getInstance(props,new javax.mail.Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	           return new PasswordAuthentication(adress, password);
	        }
	    });
	    if(store.isConnected())
			return true;
		return false;
	}
	
	/**
	 * Constructor
	 */
	public MailServer(){
	    try {
			currentFolder = store.getFolder("Inbox");
		} catch (MessagingException e) {}
	}
	/**
	 * This method sends an email.
	 * @param to - To which email-address it´s supposed to be sent to.
	 * @param msg - The message which shall be sent.
	 * @param subject - The subject of the email
	 * @param attachment- An attachment as a file object or null if no attachment is meant to be sent.
	 */
	public void sendMail(String to, String msg, String subject, File attachment){
	      try {
	          Message message = new MimeMessage(session);
	          message.setFrom(new InternetAddress(user));
	          message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
	          message.setSubject(subject);
	          BodyPart messageBodyPart = new MimeBodyPart();
	          messageBodyPart.setText(msg);
	          Multipart multipart = new MimeMultipart();
	          multipart.addBodyPart(messageBodyPart);
	          if(attachment != null){
	        	  messageBodyPart = new MimeBodyPart();
	        	  String filename = attachment.getAbsolutePath();
	        	  DataSource source = new FileDataSource(filename);
	        	  messageBodyPart.setDataHandler(new DataHandler(source));
		          messageBodyPart.setFileName(filename);
		          multipart.addBodyPart(messageBodyPart);
	          }
	          message.setContent(multipart);
	          Transport.send(message);
	       } catch (MessagingException e) {System.out.println("Failure with sending");}
	}
	
	/**
	 * Setting the currently open folder.
	 * @param folder - the folder which is to be set.
	 */
	public void setCurrentFolder(Folder folder){
		currentFolder = folder;
	}
	
	/**
	 * Gets the current open folder.
	 * @return The current open folder.
	 */
	public Folder getCurrentFolder(){
		return currentFolder;
	}
	
	/**
	 * Get the relative important information to be used in this program.
	 * @param folder - In which folder the message is meant to be in.
	 * @param position - What position the message has in that folder. Lowest is oldest.
	 * @return	An ArrayList with information about subject, from whom and the message.
	 */
	public ArrayList<String> getMessageInfo(Folder folder, int position){
		ArrayList<String> ret = new ArrayList<String>();
		try {
			openFolder(folder);
			Message msg = folder.getMessage(folder.getMessageCount()-(position-1));
			ret.add(msg.getSubject());
			ret.add(msg.getFrom()[0].toString());
			ret.add(getText(msg));
		    folder.close();
			return ret;
		} catch (MessagingException e) {}
		catch (IOException io) {}
		return null;
	}

	/**
	 * Gets the message.
	 * @param folder - In which folder the message is meant to be in.
	 * @param position - What position the message has in that folder. Lowest is oldest.
	 * @return The message which was found. If no message was found return null.
	 */
	public Message getMessage(Folder folder, int position){
		openFolder(folder);
		try {
			return folder.getMessage(position);
		} catch (MessagingException e) {}
		return null;
	}
	
	/**
	 * Retrieves all the messages in a folder, fetching them to preload them for faster downloading.
	 * @param folder - From which folder to retrieve the messages from.
	 * @return An Array with all the messages.
	 */
	public Message[] getFolderContent(Folder folder){
	    try {
	    	openFolder(folder);
			Message[] messages = folder.getMessages();
			
			//Nya mail läggs på sist. För att visa det senaste först behöver vi reversa arrayen.
			for(int i=0; i<messages.length/2; i++){
				Message temp = messages[i]; 
				messages[i] = messages[messages.length -i -1];
				messages[messages.length -i -1] = temp; 
			}
			//prefetch == snabbare inladdning
		    FetchProfile profile = new FetchProfile();
		    profile.add(FetchProfile.Item.ENVELOPE);
		    folder.fetch(messages, profile);
			return messages;
		} catch (MessagingException e) {System.out.println(e.getMessage());}
		return null;
		
	}
	
	/**
	 * Returns a folder
	 * @param folderName - The name of the folder
	 * @return A folder with the name from the parameter or null if no such folder exists.
	 */
	public Folder getFolder(String folderName){
		Folder folder = null;
		try {
			folder = store.getFolder(folderName);
			openFolder(folder);
		} catch (MessagingException e) {}
		return folder;
	}

	/**
	 * Gets all folders
	 * @return  All folders in the store object
	 */
	public Folder[] getAllFolders(){
		Folder[] folders = null;
		try {
		    folders = store.getDefaultFolder().list("*");
		} catch (MessagingException e) {}
		  return folders;
	}
		
	/**
	 * Gets the count of all messages in a folder.
	 * @param folderName - Which folder to check.
	 * @return the size of all messages in that folder.
	 */
	public int getAntalFolderMessages(String folderName){
		try {
			return store.getFolder(folderName).getMessageCount();
		} catch (MessagingException e) {}
		return -1;

	}
	
	/**
	 * Open a folder if its not open already.
	 * @param folder - Which folder to be open.
	 */
	private void openFolder(Folder folder){
		currentFolder = folder;
		try {
			if(!folder.isOpen())
				folder.open(Folder.READ_ONLY);
			
		} catch (MessagingException e) {}
	}

    /**
     * Return the text content of the message.
     * @param p - The part which the message content should be return from.
     * @return The text content of the message. Could be HTML in string format. Or null if no message was found
     */
    private String getText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            return s;
        }
      
        if (p.isMimeType("multipart/alternative")) {
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return null;
    }

	/**
	 * 
	 * @param p - What part the attachments could be in.
	 * @return	ArrayList with all attachments. Or null if no attachments was found.
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public ArrayList<File> getAttachments(Part p) throws MessagingException, IOException {
		ArrayList<File> al = new ArrayList<File>();
			if (p.isMimeType("multipart/*")) {
			    Multipart mp = (Multipart)p.getContent();
			    for (int i = 0; i < mp.getCount(); i++) {
			        MimeBodyPart part = (MimeBodyPart) mp.getBodyPart(i);
			        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			        	InputStream is = part.getInputStream();
			        	File f = new File(part.getFileName());
			        	FileOutputStream fos = new FileOutputStream(f);
			            byte[] buf = new byte[4096];
			            int bytesRead;
			            while((bytesRead = is.read(buf))!=-1) {
			                fos.write(buf, 0, bytesRead);
			            }
			        	al.add(f);
			        }
			    }
			    if(!al.isEmpty())
			    	return al;
			}
		return null;
	}

}

