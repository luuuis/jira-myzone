// create a MyZone object if it does not already exist.
if (!this.MyZone) {
    this.MyZone = {}
}

// create the methods in a closure to avoid creating global variables.
(function () {
    if (typeof MyZone.getTZ !== 'function') {
        // Based on a script by Josh Fraser
        // http://www.onlineaspect.com/2007/06/08/auto-detect-a-time-zone-with-javascript/
        MyZone.getTZ = function() {
            var rightNow = new Date();
            var jan1 = new Date(rightNow.getFullYear(), 0, 1, 0, 0, 0, 0);  // jan 1st
            var june1 = new Date(rightNow.getFullYear(), 6, 1, 0, 0, 0, 0); // june 1st
            var temp = jan1.toGMTString();
            var jan2 = new Date(temp.substring(0, temp.lastIndexOf(" ")-1));
            temp = june1.toGMTString();
            var june2 = new Date(temp.substring(0, temp.lastIndexOf(" ")-1));
            var std_time_offset = (jan1 - jan2) / (1000 * 60 * 60);
            var daylight_time_offset = (june1 - june2) / (1000 * 60 * 60);
            var dst;
            if (std_time_offset == daylight_time_offset) {
                dst = false; // daylight savings time is NOT observed
            } else {
                // positive is southern, negative is northern hemisphere
                var hemisphere = std_time_offset - daylight_time_offset;
                if (hemisphere >= 0)
                    std_time_offset = daylight_time_offset;
                dst = true; // daylight savings time is observed
            }

            var tz = {};
            tz["offset"] = std_time_offset;
            tz["dst"] = dst;

            return tz;
        }
    }
}());

jQuery(document).ready(function() {
    var canonicalBaseUrl = (function(){
        var uri = AJS.parseUri(window.location);
        return uri.protocol + "://" + uri.authority;
    })();

    var now = new Date().getTime()
    var tz = MyZone.getTZ()
    var i = 0

    jQuery(".date").add(".attachment-date").each(function() {
        var timeString = jQuery.trim(this.innerHTML)
        var draw = function(contents, trigger, showPopup) {
            // TODO (LGM) remove hardcoded URL
            jQuery.ajax({
                url: canonicalBaseUrl + contextPath + '/rest/myzone/1.0/convert',
                type: 'POST',
                data: JSON.stringify({ renderTime: now, offset: tz.offset, dst: tz.dst, time: timeString }),
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                success: function(converted) {
                    contents.empty()
                    contents.append(converted.time)
                    showPopup()
                }
            });
        }

        var options = { onHover: true, showDelay: 400, hideDelay: 400, closeOthers: false, width: 200 }
        AJS.InlineDialog(jQuery(this), "jira-myzone-" + i++, draw, options)
    })

    // update the prefs whenever the timezone is changed
    jQuery('#myzone-select-timezone').change(function() {
        jQuery('#myzone-select-timezone option:selected').each(function() {
            var selectedTZ = jQuery(this).val()

            jQuery.ajax({
                url: canonicalBaseUrl + contextPath + '/rest/myzone/1.0/prefs',
                type: 'PUT',
                data: JSON.stringify({ tzId: selectedTZ }),
                contentType: "application/json; charset=utf-8",
                success: function() { alert('Selected timezone: ' + selectedTZ) },
                error: function() { alert('Error setting timezone') }
            })
        })
    })

})
