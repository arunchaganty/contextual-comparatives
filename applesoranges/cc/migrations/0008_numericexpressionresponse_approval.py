# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0007_numericexpressionresponse_assignment_id'),
    ]

    operations = [
        migrations.AddField(
            model_name='numericexpressionresponse',
            name='approval',
            field=models.BooleanField(default=True),
        ),
    ]
