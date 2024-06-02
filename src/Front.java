import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;




public class Front {
	
	public static void LogIn(WebDriver driver, String userName, String password) {
		driver.get("https://www.linkedin.com/home");
		driver.manage().window().maximize();
		driver.findElement(By.xpath("//a[contains(text(),'Sign in')]")).click();
		driver.findElement(By.id("username")).sendKeys(userName);
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.xpath("//button[contains(text(),'Sign in')]")).click();
	}
	
	public static String[] GoToProfile(WebDriver driver) {
		String arr[] = new String[3]; //Array for name,work place and city
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15)); 
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("global-nav-search"))); //Waiting if verification comes up after login 
		driver.findElement(By.id("ember17")).click();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
		driver.findElement(By.partialLinkText("View Profile")).click();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
		
		arr[0] = driver.findElement(By.cssSelector("h1.text-heading-xlarge")).getAttribute("innerText"); //Getting the name from profile
		arr[1] = driver.findElement(By.cssSelector("div.text-body-medium")).getAttribute("innerText"); //Getting the work place from profile
		arr[2] = driver.findElement(By.cssSelector("span.text-body-small.inline")).getAttribute("innerText"); //Getting the city from profile
		
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
		driver.findElement(By.partialLinkText("connection")).click(); // Getting into the connections list
		
		return arr;
	}

	// Trying to scroll down to get all connections by going to the last element every time
	public static void ScrollDown(WebDriver driver) {
		try {
			boolean reachedBottom = false;
			while (!reachedBottom) {
				List<WebElement> elements = driver.findElements(By.className("mn-connection-card__details"));
				WebElement lastElement = elements.get(elements.size() - 1);
				new Actions(driver).moveToElement(lastElement).perform();
                Thread.sleep(1000);
				List<WebElement> newElements  = driver.findElements(By.className("mn-connection-card__details"));
				reachedBottom = elements.size() == newElements.size();
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//Keep scrolling after clicking the Show more results button
	public static void KeepScrollDown(WebDriver driver) {
		try {
        	new Actions(driver).sendKeys(Keys.PAGE_DOWN).perform();
			Thread.sleep(1000);
			driver.findElement(By.xpath("/html[1]/body[1]/div[4]/div[3]/div[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/main[1]/div[1]/section[1]/div[2]/div[2]/div[1]/button[1]")).click();
			
			ScrollDown(driver);
			
		} catch (Exception e) {}
	}
	
	//Creating a json file with all the connections
	public static void CreateJson(WebDriver driver, String name, String workPlace, String city) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject data = new JsonObject();
		data.addProperty("myName", name);
		data.addProperty("myWorkplace", workPlace);
		data.addProperty("city", city);
		
		JsonArray connections = new JsonArray();
		List<WebElement> connectionsList = driver.findElements(By.className("mn-connection-card__details"));
		for (WebElement connection : connectionsList) {
			connections.add(connection.findElement(By.className("mn-connection-card__name")).getText());
			connections.add(connection.findElement(By.className("mn-connection-card__occupation")).getText());
			connections.add(connection.findElement(By.className("time-badge")).getText());
		}
		data.add("connections", connections);
		
		try (FileWriter file = new FileWriter("connections.json")) {
			gson.toJson(data, file);
		    file.flush();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		//LogIn details
		String userName = "omerm2611@gmail.com";
		String password = "112233445566";
		
		//Creating the WebDriver
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\omerm\\eclipse-workspace\\Assignment\\drivers\\chromedriver.exe");
		WebDriver driver  = new ChromeDriver();
		
		LogIn(driver,userName,password);

		String arr[] = GoToProfile(driver);
		
		//Scroll to the bottom of connections
		ScrollDown(driver);	
		KeepScrollDown(driver);

		// Write JSON to file
		CreateJson(driver, arr[0], arr[1], arr[2]);
		
		driver.quit();
	}
}
