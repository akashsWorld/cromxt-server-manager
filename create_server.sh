#!/bin/bash

detect_package_manager() {
    if command -v apt > /dev/null 2>&1; then
        echo "apt"
    elif command -v dnf > /dev/null 2>&1; then
        echo "dnf"
    elif command -v yum > /dev/null 2>&1; then
        echo "yum"
    elif command -v pacman > /dev/null 2>&1; then
        echo "pacman"
    elif command -v zypper > /dev/null 2>&1; then
        echo "zypper"
    else
        echo "unknown"
        return 1
    fi
}

install_crombucket_dependencies(){
    working_directory=$1
    
    cd "cromxt-distributed-system-dependencies"
    
    cd crombucket
    
    cd crombucket-common-modules
    mvn clean install
    cd ..

    cd crombucket-grpc-configuration
    mvn clean install

    echo "Dependencies installed successfully."

    cd $working_directory
   
}

# Capture the output of the function
package_manager=$(detect_package_manager)

# Check if the package manager was detected
if [ "$package_manager" = "unknown" ]; then
    echo "Cannot determine the package manager."
else
    echo "This system uses $package_manager for package management."
fi


sudo $package_manager install git curl python3 maven -y

working_directory="$HOME/Development/Test/cromxt-server-manager"

mkdir -p $working_directory

current_directory="$(pwd)/cromxt-server-manager"

cp -r $current_directory/requirements.txt $current_directory/main.py "$working_directory"


cd $working_directory

git clone https://github.com/akashsWorld/cromxt-distributed-system-dependencies.git

install_crombucket_dependencies $working_directory


python3 -m venv env

source env/bin/activate

pip install -r requirements.txt
    
fastapi dev main.py

cd $current_directory


