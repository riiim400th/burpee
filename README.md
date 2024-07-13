# Burpee - Burp Suite Extension

## Overview
Burpee is a Burp Suite extension that extracts parameters to Excel and the Clipboard.

![head_image](https://github.com/user-attachments/assets/82dbe607-3847-4d04-b720-6867a33e10f8)

[![image](https://github.com/user-attachments/assets/77d1bf6a-dbca-4dae-a955-72b9a85e641d)](https://youtu.be/no15BP_kVHA)


## Features
- **Copy as TSV Format:** Formats HTTP data in TSV, ideal for pasting into Excel.
- **Write to Excel:** Automatically generates sheets for request URLs and parameters.

## Installation
1. **Download Jar:** Get the latest version from the [release page](https://github.com/riiim400th/burpee/releases).
2. **Install in Burp Suite:**
   - Go to the "Extender" tab, then "Extensions".
   - Click "Add" and select the downloaded jar.

## Usage
1. **Settings:** In the Burpee tab, configure outputs, scope (which part of the HTTP request to extract), ignore headers, etc.

   ![image](https://github.com/user-attachments/assets/669295e3-0706-492d-be52-48dcbf1cd09b)


2. **Parse and Copy:**
   - Select an HTTP request and open the menu.
   - Click "Extensions > Burpee".
   - The data is now in the clipboard and saved to a file.
  
   ![image](https://github.com/user-attachments/assets/047ec80c-9fc6-4114-a720-fd738af409d4)


3. **Paste into Excel:** Paste the copied data.

   ![image](https://github.com/user-attachments/assets/ab6e9f0e-0033-4954-a9ce-0a591a7af003)


4. **Check the file:** The file contains a summary of site visits, with lists of URLs and request details.

   **Requests Summary Sheet**

   ![image](https://github.com/user-attachments/assets/4cdec456-82e4-4797-ba10-cff76b472c1f)

   **Requests Detail Sheet**

   ![image](https://github.com/user-attachments/assets/c7f41d03-1355-4abe-ac90-1b31f1fef513)
