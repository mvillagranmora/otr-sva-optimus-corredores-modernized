/*************************************************************
 * Purpose	: Anotaciones del Portafolio
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteAnotacionesCtrl',
	function($scope,$translate,$resource,$stateParams,bcsComunes,$uibModal,servicioPortafolio,$http){ 
	
	/******************************************************
	 * INICIO DE VARIABLES
	 * ***************************************************/
		var pCliAnt = this;
		var idVentana = bcsComunes.ventanaWinId($scope); 
		var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		pCliAnt.IdCliente = $scope.admSoc.dataSocioNegocio.IdCliente;
		var idVentana = bcsComunes.ventanaWinId($scope)
		$scope.admSoc.socioNegocioPromise.then(function () {
			pCliAnt.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['ANOTACIONES_CLIENTE'];
		});
		
		pCliAnt.filtros = {
			p_IdEmpresa : $scope.SesionIdEmpresa,		
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'SOC',
			p_UsaReglasNegocio : 'S',
			p_Clientes : [{ IdCliente : Number(pCliAnt.IdCliente) }]
		};
	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/				
		pCliAnt.agregar = function() {
			pCliAnt.abrirModal(pCliAnt.IdCliente, undefined);
		};
		pCliAnt.modificar = function(aut) {
			pCliAnt.abrirModal(pCliAnt.IdCliente, pCliAnt.anotacionSeleccionado);
		};
		pCliAnt.eliminar = function(aut) {
			pCliAnt.eliminarAnotacion();
		};
		pCliAnt.descargar = function(aut) {
			pCliAnt.descargarArchivo(aut);
		};
		
	/*********************************************
	 * DATASOURCES
	 *********************************************/
		pCliAnt.confGridAnotaciones = {
			dataSource: new bcs.DataSource({
				type: "call",
				definition: {
					callTransaction	: "702171",//idServicioConsulta
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
					callParameters  : pCliAnt.filtros,
					callResultSet   : "P_OUT_RESULTADO",
					schema: {
						model: {
							fields: {
								IdAnotacion			: { type: "number"},
								IdCliente			: { type: "number"},
								IdMotivo		    : { type: "string"},
								NombreMotivo     	: { type: "string"},
								IdRelevancia	    : { type: "string"},
								NombreRelevancia   	: { type: "string"},
								IdAmbito		    : { type: "string"},
								NombreAmbito     	: { type: "string"},
								FechaAnotacion		: { type: "date"},
								Titulo				: { type: "string"},
								Detalle				: { type: "string"},
								FechaIngreso		: { type: "date"},
								FechaCreacion		: { type: "date"},
								FechaModificacion	: { type: "date"},
								IdUsuarioCreador	: { type: "number"},
								Borrado				: { type: "string"},
								NombreArchivo		: { type: "string"},
								ExisteArchivo		: { type: "string"},
								ValidoDesde			: { type: "date"},
								ValidoHasta			: { type: "date"},
								Origen				: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "FechaAnotacion", title: $translate.instant('UI_FECHA'),format: "{0:G}", attributes: {style:"text-align:center;"}, width: "100px"},
			    {field: "Titulo", title: $translate.instant('UI_TITULO'), attributes: {style:"text-align:left;"}, width: "150px"},
			    {field: "NombreMotivo", title: $translate.instant('UI_MOTIVO'), attributes: {style:"text-align:left;"}, width: "100px"},
			    {field: "Detalle", title: $translate.instant('UI_DETALLE'), attributes: {style:"text-align:left;"}},
			    {field: "NombreAmbito", title: $translate.instant('UI_AMBITO'), attributes: {style:"text-align:left;"}},
			    {field: "NombreRelevancia", title: $translate.instant('UI_RELEVANCIA'), attributes: {style:"text-align:left;"}},
			    {field: "ValidoDesde", title: $translate.instant('UI_FECHA_VALIDO_DESDE'),format: "{0:G}", attributes: {style:"text-align:center;"}, width: "100px"},
			    {field: "ValidoHasta", title: $translate.instant('UI_FECHA_VALIDO_HASTA'),format: "{0:G}", attributes: {style:"text-align:center;"}, width: "100px"},
			    {field: "ExisteArchivo", title: $translate.instant('UI_TIENE_ADJUNTO'), attributes: {style:"text-align:center;"}, width: "100px"},
			],
			gridCreated: function(grid) {
				$scope.gridAnotaciones = grid;
			},
			sortable			: false,
			pageable			: true,
			autoBind			: true,
			height				: 200,
		    selectable 			: "row"
		};
		
	/*********************************************
	 * COMBOBOX
	 *********************************************/

		
	/*********************************************
	 * GRILLAS
	 *********************************************/
		
		
	/*********************************************
	 * FUNCIONES
	 ********************************************/

		pCliAnt.abrirModal = function(p_IdCliente, p_Anotacion){
			bcsComunes.ventanaOverlay(idVentana,true);
			var id1 = bcsComunes.ventanaAbrir("ModalClienteAnotaciones", {
				IdCliente: p_IdCliente,
				Anotacion: p_Anotacion,
				VentanaPadre: idVentana,
				CodigoFormulario: pCliAnt.CodigoFormulario
			}, false, false, idVentana);
	        
			var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){
	        	bcsComunes.ventanaOverlay(idVentana,false);
	        	pCliAnt.confGridAnotaciones.dataSource.setCallParameters(pCliAnt.filtros);
				
	    	}); 
		};
		
		var modificarAnotacionSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12092";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
		
		pCliAnt.eliminarAnotacion = function()
		{
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'CLI';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdCliente = pCliAnt.anotacionSeleccionado.IdCliente;
	        parametrosSP.p_IdAnotacion = pCliAnt.anotacionSeleccionado.IdAnotacion;
	        parametrosSP.p_Titulo = pCliAnt.anotacionSeleccionado.Titulo;
	        parametrosSP.p_Detalle = pCliAnt.anotacionSeleccionado.Detalle;
	        parametrosSP.p_IdMotivo = pCliAnt.anotacionSeleccionado.IdMotivo;
	        parametrosSP.p_IdAmbito = pCliAnt.anotacionSeleccionado.IdAmbito;
	        parametrosSP.p_IdRelevancia = pCliAnt.anotacionSeleccionado.IdRelevancia;
	        parametrosSP.p_Borrado = 'S';
	        bcsComunes.ventanaOverlay(idVentana,true);
	        modificarAnotacionSP(parametrosSP).then(
				function(result) {
					titulo = ""
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						titulo = "La operacion tiene advertencias";
					}else if (result.P_OUT_ERRORESCONTADOR){
						titulo = "La operacion tiene errores";
					}else {
						titulo = "Anotación Eliminada";
					}
					pCliAnt.confGridAnotaciones.dataSource.setCallParameters(pCliAnt.filtros);
					
					bcsComunes.ventanaMsg({
						mensaje: titulo,
						conflictos:util.parseServerResponse(result)
					},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				},
				function(result){
					bcsComunes.ventanaMsg({
						mensaje: "La operacion tiene errores",
						conflictos:util.parseServerResponse(result)
					},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				});	
		};
		
		pCliAnt.descargarArchivo = function(anotacion)
		{
			bcsComunes.ventanaOverlay(idVentana,true);
			var config = {
				responseType: "arraybuffer",
				params:{
					"id": anotacion.IdAnotacion,
					"entity": "ANOTACION_CLIENTE"	
				}
			}
			
			$http.get('/BSCL/Plataforma/GWJAVA/PLA/FileBlobManager', config)
			.success(function (response) {
				   var file = new Blob([response]);
				   saveAs(file, pCliAnt.anotacionSeleccionado.NombreArchivo);
				   bcsComunes.ventanaOverlay(idVentana,false);
			}).error(function(){
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		};
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER --------------------------