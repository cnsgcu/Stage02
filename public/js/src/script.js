$(document).ready(function() {
    $('button').click(function() {
        $.ajax({
            url: "nlp",
            type: "POST",
            contentType: "text/json",
            data: JSON.stringify({movie: "Hello World"}),
            success: function(resp) {
                console.log(JSON.stringify(resp, null, 2));
            }
        })
    });
});