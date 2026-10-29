angular.module('ModuloTesoreria').controller('ModalConfiguracionChequerasCtrl',
		function($scope,$translate,bcsServiciosTesoreria,bcsComunes) {
	
	
	$scope.busquedaHabilitada = false;
	$scope.decimales = 2
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana =  bcsComunes.ventanaParams(idVentana);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	//CONFIGURACION COMBOBOX	
	$scope.oConfiguracionConfChequera={
			tipo          : "combobox-configuracion-chequeras",
			configuracion : {},
			filtros       : {
				//IdPaisBanco    : { "field"   : "IdPaisBanco","operator": "eq", "value"   : $scope.SesionIdPaisSistema },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',		
			idPostFiltro  :	false
	};
	
	
	//Consulta la unidad de medida para configuración de impresión de cheques
	var dsConfiguracionTesoreria = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering: true,
			transport  : {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1037",dataType : "json"}},
			filter     : [
			                { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
			                { field: "CodigoParametro", operator: "eq", value: 'UNIDADMEDIDAIMPRESIONCHEQUES' }
			             ],
 			schema : {
				model : {
					fields : {
						IdEmpresa		  : { type: "number"},
						CodigoParametro   : { type: "string"},
						Valor             : { type: "string"},
					}
				}
			}					             
		}
	});
	
	dsConfiguracionTesoreria.fetch().then(function(){
		 if (dsConfiguracionTesoreria.data().length > 0 )
		 {
			 $scope.$apply(function() {
				$scope.UnidadMedidaImpresionCheques = '(' + dsConfiguracionTesoreria.data()[0].Valor + ')';
				return true;					
			 });
		 }
		 else
		 {
			 $scope.UnidadMedidaImpresionCheques = '';
		     return false;
			 
		 }
    });		
		

	
		

	//
	if(parametrosVentana.ConfiguracionChequera){
		
		var objConfiguracionChequera = $.extend({}, parametrosVentana.ConfiguracionChequera);
		
		$scope.nombreConfiguracion 			= objConfiguracionChequera.NombreConfChequera;
		$scope.anchoCheque 					= objConfiguracionChequera.AnchoCheque;
		$scope.altoCheque 					= objConfiguracionChequera.AltoCheque;
		$scope.sigloImpreso 				= (objConfiguracionChequera.SigloImpreso === 'S')? true : false
		$scope.montoFila 					= objConfiguracionChequera.MontoFila;
		$scope.montoColumna 				= objConfiguracionChequera.MontoColumna;
		$scope.provinciaFila 				= objConfiguracionChequera.ProvinciaFila;
		$scope.provinciaColumna 			= objConfiguracionChequera.ProvinciaColumna;
		$scope.mesFila 						= objConfiguracionChequera.MesFila;
		$scope.mesColumna 					= objConfiguracionChequera.MesColumna;
		$scope.anioFila 					= objConfiguracionChequera.AnioFila;
		$scope.anioColumna 					= objConfiguracionChequera.AnioColumna;
		$scope.extendidoAFila 				= objConfiguracionChequera.ExtendidoAFila;
		$scope.extendidoAColumna 			= objConfiguracionChequera.ExtendidoAColumna;
		$scope.primeraCantidadFila 			= objConfiguracionChequera.PrimeraCantidadFila;
		$scope.primeraCantidadColumna 		= objConfiguracionChequera.PrimeraCantidadColumna;
		$scope.segundaCantidadFila 			= objConfiguracionChequera.SegundaCantidadFila;
		$scope.segundaCantidadColumna 		= objConfiguracionChequera.SegundaCantidadColumna;
	}
	
	var parametrosModificados = function(){
		var parametros = new Array();
		
		if ($scope.nombreConfiguracion != objConfiguracionChequera.NombreConfChequera)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'NombreConfChequera', 	ValorAntiguo: objConfiguracionChequera.NombreConfChequera, 
																											ValorNuevo: $scope.nombreConfiguracion});
		if ($scope.anchoCheque != objConfiguracionChequera.AnchoCheque)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'AnchoCheque', 			ValorAntiguo: objConfiguracionChequera.AnchoCheque, 
																											ValorNuevo: $scope.anchoCheque});
		if ($scope.altoCheque !== objConfiguracionChequera.AltoCheque)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'AltoCheque', 			ValorAntiguo: objConfiguracionChequera.AltoCheque, 
																											ValorNuevo: $scope.altoCheque});
		if ($scope.sigloImpreso !== (objConfiguracionChequera.SigloImpreso === 'S')? true : false)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'SigloImpreso', 		ValorAntiguo: objConfiguracionChequera.SigloImpreso, 
																											ValorNuevo: ($scope.sigloImpreso)? 'S': 'N'});
		if ($scope.montoFila !== objConfiguracionChequera.MontoFila)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'MontoFila', 			ValorAntiguo: objConfiguracionChequera.MontoFila, 
																											ValorNuevo: $scope.montoFila});
		if ($scope.montoColumna != objConfiguracionChequera.MontoColumna)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'MontoColumna', 		ValorAntiguo: objConfiguracionChequera.MontoColumna, 
																											ValorNuevo: $scope.montoColumna});
		if ($scope.provinciaFila !== objConfiguracionChequera.ProvinciaFila)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'ProvinciaFila', 		ValorAntiguo: objConfiguracionChequera.ProvinciaFila, 
																											ValorNuevo: $scope.provinciaFila});
		if ($scope.provinciaColumna !== objConfiguracionChequera.ProvinciaColumna)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'ProvinciaColumna', 	ValorAntiguo: objConfiguracionChequera.ProvinciaColumna, 
																											ValorNuevo: $scope.provinciaColumna});
		if ($scope.mesFila !== objConfiguracionChequera.MesFila)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'MesFila', 				ValorAntiguo: objConfiguracionChequera.MesFila, 
																											ValorNuevo: $scope.mesFila});
		if ($scope.mesColumna !== objConfiguracionChequera.MesColumna)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'MesColumna', 			ValorAntiguo: objConfiguracionChequera.MesColumna, 
																											ValorNuevo: $scope.mesColumna});
		if ($scope.anioFila !== objConfiguracionChequera.AnioFila)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'AnioFila', 			ValorAntiguo: objConfiguracionChequera.AnioFila, 
																											ValorNuevo: $scope.anioFila});
		if ($scope.anioColumna !== objConfiguracionChequera.AnioColumna)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'AnioColumna', 			ValorAntiguo: objConfiguracionChequera.AnioColumna, 
																											ValorNuevo: $scope.anioColumna});
		if ($scope.extendidoAFila !== objConfiguracionChequera.ExtendidoAFila)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'ExtendidoAFila', 		ValorAntiguo: objConfiguracionChequera.ExtendidoAFila, 
																											ValorNuevo: $scope.extendidoAFila});
		if ($scope.extendidoAColumna !== objConfiguracionChequera.ExtendidoAColumna)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'ExtendidoAColumna', 	ValorAntiguo: objConfiguracionChequera.ExtendidoAColumna, 
																											ValorNuevo: $scope.extendidoAColumna});
		if ($scope.primeraCantidadFila !== objConfiguracionChequera.PrimeraCantidadFila)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'PrimeraCantidadFila', 	ValorAntiguo: objConfiguracionChequera.PrimeraCantidadFila, 
																											ValorNuevo: $scope.primeraCantidadFila});
		if ($scope.primeraCantidadColumna !== objConfiguracionChequera.PrimeraCantidadColumna)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'PrimeraCantidadColumna',ValorAntiguo: objConfiguracionChequera.PrimeraCantidadColumna, 
																											ValorNuevo: $scope.primeraCantidadColumna});
		if ($scope.segundaCantidadFila !== objConfiguracionChequera.SegundaCantidadFila)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'SegundaCantidadFila', 	ValorAntiguo: objConfiguracionChequera.SegundaCantidadFila, 
																											ValorNuevo: $scope.segundaCantidadFila});
		if ($scope.segundaCantidadColumna !== objConfiguracionChequera.SegundaCantidadColumna)
			parametros.push({SubRegistro : 'Configuracion Chequera', CampoRegistro: 'SegundaCantidadColumna',ValorAntiguo: objConfiguracionChequera.SegundaCantidadColumna, 
																											ValorNuevo: $scope.segundaCantidadColumna});
		//console.log("PARAMETROS MODIFICADOS: " +  JSON.stringify(parametros));
		return parametros
	};
	
	var successGrabarConfiguracionChequera = function(respuesta){
		if (respuesta.P_OUT_ERRORESCONTADOR != undefined && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			
			if (parametrosVentana.ConfiguracionChequera)
			{ // Modificar
				bcsComunes.msg("info","Informacion",$translate.instant('DATOS_MODIFICACION_EXITO'));
			}
			else
			{ // Crear
				bcsComunes.msg("info","Informacion",$translate.instant('DATOS_INGRESO_EXITO'));	
			}
			
			bcsComunes.ventanaOverlay(idVentana, false);
			$scope.cancelar(true);   //Cerrar ventana modal
		}
		else {		
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error","Error",$translate.instant('UI_PROCESO_REALIZADO_CON_ERROR'));
			
			//console.log("Error:" + JSON.stringify(respuesta.P_OUT_ERRORESDETALLE));			
            var i = 0;           
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
                console.log("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);                
            }
            bcsComunes.ventanaOverlay(idVentana, false);
		}
	};
	
	var errorGrabarConfiguracionChequera = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));		
		bcsComunes.msg("error","Error",$translate.instant('UI_PROCESO_REALIZADO_CON_ERROR'));
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	$scope.grabarConfiguracionChequera = function (){
		
		bcsComunes.ventanaOverlay(idVentana, true);
		
		var parametrosSP = {};
		
		parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo					= 'TES';
		parametrosSP.p_simularOperacion			= 'N';
		parametrosSP.p_UsaReglasNegocio 		= 'S';
		parametrosSP.p_NombreConfChequera 		= $scope.nombreConfiguracion;
		parametrosSP.p_AnchoCheque				= $scope.anchoCheque;
		parametrosSP.p_AltoCheque				= $scope.altoCheque;
		parametrosSP.p_SigloImpreso				= ($scope.sigloImpreso)? 'S': 'N'
		parametrosSP.p_MontoFila				= $scope.montoFila;
		parametrosSP.p_MontoColumna				= $scope.montoColumna;
		parametrosSP.p_ProvinciaFila			= $scope.provinciaFila;
		parametrosSP.p_ProvinciaColumna			= $scope.provinciaColumna;
		parametrosSP.p_MesFila					= $scope.mesFila;
		parametrosSP.p_MesColumna				= $scope.mesColumna;
		parametrosSP.p_AnioFila					= $scope.anioFila;
		parametrosSP.p_AnioColumna				= $scope.anioColumna;
		parametrosSP.p_ExtendidoAFila			= $scope.extendidoAFila;
		parametrosSP.p_ExtendidoAColumna		= $scope.extendidoAColumna;
		parametrosSP.p_PrimeraCantidadFila		= $scope.primeraCantidadFila;
		parametrosSP.p_PrimeraCantidadColumna	= $scope.primeraCantidadColumna;
		parametrosSP.p_SegundaCantidadFila		= $scope.segundaCantidadFila;
		parametrosSP.p_SegundaCantidadColumna	= $scope.segundaCantidadColumna;
		
		//console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
		
		if (parametrosVentana.ConfiguracionChequera){
			//	Modificar
			parametrosSP.p_IdConfiguracionChequera		= parametrosVentana.ConfiguracionChequera.IdConfiguracionChequera;
			parametrosSP.p_FechaModificacion 			= util.dateSPdate(new Date())
			parametrosSP.p_Dato 						= parametrosModificados();
			
			console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.ModificarConfiguracionChequera(parametrosSP, successGrabarConfiguracionChequera, errorGrabarConfiguracionChequera);
		}
		else {
			// Crear
			console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.CrearConfiguracionChequera(parametrosSP, successGrabarConfiguracionChequera, errorGrabarConfiguracionChequera);
		}
	};
	
	
	$scope.buscarConfChequera = function () {
		
		$scope.anchoCheque 					= $scope.oConfiguracionConfChequera.seleccionado.AnchoCheque;
		$scope.altoCheque 					= $scope.oConfiguracionConfChequera.seleccionado.AltoCheque;
		$scope.sigloImpreso 				= ($scope.oConfiguracionConfChequera.seleccionado.SigloImpreso === 'S')? true : false
		$scope.montoFila 					= $scope.oConfiguracionConfChequera.seleccionado.MontoFila;
		$scope.montoColumna 				= $scope.oConfiguracionConfChequera.seleccionado.MontoColumna;
		$scope.provinciaFila 				= $scope.oConfiguracionConfChequera.seleccionado.ProvinciaFila;
		$scope.provinciaColumna 			= $scope.oConfiguracionConfChequera.seleccionado.ProvinciaColumna;
		$scope.mesFila 						= $scope.oConfiguracionConfChequera.seleccionado.MesFila;
		$scope.mesColumna 					= $scope.oConfiguracionConfChequera.seleccionado.MesColumna;
		$scope.anioFila 					= $scope.oConfiguracionConfChequera.seleccionado.AnioFila;
		$scope.anioColumna 					= $scope.oConfiguracionConfChequera.seleccionado.AnioColumna;
		$scope.extendidoAFila 				= $scope.oConfiguracionConfChequera.seleccionado.ExtendidoAFila;
		$scope.extendidoAColumna 			= $scope.oConfiguracionConfChequera.seleccionado.ExtendidoAColumna;
		$scope.primeraCantidadFila 			= $scope.oConfiguracionConfChequera.seleccionado.PrimeraCantidadFila;
		$scope.primeraCantidadColumna 		= $scope.oConfiguracionConfChequera.seleccionado.PrimeraCantidadColumna;
		$scope.segundaCantidadFila 			= $scope.oConfiguracionConfChequera.seleccionado.SegundaCantidadFila;
		$scope.segundaCantidadColumna 		= $scope.oConfiguracionConfChequera.seleccionado.SegundaCantidadColumna;
		
		$scope.busquedaHabilitada = false;
	};
	
	$scope.habilitarBusqueda = function () {
		$scope.busquedaHabilitada = true;
	};
	
	$scope.cancelar = function(respuesta) {		
		bcsComunes.ventanaData(idVentana,respuesta);  //Asocia data a la ventana, la cual será enviada a la respuesta de la promesa		
		bcsComunes.ventanaCerrar(idVentana);
	};

	var firstWidth = $('#Win_editarConfiguracionChequeras');
        firstWidth.css('width', '755px');
	var firstHeight = $('#Win_editarConfiguracionChequeras');
        firstHeight.css('height', '395px');
});