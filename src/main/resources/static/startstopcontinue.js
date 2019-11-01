$(document).ready(function () {

  loadBoard(boardId);

  $("#add-start").click(function () {
    addNote($("#start-list"), boardId, "START", $("#start-color").val(), "Start ");
  });

  $("#add-stop").click(function () {
    addNote($("#stop-list"), boardId, "STOP", $("#stop-color").val(), "Stop ");
  });

  $("#add-continue").click(function () {
    addNote($("#continue-list"), boardId, "CONTINUE", $("#continue-color").val(), "Continue ");
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
        loadNote($("#start-list"), boardId, 'START', data);
      });

      $(data.stops).each(function (index, data) {
        loadNote($("#stop-list"), boardId, 'STOP', data);
      });

      $(data.continues).each(function (index, data) {
        loadNote($("#continue-list"), boardId, 'CONTINUE', data);
      });
    });
  }

  function loadNote(list, boardId, column, note) {

    list.append(noteHtml(note.id, note.color, note.text));
    list.on('focusout', '#' + note.id, function () {

      var text = $("#" + this.id + " textarea").val().trim();
      if (text === "") {

        deleteNote(boardId, column, note.id).done(function (data) {
          $("#" + data.id).remove();
        });

      } else {

        updateNote(boardId, column, note.color, note.id, text);
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

  function updateNote(boardId, column, color, noteId, text) {

    var url = "api/board/" + boardId + "/column/" + column + "/note";
    var note = JSON.stringify({id: noteId, color: color, text: text});
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

  function addNote(list, boardId, column, color, text) {

    saveNote(boardId, column, color, text).done(function (data) {
      loadNote(list, boardId, column, data);
    });
  }
});