angular.module('ModuloTesoreria').controller('VistaPreviaComprobanteCtrl',
		function($scope,$uibModal,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$translate)
{	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);

	
	var ResumenMontos = [];
	var ListadoSocios = [];
	var NombreSocio	= '';
	var Moneda	= '';

	for (i=0;i<Object.keys($scope.parametros).length;i++){
		ListadoSocios.push($scope.parametros[i].NombrePersona +"-"+ $scope.parametros[i].IdMoneda);
	}
	ListadoSocios = _.uniq(ListadoSocios);
		
	for (i=0;i<ListadoSocios.length;i++){
		var Egreso = 0;
		var Ingreso = 0;

		for (j=0;j<Object.keys($scope.parametros).length;j++){

			if (ListadoSocios[i] === $scope.parametros[j].NombrePersona +"-"+ $scope.parametros[j].IdMoneda){

				NombreSocio = $scope.parametros[j].NombrePersona;
				Moneda	= $scope.parametros[j].IdMoneda;
				Egreso 	= Egreso + ($scope.parametros[j].IdTipoMovimiento === 'MOVEGR' ? $scope.parametros[j].Monto :0);
				Ingreso = Ingreso + ($scope.parametros[j].IdTipoMovimiento === 'MOVING' ? $scope.parametros[j].Monto :0);
			}
			else
				continue;
		}
		ResumenMontos.push({"NombrePersona":NombreSocio,
							"IdMoneda": Moneda,
							"Egreso":Egreso,
							"Ingreso":Ingreso,
							"Neto":(Ingreso - Egreso),						
						   });			
	};


	//Grilla
	var dsVistaPreviaComprobante = new bcs.DataSource({
		type : "array",
		definition : {
			data		: ResumenMontos,
			schema  	: {
				model : {
					fields : {
						NombrePersona	: {type : "string"},
						IdMoneda		: {type : "string"},
						Egreso			: {type : "number"},
						Ingreso			: {type : "number"},
						Neto			: {type : "number"},						
					}
				}
			},
			group		: {field: "IdMoneda", aggregates: [{ field: "Egreso", aggregate: "sum" }, { field: "Ingreso", aggregate: "sum" }, { field: "Neto", aggregate: "sum" }]}
		}
	});
	$scope.confVistaPreviaComprobante = {
		dataSource  : dsVistaPreviaComprobante,
		columns 	: [	 {field : "NombrePersona", 	title:"UI_SOCIO_NEGOCIO", 	width: "150px"}
						,{field : "IdMoneda", 		title:"UI_MONEDA", 			width: "30px", groupable: true}
		        	   	,{field : "Egreso", 		title:"UI_EGRESO", 			width: "80px", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#", template : " <label> #:  (util.bcs_formato(Egreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"} 
						,{field : "Ingreso",		title:"UI_INGRESO", 		width: "80px", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#", template : " <label> #:  (util.bcs_formato(Ingreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "Neto", 			title:"UI_NETO", 			width: "80px", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#", template : " <label> #:  (util.bcs_formato(Neto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						],
		contextMenu : true,
		autoBind	: true,
		userSettings: true,
		height 		: 190,
		selectable 	: "row"
	};

	$scope.confirmar = function()
	{					
		//var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_GENERARAN_NUEVOS_COMPROBANTES_A_PARTIR_SELECCIONADOS') );
		//t.result.then(function(result){
		//	if (result===OPCIONES_BCSALERTA.ACEPTAR){
				bcsComunes.ventanaData($scope.winID,'true');
				bcsComunes.ventanaCerrar($scope.winID);
		//	}
		//});
		//return;
	};

	$scope.cancelar = function()
	{					
		bcsComunes.ventanaData($scope.winID,'false');
		bcsComunes.ventanaCerrar($scope.winID);
	};
});	