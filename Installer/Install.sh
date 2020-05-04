#!/bin/bash

echo "This is the installer for the XData grading tool"
echo "================================================"
script_dir=$(dirname $0)

defaultDbServer="localhost"
defaultDbPort="5432"
defaultDbName="xdata"
defaultDbUser="xdataadmin"
defaultDbPassword="xdataadmin"
defaultSmtSolver="cvc3"
defaultAdmin="admin"
defaultAdminPassword="admin"
defaultSysUser="www-data"
defaultTempPath="/tmp"
defaultConsumerAuthKey="moodle.key"
defaultSecretKey="secret#xdata"
defaultCallBackURL="localhost/moodle"
defaultXdataUrlFromLMS="http://localhost:8080/XDataWeb/tool.jsp"
defaultLogFilePath="/tmp/logs/XData.log"
defaultdbSuperUser="pgadmin"
defaultdbSuperUserPassword="xdata"

while : ; do
	echo -n "Path of tomcat directory: "
	read tomcatDir
	if [[ ( "$tomcatDir" != "" ) && ( -d "$tomcatDir/bin" ) && ( -d "$tomcatDir/webapps" ) ]] 
	then
		break
	fi
	echo "$tomcatDir is not a valid tomcat directory"
done


#while : ; do
#	echo -n "URL of installation: "
#	read url
#	if [ "$url" != "" ] 
#	then
#		break
#	fi
#	echo "Enter a URL"
#done 




#echo -n "Database server name [default: $defaultDbServer]: "
#read dbServer
#if [[ "$dbServer" == "" ]] 
#then
	dbServer=$defaultDbServer
#fi

echo -n "Database port [default: $defaultDbPort]: "
read dbPort
if [[ "$dbPort" == "" ]]
then
	dbPort=$defaultDbPort
fi

if [[ ("$dbServer" != "localhost" ) && ("$dbServer" != "127.0.0.1" ) ]]
then
	echo -n "Database super user name(required to create databases and users): "
	read dbSuperUser
	
	echo -n "Database super user password: "
	read dbSuperUserPassword
	
	export PGPASSWORD='$dbSuperUserPassword'
fi

if [[ "$dbSuperUser" == "" ]]
then
	dbSuperUser=$defaultdbSuperUser
	dbSuperUserPassword=$defaultdbSuperUserPassword
	
fi

echo -n "Database name (this database will be created) [default: $defaultDbName]: "
read dbName
if [[ "$dbName" == "" ]] 
then
	dbName=$defaultDbName
fi

echo -n "Database user (this database user will be created) [default: $defaultDbUser]: "
read dbUser
if [[ "$dbUser" == "" ]]
then
	dbUser=$defaultDbUser
fi

echo -n "Database password (password for the database user that is created) [default: $defaultDbPassword]: "
read dbPassword
if [[ "$dbPassword" == "" ]] 
then
	dbPassword=$defaultDbPassword
fi

#echo -n "SMT Solver Command line including path [default: $defaultSmtSolver]: "
#read smtSolver

echo -n "Path for temporary files [default: $defaultTempPath]: "
read tempPath
if [[ "$tempPath" == "" ]]
then
	tempPath=$defaultTempPath
fi

#echo -n "Admin username [default: $defaultAdmin]: "
#read admin
#if [[ "$admin" == "" ]]
#then
	admin=$defaultAdmin
#fi



echo -n "Admin password [default: $defaultAdminPassword]: "
read adminPassword
if [[ "$adminPassword" == "" ]] 
then
	adminPassword=$defaultAdminPassword
fi

echo -n "Path to store log files [default: $defaultLogFilePath]:"
read logFilePath
if [[ "$logFilePath" == "" ]]
then
	logFilePath=$defaultLogFilePath
fi

#adminPasswordHash=`echo -n $adminPassword | md5sum | cut -f1 -d ' '`


echo -n "System/Tomcat user [default: $defaultSysUser]: "
read systemUser
if [[ "$systemUser" == "" ]] 
then
	systemUser=$defaultSysUser
fi


while : ; do

	echo -n "Want to configure for LMS [y/n]: "
	read lms
	if [[ ( "$lms" == "y" ) || ( "$lms" == "Y" ) || ( "$lms" == "n" ) || ( "$lms" == "n" ) ]] 
	then
		break
	fi
	echo "Please enter [y/n]"
done


if [[ ( "$lms" == "y" ) || ( "$lms" == "Y" ) ]] 
then

	echo -n "Moodle authentication key [default: $defaultConsumerAuthKey]: "
	read consumerAuthKey
	if [ "$consumerAuthKey" != "" ] 
	then
		consumerAuthKey=$defaultConsumerAuthKey
	fi

	echo -n "Moodle Secret Key [default: $defaultSecretKey]: "
	read secretKey
	if [ "$secretKey" != "" ] 
	then
		secretKey=$defaultSecretKey
	fi

echo -n "Proxy URL to access XData from moodle Ex:www.abc.com/XDataWeb/tool.jsp [default:
$defaultXdataUrlFromLMS]: "
	read xdataUrlFromLMS
	if [ "$xdataUrlFromLMS" == "" ] 
	then
		xdataUrlFromLMS=$defaultXdataUrlFromLMS
	fi

echo -n "Callback URL of moodle for uploading marks (set this to the moodle URL) [default: 		$defaultCallBackURL]: "
	read callBackURL
	if [ "$callBackURL" == "" ] 
	then
		callBackURL=$defaultCallBackURL
	fi

else 
   xdataUrlFromLMS=$defaultXdataUrlFromLMS
   callBackURL=$defaultCallBackURL
fi

echo "Deploying XData........."

cp $script_dir/XDataWeb.war "$tomcatDir/webapps/"
chown $systemUser "$tomcatDir/webapps/XDataWeb.war"
chmod 700 "$tomcatDir/webapps/XDataWeb.war"

cp $script_dir/cvc3/cvc3  /usr/local/bin/
chmod 555 /usr/local/bin/cvc3

sudo -u $systemUser "$tomcatDir/bin/shutdown.sh"  > /dev/null 2>&1
sleep 2
sudo -u $systemUser "$tomcatDir/bin/startup.sh"  > /dev/null 2>&1
sleep 5

sudo -u $systemUser echo "
#name of the database to be used; created by the default postgresql script, change if you 
# want to create your own database name
databaseName=$dbName

#database user name- 
existingDatabaseUser=$dbUser
#password
existingDatabaseUserPasswd=$dbPassword
#admin password
adminPassword=$adminPassword

# Address of database server; update this if your database is not listening on localhost port 5432
databaseIP=$dbServer
databasePort=$dbPort

#Path to SMT Solver; update it to the location where you will be installing the cvc3 executable
smtsolver=/usr/local/bin/cvc3

#Home directory; for temporary files Leave these as /tmp preferably
homeDir=$tempPath
#directory of scripts; created temporarily for application testing
scriptsDir=$tempPath
#directory for xml files created temporariliy for application testing 
dataDir=$tempPath
#Log files configuration
logFile=$logFilePath
logLevel=ALL
#Following properties are needed only if you are integrating with a learning management
# system such as moodle, blackboard or webct
#Consumer Auth Key for LTI integration; 
consumerAuthKey=$consumerAuthKey
#Secret key for LTI Integration
secretKey=$secretKey
#Call back URL of the LTI system to upload marks
callBackURL=$callBackURL
#URL for accessing XDataWeb from moodle;required for authenticating LMS request
XDataUrlFromLMS=$xdataUrlFromLMS

" > "$tomcatDir/webapps/XDataWeb/XData.properties"


sudo -u $systemUser "$tomcatDir/bin/shutdown.sh"  > /dev/null 2>&1
sleep 2
sudo -u $systemUser "$tomcatDir/bin/startup.sh"  > /dev/null 2>&1

createDB() {

	
	if [[ ("$dbServer" == "localhost" ) || ("$dbServer" == "127.0.0.1" ) ]]
	then	
		sudo -su postgres psql -p $dbPort  -c "$1"  > /dev/null 2>&1
		echo "$1"
		if [ $? -ne 0 ] 
		then
			echo "Some error occurred"
			echo "Ensure that PostgreSQL is installed and running"
			sudo -su postgres psql -p $dbPort -c drop database $dbName
			exit
		fi
	else 
		psql -h $dbServer -p $dbPort -U $dbSuperUser -d postgres -c "$1"  > /dev/null 2>&1
		if [ $? -ne 0 ] 
		then
			echo "Some error occoured"
			echo "Ensure that PostgreSQL is installed and running"
			-h $dbServer -p $dbPort -U $dbSuperUser -d postgres -c drop database $dbName
			exit
		fi		
	fi

}

runSQL() {	
	if [[ ("$dbServer" == "localhost" ) || ("$dbServer" == "127.0.0.1" ) ]]
	then
		sudo -u postgres psql -p $dbPort -d $dbName  -c "$1" > /dev/null 2>&1
		
		if [ $? -ne 0 ] 
		then
			echo "Some error occured"
			echo "Ensure that PostgreSQL is installed and running"
			sudo -su postgres psql -p $dbPort -c drop database $dbName
			sudo -su postgres psql -p $dbPort -c drop user $dbUser
			sudo -su postgres psql -p $dbPort -c drop SCHEMA $dbUser
			exit
		fi
	else 
		psql -h $dbServer -p $dbPort -U $dbSuperUser -d $dbName -c "$1"  > /dev/null 2>&1
		if [ $? -ne 0 ] 
		then
			echo "Some error occoured"
			echo "Ensure that PostgreSQL is installed and running"
			psql -h $dbServer -p $dbPort -U $dbSuperUser -d $dbName -c drop database $dbName
			psql -h $dbServer -p $dbPort -U $dbSuperUser -d $dbName -c  drop user $dbUser
			psql -h $dbServer -p $dbPort -U $dbSuperUser -d $dbName -c  drop schema $dbUser
			exit
		fi
	fi	
}


echo "Creating database for XData........."

createDB "create database $dbName"
createDB "create user $dbUser with password '$dbPassword'"
createDB "CREATE SCHEMA $dbUser"
createDB "ALTER SCHEMA $dbUser OWNER TO $dbUser"
createDB "grant all on schema $dbUser to $dbUser"

echo "The XData grading system has been installed."
