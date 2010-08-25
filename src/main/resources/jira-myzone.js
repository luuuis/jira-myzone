// create a MyZone object if it does not already exist.
if (!this.MyZone) {
    this.MyZone = {}
}

// create the methods in a closure to avoid creating global variables.
(function () {
    if (typeof MyZone.registerChangeListener !== 'function') {
        MyZone.registerChangeListener = function(selectId) {
            var selector = '#' + selectId
            var canonicalBaseUrl = (function(){
                var uri = AJS.parseUri(window.location);
                return uri.protocol + "://" + uri.authority;
            })();

            jQuery(selector).change(function() {
                jQuery(selector + ' option:selected').each(function() {
                    var selectedTZ = jQuery(this).val()

                    jQuery.ajax({
                        url: canonicalBaseUrl + contextPath + '/rest/myzone/1.0/prefs',
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

jQuery(document).ready(function() {
    var canonicalBaseUrl = (function(){
        var uri = AJS.parseUri(window.location);
        return uri.protocol + "://" + uri.authority;
    })();

    var now = new Date().getTime()
    var i = 0

    jQuery(".date").add(".attachment-date").each(function() {
        var dateNode = this
        var jQueryNode = jQuery(this)
        var jiraTime = dateNode.innerHTML
        var timeString = jQuery.trim(jiraTime)

        jQuery.ajax({
            url: canonicalBaseUrl + contextPath + '/rest/myzone/1.0/convert',
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
