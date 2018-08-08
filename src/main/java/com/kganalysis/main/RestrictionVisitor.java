package com.kganalysis.main;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

/**
 * Visits existential restrictions and collects the properties which are restricted
 */
public class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {

	private boolean processInherited = true;
	
	private boolean toCheckRestType;

	private Set<OWLClass> processedClasses;

	private Set<OWLCardinalityRestriction> restrictionAxiom;
	
	private Set<OWLPropertyExpression> restrictedProperties;

	private Set<OWLOntology> onts;

	public RestrictionVisitor(Set<OWLOntology> onts) {
		restrictedProperties = new HashSet<OWLPropertyExpression>();
		processedClasses = new HashSet<OWLClass>();
		restrictionAxiom = new HashSet<OWLCardinalityRestriction>();
		this.onts = onts;
	}

	public void setProcessInherited(boolean processInherited) {
		this.processInherited = processInherited;
	}
    
	/**
	 * This method is a common setter to indicate that the visit method is for sub or super class restrictions
	 * True for subclass, false for superclass
	 * @param toCheckRestType
	 */
	public void setToCheckRestType(boolean toCheckRestType) {
		this.toCheckRestType = toCheckRestType;
	}

	public Set<OWLPropertyExpression> getRestrictedProperties() {
		return restrictedProperties;
	}
	
	public Set<OWLCardinalityRestriction> getRestrictionAxiom() {
		return restrictionAxiom;
	}

	public void setRestrictionAxiom(Set<OWLCardinalityRestriction> restrictionAxiom) {
		this.restrictionAxiom = restrictionAxiom;
	}

	public void visit(OWLClass desc) {
		if (processInherited && !processedClasses.contains(desc)) {
			// If we are processing inherited restrictions then
			// we recursively visit named supers.  Note that we
			// need to keep track of the classes that we have processed
			// so that we don't get caught out by cycles in the taxonomy
			processedClasses.add(desc);
			for (OWLOntology ont : onts) {
				if(toCheckRestType){
				for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(desc)) {
					ax.getSuperClass().accept(this);
				}
				}else{

				for (OWLEquivalentClassesAxiom ax : ont.getEquivalentClassesAxioms(desc)) {
					Set<OWLClassExpression> setOwlExpression = ax.getClassExpressions();
					for (OWLClassExpression owlClassExpression : setOwlExpression) {
						owlClassExpression.accept(this);
					}
				}
			}
			}//End ontology for
		}
	}

	public void reset() {
		processedClasses.clear();
		restrictedProperties.clear();
	}
	
	@Override
	public void visit(OWLDataAllValuesFrom desc) {
		restrictedProperties.add(desc.getProperty().asOWLDataProperty());
	}

	@Override
	public void visit(OWLDataExactCardinality desc) {
		restrictedProperties.add(desc.getProperty().asOWLDataProperty());
	}

	@Override
	public void visit(OWLDataHasValue desc) {
		restrictedProperties.add(desc.getProperty().asOWLDataProperty());
	}

	@Override
	public void visit(OWLDataMaxCardinality desc) {
		restrictedProperties.add(desc.getProperty().asOWLDataProperty());
	}

	@Override
	public void visit(OWLDataMinCardinality desc) {
		restrictedProperties.add(desc.getProperty().asOWLDataProperty());
	}

	@Override
	public void visit(OWLDataSomeValuesFrom desc) {
		restrictedProperties.add(desc.getProperty().asOWLDataProperty());
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom desc) {
		if(desc.getProperty().isAnonymous()==false)
		restrictedProperties.add(desc.getProperty().asOWLObjectProperty());
	}

	@Override
	public void visit(OWLObjectAllValuesFrom desc) {
		restrictedProperties.add(desc.getProperty().asOWLObjectProperty());
	}
	
	@Override
	public void visit(OWLObjectHasValue desc) {
		restrictedProperties.add(desc.getProperty().asOWLObjectProperty());
	}

	@Override
	public void visit(OWLObjectExactCardinality desc) {
		restrictedProperties.add(desc.getProperty().asOWLObjectProperty());
		restrictionAxiom.add(desc);
	}

	@Override
	public void visit(OWLObjectMaxCardinality desc) {
		restrictedProperties.add(desc.getProperty().asOWLObjectProperty());
		restrictionAxiom.add(desc);
	}

	@Override
	public void visit(OWLObjectMinCardinality desc) {
		restrictedProperties.add(desc.getProperty().asOWLObjectProperty());
		restrictionAxiom.add(desc);
	}


}
