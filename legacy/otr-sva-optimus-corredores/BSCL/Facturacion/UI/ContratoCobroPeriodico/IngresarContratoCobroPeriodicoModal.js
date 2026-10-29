angular.module('ModuloFacturacion').controller('IngresarContratoCobroPeriodicoModalCtrl', 
		        function($scope, bcsComunes, $timeout, bcsServiciosFacturacion,bcsServicioPlataforma,
		        		bcsIngContCobPeriodicoGrid, $translate)		      
{
	$scope.data	= {};	
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	//ASIGNAR CAMPOS
	$scope.data.oMontoMinimoCobrar = 0;
	$scope.data.oMontoMaximoCobrar = 0;
	
	//DISABLED CAMPOS
	$scope.data.activaroAgrDetCob = true;
	$scope.data.activaroMontoMinimoCobrar = true;
	$scope.data.activaroMontoMaximoCobrar = true;
 
	var v_CobroPeriodicoDetalle = [];
         
    //COMBOBOX	
	$scope.data.oTipoContratoCobro={
			tipo : "combobox-tipo-contrato-cobro-periodico",
			configuracion : {},
			filtros : { 	
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oFormatoContratoCobro={
			tipo : "combobox-formato-contrato-cobro-periodico",
			configuracion : {},
			filtros : { 	
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oMonedaContratoCobroLiquidacion={
		tipo : "combobox-moneda-tesoreria",
		configuracion : {},
		filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' }
				  }
	};
	
	$scope.data.oSocioNegocio={
			tipo : "combobox-portafolio-contrato-cobro-periodico",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				ValorContratoCobroPeriodico : {"field": "ValorContratoCobroPeriodico","operator": "neq", "value" : 1}
			},
	};

	$scope.data.oPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
	};
	
	$scope.data.oClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdEstado :	{"field" : "IdEstado", "operator" : "eq", "value" : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oCobroPeriodico={
			tipo : "combobox-cobro",
			configuracion : {},
			filtros : {
				   	    PermiteAsignarPortafolio : { "field"   : "PermiteAsignarPortafolio","operator": "eq", "value"   : "S" }
				      },
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oTipoCobroPeriodico={
			tipo : "combobox-tipo-cobro-periodico",
			configuracion : {},			
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConceptoCobroPeriodico={
			tipo : "combobox-concepto-facturacion",
			configuracion : {},
			filtros : {
				IdEstado : {"field"   : "IdEstado","operator": "eq","value" :'VIG' },
				IdTipoOperacion : {"field"   : "IdTipoOperacion","operator": "eq","value" :'' },
			
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	
	
	
	//GRILLAS	
	//Tipo Instrumento
	var dsGridIngContCobTipIns = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3048",
							dataType: "json"
						}
					},	    		    
	    		    schema: bcsIngContCobPeriodicoGrid.modeloIngContCobTipIns()
	    		}});
	
	$scope.confBasicaTipoInstrumento = { 
		dataSource          : dsGridIngContCobTipIns,		
		columns				: bcsIngContCobPeriodicoGrid.columnasIngContCobTipIns(),
		sortable			: true,
		//pageable			: true,
		//filterable			: false,
		userSettings        : true,
		height				: 200,
        selectable 			: "multiple row"		
	};
	
	//Tipo Instrumento Seleccionado(s)
	var defGridIngContCobTipInsSel = {
		 		          type : "array",
				          definition : {                                         
				        	             dataGridIngContCobTipInsSel     : [],                                        
                                         schema : bcsIngContCobPeriodicoGrid.modeloIngContCobTipInsSel(),

    			                       }
		};
	
	var dsGridIngContCobTipInsSel = new bcs.DataSource(defGridIngContCobTipInsSel)
	
	$scope.confBasicaTipoInstrumentoSel = { 
		dataSource          : dsGridIngContCobTipInsSel,		
		columns				: bcsIngContCobPeriodicoGrid.columnasIngContCobTipInsSel(),
		sortable			: true,
		//pageable			: true,
		userSettings        : true,
		height				: 200,
		selectable 			: "multiple row"		
	};
	
	//Rango Cobros	
	var defGridCobroPeriodico = {
		 		          type : "array",
				          definition : {                                         
				        	             dataGridCobro     : [],                                        
                                         schema : bcsIngContCobPeriodicoGrid.modeloCobroPeriodico(),

    			                       }
		};
	
	var dsGridCobroPeriodico = new bcs.DataSource(defGridCobroPeriodico)	  	
	$scope.confBasicaCobro = { 
		dataSource          : dsGridCobroPeriodico,		
		columns				: bcsIngContCobPeriodicoGrid.columnasCobroPeriodico(),
		sortable			: true,
		pageable			: true,
		userSettings        : true,
		height				: 150,
		selectable 			: "multiple row"
		
	};
	
	
	
	
	//VALIDACIONES
	//******************************************************
	// Valida monto maximo no sea mayor a monto minimo 
	//******************************************************	
	$scope.$watch('data.oMontoMaximoCobrar',function(newVal,oldVal) 
	  {
		if (!newVal)  return;
		
		if($scope.data.oMontoMaximoCobrar < $scope.data.oMontoMinimoCobrar)	  	   		
			bcsComunes.msg("error","Error","Monto maximo a cobrar no puede ser mayor a monto minimo");	
			return;
	  });
	
	$scope.$watch('data.oMontoMinimoCobrar',function(newVal,oldVal) 
	  {
		if (!newVal) return;
		
		if(($scope.data.oMontoMaximoCobrar < $scope.data.oMontoMinimoCobrar) && ($scope.data.oMontoMaximoCobrar !== 0) )
			bcsComunes.msg("error","Error","Monto maximo a cobrar no puede ser mayor a monto minimo");	
			return;
	  });
	
	
	//****************************************************************************
	// Habilita monto minimo y maximo a cobrar asociado al tipo de cobro y rango 
	//****************************************************************************	
	$scope.$watch('data.oCobroPeriodico.seleccionado',function(newVal,oldVal) 
	  {
		if (!newVal) return;
		
		if($scope.data.oCobroPeriodico.seleccionado.FormaCobro === "F" && $scope.data.oCobroPorRangos)
		  {	 
		    $scope.data.activaroMontoMinimoCobrar = false;
		    $scope.data.activaroMontoMaximoCobrar = false;
		  }  
		else
		  if($scope.data.oCobroPeriodico.seleccionado.FormaCobro === "F" && (!$scope.data.oCobroPorRangos))
		    {   
		      $scope.data.activaroMontoMinimoCobrar = true;
		      $scope.data.activaroMontoMaximoCobrar = true;
		    }
		  else
			if($scope.data.oCobroPeriodico.seleccionado.FormaCobro === "V") 
			 {
			   $scope.data.activaroMontoMinimoCobrar = false;
		       $scope.data.activaroMontoMaximoCobrar = false;	
			 };  
		
		return;
	  });
	
	$scope.$watch('data.oCobroPorRangos',function(newVal,oldVal) 
	  {
		//if (!newVal) return;
		
		if($scope.data.oCobroPeriodico.seleccionado.FormaCobro === "F" && $scope.data.oCobroPorRangos)
		  {	 
		    $scope.data.activaroMontoMinimoCobrar = false;
		    $scope.data.activaroMontoMaximoCobrar = false;
		  }  
		else
		  if($scope.data.oCobroPeriodico.seleccionado.FormaCobro === "F" && (!$scope.data.oCobroPorRangos))
		    {   
		      $scope.data.activaroMontoMinimoCobrar = true;
		      $scope.data.activaroMontoMaximoCobrar = true;
		    }
		  else
			if($scope.data.oCobroPeriodico.seleccionado.FormaCobro === "V") 
			 {
			   $scope.data.activaroMontoMinimoCobrar = false;
		       $scope.data.activaroMontoMaximoCobrar = false;	
			 };
		
		return;
	  });
	
	
	//*******************************************
	// Limpiar
	//*******************************************		
	$scope.data.Limpiar = function()	 
     {
		$scope.data.oTipoContratoCobro.idActual = 0;
		$scope.data.oFormatoContratoCobro.idActual = 0;
		$scope.data.oMonedaContratoCobroLiquidacion.idActual = 0;
		$scope.data.oSocioNegocio.idActual = 0;
		$scope.data.oPortafolio.idActual = 0;
		$scope.data.oClasificacionTipoInstrumento.idActual = -1;
		$scope.data.oConceptoCobroPeriodico.idActual = 0;
		dsGridIngContCobTipIns.removeAll();
		dsGridIngContCobTipInsSel.removeAll();
		$scope.data.oCobroPeriodico.idActual = 0;
		$scope.data.oTipoCobro = '';
		$scope.data.oCobroPorRangos = false;
		$scope.data.oMontoMinimoCobrar = 0;
		$scope.data.oMontoMaximoCobrar = 0;
		dsGridCobroPeriodico.removeAll();
     };
	
	
	
	//OPCIONES	
	//****************************************************************
	// Asigna a grilla tipo(s) de instrumento(s) segun clasificacion
	//****************************************************************
	$scope.$watch('data.oClasificacionTipoInstrumento.seleccionado',function(newVal,oldVal)
	   {
		 if (!newVal) return;	 
		 
		 if ($scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
		    dsGridIngContCobTipIns.query( {filter : { field: "IdClasificacionTipoInstrumento", operator: "eq", 
	    		    value: ($scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento) } }).then(function(){
		 		                 $scope.gridApiTipoInstrumento.dataSource.add = dsGridIngContCobTipIns.data();
			                    });
		 else
			dsGridIngContCobTipIns.query({filter : { field: "IdClasificacionTipoInstrumento", operator: "neq", 
	    		    value: ("") } }).then(function(){
		 		                 $scope.gridApiTipoInstrumento.dataSource.add = dsGridIngContCobTipIns.data();
			                    }); 
        			 	
       });
	
	//*******************************************************************
	// Habilita monto maximo y minimo a cobrar segun rango y tipo cobro
	//*******************************************************************
	$scope.$watch('data.oCobroPeriodico.seleccionado',function(newVal,oldVal)
	   {
		 if (!newVal) return;	 
		 
		 if ($scope.data.oCobroPeriodico.seleccionado.FormaCobro === 'F')
		    $scope.data.oTipoCobro = 'FIJO'
		 else
			$scope.data.oTipoCobro = 'VARIABLE'; 
		 
		 $scope.data.oMontoMinimoCobrar = 0;
		 $scope.data.oMontoMaximoCobrar = 0;
       });
	
	$scope.$watch('data.oTipoCobro.seleccionado',function(newVal,oldVal)
	   {
		 if (!newVal) return;	 
		 
		 if ($scope.data.oCobroPeriodico.seleccionado.FormaCobro === 'F')
		    $scope.data.oTipoCobro = 'FIJO'
		 else
			$scope.data.oTipoCobro = 'VARIABLE';        			 	
       });
	
	
	//***********************************************************
	// Asigna a grilla tipo(s) de instrumento(s) seleccionado(s)
	//***********************************************************
	$scope.agregarTipoInstrumento = function()
	{ 
		
		if(dsGridIngContCobTipIns.data().length === 0 || $scope.seleccionTipoInstrumento === undefined)
		  {
			 bcsComunes.msg("error","Error","Debe seleccionar tipo instrumento");
			 return;
		  };
		
		if(!$scope.data.oConceptoCobroPeriodico.seleccionado.IdConcepto)
		  {
			  bcsComunes.msg("error","Error","Debe seleccionar un concepto");
			  return;
		  };
		  		
		$.each($scope.seleccionTipoInstrumento,function(key,value){
            datoAgr = _.find(dsGridIngContCobTipInsSel.data(),function(elem) { return elem.IdTipoInstrumento === value.IdTipoInstrumento})
            
            if (!datoAgr)
              {	
		        dsGridIngContCobTipInsSel.add(
        			 {   
					     IdClasificacionTipoInstrumento: value.IdClasificacionTipoInstrumento,
					     IdTipoInstrumento     : value.IdTipoInstrumento,
        				 NombreTipoInstrumento : value.Nombre,
        				 IdConceptoCobroPeriodico : $scope.data.oConceptoCobroPeriodico.seleccionado.IdConcepto,
					     NombreConceptoCobroPeriodico : $scope.data.oConceptoCobroPeriodico.seleccionado.NombreConcepto,
        			 });
              };		 
	    });			
	};
	
	//*******************************************
	// Ingresar rango de cobro periodico
	//*******************************************
    $scope.agregarCobroPeriodico = function()
	{	
	  	if($scope.data.oCobroPeriodico.seleccionado.IdCobro === undefined){
				bcsComunes.msg("error","Error","Debe seleccionar un cobro");
				return;
			};	  	
	  				
			
		if($scope.data.oMontoMinimoCobrar > $scope.data.oMontoMaximoCobrar)
		 {	
			bcsComunes.msg("error","Error","Monto maximo a cobrar no puede ser mayor a monto minimo");	
			return;
		 };
				 
        datoAgr = _.find(dsGridCobroPeriodico.data(),function(elem) { return elem.IdCobro === $scope.data.oCobroPeriodico.seleccionado.IdCobro})
            
        if (!datoAgr)
          {	
		     dsGridCobroPeriodico.add(
        		{   
					IdCobro : $scope.data.oCobroPeriodico.seleccionado.IdCobro,
        			NombreCobro : $scope.data.oCobroPeriodico.seleccionado.NombreCobro,
					IdTipoCobroPeriodico : $scope.data.oCobroPeriodico.seleccionado.FormaCobro,
					TipoCobroPeriodico : $scope.data.oCobroPeriodico.seleccionado.FormaCobro,							                
					MontoMinimoCobrar : $scope.data.oMontoMinimoCobrar,
					MontoMaximoCobrar : $scope.data.oMontoMaximoCobrar,
					PorRango : $scope.data.oCobroPorRangos ?  'S' : 'N',
					CobroPeriodicoTem : [],
        		});
          };
	 };	
	 
	 
	 //*******************************************
	// Quitar rango de cobro periodico
	//*******************************************
    $scope.quitarCobroPeriodico = function()
	{	
	  	if ($scope.seleccionCobro === undefined)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_COBRO'));
			return;
		};
    	
    	dsGridCobroPeriodico.remove($scope.seleccionCobro);
    	
	 };
	 
	 
	 
	 
	//*******************************************
	// Ingresar rango de cobro periodico
	//*******************************************	 
	 $scope.data.agregarCobrosPeriodicoDet = function (Cobro) {		 
		//Valida que se hayan seleccionado movimientos
		if(!$scope.data.oMonedaContratoCobroLiquidacion.seleccionado.IdMoneda){
				bcsComunes.msg("error","Error","Debe seleccionar una moneda");
				return;
			};
			
			
		if ($scope.seleccionCobro === undefined)
		{	
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_COBRO'));
			return;
		}
		else
		{	
			bcsComunes.ventanaOverlay(idVentana, true);	
		};	
      	
		//Crear arreglo para enviar a pantalla
		var datosCobroDetalle = [];
		 
		 bcsComunes.ventanaOverlay(idVentana, true);
		    
      	 datosCobroDetalle.push(
	       {     
	         IdCobro     : Cobro[0].IdCobro,
	         NombreCobro : Cobro[0].NombreCobro,
	      	 PorRango	 : Cobro[0].PorRango,
	      	 TipoCobro   : Cobro[0].TipoCobroPeriodico,
	      	 Det         : Cobro[0].CobroPeriodicoTem,
	      	 IdMoneda    : $scope.data.oMonedaContratoCobroLiquidacion.seleccionado.IdMoneda	      	 
	       });
		
		
		var idModal = bcsComunes.ventanaAbrir("CobroDetalle", datosCobroDetalle[0],false,false,idVentana);
		var promesa = bcsComunes.ventanaPromesa(idModal);
		
		promesa.then(function(data)
		  {		
		  	if (angular.isArray(data)) 
		  	 {
		  		Cobro[0].CobroPeriodicoTem = data;
		  	 };	  				
		  });
		
		//bcsComunes.ventanaOverlay(idVentana, false);
    };
 
	
 	 
	//*******************************************
	// Ingresar contrato cobro periodico
	//*******************************************
	var successFacturar = function(respuesta)
	{
		bcsComunes.ventanaOverlay(idVentana, false); 

		if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0") 
		  {	
			var vMsg = bcsComunes.ventanaMsg({
	    		    	                        conflictos:util.parseServerResponse(respuesta)
	    		        	                 },idVentana);
			
			bcsComunes.ventanaPromesa(vMsg).then(function() {
				                                               bcsComunes.ventanaCerrar(idVentana);
	    			                                        });
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
		
	};
	
	var errorFacturar = function(respuesta)
	{		
		bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));	
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	$scope.data.ingresarContratoCobroPeriodicoModal = function()	
    {   		
		//Valida grilla de tipo instrumento
		if(dsGridIngContCobTipInsSel.data().length === 0){
				bcsComunes.msg("error","Error","Debe agregar al menos un tipo de instrumento asociado a contrato cobro");
				return;
			};
			
		//Valida grilla cobro
		if(dsGridCobroPeriodico.data().length === 0){
				bcsComunes.msg("error","Error","Debe agregar al menos un cobro asociado al contrato cobro");
				return;
			};
			
	 	
		//Tabla Contrato Cobro Detalle
		var v_ContratoCobroPeriodicoDetalleACrear = [];
		var v_dsGridIngContCobTipInsSel = dsGridIngContCobTipInsSel.data()|| [];		
	    $.each(v_dsGridIngContCobTipInsSel,function(key,value){	
		        var ObjIngContCobTipInsSel = {
			  	                               "NumeroContratoCobroPeriodico" : 1, 
	                                           "IdTipoInstrumento" : value.IdTipoInstrumento, 
	                                           "IdClasificacionTipoInstrumento" : value.IdClasificacionTipoInstrumento,
	                                           "IdConceptoCobroPeriodico" : value.IdConceptoCobroPeriodico
		 	                                 };
		        v_ContratoCobroPeriodicoDetalleACrear.push(ObjIngContCobTipInsSel);
	    });
	    
	    
	    //Tabla Cobro Periodico
		var v_CobroPeriodicoACrear = [];		
		var v_dsGridCobroPeriodico = dsGridCobroPeriodico.data()|| [];
		var v_CobroPeriodicoDetalleACrear = [];

	    for (var j = 0; j < v_dsGridCobroPeriodico.length;j++){	    
		        var ObjCobroPeriodico = {
			  	                            "NumeroContratoCobroPeriodico" : 1,
                                            "IdCobro" : v_dsGridCobroPeriodico[j].IdCobro,
                                            "MontoMinimoACobrar" : v_dsGridCobroPeriodico[j].MontoMinimoCobrar || 0,
                                            "MontoMaximoACobrar" : v_dsGridCobroPeriodico[j].MontoMinimoCobrar || 0,
                                            "TipoCobroPeriodico" : v_dsGridCobroPeriodico[j].IdTipoCobroPeriodico,
                                            "PorRango"           : v_dsGridCobroPeriodico[j].PorRango
		 	                            };
		        v_CobroPeriodicoACrear.push(ObjCobroPeriodico);
		        
		        //Tabla Cobro Periodico Detalle
		        for (var i = 0; i < v_dsGridCobroPeriodico[j].CobroPeriodicoTem.length;i++) {
		               var ObjCobroPeriodicoDet = {
			  	                                 "NumeroContratoCobroPeriodico" : 1,
			  	                                 "IdCobro" : v_dsGridCobroPeriodico[j].CobroPeriodicoTem[i].IdCobro,
                                                 "MontoDesde" : v_dsGridCobroPeriodico[j].CobroPeriodicoTem[i].MontoDesde,
                                                 "MontoHasta" : v_dsGridCobroPeriodico[j].CobroPeriodicoTem[i].MontoHasta,
                                                 "MontoCobroPeriodico" : v_dsGridCobroPeriodico[j].CobroPeriodicoTem[i].MontoCobroPeriodico || 0,
                                                 "PorcentajeCobroPeriodico" :v_dsGridCobroPeriodico[j].CobroPeriodicoTem[i].PorcentajeCobroPeriodico || 0
		 	                                   };
		             v_CobroPeriodicoDetalleACrear.push(ObjCobroPeriodicoDet);
		        };
           };


		

        //VARIABLES DEL SERVICIO
        //return;
		var datos = {};
		datos.p_IdEmpresa                       = $scope.SesionIdEmpresa;
		datos.p_IdUsuario                       = $scope.SesionIdUsuario;
		datos.p_IdModulo                        = 'FAC';
		datos.p_simularOperacion                = 'N';
		datos.p_UsaReglasNegocio                = 'S';
		datos.p_EsIngresoAutomatico             = 'S';
		
		datos.p_ContratoCobroPeriodicoACrear	= [{
			                                         "NumeroContratoCobroPeriodico" : 1,			                                         
			                                         "IdCliente"       : $scope.data.oSocioNegocio.seleccionado.IdCliente,
			                                         "IdPortafolio"    : $scope.data.oPortafolio.seleccionado.IdPortafolio,
			                                         "IdTipoContratoCobroPeriodico"  : $scope.data.oTipoContratoCobro.seleccionado.IdTipoContratoCobroPeriodico,
			                                         "IdMoneda"        : $scope.data.oMonedaContratoCobroLiquidacion.seleccionado.IdMoneda,
			                                         "FormatoContratoCobroPeriodico" : $scope.data.oFormatoContratoCobro.seleccionado.IdFormatoContratoCobroPeriodico		                                         
			                                      }];
		
		datos.p_ContratoCobroPeriodicoDetalleACrear = v_ContratoCobroPeriodicoDetalleACrear;
		
		datos.p_CobroPeriodicoACrear          = v_CobroPeriodicoACrear;
		
		datos.p_CobroPeriodicoDetalleACrear   = v_CobroPeriodicoDetalleACrear;
		
		bcsComunes.ventanaOverlay(idVentana, true);				
       	bcsServiciosFacturacion.CrearContratoCobroPeriodico(datos,successFacturar,errorFacturar);
    };	
});
