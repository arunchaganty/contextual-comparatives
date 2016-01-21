
from django.core.management.base import BaseCommand, CommandError
from javanlp.models import Sentence
from javanlp.util import annotate_document

import os, stat, csv


class Command(BaseCommand):
    'Takes documents from [source], sends them through JavaNLP for annotations and then populates the sentences table.'
    help = 'Takes documents from [source], sends them through JavaNLP for annotations and then populates the sentences table.'

    def add_arguments(self, parser):
        parser.add_argument('--directory', type=str, help="Path to a directory from which to read each document sequentially")
        parser.add_argument('--csv', type=str, help="Path to a CSV file with format 'docid,gloss'")
        parser.add_argument('--server', type=str, default='localhost:9000', help="A JavaNLP annotation server endpoint")

    def get_docstream(self, **options):
        """
        Obtain a stream of documents from the arguments
        """
        if options['directory'] is not None:
            top = options['directory']
            for fname in os.listdir(top):
                path = os.path.join(top, fname)

                # For each file.
                if stat.S_ISREG(os.stat(path).st_mode):
                    with open(path) as f:
                        doc_id = fname.split('.', 1)[0]
                        gloss = f.read()
                    yield doc_id, gloss
        elif options['csv'] is not None:
            with open(options['csv']) as f:
                for doc_id, gloss in csv.reader(f):
                    yield doc_id, gloss

    def handle(self, *args, **options):
        if (not options['directory'] is None and not options['csv'] is None) or (options['directory'] is None and options['csv'] is None):
            raise CommandError("Must set exactly one of directory or csv")

        stream = self.get_docstream(**options)
        for doc_id, gloss in stream:
            print(doc_id)
            # Get annotations from the javanlp server.
            for sentence in annotate_document(doc_id, gloss, server=options['server']):
                sentence.save()

