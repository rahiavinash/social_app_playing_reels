# Project Name

social playing reels app like instagram (wedo)

Join our purpose-driven social media application, WeDo 80, where users come together to create and share videos and photos. Our mission is to make a positive impact on the world by allocating 80% of our profits towards fighting climate change and poverty.
Additionally, 10% of our profits go towards rewarding content creators and our valuable app users through exciting competitions and prizes for the most liked content and creators each month. This commitment is deeply embedded in our company constitution.
Your app usage is rewarded with SRC (Socially Responsible Coin). 1 SRC is made up of 100,000 AR (Armen). Accumulate up to 5000 AR every day. Supply of SRC is limited to 1 Billion. Get it before it’s depleted.
By simply being a part of WeDo 80 and using our app, you are actively contributing to a better world for everyone. Join us on this remarkable journey towards a more sustainable and equitable future for all. Together, we will make a difference.

## Installation

* Android Studio: Ensure you have Android Studio installed on your system. You can download it here.
  Installation Steps:
  Clone the Repository:

* Clone the project repository using Git: git clone <repository_url>
* Open in Android Studio:

* Launch Android Studio.
  Click on "Open an existing Android Studio project."
  Navigate to the location where you cloned the repository and select the app directory.

* SDK and Gradle Configuration:Android Studio might prompt you to install missing SDK components or update Gradle. Follow the on-screen instructions to complete the setup.

* Run with Different Environments:
* Dev Environment:
  To run the app with the dev environment, use the following command in the terminal: ./gradlew installDebug -Penv=dev

* Test Environment:
  For the test environment, execute: ./gradlew installDebug -Penv=test

* Production Environment:
  To run with the prod environment, use: ./gradlew installDebug -Penv=prod

* Default it will run on Dev Environment
* Replace installDebug with other Gradle tasks as needed (e.g., assembleDebug, installRelease, etc.).


## Usage



### Features

Most liked accounts and original content win prizes every month
Likes based rankings: Drive your fame with likes not followers
Endless customisation with AI
Stay private and still post publicly
Plant Trees
Your time valued: Earn SRC for app usage
Premium: Verified badge, No Ads, Artificial Intelligence
Your own Algorithms: Ask AI to show you what you want.

## Contributing

Guidelines for contributors

## License
By downloading or using the app, you agree to the
LICENSED APPLICATION END USER LICENSE AGREEMENT
https://www.apple.com/legal/internet-services/itunes/dev/stdeula/
Privacy Policy: https://wedo80.com/privacy-policy-2/
Terms of Use: https://wedo80.com/terms-conditions/
Community Guidelines: https://wedo80.com/community-guidelines/


## 12-Dec-2023
## Classes

## Class 1 : [EditProfileFragment]
* Purpose : class manages the user's profile information and privacy settings within our application.
  Here user Edit their Profile image ,Privacy and update their other information.


* Key Methods/Functions.
* setUpUi :- method is responsible for initializing and configuring the UI components in which Text Change Listener use for
  mention the other user in Bio section.
* setRecyclerViewAdapter:- RecyclerView used for displaying mention items in the UI.proper handling of user clicks on mention items within the RecyclerView.
  When a mention item is clicked in the RecyclerView, the onClick method is triggered.
* profileDetailObserver:- method observes changes in the user's profile and privacy data, updating the UI components
  it observe LiveData from the authViewModel .Sets different UI states based on the retrieved privacy settings, such as visibility of elements for different access levels (noOne, friends, followers, everyone)
  Important Notes:
1. Async Image Loading: Utilizes asynchronous image loading (loadImageAsync) for profile image display in background.
2. Handling Mentions: Handles mentions within the user's bio and maintains a map (mentionUserHash) for mentioned users.
3. Observing LiveData: Observes changes in LiveData to keep UI elements up-to-date with profile and privacy data changes.

* callCustomFunction;
  This method is used to display a custom dialog prompting the user for permission when the user has denied the permission twice..

* highlightTagsAndMentions:- method is responsible for highlighting hashtags and mentions within a text Editable.
1. Mention Highlighting :- Utilizes a regex pattern to detect and highlight mentions (@mention).

* convertStringFormatToSendApi(text: String):-method converts a given string containing mentions into a format .It  transformed into a specific format required for API

## 12-Dec-2023
## Class 2: [MainApplication]

* **Purpose**: The MainApplication class is an entry point for the application and extends the
  Application class. Its main purpose is to initialize essential components and perform
  global setup before the application starts.

* **Key Responsibilities**
1. [Initialization of common objects]
    - Socket IO object declared in this class so this can be use in whole app for socket listen or emit.
    - SimpleCache object defined and initialize here which will be used in whole app for data storage in cache.

2. [Context Access]: Explain how the MainApplication provides a global context instance, accessible throughout the application.

## Class 3: [HomeScreen2]
* **Purpose**: The HomeScreen2 class represents a fragment responsible for the primary UI and
  functionalities of the home screen. It handles socket communication for AR count,
  post handling.

* **Key Responsibilities**
1. [Socket Communication]: Details how the class interacts with sockets, emits and listens to
   AR count events.

* **Important Methods**
1. [setUpUi(view: View)]: Manages UI setup and interaction listeners.

2. [Socket Communication Methods]: Methods related to socket interaction (emitLogin() for login socket,
   setSocketListenON() for listening necessary listeners, startStopArCount(arg: String) for emitting
   message to start or stop the AR count.).

## Class 4: [CreatePostFragment]

* [Purpose] : This class manages the logic for creating post either by selecting photos or video. Also provide the
  functionality to select time period, post selection(for whom you want to post it, either friends, public etc),
  download option, repost option, tag list, description, comment option, see likes option etc.

* [Key methods/functions]

* [createPostInDB(data: CreatePostEntity)] : Saves a CreatePostEntity object into the local database.

* [deleteAll()] : Deletes all data stored in the local database.

* [createPost(createPost: CreatePost)]: Initiates the creation of a post without media attachments.

* [createPostWithMedia(mediaCreatePost: CreatePostWithMedia)] : Initiates the creation of a post with media attachments.

* [mutableBaseResponse] : LiveData to hold the response of creating posts without media.

* [mediaResponse] : LiveData to hold the response of creating posts with media attachments.

* [ViewModel Scope] : Uses viewModelScope from the androidx.lifecycle.viewModelScope.Launches coroutines with the IO
  dispatcher(Dispatchers.IO)to perform asynchronous tasks without blocking the UI thread. This ensures proper handling
  of network and database operations.

## Class 5: [CompressionUploadWorker]

* [Purpose] : The CompressionUploadWorker class is a worker in the Android app responsible for compressing and uploading media content to the server. It handles both video and image media uploads.

* [Features]

* [Video Compression] : Compresses selected videos using the VideoCompressor library to reduce file size before upload.

* [Image Upload] : Uploads selected images directly.

* [Media Conversion] : Converts media files into base64 format for API upload.


## Class 5: [MainActivity]

* [appUpdateManager] :  to check for the availability of updates using the Play Core Library
    The returned AppUpdateInfo instance contains the update availability status. Depending on the status of the update, the instance also contains the following:
         1. If an update is available and the update is allowed, the instance also contains an intent to start the update.
         2. If an in-app update is already in progress, the instance also reports the status of the in-progress update.**
* [override fun onActivityResult] : to handle the result of the update flow in onActivityResult
