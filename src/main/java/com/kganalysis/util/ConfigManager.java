/**
 * @Copyright EIS University of Bonn
 */

package com.kganalysis.util;

import java.io.File; 
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties; 
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;


/**
 * The aim of this class is to load RDF (turtle based) configuration file for this program
 * containing the general configuration and the configuration of KPIs
 * 
 * @class ConfigManager
 * @Version 1.0
 * @Date 21.09.2016
 * @author Irlan Grangel
 **/

public class ConfigManager {

	static Properties configurationProperties;
	static Properties kpiConfigurationProperties;
	private static RDFNode configurationLiteral;
	private static RDFNode configurationPredicate;
	private static ArrayList<RDFNode> configurationLiterals, configurationPredicates;
	private static Model model;
	public static String filePath;

	public final static String URI_NAMESPACE = "http://w3id.org/i40/sto";

	/**
	 * This method loads the Configuration file parameters
	 */
	public static Properties loadConfig() {
		configurationProperties = new Properties();
		String dir = System.getProperty("user.dir");
		File configFile = new File(dir + "/config.ttl");

		if (configFile.isFile() == false) {
			System.out.println("Please specify the configuration file" + "(config.ttl)");
			System.exit(0);
		}

		if (configFile.length() == 0) {
			System.out.println("The configuration file (config.ttl) is empty");
			System.exit(0);
		}

		model = ModelFactory.createDefaultModel();
		InputStream inputStream = FileManager.get().open(configFile.getPath());
		model.read(new InputStreamReader(inputStream), null, "TURTLE");
		// parses an InputStream assuming RDF in Turtle format

		configurationLiterals = new ArrayList<RDFNode>();
		configurationPredicates = new ArrayList<RDFNode>();

		StmtIterator iterator = model.listStatements();

		while (iterator.hasNext()) {
			Statement stmt = iterator.nextStatement();

			configurationPredicate = stmt.getPredicate();
			configurationPredicates.add(configurationPredicate);

			configurationLiteral = stmt.getLiteral();
			configurationLiterals.add(configurationLiteral);
		}

		for (int i = 0; i < configurationPredicates.size(); ++i) {
			for (int j = 0; j < configurationLiterals.size(); ++j) {
				String key = configurationPredicates.get(j).toString();
				String value = configurationLiterals.get(j).toString();
				configurationProperties.setProperty(key, value);
			}
		}

		return configurationProperties;
	}


	

	/**
	 * Gets the general file path where all the files are located in the general configuration file (config.ttl)
	 * @return
	 */
	public static String getFilePath() {
		if(filePath != null){
			return filePath;
		}
		
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "path");
		
		File pathFile = new File(filePath);

		if (pathFile.isFile() == false) {
			System.out.println("Please specify the file path for the ontology in config.ttl");
			System.exit(0);
		}
		
		if (pathFile.length() == 0) {
			System.out.println("The ontology file is empty");
			System.exit(0);
		}
		
		return filePath;
	}

	/**
	 * Gets the general file path where all the files for the SPARQL queries are located
	 * in the general configuration file (config.ttl)
	 * @return
	 */
	public static String getQueriesPath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "queries_path");
		File pathFile = new File(filePath);

		if (!pathFile.isDirectory()) {
			System.out.println("Please specify the path for the queries folder in config.ttl");
			System.exit(0);
		}
		else{
			if(pathFile.list().length <= 0){
				System.out.println("The folder 'queries' is empty. Please add queries for executing KPIs");
				System.exit(0);
			}
		}
		return filePath;
	}

	/**
	 * Gets the general file path to the KPI ontology
	 * @return path of the ontology
	 */
	public static String getKPIOntoPath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "kpi_onto_path");
		if(filePath == null){
			System.out.println("KPI dump could not be executed because of missing property in config.ttl file: kpi_onto_path");
			System.exit(0);
		}
		return filePath;
	}



	public static String getCSVPath() {
		String filePath = loadConfig().getProperty(URI_NAMESPACE + "csv");
		return filePath;
	}

}
