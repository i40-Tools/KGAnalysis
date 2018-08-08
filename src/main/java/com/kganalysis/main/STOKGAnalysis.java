package com.kganalysis.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WriterDatasetRIOT;
import org.apache.jena.vocabulary.RDF;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.hp.hpl.jena.util.FileManager;
import com.kganalysis.util.ConfigManager;
import com.kganalysis.util.OntoUtil;
import com.kganalysis.util.StringSimilarity;
import com.kganalysis.util.StringUtil;

/**
 * Main class of the KG analysis
 * @class STOKGAnalysis
 * @Version 1.0
 * @Date 08.08.2018
 * @author Irlan Grangel 
 */



public class STOKGAnalysis {

	static Model jenaModel = null;
	static OWLOntology ontology;

	/**
	 * Entry point method of the application
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		ConfigManager.loadConfig();
		readOntology();
		rdfmt();

	}


	public static void rdfmt() throws FileNotFoundException{
		String subjectToWrite = "";
		String predicateToWrite = "";
		String objectToWrite = "";
		String srcDataset = "sto";
		String dstDataset = "sto";

		PrintWriter pw = new PrintWriter(new File(ConfigManager.getCSVPath()));
		StringBuilder sb = new StringBuilder();

		sb.append("s");
		sb.append(',');
		sb.append("p");
		sb.append(',');
		sb.append('o');
		sb.append(',');
		sb.append("srcDataset");
		sb.append(',');
		sb.append("dstDataset");
		sb.append('\n');

		Set<OWLClass> classes = ontology.getClassesInSignature();

		//System.out.println("Classes");
		System.out.println("--------------------------------");
		for (OWLClass cls : classes) {
			// Of which object properties is this class domain of
			ArrayList<OWLObjectProperty>objPropertiesDomainOfClass = ClassAxiomProperty.searchForObjectDomainClass(ontology, cls);
			if(objPropertiesDomainOfClass != null){
				for (OWLObjectProperty owlObjectProperty : objPropertiesDomainOfClass) {
					//which is the range of this property
					predicateToWrite = owlObjectProperty.getIRI().toString();
					OWLClass range = ClassAxiomProperty.searchForObjectRangeClass(ontology, owlObjectProperty);
					objectToWrite = range.getIRI().toString();
					sb.append(cls.getIRI());
					sb.append(',');
					sb.append(predicateToWrite);
					sb.append(',');
					sb.append(objectToWrite);
					sb.append(',');
					sb.append(getNameSpace(cls.getIRI().toString()));
					sb.append(',');
					sb.append(getNameSpace(range.getIRI().toString()));
					sb.append('\n');
				}
			}

			// Of which data properties is this class domain of
			ArrayList<OWLDataProperty>dataPropertiesDomainOfClass = ClassAxiomProperty.
					searchForDataPropertyDomainClass(ontology, cls);

			if(dataPropertiesDomainOfClass!=null){
				for (OWLDataProperty owlDataProperty : dataPropertiesDomainOfClass) {
					predicateToWrite = owlDataProperty.getIRI().toString();
					sb.append(cls.getIRI());
					sb.append(',');
					sb.append(predicateToWrite);
					sb.append(',');
					if(ClassAxiomProperty.searchForRangeData(ontology, owlDataProperty)!=null){
						objectToWrite = ClassAxiomProperty.searchForRangeData(ontology, owlDataProperty).toString();
					}else{
						objectToWrite = "xsd:string";
					}
					sb.append(objectToWrite);
					sb.append(',');
					sb.append(getNameSpace(cls.getIRI().toString()));
					sb.append(',');
					sb.append("sto");
					sb.append('\n');
				}
			}

			
		}

		pw.write(sb.toString());
		pw.close();

	}


	public static String getNameSpace(String URI) {
		String ontName = "";
		int endWithSlash = URI.lastIndexOf("/");
		int endWithChar = URI.lastIndexOf("#");

		if(URI.contains("#")){
			ontName = URI.substring(0,endWithChar + 1);
		} else if(endWithChar!=0){
			ontName = URI.substring(0,endWithSlash + 1);
		}

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("https://w3id.org/i40/sto#","sto");
		namespaces.put("http://xmlns.com/foaf/0.1/", "foaf");
		namespaces.put("http://purl.org/dc/terms/", "dcterms");
		namespaces.put("http://www.ontology-of-units-of-measure.org/resource/om-2/","om2");
		namespaces.put("http://dbpedia.org/ontology#","dbo");


		Set set = namespaces.entrySet();
		Iterator iterator = set.iterator();
		while(iterator.hasNext()) {
			Map.Entry mentry = (Map.Entry)iterator.next();
			if(ontName.equals(mentry.getKey())){
				ontName = mentry.getValue().toString();
			}
		}

		return ontName;
	}


	/**
	 * Reads the ontology using Jena and OWL API by default
	 * @throws OWLOntologyCreationException 
	 */
	public static void readOntology() throws OWLOntologyCreationException{
		//readOntologyJena();
		readOntologyOWLAPI();
	}

	/**
	 * Reads the ontology file using Jena
	 */
	public static void readOntologyJena(){
		InputStream inputStream = FileManager.get().open(ConfigManager.getFilePath());
		jenaModel = ModelFactory.createDefaultModel();
		jenaModel.read(new InputStreamReader(inputStream), null, "TURTLE");
	}


	/**
	 * Loads the ontology file using the OWL API whether is an URL or a file path in the system
	 * @throws OWLOntologyCreationException 
	 */
	public static void readOntologyOWLAPI() throws OWLOntologyCreationException{

		if(OntoUtil.checkURL(ConfigManager.getFilePath())){
			IRI ontologyIRI = IRI.create(ConfigManager.getFilePath());
			ontology = OntoUtil.loadOntology(ontologyIRI);
		}else {
			ontology = OntoUtil.loadOntology(new File(ConfigManager.getFilePath()));
		}

	}

}
