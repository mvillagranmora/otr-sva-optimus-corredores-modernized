angular.module('ModuloTesoreria').factory('bcsBlotterDepositosGrid', ['bcsComunes',
        function(bcsComunes)
{
	var obj = {};
	
	//DEPOSITOS
	obj.dsDepositos = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdDeposito", dir: "asc" },
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1025",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdDeposito					: {type: "number"},
						IdEmpresa					: {type: "number"},
						FechaMovimiento				: {type: "sysdate"},
						FechaIngresoRegistro		: {type: "sysdate"},
						IdTipoCuentaBanco			: {type: "string"},
						IdCuentaBanco				: {type: "string"},
						NombreCuentaBancariaFull	: {type: "string"},
						IdBanco						: {type: "number"},
						NombreBanco					: {type: "string"},
						IdMoneda					: {type: "string"},
						NombreMoneda				: {type: "string"},
						Monto						: {type: "number"},
						FechaCertificacion			: {type: "sysdate"},
						IdUsuarioCertificador		: {type: "number"},
						IdUsuario					: {type: "number"},
						IdEstado					: {type: "string"},
						NombreEstado				: {type: "string"},
						IdTipoDeposito				: {type: "string"},
						NombreTipoDeposito			: {type: "string"},
						Cantidad					: {type: "number"},
						IdConceptoDeposito			: {type: "string"},
						NombreConceptoDeposito		: {type: "string"},
						Descripcion					: {type: "string"},
					}
				}
			}
		}
	};
	obj.columnasDepositos = function() {
		return [  
				  {field : "IdDeposito",				title:"UI_SECUENCIA_DEPOSITO", 	width: "70px",		groupable: true}
				 ,{field : "FechaMovimiento",			title:"UI_FECHA_MOVIMIENTO", 	width: "85px",		groupable: true}
				 ,{field : "NombreTipoDeposito", 		title:"UI_TIPO_DEPOSITO",		width: "150px",		groupable: true}
				 ,{field : "NombreBanco", 				title:'UI_BANCO', 				width: "250px",		groupable: true}
				 ,{field : "Cantidad", 					title:'UI_CANTIDAD', 			width: "70px",		groupable: true}
				 ,{field : "NombreMoneda", 				title:'UI_MONEDA', 				width: "100px",		groupable: true}
				 ,{field : "Monto", 					title:'UI_MONTO', 				width: "100px",		template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
				 ,{field : "NombreConceptoDeposito", 	title:"UI_MOTIVO_ANULACION",	width:"200px"}
              	 ,{field : "Descripcion", 				title:"UI_COMENTARIO",			width:"250px"}
				 ,{field : "NombreEstado", 				title:"UI_ESTADO", 				width:"120px", 		groupable: true  }	
		       ];
	};
	
	//DEPOSITOS DETALLES
	obj.dsDepositoDetalle = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdDepositoDetalle", dir: "asc" },			
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1026",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdDepositoDetalle					: {type: "number"},
						IdDeposito							: {type: "number"},
						TipoDeposito						: {type: "string"},
						NombreTipoDeposito					: {type: "string"},
						IdComprobanteFormaDePago			: {type: "number"},
						IdMedioDePago						: {type: "string"},
						NombreMedioDePago					: {type: "string"},
						IdModalidadDePago					: {type: "string"},
						NombreModalidadDePago				: {type: "string"},
						IdCuentaBancariaCorredora			: {type: "number"},
						IdTipoCuentaBancariaCorredora		: {type: "string"},
						NombreTipoCuentaBancariaCorredora	: {type: "string"},
						NombreCuentaBancariaCorredora		: {type: "string"},
						NumeroCuentaBancariaCorredora		: {type: "number"},
						IdTarjetaBancariaCorredora			: {type: "number"},
						IdTipoTarjetaBancariaCorredora		: {type: "string"},
						NombreTipoTarjetaBancariaCorredora	: {type: "string"},
						NumeroTarjetaBancariaCorredora		: {type: "string"},
						IdChequera							: {type: "number"},
						NumeroDocumentoCorredora			: {type: "number"},
						FechaDocumentoPagoCorredora			: {type: "sysdate"},
						
						IdCuentaBancariaSocio				: {type: "number"},
						IdTipoCuentaBancariaSocio			: {type: "string"},
						NombreTipoCuentaBancariaSocio		: {type: "string"},
						NombreCuentaBancariaSocio			: {type: "string"},
						NumeroCuentaBancariaSocio			: {type: "string"},
						IdTarjetaBancariaSocio				: {type: "number"},
						IdTipoTarjetaBancariaSocio			: {type: "string"},
						NombreTipoTarjetaBancariaSocio		: {type: "string"},
						NumeroTarjetaBancariaSocio			: {type: "string"},
						NumeroSerieSocio					: {type: "string"},
						NumeroDocumentoSocio				: {type: "number"},
						FechaDocumentoPagoSocio				: {type: "sysdate"},
						IdMoneda							: {type: "string"},
						NombreMoneda						: {type: "string"},
						Monto								: {type: "number"},
						IdCaja								: {type: "number"},
						IdComprobante						: {type: "number"},
						FechaIngresoComprobante				: {type: "sysdate"},
						IdTipoPersona						: {type: "string"},
						IdPersona							: {type: "number"},
						NombrePersona						: {type: "string"},
						DocumentoIdentidad					: {type: "string"},
						IdConcepto							: {type: "string"},
						IdEstado							: {type: "string"},
						NombreEstado						: {type: "string"},

						NombreBancoSocio					: {type: "string"},
						DiasRetencion						: {type: "number"},
						FechaLiquidacion					: {type: "sysdate"},
						IdConceptoDeposito					: {type: "string"},
						NombreConceptoDeposito				: {type: "string"},
						Descripcion							: {type: "string"},
					}
				}
			}
		}
	};
	obj.columnasDepositoDetalle = function() {
		return [
				{field :"NombrePersona", 			title:"UI_SOCIO_NEGOCIO", 			width:"200px"}
			   ,{field :"NombreBancoSocio", 		title:"UI_BANCO", 					width:"250px"}
			   ,{field :"NombreMedioDePago", 		title:"UI_MEDIO_DE_PAGO", 			width:"100px"}
		       ,{field :"NombreTipoDeposito", 		title:"UI_TIPO_DEPOSITO", 			width:"100px"}		       
		       ,{field :"DiasRetencion",			title:"UI_DIAS_RETENCION",			width:"60px"}
		       ,{field :"FechaLiquidacion",			title:"UI_FECHA_LIQUIDACION",		width:"85px"}
		       ,{field :"Monto", 					title:'UI_MONTO', 					width:"90px", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }		       
		       ,{field :"IdComprobante", 			title:"UI_SECUENCIA_COMPROBANTE", 	width:"70px", template: '<label> #:  util.formateaNumero(IdComprobante,0) #</label>' }
				
               ,{field :"NombreConceptoDeposito", 	title:"UI_MOTIVO_ANULACION",		width:"100px"}
               ,{field :"Descripcion", 				title:"UI_COMENTARIO",				width:"150px"}

		       ,{field :"NombreEstado", 			title:"UI_ESTADO",					width:"80px"}
		       	
		        ];
	};
	
	
	
	return obj;
}]);