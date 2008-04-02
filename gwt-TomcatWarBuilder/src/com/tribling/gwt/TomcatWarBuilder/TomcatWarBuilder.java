package com.tribling.gwt.TomcatWarBuilder;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;




/**
 * build the war file for tomcat gwt server deploy
 * @author branflake2267 - Brandon Donnelson
 *
 * Watch Console for output. Vars will be outputted there.  
 *
 * !!!!!!!!!!!Compile your project in the eclipse gwt debugger before you run this
 *
 * Wants
 * TODO - figure out what project directory this file is in when ran as java application
 * TODO - Compile project first
 * TODO - upload to server via tomcat deploy
 * 
 * Maybes
 * TODO - dont zip .svn*
 * TODO - Ask for username and password for tomcat deploy, instead of static var
 * 
 * Documentation
 * http://tomcat.apache.org/tomcat-6.0-doc/index.html
 */
public class TomcatWarBuilder {

	// project vars
	private static String ProjectDirectory;

	// GWT-linux location
	private static String GWT_HOME;

	// GWT-linux Version
	private static boolean isGWT15 = false;
	
	// Project vars
	private static String ProjectCompileFile; // project compile file location
	private static String ProjectCompileFileContents;
	private static String ProjectModuleName;
	private static String ProjectName;
	private static String[] ProjectDirs;
	private static String ProjectDir;
	private static String ProjectGWTxmlFile;
	private static String ProjectGWTxmlFileLocation;
	private static String ProjectGWTxmlFileContents;
	private static String ServletClassName; // xml servlet class
	private static String ServletPath; // xml servlet path
	private static String ServletClassNameIMPL;
	// private static String[] ServerProjectDirs; //skipping for now
	private static String ClassFileContents;
	private static String[] ClassLibs;
	private static String WebXML;

	
	//temp build folder in project directory
	//BEWARE - if you change this - u must change this directory in this method -> getZipPath()
	//don't change, still fouls things up
	private static String TempBuildFolder = "/production";
	
	//default location is project root. Set this for another location
	//set with no trailing slash like "/home/branflake2267/warFiles"
	//private static String TempWarFileLocation = "/home/branflake2267"; 
	private static String TempWarFileLocation = null; 
	
	//add this if your project-compile class path is relative
	//Add trailing slash "/dir/dir/gwt-linux/"
	//private static String GWT_HOME_Manual = "/opt/gwt-linux/";
	private static String GWT_HOME_Manual = null;
	
	//acts like .htaccess security - popup login
	//add security xml - uses tomcat-users.xml user security
	private static boolean askForLogin = false;
	
	/**
	 * main method
	 * 
	 * @param args
	 * @throws IOException
	 * 
	 * 
	 * TODO - upload to server
	 * 
	 */
	public static void main(String[] args) throws IOException {
		
		//is GWT version 1.5?
		isGWT15 = true; //they put the compiled files in a different directory
		
		/********************/
		/* User Configuration Vars */
		/********************/
		
		//FIRST -> Compile your project in the eclipse gwt debugger before you run this
		
		//Ask for authorization to use this servlet application. 
		askForLogin = false;
		
		// project directory
		//ProjectDirectory = "/home/branflake2267/workspace/gwt-GV";
		//ProjectDirectory = "/home/branflake2267/workspace/gwt-test-DisplayDate";
		ProjectDirectory = "/home/branflake2267/workspace/gwt-test-Clicklistener";
		//ProjectDirectory = "/home/branflake2267/workspace/gwt-test-RPC-adv";
		//ProjectDirectory = "/home/branflake2267/workspace/gwt-test-Login-Manager";
		//ProjectDirectory = "/home/branflake2267/workspace/gwt-Calendar";
		

		
		/********************/
		/* Setup Project Vars */
		/********************/
		
		
		//Compile Project
		//TODO - compile the projectdirectory project
		
		
		
		//testing - todo
		getCurrentProjectDirectory();
		
		//set Temp Build Folder 
		setTempBuildFolder(); //use gwt-project/production for now.
		
		// find file project-compile to read its contents
		checkProjectListForCompile();

		// read the compile file
		readProjectCompileFile();

		// figure Compile Vars
		getProjectVars();

		
		
		/********************/
		/* START WAR WAR BUILD */
		/********************/

		// delete previous production build
		deleteProductionFolder();
		
		// create directories for production build
		createDirectoriesForBuild();

		//create web xml file
		createWebXMLFile();
		
		//create index.jsp page
		createIndexPage();
		
		//copy www, jars, classes to production folder
		copyFilesToProductionBuildFolder();

		//zip into war file
		zipProject();

		
		//TODO - upload to production/design tomcat server
		//http://localhost:8080/manager/deploy?path=/footoo&war=file:/path/to/foo

		
		//delete production folder when done
		
		
		//Done with everything
		System.out.println("");
		System.out.println("All Done");
	}

	
	
	/**
	 * get project Vars
	 */
	public static void getProjectVars() {
		
		// GWT_HOME location
		getGWT_HOME();

		// ProjectClassName
		getProjectClassName();
		
		// figure the project directory
		getProjectDirectoryFromClassName();

		// Project Name from getProjectDirectoryFromClassName
		getProjectName();
		
		// figure out the name of the projects xml file
		getProjectsXMLFile();

		// read project.gwt.xml file
		readProjectXMLFileContents();

		// servlet class name - from client side
		getServletClassFromXMLFile();

		// get servlet path
		getServeletUrlPath();

		// get servlet Name (server class name)
		getServerClassNameIMPL();

		// get class path contents
		getClassPathContents();

		// get class libs
		getClassLibs();
	}
	

	
	
	/**
	 * create the servlet /WEB-INF/web.xml file
	 * 
	 * Reference
	 * http://tomcat.apache.org/tomcat-6.0-doc/config/context.html
	 * 
	 * change
	 * <web-app xmlns="http://java.sun.com/xml/ns/j2ee"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
		version="2.4">
	 */
	public static String createWebXMLFileContents() {
		String WebXML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
		"<web-app>\n" +
		  "<display-name>gwt-" + ProjectName + " Compiled: " + getDate() + "</display-name>\n" +
		  "<description>Google Web Toolkit Project</description>\n";
		
		if (ServletClassName != null) {
		 WebXML += "<servlet>\n" + 
		   "\t<servlet-name>" + ProjectName + "</servlet-name>\n" +
		   "\t<servlet-class>" + ServletClassName + "</servlet-class>\n" +
		 "</servlet>\n" +
		  "<servlet-mapping>\n" +
		    "\t<servlet-name>" + ProjectName + "</servlet-name>\n" +
		    "\t<url-pattern>" + ServletPath + "</url-pattern>\n" +
		  "</servlet-mapping>\n";
		 }
		
		//add user security if turned on
		WebXML += createWebXMLFileContents_Security();
		WebXML += "</web-app>\n";
		
		return WebXML;
	}
	
	/**
	 * add security to web.xml file - Need to login to view servlet application
	 * @return
	 * 
	 * Roles are in /etc/tomcat5.5/tomcat-users.xml - roles are like groups - user needs to be apart of role
	 * You can change role below to a different role in the list, 
	 * as long as the users are part of that role that want to login
	 */
	public static String createWebXMLFileContents_Security() {
		String WebXML = "" +
			"\n<!-- Define a Security Constraint on this Application -->\n" +
			"<security-constraint>\n" +
				"<web-resource-collection>" +
				"<web-resource-name>Entire Application</web-resource-name>\n" +
				"<url-pattern>/*</url-pattern>\n" +
				"</web-resource-collection>\n" +
				"<auth-constraint>\n" +
				"<role-name>admin</role-name>\n" +
				"</auth-constraint>\n" +
			"</security-constraint>\n\n" +

			"<login-config>\n" +
				"<auth-method>BASIC</auth-method>\n" +
				"<realm-name>Your Realm Name</realm-name>\n" +
			"</login-config>\n\n" +

			"<security-role>\n" +
				"<description>The role that is required to log in to the Manager Application</description>\n" +
				"<role-name>admin</role-name>\n" +
			"</security-role>\n\n";
		
		if (askForLogin == true) {
			return WebXML;
		} else {
			return null;
		}

	}
	
	public static String getDate() {
		Date date = new Date();
		String now = date.toString();
		return now;
	}
	
	
	/**
	 * create an index page for easy module access
	 * @return
	 */
	public static String createIndexPageContents() {
	
		String LintToModule = ProjectName +".html";
		String LinkToModuleDesc = LintToModule;
		
		String IndexPage = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n" +
		"<html>\n" +
		"<body>" +
		"" +
		"\t<a href=\""+LintToModule+"\">"+LinkToModuleDesc+"</a> Quick link to your gwt module." +
		"</body>" +
		"</html>\n";
		
		return IndexPage;
	}
	
	/**
	 * create file ./production/index.jsp
	 */
	public static void createIndexPage() {
		String File = TempBuildFolder + "/index.jsp";
		createFile(File, createIndexPageContents());
	}
	
	
	public static void getCurrentProjectDirectory() {
		//don't know how to get the eclipse project directory yet that this file is in
	}
	
	/**
	 * setup temp build folder, staging area for files
	 */
	public static void setTempBuildFolder() {
		TempBuildFolder = ProjectDirectory + TempBuildFolder;
	}

	/**
	 * delete the previous production folder
	 */
	public static void deleteProductionFolder() {
		String sDir = TempBuildFolder;
		File dir = new File(sDir);
		System.out.println("Deleting production directory contents: " + sDir);
		//delete the production folder contents
		boolean success = deleteDir(dir);
	}
	
	/**
	 * delete folder contents
	 * @param dir
	 * @return
	 */
	private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    } 
	
	/**
	 * create file ./production/WEB-INF/web.xml
	 */
	public static void createWebXMLFile() {
		String File = TempBuildFolder + "/WEB-INF/web.xml";
		createFile(File, createWebXMLFileContents());
	}
	
	/**
	 * create Folders for build
	 */
	public static void createDirectoriesForBuild() {

		System.out.println("");
		System.out.println("Starting Build");

		// create ./production folder for staging
		String ProductionFolder = TempBuildFolder;
		createFolder(ProductionFolder);

		// create folder ./production/WEB-INF
		String ProductionWebInf = TempBuildFolder + "/WEB-INF";
		createFolder(ProductionWebInf);

		// create folder ./production/WEB-INF/classes
		String ProductionWebInfClasses = TempBuildFolder + "/WEB-INF/classes";
		createFolder(ProductionWebInfClasses);

		// create folder ./production/WEB-INF/lib
		String ProductionWebInfLib = TempBuildFolder + "/WEB-INF/lib";
		createFolder(ProductionWebInfLib);
	}

	
	/**
	 * copy the files in the correct folders
	 * 
	 * @throws IOException
	 */
	public static void copyFilesToProductionBuildFolder() throws IOException {

		// copy the www file
		copyWWWFiles();

		// copy the compiled classes
		copyCompiledClasses();
		
		// copy class jars to libs folder
		copyJarFiles();
	}

	/**
	 * copy the compiled classes to /production/WEB-INF/classes
	 */
	public static void copyCompiledClasses() {
		
		String src = ProjectDirectory + "/bin";
		String dest = TempBuildFolder + "/WEB-INF/classes";
		
		try {
			copyFiles(src, dest);
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}

	}
	
	/**
	 * copy files - //copy ./www to ./production/
	 */
	public static void copyWWWFiles() throws IOException {
		
		String addDir = "";
		if (isGWT15 == true) {
			addDir = "/std";
		}
		
		System.out.println("Copying WWW Files");
		
		String src = ProjectDirectory + "/www/" + ProjectModuleName + addDir;
		
		//Archive/www/*files - have to change the servelt context path if you change this
		//String dest = TempBuildFolder + "/www";
		
		//root production folder - much easier to use right off the bat - If you stick it in folders deeper, you have to change the url-path web.xml
		String dest = TempBuildFolder;
		
		try {
			copyFiles(src, dest);
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * copy class jars to the libs folder
	 * 
	 * @throws IOException
	 */
	public static void copyJarFiles() throws IOException {

		System.out.println("Copying Jars" + ClassLibs.length);
		
		String src = null;
		String dest = null;

		for (int i = 0; i < (ClassLibs.length); i++) {
			src = ClassLibs[i];
			String DestFile = getDestDirectory(src);
			dest = TempBuildFolder + "/WEB-INF/lib/" + DestFile;
			
			try {
				copyFiles(src, dest);
			} catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * get class path file contents
	 * search .classpath for libs to add
	 */
	public static void getClassPathContents() {
		String classpathfile = ProjectDirectory + "/.classpath";
		ClassFileContents = readFile(new File(classpathfile));
		System.out.println("ClassFileContents: " + ClassFileContents);
	}

	/**
	 * get jar files location
	 * 
	 * TODO - don't know how to process dir constant JUNIT_HOME, need to get eclipse enviro var, do this later "/"
	 * 
	 */
	public static void getClassLibs() {

		//kind=['|\"]lib['|\"].*?
		Pattern p = Pattern.compile("path=['\"](/.*?jar)['\"]");
		Matcher matcher = p.matcher(ClassFileContents);

		// Count the matches
		int i = 0;
		while (matcher.find()) {
			//System.out.println("Found Lib in ClassPath: " + i);
			i++;
		}

		//init the array to proper size
		String[] jars = new String[i];

		matcher.reset();//reset position

		//save the jars to array
		i = 0;
		while (matcher.find()) {
			String match = matcher.group(1);
			System.out.println("Class libs: " + i + ". " + match);
			jars[i] = match;
			i++;
		}

		//need gwt-servlet?? is there an rpc method? -lest do it by default
		boolean AddGwtServlet = true;
		
		//get rid of gwt-user.jar
		jars = deleteGwtUserJar(jars, AddGwtServlet);
		
		ClassLibs = jars;
	}
	
	/**
	 * Switch out gwt-user.jar with gwt-servlet.jar
	 * @param jars
	 * @param AddGwtServlet - tell it to replace gwt-servlet.jar and not erase it
	 * @return
	 */
	public static String[] deleteGwtUserJar(String[] jars, boolean AddGwtServlet) {
		String jar;
		for(int i=0; i<jars.length; i++) {
			jar = jars[i].toString();
			
			boolean has = jars[i].contains("gwt-user.jar");
			if (has == true) {
				if (AddGwtServlet == true) {
					System.out.println("ClassLibs Changing gwt-user.jar to gwt-servlet.jar");
					jars[i] = GWT_HOME + "gwt-servlet.jar";
				} else {
					jars[i] = null;
				}
			}
		}
		return jars;
	}
	


	/**
	 * get projects client xml settings
	 */
	public static void getProjectsXMLFile() {
		int depth = ProjectDirs.length - 1;
		String ProjectName = ProjectDirs[depth];

		ProjectGWTxmlFile = ProjectName + ".gwt.xml";
		System.out.println("ProjectGWTxmlFile: " + ProjectGWTxmlFile);

		ProjectGWTxmlFileLocation = ProjectDirectory + "/src" + ProjectDir + "/" + ProjectGWTxmlFile;
		System.out.println("ProjectGWTxmlFileLocation: "+ ProjectGWTxmlFileLocation);
	}

	public static void readProjectXMLFileContents() {

		ProjectGWTxmlFileContents = readFile(new File(ProjectGWTxmlFileLocation));
		System.out.println("ProjectGWTxmlFileContents: " + ProjectGWTxmlFileContents);
	}

	
	/**
	 * get servlet class
	 */
	public static void getServletClassFromXMLFile() {

		Pattern p = Pattern.compile("<servlet.*?class=[\"'](.*?)[\"'].*?>");
		Matcher m = p.matcher(ProjectGWTxmlFileContents);
		boolean found = m.find();

		if (found == true) {
			ServletClassName = m.group(1);
		}

		//debug
		System.out.println("ServletClassName: " + ServletClassName);
		System.out.println("");
	}

	
	/**
	 * get servlet path
	 */
	public static void getServeletUrlPath() {

		Pattern p = Pattern.compile("path=['\"](.*?)['\"]");
		Matcher m = p.matcher(ProjectGWTxmlFileContents);
		boolean found = m.find();

		if (found == true) {
			ServletPath = m.group(1);
		}

		//debug
		System.out.println("ServletPath: " + ServletPath);
	}

	
	/**
	 * get the directory structure - this wont work with a project named with 1 name and no . in it
	 * 
	 * TODO - add logic to use single name for class like "class" and not just "com.domain.gwt.class.client.class"
	 */
	public static void getProjectDirectoryFromClassName() {

		String[] dirs = ProjectModuleName.split("\\.");

		ProjectDirs = dirs; //can use this else where

		String dir = "";
		for (int i = 0; i < (dirs.length - 1); i++) {
			dir = dir + "/" + dirs[i].toString();
		}

		ProjectDir = dir;
		System.out.println("ProjectDir: " + dir);
	}

	/**
	 * get project name from class name
	 */
	public static void getProjectName() {
		ProjectName = ProjectDirs[ProjectDirs.length-1].toString();
		System.out.println("ProjectName: " + ProjectName);
	}
	
	
	public static String getDestDirectory(String File) {
		String[] dirs = File.split("/");
		String dir = dirs[dirs.length - 1];
		return dir;
	}

	
	/**
	 * get the server class name server/"ServiceImpl"
	 * 
	 * TODO - add logic to use single name for class like "class" and not just "com.domain.gwt.class.client.class"
	 */
	public static void getServerClassNameIMPL() {

		if (ServletClassName == null) {
			return;
		}
		
		String[] dirs = ServletClassName.split("\\.");

		//ServerProjectDirs = dirs; //can use this else where

		/* not useing for now
		String dir = "";
		for (int i=0; i < (dirs.length-1); i++) {
			dir = dir + "/" +  dirs[i].toString();
		}
		 */

		ServletClassNameIMPL = dirs[dirs.length - 1].toString();

		System.out.println("ServletClassNameIMPL: " + ServletClassNameIMPL);
	}

	public static void getProjectClassName() {

		Pattern p = Pattern.compile("\"\\$@\"\040(.*?);");
		Matcher m = p.matcher(ProjectCompileFileContents);
		boolean found = m.find();

		if (found == true) {
			ProjectModuleName = m.group(1);
		}

		//debug
		System.out.println("ProjectClassName: " + ProjectModuleName);
	}

	/**
	 * get the location for the gwt files from eclipse class path contents
	 * 
	 * errors when the class path is relative. 
	 * 
	 * TODO - look up the relative path
	 * 
	 */
	public static void getGWT_HOME() {

		if (GWT_HOME_Manual != null) {
			GWT_HOME = GWT_HOME_Manual;
			return;
		}
		
		Pattern p = Pattern.compile(":(/.*)gwt-user.jar:");
		Matcher m = p.matcher(ProjectCompileFileContents);
		boolean found = m.find();

		if (found == true) {
			GWT_HOME = m.group(1);
		} else {
			System.out.println("Can't find GWT_Home Directory, in the ProjectCompileFileContents Classpath. debug getClassPathContents()");
			System.exit(1);
		}
		
		//debug
		System.out.println("GWT_HOME dir: " + GWT_HOME);
	}

	/**
	 * read the gwt-compile folder to get contents
	 */
	public static void readProjectCompileFile() {

		String Dir = ProjectDirectory + "/" + ProjectCompileFile;
		File ProjectCompileFile = new File(Dir);

		ProjectCompileFileContents = readFile(ProjectCompileFile);
		System.out.println("FileContents: " + ProjectCompileFileContents);
	}

	/**
	 * read the project xml file to get contents
	 */
	public static void readProjectGWTxmlFile() {

		String Dir = ProjectDirectory + "/" + ProjectGWTxmlFile;
		File ProjectCompileFile = new File(Dir);

		ProjectGWTxmlFileContents = readFile(ProjectCompileFile);
		System.out.println("FileContents: " + ProjectGWTxmlFileContents);
	}

	
	/**
	 * get file list in the directory (find project-compile)
	 */
	private static void checkProjectListForCompile() {
		File file;
		String FileList[];

		//ls the directory for files
		file = new File(ProjectDirectory);
		FileList = file.list();

		if (FileList == null) {
			System.out.println("Error reading current directory.");
			System.exit(1);
		}

		//look for ./project-compile
		findProjectCompile(FileList);
	}

	
	/**
	 * find the project gwt-compile file
	 * @param FileList
	 */
	private static void findProjectCompile(String[] FileList) {

		String file = null;
		boolean found = false;

		for (int i = 0; i <= FileList.length; i++) {
			file = FileList[i];
			found = checkForCompile(file);

			//debug output
			//System.out.println("Checking for compile: inFile: "+ file);

			if (found == true) {
				ProjectCompileFile = file;
				return;
			}
		}
	}

	/**
	 * get the file that says project-compile
	 * @param file
	 * @return
	 */
	public static boolean checkForCompile(String file) {

		if (file == null) {
			return false;
		}
		boolean found = false;

		// create a Pattern object
		Pattern p = Pattern.compile(".*compile");
		Matcher m = p.matcher(file);
		found = m.find();

		//System.out.println("FOUND?" + found);

		return found;
	}

	/**
	 * create new directory
	 * @param Dir
	 * @return
	 */
	private static boolean createFolder(String Dir) {
		boolean status = false;
		String NewDir = null;

		if (Dir != null) {
			NewDir = Dir;
			status = new File(NewDir).mkdir();
		}

		//debug
		System.out.println("Created Production Dir: " + NewDir);

		return status;
	}

	public static String readFile(File file) {

		String sFile = null;

		try {
			FileReader input = new FileReader(file);
			BufferedReader bufRead = new BufferedReader(input);

			String line;
			int count = 0;

			// Read first line
			line = bufRead.readLine();
			sFile = line;
			count++;

			// Read through file one line at time. Print line # and line
			while (line != null) {

				line = bufRead.readLine();

				if (line != null) {
					sFile = sFile + line;
				}

				count++;
			}

			bufRead.close();

		} catch (ArrayIndexOutOfBoundsException e) {

			System.out.println("Usage: java ReadFile filename\n");

		} catch (IOException e) {
			//trace the error
			e.printStackTrace();
		}

		return sFile;
	}

	/**
	 * copy files
	 * @param strPath
	 * @param dstPath
	 * @throws IOException
	 */
	public static void copyFiles(String strPath, String dstPath) throws IOException {

		File src = new File(strPath);
		File dest = new File(dstPath);

		//skip .svn files
		if (src.isDirectory() == true && src.getName() != ".svn") {

			dest.mkdirs();
			String list[] = src.list();

			for (int i = 0; i < list.length; i++) {

				String src1 = src.getAbsolutePath() + "/" + list[i];
				String dest1 = dest.getAbsolutePath() + "/" + list[i];

				if (list[i].toString().matches(".svn") != true) { //skip .svn dirs
					//debug 
					//System.out.println("copying: " + src1 + " to " + dest1);
					
					copyFiles(src1, dest1);
				}
			}
		} else {

			FileInputStream fin = new FileInputStream(src);
			FileOutputStream fout = new FileOutputStream(dest);

			System.out.println("Copying File src:" + src + " dest:" + dest);
			
			int c;
			while ((c = fin.read()) >= 0)
				fout.write(c);

			fin.close();
			fout.close();

		}
	}

	
	public static void createFile(String File, String Contents) {
		
	    try {
	        File file = new File(File);
	    
	        // Create file if it does not exist
	        boolean success = file.createNewFile();
	        if (success) {
	        	// Write to file
                BufferedWriter out = new BufferedWriter(new FileWriter(File, true));
                out.write(Contents);
                out.close();

	        } else {
	            // File already exists
	        	(new File(File)).delete();
	        	createFile(File, Contents);
	        }
	    } catch (IOException e) {
	    }
	    
	}
	
	
	/**
	 * zip up the production folder
	 * 
	 * @throws IOException
	 */
	public static void zipProject() throws IOException {
		
		System.out.println("Starting Zipping of War");
		
		String ZipFile = getWarFileName();
	    String ZipUp = TempBuildFolder;
	    
	    System.out.println("WarZipFile: " + ZipFile + " ZipUpDir:" +ZipUp);
	    
	    //create a ZipOutputStream to zip the data to 
	    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(ZipFile));
	    zipDir(ZipUp, zos); 

	    zos.close(); //close the stream 
	}
	

	/**
	 * get zip archive name for tomcat deploy
	 * @return
	 */
	public static String getWarFileName() {
		String WarName = null;
		if (TempWarFileLocation == null) {
			WarName = ProjectDirectory + "/" + ProjectName + ".war";
		} else {
			WarName = TempWarFileLocation + "/" + ProjectName + ".war";
		}
		return WarName;
	}
	
	
	
	/**
	 * archive temp build folder
	 * @param dir2zip
	 * @param zos
	 * 
	 * TODO - skip .svn files
	 */
	public static void zipDir(String dir2zip, ZipOutputStream zos) {
		try {
			//create a new File object based on the directory we have to zip File    
			File zipDir = new File(dir2zip);

			//get a listing of the directory content 
			String[] dirList = zipDir.list();

			byte[] readBuffer = new byte[2156];
			int bytesIn = 0;

			//loop through dirList, and zip the files 
			for (int i = 0; i < dirList.length; i++) {

				File f = new File(zipDir, dirList[i]);

				if (f.isDirectory()) {
					//if the File object is a directory, call this 
					//function again to add its content recursively 
					String filePath = f.getPath();
					zipDir(filePath, zos);
					//loop again 
					continue;
				}

				//if we reached here, the File object f was not a directory 
				//create a FileInputStream on top of f 
				FileInputStream fis = new FileInputStream(f);

				//create a new zip entry 
				
				String filePath = f.getPath();
				filePath = getZipPath(filePath); //change the path to relative to the production dir
				ZipEntry anEntry = new ZipEntry(filePath);

				//place the zip entry in the ZipOutputStream object 
				zos.putNextEntry(anEntry);

				//now write the content of the file to the ZipOutputStream 
				while ((bytesIn = fis.read(readBuffer)) != -1) {
					zos.write(readBuffer, 0, bytesIn);
				}
				//close the Stream 
				fis.close();
			}
		} catch (Exception e) {
			//handle exception 
		}
	}
	
	/**
	 * Figure out the build path, so we can set the zip archive to relative path
	 * @param Path
	 * @return
	 */
	public static String getZipPath(String Path) {
		
		String path = null;
		
		///.*production/
		Pattern p = Pattern.compile("/.*production/(.*)");
		Matcher m = p.matcher(Path);
		boolean found = m.find();

		if (found == true) {
			path = m.group(1);
		}

		//debug
		System.out.println("Zip path: " + path);
		return path;
	}
	
	
}//end
