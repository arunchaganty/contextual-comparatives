# -*- coding: utf-8 -*-
from django.shortcuts import render, redirect
from django.http import HttpResponse
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

from .models import NumericData, NumericMention, NumericExpression, NumericExpressionResponse, NumericMentionExpression

import random
import csv
import json

# Create your views here.

def view_candidates(request):
    """
    Renders all the relations
    """

    # Get all mentions of figures
    mentions = NumericMention.objects.all()[:10]

    # Populate candidates.
    candidates = []
    for mention in mentions:
        candidates_ = []
        for candidate in NumericData.objects.order_by('?')[:5]:
            multiplier = mention.value/candidate.value
            if multiplier > 1:
                multiplier = round(multiplier)
            else:
                multiplier = "%.1f"%(multiplier)
            candidates_.append((multiplier, candidate))
        candidates.append(candidates_)

    # return render(request, 'list.html', {'mentions': mentions, 'candidates' : candidates, 'mc' : zip(mentions, candidates)})
    return render(request, 'list.html', {'mc' : zip(mentions, candidates)})

def experiment_expression(request, cnt=5):
    """
    Display experiment to request for expressions
    """
    cnt = int(cnt)

    # Get all comparisons
    exprs = NumericExpression.objects.order_by('?')[:cnt] #filter(multiplier__lte=1001, multiplier__gte=0.09).order_by('?')

    # get the text
    tasks = "\t".join(map(str, [e.id for e in exprs]))
    prompts = "\t".join(e.get_prompt() for e in exprs)
    zs = "\t".join(e.get_z() for e in exprs)

    return render(request, 'experiment_expression.html', {'taskids': tasks, 'prompts': prompts, 'zs' : zs})


def experiment_expression_inspect_results(request):
    """
    Inspect results from experiment expr..
    """

    expressions = NumericExpression.objects.all()
    paginator = Paginator(expressions, 10) # 25 -- should be good enough to load

    page = request.GET.get('page')
    try:
        expressions = paginator.page(page)
    except PageNotAnInteger:
        # If page is not an integer, deliver first page.
        expressions = paginator.page(1)
    except EmptyPage:
        # If page is out of range (e.g. 9999), deliver last page of results.
        expressions = paginator.page(paginator.num_pages)

    # update pages
    if not any([e.numericexpressionresponse_set.filter(inspected=False).count() > 0 for e in expressions]):
        try:
            while True:
                expressions = paginator.page(expressions.next_page_number())
                if any([e.numericexpressionresponse_set.filter(inspected=False).count() > 0 for e in expressions]):
                    return redirect(request.path+'?page=%d'%expressions.number)
        except EmptyPage:
            pass

    # Now inspect within this page.
    if request.method == "POST":
        # Set all the checked boxes to be WRONG.
        print(request.POST)
        for expression in expressions:
            print(expression)
            for response in expression.numericexpressionresponse_set.filter(inspected=False):
                # See if the response is on.
                print(response.id, request.POST.get(str(response.id), "off"))
                approval = request.POST.get(str(response.id), "off") == "on"

                # Update responses
                response.approval = approval
                response.inspected = True
                response.save()
        # redirect to the next page.
        return redirect(request.path+'?page=%d'%expressions.next_page_number())

    return render(request, 'experiment_expression_view.html', {
        'restrict_noinspect' : True,
        'expressions': expressions})


def experiment_expression_results(request):
    """
    Display experiment to request for expressions
    """

    expressions = NumericExpression.objects.all()
    paginator = Paginator(expressions, 10) # 25 -- should be good enough to load

    page = request.GET.get('page')
    try:
        expressions = paginator.page(page)
    except PageNotAnInteger:
        # If page is not an integer, deliver first page.
        expressions = paginator.page(1)
    except EmptyPage:
        # If page is out of range (e.g. 9999), deliver last page of results.
        expressions = paginator.page(paginator.num_pages)

    return render(request, 'experiment_expression_view.html', {
        'restrict_noinspect' : False,
        'expressions': expressions})

class Choice(object):
    """
    lightweight choice
    """

    def __init__(self, mention_id, response_id, expression_id, gloss):
        self.mention_id = mention_id
        self.response_id = response_id
        self.expression_id = expression_id
        self.gloss = gloss

    def to_json(self):
        return json.dumps({'mention_id': self.mention_id, 
            'response_id': self.response_id,
            'expression_id' : self.expression_id,
            'gloss' : self.gloss})

class ChoiceSet(object):
    """
    Rank options
    """
    def __init__(self, mention_id, sentence_gloss, mention_gloss, choices):
        self.mention_id = mention_id
        self.sentence_gloss = sentence_gloss
        self.mention_gloss = mention_gloss
        self.choices = choices

    @staticmethod
    def from_mention(mention, choices):
        """
        from db mention
        """
        responses = [c.numericexpressionresponse_set.order_by('?').first() for c in choices]
        choices = [Choice(mention.id, r.id, r.expression_id, r.description) for r in responses]
        return ChoiceSet(mention.id, mention.html(), mention.gloss(), choices)

    def to_json(self):
        return json.dumps({'mention_id': self.mention_id,
            'sentence_gloss' : self.sentence_gloss,
            'mention_gloss' : self.mention_gloss,
            'choices' : [c.to_json() for c in self.choices]})

def rank_expressions(request):
    """
    Produce a ranking of expressions.
    """
    GROUP_SIZE = 7
    WINDOW_SHIFT = 5

    mentions = NumericMention.objects.filter(id__in = NumericMentionExpression.objects.values_list('mention')).order_by('?')[:10]

    tasks = []
    for mention in mentions:
        perspectives = NumericExpression.objects.filter(id__in = mention.numericmentionexpression_set.values_list('expression'))
        # sliding window
        if len(perspectives) > GROUP_SIZE:
            choice_sets = [perspectives[i:i+GROUP_SIZE] for i in range(0, len(perspectives), WINDOW_SHIFT)]
        else:
            choice_sets = [perspectives,]
        for choice_set in choice_sets:
            task = ChoiceSet.from_mention(mention, choice_set)
            tasks.append(task)


    return render(request, 'experiment_rank.html', {
        'tasks': "\t".join(t.to_json() for t in tasks)})

