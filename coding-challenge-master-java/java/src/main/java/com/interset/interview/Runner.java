package com.interset.interview;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.lang.Math;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Runner {

    /**
     * This is main method which is starting point for this application.
     * It requires 1 arguments to run successfully.
     *
     * @param: args[0] : Path to JSON or CSV file to read.
     *
     * The JSON and CSV files must contain the following fields:
     *  name, siblings, favourite_food, birth_timezone, birth_timestamp
     *
     * This application parses the files and prints out the following information:
     *       - Average number of siblings (round up)
     *       - Top 3 favourite foods
     *       - How many people were born in each month of the year (uses the month of each person's respective timezone of birth)
     *
     */
   
    public static JSONArray ReadFile(String filepath) {

        //Get filename, filetype for file reading
        String[] fsdelimeter = filepath.split("\\\\");
        String[] dotdelimeter = filepath.split("\\.");
        String filename = fsdelimeter[fsdelimeter.length-1];
        String filetype = dotdelimeter[dotdelimeter.length-1];
        JSONArray arr = new JSONArray();

        try  {       
             
            // Read zip file
             if (filetype.equals("gz")) {

                File file = new File(filepath.substring(0, filepath.lastIndexOf(File.separator)) + "/gizoutput");

                System.out.println(file);

                if (!file.exists()) {
                    file.mkdir();
                }
                String gzipFile = filepath;
                String newFile = filepath.substring(0, filepath.lastIndexOf(File.separator)) + File.separator + "gizoutput" + File.separator + filename.replace("gz", "");

                FileInputStream fis = new FileInputStream(gzipFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                FileOutputStream fos = new FileOutputStream(newFile);
                byte[] buffer = new byte[1024];
                int len;
                while((len = gis.read(buffer)) != -1){
                    fos.write(buffer, 0, len);
                }
                //close resources
                fos.close();
                gis.close();
                filepath = newFile;
                filetype = filepath.split("\\.")[1];
            }

            //Create Buffer Reader for file reading
            BufferedReader br = new BufferedReader(new FileReader(filepath));

            //Read json file
            if (filetype.equals("json")) {

                //JSON parser object to parse read file
                JSONParser jsonParser = new JSONParser();

                Object obj = jsonParser.parse(br);
                arr = (JSONArray) obj;
            
            }

            //Read csv file
            else if (filetype.equals("csv")) {

                HashMap<String,String> map = new HashMap<String, String>();
                String line = null;
                int count = 0;
                String[] header = {};

                while ((line = br.readLine()) != null) {
                    if (count == 0) {
                    header = line.split(",");
                    }
                    // use comma as separator
                    else {
                        String[] data = line.split(",");
                        for(int i = 0; i < data.length; i++) {
                            map.put(header[i], data[i].toString());
                        }
                    }
                    JSONObject obj = new JSONObject(map);
                    arr.add(obj);
                    count = count + 1;
                }
                arr.remove(0);
                br.close();
            }
        }
   
        // Throw exception 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        
        catch (IOException e) {
            e.printStackTrace();
        } 
        
        catch (ParseException e) {
            e.printStackTrace();
        }

    return arr;
    }

    //Question 1: Average number of siblings (round up)
    public static String AverageSiblings(JSONArray arr) {

        //Create array list for data ingestion from json array
        ArrayList<Integer> siblingsarr = new ArrayList<>(); 

        //Loop array
        for (int i = 0; i < arr.size(); i++) {

            JSONObject population = (JSONObject) arr.get(i);
            int siblingsvalue = (int) Integer.parseInt( (String) population.get("siblings"));
            siblingsarr.add(siblingsvalue);

        }

        //Calculate Average
        int sum = siblingsarr.stream().mapToInt(x -> x).sum();
        int n = siblingsarr.stream().mapToInt(x -> 1).sum();   
        long average =  (long) Math.ceil(sum/n);

        return "Average siblings: " + average;
    }

    //Question 2: Top 3 favourite foods and the number of people who like them
    public static String TopFavouriteFood(JSONArray arr) {

        ArrayList<String> favouritefoodarr = new ArrayList<>();

        for (int i = 0; i < arr.size(); i++) {

            JSONObject population = (JSONObject) arr.get(i);
            String favouritefoodvalue = (String) population.get("favourite_food");
            favouritefoodarr.add(favouritefoodvalue);

        }
        
        //Create hash map to record K: favourite food name, V: count of favourite food 
        Map<String, Integer> map = new HashMap<>();

        for(int i = 0; i < favouritefoodarr.size(); i++)
        {
            if(map.containsKey(favouritefoodarr.get(i)))
                map.put(favouritefoodarr.get(i), map.get(favouritefoodarr.get(i))+1);
            else
                map.put(favouritefoodarr.get(i), 1);
        }
        
        //Create Priority Queue for ranking items in hash map order by descending count # 
        PriorityQueue<Map.Entry<String,Integer>> maxHeap = new PriorityQueue<>(3, (a,b) -> 
            a.getValue()==b.getValue() ? a.getKey().compareTo(b.getKey()) : b.getValue()-a.getValue()); 
        
        for (Map.Entry<String,Integer> entry : map.entrySet() ) maxHeap.add(entry);
        
        String output = "Three favourite foods: ";
        
        //Output loop for mapping key, value from hash map to string
        for (int i = 0; i < 3; i++) {
            output = output + maxHeap.poll().getKey() + "(" + maxHeap.poll().getValue() + "), ";
        }

        return output.substring(0,output.length()-2);

    }

    //Question 3: How many people were born in each month of the year (uses the month of each person's respective timezone of birth)
    public static String BirthMonthCount(JSONArray arr) {

        ArrayList<Integer> birthdayarr = new ArrayList<>(); 

        //Create Date formatter for capturing month part
        SimpleDateFormat monthformatstr = new SimpleDateFormat("MMMM");
        SimpleDateFormat monthformatint = new SimpleDateFormat("MM");
        
        for (int i = 0; i < arr.size(); i++) {

            //Get birthday and timezone from data
            JSONObject population = (JSONObject) arr.get(i);
            Long birthdayvalue_long = (Long) Long.parseLong((String) population.get("birth_timestamp"));
            TimeZone timezone = TimeZone.getTimeZone((String) population.get("birth_timezone"));

            //Format birthday to local time
            monthformatstr.setTimeZone(timezone);
            int Zonedtime = (int) Integer.parseInt(monthformatint.format(birthdayvalue_long));
            birthdayarr.add(Zonedtime);

        }

        //Create hash map to record K: months of birthday, V: count of month 
        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < birthdayarr.size(); i++) {

            if(map.containsKey(birthdayarr.get(i)))
                map.put(birthdayarr.get(i), map.get(birthdayarr.get(i))+1);
            else
                map.put(birthdayarr.get(i), 1);

        } 
        
        String output = "Birth Months: ";

        for (int i = 1; i < map.size(); i++) {

            String month_str = new DateFormatSymbols().getMonths()[i-1];
            output = output + month_str + "(" + map.get(i) + "), ";
        }

        return output.substring(0,output.length()-2);
    }

    public static void main(String args[]) throws Exception {

        if (args.length > 1) {
            System.out.println("We currently only expect 1 argument! A path to a JSON or CSV file to read.");
            System.exit(1);
        }

        if (args.length == 0) {
            System.out.println("Path to a valid file is not provided.");
            System.exit(1);
        }

        
        String filepath = args[0];   
        
        //Call ReadFile function to output full dataset
        JSONArray arr = ReadFile(filepath);

        //Print outputs
        System.out.println(AverageSiblings(arr));
        System.out.println(TopFavouriteFood(arr));
        System.out.println(BirthMonthCount(arr));

    }
    
}
