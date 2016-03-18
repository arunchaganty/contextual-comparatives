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
]
