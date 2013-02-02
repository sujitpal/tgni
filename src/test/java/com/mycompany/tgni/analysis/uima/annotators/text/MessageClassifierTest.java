package com.mycompany.tgni.analysis.uima.annotators.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * TODO: class level javadocs
 */
public class MessageClassifierTest {

  @Test
  public void testClassifier() throws Exception {
    MessageClassifier mc;
    String modelName = "some-model";
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelName));
      mc = (MessageClassifier) ois.readObject();
      ois.close();
    } catch (FileNotFoundException e) {
      mc = new MessageClassifier();
    }
    // check if class value is given
    String classValue = "";
    // process message
    String message = FileUtils.readFileToString(new File("input.file"));
    if (StringUtils.isNotEmpty(classValue)) {
      mc.updateData(message, classValue);
    } else {
      mc.classifyMessage(message);
    }
    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelName));
    oos.writeObject(mc);
    oos.close();
  }
}
