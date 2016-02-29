# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.contrib.postgres.fields


class Migration(migrations.Migration):

    dependencies = [
        ('cc', '0018_auto_20160227_0623'),
    ]

    operations = [
        migrations.CreateModel(
            name='NumericMentionExpressionBlacklist',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
            ],
        ),
        migrations.AlterField(
            model_name='numericexpressionresponse',
            name='expression',
            field=models.ForeignKey(related_name='responses', to='cc.NumericExpression', help_text='expression', related_query_name='response'),
        ),
        migrations.AlterField(
            model_name='numericmentionexpression',
            name='expression',
            field=models.ForeignKey(related_name='mentionexpressions', to='cc.NumericExpression', help_text='linked numeric expression'),
        ),
        migrations.AlterField(
            model_name='numericmentionexpression',
            name='mention',
            field=models.ForeignKey(related_name='mentionexpressions', to='cc.NumericMention', help_text='linked numeric mention'),
        ),
        migrations.AlterField(
            model_name='numericmentionexpressiontask',
            name='candidates',
            field=django.contrib.postgres.fields.ArrayField(base_field=models.IntegerField(), size=None, help_text='Candidate expression responses'),
        ),
        migrations.AlterField(
            model_name='numericmentionexpressiontask',
            name='mention',
            field=models.ForeignKey(related_name='tasks', to='cc.NumericMention', help_text='linked numeric mention', related_query_name='task'),
        ),
        migrations.AlterField(
            model_name='numericmentionexpressiontaskresponse',
            name='chosen',
            field=django.contrib.postgres.fields.ArrayField(base_field=models.IntegerField(), size=None, help_text='Candidates chosen by turker'),
        ),
        migrations.AlterField(
            model_name='numericmentionexpressiontaskresponse',
            name='task',
            field=models.ForeignKey(related_name='responses', to='cc.NumericMentionExpressionTask', help_text='task turker rated', related_query_name='response'),
        ),
        migrations.AddField(
            model_name='numericmentionexpressionblacklist',
            name='task',
            field=models.ForeignKey(related_name='blacklist', to='cc.NumericMentionExpressionTask', help_text='task turker rated', related_query_name='blacklist'),
        ),
    ]
