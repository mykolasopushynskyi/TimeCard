#! /bin/bash
PROJECT_DIR='TimeCard';

if [ -d "$PROJECT_DIR" ]; then
  echo 'Stopping app...'
  cd TimeCard
  play stop
  echo 'Deleting old project...'
  cd ../
  rm -rf TimeCard
fi

if [ -f "master.zip" ]; then
  rm master.zip
fi

if [ -d "TimeCard-master" ]; then
  rm -rf TimeCard-master
fi

echo 'Downloading sources from repo...'
wget https://github.com/mykolasopushynskyi/TimeCard/archive/master.zip

echo 'Extracting archive...'
unzip -q master.zip
mv TimeCard-master/ TimeCard

cd TimeCard/
play deps --sync --forProd
play precompile

nohup play start -Dprecompiled=true --%prod &

