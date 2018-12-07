package org.futto.app.storage;

import android.content.Context;
import android.util.Log;

import org.futto.app.CrashHandler;
import org.futto.app.listeners.GPSListener;
import org.futto.app.listeners.PowerStateListener;
import org.futto.app.listeners.WifiListener;
import org.futto.app.survey.AudioRecorderActivity;
import org.futto.app.survey.AudioRecorderEnhancedActivity;
import org.futto.app.survey.SurveyAnswersRecorder;
import org.futto.app.survey.SurveyTimingsRecorder;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**The (Text)FileManager.
 * The FileManager is implemented as a Singleton.  More accurately the static object contains several
 * singletons, static instances of FileManager Objects.  Before using the FileManager the app must
 * provide it with a Context and call the Start() function.  Failure to do so causes the app to crash.
 * This Is Intentional.  The Point of the app is to record data.
 * The Reason for this construction is to construct a file write system where there is only ever a
 * single pointer to each file type, and that these files are never overwritten, written to asynchronously,
 * or left accidentally empty.
 * The files handled here are the GPSFile, accelFile, powerStateLog, audioSurveyInfo, callLog, textsLog, surveyTimings,
 * currentDailyQuestions, currentWeeklyQuestions, deviceData, and debugLogFile.
 * On construction you provide a boolean flag ("persistent").  Persistent files do not get overwritten on application start.
 * To access a file use the following construction: TextFileManager.getXXXFile()
 * @author Eli */
public class TextFileManager { 

	//Delimiter and newline strings
	public static final String DELIMITER = ",";
	
	//Static instances of the individual FileManager objects.
	private static TextFileManager GPSFile;
	private static TextFileManager powerStateLog;
	private static TextFileManager debugLogFile;

	private static TextFileManager surveyTimings;
	private static TextFileManager surveyAnswers;
	private static TextFileManager wifiLog;
		
	private static TextFileManager keyFile;
	
	//"global" static variables
	private static Context appContext;
	private static int GETTER_TIMEOUT = 75; //value is in milliseconds
	private static String getter_error = "Tried to access %s before calling TextFileManager.start().";
	private static String broken_getter_error = "Tried to access %s before calling TextFileManager.start(), but the timeout failed.";
	private static void throwGetterError(String sourceName) { throw new NullPointerException( String.format(getter_error, sourceName) ); }
	private static void throwTimeoutBrokeGetterError(String sourceName) { throw new NullPointerException( String.format(broken_getter_error, sourceName) ); }
	
	//public static getters.
	// These are all simple and nearly identical, so they are squished into one-liners.
	// checkAvailableWithTimeout throws an error and the app restarts if the TextFile is unavailable.
	public static TextFileManager getGPSFile() { checkAvailableWithTimeout("GPSFile"); return GPSFile; }
	public static TextFileManager getPowerStateFile() { checkAvailableWithTimeout("powerStateLog"); return powerStateLog; }
	public static TextFileManager getWifiLogFile() { checkAvailableWithTimeout("wifiLog"); return wifiLog; }
	public static TextFileManager getSurveyTimingsFile() { checkAvailableWithTimeout("surveyTimings"); return surveyTimings; }
	public static TextFileManager getSurveyAnswersFile() { checkAvailableWithTimeout("surveyAnswers"); return surveyAnswers; }
	//(the persistent files)
	public static TextFileManager getDebugLogFile() { checkAvailableWithTimeout("debugLogFile"); return debugLogFile; }
	public static TextFileManager getKeyFile() { checkAvailableWithTimeout("keyFile"); return keyFile; }
	
	/** Checks the availability of a given TextFile, returns true if available, false otherwise. */
	private static Boolean checkTextFileAvailable(String thing) {
		//the check for availability is whether the appropriate variable is allocated

		if (thing.equals("GPSFile") ) { return (GPSFile != null); }
		if (thing.equals("powerStateLog") ) { return (powerStateLog != null); }
		if (thing.equals("wifiLog") ) { return (wifiLog != null); }
		if (thing.equals("surveyTimings") ) { return (surveyTimings != null); }
		if (thing.equals("surveyAnswers") ) { return (surveyAnswers != null); }
		if (thing.equals("debugLogFile") ) { return (debugLogFile != null); }
		if (thing.equals("keyFile") ) { return (keyFile != null); }
		throw new NullPointerException(String.format("invalid key %s provided for checking available text file.", thing));
	}
	
	/** We check for the availability of the given TextFile, if it fails to exist we wait GETTER_TIMEOUT milliseconds and then try again. 
	 * On a regular case error we throw the getter error, if the sleep operation breaks we throw the broken timeout error. */
	private static void checkAvailableWithTimeout(String textFile) {
		if ( !checkTextFileAvailable(textFile) ) {
			try {
				Thread.sleep(GETTER_TIMEOUT);
				if ( !checkTextFileAvailable(textFile) ) { throwGetterError(textFile); }
			}
			catch (InterruptedException e) { throwTimeoutBrokeGetterError(textFile); }
		}
	}
	
	//and (finally) the non-static object instance variables
	public String name = null;
	public String fileName = null;
	private String header = null;
	private Boolean persistent = null;
	private Boolean encrypted = null;
	private Boolean isDummy = true;
	private byte[] AESKey = null;
	
	/*###############################################################################
	########################### Class Initialization ################################
	###############################################################################*/
	
	/**Starts the TextFileManager
	 * This must be called before code attempts to access files using getXXXFile().
	 * Initializes all TextFileManager object instances.  Initialization is idempotent.
	 * @param appContext a Context, provided by the app. */
	public static synchronized void initialize(Context appContext){
		//the key file for encryption (it is persistent and never written to)
		keyFile = new TextFileManager(appContext, "keyFile", "", true, true, false, false);
		// Persistent files (old, no longer used, but this is an example of a persistent file (one that does not get abandoned at shut-down/initialization) )
//		currentDailyQuestions = new TextFileManager(appContext, "currentDailyQuestionsFile.json", EMPTY_HEADER, true, true, false);
//		currentWeeklyQuestions = new TextFileManager(appContext, "currentWeeklyQuestionsFile.json", EMPTY_HEADER, true, true, false);
		// The debug file is no longer persistent, so that we can upload it to the server associated with a user, otherwise it has the name "logfile.txt" and fails to upload.
		debugLogFile = new TextFileManager(appContext, "logFile", "THIS LINE IS A LOG FILE HEADER", false, false, true, false);
		// Regularly/periodically-created files
		GPSFile = new TextFileManager(appContext, "gps", GPSListener.header, false, false, true, !PersistentData.getGpsEnabled());
		powerStateLog = new TextFileManager(appContext, "powerState", PowerStateListener.header, false, false, true, !PersistentData.getPowerStateEnabled());
		// Files created on specific events/written to in one go.
		surveyTimings = new TextFileManager(appContext, "surveyTimings_", SurveyTimingsRecorder.header, false, false, true, false);
		surveyAnswers = new TextFileManager(appContext, "surveyAnswers_", SurveyAnswersRecorder.header, false, false, true, false);
		wifiLog = new TextFileManager(appContext, "wifiLog", WifiListener.header, false, false, true, !PersistentData.getWifiEnabled());
	}
	
	/*###############################################################################
	################## Instance Construction and Initialization #####################
	###############################################################################*/
	
	/** This class has a PRIVATE constructor.  The constructor is only ever called 
	 * internally, via the static initialize() function, it creatse the "FileHandlers" used throughout the codebase. 
	 * @param appContext A Context.
	 * @param name The file's name.
	 * @param header The first line of the file.  Leave empty if you don't want a header, remember to include a new line at the end of the header.
	 * @param persistent Set this to true for a persistent file.  Persistent files are not currently encryptable.
	 * @param openOnInstantiation This boolean value dictates whether the file should be opened, mostly this is used in conjunction persistent files so that they can be read from.
	 * @param encrypted Set this to True if the file will have encrypted writes. */
	private TextFileManager(Context appContext, String name, String header, Boolean persistent, Boolean openOnInstantiation, Boolean encrypted, Boolean isDummy ){
		TextFileManager.appContext = appContext;
		if ( persistent && encrypted ) { throw new NullPointerException("Persistent files do not support encryption."); }
		this.name = name;
		this.header = header;
		this.persistent = persistent;
		this.encrypted = encrypted;
		this.isDummy = isDummy;
		//if (isDummy) { Log.e("TextFileManager", "creating dummy handle for " + this.name); }
		if (openOnInstantiation) { this.newFile(); } //immediately creating a file on instantiation was a common code pattern.
	}
	
	/** Makes a new file.
	 * Persistent files do not get a time stamp.
	 * Encrypted files get a key and have the key encrypted using RSA and written as the first line of the file.
	 * If a file has a header it is written as the second line.
	 * Fails when files are not allowed to be written to. (the rule is no encrypted writes until registraction is complete.
	 * @return A boolean value of whether a new file has been created.*/
	public synchronized boolean newFile(){
		if (this.isDummy) { return false; }
		//handle the naming cases for persistent vs. non-persistent files
		if ( this.persistent ) { this.fileName = this.name; } 
		else { // if user has not registered, stop non-persistent file generation
			if ( !PersistentData.isRegistered() ) { return false; }
			this.fileName = PersistentData.getPatientID() + "_" + this.name + "_" + System.currentTimeMillis() + ".csv";
		}
		//write the key to the file (if it has one)
		if ( this.encrypted ) {
			this.AESKey = EncryptionEngine.newAESKey();
			try { this.writePlaintext( EncryptionEngine.encryptRSA( this.AESKey ) ); }
			catch (InvalidKeySpecException e) {
				Log.e("initializing a file", "could not get key, this is not expected behavior?");
				CrashHandler.writeCrashlog(e, appContext); }
		}
		//write the csv header, if the file has a header
		if ( header != null && header.length() > 0 ) {
			this.writeEncrypted(header); }
		return true;
	}
	
	/** If it's a SurveyAnswers or SurveyTimings file, we want to append the
	 * Survey ID so that the file name reads like this:
	 * [USERID]_SurveyAnswers[SURVEYID]_[TIMESTAMP].csv
	 * @param surveyId */
	//does not require dummy check, just setting attributes on the in-memory variable
	public synchronized void newFile(String surveyId) {
		String nameHolder = this.name;
		this.name += surveyId;
		newFile(); //We do not care about return value, it is only used for handling encrypted files.
		this.name = nameHolder;
	}
	
	/*###############################################################################
	########################## Read and Write Operations ############################
	###############################################################################*/
	
	/** Takes a string. writes that to the file, adds a new line to the string.
	 * Prints a stacktrace on a write error, but does not crash. If there is no
	 * file, a new file will be created.
	 * @param data any unicode valid string*/
	public synchronized void writePlaintext(String data){
		if (this.isDummy) { return; }
		if (fileName == null) this.newFile();
		FileOutputStream outStream;
		try {							//write the output, we always want mode append
			outStream = appContext.openFileOutput(this.fileName, Context.MODE_APPEND);
			outStream.write( ( data ).getBytes() );
			outStream.write( "\n".getBytes() );
			outStream.flush();
			outStream.close(); }
		catch (FileNotFoundException e) {
			Log.e("TextFileManager", "could not find file to write to, " + this.fileName);
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext); }
		catch (IOException e) {
			Log.e("TextFileManager", "error in the write operation: " + e.getMessage() );
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext); }
	}
	
	/**Encrypts string data and writes it to a file.
	 * @param data any unicode valid string */
	public synchronized void writeEncrypted(String data) {
		if (this.isDummy) { return; }
		if ( !this.encrypted ) throw new NullPointerException( this.name + "is not supposed to have encrypted writes!" );
		if ( fileName == null ) { //when newFile fails we are not allowed to write to files.
			if (!this.newFile() ) { return; }
		}
		
		try { this.writePlaintext( EncryptionEngine.encryptAES( data, this.AESKey ) ); }
		catch (InvalidKeyException e) {
			Log.e("TextFileManager", "encrypted write operation without an AES key: " + this.name + ", " + this.fileName);
			CrashHandler.writeCrashlog(e, appContext);
//			throw new NullPointerException("encrypted write operation without an AES key: " + this.fileName );
		}
		catch (InvalidKeySpecException e) { //this occurs when an encrypted write operation occurs without an RSA key file, we eat this error because it only happens during registration/initial config.
			Log.e("TextFileManager", "EncryptionEngine.AES_TOO_EARLY_ERROR: " + this.name + ", " + data);
			e.printStackTrace(); }
	}
	
	/**@return A string of the file contents. */
	public synchronized String read() {
		if (this.isDummy) { return this.name + " is a dummy file."; }
		BufferedInputStream bufferedInputStream;
		StringBuffer stringBuffer = new StringBuffer();
		int data;
		try {  //Read through the (buffered) input stream, append to a stringbuffer.  Catch exceptions
			bufferedInputStream = new BufferedInputStream( appContext.openFileInput(fileName) );
			try { while( (data = bufferedInputStream.read()) != -1)
				stringBuffer.append((char)data); }
			catch (IOException e) {
				Log.e("Upload", "read error in " + this.fileName);
				e.printStackTrace();
				CrashHandler.writeCrashlog(e, appContext); }
			bufferedInputStream.close(); }
		catch (FileNotFoundException e) {
			Log.e("TextFileManager", "file " + this.fileName + " does not exist");
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext); }
		catch (IOException e){
			Log.e("DataFileManager", "could not close " + this.fileName);
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext);}
		
		return stringBuffer.toString();
	}
	
	
	/*###############################################################################
	#################### Miscellaneous Utility Functions ############################
	###############################################################################*/
	
	/** Delete the reference to the file so that it can be uploaded */
	public synchronized void closeFile() { this.fileName = null; }
	
	/** Deletes a file in the safest possible way, based on the file type (persistent-nonpersistent). */
	public synchronized void deleteSafely() {
		if (this.isDummy) { return; }
		String oldFileName = this.fileName;
		// For files that are persistant we have to do a slightly unsafe deletion, for everything else
		// we allocate the new file and then delete the old file.
		
		if ( this.persistent ) { //delete then create (unsafe, potential threading issues)
			TextFileManager.delete(oldFileName);
			this.newFile(); }
		else { 					//create then delete
//			this.newFile();
			TextFileManager.delete(oldFileName); }
	}
	
	/** Deletes a file.  Exists to make file deletion thread-safe.
	 * @param fileName */
	public static synchronized void delete(String fileName){
		try { appContext.deleteFile(fileName); }
		catch (Exception e) {
			Log.e("TextFileManager", "cannot delete file " + fileName );
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext); }
	}
	
	/** Make new files for all the non-persistent files. */
	public static synchronized void makeNewFilesForEverything() {
//		Log.d("TextFileManager.java", "makeNewFilesForEverything() called");
		GPSFile.newFile();

		powerStateLog.newFile();
		debugLogFile.newFile();
	}
	
	/** Very simple function, exists to make any function that needs to grab all extant files thread-safe.
	 * DO NOT USE THIS FUNCTION, USE getAllFilesSafely() INSTEAD.
	 * @return a string array of all files in the app's file directory. */
	public static synchronized String[] getAllFiles() { return appContext.getFilesDir().list(); }
		
	/** Returns all data that are not currently in use
	 * @return String[] a list of file names */
	public static synchronized String[] getAllUploadableFiles() {
		Set<String> files = new HashSet<String>();
		Collections.addAll(files, getAllFiles());
		
		// These files should never be uploaded
		files.remove(TextFileManager.getKeyFile().fileName);
		files.remove(AudioRecorderActivity.unencryptedTempAudioFileName);
		files.remove(AudioRecorderEnhancedActivity.unencryptedRawAudioFileName);
		files.remove(AudioRecorderEnhancedActivity.unencryptedTempAudioFileName); //should be identical to regular audiorecording file, but keep in case it changes.
		
		// These files are currently being written to, so they shouldn't be uploaded now
		files.remove(TextFileManager.getGPSFile().fileName);
		files.remove(TextFileManager.getPowerStateFile().fileName);
		files.remove(TextFileManager.getDebugLogFile().fileName);

		// These files are only occasionally open, but they may be currently open. If they are, don't upload them
		files.remove(TextFileManager.getSurveyAnswersFile().fileName);
		files.remove(TextFileManager.getSurveyTimingsFile().fileName);
		files.remove(TextFileManager.getWifiLogFile().fileName);

		return files.toArray(new String[files.size()]);
	}
	
	/*###############################################################################
	######################## DEBUG STUFF ############################################
	###############################################################################*/
	
	/** Returns a list of file names, all files in that list are retired and will not be written to again.
	 * @return a string array of files*/
	public static synchronized String[] getAllFilesSafely() {
		String[] file_list = getAllFiles();
		makeNewFilesForEverything();
		return file_list;
	}
		
	/**For Debug Only.  Deletes all files, creates new ones. */
	public static synchronized void deleteEverything() {
		//Get complete list of all files, then make new files, then delete all files from the old files list.
		Set<String> files = new HashSet<String>(); 
		Collections.addAll(files, getAllFilesSafely());
		
		//Need to do this crap or else we end up deleting the persistent files repeatedly
		files.remove(TextFileManager.getDebugLogFile().fileName);
		TextFileManager.getDebugLogFile().deleteSafely();
		files.remove(TextFileManager.getKeyFile().fileName);
		
		//and delete things
		for (String file_name : files) {
//			Log.i("deleting file", file_name);
			try { appContext.deleteFile(file_name); }
			catch (Exception e) {
				Log.e("TextFileManager", "could not delete file " + file_name); 
				e.printStackTrace(); }
		}
	}	
}
