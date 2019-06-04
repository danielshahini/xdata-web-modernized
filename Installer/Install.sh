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
createDB "SET statement_timeout = 0"
createDB "SET client_encoding = 'UTF8'"
createDB "SET standard_conforming_strings = on"
createDB "SET check_function_bodies = false"
createDB "SET client_min_messages = warning"
createDB "SET search_path = $dbUser, pg_catalog"
createDB "SET default_tablespace = ''"
createDB "SET default_with_oids = false"

runSQL "
SET search_path TO $dbUser;
CREATE TABLE xdata_assignment (
    course_id character varying(20),
    assignment_id integer,
    description text,
    starttime timestamp without time zone,
    endtime timestamp without time zone,
    learning_mode boolean DEFAULT false,
    connection_id integer,
    defaultschemaid integer,
    assignmentname character varying(20),
primary key(course_id,assignment_id)
);ALTER TABLE xdata_assignment OWNER TO $dbUser;
CREATE TABLE xdata_database_connection (
    course_id character varying NOT NULL,
    connection_id integer NOT NULL,
    connection_name character varying,
    database_type character varying DEFAULT 'PostgreSql'::character varying,
    jdbc_url character varying,
    database_user character varying DEFAULT 'testing1'::character varying,
    database_password character varying DEFAULT 'testing1'::character varying,
    test_user character varying,
    test_password character varying,
    database_name character varying(50),
    jdbcData varchar(100),
    PRIMARY KEY (course_id, connection_id)
);ALTER TABLE xdata_database_connection OWNER TO $dbUser;
CREATE SEQUENCE database_connection_connection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE database_connection_connection_id_seq OWNER TO $dbUser;
ALTER SEQUENCE database_connection_connection_id_seq OWNED BY xdata_database_connection.connection_id;
CREATE TABLE xdata_datasetvalue (
    queryid character varying,
    datasetid character varying,
    value character varying,
    tag character varying,
    assignment_id integer,
    question_id integer,
    query_id integer,
    course_id varchar(20),
primary key (course_id, assignment_id,question_id,query_id,datasetid)
);
ALTER TABLE xdata_datasetvalue OWNER TO $dbUser;
CREATE TABLE xdata_detectdataset (
    user_id character varying,
    queryid character varying(20),
    datasetid character varying(20),
    result text,
    assignment_id integer,
    question_id integer,
    course_id varchar(20)
);
ALTER TABLE xdata_detectdataset OWNER TO $dbUser;
CREATE TABLE xdata_student_queries (
    dbid text,
    queryid text NOT NULL,
    rollnum text NOT NULL,
    querystring text NOT NULL,
    tajudgement boolean,
    verifiedcorrect boolean,
    assignment_id integer,
    question_id integer,
    result text,
    course_id varchar(20),
    primary key(course_id,assignment_id,question_id,rollnum)
);
ALTER TABLE xdata_student_queries OWNER TO $dbUser;
CREATE TABLE xdata_instructor_query (
    assignment_id integer NOT NULL,
    question_id integer NOT NULL,
    sql text,
    status character varying(4),
    course_id character varying(20) NOT NULL,
    query_id integer NOT NULL,
    marks integer,
    PRIMARY KEY (course_id, assignment_id, question_id, query_id)
);
ALTER TABLE xdata_instructor_query OWNER TO $dbUser;
CREATE TABLE xdata_schemainfo (
    course_id character varying(20) NOT NULL,
    schema_id integer NOT NULL,
    schema_name character varying(20),
    ddltext text,
    sample_data text,
    sample_data_name character varying(50),
   primary key(course_id,schema_id)
);
ALTER TABLE xdata_schemainfo OWNER TO $dbUser;
CREATE TABLE xdata_qinfo (
    course_id character varying(20),
    assignment_id integer,
    question_id integer,
    querytext text,
    correctquery text,
    totalmarks integer,
    learningmode boolean,
    ignoreduplicates boolean,
    matchallqueries boolean,
    query_id integer,
    optionalschemaid integer,
    orderindependent boolean,
 primary key(course_id,assignment_id,question_id,query_id)
);ALTER TABLE xdata_qinfo OWNER TO $dbUser;
CREATE TABLE xdata_users (
    internal_user_id character varying(20) NOT NULL,
    user_name character varying(100),
    email character varying(100),
    sourceid text,
    password text,
    role character varying(30),
    course_id character varying(20),
    login_user_id  varchar(50),
   PRIMARY KEY (internal_user_id)
);
ALTER TABLE xdata_users OWNER TO $dbUser;
CREATE TABLE xdata_views (
    vname text NOT NULL,
    rollnum text NOT NULL,
    viewquery text NOT NULL,
PRIMARY KEY (vname, rollnum)
);
ALTER TABLE xdata_views OWNER TO $dbUser;
CREATE TABLE xdata_course (
    course_id integer,
    instructor_course_id character varying(30),
    course_name character varying(100),
    year numeric,
    semester character varying(50),
    description text
);
ALTER TABLE xdata_course OWNER TO $dbUser;
ALTER TABLE ONLY xdata_database_connection ALTER COLUMN connection_id SET DEFAULT nextval('database_connection_connection_id_seq'::regclass);
Create table xdata_roles (
	internal_user_id  varchar(25),
	login_user_id varchar(50),
	course_id varchar(20),
	role varchar(50)
	);
ALTER TABLE xdata_roles OWNER TO $dbUser;
create table xdata_LTIResponseInfo (course_id varchar(50), 
	internal_user_id varchar(20),
	rollnum varchar(50),
	assignment_id numeric, 
	sourceid text,
	primary key (assignment_id,course_id,internal_user_id)
);
ALTER TABLE xdata_LTIResponseInfo OWNER TO $dbUser;
alter table xdata_student_queries add score numeric;
alter table xdata_student_queries add max_marks numeric;
alter table xdata_student_queries add markinfo text;
create table xdata_sampledata(
	sampledata_id integer, 
	schema_id integer, 
	course_id varchar(20),
	sample_data_name text,
	sample_data text, 
	primary key(sampledata_id));
	
ALTER TABLE xdata_sampledata OWNER TO $dbUser;
alter table xdata_assignment add column defaultDSetId text;
alter table xdata_instructor_query add column default_sampledataid text;
create table xdata_lti_credentials(consumer_key varchar(100), 
				   secret_key varchar(100),
				   requesting_url text, 
				   primary key (requesting_url));
ALTER TABLE xdata_lti_credentials OWNER TO $dbUser;
alter table xdata_instructor_query add column partialmarkinfo text;
alter table xdata_instructor_query add column resultondataset text;
alter table xdata_instructor_query drop column status;
alter table xdata_instructor_query add column evaluationstatus boolean;
alter table xdata_lti_credentials add column lti_id integer;
alter table xdata_assignment add column evaluationstatus boolean;
alter table xdata_course add primary key(instructor_course_id);
alter table xdata_qinfo add column default_sampledataid text;
alter table xdata_qinfo add column equivalence_failed_datasets text;
alter table xdata_qinfo add column equivalenceStatus boolean;
alter table  xdata_datasetvalue add column isResultMatch boolean;
alter table xdata_student_queries add column late_submission_flag boolean;
alter table xdata_qinfo add column latesubmissionmarks int;
alter table  xdata_Student_queries add column isEvaluated boolean;
insert into xdata_users values('XD1','admin','admin@infolab','','$adminPassword','admin','CS631','admin');
insert into xdata_roles values('XD1','admin','CS631','admin');
insert into xdata_course values('1','CS631','DB course','2016','Spring','Default DB course');
"

echo "The XData grading system has been installed."
