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
            $('#gradleConfig').html(data);
        });
    });

});