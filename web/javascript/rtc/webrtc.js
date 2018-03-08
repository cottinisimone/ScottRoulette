'use-strict';
/**
 * 
 */
////////////////////////////////////////////////////////////////////////////////////////////////////////
var ScRTC = function(config) {
  var Self = this;
  var local = undefined;
  var connection = undefined;
  var websocket = new WebSocket(config.wsUrl);
  this.debug = !!config.debug;

  // Signals
  var signals = {};
  var READY = 'ready';
  var OFFER = 'offer';
  var ANSWER = 'answer';
  var CANDIDATE = 'candidate';
  signals[OFFER] = onoffer;
  signals[ANSWER] = onanswer;
  signals[CANDIDATE] = oncandidate;
  signals['online'] = online;
  signals['paired'] = offer;
  signals['error'] = onerror;

  /*********************************************
   * Flow methods
   */
   /** > Every user
    * When websocket is instantiated onconnect is called. New `getUserMedia` request and attach the
    *   stream to html video element. Then UI callback is called
    * @param {*} message
    */
  function online(message) {
    Self.log('RTC | getUserMedia');
    navigator.mediaDevices.getUserMedia(config.mediaTypes).then(function(stream) {
      local = stream;
      Self.onMediaAvailable(stream);
    }).catch(function(exception) {
      error(exception);
    });
  }
  /** > Caller
   */
  function offer() {
    Self.log('RTC | Offer => out');
    getPeerConnection().addStream(local);
    getPeerConnection().createOffer({offerToReceiveAudio: 1, offerToReceiveVideo: 1}).then(function(desc) {
      getPeerConnection().setLocalDescription(desc).then(function() {
        sendSignal(new Signal(OFFER, desc))
      })
    });
  }
  /** > Called
   * @param {*} signal 
   */
  function onoffer(signal) {
    Self.log('RTC | Offer => in');
    getPeerConnection()
      .setRemoteDescription(new RTCSessionDescription(signal.payload))
      .then(answer);
  }
  /** > Called
   * @param {*} to 
   */
  function answer() {
    Self.log('RTC | Answer => out');
    getPeerConnection().addStream(local);
    getPeerConnection().createAnswer().then(function(desc) {
      getPeerConnection().setLocalDescription(desc).then(function() {
        sendSignal(new Signal(ANSWER, desc))
      });
    });
  } 
  /** > Caller
   * @param {*} signal 
   */
  function onanswer(signal) {
    Self.log('RTC | Answer => in');
    getPeerConnection().remote = true;
    getPeerConnection().setRemoteDescription(new RTCSessionDescription(signal.payload))
  }
  /** > Every user
   * 
   * @param {*} signal 
   */
  function oncandidate(signal) {
    if (signal.payload) {
      getPeerConnection().addIceCandidate(new RTCIceCandidate(signal.payload));
    }
  }
  /** 
   * 
   */
  function getPeerConnection() {
    if (!connection) {
      Self.log('RTC | RTCPeerConnection');
      conn = new RTCPeerConnection(config.peerConfig);
      conn.onaddstream = function(event) { Self.onRemoteConnection(event.stream) }
      conn.onicecandidate = function(event) {
        handle(event);
        function handle(event) {
          (conn.signalingState || conn.readyState) == 'stable' &&  conn.remote == true && event.candidate ?
              sendSignal(new Signal(CANDIDATE, event.candidate)) :
              setTimeout(function() { handle(event); }, 500);
        }
      }
      connection = conn;
    }
    return connection;
  }
  /**
   * @param {*} data 
   */
  function error(data) {
    console.error(data);
    Self.onError(data);
  }
  /*********************************************
   * Exposed apis
   */
  this.ready = function() { sendSignal(new Signal(READY))}
  /** > Every user
   * Public method to send custom messages (for example text messages)
   */
  this.send = function(message) { websocket.send(message) }
  /** > Every user
   * 
   */
  this.stop = function() { websocket.send() }
  /*********************************************
   * Web socket methods
   */
  function sendSignal(signal) {
    Self.log('WS  | Send =>', signal.signal);
    websocket.send(signal);
  }

  websocket.onopen = function() {
    Self.log('WS  | Opened');
  }

  websocket.onmessage = function(event) {
    var message = JSON.parse(event.data)
    if (Signal.isSignal(message) && signals[message.signal]) {
      Self.log('WS  | Recv =>', message.signal);
      signals[message.signal](new Signal(message.signal, message.payload))
    }
    else Self.onMessage(message);
  }

  websocket.onclose = function(event) {
    Self.log('WS  | Closed')
    this.close()
  }
 
  websocket.onerror = function(event) {
    Self.error(event)
  }

  /** 
   * Signal message class
   * @param {*} signal
   * @param {*} payload
   */
  var Signal = function(signal, payload) {
    this.signal = signal;
    this.payload = payload || {};
  }
  Signal.prototype.toString = function() { return JSON.stringify(this) }
  Signal.isSignal = function(object) { return object.signal && object.payload }
}

////////////////////////////////////////////////////////////////////////////////
// TO OVERRIDE
/**
 * Hook called when websocket successfully connected
 */
ScRTC.prototype.onMediaAvailable = function(stream) {
  this.log('Override onMediaAvailable method');
}

ScRTC.prototype.onRemoteConnection = function(stream) {
  this.log('Override onRemoteConnection method')
}

ScRTC.prototype.onMessage = function(data) {
  this.log('Override onMessage method', data)
}

ScRTC.prototype.onError = function(data) {
  this.log('Override onError method', data)
}

ScRTC.prototype.log = function(message, arg) {
  if (this.debug)
    arg ? console.log(Date.now() + ' | ' + message, arg) : console.log(Date.now() + ' | ' + message);
}