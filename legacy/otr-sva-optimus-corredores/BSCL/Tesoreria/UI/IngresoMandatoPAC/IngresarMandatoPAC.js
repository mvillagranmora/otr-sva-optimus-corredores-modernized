angular.module('ModuloFFM').controller('IngresoMandatoPACCtrl',function($scope,$translate,$timeout,bcsComunes,bcsServicioPlataforma,bcsServiciosTesoreria,$uibModal,OPCIONES_BCSALERTA){
	
	var imp = this;
	$scope.formData = {};
	$scope.ingresarPAC = {};	
	var winID = bcsComunes.ventanaWinId($scope);
	$scope.formData.folioSAAPV = true;
	$scope.formData.folioCSC = true;
	$scope.formData.AgregarCTA = false;
	$scope.dataCalendar = {};
	$scope.formData.Firma = true;
	imp.params = bcsComunes.ventanaParams(winID);
	$scope.folioCSC = {};
	$scope.oClienteSocioNegocio = {};
	
	$scope.dataCalendar.oConfiguracionFechaDesde = {
			min: util.dateInicioDia($scope.SesionFechaSistema)
	};
	
	$scope.oConfiguracionDiaDescuento = {};
	$scope.oConfiguracionDiaDescuento.conf = {"min" : 1, "max" : 31};
	$scope.esEdicion = imp.params.IdTipoSolicitud == 'Editar' ? true : false;
	if (imp.params.IdTipoSolicitud == 'Editar'){
		
		$scope.mandatoAgrupadorHeredadoDesdeLevantarPantalla= imp.params.ObjetoPAC.FolioPacMandatoAgrupador;	
		$scope.formData.NumeroMandato = imp.params.ObjetoPAC.NumeroMandato;
		$scope.formData.Receptor = imp.params.ObjetoPAC.Receptor;
		$scope.formData.Fecha = imp.params.ObjetoPAC.Fecha;
		$scope.formData.IdentServicio = imp.params.ObjetoPAC.FolioPacMandatoAgrupador;
		bcsComunes.ventanaTamanoAutomatico(winID,15);
	}
	
	
	$scope.oClienteSocioNegocio ={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
	};
	

	
	$scope.folioCSC ={
			tipo : "combobox-contrato-folio-csc",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				TipoPago : {"field": "TipoPago","operator": "eq", "value"   :  'PAC' },
				IdCliente : {"field": "IdCliente","operator": "eq", "value"   : 0},
				IdEstado : {"field": "IdEstado","operator": "neq", "value" : 'PAU'}
			},
			
	};
	
	$scope.estadoCSC = {
			tipo : "combobox-estado-csc",
			idActual : 0,
			mostrar : false
	};
	
	$scope.estadoSAAPV = {
			tipo : "combobox-estado-contrato-apv",
			idActual : 0,
			mostrar : false
	};
	
	$scope.estadoMandatoPAC = {
			tipo : "combobox-estado-mandato-pac",
			idActual : 0,
			filtros : {
				IdEstado : {"field": "IdEstado","operator": "neq", "value"   : 'PAU'}
			}
	};
	
  
    
	$scope.IdEstado        ={"field": "TipoPago","operator": "eq", "value"   :  'PAC' };
    $scope.Clasificacion  = [ { "field"   : "Estado","operator": "eq", "value" : "INGRESADO"},

    		{ "field"   : "Estado","operator": "eq", "value" : "PENDIENTE AUTORIZACION"  }]
 

    //CONFIGURACION DE COMBOBOX

    $scope.filComplejocsc={
    		configuracion   : {},
    		filtros : {
    		complejo : [$scope.IdEstado,$scope.Clasificacion],
    		logic: "and",
    		filters: [
                 {
                	 logic: "or",
                	 filters: $scope.Clasificacion

                 },
                 $scope.IdEstado       
                 ]},

              idActual     : false,
              seleccionado   : false,
              idDefecto     : ''             

     };
    

    
	$scope.folioSAAPV ={
			tipo : "combobox-folio-contratos-saapv",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdMedioRecaudacionFormaPago : {"field": "IdMedioRecaudacionFormaPago","operator": "eq", "value"   : 3},
				IdCliente : {"field": "IdCliente","operator": "eq", "value"   : 0},
				//IdEstado : {"field": "IdEstado","operator": "eq", "value"   :  'VIG' }

			},
			
	};
	
	
	$scope.CtaCorriente ={
			tipo : "combobox-persona-cuentas-bancarias",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				//IdTipoCuentaBancaria : {"field": "IdTipoCuentaBancaria","operator": "eq", "value"   :  'CUCO' },
				IdPersona: {"field": "IdPersona","operator": "eq", "value"   :  0 },
				IdEstado: {"field": "IdEstado","operator": "eq","value": 'ACT'}
			},
			
	};
	
	imp.abrirModal = function(xIdPersona,xCuentaBancaria, xCodigoTipoPersona, xIdCliente, xCuentasBancarias)
	{
		
		//$scope.CtaCorriente.filtros ={};
		var id1 = bcsComunes.ventanaAbrir("agregarModificarCuentaBancaria",{ CuentaBancaria: xCuentaBancaria , IdPersona: xIdPersona ,CodigoTipoPersona: xCodigoTipoPersona, IdCliente : xIdCliente, CuentasBancarias: xCuentasBancarias },false,false,imp.ventanaId); 
        var promesa=bcsComunes.ventanaPromesa(id1);                               
        promesa.then(function(data){ 
        	$scope.CtaCorriente.filtros.read = true;
    	}); 
	};
	
	
	imp.agregar = function() {
		imp.abrirModal($scope.oClienteSocioNegocio.seleccionado.IdPersona,"", "", $scope.oClienteSocioNegocio.seleccionado.IdCliente, []);
	};
	
	$scope.esPendienteAutorizacion = $scope.esEdicion && imp.params.ObjetoPAC.IdEstado === 'PAU';
	
	$scope.$watch('folioCSC.idActual', function(newval,oldval){
		
		if (newval != oldval){
			$scope.formData.folioSAAPV =$scope.formData.folioSAAPV = false;
			$scope.estadoCSC.mostrar = newval ? true : false;
			
			if($scope.folioCSC.seleccionado && $scope.folioCSC.seleccionado.IdFolioCSC){
				if($scope.esPendienteAutorizacion){
					$scope.formData.DiaDescuento = imp.params.ObjetoPAC.DiaDescuentoPorAutorizar || $scope.folioCSC.seleccionado.DiaDescuento;
					$scope.estadoCSC.IdDefectoEstadoCSC = imp.params.ObjetoPAC.EstadoContratoPorAutorizar || $scope.folioCSC.seleccionado.IdEstado;
					$scope.estadoCSC.idActual = imp.params.ObjetoPAC.EstadoContratoPorAutorizar || $scope.folioCSC.seleccionado.IdEstado;
				}else{
					$scope.formData.DiaDescuento = $scope.folioCSC.seleccionado.DiaDescuento;
					$scope.estadoCSC.IdDefectoEstadoCSC = $scope.folioCSC.seleccionado.IdEstado;
					$scope.estadoCSC.idActual = $scope.folioCSC.seleccionado.IdEstado;
				}
				
				$scope.estadoSAAPV.idActual = false;
				
			}
			
			if (newval === false){
				$scope.formData.folioSAAPV =$scope.formData.folioSAAPV = true;
				$scope.estadoCSC.IdDefectoEstadoCSC = undefined;
				$scope.estadoSAAPV.IdDefectoEstadoSAAPV = undefined;
				$scope.formData.DiaDescuento = undefined;
				$scope.estadoCSC.idActual = false;
				$scope.estadoSAAPV.idActual = false;
			}
			
		}
		

	});
	
	$scope.$watch('folioSAAPV.idActual', function(newval,oldval){
		
		if (newval != oldval){
			$scope.formData.folioCSC =$scope.formData.folioCSC = false;
			$scope.estadoSAAPV.mostrar = newval ? true : false;
			
			if($scope.folioSAAPV.seleccionado && $scope.folioSAAPV.seleccionado.IdContratoSAAPV){
				if($scope.esPendienteAutorizacion){
					$scope.formData.DiaDescuento = imp.params.ObjetoPAC.DiaDescuentoPorAutorizar || $scope.folioSAAPV.seleccionado.DiaDescuento;
					$scope.estadoSAAPV.IdDefectoEstadoSAAPV = imp.params.ObjetoPAC.EstadoContratoPorAutorizar || $scope.folioSAAPV.seleccionado.IdEstado;
					$scope.estadoSAAPV.idActual =  imp.params.ObjetoPAC.EstadoContratoPorAutorizar || $scope.folioSAAPV.seleccionado.IdEstado;
				}else{
					$scope.formData.DiaDescuento = $scope.folioSAAPV.seleccionado.DiaDescuento;
					$scope.estadoSAAPV.IdDefectoEstadoSAAPV = $scope.folioSAAPV.seleccionado.IdEstado;
					$scope.estadoSAAPV.idActual = $scope.folioSAAPV.seleccionado.IdEstado;
				}
				
				$scope.estadoCSC.idActual = false;
			}
			
			if (newval === false){
				$scope.formData.folioCSC =$scope.formData.folioCSC = true;
				$scope.estadoCSC.IdDefectoEstadoCSC = undefined;
				$scope.estadoSAAPV.IdDefectoEstadoSAAPV = undefined;
				$scope.formData.DiaDescuento = undefined;
				$scope.estadoCSC.idActual = false;
				$scope.estadoSAAPV.idActual = false;
				
			}

		}

	});
	
	$scope.$watch('oClienteSocioNegocio.idActual', function(newval,oldval){
		
		if (newval != oldval){
			$scope.formData.AgregarCTA =$scope.formData.AgregarCTA = true ;

		}
		
		if (newval === false){

			$scope.formData.AgregarCTA =$scope.formData.AgregarCTA = false ;
		}
		

	});
	

	$scope.$watch('CtaCorriente.seleccionado.NumeroCuentaBancaria', function(newval, oldval) {
		if (imp.params.IdTipoSolicitud == 'Editar'){
    			if ($scope.CtaCorriente.seleccionado != undefined && $scope.CtaCorriente.seleccionado) {
    			var folioAgrupador 	= $scope.CtaCorriente.seleccionado.NumeroCuentaBancaria;
    			var codBanco 		= $scope.CtaCorriente.seleccionado.CodigoBanco;
    			var IdentServicio	= "";
    			IdentServicio = generadorMAgrupador();
    			if(oldval == undefined){
    				if($scope.mandatoAgrupadorHeredadoDesdeLevantarPantalla != IdentServicio){
    					//asumimos que existe cambio respecto al IdentServicio "correcto" prendemos check y cliente asume el riesgo de error
    					$scope.formData.deshabilitaCheckIndent = false;
    					//activamos check marcando como "modificado
    					$scope.formData.editarIndent= true;
    				}else{
    						$scope.formData.deshabilitaCheckIndent = false;
    				}
    			}else{
    				if (newval != oldval){
    					$scope.formData.IdentServicio = IdentServicio;	
    					$scope.formData.deshabilitaCheckIndent = false
    					//en caso de cambiar cuenta bancaria se apaga el check ya que el sistema le sugiere el folio agrupador
    					$scope.formData.editarIndent= false;
    				}
    			}
			
			
			
    			} else {
    				$scope.formData.deshabilitaCheckIndent = true;
    			}

			
		}else{
    
    //asumimos que entraron por boton ingresar y se mantiene comportamiento original
    			if ($scope.CtaCorriente.seleccionado != undefined && $scope.CtaCorriente.seleccionado) {
    				var folioAgrupador 	= $scope.CtaCorriente.seleccionado.NumeroCuentaBancaria;
    				var codBanco 		= $scope.CtaCorriente.seleccionado.CodigoBanco;
    				var IdentServicio	= "";
				
    				if (newval != oldval){
					
    					IdentServicio = generadorMAgrupador();
    					$scope.formData.IdentServicio = IdentServicio;	
    					$scope.formData.deshabilitaCheckIndent = false;
    				}
		
    			} else {
    					$scope.formData.deshabilitaCheckIndent = true;
    			}
		
		}
		
		
		
	});
	
	$scope.$watch('formData.editarIndent', function(newval, oldval) {
		
		if(!$scope.formData.editarIndent){
			var folioAgrupador 	= $scope.CtaCorriente.seleccionado.NumeroCuentaBancaria;
			var codBanco 		= $scope.CtaCorriente.seleccionado.CodigoBanco;
			var IdentServicio	= "";
			
			if (folioAgrupador != undefined && codBanco != undefined){
				
				IdentServicio = (imp.params.IdTipoSolicitud == 'Editar') ? imp.params.ObjetoPAC.FolioPacMandatoAgrupador : generadorMAgrupador();
				$scope.formData.IdentServicio = IdentServicio;		
				$scope.formData.deshabilitaCheckIndent = false;
			}			
		}
		//else{
		//	$scope.formData.editarIndent = false;		
		//}
		
	});
	
	var invertirCadena = function(cadena) {
        var cadenaInvertida = "";
        for (var x = cadena.length - 1; x >= 0; x--) {
            cadenaInvertida = cadenaInvertida + cadena.charAt(x);
        }
        return cadenaInvertida;
    }
	
	var obtenerSumaPorDigitos = function(cadena) {
		var pivote 			= 2;
	    var longitudCadena 	= cadena.length;
	    var cantidadTotal 	= 0;
	    var b 				= 1;
	    
	    for (var i = 0; i < longitudCadena; i++) {
	        if (pivote == 8) {
	            pivote = 2;
	        }
	        
	        var temporal = parseInt("" + cadena.substring(i, b));
	        b++;
	        temporal *= pivote;
	        pivote++;
	        cantidadTotal += temporal;
	    }
	    cantidadTotal = 11 - cantidadTotal % 11;
	    return cantidadTotal;
    }
	
	function generadorMAgrupador(){
		
		var cuentaCorriente 	= $scope.CtaCorriente.seleccionado.NumeroCuentaBancaria;
		var codigoBanco 		= $scope.CtaCorriente.seleccionado.CodigoBanco;
		var temporal_agrupador 	= null;
		var modulo_11 			= 0;
		var diferencia 			= 0;
		var ceros 				= "0";
		var ident_servicio 		= null;
		var final_ident_servicio = null;
		
		temporal_agrupador 		= cuentaCorriente + codigoBanco;

		//eliminar los valores que no sean numeros
		temporal_agrupador      = String(temporal_agrupador).replace(/[^0-9]/g, '');
		
		if (temporal_agrupador.length < 17) {
			modulo_11 = obtenerSumaPorDigitos(invertirCadena(temporal_agrupador));
			if (modulo_11 == 10) {
				modulo_11 = 1;
			}
			if (modulo_11 == 11) {
				modulo_11 = 0;
			}
			temporal_agrupador += modulo_11;
			diferencia = 17 - temporal_agrupador.length;
			
			for(var i = 0; i < diferencia-1; i++) {
				ceros += "0";
				
			}
			ident_servicio = ceros+temporal_agrupador;
			final_ident_servicio = ident_servicio;
		} else if (temporal_agrupador.length > 17){
			diferencia = temporal_agrupador.length - 17;
			temporal_agrupador = temporal_agrupador.substring(diferencia,temporal_agrupador.length);
			final_ident_servicio = temporal_agrupador;
		}
		else{
			final_ident_servicio = temporal_agrupador;
		}
		return final_ident_servicio;
		
	}

	
	$scope.formData.IngresarPAC = function()
    {	

		var folio_MA = 0;
		var idPort = 0;
		
		bcsComunes.ventanaOverlay(winID, true);
		
		if ($scope.CtaCorriente.seleccionado.EsPac == 'N'){
			
			bcsComunes.msg('advertencia',$translate.instant("UI_ERROR"), $translate.instant("UI_CUENTA_CAMBIO_PAC"));
			bcsComunes.ventanaOverlay(winID,false);
		}

		idPort = $scope.folioCSC.seleccionado.IdPortafolio ? $scope.folioCSC.seleccionado.IdPortafolio : $scope.folioSAAPV.seleccionado.IdPortafolio;
		if (!idPort){
			bcsComunes.msg('advertencia',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_ASOCIAR_CSC_SAAPV"));
			bcsComunes.ventanaOverlay(winID,false);
			return;
		}
		if($scope.formData.IdentServicio == ""){
			bcsComunes.msg('advertencia',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_LLENAR_IDENTSERVICIO"));
			bcsComunes.ventanaOverlay(winID,false);
			return;
		}
		if($scope.formData.IdentServicio.length < 17){
			bcsComunes.msg('advertencia',$translate.instant("UI_ERROR"), $translate.instant("UI_IDENTSERVICIO_FORMATO_INCORRECTO"));
			bcsComunes.ventanaOverlay(winID,false);
			return;
		}
		
		//var folio_CSC = $scope.folioCSC.seleccionado.FolioCSC ? $scope.folioCSC.seleccionado.FolioCSC : 0;
		var folio_CSC = $scope.folioCSC.idActual ? $scope.folioCSC.idActual : 0;
		var folio_SAAPV = $scope.folioSAAPV.idActual ? $scope.folioSAAPV.idActual :0;
		var firma = $scope.formData.Firma ? 'Si': 'No';
		var id_cliente = $scope.oClienteSocioNegocio.seleccionado.IdCliente ? $scope.oClienteSocioNegocio.seleccionado.IdCliente : imp.params.ObjetoPAC.IdCliente;
		
		folio_MA = $scope.formData.IdentServicio;

		
			 var datosIngresoPAC = {
					 p_IdEmpresa : $scope.SesionIdEmpresa, 
					 p_IdUsuario : $scope.SesionIdUsuario, 
					 p_IdModulo : 'TES',
					 p_simularOperacion : 'N',
					 p_UsaReglasNegocio : 'S',
					 p_IngresoPAC : [{
						 
						 	IdCliente 	     		    :	id_cliente ,
					    	IdPortafolio          		:	idPort,
					    	FolioCSC         			:	folio_CSC,
					    	CuentaCorriente         	:	$scope.CtaCorriente.seleccionado.NumeroCuentaBancaria,
					    	FolioSAAPV  				:	folio_SAAPV,
					    	FolioPACMandatoAgrupador	:   1,//$scope.formData.FolioPACMA
					    	Receptor					:	($scope.formData.Receptor) ? $scope.formData.Receptor : '',	
					    	Fecha						:	($scope.formData.Fecha) ? $scope.formData.Fecha : '1900-01-01',
					    	NumeroMandato				:	($scope.formData.NumeroMandato) ? $scope.formData.NumeroMandato : 0,
					    	Firma						:	firma,
					    	IdentServicio               :	folio_MA,
					    	IdEmpresa					:	1,
					    	IdCuentaBancaria			:	$scope.CtaCorriente.seleccionado.IdCuentaBancaria,
					    	Canal						:	'OPTIMUS',
					    	TipoPago					:	$scope.CtaCorriente.seleccionado.EsPac,
					    	IdMandatoPACBase            :   $scope.formData.IdMandatoPACBase,
					    	DiaDescuento				:	$scope.formData.DiaDescuento,
							EstadoContrato				:	folio_CSC != 0 ? $scope.estadoCSC.idActual || null : $scope.estadoSAAPV.idActual || null,
							EstadoMandato				:	$scope.estadoMandatoPAC.idActual || null
	
					    	
					 }],
					
			 }
			
			 if (imp.params.IdTipoSolicitud == 'Editar'){
				
				 	bcsServiciosTesoreria.ModificarPAC(datosIngresoPAC).$promise.then(function(respuesta){
			     	 console.log(respuesta);
			     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREACION_CONTRATO"),conflictos:util.parseServerResponse(respuesta)},winID);
			     		bcsComunes.ventanaOverlay(winID,false);
						bcsComunes.ventanaCerrar(winID);
			     		//bcsConfiguracionFFM.ingresarCerrarVentana(id, winID); 
			     		
			     	 }else{
				 			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_FOLIO_YA_EXISTE"),conflictos:util.parseServerResponse(respuesta)}, winID);
				     		bcsComunes.ventanaOverlay(winID,false);
				     		
				     		
				     }
					 },function(respuesta){
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
						bcsComunes.ventanaOverlay(winID,false);
					 });
				 
			 }else{
			 
			 
				
			 	bcsServiciosTesoreria.IngresarMandatoPAC(datosIngresoPAC).$promise.then(function(respuesta){
				  console.log(respuesta);
				  
				  
		     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREACION_CONTRATO"),conflictos:util.parseServerResponse(respuesta)},winID);
		     		bcsComunes.ventanaOverlay(winID,false);
					bcsComunes.ventanaCerrar(winID);
		     		//bcsConfiguracionFFM.ingresarCerrarVentana(id, winID); 
		     		
		     	 }else{
			 			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_FOLIO_YA_EXISTE"),conflictos:util.parseServerResponse(respuesta)}, winID);
			     		bcsComunes.ventanaOverlay(winID,false);
			     		
			     		
			     }
				 },function(respuesta){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
					bcsComunes.ventanaOverlay(winID,false);
				 });
			 }
		
		}
	

    	
	if (imp.params.ObjetoPAC ){
		

		$scope.CtaCorriente.IdDefectoCTACTE= imp.params.ObjetoPAC.IdCuentaBancaria;
		$scope.oClienteSocioNegocio.IdDefectoPortafolio= imp.params.ObjetoPAC.IdSocioDeNegocio;
		$scope.formData.IdDefectoCSC= imp.params.ObjetoPAC.FolioCSC;
		$scope.formData.IdDefectoSAAPV= imp.params.ObjetoPAC.FolioSAAPV;
		$scope.formData.IdMandatoPACBase = imp.params.ObjetoPAC.IdMandatoPAC;
		$scope.estadoMandatoPAC.IdDefectoEstadoMandatoPAC = imp.params.ObjetoPAC.IdEstado === 'PAU' ? imp.params.ObjetoPAC.EstadoPorAutorizar : imp.params.ObjetoPAC.IdEstado;
		$scope.formData.IdEstadoMandato = imp.params.ObjetoPAC.IdEstado;
		$scope.formData.NombreEstado = imp.params.ObjetoPAC.Estado;
	}
		
    	
	bcsComunes.ventanaTamanoAutomatico(winID,15);	

	var firstHeightCrear = $('#Win_IngresarMandatoPAC');
        firstHeightCrear.css('width', '940px');
});