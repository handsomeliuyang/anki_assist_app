//var resizeDone = false;

/*  Call displayCardAnswer() and answerCard() from anki deck template using javascript
 *  See also AbstractFlashcardViewer.
 */
//function showAnswer() {
//    window.location.href = "signal:show_answer";
//}
//function buttonAnswerEase1() {
//    window.location.href = "signal:answer_ease1";
//}
//function buttonAnswerEase2() {
//    window.location.href = "signal:answer_ease2";
//}
//function buttonAnswerEase3() {
//    window.location.href = "signal:answer_ease3";
//}
//function buttonAnswerEase4() {
//    window.location.href = "signal:answer_ease4";
//}
// Show options menu
//function ankiShowOptionsMenu() {
//    window.location.href = "signal:anki_show_options_menu";
//}

// Show Navigation Drawer
//function ankiShowNavDrawer() {
//    window.location.href = "signal:anki_show_navigation_drawer";
//}

/* Reload card.html */
//function reloadPage() {
//    window.location.href = "signal:reload_card_html";
//}

// Mark current card
//function ankiMarkCard() {
//    window.location.href = "signal:mark_current_card";
//}

/* Toggle flag on card from AnkiDroid Webview using JavaScript
    Possible values: "none", "red", "orange", "green", "blue"
    See AnkiDroid Manual for Usage
*/
//function ankiToggleFlag(flag) {
//    var flagVal = Number.isInteger(flag);
//
//    if (flagVal) {
//        switch (flag) {
//            case 0: window.location.href = "signal:flag_none"; break;
//            case 1: window.location.href = "signal:flag_red"; break;
//            case 2: window.location.href = "signal:flag_orange"; break;
//            case 3: window.location.href = "signal:flag_green"; break;
//            case 4: window.location.href = "signal:flag_blue"; break;
//            default: console.log('No Flag Found'); break;
//        }
//    } else {
//        window.location.href = "signal:flag_" + flag;
//    }
//}

// Show toast using js
//function ankiShowToast(message) {
//    var msg = encodeURI(message);
//    window.location.href = "signal:anki_show_toast:" + msg;
//}

/* Tell the app the text in the input box when it loses focus */
//function taBlur(itag) {
//    //#5944 - percent wasn't encoded, but Mandarin was.
//    var encodedVal = encodeURI(itag.value);
//    window.location.href = "typeblurtext:" + encodedVal;
//}

/* Look at the text entered into the input box and send the text on a return */
//function taKey(itag, e) {
//    var keycode;
//    if (window.event) {
//        keycode = window.event.keyCode;
//    } else if (e) {
//        keycode = e.which;
//    } else {
//        return true;
//    }
//
//    if (keycode == 13) {
//        //#5944 - percent wasn't encoded, but Mandarin was.
//        var encodedVal = encodeURI(itag.value);
//        window.location.href = "typeentertext:" + encodedVal;
//        return false;
//    } else {
//        return true;
//    }
//}

//window.onload = function() {
//    /* If the WebView loads too early on Android <= 4.3 (which happens
//       on the first card or regularly with WebView switching enabled),
//       the window dimensions returned to us will be default built-in
//       values. In this case, issuing a scroll event will force the
//       browser to recalculate the dimensions and give us the correct
//       values, so we do this every time. This lets us resize images
//       correctly. */
//   console.log("liuyang resizeImages");
//    window.scrollTo(0,0);
//    resizeImages();
//    window.location.href = "#answer";
//};

var onPageFinished = function() {
    console.log("liuyang onPageFinished")
//    if (!resizeDone) {
//        resizeImages();
//        /* Re-anchor to answer after image resize since the point changes */
//        window.location.href = "#answer";
//    }
    if (window.MathJax != null) {
        var card = document.querySelector('.card');
        /* Anki-Android adds mathjax-needs-to-render" as a class to the card when
           it detects both \( and \) or \[ and \].

           This does not control *loading* MathJax, but rather controls whether or not MathJax
           renders content.  We hide all the content until MathJax renders, because otherwise
           the content loads, and has to reflow after MathJax renders, and it's unsightly.
           However, if we hide all the content every time, folks don't like the repainting after
           every question or answer.  This is a middleground, where there is no repainting due to
           MathJax on non-MathJax cards, and on MathJax cards, there is a small flicker, but there's
           no reflowing because the content only shows after MathJax has rendered. */

        if (card.classList.contains("mathjax-needs-to-render"))
        {
            MathJax.Hub.Queue(['Typeset', MathJax.Hub, card]);
            MathJax.Hub.Queue(function () {
                card.classList.remove("mathjax-needs-to-render");
                card.classList.add("mathjax-rendered");
            });
        }
    }
}
