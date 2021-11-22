import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.io.*;

//NetIds: PJA66 && NCL44
public class AccessControlAnalysis {

  //Data Structure representing a "Privilege Matrix"
  public static HashMap<String, HashMap<String, List<String>>> privMatrix = new HashMap<>();
  //Output File Variable
  public static List<String> outputFile = new ArrayList<>();

  //Keeps track of objects seen for "T" command validation
  public static Set<String> objectsSeen = new HashSet<>();

  //Keeps track of objects seen for "R" and "W" command validation
  public static Set<String> subjectsSeen = new HashSet<>();
  
  public static void main(String[] args) throws FileNotFoundException, IOException {
    //Input File
    String path = "test.txt";
    List<String> file = inputFile(path);

    //Cycle Through File
    for(int line = 0; line < file.size(); line++)
    {
      String stLine = file.get(line);
      List<String> instruction = concatenate(stLine);

      if(validInstruction(instruction))
      {  //Command Block
        String type = instruction.get(0);
        String subject = instruction.get(1);
        String object = instruction.get(2);
        String priv = instruction.get(3);

        if(type.equals("Add"))
          add(stLine, subject, object, priv);
        else
          query(stLine, object, priv);
      }
      else //Comment NOT a Command
        outputFile.add(stLine); 
    }
    
    //We will end up just keeping this
    outputFileGen(outputFile);

    //Just Pretty Print to Terminal
    System.out.println(" ");
    for(int i = 0; i < outputFile.size(); i++)
      System.out.println(outputFile.get(i));
  }

  //Outputs List generated from orginal txt where each list Cell is a line
  private static List<String> inputFile(String path) throws FileNotFoundException, IOException
  {
    File file = new File(path);
    BufferedReader br = new BufferedReader(new FileReader(file));
    List<String> fileLst= new ArrayList<>();

    String stLine;
    while ((stLine = br.readLine()) != null)
      fileLst.add(stLine);

    br.close();
    return fileLst;
  }

  //Outputs a list where each cell is a piece of the insturctions
  //ie. {Add, S1, O1, R}
  private static List<String> concatenate(String instuction) {
    String noSpace = instuction.replace(" ", "");
    List<String> info = Arrays.asList(noSpace.split(","));    
    return info;
  }

  //Checks to see if the given line is an "Command" or "Comment"
  private static boolean validInstruction(List<String> instruction) {
    int len = instruction.size();

    if(len != 4)
      return false;

    String type = instruction.get(0);
    String potSub = instruction.get(1);
    String potObj = instruction.get(2);
    String priv =  instruction.get(3);

    //Object/Subject Validation for "T"
    if(priv.equals("T") &&  (objectsSeen.contains(potSub) || objectsSeen.contains(potObj)))
        return false;

    //Object/Subject Validation for "R", "W", and "Query"
    if(priv.equals("W") || priv.equals("R") || type.equals("Query"))
    {
      if(objectsSeen.contains(potSub) || subjectsSeen.contains(potObj))
        return false;
    }

    if(!type.equals("Add") && !type.equals("Query"))
      return false;
    
    if(!priv.equals("R") && !priv.equals("W") && !priv.equals("T"))
      return false; 
  
    return true;
  }

  //Implementation of the "Add" command
  private static void add(String stLine, String subject, String object, String priv)
  {
    //Add to output File
    outputFile.add(stLine);

    if(priv.equals("T")) //For Privledge "T"
    {
      String subjectOne = subject;
      String subjectTwo = object;
      
      //Keep track of subjects seen for validation
      subjectsSeen.add(subjectOne);
      subjectsSeen.add(subjectTwo);

      if(privMatrix.containsKey(subjectTwo))
      {
        HashMap<String, List<String>> objectHashSubjectTwo = privMatrix.get(subjectTwo);
        for (String objForSub2 : objectHashSubjectTwo.keySet()) {
          if(privMatrix.containsKey(subjectOne))
          {
            HashMap<String, List<String>> objectHashSubjectOne = privMatrix.get(subjectOne);
            if(objectHashSubjectOne.containsKey(objForSub2))
            {
              List<String> privLstOne = objectHashSubjectOne.get(objForSub2);
              List<String> privLstTwo = objectHashSubjectTwo.get(objForSub2);
              for(int i = 0; i < privLstTwo.size(); i++)
              {
                String curPriv = privLstTwo.get(i);
                if(!privLstOne.contains(curPriv))
                  privLstOne.add(curPriv); 
              }
              objectHashSubjectOne.put(objForSub2, privLstOne);
            }
            else
            {
              List<String> privLstTwo = objectHashSubjectTwo.get(objForSub2);
              objectHashSubjectOne.put(objForSub2, privLstTwo);
            }
          }
          else
          {
            HashMap<String, List<String>> objectHash = new HashMap<>();
            List<String> privLstTwo = objectHashSubjectTwo.get(objForSub2);
            objectHash.put(objForSub2, privLstTwo);
            privMatrix.put(subjectOne, objectHash);
          }
        }
      }
    }
    else if(privMatrix.containsKey(subject)) //For Privledge "W" and "R"
    {
      objectsSeen.add(object); //Keep track of files seen for validation
      subjectsSeen.add(subject); //Keep track of subjects seen for validation

      HashMap<String, List<String>> objectHash = privMatrix.get(subject);
      if(objectHash.containsKey(object))
      {
        List<String> privLst = objectHash.get(object);

        //Priv not already in privMatrix for current Subject and Object
        if(!privLst.contains(priv))
        {
          privLst.add(priv);
          objectHash.put(object, privLst);
        }
      }
      else
      {
        List<String> privLst = new ArrayList<>(); 
        privLst.add(priv);
        objectHash.put(object, privLst);
      }
    }
    else
    {
      objectsSeen.add(object); //Keep track of files seen for validation
      subjectsSeen.add(subject); //Keep track of subjects seen for validation

      HashMap<String, List<String>> objectHash = new HashMap<>();
      List<String> privLst = new ArrayList<>();
      privLst.add(priv);
      objectHash.put(object, privLst);
      privMatrix.put(subject, objectHash);
    }
  }

  private static void query(String stLine, String object, String priv)
  {
    if(priv.equals("T"))
    {
      outputFile.add(stLine + " YES");
      return;
    }

    for (String sub1 : privMatrix.keySet())
    {
      HashMap<String, List<String>> objectHash = privMatrix.get(sub1);
      if(objectHash.containsKey(object))
      {
        List<String> privLst = objectHash.get(object);
        if(privLst.contains(priv))
        {
          outputFile.add(stLine + " YES");
          return;
        }
      }
    }
    outputFile.add(stLine + " NO");
  }

  private static void outputFileGen(List<String> outputFile) throws IOException{
    FileWriter writer = new FileWriter("output.txt"); 
    for(String str: outputFile) {
      writer.write(str + System.lineSeparator());
    }
    writer.close();
  }
}