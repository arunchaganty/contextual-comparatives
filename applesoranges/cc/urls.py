from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^view/$', views.view_candidates),
    url(r'^expr/view/$', views.expr_view),
    url(r'^expr/view/(?P<cnt>[0-9]+)?$', views.expr_view),
    url(r'^expr/inspect/$', views.expr_inspect),
    url(r'^expr/results/$', views.expr_results),
    url(r'^rank/view/$', views.rank_view),
    url(r'^rank/inspect/$', views.rank_inspect),
    url(r'^rank/stats/$', views.rank_stats),
    url(r'^eval/view/$', views.eval_view),
    url(r'^eval/inspect/$', views.eval_inspect),
    url(r'^eval/workers/$', views.eval_inspect_by_worker),
    url(r'^eval/mentions/$', views.eval_inspect_by_mention),
    url(r'^rate/view/$', views.rate_view),
    url(r'^rate/inspect/$', views.rate_inspect),
    url(r'^rate/mark/$', views.rate_mark),
    url(r'^rate/workers/$', views.rate_inspect_by_worker),
    url(r'^rate/mentions/$', views.rate_inspect_by_mention),
]
