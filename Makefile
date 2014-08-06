default: 
	javac \
	./src/ksketch/IView.java \
	./src/ksketch/Stroke.java \
	./src/ksketch/GraphicalView.java \
	./src/ksketch/Frame.java


	java -classpath "./src" ksketch.Frame

clean:
	$(RM) *.class
