#FreezerChill (file name is Fridge.java)

This project is for anyone who needs a pick me up during a stressful day, it sends them positivity while also helping their stress by providing study hacks.

This project is made for the Snowglobe hackathon held in San Francisco 2026! Theme: FREEZE
It is a mindfulness game that gives study tips and positive messages to help people out of a slump! It includes a positive message generator as well as study hacks made in a fun manner!

**live demo:** https://github.com/ndmickma/Fridge/releases/tag/v1.0.0
**built by:** Sanvitti Shah (@ndmickma)

Screenshot of Project:
<img width="1212" height="726" alt="image" src="https://github.com/user-attachments/assets/c74b1930-103a-482d-9d4a-7b1a022fce36" />

## how do you use it?
-If you're ever struggling to manage all your thoughts, play this game and just follow the instructions on the screen!
-You open the freezer and click on one of the foods to go to its respective page. On that page is a mindfulness or study helper.

## how I built it
I used Java (Java Swing API) to make the different panels and graphics and GUI.

## run it yourself
1. Download jar file and also the music file (make sure they are in one folder)
2. Type this command in terminal:
java -jar FreezerChill.jar
3. Play the game!

Key Features:
-Animated freezer door opening and closing
-Animated snow falling on home page

Red Popsicle Page
-Positive messages when the user clicks the message button

Neapolitan Study Habits Page
-Ice cream flavors matched with study habits
-When user clicks generate habit a study habit is shown
-When user clicks start/pause study a 25 minute study timer started (because 25 minute work periods is best for focus)

What I Learned:
-Handling multiple panels 
-Animation loops 
-Using Java Sound API to integrate music

What I Struggled With:
-I found that my original mp3 audio file did not work with the Java sound system, to fix this
I converted my audio file to a wav file which is compatible with the Java sound system
(AUDIO CREDITS TO PIXABAY)
-In the short time of the hackathon it was at times hard to figure out the actual, exact pixel bounds for my shapes
but I eventually found dimensions I was happy with!

## what's next
-I want to add more "foods" in the freezer that lead to more mindfulness/study activities.
