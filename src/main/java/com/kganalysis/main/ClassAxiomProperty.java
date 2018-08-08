package com.kganalysis.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * This class has the aim to extract the properties that are related with the main classes in a ontology
 * The relations can be: Domain, Range axioms.  Restrictions axioms.
 * @author igrangel
 *
 */
@SuppressWarnings("unchecked")
public class ClassAxiomProperty {

	private OWLOntology ont; 
	private RestrictionVisitor restrictionVisitor;
	//private AxiomVisitor axiomVisitor;
	//private OWLOntologyManager manager;
	//private OWLDataFactory factory;
	private Set<OWLProperty>restrictionProp = new HashSet<OWLProperty>();
	private Set<OWLClass> ontClasses = null;
	//public static Set<OWLCardinalityRestriction>restriction = new HashSet<OWLCardinalityRestriction>();

	public ClassAxiomProperty(OWLOntology ont, Set<OWLClass> ontClasses) {
		super();
		this.ont = ont;
		this.ontClasses = ontClasses;
		//manager = OWLManager.createOWLOntologyManager();
		//factory = manager.getOWLDataFactory();
		restrictionVisitor = new RestrictionVisitor(Collections.singleton(ont));
		//axiomVisitor = new AxiomVisitor(Collections.singleton(ont));

		searchForDomain();
		searchForRange();
		searchEqRestriction();
		searchSubRestriction();
	}

	public Set<OWLProperty> getProperties(){
		return this.restrictionProp;
	}

	/**
	 * This method uses a Visitor Object to visit every restriction that belong to a subclass and find the properties that forms the restriction
	 * @param variant The main classes in the ontology
	 * @return Set<OWLProperty>
	 */
	public Set<OWLCardinalityRestriction> searchSubRestriction() {
		Set<OWLCardinalityRestriction> restriction;

		//Set<OWLProperty>toReturn = new HashSet<OWLProperty>();
		restrictionVisitor.setToCheckRestType(true);
		for (OWLClass currVariant : this.ontClasses) {
			for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(currVariant)) {
				OWLClassExpression superCls = ax.getSuperClass();
				superCls.accept(restrictionVisitor);
			}
		}
		//Set<OWLPropertyExpression> restProA = restrictionVisitor.getRestrictedProperties();
		//restrictionProp.addAll((Set<? extends OWLProperty>) restProA);
		restriction = restrictionVisitor.getRestrictionAxiom();

		return restriction;
	}

	/**
	 * This method uses a Visitor Object to visit every restriction that belong to a equivalent class and find the properties that forms the restriction
	 * @param variant The main classes in the ontology
	 * @return Set<OWLProperty>
	 */
	public Set<OWLCardinalityRestriction> searchEqRestriction() {
		Set<OWLCardinalityRestriction> restriction;

		restrictionVisitor.setToCheckRestType(false);
		for (OWLClass currVariant : this.ontClasses) {
			for (OWLEquivalentClassesAxiom ax : ont.getEquivalentClassesAxioms(currVariant)) {
				Set<OWLClassExpression> classExpressions = ax.getClassExpressions();
				for (OWLClassExpression owlClassExpression : classExpressions) {
					OWLClassExpression equivCls = owlClassExpression;
					equivCls.accept(restrictionVisitor);
					break;
				}
			}
		}
		//Set<OWLPropertyExpression>restPro = restrictionVisitor.getRestrictedProperties();
		restriction = restrictionVisitor.getRestrictionAxiom();
		//restrictionProp.addAll((Collection<? extends OWLProperty>) restPro);

		return restriction;
	}

	/**
	 * This method search for all the object properties of a given ontology and returns which properties are present in domain axioms
	 * with a given set of classes
	 * @param ontClass
	 * @return
	 */
	private void searchForDomain(){
		OWLClass classDomain = null;
		Set<OWLObjectProperty>domain = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty ax : ont.getObjectPropertiesInSignature()) {
			Set<OWLObjectPropertyDomainAxiom> domainAxiom = ont.getObjectPropertyDomainAxioms(ax);
			for (OWLObjectPropertyDomainAxiom owlObjectPropertyDomainAxiom : domainAxiom) {
				if(owlObjectPropertyDomainAxiom.getDomain().isAnonymous()==false){
					classDomain = (OWLClass) owlObjectPropertyDomainAxiom.getDomain();
					for (OWLClass currVariant : this.ontClasses) {
						if(classDomain.equals(currVariant)){
							domain.add(ax);
							break;
						}
					}
				}
			}
		}
		restrictionProp.addAll((Collection<? extends OWLProperty>) domain);
	}

	/**
	 * This method search for the first object property of a given ontology connected with a class in a domain axiom
	 * @param ontClass
	 * @return the property if the relation exists and null in the other case
	 */
	public static ArrayList<OWLObjectProperty> searchForObjectDomainClass(OWLOntology ont, OWLClass variant){
		ArrayList<OWLObjectProperty> domain = new ArrayList<OWLObjectProperty>(); 
		OWLClass classDomain = null;
		for (OWLObjectProperty ax : ont.getObjectPropertiesInSignature()) {
			Set<OWLObjectPropertyDomainAxiom> domainAxiom = ont.getObjectPropertyDomainAxioms(ax);
			for (OWLObjectPropertyDomainAxiom owlObjectPropertyDomainAxiom : domainAxiom) {
				if(owlObjectPropertyDomainAxiom.getDomain().isAnonymous() == false){
					classDomain = (OWLClass) owlObjectPropertyDomainAxiom.getDomain();
					if(classDomain.equals(variant)){
						domain.add(owlObjectPropertyDomainAxiom.getProperty().asOWLObjectProperty());
					}
				}
			}
		}
		if(domain.size()!=0){
			return domain;
		}else{
			return null;
		}
	}


	public static ArrayList<OWLDataProperty> searchForDataPropertyDomainClass(OWLOntology ont, OWLClass variant){
		ArrayList<OWLDataProperty> domain = new ArrayList<OWLDataProperty>(); 
		OWLClass classDomain = null;
		for (OWLDataProperty ax : ont.getDataPropertiesInSignature()) {
			Set<OWLDataPropertyDomainAxiom> domainAxiom = ont.getDataPropertyDomainAxioms(ax);
			for (OWLDataPropertyDomainAxiom dataPropertyDomainAxiom : domainAxiom) {
				if(dataPropertyDomainAxiom.getDomain().isAnonymous() == false){
					classDomain = (OWLClass) dataPropertyDomainAxiom.getDomain();
					if(classDomain.equals(variant)){
						domain.add(dataPropertyDomainAxiom.getProperty().asOWLDataProperty());
					}
				}
			}
		}
		if(domain.size()!=0){
			return domain;
		}else{
			return null;
		}
	}


	/**
	 * This method search for the first class properties of a given ontology connected with a property in a range axiom
	 * @param OWLObjectProperty
	 * @return the class if the relation exists and null in the other case
	 */
	public static OWLClass searchForObjectRangeClass(OWLOntology ont, OWLObjectProperty prop){
		OWLClass range = null; OWLObjectProperty propKey = null;
		for (OWLObjectProperty ax : ont.getObjectPropertiesInSignature()) {
			Set<OWLObjectPropertyRangeAxiom> rangeAxiom = ont.getObjectPropertyRangeAxioms(ax);
			for (OWLObjectPropertyRangeAxiom owlObjectPropertyRangeAxiom : rangeAxiom) {
				if(owlObjectPropertyRangeAxiom.getRange().isAnonymous()==false){
					propKey = (OWLObjectProperty) owlObjectPropertyRangeAxiom.getProperty();
					if(propKey.equals(prop)){
						range = (OWLClass) owlObjectPropertyRangeAxiom.getRange();
						return range;
					}
				}
			}
		}
		return null;
	}


	/**
	 * This method search for all the object properties of a given ontology and returns which properties are present in range axioms with a given class
	 * @param ontClass
	 * @return
	 */
	private void searchForRange(){
		Set<OWLObjectProperty>range = new HashSet<OWLObjectProperty>();
		OWLClass classKey = null;
		for (OWLObjectProperty ax : ont.getObjectPropertiesInSignature()) {
			Set<OWLObjectPropertyRangeAxiom> rangeAxiom = ont.getObjectPropertyRangeAxioms(ax);
			for (OWLObjectPropertyRangeAxiom owlObjectPropertyRangeAxiom : rangeAxiom) {
				if(owlObjectPropertyRangeAxiom.getRange().isAnonymous()==false){
					classKey = (OWLClass) owlObjectPropertyRangeAxiom.getRange();
					for (OWLClass currVariant : this.ontClasses) {
						if(classKey.equals(currVariant)){
							range.add(ax);
							break;
						}
					}
				}
			}
		}
		restrictionProp.addAll((Collection<? extends OWLProperty>) range);
	}

	/**
	 * This method search for all the data properties of a given ontology and returns which properties are present in range axioms with a given class
	 * @param ontClass
	 * @return 
	 * @return
	 */
	public static OWLDatatype searchForRangeData(OWLOntology ont, OWLDataProperty dataProperty){
		Set<OWLDataPropertyRangeAxiom> rangeAxiom = ont.getDataPropertyRangeAxioms(dataProperty);
		for (OWLDataPropertyRangeAxiom owlDataPropertyRangeAxiom : rangeAxiom) {
			if(owlDataPropertyRangeAxiom.getRange().isDatatype()==true){
				return owlDataPropertyRangeAxiom.getRange().asOWLDatatype();
			}
		}
		return null;
	}

	}
