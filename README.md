# Blocktrace User Application

An android application that allows Blocktrace customers to sign up and register for the service, allow companies to access their data, login to those companies, as well as report loss of their tokens should they lose it. 

## Features

### Register for Blocktrace
Customers can sign up with their email to create an account that allows them to login to the android application. On sign up, the user will be prompted to enter their identification details and await verification to obtain their physical tokens before they can proceed on to enjoy the services provided by Blocktrace. 

Should the user choose not to enter his/her personal identification information right after creating the account, this can be done the next time he/she opens the application. Otherwise, should the user's information be rejected by Blocktrace, he/she will also be prompted to re-enter his/her personal information again the next time he/she logs in to the application.

This information will be stored temporarily in the Firebase database for the Blocktrace administration to verify and generate the user's token and deliver it to the user for access to Blocktrace services.

### Allow company access to personal data
Upon verification of their data and obtaining their Blocktrace token, the user can now link their Blocktrace account to the companies they wish to sign up for to allow the respective companies to access their data. They will be prompted to input the ID of the company that they wish to sign up for, and a valid username and password will be all that is necessary to create a new account with the company they have opted to sign up for. To allow the company to access their data, the user scans his/her Blocktrace token as a proof of identity. After the verification success, the user will have to scan the token again to update their physical token.

### Login to companies
After an account with a company has been succesfully created, users can login to the company with the same username and password that they have registered for the company with and scanning their Blocktrace token. 

### Report loss of token
Should a user lose his/her token, he/she can report this so that the lost token will be rendered useless and potential malicious parties cannot use the existing tokens for their unlawful gains.

If the user discovers that token has not been lost after all, their existing token can be reactivated by contacting the Blocktrace administrative staff.

## Database
### Backend server
Communications with the backend server for linking new companies and logging in to companies is achieved mainly using HTTP requests

### Firebase 
* Firebase authentication is used for the user to login to the application using their email and password
* Firebase storage is used to store the user's uploaded image for verification
* Firebase database is used to store the user's information for verification
