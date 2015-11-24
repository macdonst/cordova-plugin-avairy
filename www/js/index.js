/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
        document.querySelector(".cameraClick").addEventListener("click", this.camera);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },
    camera: function() {

        var pictureSource = navigator.camera.PictureSourceType;
        var destinationType = navigator.camera.DestinationType;
        
        navigator.camera.getPicture(onSuccess, onFail, {quality: 100, destinationType: destinationType.FILE_URI, sourceType: pictureSource.PHOTOLIBRARY, correctOrientation: true});

        function onSuccess(imageURI) {
            console.log("image URI from Camera => " + imageURI);
            window.plugins.aviary.show(imageURI, "0", {
                success: function(result){

                    _fileSrc = result.output_path;
                    console.log("IMAGE FINAL => " + _fileSrc)

                    if (result.indexOf("content://") >= 0) {
                        window.FilePath.resolveNativePath(_fileSrc, 
                            function results(file) {
                                document.querySelector("#imageSrc").src = file;
                            },
                            function error(file) {
                                console.log("error");
                            }
                        );
                    } else {
                        document.querySelector("#imageSrc").src = _fileSrc;
                    }

                    
                },
                error: function(message){
                    console.log("ERROR => " + message);
                }
            });
        }

        function onFail(message) {
            alert('Failed because: ' + message);
        }
    }
};

app.initialize();