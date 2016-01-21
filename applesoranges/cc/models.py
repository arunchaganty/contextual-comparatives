from django.db import models
from javanlp.models import Sentence

# Create your models here.
class NumericMention(models.Model):
    """
    A mention of a numeric quantity in some text.
    """

    value = models.FloatField(help_text='Stores the absolute value of the mention')
    unit = models.CharField(max_length=128, help_text='Stores the unit of the mention')
    type = models.CharField(max_length=128, help_text='Broad category of unit')
    sentence = models.ForeignKey(Sentence, help_text='Sentence mention was extracted from')
    doc_char_begin = models.IntegerField()
    doc_char_end = models.IntegerField()
    token_begin = models.IntegerField()
    token_end = models.IntegerField()

    def __str__(self):
        return "%.2E %s"%(self.value, self.unit)

    def __repr__(self):
        return "[NMention: %.2E %s]"%(self.value, self.unit)

class NumericData(models.Model):
    """
    Stores database of numeric data.
    """

    name = models.TextField(help_text='Name of the object')
    relation = models.TextField(help_text='Relation of the object to value')
    value = models.FloatField(help_text='Value of measurement')
    unit = models.CharField(max_length=128, help_text='Value of measurement')
    type = models.CharField(max_length=128, help_text='Broad category of unit')
    qualifiers = models.TextField(help_text='Information that qualifies this figure', blank=True)

    def __str__(self):
        return "The %s of %s is %.2E %s"%(self.relation, self.name, self.value, self.unit)

    def __repr__(self):
        return "[NData: %.2E %s]"%(self.value, self.unit)

