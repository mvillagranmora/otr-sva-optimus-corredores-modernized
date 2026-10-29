/*************************************************************
 * Purpose	: Panel Persona Nombres Fantasia
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaNombresFantasiaCtrl',
	function($scope,$translate,$resource,bcsComunes,$uibModal){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppnf = this;
		ppnf.showBloqueo = false;
		ppnf.idVentana = bcsComunes.ventanaWinId($scope);
		ppnf.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz["NOMBRES_FANTASIA_PERSONA"];
		$scope.$watch("ppnf.nombreFantasiaSeleccionado",
	        function handlenombreFantasia( newValue, oldValue) {
				if(newValue !== undefined){
					if(newValue.IdEstado === 'ACT')
					{
						ppnf.showBloqueo = true;
					}
					else
					{
						ppnf.showBloqueo = false;
					}
				}
	        }
	    );

		ppnf.abrirModal = function(xIdPersona, xNombreFantasia)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarNombreFantasia",{ NombreFantasia: xNombreFantasia , IdPersona: xIdPersona, CodigoFormulario: ppnf.CodigoFormulario },false,false,ppnf.idVentana ); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true)
				{
					ppnf.nombreFantasiaSeleccionado = undefined;
					ppnf.nombreFantasiaOptions.dataSource.read();
					$scope.admSoc.actualizarTab.PERSONA["DatosGenerales"] = true;
				}
	    	}); 
		};

		ppnf.agregar = function() {
			ppnf.abrirModal(ppnf.IdPersona,undefined);
		};
		ppnf.eliminar = function(doc) {
			alert(doc);
		};
		ppnf.modificar = function(doc) {
			ppnf.abrirModal(ppnf.IdPersona,doc);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppnf.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppnf.nombreFantasiaOptions = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1317",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppnf.IdPersona) },
						schema: {
							model: {
								fields: {
									
									IdNombreFantasia			: { type: "number"},
									DescripcionNombreFantasia	: { type: "string"},
									IdPersona					: { type: "number"},
									FechaCreacion 				: { type: "date"},
									FechaModificacion			: { type: "date"},
									FechaIngreso				: { type: "date"},
									IdEstado					: { type: "String"},
									IdEstadoAutorizacion		: { type: "string"},
									Borrado 					: { type: "string"},
									IdUsuarioCreador 			: { type: "number"},
									Estado						: { type: "string"}
								}
							}
						}
					}
				}),
				reorderable: true,
				filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				columnMenu: false,
				resizable: true,
				autoBind: true,
				scrollable: true,
				height:200,
	
				columns: [{
					field: "DescripcionNombreFantasia",
					title: $translate.instant('UI_NOMBRE_FANTASIA'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},{
					field: "Estado",
					title: $translate.instant('UI_ESTADO'),
					attributes: {style:"text-align:left;"},
					width: "125px"
				}]
			};
		});	

		var cambioEstadoNombreFantasiaSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=165";
		       var r = $resource
		         ("", {}, {
			                 'urlConsulta': { method: "POST", url: urlConsulta}
		                  }
		         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };

	    ppnf.bloqueoNombre = function () {
	    	var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdNombreFantasia = ppnf.nombreFantasiaSeleccionado.IdNombreFantasia;
	        parametrosSP.p_IdEstado = 'INA';
	        parametrosSP.p_Comentario = 'Bloqueo por interfaz';
	            	    	
	        cambioEstadoNombreFantasiaSP(parametrosSP).then(
					function(result) {
						bcsComunes.ventanaMsg({ mensaje : "Nombre Fantasia Bloqueado",
							conflictos : util.parseServerResponse(result)}, ppnf.idVentana);
						ppnf.nombreFantasiaOptions.dataSource.read();
						
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
	    };
	    
	    ppnf.desbloqueoNombre = function () {
	    	var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdNombreFantasia = ppnf.nombreFantasiaSeleccionado.IdNombreFantasia;
	        parametrosSP.p_IdEstado = 'ACT';
	        parametrosSP.p_Comentario = 'Desbloqueo por interfaz';
	            	    	
	        cambioEstadoNombreFantasiaSP(parametrosSP).then(
					function(result) {

						bcsComunes.ventanaMsg({ mensaje : "Nombre Fantasia Desbloqueado",
							conflictos : util.parseServerResponse(result)}, ppnf.idVentana);
						ppnf.nombreFantasiaOptions.dataSource.read();
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
	    };

	});
}());
//------------------------- FIN CONTROLLER --------------------------
