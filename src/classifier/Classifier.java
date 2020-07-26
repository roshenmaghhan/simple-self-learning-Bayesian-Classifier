package classifier;

import java.util.*;

public class Classifier {
	public HashMap<String, Categories> storedGroups = new HashMap<String, Categories>();
	
	public HashMap<String, ArrayList<Words>> storedWords = new HashMap<String, ArrayList<Words>>();
	
	public HashMap<String, Double> finalProduct = new HashMap<String, Double>();
	
	public HashMap<String, Integer> groupCounter = new HashMap<String, Integer>();
	
	public double totalProb;
	
	public Classifier() {
		totalProb = 0.0;
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
		
		for(int i = 0; i < tmpCheck.size(); i++) {
			if(tmpCheck.get(i).wordCategory == group) {
				return true;
			}
		}
		return false;
	}
	
	public void train(String data, String group) {
		boolean categoryExists = storedGroups.containsKey(group);
		Categories currentCategory = categoryExists ? storedGroups.get(group) : new Categories();
		
		currentCategory.docCount++;
		storedGroups.put(group, currentCategory);
		
		for(String token : data.toLowerCase().split("\\W+")) {
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
		
		for(String token : data.toLowerCase().split("\\W+")) {
			if(storedWords.containsKey(token)) {
				containedWords.add(token);
			}
		}
		
		for(String token : containedWords) {
			getResults(storedWords.get(token));
		}
		
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
			
			finalProduct. put(tmpObj.wordCategory, newProb);
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
		
		for(String key : finalProduct.keySet()) {
			if(finalProduct.get(key) > highestVal) {
				res = key;
				highestVal = finalProduct.get(key);
			}
		}
		
		return res;
	}
	
	public void printProbabilities() {
		for(String key : finalProduct.keySet()) {
			System.out.println("KEY : " + key + ", VALUE : " + finalProduct.get(key));
		}
	}
	
	public void trainData(HashMap<String, String> dataSet) {
		for(String key : dataSet.keySet()) {
			this.train(key, dataSet.get(key));
		}
		
		this.probabilities();
	}
	
	
	public static void main(String[] args) {
		//DRIVER CODE HERE\
		Classifier classifier = new Classifier();
		classifier.train("My name is Roshen Maghhan", "intro");
		classifier.train("I'm currently interning in Configura", "intro");
		classifier.train(" The weather looks like its going to rain", "greeting");
		classifier.train("The sky looks cloudy", "greeting");
		classifier.probabilities();
		String ans = classifier.predict("I am turning 22 this year");
		System.out.println(ans);
	}
}
