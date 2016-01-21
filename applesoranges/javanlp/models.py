from django.db import models
from django.contrib.postgres.fields import ArrayField

# Create your models here.
class Sentence(models.Model):
    """
    Represents the consitutents of each sentence, with the basic
    annotations.
    """
    doc_id = models.TextField() # More general than a foreign key?
    sentence_index = models.IntegerField() # Index in the document to order sentences
    words = ArrayField(models.TextField())   # Tokens
    lemmas = ArrayField(models.TextField())  # Tokens
    pos_tags = ArrayField(models.TextField())     # Field
    ner_tags = ArrayField(models.TextField())     # Field.
    doc_char_begin = ArrayField(models.IntegerField())
    doc_char_end = ArrayField(models.IntegerField())
    constituencies = models.TextField(null=True) # A textual representation of the dependency tree
    dependencies = models.TextField(null=True) # A textual representation of the dependency tree
    gloss = models.TextField()

    def __str__(self):
        return self.gloss

    def __repr__(self):
        return "[Sentence {}]".format(self.gloss[:50])

