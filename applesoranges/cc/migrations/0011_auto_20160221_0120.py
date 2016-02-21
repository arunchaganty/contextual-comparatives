# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0010_remove_numericdata_qualifiers'),
    ]

    operations = [
        migrations.AlterField(
            model_name='numericexpressionresponse',
            name='assignment_id',
            field=models.CharField(unique=True, max_length=1024),
        ),
    ]
