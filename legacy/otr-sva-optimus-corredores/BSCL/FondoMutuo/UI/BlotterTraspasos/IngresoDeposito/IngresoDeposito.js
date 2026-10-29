(function() {
	angular.module('ModuloFFM').controller('IngresoDepositoCtrl',
	function($rootScope,$scope,$translate,$resource,$timeout,bcsComunes,
			 bcsAlerta,bcsServicioFondosMutuos, OPCIONES_BCSALERTA)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var x = 0;
		var IdEstadoDeposito = 0;
		var primeraCarga = 'SI';
		$scope. pctr= {};
		var depm = this;
		depm.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		var paramVentanaDistribuir = {};
		depm.IdMonedaFondo=$rootScope.SesionIdMonedaSistema;
		depm.MontoDeposito=0;
		depm.MontoDecimales=0;
		$scope.data = {};
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kOptFechaDeposito = {};
		$scope.kOptFechaDeposito.max = new Date(); //$scope.$root.SesionFechaSistema;
		$scope.data.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
		$scope.data.habilitaSucursal = false;
		depm.kngFechaDeposito 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		if(depm.parametrosEntrada.datosTraspaso){
			$scope.pctr=depm.parametrosEntrada.datosTraspaso;
			depm.MontoDeposito=$scope.pctr.Monto;
			depm.IdMonedaFondo=$scope.pctr.IdMonedaFondo;
			depm.MontoDecimales = util.bcs_formato(depm.MontoDeposito,'FMM','Monto',depm.IdMonedaFondo, '','').cantidadDecimales;
		};
		
		/*este campo se utiliza para bloquear el campo monto deposito cuando es llamado desde la gestion de traspaso*/
		depm.DeshabilitoMontoDeposito = false;
		
		//Cuenta bancaria del socio de negocio
		depm.oConfiguracionCuentaBancoSocioNegocio={
				tipo : "combobox-cuentas-bancarias-corredora",
				configuracion : {},
				filtros : {
					IdEstado  : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
					IdMoneda  : { "field"   : "IdMoneda", "operator": "eq", "value"   : depm.IdMonedaFondo  }, // Cuentas de la misma moneda del comprobante
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		//Al cambiar la "Cuenta Bancaria Socio de Negocio" se actualizan los datos de visualización.
		$scope.$watch('depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado',function(newval,oldval){
			
			if (typeof depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== 'undefined' && depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== false)
			{	
				depm.bancoDeposito = depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreBanco;			
				depm.numeroCuenta = depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
				$scope.data.habilitaSucursal = true;
			}else{
				$scope.data.habilitaSucursal = false;
			}
		});	
		depm.oConfiguracionTipoDeposito={
				tipo : "combobox-medio-pago-tesoreria",
				configuracion : {},
				filtros : {
					IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : depm.IdMonedaSistema },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};	
		
		depm.oConfiguracionSucursalBancaria={
				tipo : "combobox-sucursal-bancaria",
				configuracion : {},
				filtros : {
					IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : "" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		depm.oConfiguracionCuentaCorriente={
				tipo : "combobox-numero-cuentas-bancarias-corredora",
				configuracion : {},
				filtros : {
					IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : "" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		depm.filtroBanco = {
				IdPaisOperacion : {field: "IdPaisOperacion", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		}
		
		depm.filtroOrigen = {
				IdEstado	: {field: "IdEstado", operator: "eq", value: 'ACT'},
		}
		
		$scope.$watch('depm.bancoDeposito',function(newval, oldval){
			
			if(depm.bancoDepositoSeleccionado !== undefined && depm.bancoDepositoSeleccionado !== false){
				$scope.data.habilitaSucursal = true;
			//	depm.oConfiguracionSucursalBancaria.filtros = { IdBanco: { field: "IdBanco", operator: "eq", value: depm.bancoDepositoSeleccionado.IdBanco } };
			}else{
				$scope.data.habilitaSucursal = false;
			}	
		});
		
	
		depm.fnValidaDeposito = function()
		{ 
			var valido = false;
				
			if(

					depm.kngFechaDeposito 			=== undefined	||
					depm.kngFechaDeposito 			=== null		||
					depm.numeroCuenta				=== undefined	||
					depm.numeroCuenta				=== null		||
					depm.MontoDeposito 				=== undefined	||
					depm.MontoDeposito 				=== ""			||
					depm.MontoDeposito 				=== 0			||
					

					depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado	=== undefined	||
					depm.tipoDepositoSeleccionado	=== undefined 	||
					depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado	=== false		||
					depm.tipoDepositoSeleccionado	=== false	 
			)
			{
				return valido;
			}else{
				valido = true;
			}
			return valido;
		};
			
		depm.recaudar = function() {
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_PROCESAR" ) + ", ¿"  + $translate.instant("UI_DESEA_CONTINUAR" ) +"?",idVentana);
			t .then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						 
						 
						 var datos = {
								 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
								 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
								 p_IdModulo : 'FMM',
									 
								 p_Recaudacion : [{
						    		 	IdTraspaso		      		:	$scope.pctr.IdTraspaso,
						    		 	FechaDeposito				:	util.dateSPdate($scope.depm.kngFechaDeposito),
						    		 	IdMedioDePago				: 	$scope.depm.tipoDeposito,
						    		 	IdBanco						:	$scope.depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdBanco,
						    		 	NumeroCuentaBancaria		:	$scope.depm.numeroCuenta,
						    		 	IdCuentaBanco				:	$scope.depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdCuentaBanco,
						    		 	Sucursal					:	($scope.depm.sucursalSeleccionada.sucursal)?$scope.depm.sucursalSeleccionada.sucursal:'',
						    		 	MontoDeposito				:	$scope.depm.MontoDeposito,
						    		 	Observacion					:	($scope.depm.observacion)?$scope.depm.observacion:''
						    		 	
						    		 	
								 }]
								 
						 }
						bcsComunes.ventanaOverlay(idVentana,true);
						bcsServicioFondosMutuos.RecaudacionTraspaso(datos).$promise.then(function(respuesta){
					     	 console.log(respuesta);
					     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					     		//var id = bcsComunes.ventanaMsg({mensaje:"Operacion realizada"},idVentana);
					     		
					     		bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADO_DEL_PROCESO', 
									campoSecuencia : "id" ,
									nombreSecuencia : 'UI_SECUENCIA',
									datos : respuesta.P_OUT_MENSAJES } ,false,false,idVentana);
					     		
					     		
				     			bcsComunes.ventanaOverlay(idVentana,false);
				     			bcsComunes.ventanaCerrar(idVentana);
					     	 }else{
					     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_PROCESAR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
					     		bcsComunes.ventanaOverlay(idVentana,false);
					     	 }
				   		 },function(respuesta){
				   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_PROCESAR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				   			bcsComunes.ventanaOverlay(idVentana,false);
				   		 });
						
						
						
						
	
						 
				       };
				});
		 };
		 
		 
	

		depm.AsignaValoresDeposito = function(IdDeposito){
			if(IdDeposito === undefined)
				return;
				

		};

		$scope.$watchGroup(['depm.tipoDeposito',, 'depm.sucursal','depm.cuentaCorriente'],function(newval,oldval){
			
			if (newval !== undefined && x===0){
				if(depm.parametrosEntrada.modoVentana === "M"){
					depm.AsignaValoresDeposito(depm.parametrosEntrada.idRecaudacion);
					if(primeraCarga === 'SI')
						$scope.data.habilitaSucursal = false;
					
				}
				/*Solicita Traspaso*/
				if(depm.parametrosEntrada.Solicita === 'TRASPASO'){
					depm.MontoDeposito = depm.parametrosEntrada.MontoDeposito;
					depm.kngMontoDeposito = depm.parametrosEntrada.MontoDeposito;
					depm.origenPostFiltro = 'TRASPA';
					depm.DeshabilitoMontoDeposito = true;
				}
				primeraCarga = 'NO';
			}
			
		});
/*-------------------------------------------------------------------------------*/
	});
}());