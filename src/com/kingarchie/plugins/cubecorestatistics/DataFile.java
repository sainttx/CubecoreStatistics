package com.kingarchie.plugins.cubecorestatistics;


import java.io.File; 
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


/**
 * @author Justis R.
 * @version 2.0.1
 * @category FileManagment
 */
public class DataFile {

	private File file;

	/**
	 * Creates a file with the specified name within the server's main folder
	 * Forces file to have the extension ".myml" If file already exists, it
	 * simply gets the existing file.
	 * 
	 * @param name
	 *            The name of the file
	 */
	public DataFile(String name) {
		this("", name);
	}

	/**
	 * Creates a file at the specified path with the specified name within the
	 * server's main folder.
	 * Forces file to have the extension ".myml" If
	 * file already exists, it simply gets the existing file.
	 * 
	 * @param path
	 *            The path from the server's main folder to where the file is
	 *            or should be located
	 *            Example:
	 *            	"path/.../"
	 *            	"plugins/pluginName/.../"
	 * @param name
	 *            the name of the file
	 */
	public DataFile(String path, String name) {
		this(path, name, ".myml");
	}

	/**
	 * Creates a file at the specified path with the specified name within the
	 * server's main folder. The file's extension will be the set to the given type If
	 * file already exists, it simply gets the existing file.
	 * 
	 * @param path
	 *            The path from the server's main folder to where the file
	 *            is or should be located
	 *            Example:
	 *            	"path/.../"
	 *            	"plugins/pluginName/.../"
	 * @param name
	 *            The name of the file
	 * @param type
	 *            the file extension for this specific file
	 */
	public DataFile(String path, String name, String type) {
		File direc = new File(path);
		if (!direc.exists())
			direc.mkdirs();
		file = new File(path + name + type);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		reload();
	}
	
	/**
	 * Creates a DataFile object for specified file.
	 * @param file
	 * 			The File for which to wrap the DataFile object.
	 */
	public DataFile(File file) {
		try {
			file.createNewFile();
			this.file = file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		reload();
	}

	private Map<Integer, String> sec = new HashMap<>();
	private Map<Integer, String> content = new HashMap<>();
	private List<Object> listings = new ArrayList<Object>();

	/**
	 * Sets the value of the specified path to the specified value
	 * within memory
	 * 
	 * @param path
	 *            the path to the value (existing or not)
	 * @param value
	 *            the new value of the specified path
	 */
	public void set(String path, Object value) {
		int counter = 1;
		int lineNumber = 1;
		int mark = 1;
		section(counter, path);
		while (true) {
			String line = content.get(lineNumber);
			if (line == null || !isCommented(line)) {
				if (line != null && !isCommented(line) && line.startsWith(tab(counter))) {
					mark = lineNumber;
				}
 				if (line == null || (!isCommented(line) && !line.startsWith(tab(counter)))) {
					lineNumber = mark;
					if (content.get(lineNumber) == null)
						lineNumber--;
					if (counter <= sec.size()) {
						lineNumber++;
						for (int x = content.size(); x >= lineNumber; x--) {
							content.put(x + sec.size() - counter + 1, content.get(x));
							content.remove(content.get(x));
						}
						while (counter < sec.size()) {
							content.put(lineNumber, sec.get(counter));
							counter++;
							lineNumber++;
						}
					}
					counter = sec.size();
					if (value instanceof List) {
						setList(lineNumber, counter, mark, (List<?>) value);
						return;
					} else {
						content.put(lineNumber, sec.get(counter) + " " + value);
						return;
					}
				}
				if (line.startsWith(sec.get(counter))) {
					counter++;
				}
			}
			lineNumber++;
		}
	}

	/**
	 * @param lineNumber
	 * 		Line number that the list starts at
	 * @param counter
	 * 		How deep in sub-paths is this list starting at?
	 * @param mark
	 * 		Got to store the starting value so I can iterate through the list.
	 * @param value
	 * 		Well I've gotta return something, don't I?
	 * @return List
	 * 		Of strings, hopefully.. For the list at the location.
	 */
	private void setList(int lineNumber, int counter, int mark, List<?> value) {
		if (!content.get(lineNumber).startsWith(sec.get(counter)))
			content.put(lineNumber, sec.get(counter));
		mark = lineNumber;
		int last = lineNumber;
		lineNumber++;
		while (content.get(lineNumber) != null && (isCommented(content.get(lineNumber)) || isListing(content.get(lineNumber)))) {
			if (isListing(content.get(lineNumber)))
				last = lineNumber;
			lineNumber++;
		}
		int diff = ((List<?>) value).size() - (last - mark);
		lineNumber = last + 1;
		if (diff > 0) {
			for (int ind = content.size(); ind >= lineNumber; ind--) {
				content.put(ind + diff, content.get(ind));
			}
		} else if (diff < 0) {
			while (content.get(lineNumber) != null) {
				content.put(lineNumber + diff, content.get(lineNumber));
				content.remove(lineNumber);
				lineNumber++;
			}
		}
		for (Object o : value) {
			content.put(mark + 1, tab(counter) +  "- " + o);
			mark++;
		}
		
	}

	/**
	 * @param path
	 * 		to the path-name that should be renamed.
	 * 	
	 * @param pathName
	 * 		The new name of the final path.
	 * @throws NullPointerException
	 * 		if the path does not exist
	 */
	public void rename(String path, String pathName) {
		int counter = 1;
		int lineNumber = 1;
		section(counter, path);
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						content.put(lineNumber, tab(counter) + pathName);
						break;
					}
					counter++;
				}
			} else {
				throw new NullPointerException();
			}
			lineNumber++;
		}
	}
	
	/**
	 * @param path
	 * 		path to the line you wish to return the line number of
	 * 	
	 * @return integer
	 * 		the line number that this final path appears on.
	 * @throws NullPointerException
	 * 		if the path does not exist.
	 */
	public int lineNumber(String path) {
		int counter = 1;
		int lineNumber = 1;
		section(counter, path);
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						return lineNumber;
					}
					counter++;
				}
			} else {
				throw new NullPointerException();
			}
			lineNumber++;
		}
		throw new NullPointerException();
	}
	
	/**
	 * @param path
	 * 		the final path where the comment should appear over.
	 * @param comment
	 * 		A string comment to set above the path value
	 * @throws NullPointerException
	 * 		if the path does not exist.
	 * 
	 */
	public void comment(String path, String comment) {
		int counter = 1;
		int lineNumber = 1;
		section(counter, path);
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						for (int x = content.size(); x >= lineNumber; x--) {
							content.put(x + 1, content.get(x));
							content.remove(content.get(x));
						}
						content.put(lineNumber, tab(counter) + "# " + comment);
						break;
					}
					counter++;
				}
			} else {
				throw new NullPointerException();
			}
			lineNumber++;
		}
	}
	
	/**
	 * @param path
	 * 		the final path where the comments should appear over.
	 * @param comment
	 * 		An array of string comments to set above the path value
	 * @throws NullPointerException
	 * 		if the path does not exist.
	 */
	public void comment(String path, String[] comment) {
		if (pathExists(path))
		for (String c : comment) comment(path, c);
		else throw new NullPointerException();
	}
	
	/**
	 * @param path
	 * 		to the path that should be removed.
	 * 		This will also delete all sub-paths of that path.
	 * @throws NullPointerException
	 * 		if the path does not exist.
	 */
	public void removePath(String path) {
		int counter = 1;
		int lineNumber = 1;
		section(counter, path);
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						int last = lineNumber;
						int diff = lineNumber;
						lineNumber++;
						while (content.get(lineNumber) != null && (isListing(content.get(lineNumber)) || content.get(lineNumber).startsWith(tab(counter + 1)) || isCommented(content.get(lineNumber)))) {
							if (!isCommented(content.get(lineNumber))) {
								diff = lineNumber;
							}
							lineNumber++;
						}
						lineNumber = diff + 1;
						for (int i = last; i < lineNumber; i++) {
							content.remove(i);
						}
						for (int i = last; content.get(lineNumber) != null; i++){
							content.put(i, content.get(lineNumber));
							content.remove(lineNumber);
							lineNumber++;
						}
						return;
					}
					counter++;
				}
			} else {
				throw new NullPointerException();
			}
			lineNumber++;
		}
		throw new NullPointerException();
	}
	
	/**
	 * @param path
	 * 		to the comments to remove.
	 * 		(Comments directly above the specified path)
	 * @return true
	 * 		if there were comments and they were removed.
	 * 		False if there were not comments to remove
	 * @throws NullPointerException
	 * 		if the path does not exist.
	 */
	public boolean removeComments(String path) {
		int counter = 1;
		int lineNumber = 1;
		section(counter, path);
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						int last = lineNumber - 1;
						while (isCommented(content.get(last))) {
							last--;
						}
						int diff = last - lineNumber + 1;
						if (diff > 0) {
							while (content.get(lineNumber) != null) {
								content.put(lineNumber + (diff), content.get(lineNumber));
								content.remove(lineNumber);
								lineNumber++;
							}
							return true;
						}
						return false;
					}
					counter++;
				}
			} else {
				throw new NullPointerException();
			}
			lineNumber++;
		}
		throw new NullPointerException();
	}

	/**
	 * Gets the current value from memory of the specified path
	 * 
	 * @param path
	 *            The path the the value
	 */
	public Object get(String path) {
		String value = null;
		int counter = 1;
		int lineNumber = 1;
		section(counter, path);
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						value = line.replace(sec.get(counter) + " ", "");
						break;
					}
					counter++;
				}
			} else {
				break;
			}
			lineNumber++;
		}
		return value;
	}
	
	/**
	 * 
	 * @param path
	 * 		to the parent path.
	 * @return a set all the sub-paths to the parent path.
	 * 		Also works for empty parent paths.
	 * 	  ---------------------
	 * 		ParentPath:
	 * 		  SubPath:
	 * 		  SubPath2:
	 *          SubSubPath:
	 *    ---------------------
	 *    getPaths("ParentPath");
	 *    Will return a Set representation of {"SubPath","SubPath2"};
	 * @throws NullPointerException
	 * 		if the parent path does not exist.
	 * 	
	 */
	public Set<String> getPaths(String path) {
		Set<String> paths = new HashSet<>();
		int lineNumber = 1;
		int counter = 1;
		section(counter, path);
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						counter++;
						lineNumber++;
						break;
					}
					counter++;
				}
			} else {
				throw new NullPointerException();
			}
			lineNumber++;
		}
		while (content.get(lineNumber) != null && (content.get(lineNumber).startsWith(tab(counter)) || isCommented(content.get(lineNumber)))) {
			if (!isCommented(content.get(lineNumber)) && !content.get(lineNumber).startsWith(tab(counter + 1)) && !isListing(content.get(lineNumber))) {
				paths.add(content.get(lineNumber).trim().substring(0, content.get(lineNumber).trim().indexOf(":")));
			}
			lineNumber++;
		}
		return paths;
	}

	/**
	 * Returns the string contents of the whole file
	 */
	public String getContents() {
		String value = "";
		for (int x = 1; x <= content.size(); x++) {
			value = value + content.get(x) + "\n";
		}
		return value;
	}
	
	/**
	 * @param path
	 * 		to check for existence within the file.
	 * @return true 
	 * 		if the path exists, false if otherwise.
	 * 		
	 */
	public boolean pathExists(String path) {
		int counter = 1;
		int lineNumber = 1;
		while (content.get(lineNumber) != null && counter <= sec.size()) {
			String line = content.get(lineNumber);
			if (line.startsWith(tab(counter)) || isCommented(line)) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						return true;
					}
					counter++;
				}
			} else {
				break;
			}
			lineNumber++;
		}
		return false;
	}

	/**
	 * Returns a list at the specified path.
	 * Returns an empty list if it does not exist.
	 * 
	 * @param path
	 *            the path to the list
	 */
	public List<?> getList(String path) {
		listings.clear();
		int counter = 1;
		int lineNumber = 1;
		section(counter, path);
		String line = null;
		while (content.get(lineNumber) != null) {
			line = content.get(lineNumber);
			if (line.startsWith(tab(counter))) {
				if (line.startsWith(sec.get(counter))) {
					if (counter == sec.size()) {
						lineNumber++;
						break;
					}
					counter++;
				}
			}
			lineNumber++;
		}
		line = content.get(lineNumber);
		while (line != null && (line.startsWith(tab(counter) + "- ") || isCommented(line))) {
			if (line.startsWith(tab(counter) + "- "))
				listings.add(line.trim().replaceFirst("- ", ""));
			lineNumber++;
			line = content.get(lineNumber);
		}
		return listings;
	}

	/**
	 * Returns a list of strings (if there is one) at the specified path
	 * 
	 * @param path
	 *            the path to the list
	 */
	public List<String> getStringList(String path) {
		List<String> list = new ArrayList<String>();
		int counter = 0;
		getList(path);
		while (counter < listings.size()) {
			list.add(listings.get(counter).toString());
			counter++;
		}
		return list;
	}

	/**
	 * Returns a list of integers (if there is one) at the specified path
	 * 
	 * @param path
	 *            the path to the list
	 */
	public List<Integer> getIntList(String path) {
		List<Integer> list = new ArrayList<Integer>();
		getList(path);
		int counter = 0;
		while (counter < listings.size()) {
			list.add(Integer.parseInt(listings.get(counter).toString().trim()));
			counter++;
		}
		return list;
	}

	/**
	 * Returns a list of longs (if there is one) at the specified path
	 * 
	 * @param path
	 *            the path to the list
	 */
	public List<Long> getLongList(String path) {
		List<Long> list = new ArrayList<Long>();
		getList(path);
		int counter = 0;
		while (counter < listings.size()) {
			list.add(Long.parseLong(listings.get(counter).toString().trim()));
			counter++;
		}
		return list;
	}

	/**
	 * Returns a list of doubles (if there is one) at the specified path
	 * 
	 * @param path
	 *            the path to the list
	 */
	public List<Double> getDoubleList(String path) {
		List<Double> list = new ArrayList<Double>();
		getList(path);
		int counter = 0;
		while (counter < listings.size()) {
			list.add(Double.parseDouble(listings.get(counter).toString().trim()));
			counter++;
		}
		return list;
	}

	/**
	 * Returns a list of booleans (if there is one) at the specified path
	 * 
	 * @param path
	 *            the path to the list
	 */
	public List<Boolean> getBooleanList(String path) {
		List<Boolean> list = new ArrayList<Boolean>();
		getList(path);
		int counter = 0;
		while (counter < listings.size()) {
			list.add(Boolean.parseBoolean(listings.get(counter).toString().trim()));
			counter++;
		}
		return list;
	}

	/**
	 * gets a string value at the specified path
	 * 
	 * @param path
	 *            The path to the value
	 */
	public String getString(String path) {
		if (get(path) != null) {
			return get(path).toString();
		}
		return null;
	}

	/**
	 * gets a boolean value at the specified path
	 * 
	 * @param path
	 *            The path to the value
	 */
	public boolean getBoolean(String path) {
		return Boolean.parseBoolean(get(path).toString().trim());
	}

	/**
	 * gets a integer value at the specified path
	 * 
	 * @param path
	 *            The path to the value
	 */
	public int getInt(String path) {
		return Integer.parseInt(get(path).toString().trim());
	}

	/**
	 * gets a long value at the specified path
	 * 
	 * @param path
	 *            The path to the value
	 */
	public long getLong(String path) {
		return Long.parseLong(get(path).toString().trim());
	}

	/**
	 * gets a double value at the specified path
	 * 
	 * @param path
	 *            The path to the value
	 */
	public double getDouble(String path) {
		return Double.parseDouble(get(path).toString().trim());
	}

	/**
	 * gets a float value at the specified path
	 * 
	 * @param path
	 *            The path to the value
	 */
	public float getFloat(String path) {
		return Float.parseFloat(get(path).toString().trim());
	}

	/**
	 * Gets the contents of the file within the .jar at the path and copies it
	 * to the file
	 * 
	 * @param pathToResource
	 *            Path within the .jar to the file which you are getting the
	 *            contents of
	 * @param overWrite
	 *            Whether or not to overwrite all the contents of the file if
	 *            there are any.
	 *            
	 *	@Deprecated Due to issues with accessing paranoid protection domains after reloading.
	 */
	
    @Deprecated
	public DataFile copyDefaults(String pathToResource, boolean overWrite) {
		InputStream is = (DataFile.class.getClassLoader().getResourceAsStream(pathToResource));
		return copyDefaults(is, overWrite);
	}

	/**
	 * Gets the contents of the file within the .jar at the path and copies it
	 * to the file
	 * 
	 * @param InputStream
	 * 		<b>	is = MainClass.getPlugin(MainClass.class).getResorce(pathToResource); </b>
	 * @param overWrite
	 *            Whether or not to overwrite all the contents of the file if
	 *            there are any.
	 *            
	 */
	public DataFile copyDefaults(InputStream is, boolean overWrite) {
		if (overWrite || !file.exists() || isEmpty()) {
			if (is == null) {
				System.out.println("[Warning] " + file.getName() + "'s .jar file have been modified!");
				System.out.println("[Warning] Could not generate " + file.getName() + "!");
				System.out.println("[Warning] Please stop and restart the server completely!");
				return this;
			}
			try {
				Files.copy(is, file.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
			reload();
		}
		return this;
	}

	/**
	 * @return whether or not the file is empty 
	 * (will return false if the file contains any spaces)
	 * @throws FileNotFoundException
	 * 		If the file is not found. xP
	 */
	public boolean isEmpty() {
		Scanner input;
		try {
			input = new Scanner(file);
			if (input.hasNextLine()) {
				input.close();
				return false;
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Saves all the current contents of memory, all the paths and values,
	 * everything, to the file. Overwrites any and all existing contents of
	 * the file
	 * @throws IOException
	 * 		When the directory does not exist.
	 */
	public void save() {
		try {
			FileWriter fw = new FileWriter(file, false);
			for (int x = 1; x <= content.size(); x++) {
				fw.write(content.get(x) + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves all of the file's contents, all the paths, values, everything, to
	 * memory. Overwrites any and all existing contents of memory
	 * @throws Exceptions when the directory does not exist.
	 */
	public void reload() {
		try {
			File direc = new File(file.getPath().replace(file.getName(), ""));
			if (!direc.exists())
				direc.mkdirs();
			if (!file.exists())
				file.createNewFile();
			content.clear();
			int lineNumber = 0;
			Scanner input = new Scanner(file);
			while (input.hasNextLine()) {
				String line = input.nextLine();
				lineNumber++;
				content.put(lineNumber, line);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes the file.. Obviously.
	 */
	public void delete() {
		file.delete();
	}

	/**
	 * @return Whether or not the current line is commented out or blank
	 */
	private boolean isCommented(String line) {
		if (line.trim().startsWith("#") || line.trim().isEmpty())
			return true;
		return false;
	}
	
	private boolean isListing(String line) {
		if (line.trim().startsWith("- "))
			return true;
		return false;
	}

	/**
	 * Takes the path and splits it, according to counter, into what it will
	 * look like within the file. This means adding the correct amount of spaces
	 * and colon in front of it.
	 * 
	 * @param counter
	 *            how far into the path are we starting? (probably 1)
	 * @param path
	 *            What is the whole path?
	 */
	private void section(int counter, String path) {
		sec.clear();
		if (path.isEmpty()) return;
		String[] pathx = path.split("\\.");
		for (String x : pathx) {
			x = x + ":";
			sec.put(counter, tab(counter) + x);
			counter++;
		}
	}

	/**
	 * Returns the correct amount of spaces to put in front of a path depending
	 * on it's location within the whole path.
	 * 
	 * @param tabNumber
	 *            How far into the whole path is this specific path name
	 */
	private String tab(int tabNumber) {
		String tab = "";
		for (int x = tabNumber; x > 1; x--) {
			tab = tab + "  ";
		}
		return tab;
	}
	
	/**
	 * @return the file this object is associated with.
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * 
	 * @param string
	 * @return
	 * 
	 * Since java's File.rename() does not work on all platforms, here's your alternative.
	 * 
	 */
	
	/**
	 * @param folderLoc
	 * 		The path from the main server folder to the destination folder
	 * @return
	 * 		Set of all files contained in the destination folder.
	 */
	public static Set<DataFile> getFolderContents(String folderLoc) {
		Set<DataFile> files = new HashSet<>();
		File file = new File(folderLoc);
		if (!file.exists())
			file.mkdirs();
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				files.add(new DataFile(f));
			}
			return files;
		}
		return null;
	}
}