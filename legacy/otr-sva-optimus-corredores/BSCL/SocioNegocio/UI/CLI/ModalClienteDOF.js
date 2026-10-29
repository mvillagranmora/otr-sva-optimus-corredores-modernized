/*************************************************************
 * Purpose	: Controlador modal para creación/edición de DOF
 * Author	: JGarces - BCS
 * Date		: 17/05/2016
 *************************************************************/
/*
(function() {
	angular.module('ModuloSocioNegocio').factory('servicioClienteDOF',['$resource',function($resource) {
		var urlSaveDOF 		= "/BSCL/Plataforma/XSREST/call.xsjs?t=325";
		var urlUpdateDOF 	= "/BSCL/Plataforma/XSREST/call.xsjs?t=324";		
		return $resource("", {},
		{
	  	  'saveDOF'		: { method: "POST",	url: urlSaveDOF},
	  	  'updateDOF'	: { method: "POST",	url: urlUpdateDOF},
	     });
	}]);
}());
*/

(function() {
	angular.module('ModuloSocioNegocio').controller('modalClienteDOFCtrl',
		//function($window,$scope,$q,$translate,bcsComunes,$timeout,$state,$filter,$resource,bcsAlerta, bcsServicioPlataforma){			
		function($window,$scope,$q,$translate,bcsComunes,$timeout,$state,$filter,$resource,bcsAlerta,bcsServicioPlataforma){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		console.log("SeAbreVentana");
		var modCDOF = this;
		modCDOF.datosAntes 	= {};
		modCDOF.datosDespues= {};
		modCDOF.fn 			= {};
		modCDOF.ds			= {};
		modCDOF.cbbx		= {};
		modCDOF.win			= {}; 
		modCDOF.winId 		= bcsComunes.ventanaWinId($scope);
		modCDOF.param 		= bcsComunes.ventanaParams(modCDOF.winId);
		modCDOF.CodigoFormulario = modCDOF.param.CodigoFormulario;
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/

		bcsServicioPlataforma.CamposRequeridosFormulario(modCDOF.CodigoFormulario, function (respuesta) {
			modCDOF.camposRequeridos = respuesta;
		});

		const asignarValoresPorDefecto = function asignarValoresPorDefecto() {
			$.each(modCDOF.camposRequeridos, function (key, value) {
				const noEsRequerido = value.Requerido === 'N';
				const esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				const campo = modCDOF.datosAntes.detalle[value.CodigoValorFrontend];
				if (noEsRequerido && esValorDefectoValido && campo.Valor === 0) {
					let typedValue = value.ValorDefectoCampoFormulario;

					if (value.TipoDato === 'NUMBER') typedValue = parseInt(typedValue, 10);
					if(value.TipoCampoFrontend === 'bcs-input-texto'){
						modCDOF.datosAntes.detalle[value.CodigoValorFrontend].Valor = typedValue;
					}
			}
			});
		};

		var saveDOF = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=325";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
	    
		var updateDOF = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=324";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };

	    
		modCDOF.win.guardar = function() {
			bcsComunes.ventanaOverlay(modCDOF.winId,true);
			var datosGuardar = (modCDOF.win.modoCrear)?modCDOF.fn.genDatosCrear():modCDOF.fn.genDatosModificar();
			var parametrosSP = {};
			if(modCDOF.win.modoCrear) { //crear
				//servicioClienteDOF.saveDOF(datosGuardar,function (result){
				saveDOF(datosGuardar).then(
				function(result) {

					modCDOF.fn.postGuardar(result); 
				},
				function(result){ 
					modCDOF.fn.postGuardar(result,true);
				});	
			}
			else { //editar
				if(datosGuardar.p_RegistroCambio.length===0){
					bcsComunes.msg('warning','DOF','No hay cambios para guardar'); 
					bcsComunes.ventanaOverlay(modCDOF.winId,false);
					return;
				}
				//servicioClienteDOF.updateDOF(datosGuardar,function (result){
				updateDOF(datosGuardar).then(
				function(result) {

					modCDOF.fn.postGuardar(result); 
				},
				function(result){
					modCDOF.fn.postGuardar(result,true);
				});
			
				
				
			}
		};
				
		modCDOF.win.cancelar = function() {
			bcsComunes.ventanaCerrar(modCDOF.winId);
		};
		
		
/*********************************************
 * DATASOURCES
 *********************************************/
		
		modCDOF.dsDetalleDOF = new bcs.DataSource({
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
						IdDeclaracionOrigenFondos 	: { type: "number"},
						FechaDeclaracion			: { type: "sysdate"},
						IdCliente					: { type: "number"},
						Valor						: { type: "number"},
						Otros						: { type: "string"},
					} }
				}
			}
		});
		
		modCDOF.dsTipoOrigenFondo = new bcs.DataSource({
			type: "odata",
			definition : {
			serverFiltering : true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1281",
						dataType: "json"
					}
				},
				schema: {
					model: { fields: {		 	
						IdTipoOrigenFondo : { type: "string"},
					} }
				}
			}
		});
		
/*********************************************
 * COMBOBOX
 *********************************************/
		modCDOF.cbbx.tipoOperacionOrigenFondos={
			filtros: {
				IdEmpresa : { "field"   : "IdEmpresa","operator": "eq", "value"   :  $scope.$root.SesionIdEmpresa },
				IdEstado  : { "field"   : "IdEstado", "operator": "eq", "value"   :  'ACT' },
			},
		};
	
/*********************************************
 * GRILLAS
 *********************************************/
		
		
/*********************************************
 * FUNCIONES
 *********************************************/
		
		modCDOF.fn.cargaDatos = function(idCliente, idDeclaracionOrigenFondos) { 
			var detalleDOF = [];
			var filterTipoOrigenFondo = [{ field: "IdEstado", operator: "eq", value: 'ACT'}];
			modCDOF.dsTipoOrigenFondo.query({filter:filterTipoOrigenFondo}).then(function() {
				var datosTipoOrigenFondo = modCDOF.dsTipoOrigenFondo.view();
				if (datosTipoOrigenFondo.length > 0){
					if(idDeclaracionOrigenFondos) {
						var filterDetalleDOF = [
		    			    { field: "IdCliente", operator: "eq", value: idCliente },
		    				{ field: "IdDeclaracionOrigenFondos", operator: "eq", value: idDeclaracionOrigenFondos }
		    			];
						modCDOF.dsDetalleDOF.query({filter:filterDetalleDOF}).then(function() {
							detalleDOF = modCDOF.dsDetalleDOF.view();
		    				modCDOF.inicializarValores(datosTipoOrigenFondo,detalleDOF);
		    			});
					}
					else {
						modCDOF.inicializarValores(datosTipoOrigenFondo,detalleDOF);
					} 
				}
				else console.log("dsTipoOrigenFondo sin datos");
			});
		};
		
		modCDOF.inicializarValores = function(datosTipoOrigenFondo,detalleDOF) {
			console.log(datosTipoOrigenFondo,detalleDOF);
			modCDOF.datosAntes.detalle = {};
			modCDOF.datosDespues.detalle = {};
			$.each(datosTipoOrigenFondo, function(key,value){
				modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo] = {};
				modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].IdTipoOrigenFondo = value.IdTipoOrigenFondo;
				modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].NombreTipoOrigenFondo = value.Nombre;
				modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].Valor = 0;
				modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].Otros = '';
			});

			asignarValoresPorDefecto();

			if(detalleDOF.length>0){
				modCDOF.datosAntes = $.extend({}, modCDOF.datosAntes, detalleDOF[0]);
				modCDOF.datosAntes.IdEstado	= (modCDOF.datosAntes.IdEstado==='ACT')?true:false;
				$.each(detalleDOF, function(key,value){
					modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].Valor = value.Valor;
					modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].Otros = value.Otros;
				});
				modCDOF.datosDespues = angular.copy(modCDOF.datosAntes);
			}
			else {
				modCDOF.datosDespues.IdEstado 			= true;
				modCDOF.datosDespues.FechaDeclaracion 	= $scope.$root.SesionFechaSistema; //new Date();
				modCDOF.datosDespues.detalle = angular.copy(modCDOF.datosAntes.detalle);
				
				if (modCDOF.param.IdTipoOperacion)
				{modCDOF.datosDespues.IdTipoOperacionOrigenFondos=modCDOF.param.IdTipoOperacion;
				   modCDOF.datosAntes.IdTipoOperacionOrigenFondos=modCDOF.param.IdTipoOperacion;};
				 modCDOF.datosDespues.IdTipoOperacionOrigenFondos
				if (modCDOF.param.FechaLiquidacion)
				{modCDOF.datosDespues.FechaDeclaracion= util.dateInicioDia(modCDOF.param.FechaLiquidacion);};
				
			}
			modCDOF.inicializacionOk = true;
		}		
		 
		modCDOF.fn.genDatosCrear = function() {
			var dg = {};
			dg.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			dg.p_IdUsuario = $scope.$root.SesionIdUsuario;
			dg.p_IdModulo = 'PLA';
			dg.p_IdCliente						= modCDOF.param.IdCliente;
			dg.p_IdTipoOperacionOrigenFondos	= modCDOF.datosDespues.IdTipoOperacionOrigenFondos;
			dg.p_FechaDeclaracion				= util.dateSPseconddate(new Date(modCDOF.datosDespues.FechaDeclaracion));
			dg.p_Comentario						= modCDOF.datosDespues.Comentario;
			dg.p_IdEstado 						= (modCDOF.datosDespues.IdEstado)?'ACT':'INA';
			dg.p_OperacionInscripcion			= modCDOF.param.OperacionInscripcion || 'FICHA';
			dg.p_Detalle = [];
			$.each(modCDOF.datosDespues.detalle,function(key,value){ 
				var aux = {};
				aux.IdTipoOrigenFondo 	= value.IdTipoOrigenFondo;
				aux.Valor 				= value.Valor;
				aux.Otros 				= value.Otros || '';
				dg.p_Detalle.push(aux);
			});
			return dg;
		};	
		
		modCDOF.fn.genDatosModificar = function() {
			var dg = {};
			dg.p_IdEmpresa 					= $scope.$root.SesionIdEmpresa;
			dg.p_IdUsuario 					= $scope.$root.SesionIdUsuario;
			dg.p_IdModulo 					= 'PLA';
			dg.p_IdCliente					= modCDOF.param.IdCliente;
			dg.p_IdDeclaracionOrigenFondos 	= modCDOF.param.IdDeclaracionOrigenFondos
			dg.p_RegistroCambio 			= [];
			if(modCDOF.datosAntes.IdTipoOperacionOrigenFondos !== modCDOF.datosDespues.IdTipoOperacionOrigenFondos)
				dg.p_RegistroCambio.push({CampoModificado:'IdTipoOperacionOrigenFondos',ValorNuevo:modCDOF.datosDespues.IdTipoOperacionOrigenFondos})
			if(modCDOF.datosAntes.FechaDeclaracion !== modCDOF.datosDespues.FechaDeclaracion)
				dg.p_RegistroCambio.push({CampoModificado:'FechaDeclaracion',ValorNuevo:util.dateSPseconddate(new Date(modCDOF.datosDespues.FechaDeclaracion))})
			if(modCDOF.datosAntes.Comentario !== modCDOF.datosDespues.Comentario)
				dg.p_RegistroCambio.push({CampoModificado:'Comentario',ValorNuevo:modCDOF.datosDespues.Comentario})
			if(modCDOF.datosAntes.IdEstado !== modCDOF.datosDespues.IdEstado)
				dg.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:(modCDOF.datosDespues.IdEstado)?'ACT':'INA'})
			$.each(modCDOF.datosDespues.detalle,function(key,value){
				if(modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].Valor !== modCDOF.datosDespues.detalle[value.IdTipoOrigenFondo].Valor){
					var aux = {};
					aux.CampoModificado 	= value.IdTipoOrigenFondo;
					aux.ValorNuevo 			= value.Valor;
					dg.p_RegistroCambio.push(aux);
				}
				if(value.IdTipoOrigenFondo==="OTR" && (modCDOF.datosAntes.detalle[value.IdTipoOrigenFondo].Otros !== modCDOF.datosDespues.detalle[value.IdTipoOrigenFondo].Otros)){
					var aux = {};
					aux.CampoModificado 	= "Otros";
					aux.ValorNuevo 			= value.Otros;
					dg.p_RegistroCambio.push(aux);
				}
			});
			return dg;
		};
		
		modCDOF.fn.postGuardar = function(result, error){
			if(error){
				bcsComunes.msg('error','DOF','Error al guardar, revise log');
				console.log(result);
			}
			else {
				if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0) {
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Guardar OK",
						conflictos : util.parseServerResponse(result)}, modCDOF.winId );
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						bcsComunes.ventanaCerrar(modCDOF.winId);
					});
				}
				else {
					bcsComunes.msg('error','DOF','Error al guardar: ' + result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
					bcsComunes.setConflictos(util.parseServerResponse(result));
					console.log(result);
					bcsComunes.ventanaCerrar(modCDOF.winId);
				}	
			}
			bcsComunes.ventanaOverlay(modCDOF.winId,false);
		};

/*********************************************
 * FUNCIONES DE POPUP
 *********************************************/	
		

		
/*********************************************
 * FUNCIONES WATCH 
 *********************************************/	

/*********************************************
 * GOOOOO
 *********************************************/	
		if(modCDOF.param ===undefined || modCDOF.param.IdCliente ===undefined){
			bcsComunes.msg('error','DOF','No se recibe IdCliente');
			bcsComunes.ventanaCerrar(modCDOF.winId);
		}
		modCDOF.win.modoCrear = (modCDOF.param.IdDeclaracionOrigenFondos)?false:true;
		modCDOF.fn.cargaDatos(modCDOF.param.IdCliente,modCDOF.param.IdDeclaracionOrigenFondos);

	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------


