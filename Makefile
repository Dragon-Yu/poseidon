all:
	./gradlew fatJar
	./script/deploy.sh

run: all
	sudo ./planet_lab/run_traffic_size.sh
