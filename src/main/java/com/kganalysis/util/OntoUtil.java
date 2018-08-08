package com.kganalysis.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import com.google.common.base.Optional;

/**
 * Defines methods to load an ontology via OWL API
 * 
 * @class OntoUtil
 * @Version 1.0
 * @Date 01.05.2018
 * @author Irlan Grangel
 */

public class OntoUtil {
	
	/**
	 * Loads an ontology based on a file
	 * @param file
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public static OWLOntology loadOntology(File file) throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = createOntologyManager();
		return ontologyManager.loadOntologyFromOntologyDocument(file);
	}
	
	/**
	 * Loads an ontology based on a url
	 * @param file
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public static OWLOntology loadOntology(IRI ontologyIRI) throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = createOntologyManager();
		return ontologyManager.loadOntology(ontologyIRI);
	}
	
	/**
	 * Creates an ontology manager with required default parameters
	 * @param file
	 * @return
	 */
	public static OWLOntologyManager createOntologyManager() {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration config = ontologyManager.getOntologyLoaderConfiguration();
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		config = config.setStrict(false);
		config = config.setLoadAnnotationAxioms(true);
		ontologyManager.setOntologyLoaderConfiguration(config);
		return ontologyManager;
	}
	
	/**
	 * Gets the ontology URI of the current ontology 
	 * @param ontology
	 * @return the ontology URI as String
	 */
	public static String getOntologyIRI(OWLOntology ontology){
		
		if(ontology.getOntologyID().getOntologyIRI().toString().contains("Optional")){
			Optional<IRI> tempIRI = ontology.getOntologyID().getOntologyIRI();
			return tempIRI.get().toString();
		}else{
			return ontology.getOntologyID().getOntologyIRI().toString();
		}
		
	}
	

	/**
	 * Return the english value of the label of a given OWLEntity
	 * @param OWLEntity e
	 * @param ontology
	 * @return the rdfs:label value of a given OWLEntity
	 */
	public static String getLabelEntity(OWLEntity e, OWLOntology ont) {
		for (OWLAnnotation an : EntitySearcher.getAnnotations(e, ont)) {
			if (an.getProperty().isLabel()) {
				OWLAnnotationValue val = an.getValue();
				if (val instanceof IRI) {
					return ((IRI) val).toString();
				} else if (val instanceof OWLLiteral) {
					OWLLiteral lit = (OWLLiteral) val;
					if (lit.hasLang("en")) {
						return lit.getLiteral();
					}
				} 
			}
		}
		return "";
	}
	
	/**
	 * Return the english value of the comment of a given OWLEntity
	 * @param OWLEntity e
	 * @param ontology
	 * @return the rdfs:comment value of a given OWLEntity
	 */
	public static String getCommentEntity(OWLEntity e, OWLOntology ont) {
		for (OWLAnnotation an : EntitySearcher.getAnnotations(e, ont)) {
			if (an.getProperty().isComment()) {
				OWLAnnotationValue val = an.getValue();
				if (val instanceof IRI) {
					return ((IRI) val).toString();
				} else if (val instanceof OWLLiteral) {
					OWLLiteral lit = (OWLLiteral) val;
					if (lit.hasLang("en")) {
						return lit.getLiteral();
					}
				} 
			}
		}
		return "";
	}
	
	/**
	 * Return the expressivity of the ontology
	 * @param ontologyManager
	 * @return
	 */
	public static String getOntExpressivity(OWLOntologyManager ontologyManager){
		DLExpressivityChecker checker = new DLExpressivityChecker(ontologyManager.getOntologies());
		String expresivity = checker.getDescriptionLogicName();
		return expresivity;
	}
	
	/**
	 * This method validates the URLs 
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static boolean checkURL(String ontoUrl){
		boolean toReturn = false;
		//System.out.println("URL " + ontoUrl);
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) new URL(ontoUrl).openConnection();
			con.setRequestMethod("HEAD");
			con.setConnectTimeout(2000);
			//System.out.println("getResponseCode():" + con.getResponseCode());
			if (con.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) {
				//System.out.print("URL Exists");
				toReturn = true;
			}
			else if(con.getResponseCode() >= 500 ) {
				//System.out.println( "Error! Server error happened  ");
				toReturn = false;
			} else if ( con.getResponseCode() >= 400 ) {
				//System.out.println( "Client error ");
				toReturn = false;
			}
		// If some exception is launched when checking the URL, we return false
		} catch (MalformedURLException e) {
			toReturn = false;
		} catch (IOException e) {
			toReturn = false;
		}
		return toReturn;
	}

}
