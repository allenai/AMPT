all: venv/bin/mkdocs
	./venv/bin/mkdocs --verbose build
	zip -ro documentation.zip documentation
	cp documentation.zip ../src/main/resources

clean:
	rm -rf documentation
	rm documentation.zip


full_clean: clean
	rm -rf venv


venv/bin/mkdocs:
	virtualenv venv
	./venv/bin/pip install -r requirements.txt
