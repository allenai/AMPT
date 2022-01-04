all: venv/bin/mkdocs docs/generated/version.txt
	./venv/bin/mkdocs build
	zip -ro documentation.zip documentation
	cp documentation.zip ../src/main/resources

clean:
	rm -rf documentation
	rm documentation.zip

full_clean: clean
	rm -rf venv
	rm -rf docs/generated

venv/bin/mkdocs: requirements.txt
	python3 -m venv venv
	./venv/bin/pip install -r requirements.txt

gh-deploy: venv/bin/mkdocs docs/generated/version.txt
	./venv/bin/mkdocs gh-deploy --force

docs/generated:
	mkdir -p docs/generated

docs/generated/version.txt: ../pom.xml docs/generated
	mvn help:evaluate -f ../pom.xml -Dexpression=project.version -q -DforceStdout > docs/generated/version.txt

serve: venv/bin/mkdocs docs/generated/version.txt
	./venv/bin/mkdocs serve

