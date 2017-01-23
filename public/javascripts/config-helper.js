
$(function () {
    // Dom ready

    // Load project list to dropdown
    $.getJSON("/loadProjects", function(data) {

        var projectSelector = $("#projectSelector");
        projectSelector.html("<option selected disabled>Select project to load your Gradle configuration.</option>");
        $.each(data, function(i, item) {
            projectSelector.append("<option value='"+item.fullName+"'>" + item.name + "</option>");
        });
    });

    //Update Gradle configuration section based on project selection
    $("#projectSelector").change(function() {
        $.get("/configForSlug?slug=" + this.value, function(data) {

            $('#projectConfig').html(data);

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

        });
    });

});