// create a MyZone object if it does not already exist.
if (!this.MyZone) {
    this.MyZone = {}
}

// create the methods in a closure to avoid creating global variables.
(function () {
    /**
     * Counter for generating unique popup id's.
     */
    var popupSequence = 0
    
    /**
     * Registers a change listener for the <select> box in the profile panel.
     */
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

    /**
     * Converts a date node to the user's preferred time zone.
     */
    if (typeof MyZone.convertDate !== 'function') {
        MyZone.convertDate = function($date) {
            var serverTimeString = AJS.$.trim($date.text())

            AJS.$.ajax({
                url: contextPath + '/rest/myzone/1.0/convert',
                type: 'POST',
                data: JSON.stringify({ time: serverTimeString }),
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                success: function(reply) {
                    if (reply.time != '') {
                        // write the converted date and underline it
                        $date.html(AJS.format('<span style="border-bottom: 1px dotted #bebebe;">{0}</span>', reply.time))

                        // add a pop-up with the original date
                        var draw = function(contents, trigger, showPopup) {
                            contents.empty()
                            contents.append(AJS.format('{0}: {1}', reply.label, serverTimeString))
                            showPopup()
                        }

                        var options = { onHover: true, showDelay: 400, hideDelay: 400, closeOthers: false, width: 200 }
                        AJS.InlineDialog($date, "jira-myzone-" + popupSequence++, draw, options)
                    }
                }
            });
        }
    }

}())

AJS.$(document).ready(function() {

    // convert all dates found on the page
    AJS.$(".date, .attachment-date").each(function() {
        MyZone.convertDate(AJS.$(this))
    })

})
