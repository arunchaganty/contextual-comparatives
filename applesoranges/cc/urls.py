from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^view/$', views.view_candidates, name='index'),
    url(r'^expr/view/$', views.experiment_expression, name='expr'),
    url(r'^expr/results/$', views.experiment_expression_results, name='expr'),
#    url(r'^expr/$', views.generate_experiment_expression),
]
