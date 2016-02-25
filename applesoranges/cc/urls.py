from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^view/$', views.view_candidates),
    url(r'^expr/view/$', views.experiment_expression),
    url(r'^expr/view/(?P<cnt>[0-9]+)?$', views.experiment_expression),
    url(r'^expr/inspect/$', views.experiment_expression_inspect_results),
    url(r'^expr/results/$', views.experiment_expression_results),
    url(r'^rank/$', views.rank_expressions),
]
