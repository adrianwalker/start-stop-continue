/* global boardId */

"use strict";

var UUID = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";

function uuid() {

  var dt = new Date().getTime();
  var uuid = UUID.replace(/[xy]/g, function (c) {
    var r = (dt + Math.random() * 16) % 16 | 0;
    dt = Math.floor(dt / 16);
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });

  return uuid;
}

$(document).ready(function () {

  var _TEMP = "_temp";
  var COLUMNS = {
    "START": $("#start-list"),
    "STOP": $("#stop-list"),
    "CONTINUE": $("#continue-list")
  };

  var webSocket = openWebSocket(boardId);
  loadBoard(boardId);

  startWebSocketPing(60 * 1000);

  $("#add-start").click(function () {
    addNote(boardId, "START", {id: uuid() + _TEMP, color: $("#start-color").val(), text: "Start "});
  });

  $("#add-stop").click(function () {
    addNote(boardId, "STOP", {id: uuid() + _TEMP, color: $("#stop-color").val(), text: "Stop "});
  });

  $("#add-continue").click(function () {
    addNote(boardId, "CONTINUE", {id: uuid() + _TEMP, color: $("#continue-color").val(), text: "Continue "});
  });

  function noteHtml(id, color, text) {

    return '<li id="' + id + '" server-id="' + id + '" style="background-color: ' + color + '">'
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

      COLUMNS['START'].empty();
      $(data.starts).each(function (index, data) {
        loadNote(boardId, 'START', data);
      });

      COLUMNS['STOP'].empty();
      $(data.stops).each(function (index, data) {
        loadNote(boardId, 'STOP', data);
      });

      COLUMNS['CONTINUE'].empty();
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

    loadNote(boardId, column, {id: note.id, color: note.color, text: note.text});
    scrollNote(note.id);
  }

  function loadNote(boardId, column, note) {

    COLUMNS[column].append(noteHtml(note.id, note.color, note.text));
    COLUMNS[column].on('change', '#' + note.id + ' > textarea', function () {

      var text = $("#" + note.id + " > textarea").val().trim();
      var serverId = $("#" + note.id).attr("server-id");

      if (text === "" && serverId.endsWith(_TEMP)) {

        $("#" + note.id).remove();

      } else if (text === "") {

        deleteNote(boardId, column, serverId).done(function (data) {

          $("li[server-id='" + data.id + "']").remove();
          sendEvent(boardId, column, {id: data.id, color: note.color, text: text});

        }).fail(handleFailure);

      } else if (serverId.endsWith(_TEMP)) {

        saveNote(boardId, column, {color: note.color, text: text}).done(function (data) {

          $("#" + note.id).attr("server-id", data.id);
          sendEvent(boardId, column, {id: data.id, color: note.color, text: text});

        }).fail(handleFailure);

      } else {

        updateNote(boardId, column, {id: serverId, text: text}).done(function (data) {

          sendEvent(boardId, column, {id: data.id, color: note.color, text: text});

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

    if (note.id && $("li[server-id='" + note.id + "']").length) {

      if (note.text) {
        $("li[server-id='" + note.id + "'] > textarea").val(note.text);
      } else {
        $("li[server-id='" + note.id + "']").remove();
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