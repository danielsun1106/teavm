/*
 *  Copyright 2017 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

"use strict";

function tryConnect() {
    let ws = new WebSocket("ws://localhost:9090");

    ws.onopen = () => {
        dump("Connected established\n");
        listen(ws);
    };

    ws.onclose = () => {
        ws.close();
        setTimeout(() => {
            tryConnect();
        }, 500);
    };
}

function listen(ws) {
    ws.onmessage = (event) => {
        let resultConsumer = [];
        let request = JSON.parse(event.data);
        dump("Request #" + request.id + " received\n");
        runTests(request.tests, resultConsumer, 0, () => {
            dump("Sending response #" + request.id + "\n");
            ws.send(JSON.stringify({
                id: request.id,
                result: resultConsumer
            }));
        });
    }
}

function runTests(tests, consumer, index, callback) {
    if (index === tests.length) {
        callback();
    } else {
        let test = tests[index];
        runSingleTest(test, result => {
            consumer.push(result);
            runTests(tests, consumer, index + 1, callback);
        });
    }
}

function runSingleTest(test, callback) {
    dump("Running test " + test.name + " consisting of " + test.files + "\n");
    let iframe = document.getElementById("test");
    let handshakeListener = () => {
        dump("Handshake message received from frame\n");
        window.removeEventListener("message", handshakeListener);

        let listener = event => {
            dump("Test result message received from frame\n");
            window.removeEventListener("message", listener);
            callback(event.data);
        };
        window.addEventListener("message", listener);

        iframe.contentWindow.postMessage(test, "*");
    };
    window.addEventListener("message", handshakeListener);
    iframe.src = "about:blank";
    iframe.src = "frame.html";
}

tryConnect();