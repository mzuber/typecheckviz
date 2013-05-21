
function buildCarousel(irs)
{
   var carousel = $('.carousel-inner')
   var indicator = $('.carousel-indicators')
    
   for (var i = 0; i < irs.length; i++) {
       var step = (i==0) ? $('<div class ="item step active row">') :$('<div class ="item step row">') 
       var result = ""
       for (var key in irs[i].result) {
           result += key + " ~> "+irs[i].result[key]+"<br>";
       }
       var substitution = ""
       for (var key in irs[i].substitution) {
           substitution += key + " ~> "+irs[i].substitution[key]+"<br>";
       }
       var unsolved = "" 
       for (var key in irs[i].unsolved) {
           unsolved += irs[i].unsolved[key]+"<br>";
       }
       var content =
           '<div class="span2 offset2"><p>Unsolved:<br>'+unsolved+'</p></div>' +
           '<div class="span8"><p>Current constraint:<br>'+irs[i].current +'</p>'+
           '<p>Result:<br>'+result +'</p>'+
           '<p>Substitution:<br>'+substitution +'</p>'+
           '</div>'
                       
        
       step.html(content)
       carousel.append(step)

       var li = $('<li data-target="#myCarousel">').attr('data-slide-to',i)
       if(i==0) li.addClass('active')
       indicator.append(li)
   } 
}

function buildTable(t)
{
    var table = $('<table>');
    var r1 = $('<tr>');
    var r2 = $('<tr>');
    for (var i = 0; i < t.premises.length; i++)
    {
        var child =  $('<td>').append(buildTable(t.premises[i]));
        r1.append(child);
    }

    if(t.premises.length == 0)
    {
        $('<td>').appendTo(r1)
    }

    //rulename
    var name = $('<div data-toogle="tooltip" data-animation="true" title="'+t.constraints+'">').addClass('rulename').text(t.rulename);
    var context = ""
    for (var key in t.context) {
       context += key + " ~> "+t.context[key]+"<br>";
    }
    name.click(function(){
        $('#myModal').modal('show')
        $('.modal-body').html("Context:<br>"+context + "<br>Constraints:<br>"+t.constraints)

        
    })
    var namecell=$('<td>').attr('rowspan','2').appendTo(r1);
    namecell.append(name);
    //conclusion
    $('<td>').attr('colspan',t.premises.length).addClass('conc').text(t.conclusion).appendTo(r2);
    table.append(r1,r2);
    return table;

}




