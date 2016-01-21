"""
Utilities for using javanlp
"""

import subprocess
import json
from urllib.parse import urlencode
from javanlp.models import Sentence

class AnnotationException(Exception):
    pass

def clean_response(inp):
    """
    Remove arbitrary characters from the response string
    """
    # Remove all alert characters.
    inp = inp.replace('', '')
    return inp

def annotate_document_raw(gloss, server="localhost:9000"):
    """
    Makes a call to a JavaNLP server to annotate the document @gloss.
    Returns an object (parsed from json) containing the annotations.
    """
    props = {"annotators": "tokenize, ssplit, pos, lemma, ner, parse, depparse",
             "inputFormat": "text",
             "outputFormat": "json"}

    uri = "%s?%s"%(server, urlencode({"properties" : json.dumps(props)}))
    try:
        response = subprocess.check_output(["curl", uri, "--data", gloss], stderr=subprocess.DEVNULL).decode()
        response = clean_response(response)
    except subprocess.CalledProcessError:
        raise RuntimeError("Error calling annotator")

    if response == "CoreNLP request timed out":
        raise AnnotationException("CoreNLP timed out while parsing: " + gloss)
    try:
        return json.loads(response)
    except ValueError:
        raise AnnotationException("Could not parse response: " + response)

def __to_sentence(sentence):
    """
    Convert a sentence annotation to a gloss.
    """
    ret = ""
    for tok in sentence['tokens']:
        ret += tok['originalText'] + tok['after']
    return ret


def annotate_document(doc_id, gloss, server="localhost:9000"):
    """
    Annotate the document with gloss and populate a list of Sentences
    """
    # Get annotations from the javanlp server.
    ann = annotate_document_raw(gloss, server)

    ret = []
    for i, sentence_ in enumerate(ann['sentences']):
        sentence = Sentence(doc_id = doc_id,
                            sentence_index = i,
                            words = [tok['word'] for tok in sentence_['tokens']],
                            lemmas = [tok['lemma'] for tok in sentence_['tokens']],
                            pos_tags = [tok['pos'] for tok in sentence_['tokens']],
                            ner_tags = [tok['ner'] for tok in sentence_['tokens']],
                            doc_char_begin = [tok['characterOffsetBegin'] for tok in sentence_['tokens']],
                            doc_char_end = [tok['characterOffsetEnd'] for tok in sentence_['tokens']],
                            constituencies = sentence_['parse'],
                            dependencies_raw = json.dumps(sentence_['dependencies']),
                            dependencies_simple = json.dumps(sentence_['collapsed-ccprocessed-dependencies']),
                            gloss = __to_sentence(sentence_))
        ret.append(sentence)
    return ret


