/* global boardId */

"use strict";

$(document).ready(function () {

  var COLUMNS = {
    "START": $("#start-list"),
    "STOP": $("#stop-list"),
    "CONTINUE": $("#continue-list")
  };

  var webSocket = openWebSocket(boardId);
  loadBoard(boardId);

  startWebSocketPing(60 * 1000);

  $("#add-start").click(function () {
    addNote(boardId, "START", {color: $("#start-color").val(), text: "Start "});
  });

  $("#add-stop").click(function () {
    addNote(boardId, "STOP", {color: $("#stop-color").val(), text: "Stop "});
  });

  $("#add-continue").click(function () {
    addNote(boardId, "CONTINUE", {color: $("#continue-color").val(), text: "Continue "});
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
    }).fail(handleFailure);
  }

  function scrollNote(noteId) {

    $("#" + noteId)[0].scrollIntoView({
      behavior: "smooth",
      block: "end",
      inline: "nearest"
    });
  }

  function addNote(boardId, column, note) {

    saveNote(boardId, column, note).done(function (data) {
      loadNote(boardId, column, {id: data.id, color: note.color, text: note.text});
      scrollNote(data.id);
    }).fail(handleFailure);
  }

  function loadNote(boardId, column, note) {

    COLUMNS[column].append(noteHtml(note.id, note.color, note.text));
    COLUMNS[column].on('change', '#' + note.id + ' > textarea', function () {

      var text = $("#" + note.id + " > textarea").val().trim();
      if (text === "") {

        deleteNote(boardId, column, note.id).done(function (data) {
          $("#" + data.id).remove();
          sendEvent(boardId, column, {id: note.id, color: note.color, text: text});

        }).fail(handleFailure);

      } else if (text === "export") {

        deleteNote(boardId, column, note.id).done(function (data) {
          $("#" + data.id).remove();
          window.location = "api/board/" + boardId + "/export";
        }).fail(handleFailure);

      } else {

        updateNote(boardId, column, {id: note.id, text: text}).done(function (data) {
          sendEvent(boardId, column, {id: note.id, color: note.color, text: text});
        }).fail(handleFailure);
      }
    });
  }

  function saveNote(boardId, column, note) {

    return $.ajax({
      url: "api/board/" + boardId + "/column/" + column + "/note",
      data: JSON.stringify(note),
      type: 'POST',
      contentType: 'application/json'
    });
  }

  function updateNote(boardId, column, note) {

    return $.ajax({
      url: "api/board/" + boardId + "/column/" + column + "/note",
      data: JSON.stringify(note),
      type: 'PUT',
      contentType: 'application/json'
    });
  }

  function deleteNote(boardId, column, noteId) {

    return $.ajax({
      url: "api/board/" + boardId + "/column/" + column + "/note/" + noteId,
      type: 'DELETE',
      contentType: 'application/json'
    });
  }

  function openWebSocket(boardId) {

    var url = "ws://" + window.location.host + window.location.pathname + "events/" + boardId;
    var webSocket = new WebSocket(url);
    webSocket.onmessage = function (event) {
      var data = JSON.parse(event.data);
      handleEvent(data.boardId, data.column, data.note);
    };

    return webSocket;
  }

  function startWebSocketPing(delay) {

    setInterval(sendPing, delay);
  }

  function isWebSocketOpen(webSocket) {
    return webSocket.readyState === webSocket.OPEN;
  }

  function sendEvent(boardId, column, note) {

    if (!isWebSocketOpen(webSocket)) {
      webSocket = openWebSocket(boardId);
      loadBoard(boardId);
    }

    webSocket.send(JSON.stringify({boardId: boardId, column: column, note: note}));
  }

  function sendPing() {

    if (!isWebSocketOpen(webSocket)) {
      webSocket = openWebSocket(boardId);
      loadBoard(boardId);
    }

    webSocket.send("ping");
  }

  function handleEvent(boardId, column, note) {

    if (note.id && $("#" + note.id).length) {

      if (note.text) {
        $("#" + note.id + " > textarea").val(note.text);
      } else {
        $("#" + note.id).remove();
      }

    } else if (note.id && note.text) {
      loadNote(boardId, column, note);
    }
  }

  function handleFailure(error) {

    var error = $("#error");
    error.show();
    error.click(function () {
      error.hide();
    });
  }
});