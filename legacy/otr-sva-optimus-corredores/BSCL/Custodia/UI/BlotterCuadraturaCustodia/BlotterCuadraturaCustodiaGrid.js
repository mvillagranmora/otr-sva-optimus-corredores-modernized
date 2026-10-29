angular.module('ModuloCustodia').factory('bcsBlotterCuadraturaCustodiaGrid',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	
	modeloCuadraturaCustodia = function() {
		return {
    		model: {
    			fields: {
    				IdCuadraturaCustodia			: { type: "number"},
    				FechaCuadraturaCustodia			: { type: "date"},
    				IdClasificacionTipoInstrumento	: { type: "string"},
    				ClasificacionTipoInstrumento	: { type: "string"},
					IdTipoInstrumento				: { type: "string"},
					NombreTipoInstrumento			: { type: "string"},
					Nemotecnico						: { type: "string"},
    				TipoCuenta						: { type: "string"},
    				NombreTipoCuenta				: { type: "string"},
    				IdBoveda						: { type: "string"},
    				CuentaBoveda					: { type: "string"},
    				CantidadCustodia				: { type: "number"},
    				CantidadSimultaneaCompra		: { type: "number"},
    				CantidadSimultaneaVenta			: { type: "number"},
    				CantidadPrestamo				: { type: "number"},
    				CantidadEmprestito				: { type: "number"},
    				CantidadGarantia				: { type: "number"},
    				CantidadTransitoIngreso			: { type: "number"},
    				CantidadTransitoEgreso			: { type: "number"},
    				CantidadPactoVenta				: { type: "number"},
    				CantidadPactoCompra				: { type: "number"},
    				CantidadTotal					: { type: "number"},
    				MontoATasaMercadoActual			: { type: "number"},
    				PosicionBoveda					: { type: "number"},
    				PosicionBloqueada				: { type: "number"},
    				PosicionReventa					: { type: "number"},
    				PosicionRecompra				: { type: "number"},
    				PosicionTotal					: { type: "number"},
    				RutBeneficiario					: { type: "string"},
					NombreBeneficiario				: { type: "string"},
					Diferencia						: { type: "number"},
					ExplicacionDescuadratura		: { type: "string"},
    				FechaModificacion				: { type: "date"},
    				IdUsuario						: { type: "string"},
					IdEstado						: { type: "string"}
        		} 
        	}
		};
	};

	
	//columnas 
	obj.columnasCuadraturaCustodia = function() {
		return  [
			 {field: "IdBoveda", 						title: 'UI_BOVEDA',						width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "FechaCuadraturaCustodia",			title: 'UI_FECHA',						template : " <label> #:  util.formatoDate(FechaCuadraturaCustodia) #</label>" , 	width: "70px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "CuentaBoveda",					title: 'UI_CUENTA_BOVEDA',				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "NombreTipoCuenta",				title: 'UI_TIPO_CUENTA',				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "ClasificacionTipoInstrumento",	title: 'UI_CLASIFICACION',				width: "160px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}		         
			,{field: "Nemotecnico",						title: 'UI_NEMO', 						width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}		         			
			,{field: "CantidadCustodia", 				title: 'UI_SALDO_CUSTODIA', 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadGarantia", 				title: 'UI_GARANTIA',			 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadSimultaneaVenta",			title: 'UI_SIMULTANEA_VENTAS',			width: "110px", hidden: true, attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadSimultaneaCompra",		title: 'UI_SIMULTANEA_COMPRAS', 		width: "110px", hidden: true, attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadPrestamo",	 			title: 'UI_PRESTAMOS',		 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadEmprestito",	 			title: 'UI_EMPRESTITO',	 				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
/*			,{field: "SaldoPactoCompra", 				title: 'UI_PACTO_COMPRA',	 			width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "SaldoPactoVenta",	 				title: 'UI_PACTO_VENTA',	 			width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}*/
			,{field: "CantidadTransitoIngreso",			title: 'UI_INGRESO_TRANSITO', 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadTransitoEgreso",			title: 'UI_EGRESOS_TRANSITO', 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadPactoVenta",				title: 'UI_PACTO_VENTA', 				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadPactoCompra",				title: 'UI_PACTO_COMPRA', 				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "CantidadTotal",	 				title: 'UI_CANTIDAD_TOTAL',	 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "PosicionBoveda", 					title: 'UI_POSICION_BOVEDA', 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}			
			,{field: "PosicionBloqueada",				title: 'UI_POSICION_BLOQUEADA',			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "PosicionReventa",					title: 'UI_POSICION_REVENTA',			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "PosicionRecompra",				title: 'UI_POSICION_RECOMPRA',			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "PosicionTotal",					title: 'UI_POSICION_TOTAL',				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "RutBeneficiario",					title: 'UI_RUT_BENEFICIARIO',			width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "NombreBeneficiario",				title: 'UI_NOMBRE_BENEFICIARIO',		width: "250px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "Diferencia",						title: 'UI_DIFERENCIA',					width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "ExplicacionDescuadratura", 		title: 'UI_EXPLICACION_DESCUADRATURA', 	width: "250px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true, editable: true}
			,{field: "FechaModificacion",				title: 'UI_FECHA_MODIFICACION',			template : " <label> #:  util.formatoDate(FechaModificacion) #</label>" , 	width: "110px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "IdUsuario",						title: 'UI_USUARIO',					width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}			
			,{field: "IdEstado",						title: 'UI_ESTADO',						width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
		         ]
	};
	return obj;
}]);	








