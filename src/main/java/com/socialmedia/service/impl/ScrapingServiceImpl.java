package com.socialmedia.service.impl;

import com.socialmedia.model.Comment;
import com.socialmedia.model.Opinion;
import com.socialmedia.service.i.ScrapingServiceI;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ScrapingServiceImpl implements ScrapingServiceI {
	Opinion opinion;
	List<Comment> comments;
	Comment comment;
	List<WebElement> dateList;
	WebDriver driver;
	WebDriverWait wait;
	ChromeOptions options = new ChromeOptions();

	@Override
	public Opinion getOpinionsHespress(String req, int nbr) throws InterruptedException {
		opinion = new Opinion();
		comments = new ArrayList<Comment>();
		options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors");

		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		WebDriverWait wait = new WebDriverWait(driver, 15);
		driver.get("https://www.hespress.com/");

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("s"))).sendKeys(req, Keys.ENTER);
		// try {
		List<WebElement> cardList = wait
				.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("stretched-link")));
		List<WebElement> Comlist;
		List<WebElement> Datelist;
		int i = 0;
		Long l = 1L;
		JavascriptExecutor js = ((JavascriptExecutor) driver);
		Object new_height;
		Object last_height = js.executeScript("return document.body.scrollHeight");
		while (comments.size() < nbr) {
			cardList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("stretched-link")));
			System.out.println(cardList.size());
			cardList.get(i).click();
			try {
				Comlist = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("comment-text")));
				Datelist = wait
						.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("comment-date")));
				for (int j = 0; j < Comlist.size(); j++) {
					if (comments.size() >= nbr) {
						driver.close();
						opinion.setIdOp(2L);
						opinion.setComments(comments);
						opinion.setSite("Hespress");
						return opinion;
					}
					Comment comment = new Comment();
					System.out.println("***************************");
					comment.setIdCo(l);
					comment.setComment(Comlist.get(j).getText());
					comment.setDate(Datelist.get(j).getText());
					comments.add(comment);
					System.out.println(comments.size());
					l += 1L;
				}
			} catch (Exception e) {
				System.out.println("There are no comments in this article");
			}
			driver.navigate().back();
			i++;
			System.out.println("i  " + i);

			if (cardList.size() <= i) {
				js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
				Thread.sleep(500);
				new_height = js.executeScript("return document.body.scrollHeight");

				if (new_height == last_height) {
					break;
				}
				last_height = new_height;
				cardList = wait
						.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("stretched-link")));

				System.out.println("cardList  " + cardList.size());
				cardList.get(i).click();
				driver.navigate().back();
				i = 0;
			}

		}

		/*
		 * } catch (Exception e) {
		 * System.out.println("There are no articles on hespress"); }
		 */
		driver.close();
		opinion.setIdOp(2L);
		opinion.setComments(comments);
		opinion.setSite("Hespress");
		return opinion;
	}

	@Override
	public Opinion getOpinionsTwitter(String req, int nbr) throws InterruptedException {
		opinion = new Opinion();
		comments = new ArrayList<Comment>();
		options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors");
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		driver.get("https://www.twitter.com/login");
		wait = new WebDriverWait(driver, 15);
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("@ubsmisafar",
					Keys.ENTER);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys("safar123",
					Keys.ENTER);
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//input[@aria-label='Requête de recherche']")))
					.sendKeys(req, Keys.ENTER);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Récent"))).click();
		} catch (Exception e) {
			System.out.println("Prb de connexion");
		}
		Long l = 1L;
		JavascriptExecutor js = ((JavascriptExecutor) driver);
		Object new_height;
		Object last_height = null;
		try {
			while (comments.size() < nbr) {
				List<WebElement> list = wait.until(ExpectedConditions
						.visibilityOfAllElementsLocatedBy(By.xpath("//article[@data-testid='tweet']")));

				for (int i = 0; i < list.size(); i++) {
					if (comments.size() >= nbr) {
						driver.close();
						opinion.setIdOp(3L);
						opinion.setComments(comments);
						opinion.setSite("Twitter");
						System.out.println("Twitter done********************************");
						return opinion;
					}

					comment = new Comment();
					comment.setIdCo(l);
					comment.setDate(list.get(i).findElement(By.tagName("time")).getAttribute("datetime"));
					comment.setComment(list.get(i).findElement(By.xpath(".//div[2]/div[2]/div[1]")).getText()
							+ list.get(i).findElement(By.xpath(".//div[2]/div[2]/div[2]")).getText());
					comments.add(comment);
					System.out.println(comments.size());
					l += 1L;

				}
				js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
				Thread.sleep(500);
				new_height = js.executeScript("return document.body.scrollHeight");

				if (new_height == last_height)
					break;
				last_height = new_height;
			}
		} catch (Exception e) {
			System.out.println("There are no tweets about this subject");
		}
		driver.close();
		opinion.setIdOp(3L);
		opinion.setComments(comments);
		opinion.setSite("Twitter");
		System.out.println("Twitter done********************************");
		return opinion;
	}

	@Override
	public Opinion getOpinionsYoutube(String req, int nbr) throws InterruptedException {
		opinion = new Opinion();
		comments = new ArrayList<Comment>();
		Long l = 1L;
		ChromeOptions options = new ChromeOptions();
		List<WebElement> cardList = new ArrayList<WebElement>();
		List<WebElement> comList = new ArrayList<WebElement>();
		int index = 0;

		options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors");
		WebDriver driver = new ChromeDriver(options);

		// Access the link site
		driver.get("https://www.youtube.com/");
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(12, TimeUnit.SECONDS);
		WebDriverWait wait = new WebDriverWait(driver, 15);

		// Search
		WebElement element = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='search_query']")));
		element.click();
		element.sendKeys(req);
		element.sendKeys(Keys.RETURN);
		String url = driver.getCurrentUrl();
		System.out.println(url);
		driver.get(url);
		// WebElement element2 =
		// wait.until(ExpectedConditions.elementToBeClickable(By.id("search-icon-legacy")));
		// element2.click();

		while (comments.size() < nbr) {
			System.out.println("index" + index);
			// List of videos
			try {
				cardList = wait
						.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id='video-title']")));
				System.out.println(cardList.size());
			} catch (Exception e) {
				System.out.println("There are no videos on Youtube");
				return opinion;
			}
			// JavascriptExecutor jse = (JavascriptExecutor) driver;
			if (index < cardList.size()) {
				// jse.executeScript("arguments[0].click();", cardList.get(index));
				cardList.get(index).click();

				if (index == 0) {
					wait.until(ExpectedConditions
							.elementToBeClickable(By.xpath("//button[@aria-label='Désactiver le son (m)']"))).click();
				}
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='Pause (k)']"))).click();

				driver.manage().timeouts().implicitlyWait(12, TimeUnit.SECONDS);

				// scroll down to load comments
				JavascriptExecutor js = ((JavascriptExecutor) driver);
				js.executeScript("window.scrollTo(0,390);");
				Thread.sleep(5000);

				// Loads 20 comments , scroll two times to load next set of 40 comments.
				for (int j = 0; j < 2; j++) {
					js.executeScript(
							"window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight))");
					Thread.sleep(5000);
				}

				// List of comments for each video
				try {
					comList = wait.until(
							ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id='content-text']")));
					dateList = wait.until(
							ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id='header-author']")));
					System.out.println(comList.size());
					for (int i = 0; i < comList.size(); i++) {
						if (comments.size() >= nbr) {
							driver.close();
							opinion.setIdOp(4L);
							opinion.setComments(comments);
							opinion.setSite("Youtube");
							return opinion;
						}
						System.out.println(comList.get(i).getText());
						comment = new Comment();
						comment.setIdCo(l);
						comment.setDate(dateList.get(i).findElement(By.tagName("yt-formatted-string")).getText());
						comment.setComment(comList.get(i).getText());
						comments.add(comment);
						l += 1L;
					}
				} catch (Exception e) {
					System.out.println("There are no comments in this video");
				}
				System.out.println(comments.size());
				driver.navigate().back();
				index++;
			} else {
				break;
			}
		}
		driver.close();
		opinion.setIdOp(4L);
		opinion.setComments(comments);
		opinion.setSite("Youtube");
		return opinion;
	}

	@Override
	public void downloadOpinions(String req, List<Opinion> opinions) {
		String header = "Opinion;Site;Date";
		OutputStreamWriter file = null;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		System.out.println(dtf.format(now));
		try {
			file = new OutputStreamWriter(new FileOutputStream(req + dtf.format(now) + ".csv"), "UTF-8");
			file.append(header);
			file.append("\n");
			Iterator<Opinion> it = opinions.iterator();
			while (it.hasNext()) {
				Opinion b = (Opinion) it.next();
				for (int i = 0; i < b.getComments().size(); i++) {
					file.append(b.getComments().get(i).getComment().replaceAll("\n", "").replaceAll("\r", "")
							.replaceAll(";", ","));
					file.append(";");
					file.append(b.getSite());
					file.append(";");
					file.append(b.getComments().get(i).getDate());
					file.append("\n");
				}
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Opinion getOpinionsAljazeera(String req, int nbr) {
		opinion = new Opinion();
		comments = new ArrayList<Comment>();
		Long l = 1L;
		options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors");
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		wait = new WebDriverWait(driver, 15);
		driver.get("https://www.aljazeera.net/");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("no-styles-button"))).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("search-bar__input"))).sendKeys(req,
				Keys.ENTER);
		List<WebElement> list = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.tagName("article")));
		opinion = new Opinion();
		for (int i = 0; i < list.size(); i++) {

			comment = new Comment();
			comment.setIdCo(l);
			// comment.setDate();
			comment.setComment(list.get(i).findElement(By.tagName("span")).getText());
			comments.add(comment);
			l += 1L;

		}
		driver.close();
		opinion.setIdOp(2L);
		opinion.setComments(comments);
		opinion.setSite("Aljazeera");
		System.out.println("aljazeera done********************************************");
		return opinion;
	}

	@Override
	public Opinion getOpinionsFacebook(String req, int nbr) {
		opinion = new Opinion();
		comments = new ArrayList<Comment>();
		Long l = 1L;
		ChromeOptions options = new ChromeOptions();
		List<WebElement> comList = new ArrayList<WebElement>();
		List<WebElement> cardList = new ArrayList<WebElement>();
		List<WebElement> more = new ArrayList<WebElement>();
		int i = 0;

		options.addArguments("--disable-notifications");
		options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors");
		WebDriver driver = new ChromeDriver(options);

		// Access the link site
		driver.get("https://www.facebook.com/aljazeerachannel");
		driver.manage().window().maximize();
		WebDriverWait wait = new WebDriverWait(driver, 15);

		try {
			// Authentication
			WebElement selectGender = wait.until(ExpectedConditions.elementToBeClickable(
					By.xpath("//div[@class='_4qb-']/a[@class='_42ft _4jy0 _3obb _4jy6 _4jy1 selected _51sy']")));
			selectGender.click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("ubsmisafar9@gmail.com");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pass"))).sendKeys("safar123", Keys.ENTER);

			// Search
			WebElement selectGender2 = wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//div[@class='h676nmdw']/div[@aria-label='Search']")));
			selectGender2.click();
			WebElement selectGender3 = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("//input[@aria-label='Search this Page']")));
			selectGender3.click();
			wait.until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@aria-label='Search this Page']")))
					.sendKeys(req, Keys.ENTER);
		} catch (Exception e) {
			System.out.println("Prb de connexion");
		}
		while (comments.size() < nbr) {

			// List of articles
			try {
				cardList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
						By.xpath("//div[@class='jb3vyjys hv4rvrfc ihqw7lf3 dati1w0a']")));
			} catch (Exception e) {
				System.out.println("There are no articles on Facebook");
				break;
			}

			System.out.println("Card " + i);
			cardList.get(i).click();
			try {
				more = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(
						"//div[@aria-posinset]//div[contains(@aria-label,'Comment by')]//div[@dir='auto']//div[@tabindex='0']")));
				for (int j = 0; j < more.size(); j++) {
					more.get(j).click();
				}
				dateList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
						By.xpath("//div[@aria-posinset]//div[contains(@aria-label,'Comment by')]//ul/li[4]/a")));

				// List of comments for each article
				comList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(
						"//div[@aria-posinset]//div[contains(@aria-label,'Comment by')]//div[@class='ecm0bbzt e5nlhep0 a8c37x1j']")));
				for (int j = 0; j < comList.size(); j++) {
					if (comments.size() >= nbr) {
						driver.close();
						opinion.setIdOp(5L);
						opinion.setComments(comments);
						opinion.setSite("Facebook");
						return opinion;
					}
					System.out.println(comList.get(j).getText());
					comment = new Comment();
					comment.setIdCo(l);
					comment.setDate(dateList.get(j).getText());
					comment.setComment(comList.get(j).getText());
					comments.add(comment);
					l += 1L;
				}
				System.out.println(comList.size());
				System.out.println(comList.get(0).getText());
			} catch (Exception e) {
				System.out.println("There are no comments in this article");
			}
			driver.navigate().back();
			i++;

		}
		driver.close();
		opinion.setIdOp(5L);
		opinion.setComments(comments);
		opinion.setSite("Facebook");
		return opinion;
	}

}
