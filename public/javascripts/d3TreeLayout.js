

function displayTree(t)
{  
    $('#tree').html("")
    var level = prepareTree(t,0)
    var fontSize = parseInt($('#tree').css('font-size'),10)
    var treeHeight = Math.max((level -1)* fontSize,fontSize*2)
    var treeWidth = t.maxWidth * 1.3 
    var width = treeWidth *1.5 +100
    var height = treeHeight *1.5 +100

    var svg = d3.select("#tree").append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", "translate(" + 100 + "," + 100 + ")")

    var tree = d3.layout.tree()
        .children(function(d) { return d.premises; })
        .sort(function comparator(a, b) {
            return d3.ascending(a.index, b.index);
        })
        .separation(function(a, b) { 
            if(a.parent == b.parent) {
                return Math.max(a.maxWidth,b.maxWidth) + treeWidth * 0.01
            }
            else {
                return 0
            }
        })
        .size([treeWidth, treeHeight])

    var nodes = tree.nodes(t);

    
    var node = svg.selectAll(".node")
        .data(nodes)
        .enter().append("g")
        .attr("class", "node")
        .attr("r", 4.5)
        .attr("transform", function(d)
              {
                  return "translate(" +d.x+ "," +  (treeHeight - d.y) + ")";
              });
    
    svg.selectAll(".node").each ( function (d,i) {
        var x1 = d.x
        var x2 = x1 + d.text.sizeInPx().width
        var y = (treeHeight - d.y)
        if(d.children)
        {
            x1 = Math.min(x1,d.children[0].x)
            var last = d.children[d.children.length -1 ]
            x2 = Math.max(x2,last.x + last.text.sizeInPx().width)
        }
        svg.append("line")
            .attr("stroke","black")
            .attr("x1", x1)
            .attr("x2", x2)
            .attr("y1", y-fontSize)
            .attr("y2", y-fontSize)
            .attr("stroke-width",1)
        svg.append("text")
            .attr("class", "rulename")
            .attr("x", x2+4)
            .attr("y", y-fontSize/1.3)
            .on("click",function(x,i){
                var context = Object.keys(d.context).map(function(key) {
                    return key + " : " +d.context[key]
                })
                    $('#myModal').modal('show')
                    $('#myModalLabel').html(d.rulename)
                    $('.modal-body').html("")
                    $('.modal-body').append("<b>Context:</b>")
                    $('.modal-body').append(buildSet("Γ<sub>"+d.contextId+"</sub>",context))
                    $('.modal-body').append("<b>Constraints:</b>")
                    $('.modal-body').append(buildSet("c",d.constraints))
            })
            .attr("id", d.rulename+d.contextId)
            .attr("title", d.constraints)
            .attr("data-toogle","tooltip")
            .attr("data-animation","true")
            .text(d.rulename)
        $('#'+d.rulename+d.contextId).tooltip({container: 'body'})
    })
        
         var text = node.append("text")
        .attr("class", "name")
         text.append("tspan")
        .text("Γ")
         text.append("tspan")
        .attr("dy",3)
        .attr("font-size",10)
        .text(function(d) { return d.contextId  })
         text.append("tspan")
        .attr("dy",-3)
        .text(function(d) { return d.partText  })
    
}

function prepareTree(t,c) {
    if(c==0)
        t.index = 0

    var lpTree = function (st) { return prepareTree(st,c+1)}

    t.contextId = freshInt()
    var conclusionText = "Γ"+t.contextId+" ⊢ "+ t.conclusionExpr + " : " + t.conclusionTy
    t.partText = " ⊢ "+ t.conclusionExpr + " : " + t.conclusionTy
    t.text = conclusionText 
    t.level = c
    t.nodeWidth = (conclusionText + t.rulename ).sizeInPx().width
    t.maxWidth = t.nodeWidth  
    if(t.premises)
    {
        var cs = t.premises.map(lpTree)
        for (var i = 0; i < t.premises.length; i++)
        {
            t.premises[i].index = i
        }
        var maxCWidth = t.premises.reduce(function(prev, curr, index, array){
            return prev+curr.maxWidth;
        },0);
        t.maxWidth = Math.max(t.maxWidth, maxCWidth)
        return cs.reduce(function(prev, curr, index, array){
            return Math.max(prev,curr)+1;
        },0);
    }
    return 0;
}
