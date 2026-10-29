angular.module('ModuloOrdenes').controller('AnticipoReportesCtrl',
				function($scope,$state,$translate,$resource,bcsComunes,
				bcsServicioPlataforma,bcsOperacionesComunes, bcsServicioOrden)
{	
	$scope.data = {};
	//var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.btnActivo = true;
	

	//SETEO VARIABLES
	$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaCondicionLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.showBtnCancelar = false;	

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
		$scope.data.montoInversionOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};		
		$scope.data.montoPlazoOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.cantidadOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.precioOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Precio',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.tasaOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales, spinners: false};		
		$scope.data.montoAnticipoTotalOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		$scope.data.cantidadAnticipoOpt	= {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};	
		$scope.data.tasaAnticipoOpt	= {format: 'n'+(util.bcs_formato(0,'CUS','Tasa',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales, spinners: false};
		$scope.data.montoAnticipoOpt = {format: 'n'+(util.bcs_formato(0,'CUS','Monto',dataReportes[0].suby_IdMonedaInstrumento,'','')).cantidadDecimales,min:0, spinners: false};
		
		
	}
	
	$scope.data.plazoDisabled = true;

	//RECUPERO INFORMACION DE LA OPERACION
	var dataReportes = [];
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
					p_FechaDesde : util.dateSPdate($scope.data.kngFechaSistema),
					p_FechaHasta : util.dateSPdate($scope.data.kngFechaVencimiento),
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
			dataReportes.push(objData);
			
			$scope.data.oConfiguracionCondicionLiquidacion.filtros.IdTipoOperacion = dataReportes[0].IdTipoOperacion;		
			formatInput();
		}
	};
			
	//LLAMADA A SERVICIOS SP
	CalculoInteresesDeReportes = function(datos){
		var urlCalculoInteresesDeReportes = "/BSCL/Plataforma/GWJAVA/PLA/call?t=48"; 
		var r = $resource("", {},
			{
			'CalculoInteresesDeReportes': { method: "POST", url: urlCalculoInteresesDeReportes}
			});
		var post = r.CalculoInteresesDeReportes(datos);
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
	
		CalculoInteresesDeReportes(param).then(function(result){			
			if (origen === "OPERACION"){
				$scope.data.kngMontoAnticipoTotal = Number(result.P_OUT_VALORPRESENTE);
				$scope.data.kngMontoAnticipo = Number(result.P_OUT_VALORPRESENTE);
			}
			if (origen === "ANTICIPO")
				$scope.data.kngMontoAnticipo = Number(result.P_OUT_VALORPRESENTE);
		});
	};

	//LOGICA
	cambiarFechaLiquidacion = function()
    {
		if ($scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== false && 
			typeof $scope.data.oConfiguracionCondicionLiquidacion.seleccionado !== 'undefined' &&
			typeof dataReportes[0].IdTipoOperacion !== 'undefined'){	
			
			var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'CUS'; //sacar de variables de sistema
			datos.p_UsaReglasNegocio = 'S'; //sacar de variables de sistema
			
       		datos.p_FechaIngreso 	= $scope.SesionFechaSistema;
        	datos.p_IdTipoOperacion = dataReportes[0].IdTipoOperacion;
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
					$scope.btnAnticipar = ($scope.formAnticipoReportes.$valid ) ? true : false;
					CalculoMontoAnticipo("ANTICIPO");	
				}
			} else {
				$scope.btnAnticipar = false;
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
		$scope.btnAnticipar = ($scope.formAnticipoReportes.$valid ) ? true : false;
		$scope.btnActivo = true;
		cambiarFechaLiquidacion();
	};	
		
	//LLAMAR SERVICIO DE ANTICIPO REPORTES
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR == 0){			 
			//Se debe mostrar el listado de garantías creadas	
				
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			
			bcsComunes.ventanaData(idVentana,'true');
			bcsComunes.ventanaCerrar(idVentana);
		}
		else
		{
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error","Error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));

			$scope.btnAnticipar = ($scope.formAnticipoReportes.$valid ) ? true : false;			
		}
		$scope.btnActivo = true;
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	var error = function(respuesta)
	{
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg("error","Error Interno",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
						
		$scope.btnAnticipar = ($scope.formAnticipoReportes.$valid ) ? true : false;	
		$scope.btnActivo = true;
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	$scope.data.anticiparGarantia = function()
    {
		//overlay.encendido = true;
		bcsComunes.ventanaOverlay(idVentana, true);
		$scope.btnActivo = false;
		
		//DATOS SERVICIO
		var datos = {};
		datos.p_IdPais						= $scope.SesionIdPaisSistema;
		datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
		datos.p_IdUsuario              	 	= $scope.SesionIdUsuario;
		datos.p_IdEmpleado              	= $scope.SesionIdEmpleado;
		datos.p_IdModulo                	= 'ORD';
		datos.p_UsaReglasNegocio        	= 'S';
		
		datos.p_Operacion					= [{"IdCartera"						: dataReportes[0].IdCartera
											, "FolioCartera"					: dataReportes[0].FolioCartera
											, "IdTipoOperacion"					: dataReportes[0].IdTipoOperacion
											, "IdOperacion"						: dataReportes[0].IdOperacion
											, "IdClasificacionTipoInstrumento" 	: dataReportes[0].IdClasificacionTipoInstrumentoOperacion
											, "CantidadAnticipo" 				: $scope.data.kngCantidadAnticipo
											, "MontoAnticipo" 					: Number($scope.data.kngMontoAnticipo) || 0
											, "TasaAnticipo" 					: Number($scope.data.kngTasaAnticipo) || 0
											, "IdCondicionLiquidacion" 			: $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion
											, "FechaAnticipo" 					: util.dateSPdate($scope.data.kngFechaSistema)}]
		
		bcsComunes.log().info(JSON.stringify(datos));
		bcsServicioOrden.AnticiparOrden(datos,success,error); 
    };
    
});

