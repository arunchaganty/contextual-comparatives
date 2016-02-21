# -*- coding: utf-8 -*-
from django.shortcuts import render, redirect
from django.http import HttpResponse
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

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
        print request.POST
        for expression in expressions:
            print expression
            for response in expression.numericexpressionresponse_set.filter(inspected=False):
                # See if the response is on.
                print response.id, request.POST.get(str(response.id), "off")
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
