#!/bin/bash

out_dir=""
bin_dir=""
version=""
main_class_name=""
classp=""

# Loop through the arguments
while [ $# -gt 0 ]; do
    case "$1" in
        -version:*)
            version="${1#-version:}"
            ;;
        -main:*)
            main_class_name="${1#-main:}"
            echo "Adding Main-Class to Manifest: ${main_class_name}"
            ;;
        -cp:*)
            classp="${1#-cp:}"
            echo "Compiling with classpath: ${classp}"
            ;;
        *)
            # Unknown argument
            echo "Unknown argument: $1; use -version:X, -main:X"
            exit 1
            ;;
    esac
    shift
done

# Check if version is provided
if [ -z ${version} ]; then
  version=$(sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' < ../src/lu/pcy113/p4j/version.txt)
  echo "No argument provided; using current version: ${version}"
else
  #version=$1
  echo "Using provided version: ${version}"
fi

src_dir="../src/"
new_version=$(echo "${version}" | tr '.' '_')
out_dir="./${new_version}"
bin_dir="${out_dir}/bin"
manifest="${out_dir}/MANIFEST.MF"

# Create the bin directory
mkdir -p "${bin_dir}"

# Creating and writing the MANIFEST.MF
echo "Manifest-Version: 1.0" > ${manifest}

# Check if main class is provided
if ! [ -z ${main_class_name} ]; then
  echo "Main-Class: ${main_class_name}" >> ${manifest}
fi

# Compile sources
# Find .java files
find ${src_dir} -name "*.java" > "${out_dir}/sources.txt"

# Compile Java files

if [ -z $classp ]
then
    javac -nowarn -d "${bin_dir}" "@${out_dir}/sources.txt" 2>&1 | grep -v "^Note:"
else
    javac -nowarn -d "${bin_dir}" -cp "${classp}" "@${out_dir}/sources.txt" 2>&1 | grep -v "^Note:"
fi

echo "Compilation done to ${bin_dir}/"

# Add to JAR file
jar cvfm "${out_dir}/packets4j-${version}.jar" "${manifest}" -C "${bin_dir}" . > "/dev/null" 2>&1

echo "JAR file compressed to: ${out_dir}/packets4j-${version}.jar"
