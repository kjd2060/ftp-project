# ftp-project
Java implementation of a FTP server (FS) and client (FC) using default socket libraries.

FS run on host, listens for requests from clients (FC).  Implements the following 
commands:

    1. rftp <FTP Server Address> <User> <Password>:  This command takes three parameters, i.e., the IP address of 
       the server where FS is running, a user name and a password. If the provided user name and password are 
       correct, then access to the FS is allowed otherwise an error message is returned.
    
    2. rget  <File  Name>:    The  rget command  takes  one  parameter  that  is  the  name  of  the  file  to 
       download from FS. If the file exists,  then it is downloaded from the server to the default directory of 
       FC, otherwise an error message is returned.
    
    3. rput <File Name>: The rput command takes the name of the file to upload to the FTP server, and uploads 
       the file if it exists in the default directory of FC, otherwise informs the user that the file does not exist
There's a few issues noted in my previous commit, namely forgetting to close files in certain areas and some issues with the cd command.  Also some extra folders exist in the repository because when I uploaded I forgot to add those to the .gitignore and they were a part of the submission.  BADLY needs commenting.  Note that these issues exist due to deadlines doubling up with this and senior projects, so this had to be done in a non-optimal way in some areas.
