# ClockIT
ClockIT is an advanced time management and tracking system designed to help users efficiently manage their tasks and time. This README file provides an overview of the features offered by ClockIT, making it accessible to both technical and non-technical stakeholders.

### Amendments

Added:

-pages for easy access for logs so that the user has the ability to view the logs at first glance 

Removed:

-the awards and challenges features as we already had 2 features added



### FeedBack from lecture -

Remember to implement your lecturer’s feedback on the prototype before adding the new features. Pay special attention to the feedback on the user interface. Marks will be awarded for that again in this part, so you now have the opportunity to improve on what you did before.


*Comment*

The user can view list of entries in a period however it would have been better if the user can view those entries at first glance

*Change*

There now is a button on the homepage where a user can click on it and view all the logs for each activity and these logs will display all the data including hours done and the images for the logs

*Comment*

User can set minimum and maximum daily goals

Goals-did not get full marks
(no comment though)

*Change*

added a button to set the goals as one instea of clicking ste min and set max goals separately. the user is now able to view the goals before actually putting them in firebase

*Comment*

Excellent user interface

UI- did not get full marks
(no comment though)

*Change*
Added more consistant use of layout, fonts, and colours. Added more user friendly features such as add goals,logs,activities and categorys. Made some buttons different colours so it is easy to distinguish what does what.

## Features
### Leaderboard (feature 1)
The LeaderBoard feature in the app allows users to see their ranking based on the points they have accumulated from logging activities. Here's how it works:

1. *Data Retrieval*: When you open the leaderboard, the app connects to a Firebase Realtime Database to fetch all logged activity sessions.

2. *Points Calculation*: For each logged session, the app calculates points based on the duration of the activity. Each minute of logged activity counts as one point.

3. *User and Bot Points*: The app distinguishes between real users and predefined bot users. Bot users have fixed points for illustration purposes, making the leaderboard more competitive.

4. *Leaderboard Display*: 
    - The app calculates the total points for the user and combines this with the points of bot users.
    - It then sorts all users by their total points in descending order.
    - The top three users are highlighted at the top of the leaderboard with special backgrounds, prominently showing their names and points.
    - The rest of the users are listed below, showing their rank, names, and points.

5. *Updating the Leaderboard*: The leaderboard dynamically updates each time you open it, reflecting the most recent data from the database. This ensures that your latest activities and points are always included.

Logging your study hours and activities allows you to accumulate points and see your name rise on the leaderboard. The top spots are highlighted, motivating users to engage more and earn more points.

### Colors for categories (feature 2)
Assigning distinct colours to categories enables users to quickly identify and differentiate between various tasks or projects within the app interface. This feature enhances usability and organization, making it easier for users to navigate their time-tracking data and interpret visual cues briefly. By associating specific colours with each category, users can customize their workflow and tailor the app's appearance to suit their preferences and visual preferences.

The color will show up anywhere the category name is displayed (viewing logs, adding goals, viewing goals, adding logs, etc.) and when the user is creating new activities. This allows the user to easily and quickly identify which category they are dealing with.

### Time Tracking
#### Automatic Time Logging: 
Automatically logs the time spent on different tasks based on user activity.
#### Manual Time Entry:
Allows users to enter the time spent on tasks manually.

### Task Management
#### Create Tasks:
Users can create new tasks with specific details such as description, priority, and deadlines.
#### Delete Tasks:
Users can remove tasks that are no longer needed.

### User Management
#### Activity Reports:
Generates detailed reports on user activities, including time spent on each task.
#### Productivity Analysis:
Provides insights into user productivity based on time-tracking data.

## Additional Information
If you need any more information or troubleshooting, please refer to the documentation provided within the project or contact the project maintainers.

## Link to YouTube video
You can find the video of our app running [here](https://www.youtube.com/watch?v=waPscszF9Eo).
