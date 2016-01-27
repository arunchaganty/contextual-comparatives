/**
 * Functions for Hypatia.
 */

function formatTooltip(v) {
  return v/10.;
}

function addRow(table, idx, sid, text, value, comment) {
  var isInitialized = (value != undefined);
  value = isInitialized ? value : 0;
  // Annotate all spans in the well.

  row = $("<tr />");
  row.append($("<td />").text(idx));
  row.append($("<td />").text("the " + text));
  row.append($("<td />")
      .append($("<span />", {"class":"glyphicon glyphicon-thumbs-down", "aria-hidden": "true"}))
      .append($("<div />", {"class":"slider question-slider", "id" : "slider-"+sid}))
      .append($("<span />", {"class":"glyphicon glyphicon-thumbs-up", "aria-hidden": "true"})));
  if (isInitialized) {
    row.append($("<td />").text(comment));
  }
  table.append(row);

  // Activate slider
  var slider = $('#slider-'+sid);
  slider.slider({"min":-10, "max":10, 
    "value" : value, 
    "tooltip": "hide", 
    "handle" : (isInitialized ? "square" : "round")
  });
  // Set the "value set" property off
  slider.attr("valueSet", isInitialized);

  // Set a callback to change the handle on click.
  slider.on("slideStop", function(ev) {
    jQuery.each(slider.parent().find("div.slider-handle"), function(i, handle) {
      // Change the handle
      $(handle).removeClass("round");
      $(handle).addClass("square");

      // record as set.
      slider.attr("valueSet", true);
    });
  });
  

  var well = $(table.parent().children('div').children('.well')[0]);
  if (well.length > 0) {
    var entity_id = "entity-"+ idx;
    // Add an annotation that will save this index
    add_entity_annotation(well, text, entity_id);
    // Set a callback to highlight entities when mouse-over
    row.mouseenter(function (ev) {
      well.find('span.'+entity_id).css({"font-weight":"bold"});
    });

    row.mouseleave(function (ev) {
      well.find('span.'+entity_id).css({"font-weight":"normal"});
    });
  }

  return row;
}

/**
 * Add a row in a table for the blank entity experiment
 */
function addBlankRow(table, idx, sid, text, value, comment) {
  var isInitialized = (value != undefined);
  value = isInitialized ? value : 0;
  // Annotate all spans in the well.

  row = $("<tr />");
  row.append($("<td />").text(idx));
  row.append($("<td />")
      .append($("<span />", {"class":"glyphicon glyphicon-thumbs-down", "aria-hidden": "true"}))
      .append($("<div />", {"class":"slider question-slider", "id" : "slider-"+sid}))
      .append($("<span />", {"class":"glyphicon glyphicon-thumbs-up", "aria-hidden": "true"})));
  if (isInitialized) {
    row.append($("<td />").text(comment));
  }
  table.append(row);

  // Activate slider
  var slider = $('#slider-'+sid);
  slider.slider({"min":-10, "max":10, 
    "value" : value, 
    "tooltip": "hide", 
    "handle" : (isInitialized ? "square" : "round")
  });
  // Set the "value set" property off
  slider.attr("valueSet", isInitialized);

  // Set a callback to change the handle on click.
  slider.on("slideStop", function(ev) {
    jQuery.each(slider.parent().find("div.slider-handle"), function(i, handle) {
      // Change the handle
      $(handle).removeClass("round");
      $(handle).addClass("square");

      // record as set.
      slider.attr("valueSet", true);
    });
  });
  

  var well = $(table.parent().children('div').children('.well')[0]);
  if (well.length > 0) {
    var entity_id = "entity-"+ idx;
    // Add an annotation that will save this index
    blank_entity(well, text, entity_id);
  }

  return row;
}


function addEntityRow(table, idx, sid, text, value, comment) {
  var isInitialized = (value != undefined);
  value = isInitialized ? value : 0;
  // Annotate all spans in the well.

  row = $("<tr />");
  row.append($("<td />").text(idx));
  row.append($("<td />").text(text));
  row.append($("<td />")
      .append($("<span />", {"class":"glyphicon glyphicon-thumbs-down", "aria-hidden": "true"}))
      .append($("<div />", {"class":"slider question-slider", "id" : "slider-"+sid}))
      .append($("<span />", {"class":"glyphicon glyphicon-thumbs-up", "aria-hidden": "true"})));
  if (isInitialized) {
    row.append($("<td />").text(comment));
  }
  table.append(row);

  // Activate slider
  var slider = $('#slider-'+sid);
  slider.slider({"min":-10, "max":10, 
    "value" : value, 
    "tooltip": "hide", 
    "handle" : (isInitialized ? "square" : "round")
  });
  // Set the "value set" property off
  slider.attr("valueSet", isInitialized);

  // Set a callback to change the handle on click.
  slider.on("slideStop", function(ev) {
    jQuery.each(slider.parent().find("div.slider-handle"), function(i, handle) {
      // Change the handle
      $(handle).removeClass("round");
      $(handle).addClass("square");

      // record as set.
      slider.attr("valueSet", true);
    });
  });
  

  var well = $(table.parent().children('div').children('.well')[0]);
  if (well.length > 0) {
    var entity_id = "entity-"+ idx;
    // Add an annotation that will save this index
    add_entity_annotation(well, text, entity_id);
    // Set a callback to highlight entities when mouse-over
    row.mouseenter(function (ev) {
      well.find('span.'+entity_id).css({"font-weight":"bold"});
    });

    row.mouseleave(function (ev) {
      well.find('span.'+entity_id).css({"font-weight":"normal"});
    });
  }

  return row;
}



function checkInputsSet() {
  var entities = getEntities(); 
  var valueStr = "";
  var success = true;
  for(var i=0; i<entities.length; i++) {
    var slider = $('#slider-'+(i+1));
    var tr = $(slider.parents("tr")[0]); // Get parent row.
    if (slider.attr("valueSet") == "false") { // Damn JS types!
      tr.css("font-weight", "bold");
      success = false;
    } else {
      tr.css("font-weight", "");
    }
  }
  return success;
}

/**
  Verifies that all the inputs at these dom locations have been "clicked".
  */
function validateInputs() {
  var inputsSet = checkInputsSet();
  if (checkInputsSet()) {
    // Get the value string
    var entities = getEntities(); 
    var valueStr = "";
    for(var i=0; i<entities.length; i++) {
      var slider = $('#slider-'+(i+1));
      //assert(slider.attr("valueSet"));
      if (valueStr.length > 0) {
        valueStr += "\t";
      }
      valueStr += slider.val();
    }
    // Set value 
    $('#responses').attr("value", valueStr);

    // Change UI
    $("#errorBox").addClass("hidden");
    $("#successBox").removeClass("hidden");
    return true;
  } else {

    // Set the "Error box"
    $("#errorBox").removeClass("hidden");
    return false;
  }
}

function getEntities() {
  return $("#entities").attr("value").split("\t");
}

function getSentences() {
  return $("#sentences").attr("value").split("\t");
}

function addSentence(root, idx, sentence) {
  var tmpl = "<div class=\"panel panel-default\"> <div class=\"panel-body\"> <div class=\"well\"  style=\"font-size: large;\"> ${sentence} &nbsp; </div> In the above sentence, what do you think <em>the writers feelings</em> are towards the following things: </div> <table class=\"table table-hover\" id=\"questions-${idx}\"> <colgroup> <col span=\"1\" style=\"width: 5%;\"> <col span=\"1\" style=\"width: 65%;\"> </colgroup> <thead> <tr><th>#</th><th>Writer's feelings</th></tr> </thead> </table> </div> ";
  var div = tmpl.replace("${sentence}", sentence).replace("${idx}", idx);
  root.append(div);
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


