### Add PDF Download Feature to PdfScreen

#### Description
This PR introduces a new feature in the `PdfScreen` interface, allowing users to download PDF files and save them to the public directory. The specific implementation includes the following:

1. **PdfDownloadViewModel**: Responsible for handling the download and save operations of PDF files.
    - Provides the `savePdfToPublicDirectory` method to save PDF files to the public directory.
    - Uses `StateFlow` to manage permission status and save result status.
2. **PdfScreen**: UI interface, with the added functionality to download PDF files.
    - Contains a button that starts downloading the PDF file when clicked.
    - Uses Snackbar to display the save result (success or failure).
    - Handles permission requests to ensure the necessary permissions are granted before saving the file.

#### Main Changes
- Added the `PdfDownloadViewModel` class to handle the logic of downloading and saving PDFs.
- Added the functionality to download and save PDF files in the `PdfScreen` Composable function.

#### Testing
- Tested the download and save functionality of PDF files on devices with Android 13 and above.
- Tested the download and save functionality of PDF files on devices with Android versions below 13.
- Tested the permission request and handling logic to ensure permissions are requested when not granted and operations continue after permissions are granted.
<<<<<<< HEAD

---

Please review the code before merging to ensure all functions are working correctly. If there are any questions or suggestions, please feel free to contact me. Thank you!
=======
>>>>>>> c94f7d9b1d5aec29e75cbf1a4cf0f572445e6d0a
