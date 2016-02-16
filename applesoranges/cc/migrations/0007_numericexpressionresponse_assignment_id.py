# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0006_numericexpressionresponse'),
    ]

    operations = [
        migrations.AddField(
            model_name='numericexpressionresponse',
            name='assignment_id',
            field=models.CharField(default='', max_length=1024),
            preserve_default=False,
        ),
    ]
