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
                        success: function() { alert('Selected timezone: ' + selectedTZ) },
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
        var timeString = jQuery.trim(this.innerHTML)
        var draw = function(contents, trigger, showPopup) {
            jQuery.ajax({
                url: canonicalBaseUrl + contextPath + '/rest/myzone/1.0/convert',
                type: 'POST',
                data: JSON.stringify({ time: timeString }),
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

})
