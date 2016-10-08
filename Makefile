all:
	./gradlew fatJar
	./script/deploy.sh

run: all
	sudo ./planet_lab/run_poseidon.sh

upload: all
	rsync ./planet_lab/poseidon-all-1.0-SNAPSHOT.jar goettingenple_yang@planetlab-05.cs.princeton.edu:~/planet_lab/poseidon-all-1.0-SNAPSHOT.jar
