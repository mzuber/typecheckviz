
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
                 displayTree(data.tree)
                 
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

String.prototype.sizeInPx = function() {
    var o = $('<span>' + this + '</span>')
        .css({'position': 'absolute', 'float': 'left', 'white-space': 'nowrap', 'visibility': 'hidden' ,'margin':0, 'padding':0})
        .appendTo($('#tree'))
    var w = o.width()
    var h = o.height()
    var ret = {
        height: h,
        width: w
    };

    o.remove();
    return ret;
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
    $("#result").html('<pre><b>Result type:</b> ' + r +'</pre>').show()
}

function showError(expr,e){
    $("#result").hide()
    $("#error").html(e).show()
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
                result += key + " = "+irs[i].result[key]+"<br>";
            }
        }
        var substitution = Object.keys(irs[i].substitution).map(function(key) {
            return key + " / " +irs[i].substitution[key]
        })

        step.html("")
	step.append('<div class="span2"> </div>')
        var current = $(
            '<div class="span10"><b>Current constraint: </b>'+irs[i].current +'<p>'+
            '<p><b>Result:</b> '+ result +'</p>'+
            '<p><b>Substitution:</b><br>' +'</p>'+
            '</div>').append(buildSet("&#963;",substitution)) 
        step.append(current)
        
        var unsolved = $("<ul>") 
        for (var key in irs[i].unsolved) {
            unsolved.append($('<li>').html(irs[i].unsolved[key]));
        }
        
        var divC = $('<div>').attr('id','unsolved-'+i)
        divC.addClass('unsolved step')
        if(i!=0) { divC.hide()}
        divC.append('<b>Remaining Constraints:</b>')
        divC.append(unsolved)
        $('#unsolved').append(divC)
        
        
        carousel.append(step)

        var li = $('<li data-target="#myCarousel">').attr('data-slide-to',i)
        if(i==0) li.addClass('active')
        indicator.append(li)
    } 
}


var int = 0;
function freshInt() { return int++; }

function buildSet(name,elems)
{
    var set = $('<table>')
    for(var i=0; i <elems.length;i++){
        var tr = $('<tr>')
        set.append(tr)
        if(i==0) 
        {
            tr.append($('<td>'+name+' = {</td>'))
        }
        else
            tr.append($('<td>'))
        
        // if(i+1 == elems.length && i ==0)
            // $('<td>'+elems[i]+'}</td>').addClass('set').appendTo(tr)
        if(i+1 == elems.length)
            $('<td>'+elems[i]+' }</td>').addClass('set').appendTo(tr)
        else 
            $('<td>'+elems[i]+',</td>').addClass('set').appendTo(tr)
    }
    // closing } on new line
    // if(elems.length > 1)
    // {
    //     var tr = $('<tr>')
    //     set.append(tr)
    //     tr.append($('<td style="text-align:right">}</td>'))
    //     tr.append($('<td>'))
    // }
    return set
}



