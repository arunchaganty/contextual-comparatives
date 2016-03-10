# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0019_auto_20160229_1958'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericExpression_Train',
            fields=[
                ('numericexpression_ptr', models.OneToOneField(serialize=False, to='cc.NumericExpression', auto_created=True, parent_link=True, primary_key=True)),
            ],
            options={
                'db_table': 'cc_numericexpression_train_ids',
            },
            bases=('cc.numericexpression',),
        ),
    ]
