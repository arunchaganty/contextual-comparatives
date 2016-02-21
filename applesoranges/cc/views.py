# -*- coding: utf-8 -*-
from django.shortcuts import render
from django.http import HttpResponse

from .models import NumericData, NumericMention, NumericExpression, NumericExpressionResponse

import random
import csv

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

def experiment_expression_results(request):
    """
    Display experiment to request for expressions
    """

    if request.method == "POST":
        # Set all the checked boxes to be WRONG.
        print(request.POST)
        raise NotImplementedError()


    # Get all comparisons
    responses = NumericExpressionResponse.objects.all()
    exprs = set(r.expression for r in responses)

    return render(request, 'experiment_expression_view.html', {'exprs': exprs})


def generate_experiment_expression(request):
    """
    Generate experiment content
    """
    # Get all comparisons
    expressions = list(NumericExpression.objects.filter(multiplier__lte=1001, multiplier__gte=0.09))

    random.seed(42)
    random.shuffle(expressions)

    response = HttpResponse(content_type='text/csv')
    response['Content-Disposition'] = 'attachment; filename="expressions.csv"'

    writer = csv.writer(response)

    # Group expressions into groups of 5.
    writer.writerow(["id", "taskids", "prompts", "zs"])
    for id, lst in enumerate([expressions[i:i+5] for i in xrange(0, len(expressions), 5)]):
        tasks = "\t".join(map(str, [e.id for e in lst]))
        prompts = "\t".join(get_prompt(e) for e in lst)
        zs = "\t".join(get_z(e) for e in lst)
        writer.writerow([id, tasks, prompts, zs])

    return response
