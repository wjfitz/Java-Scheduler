import java.util.BitSet;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class timeline {

	private static BitSet[] AVAILABILITY;
	private static String[][] TIMES_WHEN_WE_DO_SHIT;
	
	private static final String WEEK[] = {"M", "T", "W", "TH", "F", "SA", "SU"};
	
	private static boolean ADD_UNSUCCESSFUL = false;
	
	public static void initialize() {
		
		AVAILABILITY = new BitSet[7];
		TIMES_WHEN_WE_DO_SHIT = new String[7][24];
		
		for(int i = 0; i < 7; i++) {for (int j = 0; j < 24; j++) TIMES_WHEN_WE_DO_SHIT[i][j] = "FREE";}
		
		for(int i = 0; i < 7; i++) {
			AVAILABILITY[i] = new BitSet(24);
			AVAILABILITY[i].clear();
		}
	}
	
	/* name = name
	 * day = "M,T,W,TH,F,SA,SU"
	 * time = ##:##AM/PM
	 */
	public static boolean add(String name, String day, String time1, String time2) {
		boolean result = false;
		
		int t1 = LocalTime.parse(time1, DateTimeFormatter.ofPattern("hh:mma")).getHour();
		int t2 = LocalTime.parse(time2, DateTimeFormatter.ofPattern("hh:mma")).getHour();
		int d = getNumberedDay(day);
		
		if (AVAILABILITY[d].get(t1, t2+1).isEmpty()) {
			AVAILABILITY[d].set(t1, t2+1);
			doTimeKeeping(name, d, t1, t2);
			result = true;
		}
		return result;
	}
	
	/*
	 * this is the thing that has the +1 bug
	 * 
	 * plus an array out of bounds error depending on the things
	 */
	public static boolean add(String name, String day, int duration) {
		boolean result = false;
		
		int d = getNumberedDay(day);
		ArrayList<Integer> startTimes = new ArrayList<>();
		
		for(int i = 0; i < 24; i++) {
			
			BitSet slice = AVAILABILITY[d].get(i, i+duration);
			if (slice.isEmpty())
				startTimes.add(i);
		}
		
		if (!startTimes.isEmpty()) {
			result = true;
			
			int randomIndex = (int)(Math.random() * startTimes.size());
			int randomTime = startTimes.get(randomIndex);
			AVAILABILITY[d].set(randomTime, randomTime+duration);
			
			//put the local time format stuff in the format string function
			String time1 = LocalTime.parse(formatString(randomTime+"00"), DateTimeFormatter.ofPattern("Hmm")).format(DateTimeFormatter.ofPattern("hh:mma"));
			String time2 = LocalTime.parse(formatString( (randomTime+duration-1) + "00"), DateTimeFormatter.ofPattern("Hmm")).format(DateTimeFormatter.ofPattern("hh:mma"));
			doTimeKeeping(name, d, randomTime, randomTime+duration-1);
		}
		
		return result;
	}
	
	public static boolean add(String name, int duration) {
		boolean result = false;
		
		int randomDay = (int)(Math.random() * 7);
		BitSet guesses = new BitSet(7);
		
		while(guesses.cardinality() < 7) {
			if (guesses.get(randomDay)) {
				randomDay = (int)(Math.random() * 7);
				continue;
			} else if (add(name, WEEK[randomDay], duration)) {
				result = true;
				break;
			} else {
				guesses.set(randomDay);
				randomDay = (int)(Math.random() * 7);
			}
		}
		
		return result;
	}
	
	public static void addMult(String name, String days[], String time1, String time2) {
		boolean added = false;
		
		for (String s : days) {
			added = add(name, s, time1, time2);
			if (added == false) {System.out.println("add unsuccessful"); ADD_UNSUCCESSFUL = true;}
		}
	}
	
	public static void addMult(String name, String days[], int duration) {
		boolean added = false;
		
		for(String s : days) {
			added = add(name, s, duration);
			if (added == false) {System.out.println("add unsuccessful"); ADD_UNSUCCESSFUL = true;}
		}
	}
	
	public static void addMult(String name, int duration, int amount) {
		for(int i = 0; i < amount; i++) {
			if (add(name, duration) == false) {System.out.println("add unsuccessful"); ADD_UNSUCCESSFUL = true;}
		}
	}
	
	/*
	 * the problem is here. well actually....
	 */
	private static void doTimeKeeping(String s, int day, int t1, int t2) {
		for(int i = t1; i <= t2; i++) {
			if (i != 24) {
				TIMES_WHEN_WE_DO_SHIT[day][i] = s;
			}else
				TIMES_WHEN_WE_DO_SHIT[day][i-1] = s;
		}
	}
	
	private static int getNumberedDay(String day) {
		for(int i = 0; i < 7; i++) {
			if (WEEK[i].equals(day))
				return i;
		}
		return -1;
	}
	
	public static void printSchedule() {
		
		for(int i = 0; i < 7; i++) {
			System.out.println("=========="+WEEK[i]+"==========");
			for(int j = 0; j < 24; j++) {
				String time = LocalTime.parse(j+"00", DateTimeFormatter.ofPattern("Hmm")).format(DateTimeFormatter.ofPattern("hh:mma")); //put the local time stuff in the format string function
				
				String task = TIMES_WHEN_WE_DO_SHIT[i][j];
				if (task.equalsIgnoreCase("LOST TO TIME") || task.equalsIgnoreCase("TBD")) {
					continue;
				}else {
					System.out.println(time + ": " + task);
				}
			}
		}
	}
	
	public static void printSchedule(int day) {
		for(int j = 0; j < 24; j++) {
			String time = LocalTime.parse(j+"00", DateTimeFormatter.ofPattern("Hmm")).format(DateTimeFormatter.ofPattern("hh:mma")); //put the local time stuff in the format string function
			System.out.println(time + ": " + TIMES_WHEN_WE_DO_SHIT[day][j]);
		}
	}
	
	public static void printAvailability(int day) {
		for(int j = 0; j < 24; j++) {
			String time = LocalTime.parse(j+"00", DateTimeFormatter.ofPattern("Hmm")).format(DateTimeFormatter.ofPattern("hh:mma")); //put the local time stuff in the format string function
			System.out.println(time + ": " + AVAILABILITY[day].get(j));
		}
	}
	
	public static String formatString(String s) {
		String result = "N/A";
		String[] shit = s.split(":");
		
		if (shit[0].length() == 1)
			result = "0" + s;
		else
			result = s;
		
		String endbits = result.substring(result.length() - 2);
		result = result.replace(endbits, endbits.toUpperCase());
		
		return result;
	}
	
	static class Task{
		String name; String timeStart; String timeEnd; String day;
		public Task(String n, String d, String s, String e) {
			name = n; day = d; timeStart = s; timeEnd = e;
		}
	}
	
	/*
	 * we not doing multi-day adding yet
	 */
	public static void main(String[] args) {
		initialize();
		
		String days[];
		String name, day, time1, time2;
		int length, amount;
		
		try {
			File f = new File("schedule.txt");
			
			if (!f.exists()) {
				f.createNewFile();
				System.out.println("created file. format it then run the program again");
				System.exit(0);
			}
			BufferedReader fr = new BufferedReader(new FileReader(f));
			String line = fr.readLine();
			while (line != null) {
				
				while (line.equals(""))
					line = fr.readLine();
					
				String[] input = line.split(" # ");
				
				if (input.length == 4) {
					
					if (input[1].contains(",") && input[2].contains("M") && input[3].contains("M")) {
						time1 = input[2]; 
						time2 = input[3];
						name = input[0];
						days = input[1].split(",");
						addMult(name, days, time1, time2);
 					} else {
						name = input[0]; 
						day = input[1]; 
						time1 = input[2]; 
						time2 = input[3];
						
						if (add(name, day, time1, time2) == false) {System.out.println("add unsuccessful"); ADD_UNSUCCESSFUL = true;}
					}
					
				} else if (input.length == 3) {
					
					if (input[1].contains(",")) {
						name = input[0];
						length = Integer.parseInt(input[2]);
						days = input[1].split(",");
						
						addMult(name, days, length);
					} else if (getNumberedDay(input[1]) == -1) {
						name = input[0];
						length = Integer.parseInt(input[1]);
						amount = Integer.parseInt(input[2]);
						
						addMult(name, length, amount);
					}else {
						name = input[0];
						day = input[1];
						length = Integer.parseInt(input[2]);
						
						if (add(name, day, length) == false) {System.out.println("add unsuccessful"); ADD_UNSUCCESSFUL = true;}
					}
					
				} else if (input.length == 2) {
					if (add(input[0], Integer.parseInt(input[1])) == false) System.out.println("add unsuccessful");
				} else {
					System.out.println("incorrect formatting. reformat schedule file and try again");
					System.exit(0);
				}
				
				line = fr.readLine();
			}
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (ADD_UNSUCCESSFUL == false) {
			printSchedule();
		}
	}
	
}
