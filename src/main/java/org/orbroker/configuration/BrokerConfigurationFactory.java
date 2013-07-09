package org.orbroker.configuration;

import static java.lang.String.format;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.orbroker.Ingester;
import org.xml.sax.SAXParseException;

public class BrokerConfigurationFactory {
	
	private static final Logger logger = Logger.getLogger(BrokerConfigurationFactory.class);
	
	private List<File> paths = new LinkedList<File>();
	
	public static BrokerConfigurationFactory getFactory(List<File> paths){
		if (paths == null){
			throw new IllegalStateException("Configuration path list MUST NOT be null.");
		}
		
		BrokerConfigurationFactory factory = new BrokerConfigurationFactory();
		for (File path: paths) {
			factory.add(path);
		}
		return factory;
	}
	
	public static BrokerConfigurationFactory getFactory(File path){
		if (path == null){
			throw new IllegalStateException("Configuration path MUST NOT be null.");
		}
		
		BrokerConfigurationFactory factory = new BrokerConfigurationFactory();
		factory.add(path);			
		return factory;
	}
	
	public void add(File path){
		if (!path.isDirectory()){
			this.paths.add(path);
		} else {
			addAllChildren(path);
		}
	}
	
	protected void addAllChildren(File path){		
		if (!path.isDirectory()){
			return;
		} 
		
		File[] files = path.listFiles(new XmlFilter());
		for (File file : files){
			this.add(file);
		}
	}
	
	public BrokerConfiguration getBrokerConfiguration() {
		
		File currentFile = null;
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			
			Ingester ingester = new Ingester();

			for (int i = 0; i < paths.size(); i++){
				currentFile = paths.get(i);
				if (!currentFile.canRead()){
					logger.warn(format("Skipping unexistent or unreadable '%s'", currentFile.getName()));
					continue;
				}

				if (logger.isDebugEnabled()){
					logger.debug("Parsing: " + currentFile);
				}
				
				parser.parse(currentFile, ingester);
			}
			
			return new BrokerConfiguration(ingester.getStatements().values(), 
													ingester.getBindings().values());
			
		} catch (SAXParseException e) {
			String msg = format("Problem parsing '%s', around %d:%d. Reason: %s", 
								currentFile, e.getLineNumber(), e.getColumnNumber(), e.getMessage());
			throw new RuntimeException(msg, e);		
		} catch (Exception e) {
			throw new RuntimeException(format("Problem parsing '%s'. %s", currentFile, e.getMessage()), e);		
		}
	}
	
	class XmlFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return dir.isDirectory() || (dir.isFile() && name.contains(".xml"));
		}
	}
}