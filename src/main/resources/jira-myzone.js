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
                dst = "0"; // daylight savings time is NOT observed
            } else {
                // positive is southern, negative is northern hemisphere
                var hemisphere = std_time_offset - daylight_time_offset;
                if (hemisphere >= 0)
                    std_time_offset = daylight_time_offset;
                dst = "1"; // daylight savings time is observed
            }

            var tz = {};
            tz["offset"] = std_time_offset;
            tz["dst"] = dst;

            return tz;
        }
    }
}());

jQuery(document).ready(function() {
    var tz = MyZone.getTZ()
    var i = new Date().getTime()
    jQuery("dd.date").each(function() {
        var timeString = this.innerHTML
        var draw = function(contents, trigger, showPopup) {
            var time = {}
            time.offset = tz.offset
            time.dst = tz.dst
            time.time = timeString
            
            // TODO (LGM) remove hardcoded URL
            jQuery.ajax({
                url: 'http://localhost:8090/jira/rest/myzone/1.0/convert',
                type: 'POST',
                data: JSON.stringify({ offset: tz.offset, dst: tz.dst, time: timeString }),
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                success: function(convertedTime) {
                    alert(convertedTime.time)
                    //contents.empty()
                    //contents.append(" ==> ")
                    //contents.append(converted)
                }
            });
        }

        var options = { cacheContent: true, onHover: true, showDelay: 400, hideDelay: 400, closeOthers: false, width: 500 }
        AJS.InlineDialog(jQuery(this), "myzone-" + (i++), draw, options)
    })

    /*
    var i=new Date().getTime()
    jQuery("#issue_actions_container").find('.action-body a').each(function() {
        if (this.href.match(/\/browse\/[A-Z]+\-\d+$/)) {
            var split = this.href.split('/browse/')
            var base = split[0]
            var key = split[1]
            var options = { cacheContent: true, onHover: true, showDelay: 400, hideDelay: 400, closeOthers: false, width: 500 }
            var draw = function(contents, trigger, showPopup) {
                jQuery.getJSON(base + '/rest/api/2.0/issue/' + key + "?expand=fields", function(data) {
                    var fields = data["fields"]
                    contents.empty()
                    contents.append(
                        "<ul class=\"item-details\">"
                        + "<li>"
                        + "<dl><dt>Summary: </dt>" + "<dd>" + fields["summary"] + "</dd></dl>"
                        + "</li></ul>")
                    contents.append("<form id=\"add-watch\" name=\"watch\" action=\"\">")
                    jQuery("<input type=\"button\" name=\"button\" value=\"Watch\"/>").click(function() {
                        jQuery.ajax({ type: "POST", url: base + "/rest/api/2.0/issue/" + key + "/watchers", dataType: "json"})
                    }).appendTo(contents)
                    contents.append("</form>")
                    showPopup()
                })
            }
            AJS.InlineDialog(jQuery(this), "issue-linking-" + (i++), draw, options)
        }
    })
    */
})
