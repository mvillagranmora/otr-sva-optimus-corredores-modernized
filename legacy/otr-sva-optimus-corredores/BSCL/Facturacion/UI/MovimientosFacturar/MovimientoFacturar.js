angular.module('ModuloFacturacion').controller('MovimientoFacturarCtrl',
		function($scope,$translate,$modal,$rootScope,
				bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,
				bcsMovimientoFacturarGrid, bcsOperacionesComunes)
{
	$scope.data	= {};
	//$scope.gridApiMovimientoFacturar = null;

	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);


	$scope.CanalCliente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "CLI" };
	$scope.CanalTrader	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "TRA" };
	$scope.CanalAgente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "AGE" };
	$scope.CanalOWP		= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "OWP" };
	
	$scope.data.filtrosCanalIngreso = {
		complejo : [$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente,$scope.CanalOWP],
		logic : "or",
		filters : [
			$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente, $scope.CanalOWP
		]
	};
		
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
	$scope.data.IdMonedaSistema = $rootScope.SesionIdMonedaSistema;
		
	$scope.data.oFechaMovimientoComprobanteDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oFechaMovimientoComprobanteHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.sumaCantidad = 0;
	$scope.sumaMonto = 0;
	
	$scope.data.oFechaTransaccionDesde = "";
	$scope.data.oFechaTransaccionHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	var DocIdentidadOperadoresDeRueda = [];

	bcsServiciosFacturacion.ConsultaOperadoresRueda().$promise.then(function(result){
		
		$scope.data.OperadoresRueda = result.d.results;
		if($scope.data.OperadoresRueda != undefined){
			$.each($scope.data.OperadoresRueda,function(key,value)
			{
				if($scope.data.OperadoresRueda[key].IdEstado == 'ACT' && $scope.data.OperadoresRueda[key].DocumentoIdentidad != null){
					DocIdentidadOperadoresDeRueda.push(
					{     
						DocumentoIdentidad				: $scope.data.OperadoresRueda[key].DocumentoIdentidad
				    });
				}
			});
					
		}
	});
	
	var datosEstadoModulo = {};
	datosEstadoModulo.p_IdEmpresa = $scope.SesionIdEmpresa; 
	datosEstadoModulo.p_IdModulo = 'FAC';
	datosEstadoModulo.p_IdUsuario = $scope.SesionIdUsuario;
	
	var errorEstadoModulo = function(respuesta){	
    	bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_NO_SE_OBTUVO_CIERRE_MODULO"));
    	return;
	};
	

	//GRILLA PRINCIPAL DE MOVIMIENTOS A FACTURAR
	$scope.confBasicaMovimientoFacturar = { 
		dataSource			: bcsMovimientoFacturarGrid.dataSourceMovimientoFacturar,
		columns				: bcsMovimientoFacturarGrid.columnasMovimientoFacturar(),
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
	/*	gridCreated			: function(g) {
			$scope.gridApiMovimientoFacturar = g;
		},*/
		////autoBind			: true,
		excel 				: { allPages : true },
		height				: 450,
		selectable 			: "multiple row",
		navigatable         : true,
		allowCopy 			: true,
		rowColorBackground : function(row) {
			console.log(row);
			switch(row["IdEstadoFacturacion"].toUpperCase()) {
				case "ANU":
					return "red";
					break;
				case "BLO":
					return "orange";
					break;	
				case "FAC":
					return "green";
					break;
				case "ING":
					return "black";
					break;	
			}
		},

		boldRow: function() {
			return "bold";
		},		
		
	};

	
	
	//SUB-GRILLA DE COBROS 
	$scope.confBasicaMovimientoCobros = function(dataItem)
	  {
		
		var filtrosMovimientoCobros = [{field: "IdMovimientoComprobante", operator: "eq", value: Number(dataItem.IdMovimientoComprobante)},
		                               {field: "IdEstado", operator: "eq", value: 'VIG'}];
		return {
			  dataSource		: bcsMovimientoFacturarGrid.dataSourceMovimientoCobros(filtrosMovimientoCobros),
			  columns			: bcsMovimientoFacturarGrid.columnasMovimientoCobros(),		
			  sortable			: true,
			  pageable			: true,		
			  height			: 180,
			  width				: 300,
			  selectable 		: "multiple row",
			  autoBind          : true,
			  navigatable       : true,
			  allowCopy 		: true
			  /*gridCreated 		: function(g) {
			  	$scope.ApiMovimientoCobros = g;
			  	},*/
	       };
	  }; 
	
	//SUB-GRILLA FORMAS DE PAGO
	$scope.confBasicaMovimientoFormasDePago = function(dataItem)
	  {
		var filtrosMovimientoForDePago = [{field: "IdMovimientoComprobante", operator: "eq", value: Number(dataItem.IdMovimientoComprobante)}];
		return {
			 dataSource			: bcsMovimientoFacturarGrid.dataSourceMovimientoFormasDePago(filtrosMovimientoForDePago),
		     columns			: bcsMovimientoFacturarGrid.columnasMovimientoFormasDePago(),		
		     sortable			: true,
		     pageable			: true,
		     height				: 120,
		     width				: 300,
		     selectable 		: "multiple row",
		     autoBind           : true,
		     navigatable        : true,
			 allowCopy 			: true
		  }
	  };
	
	
	//COMBOBOX
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionOperacion={
			tipo : "combobox-operacion",
			configuracion : {},
			filtros : { 	
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"},
				Comprobante : {"field"   : "Comprobante","operator": "eq","value"   : "S"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento : 
				   {
		             "field"   : "IdClasificacionTipoInstrumento",
		             "operator": "eq",
		             "value"   : ""
				   } 	
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	
	$scope.data.oConfiguracionEstadoMovimientoFactura={
			tipo : "combobox-estado-facturacion",
			configuracion : {},
			filtros : {
				        NombreEntidad : { "field": "NombreEntidad","operator": "eq", "value"   :  "FAC.MovimientoComprobante"}
			          },
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	
	$scope.data.oConfiguracionCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {}, 
			filtros : { 	
				Estado : {field : "EstadoTipoOperacionYCondicionDeLiquidacion", operator : "eq", value : "ACT"},
				IdTipoOperacion : {field : "IdTipoOperacion", operator : "eq", value : ""}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.data.oConfiguracionTipoComprobante={
			tipo : "combobox-tipo-comprobante",
			configuracion : {},
			filtros : {
			    IdEstadoFacturacion :	{field : "IdEstadoFacturacion", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
  	
	
	$scope.data.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
	};
	
	$scope.data.oConfiguracionTipoLiquidacionFactura={
			tipo : "combobox-tipo-liquidacion-comision-factura",
			configuracion : {}, 
			filtros : { 	
				Estado : {field : "IdEstado", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionInstrumento={
			buscar          : "-1"    
	};	
		
	
	$scope.buscar = function(){
		var filters = new Array();
	
		if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
	        filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});			
		
		if ($scope.data.oConfiguracionOperacion.seleccionado.AccionOperacion)
			filters.push( { "field": "AccionTipoOperacion","operator": "eq", "value" : $scope.data.oConfiguracionOperacion.seleccionado.AccionOperacion});
		
		if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
			filters.push( { "field": "IdTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento});
		
		//if ($scope.data.IdInstrumento)
		//    filters.push( { "field": "IdInstrumentoUnificado","operator": "eq", "value" : Number($scope.data.instrumento.IdInstrumento)});
		if ($scope.instrumentoDatos !== undefined){    		
			if ($scope.instrumentoDatos.IdInstrumento != "" && $scope.instrumentoDatos.IdInstrumento !== undefined && ($scope.instrumentoDatos.ConteoNemotecnico === undefined || $scope.instrumentoDatos.ConteoNemotecnico === 1) )
				filters.push({ field: "IdInstrumentoUnificado", operator: "eq", value: Number($scope.instrumentoDatos.IdInstrumento) });
					
			if ($scope.instrumentoDatos.Nemotecnico != "" && $scope.instrumentoDatos.ConteoNemotecnico > 1)
				filters.push({ field: "NemotecnicoInstrumento", operator: "eq", value: $scope.instrumentoDatos.Nemotecnico });
    	}
		
		if ($scope.data.oConfiguracionEstadoMovimientoFactura.seleccionado.IdEstado)
			filters.push( { "field": "IdEstadoFacturacion","operator": "eq", "value" : $scope.data.oConfiguracionEstadoMovimientoFactura.seleccionado.IdEstado});
		
		if ($scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion)
			filters.push( { "field": "IdCondicionLiquidacion","operator": "eq", "value" : $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion});
		
		if ($scope.data.oFechaMovimientoComprobanteDesde)
			filters.push( { "field": "FechaMovimientoComprobante","operator": "gte", "value" : util.dateSPdate($scope.data.oFechaMovimientoComprobanteDesde) });
						
		if ($scope.data.oFechaMovimientoComprobanteHasta)
			filters.push( { "field": "FechaMovimientoComprobante","operator": "lte", "value" : util.dateSPdate($scope.data.oFechaMovimientoComprobanteHasta) });
						
		if ($scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante)
			filters.push( { "field": "IdTipoComprobante","operator": "eq", "value" : $scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante});
		
		if (($scope.data.oClientePortafolio.seleccionado.IdCliente))	
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdCliente)});			
		
		if (($scope.data.oClientePortafolio.seleccionado.IdPortafolio))	
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)});
		
        if (($scope.data.oIndVentaCorta))	
			filters.push({ field: "EsVentaCorta", operator: "eq", value: 'S'});	
        
        if ($scope.data.oFechaTransaccionDesde)
			filters.push( { "field": "FechaTransaccion","operator": "gte", "value" : util.dateSPdate($scope.data.oFechaTransaccionDesde) });

		if ($scope.data.MarcaAsignacion)
			filters.push( { "field": "MarcaAsignacion","operator": "eq", "value" : $scope.data.MarcaAsignacion });
		if ($scope.data.IdTipoCanalIngreso)
			filters.push( { "field": "IdTipoCanalIngreso","operator": "eq", "value" : $scope.data.IdTipoCanalIngreso });
        	
						
		if ($scope.data.oFechaTransaccionHasta)
			filters.push( { "field": "FechaTransaccion","operator": "lte", "value" : util.dateSPdate($scope.data.oFechaTransaccionHasta) });

		if ($scope.data.oConfiguracionTipoLiquidacionFactura.seleccionado.IdTipoLiquidacionComisionFactura)
			filters.push( { "field": "IdTipoLiquidacionComisionFactura","operator": "eq", "value" : $scope.data.oConfiguracionTipoLiquidacionFactura.seleccionado.IdTipoLiquidacionComisionFactura});
		
		if ($scope.data.oCodigoOperador) {filters.push({ field: "CodigoOperador", operator: "eq", value: $scope.data.oCodigoOperador}); 
        
        }   
            
		
		$scope.gridApiMovimientoFacturar.dataSource.filter(filters);
		//if(!!$scope.gridApiMovimientoFacturar.tbody.find("td"))
		//$($scope.gridApiMovimientoFacturar.tbody.find("td")[1]).focus();
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0
	};
	
		
	$scope.limpiarFiltros = function()
	{	
		//$scope.data.portafolio = false;
		$scope.data.oConfiguracionInstrumento.buscar = "-1";		
		$scope.filtros.IdPortafolio = false;
		$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionOperacion.idActual = 0;
		$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionEstadoMovimientoFactura.idActual = 'ING';
		$scope.data.oConfiguracionCondicionLiquidacion.idActual = 0;
		$scope.data.oConfiguracionTipoComprobante.idActual = 0;
		$scope.data.oFechaMovimientoComprobanteDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.oFechaMovimientoComprobanteHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.oFechaTransaccionDesde = "";
		$scope.data.oFechaTransaccionHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.oCodigoOperador = undefined;
		$scope.gridApiMovimientoFacturar.dataSource.removeAll();	
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;
	};
	
	//OPCIONES
	//***********************************************
	// Crear movimiento a facturar otros conceptos. 
	//***********************************************	
	$scope.data.crearMovimientoOtrosConceptos = function () {
        var datos = {};
		datos.accion = 'NCA';
		datos.registro = '';

		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
    			
		var idModal = bcsComunes.ventanaAbrir("CrearOtrosConceptosModal", datos, false,false,idVentana);		
		var promesa = bcsComunes.ventanaPromesa(idModal);

		bcsComunes.ventanaOverlay(idVentana, true);
		promesa.then(function(data)
		  {
			$scope.gridApiMovimientoFacturar.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana, false);
		  });
    };

    
    
	//***********************************************
	// Modificar de Movimiento 
	//***********************************************	
    $scope.data.modificarMovimiento = function (MvtoCmbteTemp) {
		
      	if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
    			
		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionMovimientoFacturar === undefined)
		{	
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			return;
		}
		else
		{	
			bcsComunes.ventanaOverlay(idVentana, true);	
		};	

		var idModal = bcsComunes.ventanaAbrir("ModificarMovimientoModal", MvtoCmbteTemp[0],false,false,idVentana);				
		var promesa = bcsComunes.ventanaPromesa(idModal);

		promesa.then(function(data)
		  {
			$scope.gridApiMovimientoFacturar.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana, false);
		  });
    };

	$scope.data.modificarMontoLocal = function (MvtoCmbteTemp) {
		
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
		  bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		  return;
	  }
	  console.log(MvtoCmbteTemp[0])
	  if(MvtoCmbteTemp[0].NombreEstadoFacturacion !== 'INGRESADO'){
		bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
		return
  }
	  if(!(MvtoCmbteTemp[0].IdClasificacionTipoInstrumento === 'RV' || MvtoCmbteTemp[0].IdClasificacionTipoInstrumento === 'RF' || MvtoCmbteTemp[0].IdClasificacionTipoInstrumento === 'IF')){
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
			return
	  }

	  if(MvtoCmbteTemp[0].IdTipoInstrumento === 'SIM'){
		bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
		return
	  }

	  if(MvtoCmbteTemp[0].EsVentaCorta === "S"){
		bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
		return
  }
			  
	  //Valida que se hayan seleccionado movimientos
	  if ($scope.seleccionMovimientoFacturar === undefined)
	  {	
		  $scope.data.btnActivos   = true;     //Activa botones
		  bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
		  return;
	  }
	  else
	  {	
		  bcsComunes.ventanaOverlay(idVentana, true);	
	  };


	  MvtoCmbteTemp[0].validadorLocal = true;


	  var idModal = bcsComunes.ventanaAbrir("ModificarMovimientoModal", MvtoCmbteTemp[0],false,false,idVentana);				
	  var promesa = bcsComunes.ventanaPromesa(idModal);

	  promesa.then(function(data)
		{
		  $scope.gridApiMovimientoFacturar.dataSource.read();
		  bcsComunes.ventanaOverlay(idVentana, false);
		});
  };
    
    $scope.data.modificarPortafolioMovimientos = function (MvtoCmbteTemp) {
		
      	if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
    			
		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionMovimientoFacturar === undefined)
		{	
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			return;
		}

		let validaDocIdentidad = $scope.seleccionMovimientoFacturar[0].DocumentoIdentidad;
		let encuentraOperadorRueda = false;
		let distintosClientes = false;
		
		$.each(DocIdentidadOperadoresDeRueda,function(key,value){
			if(DocIdentidadOperadoresDeRueda[key].DocumentoIdentidad == validaDocIdentidad){
				encuentraOperadorRueda = true;
			}
		});
		if(encuentraOperadorRueda == false){
			bcsComunes.msg("warning","warning",$translate.instant('UI_CLIENTE_DISTINTO_OPERADOR_RUEDA'));
			return;
		}
		//VALIDA SI SON TODOS EL MISMO CLIENTE //seleccionMovimientoFacturar.DocumentoIdentidad
		$.each($scope.seleccionMovimientoFacturar,function(key,value)
		{
			if($scope.seleccionMovimientoFacturar[key].DocumentoIdentidad != validaDocIdentidad){
				distintosClientes = true;
			}
				
		});

		if(distintosClientes){
			bcsComunes.msg("warning","warning",$translate.instant('UI_MOVIMIENTOS_DISTINTO_CLIENTE'));
			return;
		}
		
		bcsComunes.ventanaOverlay(idVentana, true);	

		var idModal = bcsComunes.ventanaAbrir("ModificarPortafolioMovimientoModal", MvtoCmbteTemp,false,false,idVentana);				
		var promesa = bcsComunes.ventanaPromesa(idModal);

		promesa.then(function(data)
		  {
			$scope.gridApiMovimientoFacturar.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana, false);
		  });
    };
    
  
    
    
    //***********************************************
	// Costos de Movimiento 
	//***********************************************	
	$scope.data.costosMovimiento = function (MvtoCmbteTemp) {
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionMovimientoFacturar === undefined)
		  {	
			 $scope.data.btnActivos   = true;     //Activa botones
			 bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			 return;
		  }
		else
		{	
		    /*$.each($scope.seleccionMovimientoFacturar,function(key,value)
		      {
		      	 if ($scope.seleccionMovimientoFacturar[key].IdEstadoFacturacion !== 'ING')
		      	   {
		              $scope.data.btnActivos   = true;     //Activa botones
			          bcsComunes.msg("warning","warning",$translate.instant('MVTO_CMBTE_DIS_ING'));
			          return;
		           }
		           
		      });*/




			//bcsComunes.ventanaOverlay(idVentana, true);

			//Almacena seleccionado para ser enviado a pantalla de costos
			var aux = $.extend(true, {}, $scope.seleccionMovimientoFacturar);
    	    bcsOperacionesComunes.AlmacenarRegistrosGrilla(aux);

    	    //Carga Modal de costos
		    var idModal = bcsComunes.ventanaAbrir("MovimientoFacturarCostosModal", MvtoCmbteTemp,false,false,idVentana);		
		    var promesa = bcsComunes.ventanaPromesa(idModal);
		
		    //Accion a realizar posterior a cierre de pantalla modal
		    promesa.then(function(data)
		     {
			   $scope.gridApiMovimientoFacturar.dataSource.read();
			   bcsComunes.ventanaOverlay(idVentana, false);
		     });
		
		     
	     }; 	
    };
    
    	
		
	
	//***********************************************
	// Bloqueo de Movimiento
	//***********************************************	
	$scope.data.bloquearMovimiento = function() {
		
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}

		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionMovimientoFacturar === undefined)
		{	
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			return;
		}
		else
		{
			//Mensaje de confirmación
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_MOVIMIENTO_BLOQUEO"), idVentana);
			t.then(function(result)
			  {
				 if (result===OPCIONES_BCSALERTA.SI)
				   {
					 var MovimientosBloquear = [];
					 bcsComunes.ventanaOverlay(idVentana, true);
					 $scope.data.btnActivos  = false;   //Bloquea botones

					 //Recopila Id de movimientos seleccionados
					 $.each($scope.seleccionMovimientoFacturar,function(key,value)
					   {      		     
						 MovimientosBloquear.push(
						   {   
							 IdMovimientoComprobante: $scope.seleccionMovimientoFacturar[key].IdMovimientoComprobante,
						   });
					   });


					 //Parámetros de entrada al servicio de bloqueo.
					 var datosBloquear = {};

					 datosBloquear.p_IdEmpresa			= $scope.data.IdEmpresa;
					 datosBloquear.p_IdUsuario			= $scope.data.IdUsuario;
					 datosBloquear.p_IdModulo			= $scope.data.IdModulo;
					 datosBloquear.p_simularOperacion   = 'N';
					 datosBloquear.p_UsaReglasNegocio   = 'S';
					 datosBloquear.p_EsIngresoAutomatico = 'N';
					 datosBloquear.p_Estado	            = 'BLO';
					 datosBloquear.P_MOVIMIENTOFACTURAR	= MovimientosBloquear;  


					 //Resultado del servicio
					 var successBloquear = function(respuesta)
					   {	
						  bcsComunes.ventanaOverlay(idVentana, false);
						  if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
							{	
							  //Se actualiza los datos de la grilla	    			
							  bcsComunes.ventanaMsg({
													   conflictos:util.parseServerResponse(respuesta)
													},idVentana);

							  $scope.gridApiMovimientoFacturar.dataSource.read();					
							}
						  else
							{
							  if (respuesta.P_OUT_ERRORESCONTADOR > 0)  
								{
								  bcsComunes.ventanaMsg({
														   mensaje:$translate.instant(""),
															conflictos:util.parseServerResponse(respuesta)
														},idVentana);
								}
							  else
								 if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0)
								   {
									  bcsComunes.ventanaMsg({
															  mensaje:$translate.instant(""),
															  conflictos:util.parseServerResponse(respuesta)
															},idVentana); 
								   }
							}

						  $scope.data.btnActivos  = true;
						  $scope.gridApiMovimientoFacturar.dataSource.read();
					   };	    	

					 var errorBloquear = function(respuesta)
					   {	
						 bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						 bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));

						 bcsComunes.ventanaOverlay(idVentana, false);
						 $scope.data.btnActivos  = true; //Activa botones
					   };

					 //Llamada a servicio para bloquear comprobantes
					 bcsServiciosFacturacion.ModificarEstadoMovimiento(datosBloquear,successBloquear,errorBloquear);
				   }  

				 else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			  });
		}
	}

	
	//***********************************************
	// Desbloqueo de Movimiento 
	//***********************************************	
	$scope.data.desbloquearMovimiento = function() {
		
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}

		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionMovimientoFacturar === undefined)
		{
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			return;
		}
		else
		{	
			var MovimientosDesBloquear = [];
			$scope.data.btnActivos  = false;   //bloquea botones
			bcsComunes.ventanaOverlay(idVentana, true);

			//Recopila Id de movimientos seleccionados
			$.each($scope.seleccionMovimientoFacturar,function(key,value){      		     
				 MovimientosDesBloquear.push(
				 {   
					   IdMovimientoComprobante: $scope.seleccionMovimientoFacturar[key].IdMovimientoComprobante,
				 });
			});


			//Parámetros de entrada al servicio de DesBloqueo.
			var datosDesBloquear = {};

			datosDesBloquear.p_IdEmpresa	= $scope.data.IdEmpresa; 
			datosDesBloquear.p_IdUsuario	= $scope.data.IdUsuario;
			datosDesBloquear.p_IdModulo		= $scope.data.IdModulo;	  
			datosDesBloquear.p_simularOperacion = 'N';
			datosDesBloquear.p_UsaReglasNegocio = 'S';
			datosDesBloquear.p_EsIngresoAutomatico = 'N';
			datosDesBloquear.p_Estado	    = 'ING';	      	      	
			datosDesBloquear.P_MOVIMIENTOFACTURAR = MovimientosDesBloquear;

			//Resultado del servicio
			var successDesBloquear = function(respuesta)
			  {	   		
				bcsComunes.ventanaOverlay(idVentana, false);
				if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
				  {	
					//Se actualiza los datos de la grilla	    			
					bcsComunes.ventanaMsg({
											conflictos:util.parseServerResponse(respuesta)
										  },idVentana);

					$scope.gridApiMovimientoFacturar.dataSource.read();					
				  }
				else
				  {
					if (respuesta.P_OUT_ERRORESCONTADOR > 0)  
					  {
						bcsComunes.ventanaMsg({
												mensaje:$translate.instant(""),
												conflictos:util.parseServerResponse(respuesta)
											  },idVentana);
								}
					else
					  if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0)
						{
						  bcsComunes.ventanaMsg({
												  mensaje:$translate.instant(""),
												  conflictos:util.parseServerResponse(respuesta)
												},idVentana); 
						}
				  }
				$scope.data.btnActivos  = true;   //habilita botones
			};	    	

			var errorDesBloquear = function(respuesta)
			{	
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));

				$scope.data.btnActivos  = true;   //habilita botones
				bcsComunes.ventanaOverlay(idVentana, false);
				$scope.gridApiMovimientoFacturar.dataSource.read();
			};

			//Llamada a servicio para DesBloquear comprobantes
			bcsServiciosFacturacion.ModificarEstadoMovimiento(datosDesBloquear,successDesBloquear,errorDesBloquear);
		}
	}
	
	
	//***********************************************
	// Anulacion de Movimiento 
	//***********************************************	
	$scope.data.anularMovimiento = function() {
		
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
    			
		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionMovimientoFacturar === undefined)
		  { 
			 $scope.data.btnActivos   = true;     //Activa botones
			 bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			 return;
		  }
		else
		  {	
			//Mensaje de confirmación
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ANULAR_MOVIMIENTO_FACTURACION"), idVentana);
			t.then(function(result)
			  {
				 if (result===OPCIONES_BCSALERTA.SI)
				   {
					 var MovimientosAAnular = [];
					 $scope.data.btnActivos  = false;   //bloquea botones
					 bcsComunes.ventanaOverlay(idVentana, true);

					 //Recopila Id de movimientos seleccionados
					 $.each($scope.seleccionMovimientoFacturar,function(key,value){      		     
						MovimientosAAnular.push(
						 {   
						   IdMovimientoComprobante: $scope.seleccionMovimientoFacturar[key].IdMovimientoComprobante,
						   IdPortafolio : $scope.seleccionMovimientoFacturar[key].IdPortafolio,
						   IdCliente    : $scope.seleccionMovimientoFacturar[key].IdCliente,	      			   
						 });
					  });


					 //Parámetros de entrada al servicio de anulacion.
					 var datosAAnular = {};

					 datosAAnular.p_IdEmpresa	= $scope.data.IdEmpresa; 
					 datosAAnular.p_IdUsuario	= $scope.data.IdUsuario;
					 datosAAnular.p_IdModulo	= $scope.data.IdModulo;
					 datosAAnular.p_simularOperacion = 'N';
					 datosAAnular.p_UsaReglasNegocio = 'S';
					 datosAAnular.p_EsIngresoAutomatico = 'N';
					 datosAAnular.p_Glosa	    = 'Anulado por Sistema';	      	      	
					 datosAAnular.p_MovimientoAAnular = MovimientosAAnular;

					 //Resultado del servicio
					 var successAAnular = function(respuesta)
					   {	
						 bcsComunes.ventanaOverlay(idVentana, false);
						 if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
						   {	
							 //Se actualiza los datos de la grilla
							 bcsComunes.ventanaMsg({
													  conflictos:util.parseServerResponse(respuesta)
												   },idVentana);
							 $scope.gridApiMovimientoFacturar.dataSource.read();

						   }	    		         
						 else
						   {	 
							 if (respuesta.P_OUT_ERRORESCONTADOR > 0)  
							  {
								 bcsComunes.ventanaMsg({	    					                              
														  conflictos:util.parseServerResponse(respuesta)
													   },idVentana);
							  }
							 else
							   if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0)
								 {
									bcsComunes.ventanaMsg({  	    					                                 
															 conflictos:util.parseServerResponse(respuesta)
														  },idVentana); 
								 }
						   }
						 $scope.data.btnActivos  = true;   //habilita botones
						 $scope.gridApiMovimientoFacturar.dataSource.read();
					   };	    	

					 var errorAAnular = function(respuesta)
					   {	
						 bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						 bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));

						 $scope.data.btnActivos  = true;   //habilita botones
						 bcsComunes.ventanaOverlay(idVentana, false);
					   };

					 //Llamada a servicio para DesBloquear comprobantes
					 bcsServiciosFacturacion.AnularMovimiento(datosAAnular,successAAnular,errorAAnular);
				 }
			   else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			  });     
		 }
	};
	
	
	//***********************************************
	// Seleccionar todos los Movimientos 
	//***********************************************
	$scope.data.seleccionarTodosMovimientos = function() {	
		$scope.gridApiMovimientoFacturar.select($scope.gridApiMovimientoFacturar.tbody.find(">tr"));		
	};
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		$scope.gridApiMovimientoFacturar.clearSelection();
	};
	
	
	//*******************************************
	// Crear comprobante
	//*******************************************
	$scope.data.crearComprobante = function() {
		
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}

		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionMovimientoFacturar === undefined)
		{
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			return;
		}
		else
		{
			//Mensaje de confirmación
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_COMPROBANTE_FACTURACION"), idVentana);
			t.then(function(result)
			  {
				 if (result===OPCIONES_BCSALERTA.SI)
				   {
					  var MovimientoFacturar = [];
					  bcsComunes.ventanaOverlay(idVentana, true);
					  $scope.data.btnActivos  = false;

					  //Recopila datos de movimientos seleccionados
					  $.each($scope.seleccionMovimientoFacturar,function(key,value)
						{      		     
						   MovimientoFacturar.push(
							   {     
								 IdPortafolio			: $scope.seleccionMovimientoFacturar[key].IdPortafolio,  
								 IdCliente				: $scope.seleccionMovimientoFacturar[key].IdCliente,
								 IdMovimientoComprobante: $scope.seleccionMovimientoFacturar[key].IdMovimientoComprobante,
								 IdTipoComprobante		: $scope.seleccionMovimientoFacturar[key].IdTipoComprobante,
								 FechaComprobante       : util.dateSPdate($scope.seleccionMovimientoFacturar[key].FechaMovimientoComprobante)	      		                                  
							   });
						});
					  
					  //Ardena movimientos antes de enviar a facturar
					  var mvtoOrdenados = MovimientoFacturar;
					  
					  mvtoOrdenados.sort(function (a, b) {
						  if (a.IdMovimientoComprobante > b.IdMovimientoComprobante) {
						    return 1;
						  }
						  if (a.IdMovimientoComprobante < b.IdMovimientoComprobante) {
						    return -1;
						  }
						  // a must be equal to b
						  return 0;
					  });
					  					

					  //Parámetros de entrada al servicio
					  var datosFacturar = {};

					  datosFacturar.p_IdEmpresa			= $scope.data.IdEmpresa; 
					  datosFacturar.p_IdUsuario			= $scope.data.IdUsuario;
					  datosFacturar.p_IdModulo			= $scope.data.IdModulo;
					  datosFacturar.p_simularOperacion	= 'N';
					  datosFacturar.p_UsaReglasNegocio	= 'S';
					  datosFacturar.p_EsIngresoAutomatico = 'N';
					  datosFacturar.p_Glosa				= '';      	
					  datosFacturar.P_MOVIMIENTOFACTURAR = mvtoOrdenados;					  

					  var successFacturar = function(respuesta)
						 {	  	
							bcsComunes.ventanaOverlay(idVentana, false);
							bcsComunes.ventanaMsg({
								  conflictos:util.parseServerResponse(respuesta)
							 },idVentana);
							$scope.data.btnActivos  = true;
							$scope.gridApiMovimientoFacturar.dataSource.read();
						 };


					  var errorFacturar = function(respuesta)
						 {	
						   $scope.data.btnActivos  = true;     //Activa botones
						   bcsComunes.ventanaOverlay(idVentana, false);
						   bcsComunes.ventanaMsg({
								  mensaje:$translate.instant(""),
									 conflictos:util.parseServerResponse(respuesta)
								   },idVentana); 
						 };


					  //Llamada a servicio para crear comprobante de facturación()					  
					  bcsServiciosFacturacion.CrearComprobante(datosFacturar,successFacturar,errorFacturar);
				   }
				 else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			  });			
		}
	};
	//***********************************************
	// Opcion no habilitada 
	//***********************************************
    $scope.data.opcionNoHabilitada = function() 
     {
		bcsComunes.msg("warning","warning","Está opción no se encuentra habilitada.");
	 };
	
	 $scope.data.verMapaRelaciones = function (items) {
		 if (!items || !items[0])
			 return;
		 bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "MOVFACTURAR",IdEntidad : items[0].IdMovimientoComprobante},false,false );
	 };
	
	$scope.detalleMovimientoFacturar = function(datos){
		//setTimeout(bcsComunes.ventanaAbrir("DetalleMovimientoComprobante", {IdMovimientoComprobante: datos[0].IdMovimientoComprobante}, false, false, idVentana ), 3000);
			bcsComunes.ventanaAbrir("DetalleMovimientoComprobante", {IdMovimientoComprobante: datos[0].IdMovimientoComprobante}, false, false, idVentana );		
	};


	$scope.data.modificarMasivoComisiones = function () {
		bcsComunes.ventanaOverlay(idVentana, true);	

		var idModal = bcsComunes.ventanaAbrir("ModificarMasivoComisionesModal", null,false,false,idVentana);				
		var promesa = bcsComunes.ventanaPromesa(idModal);

		promesa.then(function(data)
		  {
			$scope.gridApiMovimientoFacturar.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana, false);
		  });
	};

	$scope.data.anularMovimientosPorAsignacionMasivaModal = function () {
		bcsComunes.ventanaOverlay(idVentana, true);	

		let idModal = bcsComunes.ventanaAbrir("AnularMovimientosPorAsignacionMasivaModal", null,false,false,idVentana);				
		let promesa = bcsComunes.ventanaPromesa(idModal);

		promesa.then(function(data)
		  {
			$scope.gridApiMovimientoFacturar.dataSource.read();
			bcsComunes.ventanaOverlay(idVentana, false);
		  });
	};

	let firstHeight = $('#Win_BlotterFacturacion');
        firstHeight.css('height', '728px');
        
		$scope.$watch('seleccionMovimientoFacturar',function(newval,oldval) {
			if (!newval) return;
			$scope.sumaCantidad = 0;
			$scope.sumaMonto = 0;
			var moneda = "USD";
			$.each(newval,function(key,value){
				if(isNaN(Number(value.Cantidad)) || value.Cantidad === ""){
					bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_CANTIDAD");
				}else{
					$scope.sumaCantidad = $scope.sumaCantidad + value.Cantidad;
				}
				if(isNaN(Number(value.Monto)) || value.Monto === ""){
					bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_MONTO");
				}else{
					$scope.sumaMonto = $scope.sumaMonto + value.Monto;
				}
			})
			if($scope.sumaCantidad != 0){
				$scope.sumaCantidad = util.bcs_formato($scope.sumaCantidad,'GEN','CANTIDAD',moneda,'','').valor;
			}
			if($scope.sumaMonto != 0 && util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor != 'Error'){
				$scope.sumaMonto = util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor;
			}else{
				$scope.sumaMonto = 0;
			}
		});

	var array = [		
	    { Estado: 'Ingresado', Color: 'black' },	    
	    { Estado: 'Facturado', Color: 'green' },
	    { Estado: 'Anulado', Color: 'red' },
	    { Estado: 'Bloqueado', Color: 'orange' }
	    
	];
	
		bcsComunes.tootipGlosa('toolMovimientoFacturar', array)        
});