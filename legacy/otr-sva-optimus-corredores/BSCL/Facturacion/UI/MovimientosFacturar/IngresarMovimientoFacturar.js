angular.module('ModuloFacturacion').controller('IngresarMovimientoFacturarCtrl',
		function($scope,$translate,$modal,bcsComunes,bcsServiciosFacturacion,
				 bcsServicioPlataforma, bcsIngresarMovimientoFacturarCobroGrid, $timeout)
{
		
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
    //var datosEntrada = bcsComunes.ventanaParams($scope.IdVentana);
        
	$scope.data	= {};
	$scope.dataTab	= {};
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdMonedaLocal = $scope.SesionIdMonedaSistema;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
		
	$scope.data.oFechaMovimientoComprobante = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oFechaLiquidacion = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oMontoLiquidacion = 0;
	$scope.data.oMontoOperacion = 0;
	$scope.data.oPrecio = 0;
	$scope.data.oCantidad = 0;
	$scope.data.oMontoLocal = 0;
	$scope.data.oMontoAPlazo = 0;
	$scope.data.oTasa = 0;
	$scope.data.oValorCaptacion = 0;
	$scope.data.oDiasPlazo = 0;
	$scope.data.oTasaEmision = 0;
	$scope.data.oValorRescate = 0;
	
	$scope.data.oMontoCobro = 0;
	$scope.data.oMontoLocalCobro = 0;
	$scope.data.oMontoLiquidacionCobro = 0;
	$scope.data.oPorcentajeCobro = 0;
	
	//Disabled campos
	$scope.data.activaroFechaMovimientoComprobante = true;
	$scope.data.activaroMonedaSubyacente = false;
	$scope.data.activaroEmisor = false;
	$scope.data.activaroInstrumentoSubyacente = false;
	$scope.data.activaroInstrumentoRelacionado = false;
	$scope.data.activaroTipoReajuste = false;
	$scope.data.activaroConcepto = true;
	$scope.data.activaroSecCmbte = true;
	$scope.data.activaroSecCmbteRef = true;
	$scope.data.activaroCorteMinimo = true;
	$scope.data.activaroInstrumentoSubyacente = false;
	$scope.data.activaroInstrumentoRelacionado = false;
	$scope.data.activaroMontoAPlazo = true;	
	$scope.data.activaroFolioPlazo = true;	
	$scope.data.activaroValorCaptacion = true;	
	$scope.data.activaroDiasPlazo = false;
	$scope.data.activaroFechaVencimiento = false;
	$scope.data.activaroFechaEmision = true;	
	$scope.data.activaroTasaEmision = true;	
	$scope.data.activaroValorRescate = true;
	$scope.data.activaroMonedaCobro = true;
	$scope.data.activaroIndPrima = true;
	$scope.data.activaroIndAPlazo = true;
	
	
	//COMBOBOX
	$scope.data.oClientePortafolio={
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
	};
	
	$scope.data.oTipoOperacion={
			tipo : "combobox-tipo-operacion", // "combobox-operacion",
			configuracion : {},
			filtros : {				
				IdTipoInstrumento :	{"field"   : "IdTipoInstrumento","operator": "eq","value"   : ""},
				EsMovimientoComprobante : {"field"   : "EsMovimientoComprobante","operator": "eq","value"   : "S"},
				EstadoTipoOperacionTipoInstrumento : {"field" : "EstadoTipoOperacionTipoInstrumento","operator":"eq","value" : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oSucursal={
			tipo : "combobox-sucursal",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oEjecutivo={
			tipo : "combobox-empleado",
			configuracion : {},
			filtros : {
				IdSucursal : {"field" : "IdSucursal","operator": "eq","value"   :0 },
				IdEstado : {"field"   : "IdEstado","operator": "eq","value"   : 'ACT' }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConcepto={
			tipo : "combobox-concepto-facturacion",
			configuracion : {},
			filtros : {
				IdEstado : {field   : "IdEstado", operator : "eq", value :'VIG' },
				IdTipoOperacion : {field   : "IdTipoOperacion", operator : "eq", value :'' },
				VisibleOptimus : {field   : "VisibleOptimus",operator: "eq",value  : "S"},
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oPreferenciaFacturacion={
			tipo : "combobox-detalle-tipo-atributo",
			configuracion : {},
			filtros : {
				IdTipoIndicador : {"field"   : "IdTipoIndicador","operator": "eq","value" : 21},
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	}

	$scope.data.oTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento : { field   : "IdClasificacionTipoInstrumento", operator: "eq", value   : "" },
				Comprobante                    : { field   : "Comprobante", operator: "eq", "value"   : "S" },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oTipoReajuste={
		tipo : "combobox-instrumento-tipo-reajuste",
		configuracion : {
			template: '<span >#: IdTipoReajuste # - #: NombreTipoReajuste #</span>'
		},
		filtros : {
			IdInstrumento		: { "field"   : "IdInstrumento","operator": "eq", "value"   : "" },
			IdTipoInstrumento	: { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
						
		}
	};
	
	$scope.data.oCondicionLiquidacion={
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
	
	$scope.data.oBolsa={
		tipo : "combobox-bolsa",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			EstadoBolsaTipoInstrumento : { "field"   : "EstadoBolsaTipoInstrumento","operator": "eq", "value"   : "ACT" }
		}
	};
	
	$scope.data.oBoveda={
		tipo : "combobox-boveda",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
		}
	};
	
	$scope.data.oCorredor={
		tipo : "combobox-corredora",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		}
	};
	
	$scope.data.oMonedaLiquidacion={
		tipo : "combobox-moneda-tesoreria",
		configuracion : {},
		filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' }
				  }
	};
	
	$scope.data.oMonedaOperacion={
		tipo : "combobox-moneda",
		configuracion : {},
		filtros : {
   			        IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' }
				  }
	};
	
	$scope.data.oMonedaSubyacente={
		tipo : "combobox-moneda",
		configuracion : {},
		filtros : {
   			        IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' }
				  }
	};
	
	$scope.data.oEmisor={
		tipo : "combobox-emisor",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   	: "IdEstado","operator": "eq", "value"   : "ACT" },
		}
	};
	
	$scope.data.oTipoComprobante={
			tipo : "combobox-tipo-comprobante",
			configuracion : {},
			filtros : {
			    IdEstadoFacturacion :	{field : "IdEstadoFacturacion", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oFormaCustodia={
			tipo : "combobox-forma-custodia-orden",
			configuracion : {},
			filtros : {
				   	    IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				      },
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	
				                                       
	//GRILLA DE COBROS
	var defGridCobros = {
		 		          type : "array",
				          definition : {                                         
				        	             data     : [],                                        
                                         schema : bcsIngresarMovimientoFacturarCobroGrid.modeloMovimientoCobros(),

    			                       }
		};
	
	var dsGridCobros = new bcs.DataSource(defGridCobros)	  	
	$scope.confBasicaIngresarMovimientoComprobanteCobros = { 
		dataSource          : dsGridCobros,		
		columns				: bcsIngresarMovimientoFacturarCobroGrid.columnasMovimientoCobro(),
		sortable			: true,
		pageable			: true,
		filterable			: false,
		userSettings        : true,
		height				: 150,
		selectable 			: "multiple row"
		
	};
	
				
	$scope.data.oTipoCobro={
			tipo : "combobox-cobro",
			configuracion : {},
			filtros : {
				   	    PermiteAsignarPortafolio : { "field"   : "PermiteAsignarPortafolio","operator": "eq", "value"   : "S" }
				      },
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	
	//GRILLA DE FORMA DE PAGO	
	/*$scope.confBasicaIngresarMovimientoComprobanteFDePago = { 
		//dataSource			: bcsIngresarMovimientoComprobanteCobroGrid.dataSourceMovimientosComprobanteCobro,
		columns				: bcsIngresarMovimientoComprobanteFDePagoGrid.columnasMovimientoComprobanteFDePago(),
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		////autoBind			: true,
		height				: 150,
		selectable 			: "multiple row"
		
	};*/
	
	
	//*******************************************************
	// Asigna monto de acuerdo a moneda liquidación
	//*******************************************************
	var actualizaMontos = function()
	  {
		
		 //Verifica moneda operacion contra moneda liquidacion
	   	 if ($scope.data.oMonedaOperacion.seleccionado.IdMoneda === $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda){
	   	     $scope.data.oMontoOperacion = $scope.data.oMontoLiquidacion;
	   	 }else{
	   	   	 	$scope.data.oMontoOperacion = 0;
	   	      } 
	   	 
	   	 //Asigna monto local
	   	 if ($scope.data.IdMonedaLocal === $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda){
	   		calculaMontoTotal($scope.data.oMonedaLiquidacion.seleccionado.IdMoneda,$scope.data.oMontoLiquidacion);
	   		
	   	 }else{
	   	   	 if ($scope.data.IdMonedaLocal === $scope.data.oMonedaOperacion.seleccionado.IdMoneda){
	   	   		 calculaMontoTotal($scope.data.oMonedaOperacion.seleccionado.IdMoneda, $scope.data.oMontoOperacion);	   	   		 
	   	     }else{	  
	   	   	      $scope.data.oMontoLocal = 0;
	   	   	      }   
	   	   }
	 	$scope.data.datosResumen.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',$scope.data.oMonedaOperacion.seleccionado.IdMoneda,$scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento,'').cantidadDecimales;
	 	$scope.data.datosResumen.decimalesTasa = util.bcs_formato(0,'GEN','Tasa',$scope.data.oMonedaOperacion.seleccionado.IdMoneda,$scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento,'').cantidadDecimales;
	  };
	  

	//*******************************************************
	// Calcula monto total automatico con las diferentes monedas 
	//*******************************************************
	  var calculaMontoTotal = function(MonedaOrigen , MontoOrigen){		
		  
		  $scope.data.oMontoLocal=0;
		  
		 
		  var datosActualizar = {
                  p_IdEmpresa : $scope.SesionIdEmpresa,
                  p_IdTrabajo : 0,
                  p_CalculosEntrada : [{
                  	             IdTrabajo : 0,
                  	             IdInterno : 0,
                  	             Fecha : $scope.SesionFechaSistema,
                  	             IdMonedaOrigen :MonedaOrigen,
                  	             IdMonedaDestino : $scope.data.IdMonedaLocal,
                  	             ValorCambioLocalModificado :$scope.data.oTipoCambio || 0,
                  	             MontoOrigen : Number(MontoOrigen),//Number($scope.data.oMontoLiquidacion),
                  	             IdUso : 'FACTURACION'
                  	        }]
                 };
		  
		  var successActualizar = function(respuesta)
	       {
	    	 
	    	 var resp = respuesta.p_out_CalculosSalida;
	    	 if (resp[0])
	    	   {	
		      	 //Se actualiza los montos correspondientes
	    		 	if ($scope.data.oCantidad !=0 && $scope.data.oPrecio !=0){
	    		 		 $scope.data.oMontoLocal= resp[0].MontoLocal*$scope.data.oCantidad*$scope.data.oPrecio;
	    		 	}else if($scope.data.oCantidad !=0 && $scope.data.oPrecio ==0){
	    		 		 $scope.data.oMontoLocal= resp[0].MontoLocal*$scope.data.oCantidad;
	    		 	}else if($scope.data.oCantidad ==0 && $scope.data.oPrecio !=0){
	    		 		 $scope.data.oMontoLocal= resp[0].MontoLocal*$scope.data.oPrecio;
	    		 	}else if($scope.data.oCantidad ==0 && $scope.data.oPrecio ==0){
	    		 		 $scope.data.oMontoLocal= resp[0].MontoLocal;
	    		 	}
	    		
	    		 
	    	     //Actualiza Porcentaje
	    	     bcsComunes.ventanaOverlay(idVentana, false);
	    	   }
	    	 else
	    	    {	
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.msg("error","error",$translate.instant('UI_ERROR_CALCULO'));				
					
					$scope.data.oMontoLocal = 0;
	    	       
	    	    }
	    	 
	       };
	       
	       var errorActualizar = function(respuesta)
	       {	
			  bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
				
			  //overlay.encendido = false;	
			  bcsComunes.ventanaOverlay(idVentana, false);
	       };
	    	
	    		    	
		 //Llamada a servicio para crear comprobante de facturación()
	     bcsServiciosFacturacion.ActualizarMontosCobro(datosActualizar,successActualizar,errorActualizar);
		  
		  
	  }
	  
	  
	//*******************************************************
	// Actualiza los montos 
	//*******************************************************
	  $scope.$watch('data.oTipoCambio',function(newVal,oldVal)
			   {
				
			   	 if (!newVal) return;		  	 
			   	
			   	 actualizaMontos();	   	 
			   	 
		       });
	  $scope.$watch('data.oCantidad',function(newVal,oldVal)
			   {
				
			   	 if (!newVal) return;		  	 
			   	
			   	 actualizaMontos();	   	 
			   	 
		       });
	  $scope.$watch('data.oPrecio',function(newVal,oldVal)
			   {
				
			   	 if (!newVal) return;		  	 
			   	
			   	 actualizaMontos();	   	 
			   	 
		       });
	  
	  
	//*******************************************************
	// Actualiza celdas de cobros
	//*******************************************************
	  
	  $scope.$watch('data.oTipoCobro.seleccionado',function(newVal,oldVal)
	   {
		
	   	 if (!newVal) return;
	   	 
	   	
	   	 if($scope.data.oTipoCobro.seleccionado.UnidadDeCobro==="POR"){
	   		$scope.data.oMontoLocalCobro= $scope.data.oMontoLocal*$scope.data.oTipoCobro.seleccionado.ValorCobro;
		   	$scope.data.oMontoLiquidacionCobro=$scope.data.oMontoLiquidacion*$scope.data.oTipoCobro.seleccionado.ValorCobro;
		   	$scope.data.oMontoCobro =$scope.data.oMontoOperacion*$scope.data.oTipoCobro.seleccionado.ValorCobro;
		   	$scope.data.oPorcentajeCobro = $scope.data.oTipoCobro.seleccionado.ValorCobro;
	   		 
	   	 }else{
	   		$scope.data.oMontoLocalCobro= $scope.data.oMontoLocal*$scope.data.oTipoCobro.seleccionado.ValorCobro;
		   	$scope.data.oMontoLiquidacionCobro=$scope.data.oMontoLiquidacion*$scope.data.oTipoCobro.seleccionado.ValorCobro;
		   	$scope.data.oMontoCobro =$scope.data.oMontoOperacion*$scope.data.oTipoCobro.seleccionado.ValorCobro;
		   	$scope.data.oPorcentajeCobro = $scope.data.oTipoCobro.seleccionado.ValorCobro;
	   	 }
	   	 
	   
	   	 
	   	 
       });
	  
	
	//*******************************************************
	// Asigna campos relacionado con el tipo operación
	//*******************************************************
	$scope.$watch('data.oTipoOperacion.seleccionado',function(newVal,oldVal)
	   {
	   	 if ((!newVal) && $scope.data.oTipoInstrumento.seleccionado)
	   	   {
	   	 	 $scope.data.oConcepto.filtros.IdTipoOperacion.value = '0';
	   	 	 $scope.data.oConcepto.filtros.read = true;
	   	 	 $scope.data.buscaroInstrumento = "-1";
	   	 	 return;
	   	   }
	   	 else
	   	   {
	   	   	if (!newVal)
	   	   	  {
	   	   	  	$scope.data.oConcepto.filtros.IdTipoOperacion.value = '';
	   	 	    $scope.data.oConcepto.filtros.read = true;
	   	 	    return;
	   	   	  }

	   	   }  
	   	 
	   	 //Asigna valores desde el emisor a campos relacionados
	   	 $scope.data.oIndVentaCorta = ($scope.data.oTipoOperacion.seleccionado.EsVentaCorta==='S'? true:false);
	   	 
	   	 if ($scope.data.oTipoOperacion.seleccionado.IdTipoInstrumento === 'SIM')
	   	   {
	   		 $scope.data.activaroDiasPlazo = true;	
	   		 $scope.data.activaroMontoAPlazo = false;	
	   		 $scope.data.oFechaVencimiento = '0001-01-01';
	   		 $scope.data.activaroIndAPlazo = false;
	   		 $scope.data.activaroFolioPlazo = false;
	   	   }
	   	 else 
	   	   {
	   		 $scope.data.activaroDiasPlazo = false;
	   		 $scope.data.activaroMontoAPlazo = true;
	   		 $scope.data.oFechaVencimiento = undefined;
	   		 $scope.data.activaroIndAPlazo = true;
	   		 $scope.data.activaroFolioPlazo = true;
	   	   }
	   		 
	   	 
       });
	
	//*******************************************************
	// Asigna campos relacionado con el instrumento
	//*******************************************************
	$scope.$watch('data.oInstrumento',function(newVal,oldVal)
	   {
		
	   	 if (!newVal) return;
	   	 
	   	 //Asigna valores desde el instrumento a campos relacionados
	   	 $scope.data.oEmisor.idActual = $scope.data.oInstrumento.IdEmisor;
	   	 $scope.data.oMonedaOperacion.idActual = $scope.data.oInstrumento.IdMoneda;
	   	 $scope.data.oTipoReajuste.idActual = $scope.data.oInstrumento.IdTipoReajuste;
       });
	
	//*******************************************************
	// Asigna campos relacionado con el emisor
	//*******************************************************
	$scope.$watch('data.oEmisor.seleccionado',function(newVal,oldVal)
	   {
		
	   	 if (!newVal) return;
	   	 
	   	 //Asigna valores desde el emisor a campos relacionados
	   	 $scope.data.oIndFungible = ($scope.data.oEmisor.seleccionado.EmiteFungible==='S'? true:false);
	   	 
       });
	
	//*********************************************************
	// Asigna campos relacionado con el monto/moneda operacion
	//*********************************************************	 
	$scope.$watch('data.oMonedaOperacion.seleccionado',function(newVal,oldVal)
	   {
		
	   	 if (!newVal) return;
	   	 
	   	 $scope.data.oMontoOperacion = 0;
	   	 actualizaMontos();	   	 
	   	 
       });

    
    $scope.$watch('data.oMontoOperacion',function(newVal,oldVal)
	   {
		
	   	 if (!newVal) return;	   	 
	   	 
	   	 if ($scope.data.IdMonedaLocal === $scope.data.oMonedaOperacion.seleccionado.IdMoneda)
	   	   {
	   		 $scope.data.oMontoLocal = $scope.data.oMontoOperacion;
	   	   }
	   	 
	   	 
       });   
    
   

    //***********************************************************
	// Asigna campos relacionado con el monto/moneda liquidacion
	//***********************************************************
	$scope.$watch('data.oMonedaLiquidacion.seleccionado',function(newVal,oldVal)
	   {
		
	   	 if (!newVal) return;	   	 
	   	 
	   	 $scope.data.oMontoLiquidacion = 0;
	   	 $scope.data.oMontoOperacion = 0;
	   	 $scope.data.oMontoLocal = 0;
	   	 actualizaMontos();
	   	 
       }); 
	

	$scope.$watch('data.oMontoLiquidacion',function(newVal,oldVal)
	   {
		
	   	if (!newVal) return;   	 
	   	
	   	 
	   	 actualizaMontos();	   	 
	   	 
       });   
	
	//*******************************************************
	// Habilita campos segun tipo instrumento
	//*******************************************************
	$scope.$watch('data.oTipoInstrumento.seleccionado',function(newVal,oldVal)
	   {
		 //if (!newVal) return;

		 if (!newVal) {
		 	$scope.data.oTipoOperacion.idActual = false;
	   	 	$scope.data.oConcepto.filtros.IdTipoOperacion.value = '';
	   	 	$scope.data.oConcepto.filtros.read = true;
	   	 	$scope.data.buscaroInstrumento = "-1";
	   	 	return;
	   	 }
		 
		 var IdClasificacionTipIns = $scope.data.oTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento;
	     
		 switch (IdClasificacionTipIns)
	  	  { 
		     case "RV":
		       {
		    	 $scope.data.activaroValorRescate = true;
		    	 $scope.data.activaroTasaEmision = true;
		    	 $scope.data.activaroFechaEmision = true;
		    	 $scope.data.activaroValorCaptacion = true;
		    	 $scope.data.activaroEmisor = false;
		    	 $scope.data.activaroTipoReajuste = false;
		    	 break; 
		       }	 
		     case "IF":
		       {
		    	 $scope.data.activaroValorRescate = false;
		    	 $scope.data.activaroTasaEmision = false;
		    	 $scope.data.activaroFechaEmision = false;
		    	 $scope.data.activaroValorCaptacion = false;
		    	 $scope.data.activaroEmisor = true;
		    	 $scope.data.activaroTipoReajuste = true;
		    	 break; 
		       }
		     case "RF":
		       {
		    	 $scope.data.activaroValorRescate = true;
		    	 $scope.data.activaroTasaEmision = true;
		    	 $scope.data.activaroFechaEmision = true;
		    	 $scope.data.activaroValorCaptacion = true;
		    	 $scope.data.activaroEmisor = false;
		    	 $scope.data.activaroTipoReajuste = false;
		    	 break; 
		       }  
		       
		     default:  //Otros: 
		    	 break;
	   	  };
	   	  

	   	  if ($scope.data.oTipoInstrumento.seleccionado) 	   	    
	   	    {
	   	       $scope.data.oConcepto.filtros.IdTipoOperacion.value = '0';
	   	       $scope.data.buscaroInstrumento = "-1";
	   	       $scope.data.oConcepto.filtros.read = true;	
	   	    }
	   	 
	   });
	   
	// SVARESGIN-6191 -------------------------------------------------------------
	$scope.$watch('data.oTipoComprobante.seleccionado',function(newVal,oldVal)
	{
		if (!newVal) return;

		if($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante == "FEE" || $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante == "FOX" || 
		$scope.data.oTipoComprobante.seleccionado.IdTipoComprobante == "FOE" || $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante == "NEO"){
			$scope.data.oMonedaLiquidacion.idActual = $scope.SesionIdMonedaSistema;
			$scope.data.oMonedaOperacion.idActual = $scope.SesionIdMonedaSistema;
			$scope.data.activaroMonedaOperacion = false;
		}
		else{
			$scope.data.activaroMonedaOperacion = true;
		}
	});
	// ---------------------------------------------------------

	//*******************************************
	// Limpiar
	//*******************************************		
	$scope.data.limpiar = function()	 
     {	     
	   
    	$scope.filtros.IdPortafolio = false;
    	$scope.data.oTipoInstrumento.idActual = 0;
    	$scope.data.oTipoOperacion.idActual = 0;
    	$scope.data.oSucursal.idActual = 0;
    	$scope.data.oEjecutivo.idActual = 0;
    	$scope.data.oConcepto.idActual = 0;
    	$scope.data.oPreferenciaFacturacion.idActual = 0;
    	//$scope.data.oInstrumento= {}; //pendiente
    	$scope.data.buscaroInstrumento = "-1";
    	$scope.data.buscaroInstrumentoRelacionado = "-1";
    	$scope.data.buscaroInstrumentoSubyacente = "-1";
    	$scope.data.nemotecnico = '';
    	   	
    	$scope.data.oEmisor.idActual = 0;
    	$scope.data.oMonedaSubyacente.idActual = 0;
    	$scope.data.oTipoReajuste.idActual = 0;
    	$scope.data.oFormaCustodia.idActual = 0;
    	$scope.data.oIndFungible = false;
    	$scope.data.oCondicionLiquidacion.idActual = 0;
    	$scope.data.oFechaTransaccion = '';
    	$scope.data.oFechaLiquidacion = $scope.data.oFechaMovimientoComprobante;
    	$scope.data.oBolsa.idActual = 0;
    	$scope.data.oBoveda.idActual = 0;
    	$scope.data.oMonedaLiquidacion.idActual = 0;
    	$scope.data.oFolioTransaccion = '';
		$scope.data.activaroMonedaOperacion = true;
    	$scope.data.oOrden = 0;
    	$scope.data.oCorredor.idActual = 0;
    	$scope.data.oTipoCambio = 0;
    	$scope.data.oMonedaLiquidacion.idActual = 0;
    	$scope.data.oMontoLiquidacion = 0;
    	$scope.data.oMonedaOperacion.idActual = 0;
    	$scope.data.oMontoOperacion = 0;
    	$scope.data.oPrecio = 0;
    	$scope.data.oCantidad = 0;
    	$scope.data.oMontoLocal = 0;
    	$scope.data.oMontoAPlazo = 0;
    	$scope.data.oFolioPlazo = 0;
    	$scope.data.oTasa = 0;
    	$scope.data.oValorCaptacion = 0;
    	$scope.data.oDiasPlazo = 0;
    	$scope.data.oTasaEmision = 0;
    	$scope.data.oValorRescate = 0;
    	$scope.data.oCorteMinimo = 0;
    	$scope.data.oComentario = '';
    	$scope.data.oTipoComprobante.idActual = 0;
    	$scope.data.oSecuenciaAgrupacion = '';
    	$scope.data.oIndCmbteIndiv = false;
    	$scope.data.oIndFueraRueda = false;
    	$scope.data.oIndOrdenDirecta = false;
    	$scope.data.oIndCuentaPropia = false;
    	$scope.data.oIndVentaCorta = false;
    	$scope.data.oIndActCus = false;
    	$scope.data.oIndActCusRel = false;
    	$scope.data.oIndActCtaInv = false;
    	$scope.data.oIndActTes = false;
    	$scope.data.oTipoCobro.idActual = 0;
    	$scope.data.oMontoCobro = 0;
    	$scope.data.oMontoLocalCobro = 0;
    	$scope.data.oMontoLiquidacionCobro = 0;
    	$scope.data.oPorcentajeCobro = 0;

    	/* Cobros */
    	dsGridCobros.removeAll();
    	
	 };
	 
	 $scope.data.limpiarCobros = function (){
		 $scope.data.oMontoLocalCobro= 0;
		 $scope.data.oMontoLiquidacionCobro=0;
		 $scope.data.oMontoCobro =0;
		 $scope.data.oPorcentajeCobro =0;
	   		 
	 }

		
	
	//*******************************************
	// Ingresar movimiento comprobante
	//*******************************************		
	$scope.data.ingresarMovimientoComprobante = function()	
    {    	
		bcsComunes.ventanaOverlay(idVentana, true);
		$scope.data.accionDisabled = false;
		$scope.data.limpiarDisabled = false;
		
		//Tabla Mvto a Facturar
		var v_MvtoCmbteAGenerar = [];
		v_MvtoCmbteAGenerar.push({
				                   "NumeroMvtoCmbte" : 1,
				                   "IdEmpresa"       : $scope.SesionIdEmpresa,
			                       "IdConcepto"      : $scope.data.oConcepto.seleccionado.IdConcepto, 
			                       "SecuenciaAgrupacion" : $scope.data.oSecuenciaAgrupacion || '',
			                       "IdCliente"       : $scope.data.oClientePortafolio.seleccionado.IdCliente,
			                       "IdPortafolio"    : $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
			                       "ModPrefFact"     : 1, //Revisar
			                       //"IdAgente"        : $scope.data.oEjecutivo.seleccionado.IdEmpleadoEjecutivo,			                                         
			                       "IdEmpleadoDigitador" : $scope.data.oEjecutivo.seleccionado.IdEmpleadoEjecutivo || 0,
			                       //"IdSucursal"      : $scope.data.oSucursal.seleccionado.IdSucursal,
			                       "IdInstrumento"   : $scope.data.oInstrumento && $scope.data.oInstrumento.IdInstrumento?$scope.data.oInstrumento.IdInstrumento : 0,
								   "IdInstrumentoRelacionado" : 0,
								   "IdInstrumentoSubyacente"  : 0,
								   "IdTipoProducto"           : $scope.data.oTipoOperacion.seleccionado.IdTipoProducto || '',													 
								   "IdMoneda"                 : $scope.data.oMonedaOperacion.seleccionado.IdMoneda || 'CLP',
								   "IdMonedaSubyacente"       : '',
								   "IdMonedaLiquidacion"      : $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda || '',
								   "IdTipoComprobante"        : $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante,
								   "IdTipoOperacion"          : $scope.data.oTipoOperacion.seleccionado.IdTipoOperacion || '',
								   "IdBoveda"                 : $scope.data.oBoveda.seleccionado.IdBoveda || '',
								   "IdCorredoraContraparte"   : $scope.data.oCorredor.seleccionado.IdContraparte || 0,
								   "IdCondicionLiquidacion"   : $scope.data.oCondicionLiquidacion.IdCondicionDeLiquidacion || 'T0',
								   "FechaLiquidacion"         : util.dateSPdate($scope.data.oFechaLiquidacion),
								   "IdBolsa"                  : $scope.data.oBolsa.seleccionado.IdBolsa || '',
								   "IdTipoReajuste"           : $scope.data.oTipoReajuste.seleccionado.IdTipoReajuste || 'CLP',
								   "IdTipoConvenio"           : 'SCV',
								   "IdFormaCustodia"          : $scope.data.oFormaCustodia.seleccionado.IdFormaCustodiaOrden || '',
								   "IdEmisor"                 : $scope.data.oEmisor.seleccionado.IdEmisor || '',
								   "IdOrden"                  : $scope.data.oOrden || 0,
								   "IdAsignacion"             : 0,
								   "IdAsignacionRef"          : 0,
								   "IdTipoCanalIngreso"       : $scope.SesionTipoCanalIngreso,
								   "FechaOrden"               : $scope.data.oFechaOrden?util.dateSPdate($scope.data.oFechaOrden) : '0001-01-01', 
								   "IdTransaccion"            : 0, //Va cero porque el Id es interno del modulo orden
								   "FolioTransaccion"         : $scope.data.oFolioTransaccion || 0,
								   "FechaTransaccion"         : $scope.data.oFechaTransaccion?util.dateSPdate($scope.data.oFechaTransaccion) : '0001-01-01', 
								   "FechaVencimiento"         : $scope.data.oFechaVencimiento?util.dateSPdate($scope.data.oFechaVencimiento) : '0001-01-01', 
								   "PrecioAPlazo"             : 0,
								   "MontoAPlazo"              : $scope.data.oMontoAPlazo || 0,
								   "FechaParidad"             : '0001-01-01',
								   "ValorParidad"             : 0,
								   "FechaTipoCambio"          : $scope.data.oFechaTipoCambio?util.dateSPdate($scope.data.oFechaTipoCambio) : '0001-01-01',
								   "ValorTipoCambio"          : $scope.data.oTipoCambio || 0,
								   "ValorCaptacionMonedaOrigen" : $scope.data.oValorCaptacion || 0,
								   "Tasa"                     : $scope.data.oTasa || 0,
								   "Cantidad"                 : $scope.data.oCantidad || 0,
								   "Precio"                   : $scope.data.oPrecio || 0,
								   "Monto"                    : $scope.data.oMontoOperacion || 0,
								   "MontoMovimientoComprobante" : $scope.data.oMontoLocal,
								   "MontoLiquidacion"         : $scope.data.oMontoLiquidacion,
								   "MontoDescuento"           : 0,
								   "MontoInteres"             : 0,
								   "Glosa"                    : $scope.data.oComentario || '',
								   "ValorRescate"             : $scope.data.oValorRescate || 0,
								   "TasaAgente"               : 0,
								   "Spread"                   : 0,
								   "DiasPlazo"                : $scope.data.oDiasPlazo || 0,
								   "FechaMovimientoComprobante" : util.dateSPdate($scope.data.oFechaMovimientoComprobante),
								   //"NombreAgente"             : $scope.data.oEjecutivo.seleccionado.NombreCompleto,
								   //"NombreSucursal"           : $scope.data.oSucursal.seleccionado.NombreSucursal || '',
								   "FolioCartera"             : $scope.data.oCartera || 0,
								   "FechaEmision"             : $scope.data.oFechaEmision?util.dateSPdate($scope.data.oFechaEmision) : '0001-01-01',
								   "TasaEmision"              : $scope.data.oTasaEmision || 0,
								   "EsComprobIndividual"      : $scope.data.oIndCmbteIndiv ? 'S' : 'N',
								   "EsCuentaPropia"           : $scope.data.oIndCuentaPropia ? 'S' : 'N',
								   "EsOrdenDirecta"           : $scope.data.oIndOrdenDirecta ? 'S' : 'N',
								   "EsVentaCorta"             : $scope.data.oIndVentaCorta ? 'S' : 'N',
								   "EsPrecioPrima"            : $scope.data.oIndPrima ? 'S' : 'N',
								   "EsEnRueda"                : $scope.data.oIndFueraRueda ? 'N' : 'S',
								   "EsRuteo"                  : 'N', //Revisar
								   "EsAPlazo"                 : 'N', //$scope.data.oIndPlazo ? 'N' : 'S',
								   "EsFungible"               : $scope.data.oIndFungible ? 'S' : 'N',
								   "ActualizarCustodia"       : $scope.data.oIndActCus ? 'S' : 'N',
								   "ActualizarCuentaInversion" : $scope.data.oIndActCtaInv ? 'S' : 'N',
								   "ActualizarTesoreria"      : $scope.data.oIndActTes ? 'S' : 'N',
								   "ActualizarCustodiaRelacionado" : $scope.data.oIndActCusRel ? 'S' : 'N',
								   "CrearComprobanteAutomatico" : 'N',
								   "IdUsuario"                : $scope.SesionIdUsuario
				                });
		
		

		//Tabla Cobro
		var v_MvtoCmbteCobroAGenerar = [];		
	    var v_dsGridCobros = dsGridCobros.data()|| [];		
	    $.each(v_dsGridCobros,function(key,value){	
		        var ObjMvtoCobro = {
			  	                      "NumeroMvtoCmbte" : 1, 
	                                  "IdCobro"         : $scope.data.oTipoCobro.seleccionado.IdCobro || '', 
	                                  "IdMonedaCobro"   : $scope.data.oTipoCobro.seleccionado.IdMoneda || 'CLP',
	                                  "IdMonedaLiquidacionCobro" : $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda || '', 
	                                  "MontoCobro"      : $scope.data.oMontoCobro || 0, 
	                                  "MontoMovimientoComprobanteCobro" : $scope.data.oMontoLocalCobro || 0,
	                                  "MontoLiquidacionCobro"    : $scope.data.oMontoLiquidacionCobro || 0, 
	                                  "Porcentaje"      : $scope.data.oPorcentajeCobro || 0
		 	                       };
		        v_MvtoCmbteCobroAGenerar.push(ObjMvtoCobro);
	    });
	    
	    //Tabla Forma de Pago
	    var v_MvtoCmbteForPagoAGenerar = [];

        //VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa                       = $scope.SesionIdEmpresa;
		datos.p_IdUsuario                       = $scope.SesionIdUsuario;
		datos.p_IdModulo                        = 'FAC';
		datos.p_simularOperacion                = 'N';
		datos.p_UsaReglasNegocio                = 'S';
		datos.p_EsIngresoAutomatico             = 'S';
		
		datos.p_Movimientos 				= v_MvtoCmbteAGenerar;
		
		datos.p_MovimientosFormaDePago       = v_MvtoCmbteForPagoAGenerar;
		
		datos.p_MovimientosCobro         = v_MvtoCmbteCobroAGenerar;
				
		var successFacturar = function(respuesta)
		  {
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			  {	
	    		//Se actualiza los datos de la grilla
				bcsComunes.ventanaMsg({
					                    conflictos:util.parseServerResponse(respuesta)
					                  },idVentana);
				
				//$scope.data.Limpiar();
				$scope.data.limpiar();
				
				//limpiar();
					                  
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
	    		
	    	$scope.data.btnActivos  = true;
	    	bcsComunes.ventanaOverlay(idVentana, false);
	      };
	    	
	    var errorFacturar = function(respuesta)
	      {	
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
			$scope.data.btnActivos  = true;     //Activa botones
			bcsComunes.ventanaOverlay(idVentana, false);
	      };
	    	
	    	
	    //Llamada a servicio para crear comprobante de facturación()
	    bcsServiciosFacturacion.CrearMovimientoComprobante(datos,successFacturar,errorFacturar);
	    
								
    };	
	
    //*******************************************
	// Ingresar movimiento comprobante cobro
	//*******************************************
    $scope.agregarCobros = function()
	{	
	  	if($scope.data.oMonedaLiquidacion.seleccionado.IdMoneda === undefined){
				bcsComunes.msg("error","Error","Debe seleccionar moneda de liquidación");
				return;
			};
	  	
	  	if($scope.data.oTipoCobro.seleccionado.IdCobro === undefined){
				bcsComunes.msg("error","Error","Debe seleccionar un cobro");
				return;
			};
	  		  		  	
	  	if(!$scope.data.oMontoCobro){
				bcsComunes.msg("error","Error","Debe ingresar el monto del cobro");
				return;
			};
	  	
	  	if(!$scope.data.oMontoLocalCobro){
				bcsComunes.msg("error","Error","Debe ingresar el monto local del cobro");
				return;
			};
	  	
	  	if(!$scope.data.oMontoLiquidacionCobro){
				bcsComunes.msg("error","Error","Debe ingresar el monto liquidacion del cobro");
				return;
			};
			
		if(!$scope.data.oPorcentajeCobro && $scope.data.oPorcentajeCobro !== 0){
				bcsComunes.msg("error","Error","Debe ingresar el porcentaje del cobro");
				return;
			};	
    	
	  	dsGridCobros.add(
        			 { 
        			   NombreCobro: $scope.data.oTipoCobro.seleccionado.NombreCobro,
        			   DescripcionCobroClasificacion : $scope.data.oTipoCobro.seleccionado.DescripcionCobroClasificacion,      			                       
        			   NombreFormaCobro : ($scope.data.oTipoCobro.seleccionado.FormaCobro? ($scope.data.oTipoCobro.seleccionado.FormaCobro==='F'? 'Fijo': ($scope.data.oTipoCobro.seleccionado.FormaCobro==='V'? 'Variable': '')): ''),
        			   NombreMoneda : 'PESO CHILENO',
        			   Monto        : $scope.data.oMontoCobro,
        			   MontoMovimientoComprobanteCobro : $scope.data.oMontoLocalCobro,
        			   NombreMonedaLiquidacion : $scope.data.oMonedaLiquidacion.seleccionado.Nombre, 
        			   MontoLiquidacion : $scope.data.oMontoLiquidacionCobro, 
        			   Porcentaje : $scope.data.oPorcentajeCobro, 
        			   EsRecalculable : $scope.data.oTipoCobro.seleccionado.EsRecalculable 
        			 })		
        			 
        $scope.data.activaroQuitarCobros = true;	
	  	$scope.data.limpiarCobros();
	  	
	 };
	 
	 
	 
	//**************************************
	// Quita cobros a la grilla 
	//**************************************
	$scope.quitarCobros = function()
	  {		    	
		
		if ($scope.dataTab.seleccionIngrMvtoCmbteCob === undefined)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_COBRO'));
			return;
		};
    	
    	dsGridCobros.remove($scope.dataTab.seleccionIngrMvtoCmbteCob);
    	
    	if (dsGridCobros.data().length === 0)
       	 {
    	    $scope.data.activaroQuitarCobros = false;    	    
    	 };
    	
    	//bcsIngresarMovimientoFacturarCobroGrid.dataSourceComprobantes.read();
	  	
	  }; 
		$scope.currentTab = null;
		$scope.tabClick = function (val){
			if(val !== $scope.currentTab){
				$scope.currentTab = val;
				if( val === 'gridIngresarComprobante'){
					$('div#Win_IngresarMovimiento').css("height", "565px");
				}
				if(val === 'gridIngresarIndicadores'){
					$('div#Win_IngresarMovimiento').css("height", "602px");
				}
				if(val === 'gridIngresarMovimientoComprobanteCobro'){
					$('div#Win_IngresarMovimiento').css("height", "726px");
				}
			}
		}
});