echo "What is the name of your input file: "  
read input_name  
echo "What is the desired name of your output file: "  
read output_name  

javac AccessControlAnalysis.java
java AccessControlAnalysis $input_name $output_name