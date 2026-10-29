/*************************************************************
 * Purpose	: Panel Proveedor Datos Generales.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelProveedorDatosGeneralesCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$resource,
			bcsConfiguracionSociodeNegocio,
			bcsServicioPlataforma){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/	
		var pprdg = this;
		pprdg.idVentana = bcsComunes.ventanaWinId($scope);
		pprdg.dataProveedor = {};
		pprdg.dataSolicitudCambio= {};
		pprdg.dataSolicitudCambio.CambiosPendientes=false;
		pprdg.dataSolicitudCambio.CambioEstadoPendiente=false;
		var idVentana = bcsComunes.ventanaWinId($scope)
		var cargarDatosMaestropprdg = function() {
			bcsComunes.ventanaAutoOverlay(pprdg.idVentana,'proveedorComboEstado input',5000);
			$scope.admSoc.socioNegocioPromise.then(function(){
				pprdg.IdProveedor = ($scope.admSoc.dataSocioNegocio.IdProveedor?$scope.admSoc.dataSocioNegocio.IdProveedor:0);
				
				var ProveedorDatosGeneralesDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1290",
								dataType: "json"
						    }
						},
						filter: { field: "IdProveedor", operator: "eq", value: Number(pprdg.IdProveedor) },
						schema: bcsConfiguracionSociodeNegocio.getSchemaProveedor()
					}
				});
				
				ProveedorDatosGeneralesDatasource.query().then(function(result){
					console.log(ProveedorDatosGeneralesDatasource.data());
					if (ProveedorDatosGeneralesDatasource.data().length != 1)
					{
						
					}
					else
					{
						pprdg.dataProveedor = $.extend({},ProveedorDatosGeneralesDatasource.data()[0]);
						pprdg.dataProveedorOriginal = $.extend({},pprdg.dataProveedor);
	
						// Se consultan solicitudes de cambios Pendientes
							var filterSol = new Array();
							filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'PROVEEDOR' },
									  	   { field: "IdEntidadOrigen", operator: "eq", value: pprdg.IdProveedor},
										   { field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
									
							var SolicitudDeCambioDatasource = new bcs.DataSource({
								type: "odata",
								definition : {
									serverFiltering:true,
									transport: {
										read: {
											url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1360",
											dataType: "json"
									    }
									},
									filter: filterSol,
									schema: bcsConfiguracionSociodeNegocio.getSchemaSolicitudDeCambio()
								}
							});
						
							SolicitudDeCambioDatasource.query().then(function(result){
								if(SolicitudDeCambioDatasource.data().length > 0){
									pprdg.dataSolicitudCambio = $.extend({},SolicitudDeCambioDatasource.data());
									pprdg.dataSolicitudCambio.CambiosPendientes=true;
									}
	
							// Se consulta cambio de estado Pendiente
							var filterSol = new Array();
							filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'PROVEEDOR-ESTADO' },
									  	   { field: "IdEntidadOrigen", operator: "eq", value: pprdg.IdProveedor},
										   { field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
									
							var SolicitudDeCambioEstadoDatasource = new bcs.DataSource({
								type: "odata",
								definition : {
									serverFiltering:true,
									transport: {
										read: {
											url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1360",
											dataType: "json"
									    }
									},
									filter: filterSol,
									schema: bcsConfiguracionSociodeNegocio.getSchemaSolicitudDeCambio()
								}
							});
						
							SolicitudDeCambioEstadoDatasource.query().then(function(result){
								if(SolicitudDeCambioEstadoDatasource.data().length > 0){
									pprdg.dataSolicitudCambio.CambioEstadoPendiente=true;
								}
							});												
						});					
					}		
				});
			});
		};
		
		pprdg.pantalla = {};
		pprdg.pantalla.modoEdicion = false;
		pprdg.pantalla.deshabilitado = false;
	
		/******************************************************
		 * Funciones
		 * ***************************************************/		
		
		pprdg.editarpprdg = function() {
			pprdg.pantalla.modoEdicion = true;
			
		};
		
		pprdg.guardarpprdg = function() {	
			var campos = [{ nombre: "IdAreaProveedor",tipo : "combo"},
			              { nombre: "IdEstado",tipo : "combo"}];
			
			var registroCambio = [];
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (pprdg.dataProveedor[value.nombre] !== pprdg.dataProveedorOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pprdg.dataProveedor[value.nombre]?pprdg.dataProveedor[value.nombre]:"")});
				}
				if (value.tipo === 'combo') {
					if (!((pprdg.dataProveedor[value.nombre] === false || pprdg.dataProveedor[value.nombre] === undefined) && (pprdg.dataProveedorOriginal[value.nombre] === null || pprdg.dataProveedorOriginal[value.nombre] == 0)) && (pprdg.dataProveedor[value.nombre] != pprdg.dataProveedorOriginal[value.nombre]))
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pprdg.dataProveedor[value.nombre]?pprdg.dataProveedor[value.nombre]:"")});
				}
				if (value.tipo === 'fecha') {
					if (pprdg.dataProveedor[value.nombre] !== pprdg.dataProveedorOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pprdg.dataProveedor[value.nombre]?pprdg.dataProveedor[value.nombre]:"")});
				}
				if (value.tipo === 'bool') {
					if (pprdg.dataProveedor[value.nombre] !== pprdg.dataProveedorOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pprdg.dataProveedor[value.nombre]?"S":"N")});
				}			
			});
			
			
			if (registroCambio.length === 0)
			{
				pprdg.pantalla.modoEdicion = false;
				bcsComunes.msg("warning","info","No hay modificaciones para guardar.");
				return;
			}
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_IdProveedor : pprdg.dataProveedor.IdProveedor,
				p_RegistroCambio : registroCambio,
			};
			
			bcsComunes.ventanaOverlay(pprdg.idVentana,true);
			servicioSociodeNegocio.modificarProveedorDatosGenerales(datos).$promise.then(
					function(result) {
						if (result.P_OUT_ADVERTENCIASCONTADOR){
							bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias :: " + result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
						}else if (result.P_OUT_ERRORESCONTADOR){
							bcsComunes.msg("error","Error","La operacion tiene errores :: " + result.P_OUT_ERRORESDETALLE[0].Codigo);
						}else
							bcsComunes.msg("info","info","Los datos fueron modificados");
						
						
						var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : "Los datos fueron modificados",
							conflictos : util.parseServerResponse(result)}, pprdg.idVentana);
						
						bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
							cargarDatosMaestropprdg();
							pprdg.pantalla.modoEdicion = false;
							bcsComunes.ventanaOverlay(pprdg.idVentana,false);
						});
						
					},
					function(result){
						cargarDatosMaestropprdg();
						pprdg.pantalla.modoEdicion = false;
						bcsComunes.msg("error","Error","La operacion tiene errores");
						bcsComunes.ventanaOverlay(pprdg.idVentana,false);
					});
			
		};
		
		pprdg.cancelarpprdg = function() {
			pprdg.pantalla.modoEdicion = false;
			pprdg.dataProveedor = $.extend({},pprdg.dataProveedorOriginal);
		};

		pprdg.mostrarCambios = function() {
			var ventanaId = bcsComunes.ventanaAbrir("VerCambiosPendientes",{dataSolicitudCambio : pprdg.dataSolicitudCambio},false,true);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
		};	
		
		var cambioEstadoProveedorSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=119";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
	    
	    pprdg.bloquearProveedor = function() {
	    	bcsComunes.ventanaOverlay(pprdg.idVentana,true);
	    	var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pprdg.IdProveedor, Entidad: 'PROVEEDOR',
				IdEstado:(pprdg.dataProveedor.IdEstado==='ACT'?'BLO':'ACT')},true,false);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			
			$.when(promesa).then(function(){
				cargarDatosMaestropprdg();
				bcsComunes.ventanaOverlay(pprdg.idVentana,false);
	         }); 
		};
		
		pprdg.desbloquearProveedor = function() {
			bcsComunes.ventanaOverlay(pprdg.idVentana,true);
			var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pprdg.IdProveedor, Entidad: 'PROVEEDOR',
				IdEstado:(pprdg.dataProveedor.IdEstado==='ACT'?'BLO':'ACT')},true,false);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			
			$.when(promesa).then(function(){
				cargarDatosMaestropprdg();
				bcsComunes.ventanaOverlay(pprdg.idVentana,false);
	         }); 
		};
		
			
			var crearEventoAutorizacionSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=130";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
		
		 pprdg.solicitarEventoAutorizacion = function() {
			
			var parametrosSP = {};
			var eventoFila = {};
			var evento='ENRPRO';
			var modulo='SOC';
			
	        parametrosSP.P_IDEMPRESA 				= $scope.$root.SesionIdEmpresa;
	        parametrosSP.P_IDUSUARIO 				= $scope.$root.SesionIdUsuario;
	        parametrosSP.P_IDMODULO 				= modulo;
	        parametrosSP.P_SIMULAROPERACION		= "N";
	        parametrosSP.P_USAREGLASNEGOCIO 		= "N";
	        parametrosSP.P_TABLAEVENTOS 			= [];
	        parametrosSP.P_DETALLE 				= [];

	        eventoFila.IdTipoEvento 				= evento;
	        eventoFila.IdModuloSolicita 			= modulo;
	        eventoFila.IdUsuarioSolicita 		= $scope.$root.SesionIdUsuario;
	        eventoFila.Fecha 					= pprdg.kngFechaSistema;
	        eventoFila.IdConcepto 				= evento;
	        eventoFila.NumeroOperacionConcepto	= String(pprdg.IdProveedor);
	        eventoFila.FechaOperacionConcepto	= pprdg.kngFechaSistema;
	        eventoFila.IdCliente 				= pprdg.IdProveedor;
	        eventoFila.Glosa 					= pprdg.dataProveedor.DocumentoIdentidad;

	        parametrosSP.P_TABLAEVENTOS.push(eventoFila);
					
		       
	        crearEventoAutorizacionSP(parametrosSP).then(
					function(result) {

						bcsComunes.ventanaMsg({ mensaje : "Solicitud de autorizacion enviada",
							conflictos : util.parseServerResponse(result)}, pprdg.idVentana);
						cargarDatosMaestropprdg();
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
			
		};
		
		
		//FECHA SISTEMA
		var successFecha = function(result)
		{
			if (result.P_OUT_ERRORESCONTADOR == "0"){
				pprdg.kngFechaSistema = util.dateSPdate(util.date(result.P_OUT_FECHAHORASISTEMA).toDate());
			}
			else{
				pprdg.kngFechaSistema = '0001-01-01';
				console.log("Error." + result.P_OUT_ERRORESCONTADOR);
				var i = 0;
				for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
				{
					console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
					console.log("Error2.");
				}
			}
		};
		var errorFecha = function(result)
		{
			console.log(JSON.stringify(result));
			console.log("revisar");
		};
		var seteoFechaSistema = function(){
			var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'SOC';
			datos.p_UsaReglasNegocio = 'S';		
			bcsServicioPlataforma.FechaSistema(datos,successFecha,errorFecha);
		};	

		seteoFechaSistema();
		
		/******************************************************
		 * GO
		 * ***************************************************/
		cargarDatosMaestropprdg();
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER --------------------------