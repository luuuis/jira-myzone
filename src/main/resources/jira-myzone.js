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
    if (typeof MyZone.convertDates !== 'function') {
        MyZone.convertDates = function($dates) {
            var serverTimes = []
            var timesOnPage = {}
            AJS.$.each($dates, function(i, $date) {
                var serverTime = AJS.$.trim($date.text());

                if (serverTime.length > 0) {
                    // lazily initiliase back references to jQuery node
                    var timeNodes = timesOnPage[serverTime]
                    if (timeNodes == null) {
                        timesOnPage[serverTime] = timeNodes = []
                    }

                    // back-reference to jQuery node
                    timeNodes.push($date)
                    serverTimes.push(serverTime)
                }
            })

            if (serverTimes.length > 0) {
                AJS.$.ajax({
                    url: contextPath + '/rest/myzone/1.0/convert',
                    type: 'POST',
                    data: JSON.stringify({ times: serverTimes }),
                    dataType: 'json',
                    contentType: "application/json; charset=utf-8",
                    global: false,
                    success: function(reply) {
                        AJS.$.each(reply.times, function(serverTime, localTime) {
                            if (localTime && localTime != '') {
                                // write the converted date and underline it
                                var $dates = timesOnPage[serverTime]
                                AJS.$.each($dates, function(i, $date) {
                                    $date.html(AJS.format('<span style="border-bottom: 1px dotted #bebebe;">{0}</span>', localTime))

                                    // add a pop-up with the original date
                                    var draw = function(contents, trigger, showPopup) {
                                        contents.empty()
                                        contents.append(AJS.format('{0}: {1}', reply.label, serverTime))
                                        showPopup()
                                    }

                                    var options = { onHover: true, showDelay: 400, hideDelay: 400, closeOthers: false, width: 200 }
                                    AJS.InlineDialog($date, "jira-myzone-" + popupSequence++, draw, options)
                                })
                            }
                        })
                    }
                });
            }
        }
    }

}())

AJS.$(document).ready(function() {

    // convert all dates found on the page
    MyZone.convertDates(AJS.$.map(AJS.$(".date, .attachment-date"), function(elem, i) {
        return AJS.$(elem)
    }))

})
