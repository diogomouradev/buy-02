cwd=$(pwd)
datapath="$cwd/data"
dbpath="$datapath/db"
logpath="$dbpath/log/"
logfile="$logpath/mongo.log"

if [ ! -d "$datapath" ]; then
	echo "creating db folder..."
	mkdir "$datapath"
fi
echo ""
sleep 1
if [ ! -d "$dbpath" ]; then
	echo "creating data folder..."
	mkdir "$dbpath"
fi
echo ""
sleep 1
if [ ! -d "$logpath" ]; then
	echo "creating log folder..."
	mkdir "$logpath"
fi
echo ""
sleep 1
echo "starting the database..."
mongod --dbpath "$dbpath" --logpath "$logfile" --fork &&
	echo ""
echo ""
sleep 2

echo "'starting spring boot...'"
mvn spring-boot:run
