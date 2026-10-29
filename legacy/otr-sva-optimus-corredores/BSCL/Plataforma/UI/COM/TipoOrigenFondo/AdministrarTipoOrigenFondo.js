/*************************************************************
 * Purpose	: Administrar Tipo de Origen de Fondo.
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('AdministrarTipoOrigenFondoCtrl',
		function ($scope,bcsComunes,$translate) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var admTOF 			= this;
			admTOF.idVentana 	= bcsComunes.ventanaWinId($scope);
			admTOF.parametros 	= bcsComunes.ventanaParams(admTOF.idVentana);
			admTOF.domVentana	= $("[ventanaid='"+admTOF.idVentana+"']");
			/*********************************************
			 * GRILLA
			 *********************************************/
			admTOF.GrillaTipoOrigenFondo = {
				dataSource		: new bcs.DataSource({
					type 		: "odata",
					definition 	: {
						serverFiltering : true,
						serverPaging 	: true,
						serverSorting 	: true, 
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1281",
								dataType: "json"
							}
						},
						filter: { field: 'IdEmpresa', operator: "eq", value: $scope.$root.SesionIdEmpresa},
						
						schema 	 : {
							model	: { fields	: {		 	
									IdTipoOrigenFondo		: { type: "string"},
									Nombre					: { type: "string"},
									ValorDefecto			: { type: "number"},
									IdUsuarioCreador		: { type: "number"},
									FechaIngreso			: { type: "date"},
									FechaCreacion			: { type: "date"},
									FechaModificacion		: { type: "date"},
									IdEmpresa				: { type: "number"},
									IdEstado				: { type: "string"},
									NombreEstado			: { type: "string"},
								} 
							}
						}
					}
				}),
				columns			: [
					{field: "IdTipoOrigenFondo"			,width: "150px",title: $translate.instant("UI_CODIGO")							,attributes : {style : "text-align:left;"}},		
					{field: "Nombre"					,width: "150px",title: $translate.instant("UI_NOMBRE")							,attributes : {style : "text-align:left;"}},
					{field: "ValorDefecto"				,width: "150px",title: $translate.instant("UI_VALOR_DEFECTO")	,format:"{0:n2}",attributes : {style : "text-align:left;"}},
					{field: "NombreEstado"				,width: "150px",title: $translate.instant("UI_ESTADO")							,attributes : {style : "text-align:left;"}},
				],
				gridCreated: function(grid) {
					$scope.admTOF.apiGrillaTipoOrigenFondo = grid;
				},
				sortable			: true,
				filterable			: true,
				groupable			: false,
				pageable			: true,
				autoBind			: true,  //comentar
				height				: 400,
				userSettings		: true,
				navigatable			: true,
				excel : {
					allPages: true,
				},
			    selectable 			: "row"
			 };
			/*********************************************
			 * FUNCIONES
			 *********************************************/	
	
			admTOF.modalValor = function (valor) {
				var id1 = bcsComunes.ventanaAbrir("ModalTipoOrigenFondo",{tipoOrigenFondo:valor},false,true); 
	            var promesa=bcsComunes.ventanaPromesa(id1);                               
	            $.when(promesa).then(function(respuesta){ 
	            	if(respuesta.aceptar === true){
	            		admTOF.apiGrillaTipoOrigenFondo.dataSource.read();
					}
					else {console.log("usuario cancela en el PopUp!");}
	            });
			};	
	});
}());
//------------------------- FIN CONTROLLER --------------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioTipoOrigenFondo',['$resource',function($resource) {
		var urlSaveTipoOrigenFondo		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=328";
		var urlUpdateTipoOrigenFondo	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=329";		
		return $resource("", {},
		{
	  	  'saveTipoOrigenFondo'		: { method: "POST",	url: urlSaveTipoOrigenFondo},
	  	  'updateTipoOrigenFondo'	: { method: "POST",	url: urlUpdateTipoOrigenFondo},
	     });
	}]);
}());
