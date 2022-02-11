/* global boardId */

"use strict";

var UUID = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";

function uuid() {

  var dt, uuid, r;

  dt = new Date().getTime();
  uuid = UUID.replace(/[xy]/g, function (c) {
    r = (dt + Math.random() * 16) % 16 | 0;
    dt = Math.floor(dt / 16);
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });

  return uuid;
}

if (!String.prototype.endsWith) {

  String.prototype.endsWith = function (search, this_len) {

    if (this_len === undefined || this_len > this.length) {
      this_len = this.length;
    }

    return this.substring(this_len - search.length, this_len) === search;
  };
}

$(function () {

  var _TEMP, COLUMNS, boardLocked, webSocket;

  _TEMP = "_temp";
  COLUMNS = {
    "START": $("#start-list"),
    "STOP": $("#stop-list"),
    "CONTINUE": $("#continue-list")
  };

  boardLocked = false;

  webSocket = createWebSocket(function () {
    startWebSocketPing(10 * 1000);
  });

  loadBoard(boardId);

  $("#add-start").click(function () {
    addStart(boardId);
  });

  $("#add-stop").click(function () {
    addStop(boardId);
  });

  $("#add-continue").click(function () {
    addContinue(boardId);
  });

  $("#lock").click(function () {
    lock(boardId);
  });

  function addStart(boardId) {

    addNote(boardId, "START", {
      id: uuid() + _TEMP,
      color: $("#start-color").val(),
      text: "Start "
    });
  }

  function addStop(boardId) {

    addNote(boardId, "STOP", {
      id: uuid() + _TEMP,
      color: $("#stop-color").val(),
      text: "Stop "
    });
  }

  function addContinue(boardId) {

    addNote(boardId, "CONTINUE", {
      id: uuid() + _TEMP,
      color: $("#continue-color").val(),
      text: "Continue "
    });
  }

  function addNote(boardId, column, note) {

    if (checkBoardLocked()) {
      return;
    }

    loadNote(boardId, column, {
      id: note.id,
      color: note.color,
      text: note.text
    });

    scrollNote(note.id);
  }

  function lock(boardId) {

    if (checkBoardLocked()) {
      return;
    }

    lockBoard(boardId).done(function (data) {

      setBoardLocked(true);
      sendEvent({
        boardId: boardId,
        locked: true
      });
    }).fail(handleFailure);
  }

  function loadBoard(boardId) {

    readBoard(boardId).done(function (data) {

      setBoardLocked(data.locked);

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

  function loadNote(boardId, column, note) {

    COLUMNS[column].append(noteHtml(note.id, note.color, note.text));
    COLUMNS[column].on('change', '#' + note.id + ' > textarea', function () {

      var text, serverId;

      text = $("#" + note.id + " > textarea").val().trim();
      serverId = $("#" + note.id).attr("server-id");

      if (text === "" && serverId.endsWith(_TEMP)) {
        onRemove(note);
      } else if (text === "") {
        onDelete(boardId, column, note, serverId, text);
      } else if (serverId.endsWith(_TEMP)) {
        onSave(boardId, column, note, text);
      } else {
        onUpdate(boardId, column, note, serverId, text);
      }
    });
  }

  function noteHtml(id, color, text) {

    return '<li id="' + id + '" server-id="' + id + '" style="background-color: ' + color + '">'
        + '  <textarea>' + text + '</textarea>'
        + '</li>';
  }

  function onRemove(note) {

    $("#" + note.id).remove();
  }

  function onDelete(boardId, column, note, serverId, text) {

    if (checkBoardLocked()) {
      return;
    }

    deleteNote(boardId, column, serverId).done(function (data) {

      $("li[server-id='" + data.id + "']").remove();

      sendEvent({
        boardId: boardId,
        column: column,
        note: {
          id: data.id,
          text: text
        }});

    }).fail(handleFailure);
  }

  function onSave(boardId, column, note, text) {

    if (checkBoardLocked()) {
      return;
    }

    saveNote(boardId, column, {color: note.color, text: text}).done(function (data) {

      $("#" + note.id).attr("server-id", data.id);

      sendEvent({
        boardId: boardId,
        column: column,
        note: {
          id: data.id,
          color: note.color,
          text: text
        }});

    }).fail(handleFailure);
  }

  function onUpdate(boardId, column, note, serverId, text) {

    if (checkBoardLocked()) {
      return;
    }

    updateNote(boardId, column, {id: serverId, text: text}).done(function (data) {

      sendEvent({
        boardId: boardId,
        column: column,
        note: {
          id: data.id,
          text: text
        }});

    }).fail(handleFailure);
  }

  function readBoard(boardId) {

    return $.ajax({
      url: "api/board/" + boardId,
      type: 'GET',
      contentType: 'application/json'
    });
  }

  function lockBoard(boardId) {

    return $.ajax({
      url: "api/board/" + boardId + "/lock",
      type: 'POST',
      contentType: 'application/json'
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

  function postEvent(boardId, event) {

    return $.ajax({
      url: "api/board/" + boardId + "/event",
      data: JSON.stringify(event),
      type: 'POST',
      contentType: 'application/json'
    });
  }

  function createWebSocket(callback) {

    var url, webSocket;

    url = "ws://" + window.location.host + window.location.pathname + "events/" + boardId;
    webSocket = new WebSocket(url);

    webSocket.onopen = function (event) {
      callback();
    };

    webSocket.onmessage = function (event) {
      handleEvent(JSON.parse(event.data));
    };

    webSocket.onerror = function (event) {
      console.error("WebSocket error:", event);
    };

    return webSocket;
  }

  function startWebSocketPing(delay) {

    setInterval(sendPing, delay);
  }

  function isWebSocketOpen(webSocket) {

    return webSocket.readyState === webSocket.OPEN;
  }

  function sendEvent(event) {

    var send, post;

    send = function () {
      webSocket.send(JSON.stringify(event));
    };

    post = function (boardId) {
      postEvent(boardId, {
        sessionId: '',
        data: JSON.stringify(event)
      }).fail(handleFailure);
    };

    if (isWebSocketOpen(webSocket)) {
      send();
    } else {
      post(boardId);
    }
  }

  function sendPing() {

    var send = function () {
      webSocket.send("ping");
    };

    if (isWebSocketOpen(webSocket)) {
      send();
    } else {
      webSocket = createWebSocket(function () {
        send();
        loadBoard(boardId);
      });
    }
  }

  function handleEvent(event) {

    if ('note' in event) {
      handleNoteEvent(event);
    } else if ('locked' in event) {
      handleLockedEvent(event);
    }
  }

  function handleNoteEvent(event) {

    if (event.note.id && $("li[server-id='" + event.note.id + "']").length) {
      if (event.note.text) {
        $("li[server-id='" + event.note.id + "'] > textarea").val(event.note.text);
      } else {
        $("li[server-id='" + event.note.id + "']").remove();
      }
    } else if (event.note.id && event.note.text) {
      loadNote(event.boardId, event.column, event.note);
    }
  }

  function handleLockedEvent(event) {

    if (!boardLocked && event.locked) {
      setBoardLocked(true);
    } else if (boardLocked && !event.locked) {
      setBoardLocked(false);
    }
  }

  function checkBoardLocked() {

    if (boardLocked) {
      handleFailure(null, null, "This board is locked for editing");
    }

    return boardLocked;
  }

  function setBoardLocked(locked) {

    boardLocked = locked;

    if (boardLocked) {
      $("#unlocked").hide();
      $("#locked").show();
    } else {
      $("#locked").hide();
      $("#unlocked").show();
    }
  }

  function handleFailure(jqXHR, textStatus, errorThrown) {

    var message;

    if (jqXHR) {
      message = jqXHR.responseText;
    }

    if (!message) {
      message = errorThrown;
    }

    showError(message);
  }

  function showError(message) {

    var error;

    if (!message) {
      message = "That didn't work";
    }

    $("#error textarea").val("ERROR\n\n" + message);

    error = $("#error");
    error.show();
    error.click(function () {
      error.hide();
    });
  }
});
