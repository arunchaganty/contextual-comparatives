from django.shortcuts import render

from .models import NumericData, NumericMention

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

