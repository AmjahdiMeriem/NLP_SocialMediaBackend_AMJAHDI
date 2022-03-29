package com.socialmedia.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import com.socialmedia.model.Comment;
import com.socialmedia.model.Opinion;
import com.socialmedia.service.i.SentimentServiceI;

import safar.modern_standard_arabic.basic.morphology.lemmatizer.factory.LemmatizerFactory;
import safar.modern_standard_arabic.basic.morphology.lemmatizer.interfaces.ILemmatizer;
import safar.modern_standard_arabic.basic.morphology.lemmatizer.model.WordLemmatizerAnalysis;
import safar.modern_standard_arabic.util.normalization.impl.SAFARNormalizer;
import safar.modern_standard_arabic.util.tokenization.impl.SAFARTokenizer;

public class SentimentServiceImpl2 implements SentimentServiceI {
	public static final int TAILLE_TAMPON = 10240;
	public static final String CHEMIN_FICHIERS = "C:\\Users\\hp\\Desktop\\EMI-PFE\\Code JAVA\\Preprocessing2\\src\\main\\resources\\";

	public static ILemmatizer lemmatizerFactory = LemmatizerFactory.getSAFARImplementation();
	public static SAFARNormalizer NORMALIZER = new SAFARNormalizer();
	public static List<String> prefixes = Arrays.asList("ف", "و");
	public static List<String> suffixes = Arrays.asList("ك", "كما", "كم", "كن", "ه", "ها", "هما", "هم", "هن", "نا");
	public static List<String> LIST = Arrays.asList("بل", "لكن", "فبل", "وبل", "فلكن", "ولكن", "لكنك", "فلكنك", "ولكنك",
			"لكنكما", "فلكنكما", "ولكنكما", "لكنكم", "فلكنكم", "ولكنكم", "لكنكن", "فلكنكن", "ولكنكن", "لكنه", "فلكنه",
			"ولكنه", "لكنها", "فلكنها", "ولكنها", "لكنهما", "فلكنهما", "ولكنهما", "لكنهم", "فلكنهم", "ولكنهم", "لكنهن",
			"فلكنهن", "ولكنهن", "لكننا", "فلكننا", "ولكننا");
	public static List<String> NEUTRAL = Arrays.asList("مهما", "ليت", "من", "لولا", "لو", "لعل", "كم", "لماذا", "ماذا",
			"متى", "أين", "اين", "كيف", "أنى", "أيان", "لوما", "هل", "فمهما", "ومهما", "فليت", "وليت", "ليتك", "فليتك",
			"وليتك", "ليتكما", "فليتكما", "وليتكما", "ليتكم", "فليتكم", "وليتكم", "ليتكن", "فليتكن", "وليتكن", "ليته",
			"فليته", "وليته", "ليتها", "فليتها", "وليتها", "ليتهما", "فليتهما", "وليتهما", "ليتهم", "فليتهم", "وليتهم",
			"ليتهن", "فليتهن", "وليتهن", "ليتنا", "فليتنا", "وليتنا", "فمن", "ومن", "فلولا", "ولولا", "فلو", "ولو",
			"فلعل", "ولعل", "لعلك", "فلعلك", "ولعلك", "لعلكما", "فلعلكما", "ولعلكما", "لعلكم", "فلعلكم", "ولعلكم",
			"لعلكن", "فلعلكن", "ولعلكن", "لعله", "فلعله", "ولعله", "لعلها", "فلعلها", "ولعلها", "لعلهما", "فلعلهما",
			"ولعلهما", "لعلهم", "فلعلهم", "ولعلهم", "لعلهن", "فلعلهن", "ولعلهن", "لعلنا", "فلعلنا", "ولعلنا", "فكم",
			"وكم", "فلماذا", "ولماذا", "فماذا", "وماذا", "فمتى", "ومتى", "فأين", "وأين", "فاين", "واين", "فكيف", "وكيف",
			"كيفك", "فكيفك", "وكيفك", "كيفكما", "فكيفكما", "وكيفكما", "كيفكم", "فكيفكم", "وكيفكم", "كيفكن", "فكيفكن",
			"وكيفكن", "كيفه", "فكيفه", "وكيفه", "كيفها", "فكيفها", "وكيفها", "كيفهما", "فكيفهما", "وكيفهما", "كيفهم",
			"فكيفهم", "وكيفهم", "كيفهن", "فكيفهن", "وكيفهن", "كيفنا", "فكيفنا", "وكيفنا", "وأنى", "فأنى", "وأيان",
			"فأيان", "ولوما", "فلوما", "فهل", "وهل");
	public static List<String> FORCE = Arrays.asList("قد", "إن", "أن", "ان", "فقد", "وقد", "فإن", "وإن", "فأن", "وأن",
			"فان", "وان");
	public static List<String> NEGATION = Arrays.asList("لا", "لن", "لم", "ما", "فلا", "ولا", "فلن", "ولن", "فلم",
			"ولم", "فما", "وما", "فليس", "وليس");
	public static Map<String, Float> WORDS = createMap();

	private static Map<String, Float> createMap() {
		Map<String, Float> myMap = new HashMap<String, Float>();
		myMap.put("متميز", 3f);
		myMap.put("ممتاز", 2f);
		myMap.put("جدا", 1f);
		myMap.put("تنويه", 0f);
		myMap.put("قليل", -1f);
		myMap.put("مقبول", -2f);
		myMap.put("ضعيف", -3f);
		return myMap;
	}

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
		for (int o = 0; o < opinions.size(); o++) {
			List<Comment> comments = opinions.get(o).getComments();
			for (int j = 0; j < comments.size(); j++) {
				String lem, lem2;
				Float score, score2;
				Float scoreText = 0f;
				int indexT = 0;
				List<String> sentences = tokenizeTextToSentences(comments.get(j).getComment());
				for (int i = 0; i < sentences.size(); i++) {
					Float scoreSentence = 0f;
					int indexS = 0;
					String newNormalizedText = normalizer(sentences.get(i));
					String newCleanedTxt = removeStopWords(newNormalizedText, stopWords);
					List<String> sentenceLematized = lemmatizerSafar(newCleanedTxt);
					String[] words = tokenizeSentenceToWords(newCleanedTxt);
					// first bloc
					if (NEUTRAL.contains(words[0])) {
					} else if (FORCE.contains(words[0])) {
						// third bloc
						for (int k = 1; k < words.length; k++) {

							if (k < words.length - 2 && LIST.contains(NORMALIZER.normalizeDiacritics(words[k + 1]))) {
								lem = sentenceLematized.get(k);
								lem2 = sentenceLematized.get(k + 2);
								score = score(lem, lexique);
								score2 = score(lem2, lexique);

								if (score > 0f && score2 > 0f) {
									System.out.println(lem + " : " + score + " , " + lem2 + " : " + score2 + " , "
											+ words[k] + " " + words[k + 1] + " " + words[k + 2] + " : "
											+ (score + 1 + score2 + 1) / 2);
									scoreSentence += (score + 1 + score2 + 1) / 2;
									indexS++;
								} else if (score < 0f && score2 < 0f) {
									System.out.println(lem + " : " + score + " , " + lem2 + " : " + score2 + " , "
											+ words[k] + " " + words[k + 1] + " " + words[k + 2] + " : "
											+ (score - 1 + score2 - 1) / 2);
									scoreSentence += (score - 1 + score2 - 1) / 2;
									indexS++;
								} else if (score < 0f && score2 >= 0f) {
									scoreSentence += score - 1;
									indexS++;
								} else if (score > 0f && score2 <= 0f) {
									scoreSentence += score + 1;
									indexS++;
								}
								k += 2;
							} else if (k < words.length - 1
									&& NEGATION.contains(NORMALIZER.normalizeDiacritics(words[k]))) {
								lem = sentenceLematized.get(k + 1);
								score = score(lem, lexique);

								if (score > 0f) {
									scoreSentence += score * (-1) - 1;
									System.out.println(words[k] + " " + words[k + 1] + " : " + (score * (-1) - 1));
									indexS++;
								} else if (score < 0f) {
									scoreSentence += score * (-1) + 1;
									System.out.println(words[k] + " " + words[k + 1] + " : " + (score * (-1) + 1));
									indexS++;
								}
								k++;
							} else {
								lem = sentenceLematized.get(k);
								;
								score = score(lem, lexique);
								System.out.println(words[k] + " : " + (score));
								if (score > 0f) {
									System.out.println(words[k] + "(" + words[0] + ") : " + score + " +1");
									scoreSentence += score + 1;
									indexS++;
								} else if (score < 0f) {
									System.out.println(words[k] + "(" + words[0] + ") : " + score + " -1");
									scoreSentence += score - 1;
									indexS++;
								}
							}
						}
						// end third bloc

					} else {
						for (int k = 0; k < words.length; k++) {
							if (k < words.length - 1 && NEGATION.contains(NORMALIZER.normalizeDiacritics(words[k]))) {
								lem = sentenceLematized.get(k + 1);
								score = score(lem, lexique);
								System.out.println(words[k] + " " + words[k + 1] + " : " + score * (-1));
								if (score != 0) {
									scoreSentence += score * (-1);
									indexS++;
								}
								k++;

							} else if (k < words.length - 2
									&& LIST.contains(NORMALIZER.normalizeDiacritics(words[k + 1]))) {
								lem = sentenceLematized.get(k);
								lem2 = sentenceLematized.get(k + 2);
								score = score(lem, lexique);
								score2 = score(lem2, lexique);

								if (score * score2 > 0) {

									System.out.println(
											lem + " : " + score + " , " + lem2 + " : " + score2 + " , " + words[k] + " "
													+ words[k + 1] + " " + words[k + 2] + " : " + (score + score2) / 2);

									scoreSentence += (score + score2) / 2;
									indexS++;

								} else {
									System.out.println(lem + " : " + score + " , " + lem2 + " : " + score2 + " , "
											+ words[k] + " " + words[k + 1] + " " + words[k + 2] + " : " + score);
									if (score != 0) {
										scoreSentence += score;
										indexS++;
									}

								}
								k += 2;
							} else if (NEUTRAL.contains(words[k])) {
								break;
							} else {
								lem = sentenceLematized.get(k);
								score = score(lem, lexique);
								System.out.println(lem + " : " + score);
								if (score != 0) {
									scoreSentence += score;
									indexS++;
								}
							}

						}
					}
					// end first bloc
					if (indexS != 0) {
						scoreSentence /= indexS;
						// System.out.println("scoreSentence : " + scoreSentence);
						System.out.println("************************************************************************");
					}

					if (scoreSentence != 0) {
						scoreText += scoreSentence;
						indexT++;
					}
				}
				// end second bloc

				if (indexT != 0) {
					scoreText /= indexT;
				}
				System.out.println("scores : " + scores);
				scores.add(scoreText);
			}

		}
		return scores;
	}

	@Override
	public List<Opinion> sentimentOpinion(List<Opinion> opinions) {
		return null;
	}

	@Override
	public String sentimentOpinions() {
		return null;
	}

	// LemmatizerSAFAR
	public static List<String> lemmatizerSafar(String cleanedTxt) {
		List<String> lemmatized = new ArrayList<String>();
		String lem;

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

	// ----------------------------------------------------------------------------------------//
	// tokenizeTextToSentences
	public static List<String> tokenizeTextToSentences(String text) {
		List<String> newSentences = new ArrayList<String>();
		String sentences[] = text.split("\\.|\\؟|\\!");
		for (int i = 0; i < sentences.length; i++) {
			if (sentences[i].trim().isEmpty() == false) {
				newSentences.add(sentences[i]);
			}
		}
		return newSentences;
	}

	// ----------------------------------------------------------------------------------------//
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
	// -------------------------------------------------------------------------------------------------//

	// ReadLexicon
	public static List<List<String>> readLexicon(String path) throws IOException {
		List<List<String>> lexique = new ArrayList<List<String>>();
		List<String> mots;
		List<String> lines = Files.readAllLines(Paths.get(path));
		for (String line : lines) {
			mots = new ArrayList<String>();

			line = line.replace("\"", "");
			// with harakat
			mots.add(line.split(";")[1]);
			// without last Haraka
			mots.add(line.split(";")[2]);
			// without Harakat
			mots.add(line.split(";")[3]);
			// Score
			mots.add(line.split(";")[6]);

			lexique.add(mots);
		}
		return lexique;
	}

	// --------------------------------------------------------------------------------------------//

	// Scores
	public static Float score(String lem, List<List<String>> lexique) {
		Float score = 0f;
		int index = 0;
		if (WORDS.containsKey(NORMALIZER.normalizeDiacritics(lem))) {
			return WORDS.get(NORMALIZER.normalizeDiacritics(lem));
		}
		for (int j = 1; j < lexique.size(); j++) {
			if (lexique.get(j).get(2).equals(NORMALIZER.normalizeDiacritics(lem))) {
				score += Float.parseFloat(lexique.get(j).get(3));
				index++;
			}
		}
		if (index != 0) {
			score = score / index;
		}
		return score;
	}
}
