/*************************************************************
 * Purpose	: Anotaciones del Portafolio
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloAPV').controller('VerDoctosDistCtrl',
	function($scope,$translate,$resource,$stateParams,bcsComunes,$uibModal,servicioPortafolio,$http){
	
	/******************************************************
	 * INICIO DE VARIABLES
	 * ***************************************************/
		var vdd = this;
		var idVentana = bcsComunes.ventanaWinId($scope); 
		var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		vdd.IdRecaudacion = parametros.IdRecaudacion;
		
	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/				
		vdd.agregar = function() {
			vdd.abrirModal(vdd.IdRecaudacion, undefined);
		};
		vdd.modificar = function(aut) {
			vdd.abrirModal(vdd.IdRecaudacion, vdd.anotacionSeleccionado);
		};
		vdd.eliminar = function(aut) {
			vdd.eliminarAnotacion();
		};
		vdd.descargar = function(aut) {
			vdd.descargarArchivo(aut);
		};
		
	/*********************************************
	 * DATASOURCES
	 *********************************************/
		vdd.confGridAnotaciones = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=6073",
							dataType: "json"
						}
					},
					filter: [
					    { field: "IdRecaudacion", operator: "eq", value: Number(vdd.IdRecaudacion) },
					    { field: "Borrado", operator: "eq", value: 'N' }
					],
					schema: {
						model: {
							fields: {
								IdAnotacion			: { type: "number"},
								IdRecaudacion		: { type: "number"},
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
								ValidoHasta			: { type: "date"}
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

		vdd.abrirModal = function(p_IdRecaudacion, p_Anotacion){
			bcsComunes.ventanaOverlay(idVentana,true);
			var id1 = bcsComunes.ventanaAbrir("SubirDocumentoRecaudacion",{ IdRecaudacion: p_IdRecaudacion, Anotacion : p_Anotacion, VentanaPadre : idVentana },false,false,idVentana); 
	        
			var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){
	        	bcsComunes.ventanaOverlay(idVentana,false);
	        	//$scope.gridAnotaciones.dataSource.read();
				
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
		
		vdd.eliminarAnotacion = function()
		{
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'CLI';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdRegaudacion = vdd.anotacionSeleccionado.IdRecaudacion;
	        parametrosSP.p_IdAnotacion = vdd.anotacionSeleccionado.IdAnotacion;
	        parametrosSP.p_Titulo = vdd.anotacionSeleccionado.Titulo;
	        parametrosSP.p_Detalle = vdd.anotacionSeleccionado.Detalle;
	        parametrosSP.p_IdMotivo = vdd.anotacionSeleccionado.IdMotivo;
	        parametrosSP.p_IdAmbito = vdd.anotacionSeleccionado.IdAmbito;
	        parametrosSP.p_IdRelevancia = vdd.anotacionSeleccionado.IdRelevancia;
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
					$scope.gridAnotaciones.dataSource.read();
					
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
		
		vdd.descargarArchivo = function(anotacion)
		{
			bcsComunes.ventanaOverlay(idVentana,true);
			var config = {
				responseType: "arraybuffer",
				params:{
					"id": anotacion.IdAnotacion,
					"entity": "ANOTACION_RECAUDACION_APV"	
				}
			}
			
			$http.get('/BSCL/Plataforma/GWJAVA/PLA/FileBlobManager', config)
			.success(function (response) {
				   var file = new Blob([response]);
				   saveAs(file, vdd.anotacionSeleccionado.NombreArchivo);
				   bcsComunes.ventanaOverlay(idVentana,false);
			}).error(function(){
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		};
		
	});
}());
//------------------------- FIN CONTROLLER --------------------------