# configures an example Java project folder

function display_help() {
	printf "This is a general configure script. See options below:
--help, -h
\tDisplays this output.
--initial, -i
\tSets up the initial directory hierarchy.
--manifest, -m
\tSets up an initial manifest file.\n"
}

for arg in "$@"; do
	case $arg in
	"--initial" | "-i") # set up initial hierarchy
		ds=(src src/com bin lib)
		for fld in ${ds[@]}; do
			if [ ! -e $fld ]; then mkdir $fld; fi
		done
		# populate src
		if [ ! -e manifest.mf ]; then touch manifest.mf; fi
		;;
	"--manifest" | "-m") # set up manifest file
		if [ -e manifest.mf ]; then continue; fi
		echo "Manifest-Version: 1.0" > manifest.mf
		printf "Name: %s\n" `basename $((pwd))` > manifest.mf
		printf "Class-Path: %s" `find lib -type f -name '*.java' | sed ':a;N;$$!ba;s/\n/\n /g'` > manifest.mf
		;;
	"--help" | "-h") # default
		display_help
		;;
	*)
		printf "Unknown option. Pass --help or -h for more information.\n"
		exit 1
		;;
	esac
done

if [ $# -eq 0 ]; then display_help; fi
