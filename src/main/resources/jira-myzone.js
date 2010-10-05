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
                        data: selectedTZ,
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
                    // write the converted date and underline it
                    dateNode.innerHTML = converted.time
                    jQueryNode.wrapInner('<span style="border-bottom: dotted 1px #bebebe;">')

                    // add a pop-up with the original date
                    var draw = function(contents, trigger, showPopup) {
                        contents.empty()
                        contents.append(AJS.format('{0}: {1}', converted.label, timeString))
                        showPopup()
                    }

                    var options = { onHover: true, showDelay: 400, hideDelay: 400, closeOthers: false, width: 200 }
                    AJS.InlineDialog(jQueryNode, "jira-myzone-" + i++, draw, options)
                }
            }
        });
    })

})
