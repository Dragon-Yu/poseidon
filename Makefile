all:
	./gradlew fatJar
	./script/deploy.sh
