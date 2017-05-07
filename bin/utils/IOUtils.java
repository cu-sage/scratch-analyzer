package utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import Directory;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/** This class emulates the new Java 7 "Try-With-Resources" statement.
 * Remove once Lucene is on Java 7.
 * @lucene.internal 
 * 
 * Edited and expanded by JBender for use in Scratch Analyzer
 * */
public final class IOUtils {
  
  /**
   * UTF-8 {@link Charset} instance to prevent repeated
   * {@link Charset#forName(String)} lookups
   * @deprecated Use {@link StandardCharsets#UTF_8} instead.
   */
  @Deprecated
  public static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;
  
  private static final String SE_INDENT = "\t";
  private static final String SE_OBJ_OPEN = "<<";
  private static final String SE_OBJ_CLOSE = ">>";
  private static final String OPERATOR = "utils.Operator";
  
  /**
   * UTF-8 charset string.
   * <p>Where possible, use {@link StandardCharsets#UTF_8} instead,
   * as using the String constant may slow things down.
   * @see StandardCharsets#UTF_8
   */
  public static final String UTF_8 = StandardCharsets.UTF_8.name();
  
  private IOUtils() {} // no instance
 
  
  /**
   * Deletes one or more files or directories (and everything underneath it).
   * 
   * @throws IOException if any of the given files (or their subhierarchy files in case
   * of directories) cannot be removed.
   */
  public static void rm(Path... locations) throws IOException {
    LinkedHashMap<Path,Throwable> unremoved = rm(new LinkedHashMap<Path,Throwable>(), locations);
    if (!unremoved.isEmpty()) {
      StringBuilder b = new StringBuilder("Could not remove the following files (in the order of attempts):\n");
      for (Map.Entry<Path,Throwable> kv : unremoved.entrySet()) {
        b.append("   ")
         .append(kv.getKey().toAbsolutePath())
         .append(": ")
         .append(kv.getValue())
         .append("\n");
      }
      throw new IOException(b.toString());
    }
  }

  private static LinkedHashMap<Path,Throwable> rm(final LinkedHashMap<Path,Throwable> unremoved, Path... locations) {
    if (locations != null) {
      for (Path location : locations) {
        // TODO: remove this leniency!
        if (location != null && Files.exists(location)) {
          try {
            Files.walkFileTree(location, new FileVisitor<Path>() {            
              @Override
              public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
              }
              
              @Override
              public FileVisitResult postVisitDirectory(Path dir, IOException impossible) throws IOException {
                assert impossible == null;
                
                try {
                  Files.delete(dir);
                } catch (IOException e) {
                  unremoved.put(dir, e);
                }
                return FileVisitResult.CONTINUE;
              }
              
              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                  Files.delete(file);
                } catch (IOException exc) {
                  unremoved.put(file, exc);
                }
                return FileVisitResult.CONTINUE;
              }
              
              @Override
              public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                if (exc != null) {
                  unremoved.put(file, exc);
                }
                return FileVisitResult.CONTINUE;
              }
            });
          } catch (IOException impossible) {
            throw new AssertionError("visitor threw exception", impossible);
          }
        }
      }
    }
    return unremoved;
  }
  
  /**
   *  Load all .se files in all subdirectories into memory 
   */
  public static void LoadSEDirectory(Path sePath, TreeMap<Integer, ArrayList<Tree<Block>>> userProjects) throws IOException {
	  DirectoryStream.Filter<Path> filt = new DirectoryStream.Filter<Path>() {
	        @Override
	        public boolean accept(Path file) throws IOException {
	            return (Files.isDirectory(file));
	        }
	    };

	    int userID = 0;
	    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(sePath, filt)) {
	        for (Path path : dirStream) {
	        	userID = Integer.parseInt(path.getFileName().toString());
	        	try (DirectoryStream<Path> seStream = Files.newDirectoryStream(path, "*.se")) {
	    			for (Path seFile : seStream) {
	    				loadSEFile(seFile, userProjects, userID);
	    			}
	        	} catch (IOException e) {
	        		e.printStackTrace();
	        	}
	        }
	    }	 
  }
  
  /**
   * Load a single .se file into memory by traversing line-by-line
   */
  private static void loadSEFile(Path seFile, TreeMap<Integer, ArrayList<Tree<Block>>> userProjects, int userID) {
	  Tree<Block> project = null;
	  try (BufferedReader reader = Files.newBufferedReader(seFile, StandardCharsets.UTF_8)) {
		  String line = null;
		  String objName = null;
		  Block parentObject = null;
		  Block parentBlock = null;
		  Block currentBlock = null;
		  String projectName = seFile.getFileName().toString();
		  int globalLevel = 0;
		  int localLevel = 0;
		  Pattern patIndent = Pattern.compile(SE_INDENT);
		  while ((line = reader.readLine()) != null) {
			  localLevel = 0;
			  if (line.contains(SE_OBJ_OPEN)) {
				  // an object
				  objName = line.substring(line.indexOf(SE_OBJ_OPEN) + SE_OBJ_OPEN.length(),
						  line.indexOf(SE_OBJ_CLOSE));
				  currentBlock = new Block(objName, null);
				  if (project == null) {
					  project = new Tree<Block>(currentBlock, projectName);
					  parentObject = currentBlock;
				  }
				  else
					  project.addLeaf(parentObject, currentBlock);
			  }
			  else if (line.contains(SE_INDENT)) {
				  // manage parent/child relationships
				  Matcher matches = patIndent.matcher(line);
				  while(matches.find()) {
					  ++localLevel;
				  }
				  if(localLevel > globalLevel) 
				  { // blocks can have children only one level down
					  parentBlock = currentBlock; 
					  globalLevel = localLevel;
				  }
				  else if (localLevel < globalLevel)
				  { // blocks can have parents multiple levels up
					  for (int i=localLevel; i<globalLevel; ++i) {
						  if(currentBlock.getBlockName() != null) // already at Object level
							  currentBlock = project.getTree(currentBlock).getParent().getHead();
						  if(currentBlock.getBlockName() != null) // already at Object level
							  parentBlock = project.getTree(currentBlock).getParent().getHead();
					  }
					  globalLevel = localLevel;
				  }
			  }
			  
			  if(!line.contains(SE_OBJ_OPEN)) {
				  // not an object, a block
				  currentBlock = new Block(line.replaceAll(SE_INDENT, "").trim());
				  project.addLeaf(parentBlock, currentBlock);
			  }
		  }
		  
	  } catch (IOException e) {
		  throw new RuntimeException(e);
	  }
	  
	  addProject(userID, project, userProjects);
  }
  
  /**
   * Add a project to a per-user ArrayList of projects each represented as Tree<Block>
   */
  private static void addProject(int userID, Tree<Block> project, TreeMap<Integer, ArrayList<Tree<Block>>> userProjects)
  {
	  if(userProjects.containsKey(userID))
		  userProjects.get(userID).add(project);
	  else
	  {
		  ArrayList<Tree<Block>> al = new ArrayList<Tree<Block>>();
		  al.add(project);
		  userProjects.put(userID, al);
	  }
  }

  /**
   * Dynamically load a user-supplied Operator class for use in Scratch Traverser
   */
  public static Class<?> loadClass(Path path) throws ClassNotFoundException, IOException {
	 URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { path.toUri().toURL() } );
	 return Class.forName(OPERATOR, true, classLoader);
  }
  
}
