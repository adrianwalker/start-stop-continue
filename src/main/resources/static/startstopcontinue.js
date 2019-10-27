$(document).ready(function () {

  loadBoard(boardId);

  function note(id, text) {

    return '<li id="' + id + '" >'
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
        loadNote($("#start-list"), data);
      });
      $(data.stops).each(function (index, data) {
        loadNote($("#stop-list"), data);
      });
      $(data.continues).each(function (index, data) {
        loadNote($("#continue-list"), data);
      });
    });
  }

  function loadNote(list, data) {

    list.append(note(data.id, data.text));
    list.on('focusout', '#' + data.id, function () {

      var text = $("#" + this.id + " textarea").val().trim();
      if (text === "") {

        deleteNote(data.id, data.type).done(function (data) {
          $("#" + data.id).remove();
        });

      } else {

        updateNote(data.id, data.type, text);
      }
    });
  }

  function saveNote(type, text) {

    var url = "api/board/" + boardId + "/note";
    var data = JSON.stringify({type: type, text: text});
    return $.ajax({
      url: url,
      data: data,
      type: 'POST',
      contentType: 'application/json'
    });
  }

  function updateNote(noteId, type, text) {

    var url = "api/board/" + boardId + "/note";
    var data = JSON.stringify({id: noteId, type: type, text: text});
    return $.ajax({
      url: url,
      data: data,
      type: 'PUT',
      contentType: 'application/json'
    });
  }

  function deleteNote(noteId, type) {

    var url = "api/board/" + boardId + "/note";
    var data = JSON.stringify({id: noteId, type: type});
    return $.ajax({
      url: url,
      data: data,
      type: 'DELETE',
      contentType: 'application/json'
    });
  }

  function addNote(list, type, text) {

    saveNote(type, text).done(function (data) {
      loadNote(list, data);
    });
  }

  $("#add-start").click(function () {
    addNote($("#start-list"), "START", "Start ");
  });

  $("#add-stop").click(function () {
    addNote($("#stop-list"), "STOP", "Stop ");
  });

  $("#add-continue").click(function () {
    addNote($("#continue-list"), "CONTINUE", "Continue ");
  });
});