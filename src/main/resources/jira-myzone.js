// create a MyZone object if it does not already exist.
if (!this.MyZone) {
    this.MyZone = {}
}

// create the methods in a closure to avoid creating global variables.
(function () {
    if (typeof MyZone.registerChangeListener !== 'function') {
        MyZone.registerChangeListener = function(selectId) {
            var selector = '#' + selectId

            AJS.$(selector).change(function() {
                AJS.$(selector + ' option:selected').each(function() {
                    var selectedTZ = AJS.$(this).val()

                    AJS.$.ajax({
                        url: contextPath + '/rest/myzone/1.0/prefs',
                        type: 'PUT',
                        data: JSON.stringify({ tzId: selectedTZ }),
                        contentType: "application/json; charset=utf-8",
                        error: function() { alert('Error setting timezone') }
                    })
                })
            })
        }
    }
}());

AJS.$(document).ready(function() {
    var now = new Date().getTime()
    var i = 0

    AJS.$(".date, .attachment-date").each(function() {
        var dateNode = this
        var jQueryNode = jQuery(this)
        var jiraTime = dateNode.innerHTML
        var timeString = jQuery.trim(jiraTime)

        AJS.$.ajax({
            url: contextPath + '/rest/myzone/1.0/convert',
            type: 'POST',
            data: JSON.stringify({ time: timeString }),
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            success: function(converted) {
                if (converted.time != '') {
                    dateNode.innerHTML = converted.time

                    // add a pop-up with the original date
                    jQueryNode.css('border-bottom', 'dotted 1px #000000')
                    var draw = function(contents, trigger, showPopup) {
                        contents.empty()
                        contents.append(jiraTime)
                        showPopup()
                    }

                    var options = { onHover: true, showDelay: 400, hideDelay: 400, closeOthers: false, width: 120 }
                    AJS.InlineDialog(jQueryNode, "jira-myzone-" + i++, draw, options)
                }
            }
        });
    })

})
