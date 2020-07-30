package classifier;

import java.util.*;
import java.sql.*;

public class Classifier {
	public HashMap<String, Categories> storedGroups = new HashMap<String, Categories>();
	
	public HashMap<String, ArrayList<Words>> storedWords = new HashMap<String, ArrayList<Words>>();
	
	public HashMap<String, Double> finalProduct = new HashMap<String, Double>();
	
	public HashMap<String, Integer> groupCounter = new HashMap<String, Integer>();
	
	public double totalProb;
	
	public String predictedText;
	
	public Classifier() {
		totalProb = 0.0;
		predictedText = "";
	}
	
	public void increment(String token, String group) {
		Categories curCategory = storedGroups.get(group);
		curCategory.tokenCount++;
		
		if(!storedWords.containsKey(token)) {
			Words newWord = new Words(token, group, 1);
			ArrayList<Words> tmpArray = new ArrayList<Words>();
			tmpArray.add(newWord);
			storedWords.put(token, tmpArray);
		} else if (!checkGroupExists(token, group)) {
			Words newWord = new Words(token, group, 1);
			ArrayList<Words> tmpArray = storedWords.get(token);
			tmpArray.add(newWord);
			storedWords.put(token, tmpArray);
		} else {
			increaseWordCount(storedWords.get(token), group);
		}
	}
	
	public void increaseWordCount(ArrayList<Words> arr, String group) {
		for(int i = 0; i < arr.size(); i++) {
			if(arr.get(i).wordCategory == group) {
				arr.get(i).count++;
				break;
			}
		}
	}
	
	public boolean checkGroupExists(String token, String group) {
		ArrayList<Words> tmpCheck = storedWords.get(token);
		
		for(int i = 0; i < tmpCheck.size(); i++)
			if(tmpCheck.get(i).wordCategory == group)
				return true;
		return false;
	}
	
	public void train(HashMap<String, String> dataSet) {
		for(String data : dataSet.keySet()) {
			String group = dataSet.get(data);
			boolean categoryExists = storedGroups.containsKey(group);
			Categories currentCategory = categoryExists ? storedGroups.get(group) : new Categories();
			
			currentCategory.docCount++;
			storedGroups.put(group, currentCategory);
			
			for(String token : data.toLowerCase().split("\\W+"))
				increment(token, group);
		}
	}
	
	public void probabilities() {
		for(String key : storedWords.keySet()) {
			ArrayList<Words> wordsList = storedWords.get(key);
			double totalProb = 0.0;
			for(Words tmpObj : wordsList) {
				Categories currentCategory = storedGroups.get(tmpObj.wordCategory);
				tmpObj.freq = tmpObj.count * 1.0 / currentCategory.docCount;
				totalProb = tmpObj.freq * wordsList.size();
				double prob = (totalProb > 0.0) ? tmpObj.freq / totalProb : 0.0;
				tmpObj.prob = Math.max(0.01, Math.min(0.99, prob));
			}
		}
	}
	
	public String predict(String data) {
		ArrayList<String> containedWords = new ArrayList<String>();
		
		predictedText = data.toLowerCase().replace("\\W+", " ");
		
		for(String token : data.toLowerCase().split("\\W+"))
			if(storedWords.containsKey(token))
				containedWords.add(token);
		
		for(String token : containedWords)
			getResults(storedWords.get(token));
		
		return finalizeResults(containedWords.size());
	}
	
	public void getResults(ArrayList<Words> texts) {
		for(Words tmpObj : texts) {
			int num = 1;
			double newProb = 1.0 * tmpObj.prob;
			
			if(finalProduct.containsKey(tmpObj.wordCategory)) {
				double storedProb = finalProduct.get(tmpObj.wordCategory);
				newProb = storedProb * tmpObj.prob;
				num = groupCounter.get(tmpObj.wordCategory) + 1;
			}
			
			finalProduct.put(tmpObj.wordCategory, newProb);
			groupCounter.put(tmpObj.wordCategory, num);
		}
	}
	
	public String finalizeResults(int count) {
		for(String key : groupCounter.keySet()) {
			double addProb = Math.pow(0.01, count - groupCounter.get(key));
			double newProb = addProb * finalProduct.get(key);
			totalProb += newProb;
			finalProduct.put(key, newProb);
		}
		
		for(String key : finalProduct.keySet()) {
			double finalProb = finalProduct.get(key) / totalProb;
			finalProduct.put(key, finalProb);
		}
		
		return highestValue();
	}
	
	public String highestValue() {
		double highestVal = 0.0;
		String res = "";
		String data = "";
		
		for(String key : finalProduct.keySet()) {
			if(finalProduct.get(key) > highestVal) {
				res = key;
				highestVal = finalProduct.get(key);
			}
		}
		
		ClassifierDB db = new ClassifierDB();
		if(!db.doesDataExist(predictedText, res))
			db.insertData(predictedText, res);
		db.closeConnection();
		return res;
	}
	
	public void printProbabilities() {
		for(String key : finalProduct.keySet())
			System.out.println("KEY : " + key + ", VALUE : " + finalProduct.get(key));
	}
	
	public void trainData(HashMap<String, String> dataSet) {
		
		ClassifierDB db = new ClassifierDB();

		for(String key : dataSet.keySet()) {
			String data = key.toLowerCase().replaceAll("\\W+", " ");
			if(!db.doesDataExist(data, dataSet.get(key).toLowerCase()))
				db.insertData(data, dataSet.get(key).toLowerCase());
		}

		this.train(db.listData());
		db.closeConnection();
		this.probabilities();
	}
	
	public void deleteDB() {
		ClassifierDB db = new ClassifierDB();
		db.deleteDB();
		db.closeConnection();
	}
	
	public static void main(String[] args) {
		//EXAMPLE CODE HERE
		HashMap<String, String> initialData = new HashMap<String, String>();
		initialData.put("My name is Roshen Maghhan", "intro");
		initialData.put("I'm currently interning in a Swedish company", "intro");
		initialData.put("The weather looks like its going to rain", "greeting");
		initialData.put("The sky looks cloudy", "greeting");		
		
		Classifier classifier = new Classifier();
		classifier.trainData(initialData);
		System.out.println(classifier.predict("I am turning 22 this year"));
		
		/*
		 * IF YOU WANT TO DELETE THE DATABASE,
		 * TO USE A DIFFERENT DATASET AND DIFFERENT CATEGORIES,
		 * USE deleteDB();
		 * 
		 * E.g : classifier.deleteDB();
		 * */
	}
}
