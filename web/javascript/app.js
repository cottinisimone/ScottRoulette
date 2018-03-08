//////////////////////////////////////////////////////////////////////////////////////////
/**
 * Core functionalities
 */
var rtc = new ScRTC({
  wsUrl: getWebsocketUrl(),
  mediaTypes: {video: true, audio: true},
  peerConfig: {
    iceServers: [{urls: 'stun:' + location.host}],
    iceTransportPolicy:'all'
  },
  debug: true
});

/**
 * Callback. Called when user media are available. More in practice after you click 'allow' on
 *  browser's hardware access alert.
 */
rtc.onMediaAvailable = function(stream) {
  localVideo[0].srcObject = stream;
  localVideo[0].muted = true;
  localVideo.appendTo('#local-user');
  localVideo[0].onloadeddata = function() { onLoadedData(localVideo, stream) }
}

/**
 * Callback. Remote connection has been correctly established
 */
rtc.onRemoteConnection = function(stream) {
  remoteVideo[0].srcObject = stream;
  remoteVideo[0].muted = true;
  enableCommunications();
}

/**
 * Websocket send a nonsignal message. It should be handled by the application
 */
rtc.onMessage = function(message) {
  console.log('New message from server:', message)
}
//////////////////////////////////////////////////////////////////////////////////////////
var messagesContainer = $('#chat-messages');
var chatInput = $('#chat-text');

var startButton = $('#start');
var nextButton = $('#next');
var stopButton = $('#stop');
var sendButton = $('#send');

var localVideo = $('#video-local');
var remoteVideo = $('#video-remote');
/**
 * Creates an UUID like
 */
function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}
/**
 * Dynamically get the websocket url starting from location and replacing http(s) with ws(s)
 *  and appending chat at the end. 
 *  Eg. http://localhost/ -> ws://localhost/chat
 */
function getWebsocketUrl() {
  return location.href.replace('http', 'ws') + 'chat';
}
/**
 * Stretch some elements to fit the correct dimensions (sorry, not a FE developer)
 */
function onLoadedData(localVideo, stream) {
  remoteVideo.width(localVideo.width());
  remoteVideo.height(localVideo.height());
  rendertext('Welcome to Rockroulette and thank you for joining the site!');
  registerCallbacks();
}
/**
 * Some useful callbacks to interact with UI
 */
function registerCallbacks() {
  // Start session button click callback
  startButton.on('click', function(event) {
    rtc.ready();
    startButton.addClass('disabled');
  })
  // Next user button click callback
  nextButton.on('click', function(event) {
    
  });
  // Send text button click callback
  sendButton.on('click', function(event) {
    sendtext();
  });
  // Enter key on chat text input to send without pressing 'Send' button
  chatInput.on('keyup', function (event) {
    if (event.keyCode == 13) {
      sendtext();
      chatInput.val('');
    }
  });
}
/** 
 * 
 */
function enableCommunications() {
  // Started. Button will be hidden
  startButton.css('display', 'none');
  // Showing next button
  nextButton.css('display', '');
  // Showing stop button and enable the click event to stop the communication
  stopButton.css('display', '');
  // Enabling button to send text
  sendButton.removeClass('disabled');
  // Enabling message input
  chatInput.removeAttr('disabled');
  // Stop button fires Start button to be displayed. Meanwhile next and stop becomes
  //  hidden and the Send button and message input will be disabled
  stopButton.on('click', function(event) {
    startButton.css('display', '');
    startButton.removeClass('disabled');
    nextButton.css('display', 'none');
    stopButton.css('display', 'none');
    sendButton.addClass('disabled');
    chatInput.attr('disabled');
    rtc.stop();
  })
}
/** 
 * Used to send text message to partner
 */
function sendtext() {
  var text = chatInput.val();
  if (text) {
    rtc.send(text);
    rendertext(text, true);
  }
}
/**
 * 
 */
function rendertext(text, you) {
  if (text) messagesContainer.append(text + "\n\n");
  if (you) messagesContainer.append('you > ');
}
