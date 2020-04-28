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

    /*
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

        /*
        * This is the reader function for us to ingest data from gzip, csv or json. 
        * For gzip: decompress gzip file and write a new file to a local directory.
        * For json: read data into json array using json parser.
        * For csv: read data into hashmap, and then write hashmap into json array.
        *
        * @param: filepath : String. Path to gzip, csv or json file to read.
        * @return: data: JSONArray. Full set of data in jsonarray.
        */

        //Get filename, filetype for file reading.
        String[] fs_delimeter = filepath.split("\\\\");
        String[] dot_delimeter = filepath.split("\\.");
        String filename = fs_delimeter[fs_delimeter.length-1];
        String filetype = dot_delimeter[dot_delimeter.length-1];
        JSONArray data = new JSONArray();  
        
        try {            

            // Read zip file.
            if (filetype.equals("gz")) {

                //Read current file path
                File file = new File(filepath.substring(0, filepath.lastIndexOf(File.separator)) + "/gizoutput");

                //Create dirctory to store output file from gz.
                if (!file.exists()) {
                    file.mkdir();
                }

                String gzipFile = filepath;
                String newFile = filepath.substring(0, filepath.lastIndexOf(File.separator)) + File.separator + "gizoutput" + File.separator + filename.replace("gz", "");

                FileInputStream file_input_stream = new FileInputStream(gzipFile); 
                GZIPInputStream gzip_intput_stream = new GZIPInputStream(file_input_stream);
                FileOutputStream file_output_stream = new FileOutputStream(newFile);
                byte[] buffer = new byte[1024];
                int len;

                while((len = gzip_intput_stream.read(buffer)) != -1){   //Read from gz.
                    file_output_stream.write(buffer, 0, len);           //Write to regular file.
                }

                //close resources.
                file_output_stream.close();
                gzip_intput_stream.close();

                //Pass new filepath and filetype for csv or json file reader.
                filepath = newFile; 
                filetype = filepath.split("\\.")[1];
            }

            //Create Buffer Reader for file reading.
            BufferedReader br = new BufferedReader(new FileReader(filepath));

            //Read json file.
            if (filetype.equals("json")) {

                //JSON parser object to parse read file.
                JSONParser jsonParser = new JSONParser();

                Object obj = jsonParser.parse(br);
                data = (JSONArray) obj;     
            }

            //Read csv file.
            else if (filetype.equals("csv")) {

                HashMap<String,String> map = new HashMap<String, String>();
                String line = null;
                int count = 0;
                String[] header = {};

                while ((line = br.readLine()) != null) {

                    //Read csv schema
                    if (count == 0) {
                    header = line.split(",");
                    }
                    
                    else {
                        String[] arr = line.split(",");   // use comma as separator.
                        for(int i = 0; i < arr.length; i++) {   
                            map.put(header[i], arr[i].toString());   //map each item to their column.
                        }
                    }

                    JSONObject obj = new JSONObject(map);  //convert hashmap to json object, one json object = one row in csv.
                    data.add(obj);    //append each json object to the full data set (json array).
                    count = count + 1;
                }
                data.remove(0);  //remove first item in the data set - it's empty becuase of the header.
            }
            br.close();
        }

        // Catch exception 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        catch (ParseException e) {
            e.printStackTrace();
        }

    return data;   
    }

    public static String AverageSiblings(JSONArray data) {
        /*
        * This is the funuction to calculate the average number of siblings.
        * @param: data : JSONArray. Get full data from reader function.
        * @return: output: String. Answer to qesution 1.
        */

        int sum = 0;
        int n = 0;

        //Loop array.
        for (int i = 0; i < data.size(); i++) {

            JSONObject population = (JSONObject) data.get(i);
            byte siblingsvalue = (byte) Integer.parseInt((String) population.get("siblings"));
            sum = sum + siblingsvalue;
            n = n + 1;
        }

        //Calculate Average. 
        byte average =  (byte) Math.ceil(sum/n);

        return "Average siblings: " + average;
    }

    public static String TopFavouriteFood(JSONArray data) {
        /*
        * This is the funuction to calculate the top 3 favourite foods and the number of people who like them.
        * @param: data : JSONArray. Get full data from reader function.
        * @return: output: String. Answer to question 2.
        */
        
        //Create hash map to capture K: name of favourite food, V: count of favourite food. 
        Map<String, Integer> favourite_food_map = new HashMap<>();

        for (int i = 0; i < data.size(); i++) {

            JSONObject population = (JSONObject) data.get(i);
            String favourite_food_value = (String) population.get("favourite_food");

            if(favourite_food_map.containsKey(favourite_food_value))
            favourite_food_map.put(favourite_food_value, favourite_food_map.get(favourite_food_value)+1);
            else
            favourite_food_map.put(favourite_food_value, 1);
        }
      
        //Create Priority Queue for ranking items in hash map order by descending count number.
        PriorityQueue<Map.Entry<String,Integer>> maxHeap = new PriorityQueue<>(3, (a,b) -> 
            a.getValue()==b.getValue() ? a.getKey().compareTo(b.getKey()) : b.getValue()-a.getValue()); 
        
        for (Map.Entry<String,Integer> entry : favourite_food_map.entrySet() ) maxHeap.add(entry);
        
        String output = "Three favourite foods: ";
        
        //Output loop for mapping key, value from hash map to string.
        for (int i = 0; i < 3; i++) {
            output = output + maxHeap.poll().getKey() + "(" + maxHeap.poll().getValue() + "), ";
        }

        return output.substring(0,output.length()-2);
    }

    public static String BirthMonthCount(JSONArray data) {
        /*
        * This is the funuction to calculate the numbers of people who were born in each month of the year with their respective timezone of birth.
        * @param: data : JSONArray. Get full data from reader function.
        * @return: output: String. Answer to question 3.
        */

        //Create hash map to record K: months of birthday, V: count of month.
        Map<Integer, Integer> birthday_map = new HashMap<>();

        //Create Date formatter for capturing month part
        SimpleDateFormat month_format_str = new SimpleDateFormat("MMMM");
        SimpleDateFormat month_format_int = new SimpleDateFormat("MM");
        
        for (int i = 0; i < data.size(); i++) {

            //Get birthday and timezone from data.
            JSONObject population = (JSONObject) data.get(i);
            Long birthday_value = (Long) Long.parseLong((String) population.get("birth_timestamp"));
            TimeZone timezone = TimeZone.getTimeZone((String) population.get("birth_timezone"));

            //Format birthday to local time.
            month_format_str.setTimeZone(timezone);
            int Zonedtime = (int) Integer.parseInt(month_format_int.format(birthday_value));

            if(birthday_map.containsKey(Zonedtime))
                birthday_map.put(Zonedtime, birthday_map.get(Zonedtime)+1);
            else
                birthday_map.put(Zonedtime, 1);
        } 

        //Fill in months which has no one's birthday on.
        for (int i = 1; i <= 12; i++) {

            if (!birthday_map.containsKey(i))
                birthday_map.put(i, 0);
        }
          
        String output = "Birth Months: ";

        for (int i = 1; i < birthday_map.size() + 1; i++) {

            String month_str = new DateFormatSymbols().getMonths()[i-1];
            output = output + month_str + "(" + birthday_map.get(i) + "), ";
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

        //Call ReadFile to output full dataset.
        JSONArray data = ReadFile(filepath);

        //Print outputs
        System.out.println(AverageSiblings(data));
        System.out.println(TopFavouriteFood(data));
        System.out.println(BirthMonthCount(data));
    }   
}
