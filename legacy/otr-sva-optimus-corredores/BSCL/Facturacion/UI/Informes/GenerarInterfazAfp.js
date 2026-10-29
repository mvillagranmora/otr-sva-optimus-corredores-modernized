angular.module('ModuloFacturacion').controller('InterfazAfpCtrl',
		function($scope,$translate,$modal,
				bcsComunes, bcsServiciosFacturacion, bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,
				bcsInterfazAfpGrid)
{
	$scope.data	= {};

	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
	
	$scope.data.oFechaMovimientoComprobante = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		

	//GRILLA PRINCIPAL DE MOVIMIENTOS A FACTURAR
	$scope.confBasicaOperadorAfp = { 
		dataSource			: bcsInterfazAfpGrid.dataSourceInterfazAfp,
		columns				: bcsInterfazAfpGrid.columnasInterfazAfp(),
		sortable			: true,
		pageable			: true,
		//filterable			: true,
		userSettings        : true,
		//autoBind			: true,
		height				: 400,
		filterable			: true,
		excel 				: { allPages : true },
		selectable 			: "row",
		navigatable         : true
		
	};

	
	//COMBOBOX
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
			//filtros : { IdTipoInstrumento : {"field"   : "IdTipoInstrumento","operator": "eq","value"   : ""}	},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
		
	//OPCIONES
	$scope.buscar = function(){
		var filters = new Array();	
			if ($scope.data.oClientePortafolio.seleccionado.IdCliente){
				filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdCliente)});			
			};
			if($scope.data.oClientePortafolio.seleccionado.IdPortafolio){
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)});
			};
			if($scope.data.oFechaMovimientoComprobante){
				filters.push({ field: "FechaTransaccion", operator: "eq", value: $scope.data.oFechaMovimientoComprobante});
			};
			if($scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante){
				filters.push({ field: "IdTipoComprobante", operator: "eq", value: $scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante});
			};
			if($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento){
				filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});
			};
			if($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento){
				filters.push({ field: "IdTipoInstrumento", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento});
			};	
				
				
			    //filters.push({ field: "CodigoTipoIndicador", operator: "eq", value: 'FAC_COIAFP'});
		bcsInterfazAfpGrid.dataSourceInterfazAfp.filter(filters);
	};
	
		
	$scope.limpiarFiltros = function()
	{		
		$scope.filtros.IdPortafolio = false;
		bcsInterfazAfpGrid.dataSourceInterfazAfp.removeAll();	
	};
	
    
	/** Transforma arreglo - string a archivo csv **/
	function exportToCsv(filename, rows) {
        var processRow = function (row) {
            var finalVal = '';
            for (var j = 0; j < row.length; j++) {
                var innerValue = row[j] === null ? '' : row[j].toString();
                if (row[j] instanceof Date) {
                    innerValue = row[j].toLocaleString();
                };
                var result = innerValue.replace(/"/g, '""');
                if (result.search(/("|,|\n)/g) >= 0)
                    result = '"' + result + '"';
                if (j > 0)
                    finalVal += ',';
                finalVal += result;
            }
            return finalVal + '\n';
        };

        var csvFile = '';
        for (var i = 0; i < rows.length; i++) {
            //csvFile += processRow(rows[i].movtoRes);
        	if(rows[i]!='Resultado')
            {
                csvFile += processRow(rows[i]);
            }

        }

        var blob = new Blob([csvFile], { type: 'text/csv;charset=utf-8;' });
        if (navigator.msSaveBlob) { // IE 10+
            navigator.msSaveBlob(blob, filename);
        } else {
            var link = document.createElement("a");
            if (link.download !== undefined) { // feature detection
                // Browsers that support HTML5 download attribute
                var url = URL.createObjectURL(blob);
                link.setAttribute("href", url);
                link.setAttribute("download", filename);
                link.style.visibility = 'hidden';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }
        }
    }


	
	$scope.data.generarInterfazAfp = function()
	  {
		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionOperadorAfp === undefined)
		  {				 
			 $scope.data.btnActivos   = true;     //Activa botones
			 bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_CODIGO_OPERADOR'));
			 return;
		  }
		else
		  {			 
			 bcsComunes.ventanaOverlay(idVentana, true);
		     $scope.data.btnActivos  = false;

		     var MovimientoGenerar = [];
		     var archivo = [];
			 MovimientoGenerar.push(	 
	      		                      {     
	      		                        IdCliente                  : $scope.data.oClientePortafolio.seleccionado.IdCliente,
	      			                    CodigoOperadorAfp          : $scope.seleccionOperadorAfp.ValorIndicador,
	      			                    FechaMovimientoComprobante : util.dateSPdate($scope.data.oFechaMovimientoComprobante)	      		                                  
	      		               });

			 //Parámetros de entrada al servicio.
			 var datosGenerar = {};
			 
			 datosGenerar.p_IdEmpresa    = $scope.data.IdEmpresa;
			 datosGenerar.p_IdUsuario	 = $scope.data.IdUsuario;
			 datosGenerar.p_IdModulo     = $scope.data.IdModulo;
			 datosGenerar.p_simularOperacion = 'N';
			 datosGenerar.p_UsaReglasNegocio = 'S';
			 datosGenerar.p_EsIngresoAutomatico = 'S';
			 datosGenerar.P_MOVIMIENTO = MovimientoGenerar; 
			 
			 var xx = '';
			 //Resultado del servicio
			 var successGenerar = function(respuesta)
			   {
				  if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
					{
					   $.each(respuesta.P_OUT_MOVIMIENTO,function(key,value)
						{					   	
					       xx = xx + respuesta.P_OUT_MOVIMIENTO[key].Resultado;

					    }); 					  

					   //contenido = eval("[['FechaTransaccion','FolioTransaccion','Corredora','CodigoOperadorAfp','AccionTipoOperacion','Cantidad','Precio','HoraTransaccion','NemotecnicoInstrumento','Total','Boveda','OD','Bolsa','CondicionDeLiquidacion','TipoOperador']," + xx + "]");					   
					 //  contenido = eval("[['Resultado']," + xx + "]");					   
					   contenido = eval("[['Resultado']," + xx + "]");
                       exportToCsv('Archivo-Afp.txt', contenido);


					   bcsComunes.ventanaMsg({
						                        conflictos:util.parseServerResponse(respuesta)
						                      },idVentana);
					   
					   bcsInterfazAfpGrid.dataSourceInterfazAfp.read();
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
	    		  bcsComunes.ventanaOverlay(idVentana, false);
	    		  bcsInterfazAfpGrid.dataSourceInterfazAfp.read();
	    	   };
	    	   
	    	   
	    	   var errorGenerar = function(respuesta)
	    	     {
	    		    bcsComunes.msg("error","error",$translate.instant('UI_ERROR_GENERACION'));
	    		    $scope.data.btnActivos  = true;     //Activa botones
	    		    bcsComunes.ventanaOverlay(idVentana, false);
	    		 };
	    	
	    	
	    	   //Llamada a servicio para crear comprobante de facturación()
	    	   bcsServiciosFacturacion.GenerarInterfazAfp(datosGenerar,successGenerar,errorGenerar);
			       
		};
	  };
	  
	  $scope.configuracionAFP = function()
		{		
//			alert('Crear Ventana de configuracion AFP');	
			bcsComunes.ventanaAbrir("ConfiguracionAFP", {} );
		};	
		
	 $scope.generarArchivoAFP = function()
		{		
//			alert('Crear Generacion Archivo AFP');	
			bcsComunes.ventanaAbrir("GenerarArchivoAFP", {} );
		};	
	
	//***********************************************
	// Opcion no habilitada 
	//***********************************************
	
	
	
    $scope.data.opcionNoHabilitada = function() 
      {
		bcsComunes.msg("warning","warning","Está opción no se encuentra habilitada.");
	  };
	
});


