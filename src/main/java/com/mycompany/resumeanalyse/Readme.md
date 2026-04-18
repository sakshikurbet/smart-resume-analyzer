# Smart Resume Analyzer (Java)

## Overview

The Smart Resume Analyzer is a Java-based application that evaluates resumes by matching candidate skills across multiple domains with predefined job criteria. It calculates a relevance score, stores candidate data in a database, and provides an efficient way to screen resumes.

##  Features

* Resume parsing and analysis
* Keyword and skill matching across multiple domains
* Resume scoring based on relevance
* Multi-domain skill evaluation
* Database integration to store candidate details
* Login system for secure access
* User-friendly GUI interface

##  Technologies Used
* Java
* NetBeans IDE
* MySQL (Database)
* JDBC (Database Connectivity)
  
##  How It Works
1. User logs into the system
2. Uploads or inputs a resume
3. System extracts skills and keywords
4. Matches them with job criteria across domains
5. Generates a matching score
6. Stores results in the database
7. Displays analysis to the user

## How to Run the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/smart-resume-analyzer.git
   ```
2. Open the project in NetBeans
3. Configure your MySQL database connection
4. Run the project

## Project Structure
```
src/
 └── main/java/com/mycompany/resumeanalyse/
     ├── DBConnection.java
     ├── LoginGUI.java
     ├── ResumeGUI.java
     ├── PDFReader.java
pom.xml
```
## Use Cases
* Resume screening for recruiters
* Candidate shortlisting
* Academic project demonstrating Java + Database integration

## Future Enhancements
* AI/NLP-based resume analysis
* Integration with job portals
* Web-based version
* Advanced ranking algorithms
  
## Author
Sakshi Kurbet
