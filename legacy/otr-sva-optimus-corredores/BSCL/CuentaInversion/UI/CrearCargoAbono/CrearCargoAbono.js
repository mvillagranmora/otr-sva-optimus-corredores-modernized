angular.module('ModuloCuentaInversion').controller('CrearCargoAbonoCtrl',
				function($scope,$translate,$timeout,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
						bcsServiciosCuentaInversion,bcsServicioPlataforma)
{
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	$scope.data = {};
	$scope.data.kngFechaSistema 				= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaOperacion 				= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.optTipoCambio 					= {format: 'n'+(util.bcs_formato($scope.data.kngTipoCambio,'GEN','TipoCambio',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0, spinners: false};


	//COMBOBOX
	$scope.data.oSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa 		: {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente 		: {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
			EstadoCliente 	: {"field": "EstadoCliente","operator": "eq", "value"   : "ACT"},
			IdCliente 		: { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};
	$scope.data.oPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente 		: { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdPortafolio 	: { "field": "IdPortafolio","operator": "eq", "value"   :  0 },
			IdEstado 		: { "field": "IdEstado","operator": "eq", "value"   :  'ACT' }
		},
	};
	$scope.data.oSocioNegocioTransferido={ //debe corresponder a un relacionado
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa 		: {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente 		: {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
			EstadoCliente 	: {"field": "EstadoCliente","operator": "eq", "value"   : "ACT"}
		},
	};

	$scope.Cliente 		= {"field" : "IdCliente","operator": "eq", "value"   : 0 };
	$scope.Portafolio  	= [{"field" : "IdPortafolio","operator": "eq", "value"   : 0 }];
	$scope.data.oPortafolioTransferido={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros : {
			  complejo : [$scope.Cliente,$scope.Portafolio],
			  logic: "and",
			  filters: [
				{
				  logic: "or",
				  filters: $scope.Portafolio 
				},
				$scope.Cliente
			  ]
		},
	};
	
	$scope.data.oConfiguracionTipoMovimiento={
			tipo : "combobox-tipo-movimiento",
			configuracion : {},
			filtros : {
				IdModulo : { "field"   : "IdModulo","operator": "eq", "value"   : "CTA" },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};

	$scope.Origen					= { "field"   : "Origen","operator": "eq", "value"   : "CTA" };
	$scope.DesplegarEnCombobox 		= { "field"   : "DesplegarEnCombobox","operator": "eq", "value"   : "S" };
	$scope.DisponibleIngresoManual 	= { "field"   : "DisponibleIngresoManual","operator": "eq", "value"   : "S" };
	$scope.EstadoConcepto 	        = { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" };
	$scope.TipoConcepto				= []
	$scope.data.oConfiguracionConcepto={
			tipo : "combobox-concepto-cta-inv",
			configuracion : {},			
			filtros : {
				//complejo : [$scope.Origen,$scope.DesplegarEnCombobox,$scope.DisponibleIngresoManual,$scope.TipoConcepto],
				complejo : [$scope.DesplegarEnCombobox,$scope.DisponibleIngresoManual,$scope.TipoConcepto],
			  	logic: "and",
			  	filters: [
				{
				  logic: "or",
				  filters: $scope.TipoConcepto
				},
				//$scope.Origen,
				$scope.DesplegarEnCombobox,
				$scope.DisponibleIngresoManual,
				$scope.VisibleOptimus = { "field" : "VisibleOptimus","operator": "eq", "value" : "S" },
				$scope.EstadoConcepto,
			  ]},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	var dataSourceConsultarParametrosSistema = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering	: true, 
				serverPaging 	: true, 
				serverSorting	: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
						dataType: "json"
					}		
				},
				schema: {
			     	model: {
			     		fields: { Valor : { type: "string"}	}
			     	}
				},
				filter: [{
							logic: 'or', 
							filters: [{ field: "CodigoParametro", operator: "eq", value: 'CONCEPTOCUENTAINVERSIONDEFECTOABONO' }]		
						},		
						{ field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
			}
	});
	
	dataSourceConsultarParametrosSistema.fetch(function(){
			if(dataSourceConsultarParametrosSistema.data().length > 0){
				$.each(dataSourceConsultarParametrosSistema.data(), function(index, value){
					$scope.IdConceptoDefecto = value.Valor ? value.Valor : (value.ValorDefecto ? value.ValorDefecto : false);{
					$scope.modificarConcepto = true;
					}
				});				
			}
	});
	
		
	$scope.data.oConfiguracionCuentaInversion={
			tipo 			: "combobox-cuenta-inversion",
			configuracion 	: {},
			filtros 		: {
								 IdCliente 				: { "field"   : "IdCliente",			   "operator": "eq", "value"  :  0 },
								 IdPortafolio 			: { "field"   : "IdPortafolio",			   "operator": "eq", "value"  :  0 },			
								 EstadoCuentaInversion 	: { "field"   : "IdEstadoCuentaInversion", "operator": "eq", "value"  : "ACT" }
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};
	
	
	$scope.data.oConfiguracionTipoOperacion={
			tipo : "combobox-tipo-operacion",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "EstadoTipoOperacionTipoInstrumento","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.data.oConfiguracionCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {},
			filtros : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.data.oConfiguracionMoneda={
			tipo : "combobox-moneda",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				//IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
				//EstadoTipoOperacionYMoneda : { "field"   : "EstadoTipoOperacionYMoneda","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};




	var successCondicionLiquidacion = function(result)
	{
		if (result.P_OUT_ERRORESCONTADOR == "0")
			$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia(result.P_OUT_FECHALIQUIDACION);
		else {
			$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
			
            var i = 0;
            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);                
            }
	    }
	};
	var errorCondicionLiquidacion = function(result)
	{
		$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);	
		console.log("Error: " + JSON.stringify(result));
	};
	cambiarFechaLiquidacion = function()
    {
		if ($scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== false && 
			typeof $scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== 'undefined' &&
			$scope.data.oConfiguracionTipoOperacion.seleccionado !== false &&
			typeof $scope.data.oConfiguracionTipoOperacion.seleccionado !== 'undefined'){				
			var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'CTA'; //sacar de variables de sistema
			datos.p_UsaReglasNegocio = 'S'; //sacar de variables de sistema
			
       		datos.p_FechaIngreso = util.dateSPdate($scope.data.kngFechaOperacion);
        	datos.p_IdTipoOperacion = $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion;
        	datos.P_IdCondicionDeLiquidacion = $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion;
        	datos.p_IdPais = $scope.SesionIdPaisSistema; 
	 		
			bcsServicioPlataforma.FechaLiquidacion(datos,successCondicionLiquidacion,errorCondicionLiquidacion);
		} else {
			$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
		}
    };

	
	$scope.data.limpiar = function()
	{				
		//Variables
		$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		
		if ($scope.parametros.IdSocioDeNegocio === undefined){
			$scope.data.oSocioNegocio.idActual = false;
			$scope.data.oConfiguracionCuentaInversion.idActual = 0;
		}
		if ($scope.parametros.IdMoneda !== undefined && $scope.parametros.ActivarCombo)
			$scope.data.oConfiguracionCuentaInversion.idActual = 0;


		$scope.data.oSocioNegocioTransferido.idActual = false;
		$scope.data.oConfiguracionTipoMovimiento.idActual = 0;
		$scope.data.oConfiguracionConcepto.idActual = 0;
		$scope.data.kngMonto = undefined;
		$scope.data.descripcion = '';
		
		
		//Datos de la Operación
		$scope.data.kngFolio = undefined;
		$scope.data.kngTipoCambio = undefined;
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.oConfiguracionTipoOperacion.idActual = 0;
		$scope.data.oConfiguracionCondicionLiquidacion.idActual = 0;
		$scope.data.oConfiguracionMoneda.idActual = 0;
		
		$scope.data.indicadorBilateral = false;
	};
	
	$scope.$watchGroup(['data.oConfiguracionTipoOperacion.idActual','data.fechaOperacion','data.oConfiguracionCondicionLiquidacion.idActual'],function(newval,oldval){
		if (typeof $scope.data.oConfiguracionTipoOperacion.seleccionado !== 'undefined' &&
			typeof $scope.data.oConfiguracionTipoOperacion.idActual !== 'undefined'){
			cambiarFechaLiquidacion();	
		};
	});	
	$scope.$watch('data.oConfiguracionCuentaInversion.idActual',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionCuentaInversion.seleccionado !== 'undefined' && $scope.data.oConfiguracionCuentaInversion.idActual > 0){
			$scope.data.MontoSimbolo = $scope.data.oConfiguracionCuentaInversion.seleccionado.IdMoneda;
			$scope.data.MontoDecimales = (util.bcs_formato(0,'GEN','Monto',$scope.data.MontoSimbolo,'','')).cantidadDecimales;
		}
	},true);
	
	

	$scope.$watch('data.oSocioNegocio.seleccionado',function(newval,oldval){		
		if (typeof $scope.data.oSocioNegocio.seleccionado !== 'undefined' && typeof $scope.data.oSocioNegocio.idActual !== 'undefined'){
																																									   
			if ($scope.data.oSocioNegocio.seleccionado.BloqueadoPorCumplimiento === 'S' && $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVABN')
			{ 
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO")},$scope.winID);	
			}
		};			
	},true);
	
	
	$scope.$watch('data.oConfiguracionTipoMovimiento.idActual',function(newval,oldval){		
		if ($scope.data.oConfiguracionTipoMovimiento.idActual == 'MOVTRN'){
			$scope.data.showSocioNegocioTransferido = true;
			$scope.data.showPortafolioTransferido = true;
		}
		else{
			$scope.data.showSocioNegocioTransferido = false;
			$scope.data.showPortafolioTransferido = false;
		};
		
		while($scope.TipoConcepto.length > 0) {$scope.TipoConcepto.pop();}
		$scope.TipoConcepto.push(
			{ "field"   : "TipoConcepto","operator": "eq", "value"   : "AM" },
			{ "field"   : "TipoConcepto","operator": "eq", "value"   : $scope.data.oConfiguracionTipoMovimiento.seleccionado.Tipo || "" }
		);
		$scope.modificarConcepto = true;
	},true);
	
	$scope.$watch('data.oConfiguracionConcepto.seleccionado',function(newval,oldval){		
		if($scope.IdConceptoDefecto && $scope.data.oConfiguracionTipoMovimiento.seleccionado.Tipo == 'AB' && $scope.modificarConcepto){
			var valorantiguo = $scope.data.oConfiguracionConcepto.idActual;
			$scope.data.oConfiguracionConcepto.idActual = $scope.IdConceptoDefecto;
			if($scope.data.oConfiguracionConcepto.idActual !== $scope.data.oConfiguracionConcepto.seleccionado.IdConcepto && newval && oldval){
			$scope.data.oConfiguracionConcepto.idActual = valorantiguo;
			}
		}
		if(newval && oldval)
		$scope.modificarConcepto = false;
	},true);	
	
	//$scope.$watch('data.oSocioNegocioTransferido.idActual',function(newval,oldval){
	$scope.$watchGroup(['data.oSocioNegocioTransferido.idActual','data.oConfiguracionCuentaInversion.idActual'],function(newval,oldval){	
		if (typeof $scope.data.oSocioNegocioTransferido.seleccionado !== 'undefined' && 				   
		    typeof $scope.data.oConfiguracionCuentaInversion.seleccionado !== 'undefined' &&
		    $scope.data.oSocioNegocioTransferido.idActual > 0 &&
		    typeof $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente !== 'undefined' && 
		    typeof $scope.data.oSocioNegocio.seleccionado.IdCliente !== 'undefined' && 
		    typeof $scope.data.oPortafolio.seleccionado.IdPortafolio !== 'undefined')
		{
			var listPortafolio = [];

			//if ($scope.data.oSocioNegocio.seleccionado.IdCliente == $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente){
			if ($scope.data.oPortafolio.seleccionado.IdPortafolio === $scope.data.oPortafolioTransferido.seleccionado.IdPortafolio){				
				bcsComunes.msg("warning","warning",$translate.instant('UI_NO_PUEDE_REALIZAR_TRANSFERENCIA_AL_MISMO_PORTAFOLIO'));
				$scope.data.oSocioNegocioTransferido.idActual = false;
				$scope.data.oPortafolioTransferido.idActual = false;
			} 
			else{
				//Buscar portafolios del cliente a transferir
				var portafolioDatasource = new kendo.data.DataSource({
					type: "odata",
					serverFiltering:true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1365",
							dataType: "json"
							}
					},
					filter: [{ field: "IdCliente", operator: "eq", value: $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente},
							 { field: "IdMoneda", operator: "eq", value: $scope.data.oConfiguracionCuentaInversion.seleccionado.IdMoneda},
							 { field: "IdEstadoCuentaInversion", operator: "eq", value: 'ACT'},
							 { field: "IdEstadoPortafolio", operator: "eq", value: 'ACT'}							 
							]
				});	
				portafolioDatasource.fetch().then(function(){				
					while($scope.Portafolio.length > 0) {
						$scope.Portafolio.pop();
					}

					for (var i = 0; i<portafolioDatasource.data().length;i++) {
						$scope.Cliente.value = $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente; 

						if (portafolioDatasource.data()[i].IdPortafolio !== $scope.data.oPortafolio.seleccionado.IdPortafolio)
							$scope.Portafolio.push({"field" : "IdPortafolio","operator": "eq", "value" : Number(portafolioDatasource.data()[i].IdPortafolio) });
					}

					if ($scope.Portafolio.length == 0)
						bcsComunes.msg("warning","warning",$translate.instant('UI_NO_EXISTEN_PORTAFOLIO_MONEDA'));
						$scope.data.oPortafolio.idActual = false;
				});
			};
		}else{
			   if (typeof $scope.data.oSocioNegocioTransferido.seleccionado !== 'undefined' && 				   
			       typeof $scope.data.oConfiguracionCuentaInversion.seleccionado !== 'undefined' &&
			       $scope.data.oSocioNegocioTransferido.idActual > 0 &&
			       typeof $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente !== 'undefined' && 
			       typeof($scope.parametros.IdCliente) && 
			       typeof($scope.parametros.IdPortafolio))
			   {
				  var listPortafolio = [];

				
				if ($scope.parametros.IdPortafolio === $scope.data.oPortafolioTransferido.seleccionado.IdPortafolio){				
					bcsComunes.msg("warning","warning",$translate.instant('UI_NO_PUEDE_REALIZAR_TRANSFERENCIA_AL_MISMO_PORTAFOLIO'));
					$scope.data.oSocioNegocioTransferido.idActual = false;
					$scope.data.oPortafolioTransferido.idActual = false;
				} 
				else{
					//Buscar portafolios del cliente a transferir
					var portafolioDatasource = new kendo.data.DataSource({
						type: "odata",
						serverFiltering:true,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1365",
								dataType: "json"
								}
						},
						filter: [{ field: "IdCliente", operator: "eq", value: $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente},
								 { field: "IdMoneda", operator: "eq", value: $scope.data.oConfiguracionCuentaInversion.seleccionado.IdMoneda},
								 { field: "IdEstadoCuentaInversion", operator: "eq", value: 'ACT'},
								 { field: "IdEstadoPortafolio", operator: "eq", value: 'ACT'}							 
								]
					});	
					portafolioDatasource.fetch().then(function(){				
						while($scope.Portafolio.length > 0) {
							$scope.Portafolio.pop();
						}

						for (var i = 0; i<portafolioDatasource.data().length;i++) {
							$scope.Cliente.value = $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente; 

							if (portafolioDatasource.data()[i].IdPortafolio !== $scope.parametros.IdPortafolio)
								$scope.Portafolio.push({"field" : "IdPortafolio","operator": "eq", "value" : Number(portafolioDatasource.data()[i].IdPortafolio) });
						}

						if ($scope.Portafolio.length == 0)
							bcsComunes.msg("warning","warning",$translate.instant('UI_NO_EXISTEN_PORTAFOLIO_MONEDA'));
							$scope.data.oPortafolio.idActual = false;
					});
				};
			}
			
			
			
			
			
			
			
			
			
			
			
		};	
	},true);
	
	
	
	$scope.data.CrearCargoAbono = function(datosIngreso)
    {
		bcsComunes.ventanaOverlay($scope.winID, true);
		$scope.data.accionDisabled = false;
		$scope.data.limpiarDisabled = false;

        //VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa                       = $scope.SesionIdEmpresa;
		datos.p_IdUsuario                       = $scope.SesionIdUsuario;
		datos.p_IdModulo                        = 'CTA';
		datos.p_simularOperacion                = 'N';
		datos.p_UsaReglasNegocio                = 'S';

		datos.p_FechaSistema                    = util.dateSPdate($scope.data.kngFechaSistema);
		datos.p_CargoAbonoAGenerar				= [{"NumeroCargoAbono" 				: 1,
													//"IdPortafolio"				: $scope.data.oPortafolio.seleccionado.IdPortafolio || 0,
													"IdPortafolio"					: (typeof $scope.parametros.IdPortafolio !== 'undefined') ? $scope.parametros.IdPortafolio : $scope.data.oPortafolio.seleccionado.IdPortafolio || 0,
												    "IdMoneda"						: $scope.data.oConfiguracionCuentaInversion.seleccionado.IdMoneda || '',
												    "IdTipoMovimiento"				: $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento == 'MOVTRN' ? 'MOVCAR' : $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento || '',    
												    "IdConcepto"					: $scope.data.oConfiguracionConcepto.seleccionado.IdConcepto || '',
												    "Descripcion"					: $scope.data.descripcion || '',
												    "IdCondicionDeLiquidacion"		: 'T0',
												    "FechaCondicionDeLiquidacion" 	: util.dateSPdate($scope.data.kngFechaSistema),
												    "Monto"							: $scope.data.kngMonto || 0,
												    
												    "IdEjecutivo"					: 0,
												    "IdSucursal"					: 0,
												    "IdTipoProducto"				: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto ? $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto : '',
												    "IdTipoOperacion"				: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion ? $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion : '',
												    "IdOperacion"					: $scope.data.kngFolio ? $scope.data.kngFolio : 0,
												    "FechaOperacion"				: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto ? ($scope.data.kngFechaOperacion ? util.dateSPdate($scope.data.kngFechaOperacion) : '0001-01-01') : '0001-01-01',
												    "FechaLiquidacionOperacion"		: $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoProducto ? ($scope.data.kngFechaCondicionLiquidacion ? util.dateSPdate($scope.data.kngFechaCondicionLiquidacion) : '0001-01-01') : '0001-01-01',
												    "FechaVencimientoOperacion"		: null,
												    "IdMonedaOperacion"				: $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda ?  $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda : '',
												    "MontoOperacion"				: null,
												    "TipoCambioOperacion"			: $scope.data.kngTipoCambio ? $scope.data.kngTipoCambio : 0,
												    "IdContraparte"					: null,
												    "IdTipoComprobante"				: null,
												    "IdComprobante"					: null,
												    "FolioComprobante"				: null,
												    "IdComprobanteDetalle"			: null,
												    "IdComprobanteFormaDePago"		: null,
												    "IdTipoMovimientoAsociado"		: null,
												    "IdMovimientoAsociado"			: null,
												    "FechaComprobante"				: null,
												    
												    "IdPersonaPagoCobro"			: null,
												    "IdInstrumento"					: null,
												    "IdTipoInstrumento"				: null,
												    "Nemo"							: null,
												    "EsIndicadorBilateral"			: $scope.data.indicadorBilateral ? 'S' : 'N',
												    "EsCargoAbonoPorTraspaso"		: $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento == 'MOVTRN' ? 'S' : 'N',
												    "IdPortafolioTraspaso"			: $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento == 'MOVTRN' ? $scope.data.oPortafolioTransferido.seleccionado.IdPortafolio || 0 : null,
												    "GeneroContraparteEnTesoreria"	: 'N',
												    "CertificadoAutomatico"			: 'N',
												   }];
		datos.p_EsIngresoManual                 = 'S';
		

		if ($scope.data.oSocioNegocio.seleccionado.IdCliente !== $scope.data.oSocioNegocioTransferido.seleccionado.IdCliente &&
			$scope.data.oConfiguracionTipoMovimiento.idActual === 'MOVTRN'){
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_TRANSFERENCIA_ENTRE_DISTINTOS_CLIENTES') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					$scope.LlamadaCrearCargoAbono(datos);
				}else{
					$scope.data.oSocioNegocioTransferido.idActual = false;
					$scope.data.oPortafolioTransferido.idActual = false;
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			});
    	} else {
    		$scope.LlamadaCrearCargoAbono(datos);
    	};	
    };

    $scope.LlamadaCrearCargoAbono = function(datos){
    	bcsServiciosCuentaInversion.CrearCargoAbono(
       		datos,
       		function(respuesta){
				bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
       			  	
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
					$scope.data.limpiar();
				}
				bcsComunes.ventanaOverlay($scope.winID, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_CREAR_MOVIMIENTO_CUENTAINVERSION"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
       	);
    };
    
 


    $timeout(function(){
        //Cuando llegan los datos desde otra ventana(Ej. Cuenta Inversion Cliente)
    	if (typeof $scope.parametros.IdPortafolio !== 'undefined')
    	{
    		$scope.data.oSocioNegocio.idDefecto	= $scope.parametros.IdSocioDeNegocio;
    		$scope.data.oSocioNegocio.filtros.IdCliente.value = $scope.parametros.IdCliente;
    		$scope.data.oSocioNegocio.filtros.read = true;

    		$scope.data.oPortafolio.idDefecto	= $scope.parametros.IdPortafolio;
    		$scope.data.oPortafolio.filtros.IdPortafolio.value = $scope.parametros.IdPortafolio;
    		$scope.data.oPortafolio.filtros.read = true;

    		//Seteamos combo cuenta inversión
    		$scope.data.oConfiguracionCuentaInversion.filtros.IdPortafolio.value = $scope.parametros.IdPortafolio;
    		$scope.data.oConfiguracionCuentaInversion.filtros.IdCliente.value    = $scope.parametros.IdCliente;			
    		$scope.data.oConfiguracionCuentaInversion.idDefecto = $scope.parametros.IdCuentaInversion;
    		$scope.data.oConfiguracionCuentaInversion.filtros.read = true;
    	}
    },1000);
    var firstHeight = $('#Win_CrearCargoAbono');
        firstHeight.css('height', '390px');
});



