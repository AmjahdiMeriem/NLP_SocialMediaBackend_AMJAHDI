package com.socialmedia.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

import com.socialmedia.model.Comment;
import com.socialmedia.model.Opinion;
import com.socialmedia.service.i.SentimentServiceI;

import safar.modern_standard_arabic.basic.morphology.lemmatizer.factory.LemmatizerFactory;
import safar.modern_standard_arabic.basic.morphology.lemmatizer.interfaces.ILemmatizer;
import safar.modern_standard_arabic.basic.morphology.lemmatizer.model.LemmatizerAnalysis;
import safar.modern_standard_arabic.basic.morphology.lemmatizer.model.WordLemmatizerAnalysis;
import safar.modern_standard_arabic.util.normalization.impl.SAFARNormalizer;
import safar.modern_standard_arabic.util.tokenization.impl.SAFARTokenizer;

public class SentimentServiceImpl implements SentimentServiceI {

	public static final int TAILLE_TAMPON = 10240;
	public static final String CHEMIN_FICHIERS = "C:\\Users\\hp\\Desktop\\EMI-PFE\\Code JAVA\\SocialMediaBackend\\src\\main\\resources\\";

	public static ILemmatizer lemmatizerFactory = LemmatizerFactory.getSAFARImplementation();
	public static ILemmatizer lemmatizerFactoryFarasa = LemmatizerFactory.getFARASAImplementation();

	public static SAFARNormalizer NORMALIZER = new SAFARNormalizer();
	public static List<String> NEUTRAL = Arrays.asList("مَهْمَا", "لَيْتَ", "مَنْ", "لَوْلَا", "لَوْ", "لَعَلَّ",
			"كَمْ", "لِمَاذَا", "مَتَى", "أَيْنَ", "اَيْنَ", "كَيْفَ");
	public static List<String> FORCE = Arrays.asList("قَدْ", "إِنَّ", "اِنَّ");
	public static List<String> NEGATION = Arrays.asList("لَا", "لن", "لَمْ");
	public static List<Float> scores = new ArrayList<Float>();

	@Override
	public List<Float> calculateScores(List<Opinion> opinions) {
		List<List<String>> lexique = new ArrayList<List<String>>();
		try {
			lexique = readLexicon(CHEMIN_FICHIERS + "lexique-ARSOA-IERA-VF.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> stopWords = readStopWords("src/main/resources/sw2_updated.xml");
		System.out.println(opinions);
		Float score;

		for (int i = 0; i < opinions.size(); i++) {
			List<Comment> comments = opinions.get(i).getComments();
			for (int j = 0; j < comments.size(); j++) {
				System.out.println("Input Text : " + comments.get(j).getComment());

				String newNormalizedText = normalizer(comments.get(j).getComment());

				String newCleanedTxt = removeStopWords(newNormalizedText, stopWords);

				List<String> newLemmatizedFarasa = lemmatizerFarasa(newCleanedTxt);

				HashMap<String, Integer> vect = vect(newLemmatizedFarasa);

				HashMap<String, Float> scores2 = score2(vect, lexique);
				System.out.println("scores : " + scores2 + "\n");
				if (scores2.size() != 0) {
					score = calculateScoreCommentAverage(newLemmatizedFarasa, vect, scores2, lexique);
				} else {
					score = 0f;
				}
				System.out.println("scores : " + scores);
				scores.add(score);
			}
		}
		return scores;
	}

	@Override
	public List<Opinion> sentimentOpinion(List<Opinion> opinions) {
		for (int i = 0; i < opinions.size(); i++) {
			List<Comment> comments = opinions.get(i).getComments();
			for (int j = 0; j < comments.size(); j++) {
				if (scores.get(j) <= 0.5 && scores.get(j) >= -0.5) {
					System.out.println(scores.get(j) + " Neutral");
					opinions.get(i).getComments().get(j).setPolarity("Neutral");

				} else if (scores.get(j) > 0.5) {
					System.out.println(scores.get(j) + " Positive");
					opinions.get(i).getComments().get(j).setPolarity("Positive");

				} else {
					System.out.println(scores.get(j) + " Negative");
					opinions.get(i).getComments().get(j).setPolarity("Negative");
				}
			}
		}
		System.out.println(opinions);
		return opinions;
	}

	@Override
	public String sentimentOpinions() {
		Float somme = 0f;
		Float scoreMoy = 0f;
		if (scores.size() == 0) {
			return "Neutral";
		}
		for (int i = 0; i < scores.size(); i++) {
			somme += scores.get(i);
		}
		scoreMoy = somme / scores.size();
		System.out.println("scoreMoy :" + scoreMoy);
		if (scoreMoy >= -0.5 && scoreMoy <= 0.5) {
			return "Neutral";

		} else if (scoreMoy > 0.5) {
			return "Positive";

		} else {
			return "Negative";
		}

	}

	// Normalizer
	public String normalizer(String str) {
		SAFARNormalizer normalizer = new SAFARNormalizer();
		String normalizedText = normalizer.normalize(str);
		System.out.println("NormalizedText : " + normalizedText);

		// String regex = "\\،";
		// String regex = "\\p{Punct}"; //Special character :
		// `~!@#$%^&*()-_+=\|}{]["';:/?.,><
		String regex = "\\p{Punct}|،|[0-9]";
		StringBuilder builder = new StringBuilder(str);
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

	// tokenizeSentenceToWords
	public static String[] tokenizeSentenceToWords(String sentence) {
		SAFARTokenizer tokenizer = new SAFARTokenizer();
		String[] tokens = tokenizer.tokenize(sentence);
		return tokens;
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

	// Tokenize
	public String[] tokenize(String cleanedTxt) {
		SAFARTokenizer tokenizer = new SAFARTokenizer();
		String[] tokens = tokenizer.tokenize(cleanedTxt);
		return tokens;
	}

	// LemmatizerSAFAR
	public List<String> lemmatizerSafar(String cleanedTxt) {
		List<String> lemmatized = new ArrayList<String>();
		String lem;

		List<List<String>> lemmatized2 = new ArrayList<List<String>>();
		List<String> lem2;

		List<WordLemmatizerAnalysis> analysis = lemmatizerFactory.lemmatize(cleanedTxt);
		for (WordLemmatizerAnalysis wordAnalysis : analysis) {
			lem = wordAnalysis.getStandardAnalysisList().get(0).getLemma();
			lemmatized.add(lem);
			lem2 = new ArrayList<String>();
			// Print the list of possible lemmas for the current word
			for (LemmatizerAnalysis lemma : wordAnalysis.getStandardAnalysisList()) {
				lem2.add(lemma.getLemma());
			}
			lemmatized2.add(lem2);
		}
		// System.out.println("Lemmatized Safar : " + lemmatized2 + "\n");
		return lemmatized;
	}

	// LemmatizerFarasa
	public static List<String> lemmatizerFarasa(String cleanedTxt) {
		// https://www.aclweb.org/anthology/N16-3003.pdf
		List<String> prefixes = Arrays.asList("ف", "و", "ل", "ب", "ك", "ال", "س");
		List<String> suffixes = Arrays.asList("ا", "ة", "ت", "ك", "ن", "و", "ي", "ات", "ان", "ون", "وا", "ين", "كما",
				"كم", "كن", "ه", "ها", "هما", "هم", "هن", "نا", "تما", "تم", "تن");
		String SEPARATEUR = "\\+";
		List<String> lemmatized = new ArrayList<String>();
		List<String> newLemmatized = new ArrayList<String>();

		List<WordLemmatizerAnalysis> analysis = lemmatizerFactoryFarasa.lemmatize(cleanedTxt);

		for (WordLemmatizerAnalysis wordAnalysis : analysis) {
			for (LemmatizerAnalysis lemma : wordAnalysis.getStandardAnalysisList()) {
				lemmatized.add(lemma.getLemma());
				String mots[] = lemma.getLemma().split(SEPARATEUR);
				for (int j = 0; j < mots.length; j++) {
					if (!prefixes.contains(mots[j]) && !suffixes.contains(mots[j])) {
						newLemmatized.add(mots[j]);
					}
				}
			}
		}
		System.out.println("Lemmatized Farasa : " + lemmatized + "\n");
		System.out.println("New Lemmatized Farasa : " + newLemmatized + "\n");

		return newLemmatized;
	}

	// ReadLexicon
	public static List<List<String>> readLexicon(String path) throws IOException {
		List<List<String>> lexique = new ArrayList<List<String>>();
		List<String> mots;
		List<String> lines = Files.readAllLines(Paths.get(path));
		for (String line : lines) {
			mots = new ArrayList<String>();

			line = line.replace("\"", "");
			mots.add(line.split(";")[1]);
			mots.add(line.split(";")[2]);
			mots.add(line.split(";")[3]);
			mots.add(line.split(";")[6]);

			lexique.add(mots);
		}
		return lexique;
	}

	// Claculate Score Comment Average
	public float calculateScoreCommentAverage(List<String> lemmatized, HashMap<String, Integer> vect,
			HashMap<String, Float> scores2, List<List<String>> lexique) {
		System.out.println("\n *********option 1 : lematizedFarasa / AverageScore*********");

		Float scoreCommentAverage = 0f;
		int comp = 0;

		System.out.println("occurrence vector : " + vect + "\n");

		Iterator<String> it = scores2.keySet().iterator();
		while (it.hasNext()) {
			String s = it.next();
			scoreCommentAverage += scores2.get(s);
			comp += vect.get(s);
		}

		if (comp != 0)
			scoreCommentAverage /= comp;
		System.out.println("The score for this comment with Average is " + scoreCommentAverage);
		return scoreCommentAverage;
	}

	// Occurrence Vector for words
	public HashMap<String, Integer> vect(List<String> lemmatized) {

		HashMap<String, Integer> vect = new HashMap<String, Integer>();
		for (int i = 0; i < lemmatized.size(); i++) {
			if (vect.containsKey(lemmatized.get(i))) {
				vect.put(lemmatized.get(i), vect.get(lemmatized.get(i)) + 1);
			} else {
				vect.put(lemmatized.get(i), 1);
			}
		}
		System.out.println("occurrence vector : " + vect + "\n");
		return vect;
	}

	// Scores2
	public HashMap<String, Float> score2(HashMap<String, Integer> vect, List<List<String>> lexique) {
		HashMap<String, Float> scores2 = new HashMap<String, Float>();

		Iterator<String> it = vect.keySet().iterator();
		Float score;
		int index;
		while (it.hasNext()) {
			String s = it.next();
			score = 0f;
			index = 0;
			for (int j = 1; j < lexique.size(); j++) {
				if (lexique.get(j).get(2).equals(s)) {
					score += Float.parseFloat(lexique.get(j).get(3));
					index++;
				}
			}
			if (index != 0) {
				score = score / index;
			}

			if (score != 0f) {
				scores2.put(s, score * vect.get(s));
			}
		}
		return scores2;
	}

}
