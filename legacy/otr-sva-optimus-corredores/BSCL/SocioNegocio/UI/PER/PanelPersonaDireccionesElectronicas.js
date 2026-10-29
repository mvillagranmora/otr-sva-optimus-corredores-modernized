/*************************************************************
 * Purpose	: Panel Persona Direcciones Electronicas
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaDireccionesElectronicasCtrl',
	function($scope,$translate,bcsComunes,$uibModal,$resource,bcsAlerta,servicioSociodeNegocio){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppde = this;
		ppde.idVentana = bcsComunes.ventanaWinId($scope);
		ppde.abrirModal = function(xIdPersona,xDireccionElectronica)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarDireccionElectronica",
			{ DireccionElectronica: xDireccionElectronica , 
				IdPersona: xIdPersona,
				CodigoFormulario: ppde.CodigoFormulario },
				false,false,ppde.idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true)
				{
					ppde.direccionElectronicaSeleccionada = undefined;
					ppde.direccionElectronicaOptions.dataSource.read();
				}
	    	}); 
		};	
		
		ppde.agregar = function() {
			ppde.abrirModal(ppde.IdPersona,undefined);
		};
		ppde.eliminar = function() {
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_MENSAJE_ELIMINAR_DIRECCION_ELECTRONICA"), ppde.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){
					var registroCambio = [];
					registroCambio.push({
						CampoModificado: "Borrado",
						ValorNuevo: "S",
						IdEntidad: ppde.direccionElectronicaSeleccionada.IdDireccionElectronica
					});
					
					var parametros = {
						p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
		        		p_IdUsuario 		:  $scope.$root.SesionIdUsuario,
					p_IdPersona			: ppde.IdPersona,
		        		p_IdModulo 			: 'SOC',
		        		p_UsaReglasNegocio	: "N",
		        		p_IdTipoCanal 		: "OPT",
						p_RegistroCambio	: registroCambio
					};
					
					bcsComunes.ventanaOverlay(ppde.idVentana,true);
				
					servicioSociodeNegocio.eliminarPersonaDireccionElectronicaMasivo(parametros).$promise.then(
						function(result){
							bcsComunes.ventanaOverlay(ppde.idVentana,false);
							var srv=util.parseServerResponse(result);
							
							var subtitulo='';
							if (srv.erroresContador > 0){
								subtitulo='UI_OPERACION_CON_ERRORES';
								bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
							} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
								subtitulo='UI_DIRECCION_ELECTRONICA_BORRADA';
							} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
								 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
							}
							
							bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, ppde.idVentana);
							ppde.direccionElectronicaOptions.dataSource.read();	
						},
						function(result){		
							bcsComunes.ventanaOverlay(ppde.idVentana,false);						
							var srv=util.parseServerResponse(result);
							var p=bcsComunes.ventanaMsg({ 
								mensaje : '', 
								conflictos : srv
							},ppde.idVentana);
						}
					);
					
					/*
					var eliminarDireccionFisicaSP = function(v_Dato)
					{
						var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13033";
						var r = $resource
						("", {}, {
							'urlConsulta': { method: "POST", url: urlConsulta}
						}
					);
					var post = r.urlConsulta(v_Dato);
						return post.$promise;
					};

					var parametrosSP = {};
					parametrosSP.p_IdEmpresa 		= $scope.SesionIdEmpresa;
					parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario;
					parametrosSP.p_IdModulo 		= 'SOC';
					parametrosSP.p_UsaReglasNegocio = "N";
					parametrosSP.p_IdDireccionElectronica = ppde.direccionElectronicaSeleccionada.IdDireccionElectronica;

					bcsComunes.ventanaOverlay(ppde.idVentana, true);
					console.log(parametrosSP);
					eliminarDireccionFisicaSP(parametrosSP).then(
					function(result) {
						bcsComunes.ventanaMsg({ 
							mensaje : "Direccion Electrónica Borrada",
							conflictos : util.parseServerResponse(result)}, ppde.idVentana);
						ppde.direccionElectronicaOptions.dataSource.read();
						bcsComunes.ventanaOverlay(ppde.idVentana, false);
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
						bcsComunes.ventanaOverlay(ppde.idVentana, false);
					});*/
				}
			});
		};
		ppde.modificar = function(die) {
			ppde.abrirModal(ppde.IdPersona,die);
		};
		ppde.predeterminar = function(die) {
			
			var predeterminarDireccionElectronicaSP = function(v_Dato)
		    {
			       var urlServicio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13026";
			       var r = $resource
			         ("", {}, {
				                 'urlServicio': { method: "POST", url: urlServicio}
			                  }
			         );
			       var post = r.urlServicio(v_Dato);
			       return post.$promise;
		    };
		    
		    var parametrosSP = {};
	        parametrosSP.p_IdEmpresa 		=  $scope.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.SesionIdUsuario;
	        parametrosSP.p_IdModulo 		=  'SOC';
	        parametrosSP.p_UsaReglasNegocio =  "S";
	        parametrosSP.p_IdDireccionElectronica = ppde.direccionElectronicaSeleccionada.IdDireccionElectronica;
	  
	        bcsComunes.ventanaOverlay(ppde.idVentana, true);    	    	
	        predeterminarDireccionElectronicaSP(parametrosSP).then(
				function(result) {
					if (result.P_OUT_ERRORESCONTADOR>0) {						
							bcsComunes.ventanaMsg({mensaje : result.P_OUT_ERRORESDETALLE[0].Codigo,
							conflictos : util.parseServerResponse(result),
							}, ppde.idVentana);							
							bcsComunes.msg("error","Error",$translate.instant('UI_ERROR_CORREO_INACTIVO'));
							bcsComunes.ventanaOverlay(ppde.idVentana, false)
								
						}else{		
						bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_DIRECCION_ELECTRONICA_PREDETERMINADO"),
							conflictos : util.parseServerResponse(result)}, ppde.idVentana);
						ppde.direccionElectronicaOptions.dataSource.read();
						bcsComunes.ventanaOverlay(ppde.idVentana, false);
						}
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppde.idVentana, false);
				});
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppde.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppde.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['DIRECCION_ELECTRONICA'];
			
			ppde.direccionElectronicaOptions = {
				dataSource: new bcs.DataSource({
					type : "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1311",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppde.IdPersona) },
						schema: {
							model: {
								fields: {
									IdDireccionElectronica : { type: "number"},
									Direccion : { type: "string"},
									IdPersona : { type: "number"},
									IdEstado : { type: "string"},
									CodigoProposito : { type: "string"},
									DescripcionProposito : { type: "string"},
									Comentario: { type: "string"},
									NombreEstado : { type: "string"},
									FechaIngreso : { type: "date"},
									FechaCreacion : { type: "date"},
									FechaModificacion : { type: "date"},
									Predeterminado: {type: "string"}
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
					field: "Direccion",
					title: $translate.instant('UI_DIRECCION'),
					attributes: {style:"text-align:left;"},
					width: "350px"
				},
				{
					field: "DescripcionProposito",
					title: $translate.instant('UI_PROPOSITO'),
					attributes: {style:"text-align:left;"},
					width: "250px"
				},
				{
					field: "Comentario",
					title: $translate.instant('UI_COMENTARIO'),
					attributes: {style:"text-align:left;"},
					width: "350px"
				},
				{
					field: "NombreEstado",
					title: $translate.instant('UI_ESTADO'),
					attributes: {style:"text-align:left;"},
					width: "150px"
				},{
					field: "Predeterminado",
					title: $translate.instant("UI_PREDETERMINADO"),
					template: "<span translate> #: Predeterminado # </span>",
					attributes: {style:"text-align:left;"},
					width: "80px"
				}]	
			};
		});	
	});
}());
//------------------------- FIN CONTROLLER --------------------------
