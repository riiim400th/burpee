# Burpee - Burp Suite Extension

## 👀Overview
 **Burpee** is a Burp Suite extension that organizes HTTP requests and exports them to Excel. 

![head_image](https://github.com/user-attachments/assets/82dbe607-3847-4d04-b720-6867a33e10f8)

**Introduction movie**

[![image](https://github.com/user-attachments/assets/77d1bf6a-dbca-4dae-a955-72b9a85e641d)](https://youtu.be/no15BP_kVHA)

## ✅Features
- **Copy as TSV Format:** Formats HTTP data in TSV to clipboard, ideal for pasting into Excel. 
- **Write to Excel:** Automatically generates sheets for request URLs and parameters. 

## ⬇️Installation
1. **Download Jar:** Get the latest version from the [release page](https://github.com/riiim400th/burpee/releases). 
2. **Install in Burp Suite:**
   - Go to the "Extender" tab, then "Extensions".
   - Click "Add" and select the downloaded jar. 🔧

## Usage (Excel output)
1. ⚙️**Settings:** Select the Excel file in the settings tab. 

   ![image](https://github.com/user-attachments/assets/c333673c-69cb-4de8-bd64-6b737cd11ce6)

2. 📤**Select and Output:**
   1. Select requests and open the menu. 
   2. Click "Extensions > Burpee".
   3. The analysis is performed and output to Excel file. 

   ![image](https://github.com/user-attachments/assets/c646da1f-ec44-47b5-b176-c20bdf925b9a)

4. 🗂️**Open the file:** Individual sheets for each HTTP request and a summary sheet are created. 
   
     These values are decoded, making them easier to read without having to use Burp's decoder !

   **Requests Summary Sheet**

   ![image](https://github.com/user-attachments/assets/100c20ae-7c7d-452a-a1a6-d3a6c023e321)

   **Requests Detail Sheet**

   ![image](https://github.com/user-attachments/assets/6c073c33-70ad-4003-9b5a-483ea96ac048)
