const wClient = new WebSocket("ws://localhost:8080/echo")

wClient.onopen = function () {
    console.log("established")
}

wClient.onclose = function () {
    console.log("closed")
}

wClient.onmessage = function (event) {
    let data = JSON.parse(event.data)
    $("#greetings").append("<tr><td>" + data.userName + ": " + data.message + "</td></tr>");
}

function sendName() {
    let payload = {
        "action": "send",
        "data": JSON.stringify({
            "message": $("#message").val()
        })
    };
    wClient.send(JSON.stringify(payload));
}

function connect() {
    let payload = {
        "action": "login",
        "data": JSON.stringify({
            "topic": $("#topic").val(),
            "name": $("#name").val()
        })
    };
    wClient.send(JSON.stringify(payload));
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $("#connect").click(() => connect());
    $("#send").click(() => sendName());
});