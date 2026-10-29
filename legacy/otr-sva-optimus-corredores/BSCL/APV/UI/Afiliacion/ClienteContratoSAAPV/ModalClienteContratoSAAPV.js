(function() {
	angular.module('ModuloAPV').controller('ModalClienteContratoSAAPVCtrl',
	function($scope,$translate,$resource,bcsComunes,bcsServiciosAPV,bcsConsultasAPV,bcsAlerta,OPCIONES_BCSALERTA) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var mccs = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		mccs.btnAceptarEnabled = true;
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var filters = new Array();
		var contratosSeleccionados = parametrosVentana.seleccionadoContrato;
		
		 angular.forEach(contratosSeleccionados, function (value, key) {	
			
			var IdPortafolio= value.IdPortafolio;
				
			filters.push({ field: "IdPortafolio", operator: "eq", value: IdPortafolio});
		});
		
		var dsClienteContrato = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				pageSize        : 50,
				transport		:
				{
					read:
					{
						url 		: bcsConsultasAPV('ConsultarClienteContratoPortafolioSAAPV'),
						dataType	: "json"
					}
				},
			    filter			: {
    				logic : 'or',
    				filters : filters
    			 },
				schema : {
					model : {
						fields : {
			    			IdCliente			: {type: "number"},
			    			IdPersona			: {type: "number"},
			    			RutCliente			: {type: "string"},
			    			NombrePersona		: {type: "string"},
			    			IdPortafolio		: {type: "number"},
			    			CodigoPortafolio	: {type: "string"},
			    			RegimenTributario	: {type: "string"},
			    			Producto			: {type: "string"},
			    			IdContratoSAAPV		: {type: "number"},
							FolioContratoSAAPV	: {type: "string"}
			    		}
					}
				}
			},
		});
		
		/*********************************************
		 * GRILLAS
		 ********************************************/
		mccs.configuracionGrillaClienteContrato = {
			dataSource: dsClienteContrato,
			columns: [
				 {field: "IdCliente",		hidden: true}
				,{field: "IdPersona",		hidden: true}
			    ,{field: "RutCliente",		title: "UI_DOCUMENTO_IDENTIDAD",width: "110px"}
				,{field: "NombrePersona",	title: "UI_NOMBRE_PERSONA",		width: "160px"}
				,{field: "IdPortafolio",	hidden: true}
				,{field: "RegimenTributario",	hidden: true}
				,{field: "CodigoPortafolio", title: "UI_CODIGO_PORTAFOLIO",	width: "100px"} 
				,{field: "Producto",		 title: "UI_PRODUCTO",	width: "100px"}
				,{field: "FolioContratoSAAPV", title: "UI_FOLIO_CONTRATO",	width: "90px"}
			],
			sortable			: false,
			pageable			: false,
			filterable			: false,
			navigatable 		: true,
			userSettings 		: false,
			height				: 500,
			autoBind 			: true
		};
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		mccs.Aceptar = function() {
			var contratosModal = dsClienteContrato.data();
						
			var t = bcsAlerta.crear("PREGUNTA","El cambio de los Portafolios seleccionados a Plan Activo es definitivo. " + $translate.instant("UI_CONTINUAR"));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					var contratoCambioPlan = [];
					$.each(contratosModal,function(key,value){
						var contrato = {
							"IdPersona"			: value.IdPersona,
							"IdCliente"			: value.IdCliente,
							"RutCliente"		: value.RutCliente,
							"IdPortafolio"		: value.IdPortafolio,
							"RegimenTributario"	: value.RegimenTributario,
							"IdContratoSAAPV"	: value.IdContratoSAAPV,
							"FolioContratoSAAPV": value.FolioContratoSAAPV
						};
						contratoCambioPlan.push(contrato);
					});
					
					var datosCambiarPlan = {
						p_IdEmpresa	: $scope.SesionIdEmpresa,
						p_IdUsuario	: $scope.SesionIdUsuario,
						p_IdModulo	: "APV",
						p_ContratoCambioPlanIn	: contratoCambioPlan
					};
			   		
					bcsComunes.ventanaOverlay(idVentana,true);
					bcsServiciosAPV.CambiarAPlanActivoContratoSAAPVWrapper(datosCambiarPlan).$promise.then(function(respuesta){
				   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				   		
				   		if(respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
				   			bcsComunes.ventanaMsg({
									mensaje:$translate.instant("Solicitud Pendiente de autorizacion."),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);
				   		}else{ 
				   			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("Portafolios cambiados correctamente a Plan Activo."),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);
							}
							else{
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("Existen errores al cambiar Plan de Portafolios."),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);
					    	}
					    }
						
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						
			   		},function(respuesta){
			   			bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						bcsComunes.msg("error","Error","Existen errores al cambiar Plan de Contratos.");
						bcsComunes.ventanaCerrar(idVentana);
			   		});
				}
				else return;
			});
		};
		
		mccs.Cancelar = function (){
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};	
	});
}());