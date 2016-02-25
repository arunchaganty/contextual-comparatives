# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0015_remove_numericmention_type'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericMentionExpression',
            fields=[
                ('id', models.AutoField(serialize=False, auto_created=True, verbose_name='ID', primary_key=True)),
                ('multiplier', models.FloatField(help_text='updated value')),
                ('expression', models.ForeignKey(help_text='linked numeric expression', to='cc.NumericExpression')),
            ],
        ),
        migrations.AlterField(
            model_name='numericmention',
            name='normalized_unit',
            field=models.CharField(help_text='Stores the normalized unit of the mention', max_length=128),
        ),
        migrations.AlterField(
            model_name='numericmention',
            name='normalized_value',
            field=models.FloatField(help_text='Stores the normalized value of the mention'),
        ),
        migrations.AddField(
            model_name='numericmentionexpression',
            name='mention',
            field=models.ForeignKey(help_text='linked numeric mention', to='cc.NumericMention'),
        ),
    ]
