/**
 * Include annotations for spans.
 */

function add_sentiment_annotation(elem, score_bg, score_blank, score_context) {
  var txt = elem.html();
  txt = 
<sup>{bg_score:.2}</sup>\1<sup>{b_score:.2} > {score:.2}</sup>

            out = matcher.sub(r"<span title='{details}' style='color:hsl({color}, 100%, 50%); text-decoration:{decoration}' class='entity'><sup>{bg_score:.2}</sup>\1<sup>{b_score:.2} > {score:.2}</sup></span>"
  elem.html(txt);

}

/** 
 * Replace every occurrance of the keyword in the paragraph at root
 * with a marked span with the classname.
 */
function add_entity_annotation(root, keyword, classname) {
  var txt = root.html();
  var re = new RegExp("\\b("+keyword.toLowerCase()+")\\b", "gi");

  txt = txt.replace(re, "<span class='"+classname+"'>$1</span>");
  root.html(txt);
}

/** 
 * Replace every occurrance of the keyword in the paragraph at root
 * with a marked span with the classname.
 */
function blank_entity(root, keyword, classname) {
  var txt = root.html();
  var re = new RegExp("\\b("+keyword.toLowerCase()+")\\b", "gi");

  txt = txt.replace(re, "<span class='"+classname+"'>______</span>");
  root.html(txt);
}

add_sentiment_annotation($('subject-{{triple.id}'),
  {{triple.subject_background}},
  {{triple.subject_blankedcontextual}},
  {{triple.subject_contextual}});
add_sentiment_annotation($('object-{{triple.id}'),
  {{triple.object_background}},
  {{triple.object_blankedcontextual}},
  {{triple.object_contextual}});
highlight_spans($('sentence-{{triple.id}'),
  {{triple.subject.gloss}},
  {{triple.relation}},
  {{triple.object.gloss}});
