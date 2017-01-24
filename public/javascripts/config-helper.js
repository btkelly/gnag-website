
$(function () {
    // Dom ready
    $("#tabs").tabs();


    $('pre code').each(function(index, block) {
        hljs.highlightBlock(block);
    });

    // Load project list to dropdown
    $.getJSON("/loadProjects", function(data) {

        var projectSelector = $("#combobox");
        $.each(data, function(i, item) {
            projectSelector.append("<option value='"+item.fullName+"'>" + item.fullName + "</option>");
        });
    });

    //Update Gradle configuration section based on project selection
    $("#combobox").change(function() {
        $.get("/configForSlug?slug=" + this.value, function(data) {

            var projectConfig = $('#projectConfig')

            var animate = !$.trim(projectConfig.html())

            projectConfig.html(data);

            $('pre code').each(function(index, block) {
                hljs.highlightBlock(block);
            });

            var clipboardSnippets = new Clipboard('[data-clipboard-snippet]',{
                target: function(trigger) {
                    return trigger.nextElementSibling;
                }
            });
            clipboardSnippets.on('success', function(e) {
                e.clearSelection();
            });

            if (animate) {
                var slideDuration = 200;
                projectConfig.stop(true, true)
                    .fadeIn({duration: slideDuration, queue: false})
                    .css('display', 'none')
                    .slideDown(slideDuration);
            }
        });
    });

});