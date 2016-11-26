import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.model.output_info.ConceptOutputInfo;
import clarifai2.dto.prediction.Concept;

//Install the Java helper library from twilio.com/docs/java/install
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class Main {
	
	public static final String ACCOUNT_SID = "-----";
    public static final String AUTH_TOKEN = "-----";
    public static final String contactNum = "-----";
    public static final double MINIMUM_PROBABILITY = 0.3; //30%
	
	private static void sendSMS(String msg) throws URISyntaxException{
	
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message message = Message.creator(
			new PhoneNumber(contactNum),  // to
			new PhoneNumber("-----"),  // from, Twilio Phone Number
			msg).create();
	}
	
	private static void analyzeFrames(ArrayList<ArrayList<List<String>>> w) {
				
		double[] averages = new double[9];
		int count = 0;
		
		for (int i = 0; i < w.size(); i++) {
			for (int j = 0; j < 9; j++) {
				averages[j] += Double.parseDouble(w.get(i).get(j).get(1));
			}
		}
		
		ArrayList<String> tags = new ArrayList<String>();
		
		for (int j = 0; j < 9; j++) {
			
			averages[j] /= w.size();
			
			if (j != 0 && j != 6 && averages[j] >= MINIMUM_PROBABILITY) {
				tags.add(w.get(0).get(j).get(0));
				count++;
			}
		}
				
		if (count >= 3) {
			System.out.println("Footage appears to be Abnormal - manual inspection is advised.");
			System.out.println("Tags: " + tags);
			
			try {
				sendSMS("Footage appears to be Abnormal - manual inspection advised. Tags: " + tags);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if(averages[0] > averages[6]) {
			
			System.out.println("Footage appears to be abnormal - manual inspectiom is advised.");
			
			try {
				sendSMS("Footage appears to be Abnormal - manual inspection advised.");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			System.out.println("Footage appears to be normal - no action is necessary.");
		}
	}
	
	public static void main(String[] args) {
		
		boolean haveCreatedModel = true;
		ConceptModel response = null;
		//final ConceptModel alertModel;
		
		final ClarifaiClient client =
			    new ClarifaiBuilder("-CHB4ySAjGq4xXuTGb8ThFX6f1grsfC6qbv4oXXA", "5zgwVM6qQ_LsUt58F-NMHp500lWfqEU-n9EHjJ14").buildSync();
		
		// Mohsin: !BEWARE! Used to delete a model
				//client.deleteModel("abnormal_m").executeSync().get();
		// Mohsin: !BEWARE! Deleting all inputs
				//client.deleteAllInputs().executeSync();
				
		client.addConcepts()
			  .plus(
			        Concept.forID("robbery"),
			        Concept.forID("knife"),
			        Concept.forID("fist fight"),
			        Concept.forID("gun"),
			        Concept.forID("weapon"),
			        Concept.forID("vandalism"),
			        Concept.forID("car crash"),
			        Concept.forID("normal"),
			        Concept.forID("abnormal")
			    )
			    .executeSync();
//		
//		// Training
//		
		
		client.addInputs()
			.plus(ClarifaiInput.forImage(ClarifaiImage.of("https://tribktla.files.wordpress.com/2013/03/liquor-store-ron.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(true),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(true), 
							Concept.forID("weapon").withValue(true), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true))).executeSync();
		
//		// Mohsin: Adding images with concepts
		client.addInputs()
			.plus(
				// Black Sweatshirt Robber with weapon going after white male
				ClarifaiInput.forImage(ClarifaiImage.of("http://files.pressherald.com/uploads/2015/06/661093_681635-robber.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(true),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(true), 
							Concept.forID("weapon").withValue(true), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true)),
					// Two black men throwing punches
					ClarifaiInput.forImage(ClarifaiImage.of("http://i.imgur.com/7mizV1Q.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(true), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true)),
					// Guy in black doing graffitee
					ClarifaiInput.forImage(ClarifaiImage.of("https://media.apnarm.net.au/img/media/images/2015/06/16/FFC_17-06-2015_LETTERS_01_ThinkstockPhotos-467921573_ct620x465.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(true), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true)),
					// Justin Bieber doing graffiti
					ClarifaiInput.forImage(ClarifaiImage.of("http://akns-images.eonline.com/eol_images/Entire_Site/2013107/rs_600x600-131107083433-600-2justin-bieber-instagram.ls.11713.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(true), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true)),
					// Dog walking, two old people
					ClarifaiInput.forImage(ClarifaiImage.of("http://www.houstonpettalk.com/wp-content/uploads/2010/02/Walking-Dog.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(true), 
							Concept.forID("abnormal").withValue(false)),
					// Woman with black shirt just standing
					ClarifaiInput.forImage(ClarifaiImage.of("http://www.dianecarbonell.com/wp-content/uploads/2010/07/DSC_1243-e1280446941687-680x1024.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(true), 
							Concept.forID("abnormal").withValue(false)),
					// Two men talking on the public train
					ClarifaiInput.forImage(ClarifaiImage.of("http://i.onionstatic.com/onion/1810/6/16x9/600.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(false)),
					// man walking his dog, alone in frame, white dog
					ClarifaiInput.forImage(ClarifaiImage.of("http://safety.fhwa.dot.gov/ped_bike/pssp/fhwasa10035/images/index_clip_image002_0004.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(false)),
					// bustling taxis and small cars in NYC
					ClarifaiInput.forImage(ClarifaiImage.of("http://a.img-zemotoring.com/media/news/2010/12/hertz-smart-nyc-04.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(false)),
					// gray shirt guy stabbing a black shirt guy
					ClarifaiInput.forImage(ClarifaiImage.of("http://i2.cdn.turner.com/cnnnext/dam/assets/160107123248-reporter-stabbing-israel-demonstration-pkg-00015217-large-169.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(true),
							Concept.forID("knife").withValue(true), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(true), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true)),
					// popo wielding a long blade
					ClarifaiInput.forImage(ClarifaiImage.of("http://media1.s-nbcnews.com/j/newscms/2015_31/1149021/150730-jerusalem-stabbing-1624_71f74dd2e0930b5a7671c8a282f2e855.nbcnews-ux-2880-1000.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(true),
							Concept.forID("knife").withValue(true), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(true), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true)),
					// car crash
					ClarifaiInput.forImage(ClarifaiImage.of("https://upload.wikimedia.org/wikipedia/commons/e/e1/Ljubljana_car_crash_2013.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(false),
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(true), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true)),
					// woman's purse being stolen by man with no weapon, masked
				ClarifaiInput.forImage(ClarifaiImage.of("http://i.imgur.com/Hh0DKjH.jpg"))
					.withConcepts(
							Concept.forID("robbery").withValue(true), 
							Concept.forID("knife").withValue(false), 
							Concept.forID("fist fight").withValue(false), 
							Concept.forID("gun").withValue(false), 
							Concept.forID("weapon").withValue(false), 
							Concept.forID("vandalism").withValue(false), 
							Concept.forID("car crash").withValue(false), 
							Concept.forID("normal").withValue(false), 
							Concept.forID("abnormal").withValue(true))).executeSync();

		final ConceptModel alertModel = client.createModel("abnormal_m")
				    .withOutputInfo(ConceptOutputInfo.forConcepts(
				        Concept.forID("robbery"),
				        Concept.forID("knife"),
				        Concept.forID("fist fight"),
				        Concept.forID("gun"),
				        Concept.forID("weapon"),
				        Concept.forID("vandalism"),
				        Concept.forID("car crash"),
				        Concept.forID("normal"),
				        Concept.forID("abnormal")
				    ))
				    .executeSync()
				    .get();
	
			alertModel.train().executeSync();


			final List<?> predictionResults2 =
					client.getModelByID("abnormal_m").executeSync().get().predict()
					.withInputs(ClarifaiInput.forImage(ClarifaiImage.of("https://samples.clarifai.com/metro-north.jpg")),
							ClarifaiInput.forImage(ClarifaiImage.of("https://tribktla.files.wordpress.com/2016/01/hb-mobil-attempt-robbery.jpg")))
					.executeSync()
					.get();
			
			//System.out.println("--- FIRST IMAGE ---");
			
			ArrayList<ArrayList<List<String>>> wrapperList = new ArrayList<ArrayList<List<String>>>();
			
			
			List<ClarifaiOutput> out = null;
			for(int i = 0; i < predictionResults2.size(); i++) {
				ArrayList<List<String>> myList = new ArrayList<List<String>>();
				ClarifaiOutput output = (ClarifaiOutput) predictionResults2.get(i);
				out = output.data();
				//System.out.println("woo");
				for(int j = 0; j < out.size(); j++) {
					//System.out.println(out.get(i));

	     			String toParse = String.valueOf(out.get(j));
					int startConcept = toParse.indexOf("=") + 1;
					int endConcept = toParse.indexOf(",");
					String conceptString = toParse.substring(startConcept, endConcept); 
					//System.out.println(conceptString);
					
					int startValue = toParse.indexOf("=0") + 2;
					int endValue = toParse.indexOf("}");
					String valueString = toParse.substring(startValue, endValue);
					//System.out.println(valueString);
					
					List<String> tempList = new ArrayList<String>();
					tempList.add(conceptString);
					tempList.add(valueString);
					myList.add(tempList);
				}
				wrapperList.add(myList);
				//System.out.println("--- NEXT IMAGE ---");
			}
			
// 			for(int i = 0; i < wrapperList.size(); i++) {
// 				System.out.println(wrapperList.get(i));
// 			}
			
			analyzeFrames(wrapperList);
			
			//imgProperties = [ ["abnormal", .135325476], ["car crash", ], [], [], [], [], [], [], [] ]
			
//		client.predict("abnormal_m")
//			.withInputs(
//					ClarifaiInput.forImage(ClarifaiImage.of("http://2.bp.blogspot.com/-DnF8fY0BhW4/UVjbGMo6jmI/AAAAAAAACEk/qtPz-pYSPJ4/s1600/IMG_0389.JPG"))
//			).executeSync().get();
		
	}
}