/*
 * Copyright 2023 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function DashboardPeriodicalExecutor(url, pause_condition) {
  this.setUrl(url);
  this.sequenceNumber = 0;
  this.frequency = 5000; //milli-seconds;
  this.observers = [];
  this.is_paused = false;
  this.pause_condition = pause_condition;
}

DashboardPeriodicalExecutor.prototype = {
  start: function() {
    if (!this.is_paused) {
      this.onRequest();
    }
  },
  stop: function() {
    clearTimeout(this.timer);
  },
  redirectToLoginPage: function(){
    window.location= window.location.protocol + '//' + window.location.host + context_repath('auth/login');
  },

  /*
     * We should use fireNow function trigger a manual request.
     * So we should call start() first. And when we can't wait for next automatically request,
     * then, we can use this fireNow method.
     */
  fireNow : function() {
    clearTimeout(this.timer);
    if (this.ongoingRequest && this.ongoingRequest.transport) {
      try {
        this.ongoingRequest.transport.abort();
      } catch(e) {}
    }
    this.start();
  },

  onRequest: function() {
    var executer = this;
    var requestSequenceNumber = this.generateSequenceNumber();
    this.ongoingRequest = jQuery.ajax({
      url: executer.url,
      dataType: "json",
      success: function(json_array) {
        executer._loop_observers(json_array, requestSequenceNumber);
        if (executer.pause_condition && executer.pause_condition(json_array)) {
          executer.is_paused = true;
        }
      },
      error: function(jqXHR, textStatus) {
      },
      complete : function() {
        //makes sure only 1 timer in this executor
        clearTimeout(executer.timer);
        delete executer.timer;

        if (!executer.is_paused) {
          executer.timer = setTimeout(executer.onRequest.bind(executer), executer.frequency);
        }
        //avoid memory leak
        executer = null;
        requestSequenceNumber = null;
      },
      statusCode: {
        401: function () {
          executer.redirectToLoginPage();
        }
      }
    });

  },
  _loop_observers : function(json, requestSequenceNumber) {
    if (json.error) {
      return;
    }

    for (var index = 0; index < this.observers.length; index++) {
      var observer = this.observers[index];

      if (!observer.notify) return;

      if (observer.dropExpiredCallback && !this.isSequenceNumberValid(requestSequenceNumber)) {
        //this request will be dropped, because it's expired
        return;
      }

      if (!this.is_paused) {
        observer.notify(json);
      }
    }
  },
  register : function() {
    for (var i = 0; i < arguments.length; i++) {
      this.observers.push(arguments[i]);
    }
  },
  unregister : function(observer) {
    this.observers = $A(this.observers);
    var position = this.observers.indexOf(observer);
    this.observers[position] = null;
    this.observers = this.observers.compact();
  },
  clean : function() {
    this.observers = [];
    this.is_paused = false;
  },
  pause : function() {
    this.is_paused = true;
  },
  resume: function() {
    this.is_paused = false;
  },
  generateSequenceNumber : function() {
    this.sequenceNumber++;
    return this.sequenceNumber;
  },
  isSequenceNumberValid : function(request_sequence_number){
    return this.sequenceNumber == request_sequence_number;
  },
  setUrl: function(url){
    try{
      this.url = context_path(url);
    } catch(e){
      this.url = '/go/' + url;
    }
  }
};
