(function() {
	angular.module('ModuloAPV').controller('ClienteContratoSAAPVCtrl',
	function($scope,$translate,$resource,bcsComunes,bcsServiciosAPV,bcsConsultasAPV,bcsAlerta,OPCIONES_BCSALERTA) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var ccs = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		ccs.oConfiguracionCliente = {
			filtros : {
				IdEstado : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
				EsCliente : {field: "EsCliente", operator: "eq", value: 'UI_SI'},
				Tipo : {field: "CodigoTipoPersona",	operator: "eq", value: "N"}
			}
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var dsClienteContrato = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				//sort 			: { field: "IdCliente", dir: "DESC" },
				pageSize        : 250,
				transport		:
				{
					read:
					{
						url 		: bcsConsultasAPV('ConsultarClientePortafolioSAAPV'),
						dataType	: "json"
					}
				},
				filter: [
					{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }
				],  
				schema : {
					model : {
						fields : {
							IdEmpresa		: {type : "number"},
			    			IdPersona		: {type : "number"},
			    			IdCliente	    : {type : "number"},
			    			RutCliente		: {type : "string"},
			    			NombrePersona	: {type : "string"},
			    			IdEstadoAutorizacion : {type : "string"},
			    			EstadoAutorizacion	: {type : "string"}
			    		}
					}
				}
			},
		});
		
		/*********************************************
		 * GRILLAS
		 ********************************************/
		ccs.configuracionGrillaClienteContrato = {
			dataSource: dsClienteContrato,
			columns: [
				 {field: "IdEmpresa", hidden: true}
			    ,{field: "IdPersona",			title: "UI_ID_PERSONA",				width: "100px", hidden: true}
				,{field: "IdCliente",			title: "UI_ID_CLIENTE",				width: "180px", hidden: true}
				,{field: "RutCliente",			title: "UI_DOCUMENTO_IDENTIDAD", 	width: "100px"}
				,{field: "NombrePersona",		title: "UI_NOMBRE_PERSONA",			width: "180px" }
				,{field: "IdEstadoAutorizacion",	hidden: true}
				,{field: "EstadoAutorizacion",	title: "UI_ESTADO_AUTORIZACION",	width: "80px"}
			],
			sortable			: true,
			pageable			: true,
			filterable			: true,
			navigatable 		: true,
			userSettings 		: true,
			excel 				: { allPages: true },
			height				: 560,
			selectable 			: "multiple row",
			autoBind 			: true
		};
		
	
		ccs.configuracionGrillaContrato = function(dataItem) {	
			var dsContrato = new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging    : true, // Paginado de los datos en el servidor.
					serverSorting   : true, // Ordenamiento de los datos en el servidor.
					//sort 			: { field: "IdCliente", dir: "DESC" },
					pageSize        : 5,
					transport		:
					{
						read:
						{
							url 		: bcsConsultasAPV('ConsultarClienteContratoPortafolioSAAPV'),
							dataType	: "json"
						}
					},
					filter: [
					    { field: "IdCliente", operator: "eq", value: dataItem.IdCliente },
					],
				    schema : {
				    	model : {
				    		fields : {
				    			IdCliente					: {type: "number"},
				    			IdPortafolio				: {type: "number"},	
				    			CodigoPortafolio			: {type: "string"},			    			
				    			RegimenTributario			: {type: "string"},
				    			IdContratoSAAPV				: {type: "number"},
								FolioContratoSAAPV			: {type: "string"},
								Folio						: {type: "string"},
								NombrePlan					: {type: "string"},
								RutEmpleador				: {type: "string"},
								NombreEmpleador				: {type: "string"},
								NombreEncargado				: {type: "string"},
								FechaSuscripcion 			: {type: "sysdate"},
								FechaCreacion 				: {type: "sysdate"},
								FechaAprobacion 			: {type: "sysdate"},
								FechaNotificacion 			: {type: "sysdate"},
								FechaRecepcion 				: {type: "sysdate"},
								OrigenAhorro				: {type: "string"},
								MontoPesos					: {type: "number"},
								MontoUF						: {type: "number"},
								PorcentajeRegimenImponible	: {type: "number"},
								Pago						: {type: "string"},
								PeriodoInicio				: {type: "string"},
								PeriodoTermino				: {type: "string"},					
								FormaDePago					: {type: "string"},
								NombreMedioRecaudacion		: {type: "string"},
								DiaDescuento				: {type: "number"},
								Institucion					: {type: "string"},
								ComisionTransferencia		: {type: "number"},
								CodigoTipoSolicitud			: {type: "string"},
								RutEntidadOrigen			: {type: "string"},
								NombreEntidadOrigen			: {type: "string"},
								TipoDeAhorro				: {type: "string"},
								FondoDeOrigen				: {type: "string"},
								TipoTraspaso				: {type: "string"},
								MontoTraspaso				: {type: "number"},
								UFTraspaso					: {type: "number"},
								PorcentajeTraspaso			: {type: "number"},
								Estado						: {type: "string"},
								EstadoNotificacion			: {type: "string"},
								EstadoPAC					: {type: "string"}
				    		}
				    	}
				    }
				}
		    });
			
			return {
				dataSource: dsContrato,
				columns: [
					 {field: "IdCliente"				, hidden: true}
	    			,{field: "IdPortafolio"				, title: "UI_ID_PORTAFOLIO"			, width: "80px"}
	    			,{field: "CodigoPortafolio"			, title: "UI_CODIGO_PORTAFOLIO"		, width: "100px"}
	    			,{field: "RegimenTributario"		, title: "UI_REGIMEN_TRIBUTARIO"	, width: "100px"}
	    			,{field: "IdContratoSAAPV"			, title: "UI_ID_CONTRATO_SAAPV"		, width: "100px"}
					,{field: "FolioContratoSAAPV"		, title: "UI_FOLIO_CONTRATO"		, width: "80px"}
					,{field: "Folio"					, title: "UI_FOLIO"					, width: "90px"}
					,{field: "NombrePlan"				, title: "UI_NOMBRE_PLAN"			, width: "80px"}
					,{field: "RutEmpleador"				, title: "UI_RUT_EMPLEADOR"			, width: "90px"}
					,{field: "NombreEmpleador"			, title: "UI_NOMBRE_EMPLEADOR"		, width: "150px"}
					,{field: "NombreEncargado"			, title: "UI_NOMBRE_ENCARGADO"		, width: "150px"}
					,{field: "FechaSuscripcion"			, title: "UI_FECHA_SUSCRIPCION"		, width: "80px"}
					,{field: "FechaCreacion"			, title: "UI_FECHA_CREACION"		, width: "80px"}
					,{field: "FechaAprobacion"			, title: "UI_FECHA_APROBACION"		, width: "80px"}
					,{field: "FechaNotificacion"		, title: "UI_FECHA_NOTIFICACION"	, width: "80px"}
					,{field: "FechaRecepcion"			, title: "UI_FECHA_RECEPCION"		, width: "80px"}
					,{field: "OrigenAhorro"  			, title: "UI_ORIGEN_AHORRO"			, width: "80px"}
					,{field: "MontoPesos"	  	    	, title: "UI_MONTO_AHORRO"			, width: "90px",format: function(value, item){ return util.bcs_formato(value,'GEN','Monto',$scope.SesionIdMonedaSistema,'',''); }}
					,{field: "MontoUF"			 		, title: "UI_UF"					, width: "90px"} 
					,{field: "PorcentajeRegimenImponible", title: "UI_PORCENTAJE_REGIMEN"	, width: "90px"} 
					,{field: "Pago"						, title: "UI_PAGO"					, width: "80px"}
					,{field: "PeriodoInicio"			, title: "UI_PERIODO_INICIO"		, width: "80px"}
					,{field: "PeriodoTermino"			, title: "UI_PERIODO_TERMINO"		, width: "80px"}	
					,{field: "FormaDePago"				, title: "UI_FORMA_PAGO"			, width: "90px"}
					,{field: "NombreMedioRecaudacion"	, title: "UI_NOMBRE_MEDIO_RECAUDACION",width:"110px"}
					,{field: "DiaDescuento"				, title: "UI_DIA_DESCUENTO"			, width: "90px"}
					,{field: "Institucion"				, title: "UI_INSTITUCION"			, width: "110px"}
					,{field: "ComisionTransferencia"	, title: "UI_COMISION_TRANSFERENCIA", width: "110px"}
					,{field: "CodigoTipoSolicitud"		, title: "UI_TIPO_SOLICITUD"		, width: "80px"} 
					,{field: "RutEntidadOrigen"			, title: "UI_RUT_ENTIDAD_ORIGEN"	, width: "110px"}
					,{field: "NombreEntidadOrigen"		, title: "UI_NOMBRE_ENTIDAD_ORIGEN"	, width: "150px"}
					,{field: "TipoDeAhorro"				, title: "UI_TIPO_AHORRO"			, width: "80px"}
					,{field: "FondoDeOrigen"			, title: "UI_FONDO_ORIGEN"			, width: "80px"}
					,{field: "TipoTraspaso"				, title: "UI_TIPO_TRASPASO"			, width: "80px"}
					,{field: "MontoTraspaso"			, title: "UI_MONTO_TRASPASO"		, width: "90px", format: function(value, item){ return util.bcs_formato(value,'GEN','Monto',$scope.SesionIdMonedaSistema,'',''); }}
					,{field: "UFTraspaso"				, title: "UI_UF_TRASPASO"			, width: "90px"}
					,{field: "PorcentajeTraspaso"		, title: "UI_PORCENTAJE_TRASPASO"	, width: "90px"}
					,{field: "Estado"				, title: "UI_ESTADO_CONTRATO"				, width: "100px"}
					,{field: "EstadoNotificacion"		, title: "UI_ESTADO_NOTIFICACION"	, width: "100px"}
					,{field: "EstadoPAC"				, title: "UI_ESTADO_PAC"			, width: "100px"}
				],
				sortable	: false,
				pageable	: true,
				autoBind	: true,
				height		: 175,
			    selectable	: "multiple row"
			};
		};
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/		
		ccs.buscar = function() {
			var idCliente = ccs.dataCliente.seleccionado !== undefined && ccs.dataCliente.seleccionado.IdCliente ? ccs.dataCliente.seleccionado.IdCliente : null;
			
			if(idCliente === null){
				dsClienteContrato.filter({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
				return;
			}
			
			var filter = new Array();
			filter.push({ field: "IdCliente", operator: "eq", value: idCliente});
			dsClienteContrato.filter(filter);				
			ccs.seleccionadoClienteContrato = undefined;
			
		};
		
		ccs.limpiar = function() {
			ccs.dataCliente.idActual = false;
		};
		
		ccs.cambiarPlan = function() {
			if (!ccs.seleccionadoContrato) {
				bcsComunes.msg("error","Informacion","Debe seleccionar al menos un Portafolio.");
				return;
			}
			
			var vigentes = 0;
			$.each(ccs.seleccionadoClienteContrato,function(key,value){
				var estadoAutorizacion =  value.IdEstadoAutorizacion;
				if(estadoAutorizacion === 'VIG')
					vigentes++;
			});
			
			if (vigentes > 0) {
				bcsComunes.msg("error","Informacion","Hay clientes seleccionados con autorización para cambio de plan APV vigente.");
				return;
			}
			
			//var id = bcsComunes.ventanaAbrir("ModalClienteContratoSAAPV", {seleccionadoClienteContrato : ccs.seleccionadoClienteContrato}, false, true);//TODO ver que ventanas queden unidas
			var id = bcsComunes.ventanaAbrir("ModalClienteContratoSAAPV", {seleccionadoContrato : ccs.seleccionadoContrato});
			var promesa = bcsComunes.ventanaPromesa(id);
			promesa.then(function(respuesta){
	        	if(respuesta){
	        		ccs.seleccionadoContrato = undefined;
	        		ccs.buscar();
			}
	    	});
		};
		
	});
}());