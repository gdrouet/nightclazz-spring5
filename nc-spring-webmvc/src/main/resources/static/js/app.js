var app = (function() {
    const apiUrl = 'https://localhost:9443';
    const drawingAreaX = 111;
    const drawingAreaY = 11;
    const drawingAreaWidth = 267;
    const drawingAreaHeight = 200;
    const copyCanvas = document.createElement('canvas');
    const copyContext = copyCanvas.getContext("2d");
    copyContext.canvas.width = drawingAreaWidth;
    copyContext.canvas.height = drawingAreaHeight;
    var canvas;
    const catalog = $('#catalog');
    const view = $('#view');
    const name = $('#name');

    var evtSource = new EventSource(apiUrl + "/drawings");

    evtSource.onmessage = function (e) {
        var data = JSON.parse(e.data);

        var img = new Image();
        img.id = data.id;
        img.src = "/drawing/" + img.id;
        img.className = "minimize";

        catalog.prepend(img);

        $("#" + data.id).hover(function(){
            view.html("<img src='" + img.src + "'><span class='help-block'>&nbsp;Author: " + data.author + "</span>");
            view.show();
        }, function(){
        });
    };

    function add() {
        const author = name.val();
        if (author.trim() === '') {
            alert('Enter your name first!');
            return
        }

        copyContext.drawImage(canvas,
            drawingAreaX, drawingAreaY, drawingAreaWidth, drawingAreaHeight,
            0, 0, copyContext.canvas.width, copyContext.canvas.height);

        var base64Image = copyCanvas.toDataURL("image/png");
        var req = new XMLHttpRequest();

        req.onreadystatechange = function (event) {
            // XMLHttpRequest.DONE === 4
            if (this.readyState === XMLHttpRequest.DONE) {
                if (this.status === 200) {
                    console.log("Réponse reçu: %s", this.responseText);
                } else {
                    console.log("Status de la réponse: %d (%s)", this.status, this.statusText);
                }
            }
        };

        req.open('POST', apiUrl + '/drawing', true);
        req.setRequestHeader("Content-Type", "application/json");
        req.send(JSON.stringify({author: author, base64Image: base64Image.substring(base64Image.indexOf(',') + 1)}));
    }

    function start(panel) {
        if (panel.hidden) {
            panel.hidden = false;
            $.getScript('/js/html5-canvas-drawing-app.js', function () {
                canvas = drawingApp.init();
            });
        }
    }

    return {
        add : add,
        start : start
    };
})();