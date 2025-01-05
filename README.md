# LeetCode Progress Tracker

This program allows you to track your progress on LeetCode problems and automatically updates a **Google Sheet** with the status of each problem. The program fetches the problem status from LeetCode and updates the Google Sheet accordingly.

**Important**: This program works specifically with the **LeetCode Progress Tracker Sheet created by Dinesh Varyani**. The sheet structure and format are predefined, so it will not work with any random Google Sheet. You need to copy the sheet created by Dinesh Varyani for this program to function properly.

## Requirements

1. **Java Development Kit (JDK)** - Make sure you have Java installed on your system. You can download it from [here](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).
2. **Google Developer Console Account** - You'll need to create a project in Google Developer Console and enable the Google Sheets API to interact with Google Sheets.
3. **LeetCode Account** - You need to provide your LeetCode username and session ID to track your progress.
4. **API Credentials** - You'll need to generate credentials for accessing the Google Sheets API.

## Setup Instructions

### Step 1: Copy the Google Sheet Template

You must first create a copy of the **LeetCode Progress Tracker Sheet** (Dinesh Varyani's sheet) to use with this tool.

1. Click the link below to open the Google Sheet template:
    - [LeetCode Progress Tracker Sheet](https://docs.google.com/spreadsheets/d/1pnI8HmSMPcfwrCCu7wYETCXaKDig4VucZDpcjVRuYrE/edit?gid=237636947#gid=237636947)

2. In Google Sheets, click on `File` → `Make a copy...` to create your own copy of the sheet. This sheet will be used to track your progress.

### Step 2: Set Up Google Developer Console

1. Go to the [Google Developer Console](https://console.developers.google.com/).
2. Create a new project.
3. Enable the **Google Sheets API** for the project.
4. Create **OAuth 2.0 credentials** and download the credentials JSON file. Rename the file to `google-sheets-client-secret.json` and place it in the `src/main/resources` folder of your project.

### Step 3: LeetCode API Setup

Since the program doesn't support direct login through the Java API, you'll need to manually get your **LeetCode session ID**.

1. Open your browser and go to [LeetCode](https://leetcode.com/).
2. Right-click on the page and select **Inspect** (Developer Tools).
3. Go to the **Application** tab.
4. In the **Cookies** section, find your **LEETCODE_SESSION**. Copy this session ID for use in the program.

### Step 4: Configure `application.properties` File

You need to configure the `application.properties` file with your Google Sheets API credentials, spreadsheet ID, LeetCode username, and session ID.

1. Open the `config.properties` file in your project directory.
2. Update the following properties:

```properties
# Google Sheet properties
credentials.file.path=/google-sheets-client-secret.json   # Path to the Google Sheets API credentials file
spreadsheet.id=your-sheet-id  # The ID of your Google Sheet (copy from the Sheet's URL)

# LeetCode properties
leetcode.username=your-leetcode-username  # Your LeetCode username
leetcode.sessionId=your-leetcode-session-id  # Your LeetCode session ID (from browser Developer Tools)
```

### Step 5: Run the Program

Once everything is set up, you can run the program to automatically fetch your LeetCode problem statuses and update the corresponding Google Sheet.

1. In your terminal or IDE, run the program.
2. The program will authenticate with the Google Sheets API and then fetch data from LeetCode using your session ID.
3. The status of each LeetCode problem will be updated in your Google Sheet.

## Example Usage

### Command Line

```bash
java -jar leetcode-tracker.jar
