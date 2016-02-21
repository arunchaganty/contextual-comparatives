# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0011_auto_20160221_0120'),
    ]

    operations = [
        migrations.AlterField(
            model_name='numericexpressionresponse',
            name='assignment_id',
            field=models.CharField(max_length=1024),
        ),
        migrations.AlterUniqueTogether(
            name='numericexpressionresponse',
            unique_together=set([('assignment_id', 'expression')]),
        ),
    ]
