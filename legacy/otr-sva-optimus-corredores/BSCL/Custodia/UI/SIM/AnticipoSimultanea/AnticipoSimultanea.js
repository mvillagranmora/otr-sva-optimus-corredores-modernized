angular.module('ModuloCustodia').controller('AnticipoSimultaneaCtrl',
				function($scope,$state,$translate,$resource,bcsComunes,
				bcsServicioPlataforma,bcsOperacionesComunes, bcsServiciosSimultanea, bcsConfiguracionCustodia)
{	
	$scope.data = {};	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.btnActivo = true;
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	

	//SETEO VARIABLES
	$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.showBtnCancelar = false;
	$scope.data.MontoAnticipoOriginal = 0;
	$scope.configCus = {};
	
	bcsConfiguracionCustodia.CONFIGURACION.promise.then(function(result){
		if(bcsConfiguracionCustodia.CONFIGURACION['CUS.RangoMaximoMontoAnticipoSimultanea'] !== undefined){
			$scope.configCus.RangoMaximoAnticipoSimultanea = Number(bcsConfiguracionCustodia.CONFIGURACION['CUS.RangoMaximoMontoAnticipoSimultanea']);
		}
	});

	var successCondicionLiquidacion = function(result)
	{
		if (result.P_OUT_ERRORESCONTADOR == "0"){
			if ($scope.data.kngFechaVencimiento < util.dateInicioDia(result.P_OUT_FECHALIQUIDACION)){
				$scope.data.oConfiguracionCondicionLiquidacion.idActual = 'T0';	
				$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
				bcsComunes.msg("error","Error","La fecha de liquidación no puede ser superior a la fecha de vencimiento");
			}	
			else
				$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia(result.P_OUT_FECHALIQUIDACION);
		}
		else{
			$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
			bcsComunes.log().error("Error." + result.P_OUT_ERRORESCONTADOR);
            var i = 0;
           
            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
            	bcsComunes.log().error("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
            }
	    }
	};
	var errorCondicionLiquidacion = function(result){
   		bcsComunes.log().info(JSON.stringify(result));
	};
	
	
	//COMBOBOX
	$scope.data.oConfiguracionCondicionLiquidacion={
		tipo : "combobox-condicion-liquidacion",
		configuracion : {},
		filtros : {
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : ""},
			EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	//FORMAT
	var formatInput = function(){		
		$scope.data.montoInversionOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};		
		$scope.data.montoPlazoOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.cantidadOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.precioOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Precio',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.tasaOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales, spinners: false};		
		$scope.data.montoAnticipoTotalOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.cantidadAnticipoOpt	= {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};	
		$scope.data.tasaAnticipoOpt	= {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales, spinners: false};
		$scope.data.montoAnticipoOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataSimultanea[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.tasaAnticipoDisabled = true;
		
		
	}
	
	$scope.data.plazoDisabled = true;

	//RECUPERO INFORMACION DE LA OPERACION
	var dataSimultanea = [];
	RecuperarData = function(){
		var objData = parametros;
		if (objData != undefined){
			$scope.data.tipoOperacion			= objData.NombreOperacion;
			//$scope.data.kngFolioCartera			= Number(objData.FolioCartera);
			$scope.data.kngFolioCartera			= Number(objData.FolioCartera).toString();
			$scope.data.instrumento				= objData.suby_Nemotecnico;			
			$scope.data.kngFechaInversion		= objData.FechaInversion; 
			$scope.data.kngFechaVencimiento		= objData.FechaVencimientoOperacion;	
			$scope.data.kngMontoInversion		= Number(objData.INGMontoATasaCompraActual); 
			$scope.data.kngMontoPlazo			= Number(objData.INGMontoAPlazo);
			$scope.data.kngCantidad				= Number(objData.INGCantidad);
			$scope.data.kngPrecio				= Number(objData.INGPrecioCompra);
			$scope.data.kngTasa					= Number(objData.INGTasaCompra);			
			$scope.data.condicionLiquidacion	= objData.NombreLargo;
			$scope.data.kngFechaCondLiquidOpe	= objData.FechaLiquidacion; //tag
			$scope.data.socioNegocio			= objData.Cliente;
			$scope.data.portafolio				= objData.Portafolio;
			
			
			//Seteo inicial
			$scope.data.kngCantidadAnticipo		= Number(objData.INGCantidad);
			$scope.data.kngTasaAnticipo			= Number(objData.INGTasaCompra);
			
			//Valores calculados
			$scope.data.kngPlazo				= util.date($scope.data.kngFechaVencimiento).diff(util.date($scope.data.kngFechaCondLiquidOpe), 'days') || 0;

			if (util.date($scope.data.kngFechaSistema) >= util.date($scope.data.kngFechaCondLiquidOpe)){
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'CUS',
					p_UsaReglasNegocio : 'S',
					p_IdCondicionDeLiquidacion : 'T0', 
					p_FechaDesde : util.dateSPdate($scope.data.kngFechaInversion),
					p_FechaHasta : util.dateSPdate($scope.data.kngFechaSistema),
					p_DiasHabiles : 'N',
					p_IdPais :  $scope.SesionIdPaisSistema
				};
				bcsServicioPlataforma.Plazo(datos,function(result)
				{
					if (result.P_OUT_PLAZODIAS && result.P_OUT_PLAZODIAS > 0){
						$scope.data.kngPlazoResidual	= Number(result.P_OUT_PLAZODIAS)
						$scope.data.showBtnCancelar		= true;
						CalculoMontoAnticipo("OPERACION");
					}
					else {
						$scope.data.kngPlazoResidual	= 0;
						bcsComunes.msg("error","Error",$translate.instant("UI_PLAZO_INVALIDO"));
					}
				}
				,function(result)
				{
					console.log("ERROR: " + JSON.stringify(result));
				});
			} else {
				$scope.data.kngPlazoResidual = $scope.data.kngPlazo;
				CalculoMontoAnticipo("OPERACION");
			}
			dataSimultanea.push(objData);
			
			$scope.data.oConfiguracionCondicionLiquidacion.filtros.IdTipoOperacion = dataSimultanea[0].IdTipoOperacion;		
			formatInput();
		}
	};
			
	//LLAMADA A SERVICIOS SP
	CalculoInteresesDeSimultanea = function(datos){
		var urlCalculoInteresesDeSimultanea = "/BSCL/Plataforma/GWJAVA/PLA/call?t=48"; 
		var r = $resource("", {},
			{
			'CalculoInteresesDeSimultanea': { method: "POST", url: urlCalculoInteresesDeSimultanea}
			});
		var post = r.CalculoInteresesDeSimultanea(datos);
		return post.$promise;
	};
	CalculoMontoAnticipo = function(origen){
		
		if ($scope.data.kngPlazoResidual == undefined ) return;
		
		var param = {};	
		param.p_IdEmpresa	 	= $scope.SesionIdEmpresa;  
		param.p_IdUsuario 		= $scope.SesionIdUsuario; 
		param.p_IdModulo  		= "CUS"; 
		param.p_FechaSistema	= util.dateInicioDia($scope.SesionFechaSistema);
		param.p_Precio			= Number($scope.data.kngPrecio) || 0;
		param.p_Plazo			= Number($scope.data.kngPlazo) || 0;
		param.p_PlazoResidual	= Number($scope.data.kngPlazoResidual) || 0;
		param.p_Base			= 30;
		
		param.p_Cantidad		= origen === "OPERACION" ? $scope.data.kngCantidad : ($scope.data.kngCantidadAnticipo || 0);
		param.p_Tasa			= origen === "OPERACION" ? $scope.data.kngTasa : ($scope.data.kngTasaAnticipo || 0);	
	
		CalculoInteresesDeSimultanea(param).then(function(result){
			$scope.data.MontoAnticipoOriginal = Number(result.P_OUT_VALORPRESENTE);
			if (origen === "OPERACION"){
				$scope.data.kngMontoAnticipoTotal = Math.round(Number(result.P_OUT_VALORPRESENTE));
				$scope.data.kngMontoAnticipo = Math.round(Number(result.P_OUT_VALORPRESENTE));
			}
			if (origen === "ANTICIPO")
				$scope.data.kngMontoAnticipo = Math.round(Number(result.P_OUT_VALORPRESENTE));
		});
	};

	//LOGICA
	cambiarFechaLiquidacion = function()
    {
		if ($scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== false && 
			typeof $scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== 'undefined' &&
			typeof dataSimultanea[0].IdTipoOperacion !== 'undefined'){	
			
			var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'CUS'; //sacar de variables de sistema
			datos.p_UsaReglasNegocio = 'S'; //sacar de variables de sistema
			
       		datos.p_FechaIngreso 	= $scope.SesionFechaSistema;
        	datos.p_IdTipoOperacion = dataSimultanea[0].IdTipoOperacion;
        	datos.P_IdCondicionDeLiquidacion = $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion;
        	datos.p_IdPais = $scope.SesionIdPaisSistema; 
	 		
			bcsServicioPlataforma.FechaLiquidacion(datos,successCondicionLiquidacion,errorCondicionLiquidacion);
		}
    };
	$scope.$watch('data.oConfiguracionCondicionLiquidacion.seleccionado',function(newval,oldval){
		if (newval){
			cambiarFechaLiquidacion();				
		}		
	});	
	$scope.$watchGroup(['data.kngCantidadAnticipo','data.kngTasaAnticipo'],function(newval,oldval){
		if (newval){
			if (newval[0] >0 && newval[1]>0){
				if (newval[0] > Number($scope.data.kngCantidad))
					bcsComunes.msg("error","Error","Cantidad a anticipar es superior a lo permitido");
				else{				
					$scope.btnAnticipar = ($scope.formAnticipoSimultanea.$valid ) ? true : false;
					CalculoMontoAnticipo("ANTICIPO");	
				}
			} else {
				$scope.btnAnticipar = false;
			}

		}		
	});
	
	$scope.$watch('data.kngMontoAnticipo',function(newval,oldval){
		if (newval && newval !== oldval && $scope.data.kngMontoAnticipo !== $scope.data.MontoAnticipoOriginal && $scope.configCus.RangoMaximoAnticipoSimultanea !== undefined){
			if(Math.abs(newval - $scope.data.MontoAnticipoOriginal) > $scope.configCus.RangoMaximoAnticipoSimultanea){
				bcsComunes.msg("error","Error","Monto a anticipar supera rango de lo permitido");
				$scope.data.kngMontoAnticipo = $scope.data.MontoAnticipoOriginal;
			}
		}		
	});
	
	//EJECUCION
	RecuperarData();
		

	//BOTONES
	$scope.data.cancelar = function(refrescar){
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};
	$scope.data.limpiar = function(){
		$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema); 
		$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);	
		
		$scope.data.kngMontoAnticipo 	= 0;
		$scope.data.kngTasaAnticipo		= 0;
		$scope.data.kngCantidadAnticipo	= 0;
		$scope.data.oConfiguracionCondicionLiquidacion.idActual = 'T0';	
		$scope.btnAnticipar = ($scope.formAnticipoSimultanea.$valid ) ? true : false;
		$scope.btnActivo = true;
		cambiarFechaLiquidacion();
	};	
		
	//LLAMAR SERVICIO DE ANTICIPO SIMULTANEA
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR == 0){	
			let anticipoParcial = false;
			for (let mensajeRespuesta of respuesta.P_OUT_MENSAJES){
				if (mensajeRespuesta.codigo === "ANTICIPO_PARCIALMENTE_CUBIERTA"){
					anticipoParcial = true;	
					break;
				}	 
			}

			if(anticipoParcial){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				
				bcsComunes.ventanaData(idVentana,'true');
				bcsComunes.ventanaCerrar(idVentana);
			}else{
				//Se debe mostrar el listado de garantías creadas			
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO")},idVentana);
				bcsComunes.ventanaData(idVentana,'true');
				bcsComunes.ventanaCerrar(idVentana);
			}
		}
		else
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
			

			$scope.btnAnticipar = ($scope.formAnticipoSimultanea.$valid ) ? true : false;			
		}
		$scope.btnActivo = true;				
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	var error = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},idVentana);
		
		
		$scope.btnAnticipar = ($scope.formAnticipoSimultanea.$valid ) ? true : false;	
		$scope.btnActivo = true;		
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	$scope.data.anticiparGarantia = function()
    {	
		bcsComunes.ventanaOverlay(idVentana, true);
		$scope.btnActivo = false;
		
		//DATOS SERVICIO
		var datos = {};
		datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
		datos.p_IdUsuario              	 	= $scope.SesionIdUsuario;
		datos.p_IdEmpleado              	= $scope.SesionIdEmpleado;
		datos.p_IdModulo                	= 'CUS';
		datos.p_UsaReglasNegocio        	= 'S';
		
		datos.p_FolioCartera				= dataSimultanea[0].FolioCartera;
		datos.p_IdPais						= $scope.SesionIdPaisSistema; 
	    datos.p_FechaSistema				= util.dateSPdate($scope.data.kngFechaSistema);
		datos.p_IdCondicionLiquidacion		= $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion;
	    datos.p_FechaCondicionLiquidacion	= util.dateSPdate($scope.data.kngFechaCondicionLiquidacion);	
		datos.p_CantidadAnticipo			= $scope.data.kngCantidadAnticipo;
		datos.p_TasaAnticipo				= Number($scope.data.kngTasaAnticipo) || 0;
		datos.p_MontoAnticipo				= Number($scope.data.kngMontoAnticipo) || 0;
		datos.p_PlazoOriginal 				= Number($scope.data.kngPlazo) || 0;
		datos.p_PlazoResidual 				= Number($scope.data.kngPlazoResidual) || 0;
		
		bcsComunes.log().info(JSON.stringify(datos));
       	bcsServiciosSimultanea.AnticipoSimultanea(datos,success,error); 
    };
    
//    CalculoMontoAnticipo("ANTICIPO");
});

