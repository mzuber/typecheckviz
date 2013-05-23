
function eval(s)
{
    $.ajax({
        url:'/eval',
        contentType: 'text/plain',
        type : 'POST',
        data: s
    }).done( function (data)
             {
                 $('.carousel-inner').html('')
                 $('.carousel-indicators').html('')
                 $('#unsolved').html('')
                 $( "#error" ).html('').hide()
                 $('#tree').html(buildTable(data.tree,0));
                 displayCarousel(data.solverSteps)
                 displayResult(s,data)
             })
        .fail( function (data) {
            showError(s,data.responseText)
            $('.carousel-inner').html('')
            $('.carousel-indicators').html('')
            $('#result').html('')
            $('#unsolved').html('')
        })
}

$(document).ready(function(){
    $("#form").submit(function(){
        eval($('#eval').val())
        return false;  
    });
});

$(document).ready(function ($) {
    $('#tabs').tab();
    $('.carousel').carousel({
        interval: false 
    });


    $('.carousel').on('slide',function(e){
        var index = $(e.relatedTarget).index();
        $('.unsolved').hide()
        $('#unsolved-'+index).show()
    });
    
}); 

$(document).tooltip({
    selector: '.rulename'
});

function displayResult(expr, d){
    if(d.result)
        showResult(expr,d.result)
    else if(d.error)
        showError(expr,d.error)
    else
        showError(expr,"missing messages")
}
function showResult(expr, r){
    $("#error").hide()
    $("#result").html('<pre><b>Result</b> for Expression '+expr+' : <br>'+r+'</pre>').show()
}

function showError(expr,e){
    $("#result").hide()
    $("#error").html('<b>Error</b> for Expression '+expr+' : <br>'+e).show()
}


function displayCarousel(irs)
{
    var carousel = $('.carousel-inner')
    var indicator = $('.carousel-indicators')
    
    for (var i = 0; i < irs.length; i++) {
        var step = (i==0) ? $('<div class ="item step active row">') :$('<div class ="item step row">') 
        var result = ""
        if(typeof irs[i].result == 'string')
        {
            result = irs[i].result
        }
        else
        {
            for (var key in irs[i].result) {
                result += key + " ~> "+irs[i].result[key]+"<br>";
            }
        }
        var substitution = ""
        for (var key in irs[i].substitution) {
            substitution += key + " ~> "+irs[i].substitution[key]+"<br>";
        }
        var content =
            '<div class="text-center"><p><b>Current constraint:</b><br>'+irs[i].current +'</p>'+
            '<p><b>Result:</b><br>'+result +'</p>'+
            '<p><b>Substitution:</b><br>'+substitution +'</p>'+
            '</div>'
        
        var unsolved = $("<ul>") 
        for (var key in irs[i].unsolved) {
            unsolved.append($('<li>').html(irs[i].unsolved[key]));
        }
        
        var divC = $('<div>').attr('id','unsolved-'+i)
        divC.addClass('unsolved step')
        if(i!=0) { divC.hide()}
        divC.append('<b>Unsolved:</b>')
        divC.append(unsolved)
        $('#unsolved').append(divC)
        
        
        step.html(content)
        carousel.append(step)

        var li = $('<li data-target="#myCarousel">').attr('data-slide-to',i)
        if(i==0) li.addClass('active')
        indicator.append(li)
    } 
}

function buildTable(t,c)
{
    var table = $('<table>');
    var r1 = $('<tr>');
    var r2 = $('<tr>');
    var cc = c+1;
    for (var i = 0; i < t.premises.length; i++)
    {
        var child =  $('<td>').append(buildTable(t.premises[i],cc));
        cc = cc + 1
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
        $('.modal-body').html("<b>Context Γ<sub>"+c+"</sub> :</b><br>"+context + "<br><b>Constraints:</b><br>"+t.constraints)

        
    })
    var namecell=$('<td>').attr('rowspan','2').appendTo(r1);
    namecell.append(name);
    //conclusion
    var conclusion = "Γ<sub>"+c+"</sub> ⊢ "+ t.conclusionExpr + " : " + t.conclusionTy
    $('<td>').attr('colspan',t.premises.length).addClass('conc').html(conclusion).appendTo(r2);
    table.append(r1,r2);
    return table;

}




