

function displayTree(t) {
    $('#tree').html(buildTable(t));
}


function buildTable(t)
{
    var table = $('<table>').attr("border",0)
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
    var context = Object.keys(t.context).map(function(key) {
        return key + " : " +t.context[key]
    })

    name.click(function(){
        $('#myModal').modal('show')
        $('#myModalLabel').html(t.rulename)
        $('.modal-body').html("")
        $('.modal-body').append("<b>Context:</b>")
        $('.modal-body').append(buildSet("Γ<sub>"+c+"</sub>",context))
        $('.modal-body').append("<b>Constraints:</b>")
        $('.modal-body').append(buildSet("c",t.constraints))
    })

    var namecell = $('<td>').attr('rowspan','2').appendTo(r1);
    namecell.append(name);
    //conclusion
    var conclusionText = "Γ<sub>"+freshInt()+"</sub> ⊢ "+ t.conclusionExpr + " : " + t.conclusionTy
    var conclusion = $('<td nowrap>').attr('colspan',t.premises.length).addClass('conc').html(conclusionText)
    // $('<span>').addClass("rulename").append(t.rulename).appendTo(conclusion)
    conclusion.appendTo(r2);
    table.append(r1,r2);
    return table;
}
