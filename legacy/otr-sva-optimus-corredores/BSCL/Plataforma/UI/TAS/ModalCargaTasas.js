(function() {
	angular.module('ModuloPlataforma').controller('ModalCargaTasasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosTasas){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		// var mCTas 		= this;
		$scope.mCTas = {};
		$scope.mCTas.idVentana	= bcsComunes.ventanaWinId($scope);
		$scope.mCTas.parametros= bcsComunes.ventanaParams($scope.mCTas.idVentana);
		$scope.mCTas.domVentana= $("[ventanaid='"+$scope.mCTas.idVentana+"']");
		
		var precioTasaSel = _.clone($scope.mCTas.parametros.PrecioTasaSel);
		var fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");		

		
		$scope.mCTas.win	= {};
		$scope.mCTas.cmbbx	= {};
		$scope.mCTas.fn	= {};
		$scope.mCTas.grilla= {};
		$scope.mCTas.pagReajuste = false ;
		
		$scope.mCTas.datosTasa	= {};
		$scope.mCTas.datosTasa.IdEstadoDef = 'ACT';
		
		$scope.mCTas.datosTasa.FechaCreSol	= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		$scope.mCTas.win.TasaDecimal	= (util.bcs_formato(0,'TAS','TASA','CLP','','')).cantidadDecimales;
		$scope.mCTas.win.MontoDecimal	= (util.bcs_formato(0,'TAS','MONTO','CLP','','')).cantidadDecimales;
		
		$scope.mCTas.IdBotonAceptar = 'ModalCargaTasas_aceptar' + $scope.mCTas.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(precioTasaSel){
			bcsComunes.ventanaTitulo($scope.mCTas.idVentana, $translate.instant('UI_MODIFICAR_TASA'));
			$scope.mCTas.datosTasa.IdPrecioTasa		= precioTasaSel.IdPrecioTasa || 0;
			$scope.mCTas.datosTasa.IdFuente			= precioTasaSel.IdFuente || 0;
			$scope.mCTas.datosTasa.Codigo			= precioTasaSel.CodigoFuente || 0;
			$scope.mCTas.datosTasa.CodigoDef		= precioTasaSel.CodigoFuente || 0;
			$scope.mCTas.datosTasa.IdClasificacionTipoInstrumento		= precioTasaSel.IdClasificacionTipoInstrumento || 0;
			$scope.mCTas.datosTasa.IdClasificacionTipoInstrumentoDef	= precioTasaSel.IdClasificacionTipoInstrumento || 0;
			$scope.mCTas.datosTasa.IdInstrumento	= precioTasaSel.IdInstrumento || 0;
			$scope.mCTas.datosTasa.IdInstrumentoDef	= precioTasaSel.IdInstrumento || 0;
			$scope.mCTas.datosTasa.Nemotecnico		= precioTasaSel.Nemotecnico || 0;
			$scope.mCTas.datosTasa.Valor			= precioTasaSel.TipoRegistro || 0;
			$scope.mCTas.datosTasa.ValorDef			= precioTasaSel.TipoRegistro || 0;
			$scope.mCTas.datosTasa.IdMoneda			= precioTasaSel.IdMoneda || 0;
			$scope.mCTas.datosTasa.IdMonedaDef		= precioTasaSel.IdMoneda || 0;
			$scope.mCTas.datosTasa.IdMonedaReajuste			= precioTasaSel.IdMoneda || 0;
			$scope.mCTas.datosTasa.IdMonedaDefReajuste		= precioTasaSel.IdMoneda || 0;
			$scope.mCTas.datosTasa.kngIdMonedaPagoInstrumento	= precioTasaSel.IdMoneda || 0;
			$scope.mCTas.datosTasa.kngIdMonedaPagoInstrumentoDef	= precioTasaSel.IdMoneda || 0;	

			if(precioTasaSel.TipoRegistro === 'TASA' || precioTasaSel.TipoRegistro === 'PUNTO'){
				$scope.mCTas.datosTasa.IdTipoTasa	= precioTasaSel.TipoTasa || 0;
				$scope.mCTas.datosTasa.IdTipoTasaDef= precioTasaSel.TipoTasa || 0;
				$scope.mCTas.datosTasa.Tasa			= precioTasaSel.Tasa || 0;
				$scope.mCTas.datosTasa.TasaEstimada	= precioTasaSel.TasaEstimada || 0;
			}
			else if(precioTasaSel.TipoRegistro === 'PRECIO'){
				$scope.mCTas.datosTasa.IdTipoPrecio	= precioTasaSel.IdTipoPrecio || 0;
				$scope.mCTas.datosTasa.IdTipoPrecioDef	= precioTasaSel.IdTipoPrecio || 0;
				$scope.mCTas.datosTasa.Precio		= precioTasaSel.Precio || 0;
			}
			$scope.mCTas.datosTasa.Plazo			= precioTasaSel.Plazo || 0;
			$scope.mCTas.datosTasa.FechaCreSol		= precioTasaSel.FechaSolicitud || $scope.mCTas.datosTasa.FechaCreSol;
			
			$scope.mCTas.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo($scope.mCTas.idVentana, $translate.instant('UI_AGREGAR_TASA'));
			$scope.mCTas.datosTasa.Tasa		= 0;
			$scope.mCTas.datosTasa.TasaEstimada= 0;
			$scope.mCTas.datosTasa.Precio		= 0;
			$scope.mCTas.datosTasa.Plazo		= 0;
			
			$scope.mCTas.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		$scope.mCTas.cmbbx.Fuente = {
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'},
				NombreEntidad:{field: "NombreEntidad",	operator: "eq", value: 'MANTENEDOR'}
			}
		};
		
		$scope.mCTas.cmbbx.Instrumento = {
			filtros : {
				IdEmpresa : { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: '-1' }
			}
		};
		
		$scope.mCTas.cmbbx.TipoRegistro = {
			filtros: {
				IdEmpresa		: { field: "IdEmpresa",		operator: "eq", value: $scope.$root.SesionIdEmpresa },
				NombreEntidad	: { field: "NombreEntidad",	operator: "eq", value: "TIPOREGISTROPRECIOTASA" }
			},
			configuracion : {
				placeholder: $translate.instant('UI_TIPO_REGISTRO'),
			},
		};
		
		$scope.mCTas.cmbbx.TipoTasa = {
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'},
				TipoRegistro: {field: "TipoRegistro", operator: "eq", value: '-1'},
			}
		};
		
		$scope.mCTas.cmbbx.TipoPrecio = {
			filtros: {
				IdEstado : {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		

		$scope.mCTas.cmbbx.Emisor = {
			filtros: {
				EmiteIIF	: {field: "EmiteIIF", operator: "eq", "value"   :'S'},
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'},
			}
		};		
		
		
		$scope.mCTas.cmbbx.Moneda = {
			configuracion : {
				template: '<span >#: IdTipoReajuste # - #: NombreTipoReajuste #</span>'
			},
			filtros : {
				IdInstrumento		: { "field"   : "IdInstrumento", "operator": "eq", "value"   : "" },
				IdTipoInstrumento	: { "field"   : "IdTipoInstrumento", "operator": "eq", "value"   : "" },
				IdMoneda			: { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
							
			}
		};

		$scope.mCTas.cmbbx.MonedaReajuste = {
			configuracion : {
				template: '<span >#: IdTipoReajuste # - #: NombreTipoReajuste #</span>'
			},
			filtros : {
				IdInstrumento		: { "field"   : "IdInstrumento", "operator": "eq", "value"   : "" },
				IdTipoInstrumento	: { "field"   : "IdTipoInstrumento", "operator": "eq", "value"   : "" },
				IdMoneda			: { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
							
			}
		};


		$scope.mCTas.cmbbx.oConfiguracionMonedaPagoInstrumento={
			configuracion : {
				template: '<span >#: IdMonedaLiquidacion #</span>',
				dataTextField: 'IdMonedaLiquidacion'
			},
			filtros : {
				IdTipoInstrumento	: { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
				IdMoneda			: { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto: false,
			idPostFiltro : false
		};

		$scope.mCTas.datosTasa.ConfigFechaVencimiento = {
				min: new Date(fechaSistema.setDate(fechaSistema.getDate() + 1))
		};

		$scope.mCTas.datosTasa.configMontoMinimo = {
			min: -100
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		$scope.mCTas.fn.cancelar = function () {
			bcsComunes.ventanaData($scope.mCTas.idVentana, false);
			bcsComunes.ventanaCerrar($scope.mCTas.idVentana);
		};
		
		
		$scope.mCTas.fn.aceptar = function () {
			var datosGuardar = {};							
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(precioTasaSel && precioTasaSel.IdPrecioTasa) { // EDITAR
				if($scope.mCTas.datosTasa.Tasa			=== precioTasaSel.Tasa && 
				   $scope.mCTas.datosTasa.TasaEstimada	=== precioTasaSel.TasaEstimada && 
				   $scope.mCTas.datosTasa.Precio		=== precioTasaSel.Precio && 
				   $scope.mCTas.datosTasa.Plazo		=== precioTasaSel.Plazo){
					bcsComunes.msg("warning","UI_MANTENEDOR_TASAS",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData($scope.mCTas.idVentana, false);
					bcsComunes.ventanaCerrar($scope.mCTas.idVentana);
					return
				}
				
				datosGuardar.p_IdPrecioTasa	= precioTasaSel.IdPrecioTasa;
				datosGuardar.p_Tasa			= $scope.mCTas.datosTasa.Tasa;
				datosGuardar.p_TasaEstimada	= $scope.mCTas.datosTasa.TasaEstimada;
				datosGuardar.p_Precio		= $scope.mCTas.datosTasa.Precio;
				datosGuardar.p_Plazo		= $scope.mCTas.datosTasa.Plazo;
				
				datosGuardar.p_DetalleLog = [];
				if((precioTasaSel.TipoRegistro === 'TASA' || precioTasaSel.TipoRegistro === 'PUNTO') && $scope.mCTas.datosTasa.Tasa !== precioTasaSel.Tasa)
					datosGuardar.p_DetalleLog.push({SubRegistro:precioTasaSel.CodDescFuente+' - '+precioTasaSel.Nemotecnico, CampoRegistro:'Tasa', ValorAntiguo:precioTasaSel.Tasa, ValorNuevo:$scope.mCTas.datosTasa.Tasa});
				if((precioTasaSel.TipoRegistro === 'TASA' || precioTasaSel.TipoRegistro === 'PUNTO') && $scope.mCTas.datosTasa.TasaEstimada !== precioTasaSel.TasaEstimada)
					datosGuardar.p_DetalleLog.push({SubRegistro:precioTasaSel.CodDescFuente+' - '+precioTasaSel.Nemotecnico, CampoRegistro:'TasaEstimada', ValorAntiguo:precioTasaSel.TasaEstimada, ValorNuevo:$scope.mCTas.datosTasa.TasaEstimada});
				if(precioTasaSel.TipoRegistro === 'PRECIO' && $scope.mCTas.datosTasa.Precio !== precioTasaSel.Precio)
					datosGuardar.p_DetalleLog.push({SubRegistro:precioTasaSel.CodDescFuente+' - '+precioTasaSel.Nemotecnico, CampoRegistro:'Precio', ValorAntiguo:precioTasaSel.Precio, ValorNuevo:$scope.mCTas.datosTasa.Precio});
				if($scope.mCTas.datosTasa.Plazo !== precioTasaSel.Plazo)
					datosGuardar.p_DetalleLog.push({SubRegistro:precioTasaSel.CodDescFuente+' - '+precioTasaSel.Nemotecnico, CampoRegistro:'Plazo', ValorAntiguo:precioTasaSel.Plazo, ValorNuevo:$scope.mCTas.datosTasa.Plazo});
				
				bcsComunes.ventanaOverlay($scope.mCTas.idVentana,true);
				bcsServiciosTasas.modificarTasa(datosGuardar,function (result){
					$scope.mCTas.fn.evaluaRespuestaSP(result, 'UI_TASA_MODIFICADA');
				}, function(resError){
					bcsComunes.msg("error","UI_MANTENEDOR_TASAS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay($scope.mCTas.idVentana,false);
				});
				
					 				
			}
			else { // CREAR
				
				if ( ( $scope.mCTas.datosTasa.IdClasificacionTipoInstrumento == 'RF' || $scope.mCTas.datosTasa.IdClasificacionTipoInstrumento == 'IF' )  && $scope.mCTas.datosTasa.IdTipoTasa !== 'CURVA' ) {
					// Decodifica NemoSVS 
					TraeNemoSVS();
				} else { 
					CreaTasa();								
				}								
			
				
			}
		};
		
		
		$scope.mCTas.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_MANTENEDOR_TASAS",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_MANTENEDOR_TASAS",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_MANTENEDOR_TASAS",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_MANTENEDOR_TASAS",$translate.instant("UI_REVISE_LOG"));
			
			//console.log(result);
			bcsComunes.ventanaOverlay($scope.mCTas.idVentana,false);
			bcsComunes.ventanaData($scope.mCTas.idVentana, true);
			bcsComunes.ventanaCerrar($scope.mCTas.idVentana);
		};
		
		var TraeNemoSVS = function() {
			var datos = Object();
			if ( $scope.mCTas.datosTasa.IdClasificacionTipoInstrumento == 'IF' ) {
				if ( ($scope.mCTas.datosTasa.Instrumento_IFRF.Nemotecnico === "PDBC" || $scope.mCTas.datosTasa.Instrumento_IFRF.Nemotecnico === "PRBC" ) 
					 &&	$scope.mCTas.datosTasa.IdEmisor !== "CENTRAL") {
					bcsComunes.msg("error","UI_EMISOR_INVALIDO",""); 
					return;
				}

				var diferenciaMilisegundos = $scope.mCTas.datosTasa.FechaVencimiento - fechaSistema;
				var diferenciaDias = Math.floor(diferenciaMilisegundos / (1000 * 60 * 60 * 24));


				datos.p_Instrumentos = [{
					"IdInterno" 	: 1,
					"IdEmisor"		: $scope.mCTas.datosTasa.IdEmisor,
					"FechaVencimiento": util.dateSPdate(fechaSistema),
					"Plazo"			: diferenciaDias,				
					"Nemotecnico"	: $scope.mCTas.datosTasa.Instrumento_IFRF.Nemotecnico,
					"IdTipoReajuste": $scope.mCTas.datosTasa.IdMonedaReajuste,
					"PagaderoEn"	: !$scope.mCTas.pagReajuste ? $scope.mCTas.datosTasa.IdMoneda : $scope.mCTas.datosTasa.kngIdMonedaPagoInstrumento
				}];			

				bcsServiciosTasas.ObtenerNemoSVS( datos ).$promise.then(function(respuesta) {
					if (respuesta.p_out_Instrumento.length > 0){
						// Ahora envia a valorizar...
						$scope.mCTas.datosTasa.Nemotecnico = respuesta.p_out_Instrumento[0].NemoSVS
						CreaTasa();
					}
				});
			} else {
				$scope.mCTas.datosTasa.Nemotecnico = $scope.mCTas.datosTasa.Instrumento_IFRF.Nemotecnico;
				$scope.mCTas.datosTasa.IdMoneda = $scope.mCTas.datosTasa.Instrumento_IFRF.IdTipoReajuste;				
				CreaTasa();
			}
		};


		var monedaIf = function () {
			return $scope.mCTas.datosTasa.kngIdMonedaPagoInstrumento != false ? $scope.mCTas.datosTasa.kngIdMonedaPagoInstrumento : ''
		}
		
		var CreaTasa = function() {

			var datosGuardar = {};							
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			datosGuardar.p_TipoRegistro		= $scope.mCTas.datosTasa.Valor;
			datosGuardar.p_IdFuente			= $scope.mCTas.datosTasa.FuenteObj.IdFuente;
			datosGuardar.p_IdClasificacionTipoInstrumento = $scope.mCTas.datosTasa.IdClasificacionTipoInstrumento;

			if( $scope.mCTas.datosTasa.IdTipoTasa !== 'CURVA' && $scope.mCTas.datosTasa.InstrumentoObj ) {
				datosGuardar.p_IdTipoInstrumento= $scope.mCTas.datosTasa.InstrumentoObj.IdTipoInstrumento;
				datosGuardar.p_IdInstrumento	= $scope.mCTas.datosTasa.InstrumentoObj.IdInstrumento;
				datosGuardar.p_Nemotecnico		= $scope.mCTas.datosTasa.InstrumentoObj.Nemotecnico;
				datosGuardar.p_IdMoneda			= $scope.mCTas.datosTasa.InstrumentoObj.IdMonedaPrincipal;
			}
			else if ( $scope.mCTas.datosTasa.IdTipoTasa !== 'CURVA' && $scope.mCTas.datosTasa.Instrumento_IFRF ){
				$scope.mCTas.datosTasa.Plazo 	= ( $scope.mCTas.datosTasa.Instrumento_IFRF.FechaVencimiento - $scope.mCTas.datosTasa.Instrumento_IFRF.FechaEmision ) / (1000*60*60*24),								
				datosGuardar.p_IdTipoInstrumento= $scope.mCTas.datosTasa.Instrumento_IFRF.IdTipoInstrumento;
				datosGuardar.p_IdInstrumento	= $scope.mCTas.datosTasa.Instrumento_IFRF.IdInstrumento;
				datosGuardar.p_Nemotecnico		= $scope.mCTas.datosTasa.Nemotecnico
				if (typeof $scope.mCTas.datosTasa === "undefined" ||
			    typeof $scope.mCTas.datosTasa.InstrumentoObj === "undefined" ||
			    typeof $scope.mCTas.datosTasa.InstrumentoObj.IdMonedaPrincipal === "undefined") {
					datosGuardar.p_IdMoneda			= $scope.mCTas.datosTasa.IdMoneda === undefined || $scope.mCTas.datosTasa.IdMoneda === false ? monedaIf() : $scope.mCTas.datosTasa.IdMoneda;
				}else{
					datosGuardar.p_IdMoneda			= $scope.mCTas.datosTasa.InstrumentoObj.IdMonedaPrincipal;	
				}
				datosGuardar.p_IdClasificacionTipoInstrumento = $scope.mCTas.datosTasa.IdClasificacionTipoInstrumento;
			}
			else{
				datosGuardar.p_IdMoneda			= $scope.mCTas.datosTasa.IdMoneda === undefined || $scope.mCTas.datosTasa.IdMoneda === false ? '': $scope.mCTas.datosTasa.IdMoneda;
			}
			if($scope.mCTas.datosTasa.Valor === 'TASA' || $scope.mCTas.datosTasa.Valor === 'PUNTO'){
				datosGuardar.p_IdTipoTasa	= $scope.mCTas.datosTasa.IdTipoTasa;
				datosGuardar.p_Tasa			= $scope.mCTas.datosTasa.Tasa;
				datosGuardar.p_TasaEstimada	= $scope.mCTas.datosTasa.TasaEstimada;
			}
			else if($scope.mCTas.datosTasa.Valor === 'PRECIO'){
				datosGuardar.p_IdTipoPrecio	= $scope.mCTas.datosTasa.IdTipoPrecio;
				datosGuardar.p_Precio		= $scope.mCTas.datosTasa.Precio;
			}
			datosGuardar.p_Plazo		= $scope.mCTas.datosTasa.Plazo || 0;
			datosGuardar.p_FechaCreSol	= util.dateSPdate($scope.mCTas.datosTasa.FechaCreSol);

			bcsComunes.ventanaOverlay($scope.mCTas.idVentana,true);
			bcsServiciosTasas.crearTasa(datosGuardar,function (result){
				$scope.mCTas.fn.evaluaRespuestaSP(result, 'UI_TASA_CREADA');
			}, function(resError){
				bcsComunes.msg("error","UI_MANTENEDOR_TASAS",$translate.instant("UI_REVISE_LOG"));
				bcsComunes.ventanaOverlay($scope.mCTas.idVentana,false);
			});			
		};
		
		
		/*
		$scope.$watch('$scope.mCTas.datosTasa.Valor', function(newValue, oldValue){
			if(newValue && newValue !== oldValue){
				if($scope.mCTas.datosTasa.Valor === 'PUNTO'){
					$scope.mCTas.datosTasa.IdTipoTasa = $scope.mCTas.datosTasa.IdTipoTasaDef = 'PUNTO';
				}
			}
		});
		*/


		var configDataCombobox = function () {
			$scope.mCTas.datosTasa.IdMonedaDef = '';
			$scope.mCTas.cmbbx.Moneda.filtros.IdInstrumento.value= '';
			$scope.mCTas.cmbbx.Moneda.filtros.IdTipoInstrumento.value= '';
			$scope.mCTas.cmbbx.Moneda.filtros.IdMoneda.value= '';
			$scope.mCTas.pagReajuste = false ;
		}

		var monedasIF = function (nemo) {
			$scope.mCTas.pagReajuste = true;

			if (nemo === 'PDBC' || nemo === 'PRBC' ) {
				$scope.mCTas.datosTasa.IdEmisor = "CENTRAL";
			}
			
			// tipo reajuste
			$scope.mCTas.cmbbx.MonedaReajuste.filtros.IdInstrumento.value = $scope.mCTas.datosTasa.Instrumento_IFRF.IdInstrumento;
			$scope.mCTas.cmbbx.MonedaReajuste.filtros.IdTipoInstrumento.value = $scope.mCTas.datosTasa.Instrumento_IFRF.IdTipoInstrumento;

			// moneda
			$scope.mCTas.cmbbx.oConfiguracionMonedaPagoInstrumento.filtros.IdTipoInstrumento.value = $scope.mCTas.datosTasa.Instrumento_IFRF.IdTipoInstrumento;
		}


		 $scope.$watch('mCTas.datosTasa.MonedaObjReajuste', function(newval,oldval){
			if ( newval && newval != oldval ) {
				$scope.mCTas.cmbbx.oConfiguracionMonedaPagoInstrumento.filtros.IdMoneda.value = newval.IdMoneda;
			}
		 });

		$scope.$watch('mCTas.datosTasa.Instrumento_IFRF', function(newval,oldval ){
			configDataCombobox();
			if ( newval && newval != oldval ) {
				if ( $scope.mCTas.datosTasa.Instrumento_IFRF.IdClasificacionTipoInstrumento === 'IF') {
				
					var verificacion = $scope.mCTas.datosTasa.FuenteObj.Codigo === "RISK" && $scope.mCTas.datosTasa.TipoRegistroObj.Valor === "TASA" && $scope.mCTas.datosTasa.TipoTasaObj.Codigo === "TASA";

					if (verificacion) {
						monedasIF( $scope.mCTas.datosTasa.Instrumento_IFRF.Nemotecnico);
					} else if ($scope.mCTas.datosTasa.Instrumento_IFRF.Nemotecnico === "PDBC" || $scope.mCTas.datosTasa.Instrumento_IFRF.Nemotecnico === "PRBC") {
							$scope.mCTas.datosTasa.IdEmisor = "CENTRAL";	
					}					
					
				} else if ($scope.mCTas.datosTasa.Instrumento_IFRF.IdClasificacionTipoInstrumento === 'RF') {
					var verificacion = $scope.mCTas.datosTasa.FuenteObj.Codigo === "RISK" && $scope.mCTas.datosTasa.TipoRegistroObj.Valor === "TASA" && $scope.mCTas.datosTasa.TipoTasaObj.Codigo === "TASA";
					if (verificacion){
						$scope.mCTas.cmbbx.Moneda.filtros.IdMoneda.value= $scope.mCTas.datosTasa.Instrumento_IFRF.IdMoneda;
						$scope.mCTas.datosTasa.IdMonedaDef = $scope.mCTas.datosTasa.Instrumento_IFRF.IdMoneda;
					}
					
				}
			}
		});

		$scope.$watchGroup(['mCTas.datosTasa.FuenteObj','mCTas.datosTasa.TipoRegistroObj','mCTas.datosTasa.TipoTasaObj.Codigo'], function() {
			if ($scope.mCTas.datosTasa.TipoRegistroObj && $scope.mCTas.datosTasa.TipoRegistroObj.Valor) {
				$scope.mCTas.datosTasa.IdClasificacionTipoInstrumento = '';
				configDataCombobox();
			}
		})

		$scope.$watch('mCTas.datosTasa.IdClasificacionTipoInstrumento', function(newval,oldval) {
			if ( newval && newval != oldval ) {
				$scope.mCTas.datosTasa.setInstrumento_IFRF = "-1";
				if ($scope.mCTas.datosTasa.IdClasificacionTipoInstrumento !== 'RF') {
					configDataCombobox();
				}
			}
		});

		
	});
}());