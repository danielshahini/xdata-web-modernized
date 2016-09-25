$(document).ready(function () {
    var d = new Date();
    //d.getMonth()+1 --> +1 because index starts from 0
    today = d.getMonth() + 1 + '/' + d.getDate() + '/' + d.getFullYear();
    //alert today's date
    alert(today);
    //alert today's timestamp without time consideration
    alert(new Date(today).getTime());
    //alert today's datetime
    alert(d);
    //alert today's timesatmp with taking time into consideration
    alert(new Date(d).getTime());
    //date from data base
    arrDate = ["27", "06", "2013"];
    //spliting date to new fromat mm/dd/yyyy
    var newDate = arrDate[1] + "/" + arrDate[0] + "/" + arrDate[2];
    //alert db date
    alert(newDate);
    //alert db date timestamp
    alert(new Date(newDate).getTime());

    //comapring db date and today date
    //use d if you want to conside time also in today's date instead of today
    if (new Date(newDate).getTime() < new Date(today).getTime()) {
        $('#test').hide();
    }
});
