/*************************************************************
 * Purpose	: Administrar requisitos operativos.
 * 			  el negocio debe implementar uso en servicios.
 * Author	: MM - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('AdministrarRequisitosOperativosCtrl',function($scope,bcsComunes,$compile,$translate){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var overlay = bcsComunes.overlay;
		var aReqOp = this;
		
		aReqOp.idVentana 	= bcsComunes.ventanaWinId($scope);
		aReqOp.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
		aReqOp.agregar = function() {
			aReqOp.abrirModal(undefined, 'Agregar');
		};
		aReqOp.modificar = function(reqOpSeleccionado) {
			aReqOp.abrirModal(reqOpSeleccionado, 'Modificar');
		};
		
/*********************************************
 * COMBOBOX
 ********************************************/
	
/*********************************************
 * DATASOURCES
 ********************************************/	
		aReqOp.gridReqOp = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true,
					serverSorting : true,
					sort : [
						{ field : "NombreCondicionOperativa",dir : "asc" },
						{ field : "NombreTipoEtapa",dir : "asc" },
						{ field : "NombreTipoCanalIngreso",dir : "asc" },
					],
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1564",
							dataType: "json"
						}
					},
					filter: { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa},
					schema: {
						model: {
							fields: {
								IdModeloRequisitoOperativo	: { type: "string"},
								IdRequisitoOperativo		: { type: "number"},
								IdCondicionOperativa		: { type: "string"},
								IdTipoEtapa					: { type: "string"},
								IdTipoComportamiento		: { type: "string"},
								IdTipoCanalIngreso			: { type: "string"},
								IdMoneda					: { type: "string"},
								ValorAtributo1				: { type: "string"},
								ValorAtributo2				: { type: "string"},
								IdUsuarioCreador			: { type: "number"},
								IdEmpresa					: { type: "number"},
								IdEstado					: { type: "string"},
								
								NombreCondicionOperativa	: { type: "string"},
								NombreTipoEtapa				: { type: "string"},
								NombreTipoComportamiento	: { type: "string"},
								NombreTipoCanalIngreso		: { type: "string"},
								NombreEstado				: { type: "string"}
								
							}
						}
					}
				}
			}),
			columns: [
			    {field: "NombreCondicionOperativa",		title: $translate.instant('UI_CONDICION_OPERATIVA'),	width: "200px"},
			    {field: "NombreTipoEtapa",				title: $translate.instant('UI_TIPO_ETAPA'),				width: "120px"},
			    {field: "NombreTipoCanalIngreso",		title: $translate.instant('UI_TIPO_CANAL_INGRESO'),		width: "100px"},
			    {field: "NombreTipoComportamiento",		title: $translate.instant('UI_TIPO_COMPORTAMIENTO'),	width: "100px"},
				{field: "NombreEstado",					title: $translate.instant('UI_ESTADO'),					width: "50px"}
			],	
			sortable			: true,
			filterable			: true,
			autoBind			: true,
			height				: 450,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row"
		};
		
/*********************************************
 * GRILLAS
 ********************************************/			
		
/*********************************************
 * FUNCIONES
 ********************************************/
		aReqOp.abrirModal = function(reqOpSeleccionado, p_accion){
			var id1 = bcsComunes.ventanaAbrir("agregarRequisitoOperativo",{ ReqOpSeleccionado: reqOpSeleccionado, AccionBoton : p_accion },false,false,aReqOp.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
	        		aReqOp.reqOpSeleccionado = undefined;
	        		aReqOp.gridReqOp.dataSource.read();
				}
	    	}); 
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------