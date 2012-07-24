package jp.co.isid.felica;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.ldif.LdapLdifException;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Workers {

	private HashMap<String, ArrayList<String>>	mapWorker = new HashMap(); 
	
	Workers(){
		
		PropertyConfigurator.configure("." + File.separator + "log"
				+ File.separator + "log4j.properties");

		LdifReader reader = new LdifReader();
		List<LdifEntry> entries = null;
		File ldifFile	= null;
		try {
			for(File file: new File(".").listFiles()){
				if(file.getName().endsWith(".ldif")){
					ldifFile	= file;
					break;				
				}
			}
			entries = reader.parseLdifFile(ldifFile.getName());
		} catch (LdapLdifException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// iterate the entriese
//		String dnISID	= "ou=Accounts,ou=People,o=isid.co.jp,o=isid-g,dc=isid,dc=co,dc=jp";
//		String dnAO		= "ou=Accounts,ou=People,o=isid-ao.co.jp,o=isid-g,dc=isid,dc=co,dc=jp";
//		String dnIT		= "ou=Accounts,ou=People,o=isid-intertech.co.jp,o=isid-g,dc=isid,dc=co,dc=jp";
		
		for (LdifEntry entry : entries) {
			final String dnName = entry.getDn().getName();
	//		if (dnName.endsWith(dnISID) || dnName.endsWith(dnAO) || dnName.endsWith(dnIT)) {
				if(entry.get("cn") == null){
					continue;
				}
				ArrayList<String> lis	= new ArrayList<String>();

				String employeeNumber = String.valueOf(entry.get("EmployeeNumber"));
				if(employeeNumber.indexOf(":") > 0){
					lis.add(employeeNumber.substring(employeeNumber.indexOf(":") + 2).trim());
				}

				String name = entry.get("cn").get().getString();
				lis.add(name.substring(name.indexOf(":") + 1).trim());

				String mail = String.valueOf(entry.get("mail"));
				if(mail.indexOf(":") > 0){
					lis.add(mail.substring(mail.indexOf(":") + 2).trim());
				}

				String ou = String.valueOf(entry.get("ou"));
				if(ou.indexOf(":") > 0){
					lis.add(entry.get("ou").get().getString());
				}

				if(lis.size() == 4){
					String pager = String.valueOf(entry.get("pager"));
					if(pager.indexOf(":") > 0){
						mapWorker.put(pager.substring(pager.indexOf(":") + 2).trim(), lis);
					}
				}
	//		}
		}
	}

	public boolean isContainWorker(String pager){
		return mapWorker.containsKey(pager);
	}

	public String getWorkerNumber(String pager){
		return mapWorker.get(pager).get(0);
	}

	public String getWorkerName(String pager){
		return mapWorker.get(pager).get(1);
	}

	public String getWorkerMail(String pager){
		return mapWorker.get(pager).get(2);
	}

	public String getWorkerSection(String pager){
		return mapWorker.get(pager).get(3);
	}

	 public String decodeDumpString(String s) {
		 	s = s.replace("0x", "");
		 	s = s.replace(" 0x", "");
		 
		   StringBuffer result = new StringBuffer();
		   StringTokenizer tok = new StringTokenizer(s.trim(), " "); 
		   
		   List lst = new ArrayList();
		   while(tok.hasMoreTokens()) {
		     lst.add(new Character((char)Integer.parseInt(tok.nextToken(),16)));
		   }
		   char[] buf = new char[lst.size()];
		   for (int i=0; i<lst.size(); i++) {
		     buf[i] = ((Character)lst.get(i)).charValue();
		   }
		   return (new String(buf));
		 }
}
