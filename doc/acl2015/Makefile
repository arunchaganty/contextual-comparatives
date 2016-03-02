default: refdb figures
	pdflatex main
	bibtex main

figures:
	make -C figures

clean:
	rm main.aux main.log main.out main.bbl main.blg

refdb:
	git clone https://github.com/percyliang/refdb

run:
	go main.pdf

.PHONY: figures clean
		
