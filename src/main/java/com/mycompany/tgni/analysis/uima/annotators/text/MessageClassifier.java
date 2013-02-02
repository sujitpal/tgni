package com.mycompany.tgni.analysis.uima.annotators.text;

import java.io.Serializable;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * TODO: class level javadocs
 */
public class MessageClassifier implements Serializable {

  private static final long serialVersionUID = -8358975344205311124L;
  
  private Instances data;
  private boolean uptodate;
  private StringToWordVector filter;
  private Classifier classifier = new J48();
  
  public MessageClassifier() throws Exception {
    String datasetName = "MessageClassificationProblem";
    FastVector attributes = new FastVector(2);
    // add attribute for holding message
    attributes.addElement(new Attribute("Message", (FastVector) null));
    // add class attributes
    FastVector classValues = new FastVector(2);
    classValues.addElement("miss");
    classValues.addElement("hit");
    attributes.addElement(new Attribute("Class", classValues));
    // create dataset with initial capacity of 100 and set index of class
    data = new Instances(datasetName, attributes, 100);
    data.setClassIndex(data.numAttributes() - 1);
  }
  
  public void updateData(String message, String classValue) throws Exception {
    // make message into instance
    Instance instance = makeInstance(message, data);
    // set class value for instance
    instance.setClassValue(classValue);
    // add instance to training data
    data.add(instance);
    uptodate = false;
  }
  
  public void classifyMessage(String message) throws Exception {
    // check whether classifier has been built
    if (data.numInstances() == 0) {
      throw new Exception("No classifier available");
    }
    // check whether classifier and filter are up-to-date
    if (uptodate) {
      // initialize filter and tell it about the input format
      filter.setInputFormat(data);
      // generate word counts from the training data
      Instances filteredData = Filter.useFilter(data, filter);
      // rebuild classifier
      classifier.buildClassifier(filteredData);
      uptodate = true;
    }
    // make separate test set so that message does not get added
    // to string attributes in data
    Instances testset = data.stringFreeStructure();
    // make message into test instance
    Instance instance = makeInstance(message, testset);
    // filter instance
    filter.input(instance);
    Instance filteredInstance = filter.output();
    // get index of predicted class value
    double predicted = classifier.classifyInstance(filteredInstance);
    // output class value
    System.err.println("message classified as: " + 
      data.classAttribute().value((int) predicted));
  }

  private Instance makeInstance(String text, Instances data) {
    // create instance of length 2
    Instance instance = new Instance(2);
    // get value for message attribute
    Attribute mattr = data.attribute("Message");
    instance.setValue(mattr, mattr.addStringValue(text));
    // give instance access to attribute information from dataset
    instance.setDataset(data);
    return instance;
  }
}
