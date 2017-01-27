function formatCodeBlocks() {

    $('pre code').each(function(index, block) {
        hljs.highlightBlock(block);
    });

    new Clipboard('[data-clipboard-snippet]',{
        target: function(trigger) {
            return trigger.nextElementSibling;
        }
    }).on('success', function(e) {
        e.clearSelection();
    });
}


$(function () {
    // Dom ready
    formatCodeBlocks();

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

            var selectRepo = $('#select-repo');

            selectRepo.fadeOut(100, function(){

                selectRepo.html(data);

                formatCodeBlocks();

                var slideDuration = 500;
                selectRepo.stop(true, true)
                    .fadeIn({duration: slideDuration, queue: false})
                    .css('display', 'none')
                    .slideDown(slideDuration);
            });
        });
    });

});