"use strict";

$(document).ready(function () {

  var COLUMNS = {
    "START": $("#start-list"),
    "STOP": $("#stop-list"),
    "CONTINUE": $("#continue-list"),
  }

  loadBoard(boardId);

  $("#add-start").click(function () {
    addNote(boardId, "START", $("#start-color").val(), "Start ");
  });

  $("#add-stop").click(function () {
    addNote(boardId, "STOP", $("#stop-color").val(), "Stop ");
  });

  $("#add-continue").click(function () {
    addNote(boardId, "CONTINUE", $("#continue-color").val(), "Continue ");
  });

  function noteHtml(id, color, text) {

    return '<li id="' + id + '" style="background-color: ' + color + '">'
            + '  <textarea>' + text + '</textarea>'
            + '</li>';
  }

  function loadBoard(boardId) {

    var url = "api/board/" + boardId;
    return $.ajax({
      url: url,
      type: 'GET',
      contentType: 'application/json'
    }).done(function (data) {

      $(data.starts).each(function (index, data) {
        loadNote(boardId, 'START', data);
      });

      $(data.stops).each(function (index, data) {
        loadNote(boardId, 'STOP', data);
      });

      $(data.continues).each(function (index, data) {
        loadNote(boardId, 'CONTINUE', data);
      });
    });
  }

  function loadNote(boardId, column, note) {

    COLUMNS[column].append(noteHtml(note.id, note.color, note.text));
    COLUMNS[column].on('focusout', '#' + note.id, function () {

      var text = $("#" + this.id + " textarea").val().trim();
      if (text === "") {

        deleteNote(boardId, column, note.id).done(function (data) {
          $("#" + data.id).remove();
        });

      } else {

        updateNote(boardId, column, note.id, text);
      }
    });
  }

  function saveNote(boardId, column, color, text) {

    var url = "api/board/" + boardId + "/column/" + column + "/note";
    var note = JSON.stringify({color: color, text: text});
    return $.ajax({
      url: url,
      data: note,
      type: 'POST',
      contentType: 'application/json'
    });
  }

  function updateNote(boardId, column, noteId, text) {

    var url = "api/board/" + boardId + "/column/" + column + "/note";
    var note = JSON.stringify({id: noteId, text: text});
    return $.ajax({
      url: url,
      data: note,
      type: 'PUT',
      contentType: 'application/json'
    });
  }

  function deleteNote(boardId, column, noteId) {

    var url = "api/board/" + boardId + "/column/" + column + "/note/" + noteId;
    return $.ajax({
      url: url,
      type: 'DELETE',
      contentType: 'application/json'
    });
  }

  function addNote(boardId, column, color, text) {

    saveNote(boardId, column, color, text).done(function (data) {
      loadNote(boardId, column, data);
    });
  }
});