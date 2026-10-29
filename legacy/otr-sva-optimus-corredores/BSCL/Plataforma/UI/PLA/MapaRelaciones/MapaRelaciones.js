angular.module('ModuloPlataforma').controller('MapaRelacionesCtrl',function($scope,bcsComunes,bcsServicioPlataforma){
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	console.log(idVentana)
	var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
	$scope.random = "ididid_" + Math.floor(Math.random() * (10000));
	
	var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : "PLA",
			p_TipoEntidad : parametros.TipoEntidad || "TRANSACCION",
			p_IdEntidad : parametros.IdEntidad || 70288231
	};
	
	bcsServicioPlataforma.Servicios().ConsultarMapaRelaciones(datos).$promise.then(function(result) {
		
		var nodosSalida = result.p_out_entidades;
		var conectoresSalida = result.p_out_conectores;
		
		var entidades = [];
		var conectores = [];
		
		$.each(nodosSalida,function(key,value){
			value.id = value.IdentificadorNodo;
			entidades.push(value);
			
		});
		
		$.each(conectoresSalida,function(key,value){
			var a = {
				from : value.IdentificadorDesde,
				to : value.IdentificadorHacia,
				label : ""
			};
			conectores.push(a);
		})
		
		$("#" + $scope.random).kendoDiagram({
		    dataSource : entidades,
		    connectionsDataSource : conectores,
		    layout: {
		        type: "tree",
		      	subtype : "right",
		      	nodeDistance : 70
		    },
		    shapeDefaults: {
		        visual: visualTemplate
		    },
		    click : function(e) {
		    	console.log(e.item.dataItem);
		    	var param = {};
		    	if (e.item.dataItem.NombreParametroEnlace == "IdOperacionSpot"){
		    		param["IdOperacion"]			= e.item.dataItem.IdentificadorEnlace;
		    		param["FechaOperacion"]			= e.item.dataItem.Linea2;
		    		param["IdOperacionUI"]			= e.item.dataItem.IdentificadorEnlace;
		    		param["IdTipoProducto"]			= "MEME";
		    		param["Opcion"]					= "CONSULTAR";
		    		bcsComunes.ventanaAbrir(e.item.dataItem.TipoEnlace, param,false,false);
		    	}else{
		    		param[e.item.dataItem.NombreParametroEnlace] = e.item.dataItem.IdentificadorEnlace;
		    		bcsComunes.ventanaAbrir(e.item.dataItem.TipoEnlace, param,false,false);
		    	}
		    },
		    editable : false,
		    connectionDefaults: {
		        stroke: {
		            color: lineColor,
		            width: 1
		        },
		        editable : false,
		        type: "cascading",
		        /*content:{
		            template:"#= label#"
		        }*/
		    },
		
		    autoBind: true
		});	
		
		
	},
	function(result){
		console.log(result);
	});
	
	
	
	
	var xMargin = 5;
    var width = 150;
    var height = 100;
    
    var lineHeight = 18;
    var linePadding = 4;
    var fontSize = 10;
    
    var titleSize = 11;
    var titleHeight = 18;
    var titlePadding = 3;
    
    var lineColor = "#979797";
    
	function visualTemplate(options) {
        var dataviz = kendo.dataviz;
        var g = new dataviz.diagram.Group();
        var dataItem = options.dataItem;
        
        

        if (dataItem.Color === undefined) {
        	dataItem.Color = "#fff";
        }
        
        g.append(new dataviz.diagram.Rectangle({
            width: width,
            height: height,
            stroke: {
                width: 1,
                color : lineColor
            },
            fill: {
                color : dataItem.Color
            }
        }));
        
        var barraTitulo = new dataviz.diagram.Rectangle({
            width: width,
            height: titleHeight,
            stroke: {
                width: 1,
                color : lineColor
            },
            fill: {
                color : "#d6ecff"//"#E7912D"
            }
        });

        g.append(barraTitulo);
        
        g.append(new dataviz.diagram.TextBlock({
            text: dataItem.Titulo,
            fontWeight :"bold",
            fontSize : titleSize,
            x: xMargin,
            y: titlePadding
        }));
        
        
        
        g.append(new dataviz.diagram.Line({
        	from : new kendo.dataviz.diagram.Point(0,lineHeight*1),
        	to : new kendo.dataviz.diagram.Point(width,lineHeight*1),
        	stroke: {
                color: lineColor,
                width: 1
            }
        }))

		g.append(new dataviz.diagram.TextBlock({
            text: dataItem.Linea1,
            fontSize : fontSize,
            
            x: xMargin,
            y: lineHeight*1 + linePadding
        }));

		g.append(new dataviz.diagram.TextBlock({
            text: dataItem.Linea2,
            fontSize : fontSize,
            x: xMargin,
            y: lineHeight*2 + linePadding
        }));
		
		g.append(new dataviz.diagram.TextBlock({
            text: dataItem.Linea3,
            fontSize : fontSize,
            x: xMargin,
            y: lineHeight*3 + linePadding
        }));
		
		g.append(new dataviz.diagram.TextBlock({
            text: dataItem.Linea4,
            fontSize : fontSize,
            x: xMargin,
            y: lineHeight*4 + linePadding
        }));

        return g;
    }

	var firstWidth = document.querySelector("div#FORM_MapaRelaciones[ventanaid='"+idVentana+"']");
		firstWidth.parentNode.style.width = "980px";
});




