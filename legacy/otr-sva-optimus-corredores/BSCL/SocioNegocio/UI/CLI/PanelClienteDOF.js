/*********************************************************
 * Purpose	: Controlador Panel para administrar DOF
 * Author	: JGarces - BCS
 * Date		: 11/05/2016
 ********************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteDOFCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$q,
			bcsConfiguracionSociodeNegocio){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var pcdof = this;
		pcdof.ventanaId = bcsComunes.ventanaWinId($scope);
		pcdof.nuevoDOF = {}, pcdof.nuevoDOF.IdDOF = 0;
		var idVentana = bcsComunes.ventanaWinId($scope)
		/******************************************************
		 * DATASOURCES
		 * ***************************************************/
		pcdof.dsDOF = function(IdCliente) {
			return new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
					schema 			: {
						model	: {
		                    fields	: {
		                    	IdDeclaracionOrigenFondos		: { type: "number"},
							 	IdCliente						: { type: "number"},
								IdTipoOperacionOrigenFondos		: { type: "string"},
								NombreTipoOperacionOrigenFondos	: { type: "string"},
							 	FechaDeclaracion				: { type: "date"},
							 	Comentario						: { type: "string"},
							 	IdUsuarioCreador				: { type: "number"},
							 	FechaIngreso					: { type: "date"},
							 	FechaCreacion					: { type: "date"},
							 	FechaModificacion				: { type: "date"},
							 	IdEmpresa						: { type: "number"},
							 	IdEstado						: { type: "string"},
								NombreEstado					: { type: "string"},
								IdEstadoCalculado				: { type: "string"},
								NombreEstadoCalculado			: { type: "string"},
								OperacionInscripcion			: { type: "string"},
	                        }
	               		}
					},
					transport : {
						read : {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1276",
							dataType : "json"
						}
					},
					filter: { field: "IdCliente", operator: "eq", value: IdCliente},
				}
			})
		};
		
		pcdof.dsDetalleDOF = function(){
			return new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering : true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1277",
						dataType: "json"
					}
				},
				schema: {
					model: { fields: {		 	
						IdDeclaracionOrigenFondos 		: { type: "number"},
						IdCliente						: { type: "number"},
					} }
				}
				}
			})
		};
		
		/******************************************************
		 * FUNCIONES
		 * ***************************************************/
		pcdof.administrarDOF = function(item){
			var parametros = {
				IdCliente					: Number($scope.admSoc.dataSocioNegocio.IdCliente),
				IdDeclaracionOrigenFondos	: (item)?item.IdDeclaracionOrigenFondos:undefined,
				CodigoFormulario			: $scope.admSoc.dataSocioNegocio.Matriz['DECLARACION_ORIGEN_FONDOS'] };
			var id1 = bcsComunes.ventanaAbrir("ModalClienteDOF",parametros,false,false,pcdof.ventanaId); 
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(data){ 
            	pcdof.DOFsOptions.dataSource.read();
            	pcdof.DOFSeleccionado = undefined;
             }); 
		};
	
		pcdof.initDetalle = function(e) {
			bcsComunes.ventanaOverlay(pcdof.ventanaId,true);
			console.log("Inicializa el detalle!",e);
			var filterDetalleDOF = [
			    { field: "IdCliente", operator: "eq", value: e.data.IdCliente },
				{ field: "IdDeclaracionOrigenFondos", operator: "eq", value: e.data.IdDeclaracionOrigenFondos },
				{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
			];
			var dsDet = pcdof.dsDetalleDOF();
			dsDet.query({filter:filterDetalleDOF}).then(function() {
				var datos = dsDet.data();
				if (datos.length > 0){
					console.log(datos);
					e.data.detalleDOF = datos;
				}
				bcsComunes.ventanaOverlay(pcdof.ventanaId,false);
			});
		}
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			pcdof.DOFsOptions = {
				dataSource	: pcdof.dsDOF(Number($scope.admSoc.dataSocioNegocio.IdCliente)),
				columns: [{
					field: "IdDeclaracionOrigenFondos",
					title: $translate.instant('UI_ID'),
					attributes: {style:"text-align:left !important;"},
					width: "150px"
				},{
					field: "Comentario",
					title: $translate.instant('UI_COMENTARIO'),
					width: "150px"
				},{
					field: "FechaDeclaracion",
					title: $translate.instant('UI_FECHA_DECLARACION'),
					width: "150px"
				},{
					field: "OperacionInscripcion",
					title: $translate.instant('UI_OPERACION_INSCRIPCION'),
					width: "150px"
				},{
					field: "NombreTipoOperacionOrigenFondos",
					title: $translate.instant('UI_TIPO_OPERACION'),
					width: "150px"
				},{
					field: "NombreEstadoCalculado",
					title: $translate.instant('UI_ESTADO'),
					width: "150px"
				}],	
				sortable			: true,
				pageable			: true,
				autoBind			: true,  //comentar
				filterable			: false,
				height				: 250,
			    selectable 			: "row",
			    detailInit : function(e) {
			    	pcdof.initDetalle(e);
				}
			};
		});
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());

//-------------------------------------------------------------------
/*
(function() {
	angular.module('ModuloSocioNegocio').factory('servicioClienteDOF',['$resource',function($resource) {
		var urlSaveDOF 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=325";
		var urlUpdateDOF 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=324";		
		return $resource("", {},
		{
	  	  'saveDOF'		: { method: "POST",	url: urlSaveDOF},
	  	  'updateDOF'	: { method: "POST",	url: urlUpdateDOF},
	     });
	}]);
}());
*/
