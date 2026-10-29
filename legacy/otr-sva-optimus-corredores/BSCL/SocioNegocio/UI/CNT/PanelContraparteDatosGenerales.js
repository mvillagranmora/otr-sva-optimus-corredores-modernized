/*************************************************************
 * Purpose	: Panel Contraparte Datos Generales.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelContraparteDatosGeneralesCtrl',function($scope,
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
		var pcodg = this;
		pcodg.idVentana = bcsComunes.ventanaWinId($scope);
		pcodg.dataContraparte = {};
		pcodg.dataContraparteOriginal = {};
		pcodg.dataSolicitudCambio= {};
		pcodg.dataSolicitudCambio.CambiosPendientes=false;
		pcodg.dataSolicitudCambio.CambioEstadoPendiente=false;
		var idVentana = bcsComunes.ventanaWinId($scope)
		var cargarDatosMaestropcodg = function() {
			bcsComunes.ventanaAutoOverlay(pcodg.idVentana,'cntCodigoInscripcion input',3000);
			bcsComunes.ventanaAutoOverlay(pcodg.idVentana,'cntOtros input',3000);
			$scope.admSoc.socioNegocioPromise.then(function(){
				pcodg.IdContraparte = ($scope.admSoc.dataSocioNegocio.IdContraparte?$scope.admSoc.dataSocioNegocio.IdContraparte:0);
				
				var ContraparteDatosGeneralesDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1284",
								dataType: "json"
						    }
						},
						filter: { field: "IdContraparte", operator: "eq", value: Number(pcodg.IdContraparte) },
						schema: bcsConfiguracionSociodeNegocio.getSchemaCliente()
					}
				});
				
				ContraparteDatosGeneralesDatasource.query().then(function(result){
					console.log(ContraparteDatosGeneralesDatasource.data());
					if (ContraparteDatosGeneralesDatasource.data().length != 1)
					{
						
					}
					else
					{
						pcodg.dataContraparte = $.extend({},ContraparteDatosGeneralesDatasource.data()[0]);
						pcodg.dataContraparteOriginal = $.extend({},pcodg.dataContraparte);
						asignarcheckbox();
					
						// Se consultan solicitudes de cambios Pendientes
						var filterSol = new Array();
						filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'CONTRAPARTE' },
								  	   { field: "IdEntidadOrigen", operator: "eq", value: pcodg.IdContraparte},
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
								pcodg.dataSolicitudCambio = $.extend({},SolicitudDeCambioDatasource.data());
								pcodg.dataSolicitudCambio.CambiosPendientes=true;
							}
							
							
							// Se consulta cambio de estado Pendientes
							var filterSol = new Array();
							filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'CONTRAPARTE-ESTADO' },
									  	   { field: "IdEntidadOrigen", operator: "eq", value: pcodg.IdContraparte},
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
								pcodg.dataSolicitudCambio.CambioEstadoPendiente=true;
								}
						});
							
							
						});
					}
				});
			});
		};
		
		pcodg.pantalla = {};
		pcodg.pantalla.modoEdicion = false;
		pcodg.pantalla.deshabilitado = false;
		
		asignarcheckbox = function()
		{
			if (pcodg.dataContraparte.Corredora==='S') {pcodg.dataContraparte.Corredora=true;}else{pcodg.dataContraparte.Corredora=false;};
			if (pcodg.dataContraparte.Emisor==='S') {pcodg.dataContraparte.Emisor=true;}else{pcodg.dataContraparte.Emisor=false;};
			if (pcodg.dataContraparte.Depositos==='S') {pcodg.dataContraparte.Depositos=true;}else{pcodg.dataContraparte.Depositos=false;};
			if (pcodg.dataContraparte.Bolsa==='S') {pcodg.dataContraparte.Bolsa=true;}else{pcodg.dataContraparte.Bolsa=false;};
			if (pcodg.dataContraparte.CamaraDeCompensacion==='S') {pcodg.dataContraparte.CamaraDeCompensacion=true;}else{pcodg.dataContraparte.CamaraDeCompensacion=false;};
			if (pcodg.dataContraparte.Banco==='S') {pcodg.dataContraparte.Banco=true;}else{pcodg.dataContraparte.Banco=false;};
			if (pcodg.dataContraparte.Boveda==='S') {pcodg.dataContraparte.Boveda=true;}else{pcodg.dataContraparte.Boveda=false;};
			
			if (pcodg.dataContraparteOriginal.Corredora==='S') {pcodg.dataContraparteOriginal.Corredora=true;}else{pcodg.dataContraparteOriginal.Corredora=false;};
			if (pcodg.dataContraparteOriginal.Emisor==='S') {pcodg.dataContraparteOriginal.Emisor=true;}else{pcodg.dataContraparteOriginal.Emisor=false;};
			if (pcodg.dataContraparteOriginal.Depositos==='S') {pcodg.dataContraparteOriginal.Depositos=true;}else{pcodg.dataContraparteOriginal.Depositos=false;};
			if (pcodg.dataContraparteOriginal.Bolsa==='S') {pcodg.dataContraparteOriginal.Bolsa=true;}else{pcodg.dataContraparteOriginal.Bolsa=false;};
			if (pcodg.dataContraparteOriginal.CamaraDeCompensacion==='S') {pcodg.dataContraparteOriginal.CamaraDeCompensacion=true;}else{pcodg.dataContraparteOriginal.CamaraDeCompensacion=false;};
			if (pcodg.dataContraparteOriginal.Banco==='S') {pcodg.dataContraparteOriginal.Banco=true;}else{pcodg.dataContraparteOriginal.Banco=false;};
			if (pcodg.dataContraparteOriginal.Boveda==='S') {pcodg.dataContraparteOriginal.Boveda=true;}else{pcodg.dataContraparteOriginal.Boveda=false;};
		};	
	
		
		/******************************************************
		 * Funciones
		 * ***************************************************/		
		
		pcodg.editarpcodg = function() {
			pcodg.pantalla.modoEdicion = true;
			
		};
		
		pcodg.guardarpcodg = function() {
				
			var campos = [{ nombre: "Emisor" ,tipo : "bool"},
			              { nombre: "Depositos",tipo : "bool"},
			              { nombre: "Bolsa",tipo : "bool"},
			              { nombre: "CodigoInscripcion",tipo : "texto"},
			              { nombre: "Otros",tipo : "texto"},
			              { nombre: "Corredora",tipo : "bool"},
			              { nombre: "CamaraDeCompensacion",tipo : "bool"},
			              { nombre: "Banco",tipo : "bool"},
			              { nombre: "Boveda",tipo : "bool"}];
			
			var registroCambio = [];
			var registrosNoNulos=[];
			var registrosNulos=[];
			
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (pcodg.dataContraparte[value.nombre] !== pcodg.dataContraparteOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pcodg.dataContraparte[value.nombre]?pcodg.dataContraparte[value.nombre]:"")});
					
				}
				if (value.tipo === 'combo') {
					if (!((pcodg.dataContraparte[value.nombre] === false || pcodg.dataContraparte[value.nombre] === undefined) && (pcodg.dataContraparteOriginal[value.nombre] === null || pcodg.dataContraparteOriginal[value.nombre] == 0)) && (pcodg.dataContraparte[value.nombre] != pcodg.dataContraparteOriginal[value.nombre]))
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pcodg.dataContraparte[value.nombre]?pcodg.dataContraparte[value.nombre]:"")});
				}
				if (value.tipo === 'fecha') {
					if (pcodg.dataContraparte[value.nombre] !== pcodg.dataContraparteOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pcodg.dataContraparte[value.nombre]?pcodg.dataContraparte[value.nombre]:"")});
				}
				if (value.tipo === 'bool') {
					if (pcodg.dataContraparte[value.nombre] !== pcodg.dataContraparteOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pcodg.dataContraparte[value.nombre]?"S":"N")});
				}			
			});
			
			
			if (registroCambio.length === 0)
			{
				$.each(campos,function(key,value) {
					
					if (value.tipo === 'texto') {
						if(!pcodg.dataContraparteOriginal[value.nombre] === '' || !pcodg.dataContraparteOriginal[value.nombre] === null){
							console.log('texto',pcodg.dataContraparteOriginal[value.nombre])
							registrosNoNulos.push((pcodg.dataContraparte[value.nombre]?pcodg.dataContraparte[value.nombre]:""))
						}
					}
					
					if (value.tipo === 'bool') {
						if(!pcodg.dataContraparteOriginal[value.nombre] === false || !pcodg.dataContraparteOriginal[value.nombre]=== undefined){
							console.log('bool',pcodg.dataContraparteOriginal[value.nombre])
							registrosNoNulos.push((pcodg.dataContraparte[value.nombre]?"S":"N"))
						}
					}			
				});
				
				if (registrosNoNulos.length === 0){
					pcodg.pantalla.modoEdicion = true;
					bcsComunes.msg("warning","info","Debe seleccionar al menos una opcion.");
					return;
					
				}else{
					pcodg.pantalla.modoEdicion = false;
					bcsComunes.msg("warning","info","No hay modificaciones para guardar.");
					return;
				}
			}else{
				$.each(campos,function(key,value) {
					if (pcodg.dataContraparte[value.nombre]?pcodg.dataContraparte[value.nombre]:"" !== ""){
						registrosNulos.push(value)
					}
				
				});
				
				if (registrosNulos.length===0){
					pcodg.pantalla.modoEdicion = true;
					bcsComunes.msg("warning","info","Debe seleccionar al menos una opcion.");
					return;
				}
			
			}
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_IdContraparte : pcodg.dataContraparte.IdContraparte,
				p_RegistroCambio : registroCambio,
			};
			bcsComunes.ventanaOverlay(pcodg.idVentana,true);
			servicioSociodeNegocio.modificarContraparteDatosGenerales(datos).$promise.then(
					function(result) {
						cargarDatosMaestropcodg();
						bcsComunes.ventanaOverlay(pcodg.idVentana,true);
						pcodg.pantalla.modoEdicion = false;
						bcsComunes.ventanaMsg({ mensaje : "Los datos fueron modificados",
							conflictos : util.parseServerResponse(result)}, pcodg.idVentana);
					},
					function(result){
						cargarDatosMaestropcodg();
						pcodg.pantalla.modoEdicion = false;
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
			
		};
		
		pcodg.cancelarpcodg = function() {
			pcodg.pantalla.modoEdicion = false;
			pcodg.dataContraparte = $.extend({},pcodg.dataContraparteOriginal);
		};
		
		pcodg.mostrarCambios = function() {
			var ventanaId = bcsComunes.ventanaAbrir("VerCambiosPendientes",{dataSolicitudCambio : pcodg.dataSolicitudCambio},false,true);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
		};

		var bloqueoContraparteSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=111";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
		
		pcodg.bloquearContraparte = function() {
			var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pcodg.IdContraparte, Entidad: 'CONTRAPARTE',
				IdEstado:(pcodg.dataContraparte.IdEstado==='ACT'?'BLO':'ACT')},true,true);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			$.when(promesa).then(function(){
				cargarDatosMaestropcodg();
	         }); 
		};
		
		pcodg.desbloquearContraparte = function() {
			var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pcodg.IdContraparte, Entidad: 'CONTRAPARTE',
				IdEstado:(pcodg.dataContraparte.IdEstado==='ACT'?'BLO':'ACT')},true,true);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			
			$.when(promesa).then(function(){
				cargarDatosMaestropcodg();
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
		
		 pcodg.solicitarEventoAutorizacion = function() {
			
			var parametrosSP = {};
			var eventoFila = {};
			var evento='ENRCNT';
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
	        eventoFila.Fecha 					= pcodg.kngFechaSistema;
	        eventoFila.IdConcepto 				= evento;
	        eventoFila.NumeroOperacionConcepto	= String(pcodg.IdContraparte);
	        eventoFila.FechaOperacionConcepto	= pcodg.kngFechaSistema;
	        eventoFila.IdCliente 				= pcodg.IdContraparte;
	        eventoFila.Glosa 					= pcodg.dataContraparte.DocumentoIdentidad;

	        parametrosSP.P_TABLAEVENTOS.push(eventoFila);
					
		       
	        crearEventoAutorizacionSP(parametrosSP).then(
					function(result) {
						bcsComunes.ventanaMsg({ mensaje : "Solicitud de autorizacion enviada",
							conflictos : util.parseServerResponse(result)}, pcodg.idVentana);
					},
					function(result){
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
			
		};
		
		var successFecha = function(result)
		{
			if (result.P_OUT_ERRORESCONTADOR == "0"){
				pcodg.kngFechaSistema = util.dateSPdate(util.date(result.P_OUT_FECHAHORASISTEMA).toDate());
			}
			else{
				pcodg.kngFechaSistema = '0001-01-01';
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
		cargarDatosMaestropcodg();
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER --------------------------