angular.module('ModuloFacturacion').controller('ModificarMovimientoFormasDePagoModalCtrl',
		        function($scope,$translate,$modal,bcsComunes,bcsServiciosFacturacion,
				         bcsServicioPlataforma, bcsModificarMovimientoFormasDePagoModalGrid,
				         $timeout)
{
	
	$scope.data	= {};	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
    
		
	$scope.data.oModalidadMedPago =
	   {
	      tipo : "combobox-modalidad-pago",
	      configuracion :{},
	      idActual : false,
	      seleccionado : false,
	      filtros : {			         
			           IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		            },
       };
	
   $scope.data.oEstadoMovimientoFDePago =
     {
	   	tipo : "combobox-estado-facturacion",
	    configuracion :{},
	    idActual : false,
	    seleccionado : false,
	    filtros: {
		    	      NombreEntidad : { "field": "NombreEntidad","operator": "eq", "value"   :  "FAC.MovimientoComprobanteFormaDePago"}
			     },   
     };
   
   $scope.data.oCuentaBancariaFDePago =
     {
		tipo : "combobox-persona-cuenta-bancaria",
	    configuracion :{},
	    idActual : false,
	    seleccionado : false, 
	    filtros : {  
	    	         IdPersona : {field: "IdPersona", operator: "eq", value: Number($scope.bModifMvtoMod.oMovimientoComprobante.IdPersona)},
					 IdMoneda : {field: "IdMoneda", operator : "eq", value: $scope.bModifMvtoMod.oMovimientoComprobante.IdMoneda}
	              },
	    idDefecto : ''          
    };   
  
   $scope.data.oTipoTarjBancariaFDePago =
    {
		tipo : "combobox-tipo-tarjeta-banco",
	    configuracion :{},
	    idActual : false,
	    seleccionado : false,   
    };


	function ValidaIdMonedaFormaPagoCliente(data) {
		$scope.data.oCuentaBancariaFDePago.filtros = {
			IdPersona: { field: "IdPersona", operator: "eq", value: Number($scope.bModifMvtoMod.oMovimientoComprobante.IdPersona) },
			IdMoneda: { field: "IdMoneda", operator: "eq", value: data.IdMoneda }
		}
	};
	
	
	
	//GRILLA DE FORMAS DE PAGO 
	$scope.confBasicaModificarMovimientoFormasDePago = {
		dataSource: new bcs.DataSource({
				                              type: "array",
				                              definition : {
				                            	             data : [],
					                                         schema : bcsModificarMovimientoFormasDePagoModalGrid.modeloMovimientoFormasDePago(),
					                                         aggregate : [ //Para resumen de totales de cobro
	                 			                                           { field: "MontoLiquidacionFormaDePago", aggregate: "sum" },						                  
						                                                 ]  
				                                           }}),	
		columns				: bcsModificarMovimientoFormasDePagoModalGrid.columnasMovimientoFormasDePago(),		
		sortable			: true,
		pageable			: {
								pageSize	: 2
							  },		
		height				: 150,
		//width				: 300,
		selectable 			: "row",
		autoBind            : true
	 };
	
	 
	//Almacena los datos para mostrar en la grilla	
	var ModifMvtoFormasDePagoDatasource = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			filter : { 
				field : "IdMovimientoComprobante",
				operator : "eq",
				value : $scope.bModifMvtoMod.oMovimientoComprobante.IdMovimientoComprobante
			},
			transport : {
				read : {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1237",
					dataType : "json"
				}
			},
			schema : bcsModificarMovimientoFormasDePagoModalGrid.modeloMovimientoFormasDePago()
		}
	});	
	
	//Carga Grilla con datasource 
	function ModificarMvtoFormasDePago ()
	  {
	  	 //Limpia seleccionado de grilla
		 $scope.seleccionModificarMovimientoFormasDePago = undefined;
		 
		 //Lee datos de grilla
		 ModifMvtoFormasDePagoDatasource.fetch().then(function() 
		   {
			  var data = ModifMvtoFormasDePagoDatasource.data();
			
			  if ($scope.gridApiModificarMovimientoFormasDePago) 
				 {
				    $scope.gridApiModificarMovimientoFormasDePago.dataSource.data(data);
				 }
			  else 
				 {
				   $scope.confBasicaModificarMovimientoFormasDePago.dataSource.definition.data = data;
				 }
			  
			  //Carga Datos en Grilla			  
			  $timeout(function() { CargaPorDefectosDatoMvtoFormasDePago(data[0]);
		      $scope.seleccionModificarMovimientoFormasDePago= data[0];} ,550);
		      		     
		   });
	  };

					
	
	//Limpiar Datos Modificables
	function LimpiarDatosMvtoFormasDePago ()
	  {	  	 
		 $scope.data.activaroMedioDePago = true;
		 $scope.data.oMedioDePago = ''; 
		 $scope.data.activaroModalidadMedPago = false;
		 $scope.data.oModalidadMedPago.idActual = 'MODDEP';
		 $scope.data.activaroMontoFDePago = true;
         $scope.data.oMontoFDePago = 0;
         $scope.data.activaroCuentaBancariaFDePago = false;
		 $scope.data.oCuentaBancariaFDePago.idActual = 0;
		 $scope.data.activaroTipoCuentaBancariaFDePago = true;
		 $scope.data.oTipoCuentaBancariaFDePago = '';
		 $scope.data.activaroBancoFDePago = true;
         $scope.data.oBancoFDePago = '';
         $scope.data.activaroNumeroCuentaBancaria = true;
		 $scope.data.oNumeroCuentaBancaria = '';		 
         $scope.data.activaroMonedaFDePago = true;
         $scope.data.oMonedaFDePago = "";
         $scope.data.activaroNumeroSerie = true;
		 $scope.data.oNumeroSerie = '';		 
		 $scope.data.activaroNumeroDocumento = true;
		 $scope.data.oNumeroDocumento = 0;		 
		 $scope.data.activaroFechaDocumento = true;
		 $scope.data.oFechaDocumento = '0001-01-01';
		 $scope.data.activaroTipoTarjBancariaFDePago = false;
		 $scope.data.oTipoTarjBancariaFDePago.idActual = 0;
		 $scope.data.activaroNumeroTarjetaBancaria = true;
		 $scope.data.oNumeroTarjetaBancaria = '';
		 $scope.data.activaroFechaVencimientoTarjeta = true;
		 $scope.data.oFechaVencimientoTarjeta = '';
		 $scope.data.activaroEstadoMovimientoFDePago = false;
		 $scope.data.oEstadoMovimientoFDePago.idActual = 0;		 
	  };

	function ValidaCuentaClienteFormaPago(movimiento)
	  {
			
		if((movimiento.IdTipoMovimiento === 'CMMESP' || movimiento.IdTipoMovimiento === 'VNMESP')){
			ValidaIdMonedaFormaPagoCliente(movimiento);
		}

		  if(movimiento.IdTipoMovimiento === 'CMMESP' && movimiento.IdTipoMovimientoSpot === 'MOVING')
		  {
			  //Si es compra spot y es un ingreso, la forma de pago es cuenta propia
			  return true;
		  }
		  else if(movimiento.IdTipoMovimiento === 'VNMESP' && movimiento.IdTipoMovimientoSpot === 'MOVING')
		  {
			  //Si es venta spot y es un egreso, la forma de pago es cuenta propia
			  return true;
		  }
		  else
		  {
			  return false;
		  }
	  }  
	//Carga Datos por defecto
	function CargaPorDefectosDatoMvtoFormasDePago(primerDatoGrilla)
	  {
		
         //Valida segun medio de pago los objetos habilitados para modificar
		LimpiarDatosMvtoFormasDePago();
		
		//Valida que grilla contenga datos
		//if (!primerDatoGrilla) return;
		if (!primerDatoGrilla)
		 {
		   var data = [];		   

		   data.push({NombreMedioDePago:'CUENTA INVERSION', NombreModalidadDePago:'DEPOSITO', NombreCuentaBancaria:'',
					  NombreTipoCuentaBancaria:'', NumeroCuentaBancaria:0, NombreBanco:'',
					  NombreCuentaBancaria:'', NombreTipoTarjetaBancaria:'', NumeroTarjetaBancaria:0,
					  FechaVctoTarjetaBancaria:'0001-01-01', NumeroSerie:'', NumeroDocumento:0, FechaDocumentoPago:'0001-01-01',
					  IdMoneda:$scope.bModifMvtoMod.oMovimientoComprobante.IdMonedaLiquidacion, Monto:$scope.bModifMvtoMod.oMovimientoComprobante.Monto, 
					  NombreEstado:'ACTIVO', IdMovimientoComprobanteFormaDePago:0, IdMedioDePago:'CIN',
					  IdMovimientoComprobante:$scope.bModifMvtoMod.oMovimientoComprobante.IdMovimientoComprobante, 
					  IdModalidadDePago:'MODDEP', IdBanco:0, IdTipoCuentaBancaria:'', IdCuentaBancaria:0, IdTarjetaBancaria:''});

		   $scope.gridApiModificarMovimientoFormasDePago.dataSource.data(data);
           
		   return;
		 } 

		//Asigna Variables para funcion de insercion
        IdTipoCuentaBancariaFDePago = '';
        NombreTipoCuentaBancaria = '';

	    IdBanco = 0;
	    CodigoBanco = '';
        NombreBanco = '';
     	     
	    IdMoneda = $scope.bModifMvtoMod.oMovimientoComprobante.IdMonedaLiquidacion;  

		
		//Carga en campos el primer registro de grilla
		//overlay.encendido = true;
		bcsComunes.ventanaOverlay(idVentana, true);
		
		var IdMedioDePago = primerDatoGrilla.IdMedioDePago;		
		switch (IdMedioDePago)
	  	  { 
		     //Transferencias:
		     case "TDP":
		     case "TSW":
		     case "TLR":
		     case "TRA":
		       { 
		         //Medio de Pago
				 $scope.data.oMedioDePago = primerDatoGrilla.NombreMedioDePago;
				 //Modalidad
				 $scope.data.activaroModalidadMedPago = false; 
				 $scope.data.oModalidadMedPago.IdActual = 'MODDEP';
				 //Monto
				 $scope.data.activaroMontoFDePago = true;
				 $scope.data.simbolooMontoFDePago = primerDatoGrilla.IdMoneda;
				 $scope.data.decimalesoMontoFDePago = (util.bcs_formato(0,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).cantidadDecimales;
				 $scope.data.oMontoFDePago = (util.bcs_formato(primerDatoGrilla.MontoLiquidacionFormaDePago,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).valor;
				 //Moneda
				 $scope.data.oMonedaFDePago = primerDatoGrilla.IdMoneda;
				 //Banco
				 $scope.data.oBancoFDePago = primerDatoGrilla.NombreBanco;
				 //Estado
				 $scope.data.activaroEstadoMovimientoFDePago = true;
				 $scope.data.oEstadoMovimientoFDePago.idActual = primerDatoGrilla.IdEstado
				 //Cuenta Bancaria
				 $scope.data.activaroCuentaBancariaFDePago = !ValidaCuentaClienteFormaPago(primerDatoGrilla);
				 $scope.data.oCuentaBancariaFDePago.idActual = primerDatoGrilla.IdCuentaBancaria;		
			     //Tipo Cuenta
			     $scope.data.oTipoCuentaBancariaFDePago = primerDatoGrilla.NombreTipoCuentaBancaria;
			     //Nro Cuenta
			     $scope.data.oNumeroCuentaBancaria = primerDatoGrilla.NumeroCuentaBancaria;
			     //Nro Serie
			     $scope.data.activaroNumeroSerie = true;
			     $scope.data.oNumeroSerie = '';
			     //Nro Documento
			     $scope.data.activaroNumeroDocumento = true;
			     $scope.data.oNumeroDocumento = 0;
			     //Fecha Documento
			     $scope.data.activaroFechaDocumento = true;
			     $scope.data.oFechaDocumento = '0001-01-01';
			     //Tipo Tarjeta
			     $scope.data.activaroTipoTarjBancariaFDePago = false;
		         $scope.data.oTipoTarjBancariaFDePago.idActual = 0;			     
			     //Nro Tarjeta
		         $scope.data.activaroNumeroTarjetaBancaria = true;
			     $scope.data.oNumeroTarjetaBancaria = '';			     
			     //Fecha Tarjeta
			     $scope.data.activaroFechaVencimientoTarjeta = true;
			     $scope.data.oFechaVencimientoTarjeta = '';			     
			     break;
		       }
		     //Cheque:  
			 case "CHE":
			   { 
			     //Medio de Pago				 
			     $scope.data.oMedioDePago = primerDatoGrilla.NombreMedioDePago;
			     //Modalidad
			     $scope.data.activaroModalidadMedPago = true;
			     $scope.data.oModalidadMedPago.idActual = primerDatoGrilla.IdModalidadDePago 
				 //Monto
				 $scope.data.activaroMontoFDePago = true;
				 $scope.data.simbolooMontoFDePago = primerDatoGrilla.IdMoneda;
				 $scope.data.decimalesoMontoFDePago = (util.bcs_formato(0,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).cantidadDecimales;
				 $scope.data.oMontoFDePago = (util.bcs_formato(primerDatoGrilla.MontoLiquidacionFormaDePago,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).valor;
				 //Cuenta Bancaria
				 $scope.data.activaroCuentaBancariaFDePago = true;
				 $scope.data.oCuentaBancariaFDePago.idActual = primerDatoGrilla.IdCuentaBancaria;
				 //Tipo Cuenta Bancaria
				 $scope.data.oTipoCuentaBancariaFDePago = primerDatoGrilla.NombreTipoCuentaBancaria;
				 //Banco
				 $scope.data.oBancoFDePago = primerDatoGrilla.NombreBanco;
				 //Nro Cuenta Bancaria 
			     $scope.data.oNumeroCuentaBancaria = primerDatoGrilla.NumeroCuentaBancaria;
				 //Moneda
				 $scope.data.oMonedaFDePago = primerDatoGrilla.IdMoneda;
				 //Nro Serie
				 $scope.data.activaroNumeroSerie = false;
			     $scope.data.oNumeroSerie = primerDatoGrilla.NumeroSerie;
                 //Nro Documento
			     $scope.data.activaroNumeroDocumento = false;
			     $scope.data.oNumeroDocumento = primerDatoGrilla.NumeroDocumento;
			     //Fecha Documento
			     $scope.data.activaroFechaDocumento = false;			     
		         $scope.data.oFechaDocumento = primerDatoGrilla.FechaDocumentoPago;
		         //Tipo Tarjeta
		         $scope.data.activaroTipoTarjBancariaFDePago = false;
		         $scope.data.oTipoTarjBancariaFDePago.idActual = 0;			     
			     //Nro Tarjeta
		         $scope.data.activaroNumeroTarjetaBancaria = true;
			     $scope.data.oNumeroTarjetaBancaria = '';			     	
			     //Fecha Tarjeta
			     $scope.data.activaroFechaVencimientoTarjeta = true;			     
		         $scope.data.oFechaVencimientoTarjeta = '';			     
				 //Estado
				 $scope.data.activaroEstadoMovimientoFDePago = true;
				 $scope.data.oEstadoMovimientoFDePago.idActual = primerDatoGrilla.IdEstado;	         
			     
			     break;
			   }
			 //Efectivo:  
			 case "EFE":
			   { 
				 //Medio de Pago 
			     $scope.data.oMedioDePago = primerDatoGrilla.NombreMedioDePago;
			     //Modalidad
			     $scope.data.activaroModalidadMedPago = true;
				 $scope.data.oModalidadMedPago.idActual = primerDatoGrilla.IdModalidadDePago;
				 //Monto				 
				 $scope.data.activaroMontoFDePago = true;
				 $scope.data.simbolooMontoFDePago = primerDatoGrilla.IdMoneda;
				 $scope.data.decimalesoMontoFDePago = (util.bcs_formato(0,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).cantidadDecimales;
				 $scope.data.oMontoFDePago = (util.bcs_formato(primerDatoGrilla.MontoLiquidacionFormaDePago,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).valor;
				 //Cuenta Bancaria
				 $scope.data.activaroCuentaBancariaFDePago = true;
				 $scope.data.oCuentaBancariaFDePago.idActual = primerDatoGrilla.IdCuentaBancaria;
				 //Tipo Cuenta
			     $scope.data.oTipoCuentaBancariaFDePago = primerDatoGrilla.NombreTipoCuentaBancaria;
			     //Banco
				 $scope.data.oBancoFDePago = primerDatoGrilla.NombreBanco;
				 //Nro Cuenta
			     $scope.data.oNumeroCuentaBancaria = primerDatoGrilla.NumeroCuentaBancaria;
				 //Moneda
				 $scope.data.oMonedaFDePago = primerDatoGrilla.IdMoneda;
				 //Nro Serie
				 $scope.data.activaroNumeroSerie = true;
		         $scope.data.oNumeroSerie = '';				 
				 //Nro Documento
		         $scope.data.activaroNumeroDocumento = true;
		         $scope.data.oNumeroDocumento = 0;
		         //Fecha Documento
				 $scope.data.activaroFechaDocumento = true;
				 $scope.data.oFechaDocumento = '0001-01-01';
				 //Tipo Tarjeta
			     $scope.data.activaroTipoTarjBancariaFDePago = false;
			     $scope.data.oTipoTarjBancariaFDePago.idActual = 0;
			     //Nro Tarjeta
			     $scope.data.activaroNumeroTarjetaBancaria = true; 
			     $scope.data.oNumeroTarjetaBancaria = '';	
			     //Fecha Tarjeta
			     $scope.data.activaroFechaVencimientoTarjeta = true;
				 $scope.data.oFechaVencimientoTarjeta = '';			     
				 //Estado
				 $scope.data.activaroEstadoMovimientoFDePago = true;
				 $scope.data.oEstadoMovimientoFDePago.idActual = primerDatoGrilla.IdEstado;				 
			     
			     break;
			   }
			 //Vale Vista:  
			 case "VVI":
			   { 
				 //Medio de Pago 
				 $scope.data.oMedioDePago = primerDatoGrilla.NombreMedioDePago;
				 //Modalidad
				 $scope.data.activaroModalidadMedPago = true;
				 $scope.data.oModalidadMedPago.idActual = primerDatoGrilla.IdModalidadDePago;
				 //Monto
				 $scope.data.activaroMontoFDePago = true;
				 $scope.data.simbolooMontoFDePago = primerDatoGrilla.IdMoneda;
				 $scope.data.decimalesoMontoFDePago = (util.bcs_formato(0,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).cantidadDecimales;
				 $scope.data.oMontoFDePago = (util.bcs_formato(primerDatoGrilla.MontoLiquidacionFormaDePago,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).valor;
				 //Moneda
				 $scope.data.oMonedaFDePago = primerDatoGrilla.IdMoneda;
				 //Banco
				 $scope.data.oBancoFDePago = primerDatoGrilla.NombreBanco;
				 //Estado
				 $scope.data.activaroEstadoMovimientoFDePago = true;
				 $scope.data.oEstadoMovimientoFDePago.idActual = primerDatoGrilla.IdEstado;
				 //Cuenta Bancaria
				 $scope.data.activaroCuentaBancariaFDePago = true;
				 $scope.data.oCuentaBancariaFDePago.idActual = primerDatoGrilla.IdCuentaBancaria;
				 //Tipo Cuenta
			     $scope.data.oTipoCuentaBancariaFDePago = primerDatoGrilla.NombreTipoCuentaBancaria;
			     //Nro Cuenta
			     $scope.data.oNumeroCuentaBancaria = primerDatoGrilla.NumeroCuentaBancaria;
			     //Nro Serie
			     $scope.data.activaroNumeroSerie = true;
			     $scope.data.oNumeroSerie = '';			     
			     //Nro Documento
			     $scope.data.activaroNumeroDocumento = true;
			     $scope.data.oNumeroDocumento = 0;
			     //Fecha Documento
			     $scope.data.activaroFechaDocumento = true;
			     $scope.data.oFechaDocumento = '0001-01-01';
			     //Tipo Tarjeta
			     $scope.data.activaroTipoTarjBancariaFDePago = false;
			     $scope.data.oTipoTarjBancariaFDePago.idActual = 0;
			     //Nro Tarjeta
			     $scope.data.activaroNumeroTarjetaBancaria = true;
			     $scope.data.oNumeroTarjetaBancaria = '';			     
			     //Fecha Tarjeta
			     $scope.data.activaroFechaVencimientoTarjeta = true;
			     $scope.data.oFechaVencimientoTarjeta = '';
			     break;
			   }    
			 //Cuenta Inversion:  
			 case "CIN":
			   { 
				 //Medio de Pago 
				 $scope.data.oMedioDePago = primerDatoGrilla.NombreMedioDePago;
				 //Modalidad
				 $scope.data.activaroModalidadMedPago = true;
				 $scope.data.oModalidadMedPago.idActual = primerDatoGrilla.IdModalidadDePago;
				 //Monto
				 $scope.data.activaroMontoFDePago = true;
				 $scope.data.simbolooMontoFDePago = primerDatoGrilla.IdMoneda;
				 $scope.data.decimalesoMontoFDePago = (util.bcs_formato(0,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).cantidadDecimales;
				 $scope.data.oMontoFDePago = (util.bcs_formato(primerDatoGrilla.MontoLiquidacionFormaDePago,'GEN','Monto',primerDatoGrilla.IdMoneda,'','')).valor;
				 //Moneda
				 $scope.data.oMonedaFDePago = primerDatoGrilla.IdMoneda;
				 //Banco
				 $scope.data.oBancoFDePago = primerDatoGrilla.NombreBanco;
				 //Estado
				 $scope.data.activaroEstadoMovimientoFDePago = true;
				 $scope.data.oEstadoMovimientoFDePago.idActual = 'ACT';
				 //Cuenta Bancaria
				 $scope.data.activaroCuentaBancariaFDePago = false;
				 $scope.data.oCuentaBancariaFDePago.idActual = primerDatoGrilla.IdCuentaBancaria;
				 //Tipo Cuenta
			     $scope.data.oTipoCuentaBancariaFDePago = primerDatoGrilla.NombreTipoCuentaBancaria;
			     //Nro Cuenta
			     $scope.data.oNumeroCuentaBancaria = primerDatoGrilla.NumeroCuentaBancaria;
			     //Nro Serie
			     $scope.data.activaroNumeroSerie = true;
			     $scope.data.oNumeroSerie = '';			     
			     //Nro Documento
			     $scope.data.activaroNumeroDocumento = true;
			     $scope.data.oNumeroDocumento = 0;
			     //Fecha Documento
			     $scope.data.activaroFechaDocumento = true;
			     $scope.data.oFechaDocumento = '0001-01-01';
			     //Tipo Tarjeta
			     $scope.data.activaroTipoTarjBancariaFDePago = false;
			     $scope.data.oTipoTarjBancariaFDePago.idActual = 0;
			     //Nro Tarjeta
			     $scope.data.activaroNumeroTarjetaBancaria = true;
			     $scope.data.oNumeroTarjetaBancaria = '';			     
			     //Fecha Tarjeta
			     $scope.data.activaroFechaVencimientoTarjeta = true;
			     $scope.data.oFechaVencimientoTarjeta = '';
			     break;
			   }    			     
		     default:  //Otros: muestra combo "Modalidad pago" y activa todas las modalidades
		    	 break;
	   	  };		
	
		//overlay.encendido = false;
		bcsComunes.ventanaOverlay(idVentana, false);
	  };
	
	
	//Ejecuta function de carga grilla
	ModificarMvtoFormasDePago();
	
	
	//Activa botones depende de Medio De Pago y Modalidad	
	$scope.$watch('seleccionModificarMovimientoFormasDePago',function(newVal,oldVal) 
	  {					
		if (!newVal) return;
		
		//Respalda valor inicial de arreglo de movimientos 
		$scope.MvtoCbmteFDePagoOriginal = $.extend({},newVal);
		$scope.MvtoCbmteFDePago = $.extend({},newVal)
		
		//Valida segun medio de pago los objetos habilitados para modificar
		//overlay.encendido = true;
		bcsComunes.ventanaOverlay(idVentana, true);
		
		//Carga Datos en Grilla
		CargaPorDefectosDatoMvtoFormasDePago(newVal);
		
		//overlay.encendido = false;		
		bcsComunes.ventanaOverlay(idVentana, false);
      });
	
   $scope.$watch('data.oCuentaBancariaFDePago.seleccionado',function(newVal,oldVal)
	   {
		
	   	 if (!newVal) return;
	   	 
	   	 $scope.data.simbolooMontoFDePago = newVal.IdMoneda;
	     $scope.data.oTipoCuentaBancariaFDePago =  $scope.data.oCuentaBancariaFDePago.seleccionado.NombreTipoCuentaBancaria;
	     IdTipoCuentaBancariaFDePago = $scope.data.oCuentaBancariaFDePago.seleccionado.IdTipoCuentaBancaria;
         NombreTipoCuentaBancaria = $scope.data.oCuentaBancariaFDePago.seleccionado.NombreTipoCuentaBancaria? $scope.data.oCuentaBancariaFDePago.seleccionado.NombreTipoCuentaBancaria:'';

	     $scope.data.oBancoFDePago = $scope.data.oCuentaBancariaFDePago.seleccionado.NombreBanco;
	     IdBanco = Number($scope.data.oCuentaBancariaFDePago.seleccionado.IdBanco);
	     CodigoBanco = $scope.data.oCuentaBancariaFDePago.seleccionado.CodigoBanco?$scope.data.oCuentaBancariaFDePago.seleccionado.CodigoBanco:'';
         NombreBanco = $scope.data.oCuentaBancariaFDePago.seleccionado.NombreBanco?$scope.data.oCuentaBancariaFDePago.seleccionado.NombreBanco:'';

	     $scope.data.oNumeroCuentaBancaria = $scope.data.oCuentaBancariaFDePago.seleccionado.NumeroCuentaBancaria;
	    
	     $scope.data.oMonedaFDePago = $scope.data.oCuentaBancariaFDePago.seleccionado.IdMoneda;
	     
	     var monto = $scope.MvtoCbmteFDePago.MontoLiquidacionFormaDePago;
	     /*
	     if(newVal.IdMoneda === 'CLP'){
	    	 monto = ($scope.dato.Precio * $scope.dato.Cantidad);
		 }else{
			 monto =($scope.dato.Precio * $scope.dato.Cantidad) / $scope.dato.Precio;
		 }*/
		 
	     $scope.data.decimalesoMontoFDePago = (util.bcs_formato(0,'GEN','Monto',newVal.IdMoneda,'','')).cantidadDecimales;
	     $scope.data.oMontoFDePago = (util.bcs_formato(monto,'GEN','Monto',newVal.IdMoneda,'','')).valorNumerico;
       	 
	   });	
		
			
   //Actualiza el registro seleccionado 	
   $scope.data.modificarMovimientoComprobanteFormDePago = function()   
     {	
	    bcsComunes.ventanaOverlay(idVentana, true);
	    
	    //Valida que se hayan seleccionado movimientos forma de pago
		if ($scope.seleccionModificarMovimientoFormasDePago === undefined)
		  {	
			 $scope.data.btnActivos   = true;     //Activa botones
			 bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
			 //overlay.encendido = false;
			 bcsComunes.ventanaOverlay(idVentana, false);

			 return;
		  };
		  
	    //Envio a actualizar forma de pago
	   	bcsComunes.setConflictos([]);
	   	
	   	//Acumula tabla de entrada de movimiento comprobante - forma de pago
		var MovimientoFacturarFDPago = [];
		MovimientoFacturarFDPago.push(
	      		                     {     
	      			                    IdMovimientoComprobanteFormaDePago : $scope.seleccionModificarMovimientoFormasDePago.IdMovimientoComprobanteFormaDePago,
	      			                    IdMovimientoComprobante : $scope.seleccionModificarMovimientoFormasDePago.IdMovimientoComprobante,
	      			                    IdTipoMovimiento : $scope.bModifMvtoMod.oMovimientoComprobante.IdConceptoMovimiento,
	      			                    IdMedioDePago : $scope.seleccionModificarMovimientoFormasDePago.IdMedioDePago,
	      			                    NombreMedioDePago : $scope.seleccionModificarMovimientoFormasDePago.NombreMedioDePago,
	      			                    IdModalidadDePago : ($scope.data.oModalidadMedPago.seleccionado.IdModalidadDePago !== $scope.seleccionModificarMovimientoFormasDePago.IdModalidadDePago) || ($scope.seleccionModificarMovimientoFormasDePago.IdMovimientoComprobanteFormaDePago === 0)?$scope.data.oModalidadMedPago.seleccionado.IdModalidadDePago:null,
	      			                    NombreModalidadDePago : ($scope.data.oModalidadMedPago.seleccionado.Nombre !== $scope.seleccionModificarMovimientoFormasDePago.NombreModalidadDePago) || ($scope.seleccionModificarMovimientoFormasDePago.IdMovimientoComprobanteFormaDePago === 0)?$scope.data.oModalidadMedPago.seleccionado.Nombre:null,
	      			                    MontoLiquidacionFormaDePago : ($scope.data.oMontoFDePago !== $scope.seleccionModificarMovimientoFormasDePago.Monto) || ($scope.seleccionModificarMovimientoFormasDePago.IdMovimientoComprobanteFormaDePago === 0)?$scope.data.oMontoFDePago:null,
	      			                    IdMoneda : ($scope.data.oMonedaFDePago !== $scope.seleccionModificarMovimientoFormasDePago.IdMoneda) || ($scope.seleccionModificarMovimientoFormasDePago.IdMovimientoComprobanteFormaDePago === 0)?$scope.data.oMonedaFDePago:null,
   			                    		//IdBanco : $scope.data.oBancoFDePago !== $scope.seleccionModificarMovimientoFormasDePago.IdBanco?$scope.data.oBancoFDePago:null,
   			                    		IdBanco : IdBanco !== $scope.seleccionModificarMovimientoFormasDePago.IdBanco?IdBanco:null,
   			                    		CodigoBanco : CodigoBanco !== $scope.seleccionModificarMovimientoFormasDePago.CodigoBanco?CodigoBanco:null,
   			                    		NombreBanco : NombreBanco !== $scope.seleccionModificarMovimientoFormasDePago.NombreBanco?NombreBanco:null,
   			                    		IdEstado : ($scope.data.oEstadoMovimientoFDePago.seleccionado.IdEstado !== $scope.seleccionModificarMovimientoFormasDePago.IdEstado) || ($scope.seleccionModificarMovimientoFormasDePago.IdMovimientoComprobanteFormaDePago === 0)?$scope.data.oEstadoMovimientoFDePago.seleccionado.IdEstado:null,
   			                    		IdCuentaBancaria : $scope.data.oCuentaBancariaFDePago.seleccionado.IdCuentaBancaria !== $scope.seleccionModificarMovimientoFormasDePago.IdCuentaBancaria?$scope.data.oCuentaBancariaFDePago.seleccionado.IdCuentaBancaria:null,
   			                    		NombreCuentaBancaria : $scope.data.oCuentaBancariaFDePago.seleccionado.NombreCuentaBancariaFull !== $scope.seleccionModificarMovimientoFormasDePago.NombreCuentaBancaria?$scope.data.oCuentaBancariaFDePago.seleccionado.NombreCuentaBancariaFull:null,
   			                    		NumeroCuentaBancaria : $scope.data.oNumeroCuentaBancaria !== $scope.seleccionModificarMovimientoFormasDePago.NumeroCuentaBancaria?$scope.data.oNumeroCuentaBancaria:null,
   			                    		IdTarjetaBancaria : 0,
   			                    		FechaVctoTarjetaBancaria : $scope.data.oFechaVencimientoTarjeta !== $scope.seleccionModificarMovimientoFormasDePago.FechaVctoTarjetaBancaria?$scope.data.oFechaVencimientoTarjeta:null,
   			                    		IdTipoCuentaBancaria	: IdTipoCuentaBancariaFDePago !== $scope.seleccionModificarMovimientoFormasDePago.IdTipoCuentaBancaria?IdTipoCuentaBancariaFDePago:null,		
   			                    		NombreTipoCuentaBancaria : NombreTipoCuentaBancaria !== $scope.seleccionModificarMovimientoFormasDePago.NombreTipoCuentaBancaria?NombreTipoCuentaBancaria:null,
   			                    		NumeroSerie : $scope.data.oNumeroSerie !== $scope.seleccionModificarMovimientoFormasDePago.NumeroSerie?$scope.data.oNumeroSerie:null,		
   			                    		NumeroDocumento : $scope.data.oNumeroDocumento !== $scope.seleccionModificarMovimientoFormasDePago.NumeroDocumento?$scope.data.oNumeroDocumento:null,		
	      			                    FechaDocumentoPago : $scope.data.oFechaDocumento !== $scope.seleccionModificarMovimientoFormasDePago.FechaDocumentoPago?$scope.data.oFechaDocumento:null,
	      			                    IdTipoTarjetaBancaria : $scope.data.oTipoTarjBancariaFDePago.seleccionado.IdTipoTarjetaBancaria !== $scope.seleccionModificarMovimientoFormasDePago.IdTipoTarjetaBancaria?$scope.data.oTipoTarjBancariaFDePago.seleccionado.IdTipoTarjetaBancaria:null,		
	      			                    NombreTipoTarjetaBancaria : $scope.data.oTipoTarjBancariaFDePago.seleccionado.NombreTipoTarjetaBancaria !== $scope.seleccionModificarMovimientoFormasDePago.NombreTipoTarjetaBancaria?$scope.data.oTipoTarjBancariaFDePago.seleccionado.NombreTipoTarjetaBancaria:null,
	      			                    NumeroTarjetaBancaria : $scope.data.oNumeroTarjetaBancaria !== $scope.seleccionModificarMovimientoFormasDePago.NumeroTarjetaBancaria?$scope.data.oNumeroTarjetaBancaria:null,
	      			                    IdTipoMovimientoSpot : ($scope.data.oTipoMovimientoSpot !== $scope.seleccionModificarMovimientoFormasDePago.IdTipoMovimientoSpot)?$scope.data.oTipoMovimientoSpot:null,
	      			                    AfectarTesoreria : $scope.data.oAfectarTesoreria !== $scope.seleccionModificarMovimientoFormasDePago.AfectarTesoreria?$scope.data.oAfectarTesoreria:null,
	      			                    MontoMovimientoComprobante : $scope.bModifMvtoMod.oMovimientoComprobante.MontoMovimientoComprobante
	      			                 }
	      		                   );
	    		
		//parametros de servicio	   	   	
		var datos = {};
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo = 'FAC';
		datos.p_simularOperacion = 'N';
		datos.p_UsaReglasNegocio = 'S';
		datos.p_EsIngresoAutomatico = 'N';
		datos.P_MOVIMIENTOFACTURARFDPAGO = MovimientoFacturarFDPago;		
    								
		bcsServiciosFacturacion.ModificarMovimientoComprobanteCallSP(datos).$promise.then(function(respuesta){		  
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
					
				//Ejecuta function de carga grilla
				ModificarMvtoFormasDePago();

				//Limpia datos panel  
				LimpiarDatosMvtoFormasDePago();
			} else{
				if (respuesta.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({
					   mensaje:$translate.instant(""),
					   conflictos:util.parseServerResponse(respuesta)
					}, idVentana);
				} else if(respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
					bcsComunes.ventanaMsg({
						 mensaje:$translate.instant(""),
						 conflictos:util.parseServerResponse(respuesta)
					}, idVentana); 
				}
				  
			}
		},function(respuesta){
			bcsComunes.ventanaOverlay(idVentana, false);				  
			bcsComunes.msg("error","Error",'UI_ERROR_MODIFICACION');
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		});	

		//overlay.encendido = false;
		bcsComunes.ventanaOverlay(idVentana, false);
    };
	 
	 
	 
   //Cancela Operación	
   $scope.data.cancelar = function() 
     {	     
		bcsComunes.ventanaCerrar(idVentana);	
	 };	
	 
   
   //Opción no habilitada
   $scope.data.opcionNoHabilitada = function() 
     {
		bcsComunes.msg("warning","warning","Está opción no se encuentra habilitada.");
	 };
});
