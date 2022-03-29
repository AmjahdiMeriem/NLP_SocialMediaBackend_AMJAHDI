package com.socialmedia.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.socialmedia.model.Opinion;
import com.socialmedia.service.i.SummaryServiceI;

import safar.modern_standard_arabic.basic.morphology.lemmatizer.factory.LemmatizerFactory;
import safar.modern_standard_arabic.basic.morphology.lemmatizer.interfaces.ILemmatizer;
import safar.modern_standard_arabic.basic.morphology.lemmatizer.model.WordLemmatizerAnalysis;
import safar.modern_standard_arabic.util.normalization.impl.SAFARNormalizer;
import safar.modern_standard_arabic.util.tokenization.impl.SAFARTokenizer;

public class SummaryServiceImpl implements SummaryServiceI {
	public static SAFARNormalizer NORMALIZER = new SAFARNormalizer();

	@Override
	public String summaryOpinions(List<Opinion> opinions, String polarity, String transReq) {
		List<String> opinionsPol = new ArrayList<String>();
		for (int i = 0; i < opinions.size(); i++) {
			for (int j = 0; j < opinions.get(i).getComments().size(); j++) {
				if (opinions.get(i).getComments().get(j).getPolarity().equals(polarity)) {
					opinionsPol.add(opinions.get(i).getComments().get(j).getComment());
				}
			}
		}
		System.out.println(opinionsPol);

		return getSummary(transReq, opinionsPol);
	}

	public static String getSummary(String transReq, List<String> opinionsPol) {
		List<String> stopWords = readStopWords("src/main/resources/sw3_updated.xml");
		List<List<String>> lemms = new ArrayList<List<String>>();
		List<HashMap<String, Integer>> vects = new ArrayList<HashMap<String, Integer>>();
		Collection<String> noDups = new HashSet<String>();
		List<String> unique = new ArrayList<String>();
		List<List<Integer>> occZeros = new ArrayList<List<Integer>>();
		opinionsPol.add(transReq);

		for (int i = 0; i < opinionsPol.size(); i++) {
			String newNormalizedText = normalizer(opinionsPol.get(i));
			String newCleanedTxt = removeStopWords(newNormalizedText, stopWords);
			if (newCleanedTxt.trim().isEmpty() == false) {
				List<String> sentenceLematized = lemmatizerSafar(newCleanedTxt);
				lemms.add(sentenceLematized);
				vects.add(vect(sentenceLematized));
				for (int j = 0; j < sentenceLematized.size(); j++) {
					noDups.add(sentenceLematized.get(j));
				}
			}
		}

		for (String elem : noDups) {
			System.out.println(elem);
			unique.add(elem);
		}

		System.out.println("lemms : " + lemms);

		System.out.println("vects : " + vects);
		System.out.println("noDup : " + noDups);
		System.out.println("unique : " + unique);

		occZeros = vectorRepresentation(lemms, unique, vects);
		System.out.println("occZeros : " + occZeros);
		List<Double> max = new ArrayList<Double>();
		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		for (int i = 0; i < occZeros.size() - 1; i++) {
			double sim = cosineSimilarity(occZeros.get(occZeros.size() - 1), occZeros.get(i));
			max.add(sim);
			hm.put(i, sim);
			System.out.println("cosineSimilarity : " + sim);
		}
		System.out.println(max);
		Collections.sort(max, Collections.reverseOrder());
		System.out.println(max);
		Map<Integer, Double> hm1 = sortByValue(hm);
		System.out.println(hm1);
		Iterator<Integer> itr = hm1.keySet().iterator();
		int cont = 0;
		String summary = "";
		while (itr.hasNext() && cont < 1) {
			// String txt=opinionsPol.get(itr.next());
			// summary+=txt+" ";
			summary = opinionsPol.get(itr.next());
			cont += 1;
		}
		return normalizer(summary);
	}

	public static List<List<Integer>> vectorRepresentation(List<List<String>> lemms, List<String> unique,
			List<HashMap<String, Integer>> vects) {
		List<List<Integer>> occZeros = new ArrayList<List<Integer>>();
		for (int i = 0; i < lemms.size(); i++) {
			List<Integer> o = new ArrayList<Integer>();
			for (int j = 0; j < unique.size(); j++) {
				o.add(0);
			}
			occZeros.add(o);
		}

		for (int i = 0; i < lemms.size(); i++) {
			for (int j = 0; j < lemms.get(i).size(); j++) {
				int index = unique.indexOf(lemms.get(i).get(j));
				System.out.println(index);
				occZeros.get(i).set(index, vects.get(i).get(lemms.get(i).get(j)));
			}
		}
		return occZeros;
	}

	public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to hashmap
		HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
		for (Map.Entry<Integer, Double> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

	public static double cosineSimilarity(List<Integer> vectorA, List<Integer> vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.size(); i++) {
			dotProduct += vectorA.get(i) * vectorB.get(i);
			normA += Math.pow(vectorA.get(i), 2);
			normB += Math.pow(vectorB.get(i), 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	// tokenizeSentenceToWords
	public static String[] tokenizeSentenceToWords(String sentence) {
		SAFARTokenizer tokenizer = new SAFARTokenizer();
		String[] tokens = tokenizer.tokenize(sentence);
		return tokens;
	}

	// Normalizer
	public static String normalizer(String sentence) {
		SAFARNormalizer normalizer = new SAFARNormalizer();
		String regex = "\\p{Punct}|،|[0-9]";
		StringBuilder builder = new StringBuilder(sentence);
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(builder.toString());
		String newNormalizedText = matcher.replaceAll(" ");
		newNormalizedText = normalizer.normalize(newNormalizedText);
		System.out.println("New Normalized Text : " + newNormalizedText + "\n");
		return newNormalizedText;
	}

	// ReadXMLStopWords
	public static List<String> readStopWords(String filePath) {

		File xmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		List<String> stopWords = new ArrayList<String>();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("stopWord");
			for (int itr = 0; itr < nodeList.getLength(); itr++) {
				Node node = nodeList.item(itr);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					stopWords.add(NORMALIZER
							.normalizeDiacritics(eElement.getElementsByTagName("vowForm").item(0).getTextContent()));
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stopWords;
	}

	// RemoveStopWords
	public static String removeStopWords(String normalizedText, List<String> stopWords) {
		String newCleanedTxt = "";
		String[] tokens = tokenizeSentenceToWords(normalizedText);
		boolean drapeau;

		for (int i = 0; i < tokens.length; i++) {
			drapeau = false;
			for (int j = 0; j < stopWords.size(); j++) {
				if (tokens[i].equals(stopWords.get(j))) {
					drapeau = true;
					break;
				}
			}
			if (drapeau == false) {
				newCleanedTxt += tokens[i] + " ";
			}
		}
		System.out.println("New Cleaned Text : " + newCleanedTxt + "\n");
		return newCleanedTxt;
	}

	// LemmatizerSAFAR
	public static List<String> lemmatizerSafar(String cleanedTxt) {
		List<String> lemmatized = new ArrayList<String>();
		String lem;
		ILemmatizer lemmatizerFactory = LemmatizerFactory.getSAFARImplementation();
		List<WordLemmatizerAnalysis> analysis = lemmatizerFactory.lemmatize(cleanedTxt);
		for (WordLemmatizerAnalysis wordAnalysis : analysis) {
			lem = wordAnalysis.getStandardAnalysisList().get(0).getLemma();
			if (lem == "unk") {
				lem = " ";
			}
			lemmatized.add(lem);

		}
		System.out.println("Lemmatized Safar : " + lemmatized + "\n");
		return lemmatized;
	}

	public static HashMap<String, Integer> vect(List<String> lemmatized) {
		// occurrence vector for words
		HashMap<String, Integer> vect = new HashMap<String, Integer>();
		for (int i = 0; i < lemmatized.size(); i++) {
			if (vect.containsKey(lemmatized.get(i))) {
				vect.put(lemmatized.get(i), vect.get(lemmatized.get(i)) + 1);
			} else {
				vect.put(lemmatized.get(i), 1);
			}
		}
		System.out.println("occurrence vector  for words: " + vect + "\n");
		return vect;
	}

}
