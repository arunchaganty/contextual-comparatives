# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.contrib.postgres.fields


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0008_numericexpressionresponse_approval'),
    ]

    operations = [
        migrations.AlterField(
            model_name='numericdata',
            name='name',
            field=models.TextField(help_text='Name of the object'),
        ),
        migrations.AlterField(
            model_name='numericdata',
            name='qualifiers',
            field=models.TextField(blank=True, default='', help_text='Information that qualifies this figure'),
        ),
        migrations.AlterField(
            model_name='numericdata',
            name='relation',
            field=models.TextField(help_text='Relation of the object to value'),
        ),
        migrations.AlterField(
            model_name='numericdata',
            name='unit',
            field=models.CharField(max_length=128, help_text='Value of measurement'),
        ),
        migrations.AlterField(
            model_name='numericdata',
            name='value',
            field=models.FloatField(help_text='Value of measurement'),
        ),
        migrations.AlterField(
            model_name='numericexpression',
            name='arguments',
            field=django.contrib.postgres.fields.ArrayField(size=None, base_field=models.IntegerField(), help_text='Arguments to this expression (always multiplied)'),
        ),
        migrations.AlterField(
            model_name='numericexpression',
            name='multiplier',
            field=models.FloatField(help_text='multiplier for expression'),
        ),
        migrations.AlterField(
            model_name='numericexpression',
            name='unit',
            field=models.TextField(help_text='unit for expression'),
        ),
        migrations.AlterField(
            model_name='numericexpression',
            name='value',
            field=models.FloatField(help_text='value for expression'),
        ),
        migrations.AlterField(
            model_name='numericexpressionresponse',
            name='description',
            field=models.TextField(help_text='description returned by the turker'),
        ),
        migrations.AlterField(
            model_name='numericexpressionresponse',
            name='expression',
            field=models.ForeignKey(to='cc.NumericExpression', help_text='expression'),
        ),
        migrations.AlterField(
            model_name='numericexpressionresponse',
            name='prompt',
            field=models.TextField(help_text='prompt given to the turker'),
        ),
        migrations.AlterField(
            model_name='numericmention',
            name='sentence',
            field=models.ForeignKey(to='javanlp.Sentence', help_text='Sentence mention was extracted from'),
        ),
        migrations.AlterField(
            model_name='numericmention',
            name='type',
            field=models.CharField(max_length=128, help_text='Broad category of unit'),
        ),
        migrations.AlterField(
            model_name='numericmention',
            name='unit',
            field=models.CharField(max_length=128, help_text='Stores the unit of the mention'),
        ),
        migrations.AlterField(
            model_name='numericmention',
            name='value',
            field=models.FloatField(help_text='Stores the absolute value of the mention'),
        ),
    ]
