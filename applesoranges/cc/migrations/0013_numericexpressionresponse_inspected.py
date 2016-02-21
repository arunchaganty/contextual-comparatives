# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0012_auto_20160221_0125'),
    ]

    operations = [
        migrations.AddField(
            model_name='numericexpressionresponse',
            name='inspected',
            field=models.BooleanField(default=False),
        ),
    ]
