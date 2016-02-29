from django.db import models
from javanlp.models import Sentence
from django.contrib.postgres.fields import ArrayField

from cc.util import easy_number, round_multiplier, easy_unit
from collections import defaultdict

import json

# Create your models here.
class NumericMention(models.Model):
    """
    A mention of a numeric quantity in some text.
    """

    value = models.FloatField(help_text='Stores the absolute value of the mention')
    unit = models.CharField(max_length=128, help_text='Stores the unit of the mention')
    normalized_value = models.FloatField(help_text='Stores the normalized value of the mention')
    normalized_unit = models.CharField(max_length=128, help_text='Stores the normalized unit of the mention')
    sentence = models.ForeignKey(Sentence, help_text='Sentence mention was extracted from')
    doc_char_begin = models.IntegerField()
    doc_char_end = models.IntegerField()
    token_begin = models.IntegerField()
    token_end = models.IntegerField()

    def __str__(self):
        return "%.2E %s"%(self.value, self.unit)

    def __repr__(self):
        return "[NMention: %.2E %s]"%(self.value, self.unit)

    def gloss(self):
        offset = self.sentence.doc_char_begin[0]
        start, end = self.doc_char_begin - offset, self.doc_char_end - offset
        gloss = self.sentence.gloss
        return gloss[start:end]

    def html(self):
        """
        Prints an annotated version of the mention with the mentioned number highlighted.
        """
        offset = self.sentence.doc_char_begin[0]
        start, end = self.doc_char_begin - offset, self.doc_char_end - offset
        gloss = self.sentence.gloss
        return gloss[:start] + "<b>" + gloss[start:end] + "</b>" + gloss[end:]

class NumericData(models.Model):
    """
    Stores database of numeric data.
    """

    name = models.TextField(help_text='Name of the object')
    relation = models.TextField(help_text='Relation of the object to value')
    value = models.FloatField(help_text='Value of measurement')
    unit = models.CharField(max_length=128, help_text='Value of measurement')

    def __str__(self):
        return "The %s of %s is %.2E %s"%(self.relation, self.name, self.value, self.unit)

    def __repr__(self):
        return "[NData: %.2E %s]"%(self.value, self.unit)

class NumericExpression(models.Model):
    """
    Compare some expressions
    """

    # multiplier
    multiplier = models.FloatField(help_text="multiplier for expression")
    # sequence of numeric data (they will always be multiplied).
    arguments = ArrayField(models.IntegerField(), help_text="Arguments to this expression (always multiplied)")   # Tokens
    # net value
    value = models.FloatField(help_text="value for expression")
    # net unit
    unit = models.TextField(help_text="unit for expression")
    # Links to mentions (too tedious to keep track of right now)
    # mentions = models.ManyToManyField(NumericMention, help_text="associated mentions")

    def __str__(self):
        return "%.2f * %s"%(self.multiplier, " * ".join(["(%s)"%(str(a)) for a in self.get_arguments()]))

    def __repr__(self):
        return "%.2f * %s"%(self.multiplier, " * ".join([str(a) for a in self.arguments]))

    def get_arguments(self):
        """
        Parse the arguments array to get the actual arguments.
        """
        return [NumericData.objects.get(id=i) for i in self.arguments]

    def __ndata_to_string(self, ndata, filler="X"):
        return "%s %s (<u><b>%s</b></u>)"%(easy_number(ndata.value), easy_unit(ndata.unit), ndata.name,)

    def get_prompt(self):
        """
        Convert an selfession into a prompt
        """
        parts = ["<u><b>%s</b></u>"%(round_multiplier(self.multiplier))] + [
                 self.__ndata_to_string(arg) 
                 for arg in reversed(self.get_arguments())]
        return self.get_z() + " &asymp; " + " &times; ".join(parts)



    def get_z(self):
        """Get Z = expression"""
        return easy_number(self.value * self.multiplier) + " " + easy_unit(self.unit)

class NumericExpressionResponse(models.Model):
    """
    Turker responses
    """
    expression = models.ForeignKey(NumericExpression, help_text="expression", related_name='responses', related_query_name='response')
    prompt = models.TextField(help_text = "prompt given to the turker")
    description = models.TextField(help_text = "description returned by the turker")

    assignment_id = models.CharField(max_length=1024)
    worker_id = models.CharField(max_length=1024)
    worker_time = models.DurationField()
    approval = models.BooleanField(default=True)
    inspected = models.BooleanField(default=False)

    class Meta:
        unique_together = ('assignment_id', 'expression',)

    def __str__(self):
        return self.description

    def __repr__(self):
        return "[NExprResp: {} {}]".format(self.expression[:50], self.description[:50])


# TODO(chaganty): probably make this a many2many field in NumericMention.
class NumericMentionExpression(models.Model):
    """
    A many to many map between an existing numeric expression and numeric mention
    """
    mention = models.ForeignKey(NumericMention, help_text="linked numeric mention", related_name='mentionexpressions')
    expression = models.ForeignKey(NumericExpression, help_text="linked numeric expression", related_name='mentionexpressions')
    multiplier = models.FloatField(help_text="updated value")

class NumericMentionExpressionTask(models.Model):
    """
    Keeps track of a single mention and a list of (up to 4) expressions used to visualize it.
    """
    mention = models.ForeignKey(NumericMention, help_text="linked numeric mention", related_name='tasks', related_query_name='task')
    candidates = ArrayField(models.IntegerField(), help_text="Candidate expression responses")   # Tokens

    def get_candidates(self):
        """
        Parse the arguments array to get the actual arguments.
        """
        return NumericExpressionResponse.objects.filter(id__in=self.candidates)

    def get_grouped_responses(self):
        """Return responses grouped by candidates."""
        candidates = defaultdict(list)
        for response in self.responses.all():
            for choice in response.get_positive_candidates():
                candidates[choice].append(response)
            if len(response.chosen) == 0: # None!
                candidates[None].append(response)
        return candidates

    def get_positive_candidates(self):
        """Return candidates that have more than 3 agreements."""
        return dict((c,rs) for (c, rs) in self.get_grouped_responses().items() if len(rs) >= 3)

    def is_dud(self):
        """Return true if there are more than 4 agreements on the none category."""
        return len(self.get_grouped_responses()[None]) >= 3

    def to_json(self):
        return json.dumps({'id': self.id,
            'sentence_gloss' : self.mention.html(),
            'mention_gloss' : self.mention.gloss(),
            'choices' : [{
                'task_id': self.id,
                'response_id' : c.id,
                'gloss' : c.description} for c in self.get_candidates()]})
                
class NumericMentionExpressionTaskResponse(models.Model):
    """
    Turker responses
    """
    task = models.ForeignKey(NumericMentionExpressionTask, help_text="task turker rated", related_name='responses', related_query_name='response' )
    chosen = ArrayField(models.IntegerField(), help_text = "Candidates chosen by turker")

    assignment_id = models.CharField(max_length=1024)
    worker_id = models.CharField(max_length=1024)
    worker_time = models.DurationField()
    approval = models.BooleanField(default=True)
    inspected = models.BooleanField(default=False)

    class Meta:
        unique_together = ('assignment_id', 'task',)

    def get_positive_candidates(self):
        """
        Parse the arguments array to get the actual arguments.
        """
        return NumericExpressionResponse.objects.filter(id__in=self.chosen)

    def get_negative_candidates(self):
        """
        Parse the arguments array to get the actual arguments.
        """
        not_chosen = set(self.task.candidates) - set(self.chosen)
        return NumericExpressionResponse.objects.filter(id__in=not_chosen)

class NumericMentionExpressionBlacklist(models.Model):
    task = models.ForeignKey(NumericMentionExpressionTask, help_text="task turker rated", related_name='blacklist', related_query_name='blacklist' )
