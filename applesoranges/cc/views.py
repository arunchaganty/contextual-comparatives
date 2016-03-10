# -*- coding: utf-8 -*-
from django.shortcuts import render, redirect
from django.http import HttpResponse
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

from collections import Counter

from .models import *

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

def expr_view(request, cnt=5):
    """
    Display experiment to request for expressions
    """
    cnt = int(cnt)

    # Get all comparisons
    exprs = NumericExpression.objects.order_by('?')[:cnt] #filter(multiplier__lte=1001, multiplier__gte=0.09).order_by('?')
    #exprs = NumericExpression.objects.filter(id__in = [21037, 21039, 21040, 21042, 24421, 24435, 24563, 24669, 24685, 24746, 24856, 24989, 25023,])

    # get the text
    tasks = "\t".join(map(str, [e.id for e in exprs]))
    prompts = "\t".join(e.get_prompt() for e in exprs)
    zs = "\t".join(e.get_z() for e in exprs)

    return render(request, 'expr_view.html', {'taskids': tasks, 'prompts': prompts, 'zs' : zs})


def expr_inspect(request):
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

    return render(request, 'expr_inspect.html', {
        'restrict_noinspect' : True,
        'expressions': expressions})


def expr_results(request):
    """
    Display experiment to request for expressions
    """

    expressions = NumericExpression.objects.all()
    #expressions = NumericExpression.objects.filter(id__in = [22997, 23103])
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

    return render(request, 'expr_inspect.html', {
        'restrict_noinspect' : False,
        'expressions': expressions})

def rank_view(request):
    """
    Produce a ranking of expressions.
    """
    blacklist = set(elem.task_id for elem in NumericMentionExpressionBlacklist.objects.all())
    tasks = [t for t in NumericMentionExpressionTask.objects.order_by('?') if t.id not in blacklist][:5]

    return render(request, 'rank_view.html', {
        'tasks': "\t".join(t.to_json() for t in tasks)})

def rank_inspect(request):
    """
    Inspect results from experiment expr..
    """

    tasks = NumericMentionExpressionTask.objects.filter(response__id__gte = 0).distinct()

    paginator = Paginator(tasks, 250) # 25 -- should be good enough to load

    page = request.GET.get('page')
    try:
        tasks = paginator.page(page)
    except PageNotAnInteger:
        # If page is not an integer, deliver first page.
        tasks = paginator.page(1)
    except EmptyPage:
        # If page is out of range (e.g. 9999), deliver last page of results.
        tasks = paginator.page(paginator.num_pages)

    # update pages
    if not any([t.responses.filter(inspected=False).count() > 0 for t in tasks]):
        try:
            while True:
                tasks = paginator.page(tasks.next_page_number())
                if any([t.responses.filter(inspected=False).count() > 0 for t in tasks]):
                    return redirect(request.path+'?page=%d'%tasks.number)
        except EmptyPage:
            pass

    # Now inspect within this page.
    if request.method == "POST":
        # Set all the checked boxes to be WRONG.
        print(request.POST)
        for task in tasks:
            print(task)
            for response in task.responses.filter(inspected=False):
                # See if the response is on.
                print(response.id, request.POST.get(str(response.id), "off"))
                approval = request.POST.get(str(response.id), "off") == "on"

                # Update responses
                response.approval = approval
                response.inspected = True
                response.save()
        # redirect to the next page.
        return redirect(request.path+'?page=%d'%tasks.next_page_number())

    return render(request, 'rank_inspect.html', {
        'restrict_noinspect' : True,
        'tasks': tasks})

def rank_stats(request):
    """
    Inspect results from experiment expr..
    """

    tasks = NumericMentionExpressionTask.objects.filter(responses__id__gte = 0).distinct()

    histogram = Counter()

    for task in tasks:
        histogram_ = Counter()
        for response in task.numericmentionexpressiontaskresponse_set.all():
            if len(response.chosen) == 0:
                histogram_['none'] += 1
            for elem in response.chosen:
                histogram_[elem] += 1
        # summarize histogram
        for i, c in enumerate(task.candidates):
            histogram[(0, histogram_[c])] += 1
            histogram[(i+1, histogram_[c])] += 1
        histogram[('none', histogram_['none'])] += 1

    print(sorted(list(histogram.items())))

    return None

