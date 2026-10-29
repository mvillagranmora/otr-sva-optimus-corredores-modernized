angular.module('ModuloTesoreria').factory('bcsBlotterCuentaBancariaGrid',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
  
	//CUENTAS BANCARIAS TESORERIA	
	obj.dsCuentasBancarias = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdCuentaBanco", dir: "asc" },
			pageSize 			: 50,			
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1017",dataType : "json"}},
			schema : {
				model : {
					fields : {
						 IdCuentaBanco			  : { type: "number"},
						 IdBanco 				  : { type: "number"},
						 NombreBanco 			  : { type: "string"},					 
						 CodigoBanco              : { type: "string"},
						 NombreCuentaBancaria 	  : { type: "string"},
						 IdTipoCuentaBancaria	  : { type: "string"},
						 NombreTipoCuentaBancaria : { type: "string"},
						 IdMoneda				  : { type: "string"},
						 NombreMoneda 			  : { type: "string"},
						 NumeroCuentaBancaria 	  : { type: "string"},
						 Sucursal                 : { type: "string"},
						 Contacto                 : { type: "string"},
						 TelefonoContacto         : { type: "string"},
						 IdEstado                 : { type: "string"},
						 NombreEstado             : { type: "string"},
						 SaldoInicial			  : { type: "number"},
						 IdPlaza			      : { type: "number"},
						 NombrePlaza	          : { type: "string"},
						 AfectaPosicionInicial	  : { type: "string"},
						 NombreGeneraNomina	      : { type: "string"},
						 NombreGeneraTransferencia: { type: "string"},
						 NombreEmpleadoFirma1	  : { type: "string"},
						 MontoSegundaFirma	  	  : { type: "number"},
						 NombreEmpleadoFirma2	  : { type: "string"},
						 CodigoCuentaInterbancario  : { type: "string"},
						 CodigoAba  				: { type: "string"},
						 OtroCodigoBancoExtranjero  : { type: "string"},
						 CodigoIban  				: { type: "string"},
						 OtroCodigoCuentaExtranjero : { type: "string"},
						 RecaudacionExclusivaPAC : { type: "string"}
					}
				}
			}
		}
	});
	
	
	obj.columnasCuentasBancarias = function() {
		return  [
			 {field: "NombreBanco",              title: "UI_BANCO",         width: "125px", groupable: true }
			,{field: "NombreCuentaBancaria",     title: "UI_ALIAS_CUENTA_BANCO",         width: "120px"}
			,{field: "CodigoBanco",              title: "UI_CODIGO_BANCO",  width: "70px"}
			,{field: "NombreTipoCuentaBancaria", title: "UI_TIPO_CUENTA",   width: "95px",  groupable: true , attributes:{style:"white-space:nowrap;"}}
			,{field: "NombreMoneda",             title: "UI_MONEDA",        width: "90px",  groupable: true , attributes:{style:"white-space:nowrap;"} }			
			,{field: "NumeroCuentaBancaria",     title: "UI_NUMERO_CUENTA", width: "90px"}			
			,{field: "NombreEstado",             title: "UI_ESTADO",        width: "70px",  sortable : false}
			,{field: "Sucursal",                 title: "UI_SUCURSAL",      width: "90px",  groupable: true }
			,{field: "NombrePlaza",              title: "UI_PLAZA",         width: "90px",  groupable: true }
			,{field: "Contacto",                 title: "UI_CONTACTO",      width: "100px", groupable: true }
			,{field: "TelefonoContacto",         title: "UI_TELEFONO"}
			,{field: "SaldoInicial",             title: "UI_SALDO_INICIAL",  		width: "80px", groupable: true, template : " <label> #:  (util.bcs_formato(SaldoInicial,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
			,{field: "NombreGeneraNomina",       title: "UI_NOMINA",         		width: "70px", groupable: true }
			,{field: "NombreGeneraTransferencia",title: "UI_TRANSFERENCIA",  		width: "70px", groupable: true }
			,{field: "NombreEmpleadoFirma1",     title: "UI_PRIMERA_FIRMA", 		width: "150px"}
			,{field: "MontoSegundaFirma",        title: "UI_MONTO_SEGUNDA_FIRMA", 	width: "70px", template : " <label> #:  (util.bcs_formato(MontoSegundaFirma,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field: "NombreEmpleadoFirma2",     title: "UI_SEGUNDA_FIRMA", 		width: "150px"}
			,{field: "RecaudacionExclusivaPAC",	 title: "UI_RECAUDACION_EXCLUSIVA_PAC", width: "80px"}
			
		];
	};
	
	

	
	
	//CHEQUERAS TESORERIA
	obj.dsChequeras = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdChequera", dir: "asc" },
			pageSize 			: 50,			
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1018",dataType : "json"}},
			schema : {
				model : {
					fields : {
						 IdChequera 			  : { type: "number"},
						 FechaRetiro			  : { type: "date"},
						 Serie					  : { type: "string"},
						 CantidadCheques		  : { type: "string"},
						 Remanente				  : { type: "string"},
						 NumeroChequeDesde		  : { type: "string"},
						 NumeroChequeHasta		  : { type: "string"},
						 NumeroChequeActual		  : { type: "string"},
						 IdCuentaBanco			  : { type: "number"},
						 IdEmpresa				  : { type: "number"},
						 IdBanco 				  : { type: "number"},
						 NombreBanco 			  : { type: "string"},					 
						 CodigoBanco              : { type: "string"},
						 NombreCuentaBancaria 	  : { type: "string"},
						 IdTipoCuentaBancaria	  : { type: "string"},
						 NombreTipoCuentaBancaria : { type: "string"},
						 IdMoneda				  : { type: "string"},
						 NombreMoneda 			  : { type: "string"},
						 NumeroCuentaBancaria 	  : { type: "string"},
						 IdEstado                 : { type: "string"},
						 NombreEstado             : { type: "string"},
						 IdConfiguracionChequera  : { type: "string"},
						 NombreConfChequera		  : { type: "string"}
					}
				}
			}
		}
	});
	
	
	obj.columnasChequeras = function() {
		return  [		         
			 {field: "NombreCuentaBancaria",     title: "UI_NOMBRE_CUENTA_BANCARIA", width: "120px"}
			,{field: "NombreBanco",              title: "UI_BANCO",        			 width: "125px", groupable: true }			
			,{field: "NombreTipoCuentaBancaria", title: "UI_TIPO_CUENTA_BANCO",  	 width: "95px", groupable: true }
			,{field: "NombreMoneda",             title: "UI_MONEDA",       			 width: "90px", groupable: true }			
			,{field: "NumeroCuentaBancaria",     title: "UI_NUMERO_CUENTA_BANCO",	 width: "90px"}
			,{field: "FechaRetiro",     		 title: "UI_FECHA_RETIRO",			 width: "90px", format: "{0:G}"}			
			,{field: "Serie",     		 		 title: "UI_SERIE_CHEQUERA", 		 width: "90px"}			
			,{field: "CantidadCheques",     	 title: "UI_CANTIDAD_CHEQUES", 		 width: "90px"}
			,{field: "Remanente",		     	 title: "UI_REMANENTE", 			 width: "90px"}
			,{field: "NumeroChequeDesde",     	 title: "UI_CHEQUE_DESDE", 			 width: "90px"}
			,{field: "NumeroChequeHasta",     	 title: "UI_CHEQUE_HASTA", 			 width: "90px"}
			,{field: "NumeroChequeActual",     	 title: "UI_CHEQUE_ACTUAL", 		 width: "90px"}
			,{field: "NombreConfChequera",     	 title: "UI_NOMBRE_CONFIGURACION",	 width: "90px"}
			,{field: "NombreEstado",	     	 title: "UI_ESTADO", 				 width: "90px"}
		];
	};
	
	
	
	
	//CONFIGURACION DE CHEQUES TESORERIA	
	obj.dsConfiguracionChequeras = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdConfiguracionChequera", dir: "asc" },
			pageSize 			: 50,
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1019",dataType : "json"}},
			schema : {
				model : {
					fields : {
						 IdConfiguracionChequera 	: { type: "number"},
						 NombreConfChequera			: { type: "string"},
						 AnchoCheque				: { type: "number"},
						 AltoCheque					: { type: "number"},
						 SigloImpreso				: { type: "string"},
						 MontoFila					: { type: "number"},
						 MontoColumna				: { type: "number"},
						 ProvinciaFila				: { type: "number"},
						 ProvinciaColumna			: { type: "number"},
						 MesFila					: { type: "number"},
						 MesColumna 				: { type: "number"},
						 AnioFila 			  		: { type: "number"},					 
						 AnioColumna              	: { type: "number"},
						 ExtendidoAFila 	  		: { type: "number"},
						 ExtendidoAColumna	  		: { type: "number"},
						 PrimeraCantidadFila 		: { type: "number"},
						 PrimeraCantidadColumna		: { type: "number"},
						 SegundaCantidadFila 		: { type: "number"},
						 SegundaCantidadColumna 	: { type: "number"}
					}
				}
			}
		}
	});	
	
	obj.columnasconfiguracionChequeras = function() {
		return  [		         
			 {field: "NombreConfChequera",			title: "UI_NOMBRE_CONFIGURACION",		width: "120px"}
			,{field: "AnchoCheque",					title: "UI_ANCHO_CHEQUE",				width: "90px"}			
			,{field: "AltoCheque", 					title: "UI_ALTO_CHEQUE",				width: "90px"}
			,{field: "SigloImpreso",				title: "UI_SIGLO_IMPRESO",				width: "90px"}			
			,{field: "MontoFila",					title: "UI_MONTO_FILA",					width: "90px"}
			,{field: "MontoColumna",				title: "UI_MONTO_COLUMNA",				width: "90px"}
			,{field: "ProvinciaFila",				title: "UI_PROVINCIA_FILA",				width: "90px"}
			,{field: "ProvinciaColumna",			title: "UI_PROVINCIA_COLUMNA",			width: "90px"}
			,{field: "MesFila",						title: "UI_MES_FILA",					width: "90px"}
			,{field: "MesColumna",					title: "UI_MES_COLUMNA",				width: "90px"}
			,{field: "AnioFila",					title: "UI_ANIO_FILA",					width: "90px"}
			,{field: "AnioColumna",					title: "UI_ANIO_COLUMNA",				width: "90px"}
			,{field: "ExtendidoAFila",				title: "UI_EXTENDIDO_A_FILA",			width: "90px"}
			,{field: "ExtendidoAColumna",			title: "UI_EXTENDIDO_A_COLUMNA",		width: "90px"}
			,{field: "PrimeraCantidadFila",			title: "UI_PRIMERA_CANTIDAD_FILA",		width: "90px"}
			,{field: "PrimeraCantidadColumna",		title: "UI_PRIMERA_CANTIDAD_COLUMNA",	width: "90px"}
			,{field: "SegundaCantidadFila",			title: "UI_SEGUNDA_CANTIDAD_FILA",		width: "90px"}
			,{field: "SegundaCantidadColumna",		title: "UI_SEGUNDA_CANTIDAD_COLUMNA",	width: "90px"}
		];
	};
	
	return obj;
}]);	

