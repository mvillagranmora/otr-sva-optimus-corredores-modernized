(function() {
	angular.module('ModuloAPV').controller('AsignacionPortafolioAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV,bcsAlerta,OPCIONES_BCSALERTA)
	{
		/******************************************************/
		/*** INICIO DE VARIABLES							***/
		/******************************************************/
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		
		var apapv = this;
		apapv.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		
		var mostrarAPV = apapv.parametrosEntrada.TipoRegimen == 'APV' ? true : false;
		var mostrarDC = apapv.parametrosEntrada.TipoRegimen == 'DC' ? true : false;

		$scope.apapv.ContratoSAAPVVisible = mostrarAPV;
		$scope.apapv.ContratoDCVisible = mostrarDC;
		
		apapv.NombreCliente = apapv.parametrosEntrada.Nombre;
		apapv.RutCliente = apapv.parametrosEntrada.RutCliente;
		
		/******************************************************/
		/*** PRECARGA DE VARIABLES							***/
		/*******************************************************/

		var Regimen = {};

		if( (apapv.parametrosEntrada.TipoRegimen === 'APV' && apapv.parametrosEntrada.TipoRegimen !== apapv.parametrosEntrada.Regimen) || 
			( apapv.parametrosEntrada.TipoRegimen === 'DC' && apapv.parametrosEntrada.TipoRegimen !== apapv.parametrosEntrada.Regimen )){

				Regimen = {field: "Regimen", operator: "eq", value : apapv.parametrosEntrada.Regimen}
		}

		
 
		$scope.apapv.filtroContratoSAAPV = {

				RutCliente : {field: "RutCliente", operator: "eq", value: apapv.parametrosEntrada.RutCliente},
				TipoContrato : {field: "TipoContrato", operator: "eq", value : "APV"},
				CodigoMedioRecaudacion :  {field: "CodigoMedioRecaudacion", operator: "eq", value : apapv.parametrosEntrada.CodigoMedioRecaudacion},
				Regimen :  Regimen
		}
		
		$scope.apapv.filtroContratoDC = {

				RutCliente : {field: "RutCliente", operator: "eq", value: apapv.parametrosEntrada.RutCliente},
				TipoContrato : {field: "TipoContrato", operator: "eq", value : "DC"},
				IdMedioRecaudacionFormaPago :  {field: "IdMedioRecaudacionFormaPago", operator: "ne", value : 3},
				Regimen :  Regimen
		}
	
		/******************************************************/
		/*** FUNCIONES										***/
		/******************************************************/
		
		apapv.verContrato = function(modoVentana)
		{
			var filterVentana = Array();
			filterVentana.modoVentana = modoVentana;

			if(apapv.contratoSAAPVSeleccionado.Portafolio !== undefined)
			{
				filterVentana.IdContratoSAAPV 		= apapv.contratoSAAPVSeleccionado.IdContratoSAAPV;
				filterVentana.DocumentoIdentidad	= apapv.parametrosEntrada.RutCliente;
				filterVentana.modoVentana 			= "C";
			}
			else
			{
				bcsComunes.msg("warning",'Warning','Debe seleccionar un contrato para ver la informacion completa.');
				return;
			}
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("ConsultaContratoSAAPV",filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
					//
		    });
		}

		apapv.verContratoDC = function(modoVentana)
		{
			var filterVentana = Array();
			filterVentana.modoVentana = modoVentana;

			if(apapv.contratoDCSeleccionado.Portafolio !== undefined)
			{
				filterVentana.IdContratoSAAPV 		= apapv.contratoDCSeleccionado.IdContratoSAAPV;
				filterVentana.DocumentoIdentidad	= apapv.parametrosEntrada.RutCliente;
				filterVentana.modoVentana 			= "C";
			}
			else
			{
				bcsComunes.msg("warning",'Warning','Debe seleccionar un contrato para ver la informacion completa.');
				return;
			}
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("ConsultaContratoSAAPV",filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
					//
		    });
		}
		
		apapv.asignar = function()
		{

			if (apapv.idContratoSAAPV === false) { 
				apapv.idContratoSAAPV = 0;
			}
			if (apapv.idContratoDC === false) { 
				apapv.idContratoDC = 0;
			}
			if (apapv.idContratoDC !== 0) {
				apapv.idContratoSAAPV = apapv.idContratoDC;
			}
			
			var parametrosAsignacion= [];
			var datosparamEntrada = { 
					IdRecaudacionCliente: apapv.parametrosEntrada.IdRecaudacionCliente,
					IdContratoSAAPV: apapv.idContratoSAAPV
				};				
			
			parametrosAsignacion.push(datosparamEntrada);
			
			var parametros = {				
				p_IdEmpresa  : $scope.SesionIdEmpresa,
				p_IdUsuario  : $scope.SesionIdUsuario,
				p_IdModulo   : "APV", 
				p_Simulacion : "S", 
				p_Parametros : parametrosAsignacion
			};
						
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
				
			bcsServiciosAPV.AsignarContratoARecaudacion(parametros).$promise.then(function(respuesta){
				if (respuesta.P_OUT_MENSAJES[0].codigo =="UI_CONTRATO_YA_ASIGNADO") {
						consulta = bcsAlerta.crear("PREGUNTA_SI_NO","El Contrato ya fue asignado a este Cliente este mes. ¿Desea continuar?" ,idVentana);
						consulta.then(function(result)
						{
							if (result === OPCIONES_BCSALERTA.NO)
							{
								bcsComunes.ventanaOverlay(idVentana,false);
								return;
							}else{
								parametros.p_Simulacion = "N";
								bcsServiciosAPV.AsignarContratoARecaudacion(parametros).$promise.then(function(respuesta){
									bcsComunes.setConflictos(util.parseServerResponse(respuesta));
									if (respuesta.P_OUT_ERRORESCONTADOR === 0){
										bcsComunes.ventanaMsg({ mensaje: $translate.instant(respuesta.P_OUT_MENSAJES[0].codigo)}, idVentana);
										bcsComunes.ventanaOverlay(idVentana,false);
									}
									else{
										var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
										$.each(ErrorDetalle,function(key,value){	
											bcsComunes.msg("error","Error","Existen problemas:" + value );
										});
										bcsComunes.ventanaOverlay(idVentana,false);
									}
								},function(respuesta){
									var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
									$.each(ErrorDetalle,function(key,value){	
										bcsComunes.msg("error","Error","Existen problemas:" + value );
									});
									bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
								});
							}
					   });
				}
		   		// bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				else if (respuesta.P_OUT_ERRORESCONTADOR === 0 
					&& respuesta.P_OUT_MENSAJES[0].codigo =="UI_ASIGNACION_EXITOSA"){

					bcsComunes.ventanaMsg({ mensaje: $translate.instant(respuesta.P_OUT_MENSAJES[0].codigo)}, idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				}
				else{
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
					bcsComunes.ventanaOverlay(idVentana,false);
			    }
				
	   		},function(respuesta){
				var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
	   			$.each(ErrorDetalle,function(key,value){	
					bcsComunes.msg("error","Error","Existen problemas:" + value );
				});
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   		});
		}

		$scope.$watchGroup(['apapv.contratoDCSeleccionado.Portafolio','apapv.contratoSAAPVSeleccionado.Portafolio'],function(newVal,oldval)
		{
			if (newVal!=oldval) {
				apapv.contratoSeleccionado = true;
			}
		});
	});
}());