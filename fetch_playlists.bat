set RAWPATH=src\main\res\raw

mkdir %RAWPATH%

curl.exe http://a1.phobos.apple.com/us/r1000/000/Features/atv/AutumnResources/videos/entries.json -o %RAWPATH%\tvos10.json

curl.exe http://sylvan.apple.com/Aerials/2x/entries.json -o %RAWPATH%\tvos11.json

curl.exe http://sylvan.apple.com/Aerials/resources.tar -o resources.tar
tar -xOf resources.tar entries.json > %RAWPATH%\tvos12.json
del resources.tar

curl.exe http://sylvan.apple.com/Aerials/resources-13.tar -o resources-13.tar
tar -xOf resources-13.tar entries.json > %RAWPATH%\tvos13.json
del resources-13.tar