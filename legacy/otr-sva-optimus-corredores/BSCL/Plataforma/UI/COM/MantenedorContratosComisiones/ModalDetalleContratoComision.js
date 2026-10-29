angular.module('ModuloPlataforma').controller('ModalDetalleContratoComisionCtrl',function ($scope, bcsComunes, $translate, servicioContratoComision) {

	const detalleContratoComision 						= this;
	const idVentana 									= bcsComunes.ventanaWinId($scope);
	detalleContratoComision.parametros 					= bcsComunes.ventanaParams(idVentana);
	detalleContratoComision.domVentana					= $("[ventanaid='"+idVentana+"']");
	detalleContratoComision.combobox 					= {};
	detalleContratoComision.dataOriginal				= angular.copy(detalleContratoComision.parametros);
	detalleContratoComision.data 						= {};
	detalleContratoComision.subdetalles					= [];
	detalleContratoComision.subdetallesEliminados 		= [];	
	detalleContratoComision.totalSubdetalles 			= 0;
	detalleContratoComision.puedeIngresarSubdetalles 	= true;	
	detalleContratoComision.totalSubdetallesPorRangos 	= 0;
	detalleContratoComision.puedeEditarDetalle 			= true;
	
	const filtros = [{field : "IdDetalleContratoComision", operator: "eq", value: detalleContratoComision.dataOriginal.IdDetalleContratoComision || null}];
	
	detalleContratoComision.configuracionGrillaSubdetalle = {
		dataSource: new bcs.DataSource({
			type: "odata",
			definition 	: {
				serverFiltering : true, 
				serverPaging 	: true,
				//serverSorting 	: true,
				sort 			: [ { field: "MontoMaximo", dir: "asc" } ],
				sortable 		: true,
				pageSize 		: 20,
                transport		: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700295",
						dataType	: "json"
				    }
                },
				schema: {
					model: {
						fields: {
							IdDetalleContratoComision		: { type: "number"},
							IdSubdetalleContratoComision	: { type: "number"},
							Monto							: { type: "number"},
							Porcentaje						: { type: "number"},
							MontoMaximo						: { type: "number"}
						}
					}
				},
				filter: filtros
			},
			
		}),
		columns: [
			{field: "MontoMaximo",	title: $translate.instant('UI_MONTO_MAXIMO'), width: "100px", align: "left", sortable : true, editable: (detalleContratoComision.dataOriginal.EsPorRangos == 'S') ? true : false , validation: { min: 0, required: true }, format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
			{field: "Porcentaje",	title: $translate.instant('UI_PORCENTAJE'), width: "100px", align: "left", sortable : true, editable: (detalleContratoComision.dataOriginal.IdCobro == 'COVA' || detalleContratoComision.dataOriginal.IdCobro == 'DEVA') ? true : false, format: function(value, item){ return util.bcs_formato(value,'TAS','Cantidad','CLP','',''); }},
			{field: "Monto",	title: $translate.instant('UI_MONTO'), width: "100px", align: "left", sortable : true, editable: (detalleContratoComision.dataOriginal.IdCobro == 'COFI' || detalleContratoComision.dataOriginal.IdCobro == 'DEFI') ? true : false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
		],
		sortable			: false,
		pageable			: true,
		autoBind			: true,
    	filterable			: false,
		height				: 150,
	    selectable 			: "row",
	    scrollable			: true,
	    userSettings		: true,
		navigatable			: true,
		dataBound			: function(){
				if(detalleContratoComision.puedeIngresarSubdetalles){
					detalleContratoComision.totalSubdetalles = detalleContratoComision.configuracionGrillaSubdetalle.dataSource.data().length;
					detalleContratoComision.puedeIngresarSubdetalles = false;
				}
				detalleContratoComision.totalSubdetallesPorRangos = detalleContratoComision.configuracionGrillaSubdetalle.dataSource.data().length;
			
			}
	};
	
	detalleContratoComision.combobox.CobroClasificacion = {	
		logic: "or",
		Comision : { "field"   : "CodigoCobroClasificacion","operator": "eq", "value"   : "COMISION" },
		Derecho : { "field"   : "CodigoCobroClasificacion","operator": "eq", "value"   : "DERECHO" }
		
	};
	
	detalleContratoComision.combobox.Concepto = {	
		tipo : "combobox-concepto-facturacion",
		configuracion : {},
		filtros : {
			IdEstado 		 	: {"field"   : "IdEstado", 		  "operator" : "eq", "value" :"VIG" },
			EsOtrosConceptos 	: {"field"   : "EsOtrosConceptos", "operator" : "eq", "value" :"S" },
			ConfiguradoEnModulo : {"field"   : "ConfiguradoEnModulo","operator": "eq", "value" : "S" }			
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
		};
			
	detalleContratoComision.Cobro = [
							{ "field"   : "IdCobro","operator": "eq", "value"   : "COFI" },
							{ "field"   : "IdCobro","operator": "eq", "value"   : "COVA" },
							{ "field"   : "IdCobro","operator": "eq", "value"   : "DEFI" },
							{ "field"   : "IdCobro","operator": "eq", "value"   : "DEVA" } ];
			
					
	detalleContratoComision.cargaDatosPantalla = function(){
	
		detalleContratoComision.data.EsPorRangos 		= (detalleContratoComision.dataOriginal.EsPorRangos === 'S') ? true : false,
		detalleContratoComision.data.IdContratoComision	= detalleContratoComision.dataOriginal.IdContratoComision;
		detalleContratoComision.data.MontoMinimo		= detalleContratoComision.dataOriginal.MontoMinimo || 0;
		detalleContratoComision.data.MontoMaximo		= detalleContratoComision.dataOriginal.MontoMaximo || 0;
		detalleContratoComision.data.formModo			= detalleContratoComision.dataOriginal.formModo;
		
		
	};		

	detalleContratoComision.aceptar = function () {
	
		if(!detalleContratoComision.esValidoMontoMinimo()){
			bcsComunes.msg("warning", "warning", $translate.instant('UI_MINIMO_MAYOR_MAXIMO'));
			return;
		}
		
		if(detalleContratoComision.parametros.IdDetalleContratoComision) { // EDITAR
		
			const datosGuardar = detalleContratoComision.preparaDatos();
			bcsComunes.ventanaOverlay(idVentana,true);
			servicioContratoComision.modificarDetalleContratoComision(datosGuardar,function (result){
				
				let titulo = (result.P_OUT_REFID.length  > 0 && result.P_OUT_ERRORESCONTADOR === 0) 
								? $translate.instant("UI_EDITAR_DETALLE_CONTRATO_EXITOSAMENTE")
								: $translate.instant("UI_ERROR_EDITAR_DETALLE_CONTRATO");
		
				let idModal = bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);

				bcsComunes.ventanaPromesa(idModal).then(function(){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);	
				});
				
			}, function(resError){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.msg("error","error",$translate.instant("UI_ERROR_EDITAR_DETALLE_CONTRATO"));
				console.log(resError);
			});
			
		}
		else { // CREAR
			const datosGuardar = detalleContratoComision.preparaDatos();
			
			bcsComunes.ventanaOverlay(idVentana,true);
			servicioContratoComision.crearDetalleContratoComision(datosGuardar,function (result){
				
				let titulo = (result.P_OUT_REFID.length  > 0 && result.P_OUT_ERRORESCONTADOR === 0) 
							? $translate.instant("UI_CREAR_DETALLE_CONTRATO_EXITOSAMENTE")
							: $translate.instant("UI_ERROR_CREAR_DETALLE_CONTRATO");
							
				let idModal = bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);

				bcsComunes.ventanaPromesa(idModal).then(function(){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);	
				});
					
			}, function(resError){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.msg("error","error",$translate.instant("UI_ERROR_CREAR_DETALLE_CONTRATO"));
				console.log(resError);
			});
		}
	};
	
	
	$scope.$watch('detalleContratoComision.data.IdCobroClasificacion', function(newval,oldval){
		if(newval){
			detalleContratoComision.IdCobroClasificacion = { "field" : "IdCobroClasificacion", "operator": "eq","value" : newval };
			detalleContratoComision.combobox.Cobro = {
			filtros : {
				complejo: [detalleContratoComision.IdCobroClasificacion,  detalleContratoComision.Cobro],
				logic: "and",
				filters: [
			        {
			            logic: "or",
			            filters: detalleContratoComision.Cobro
			        },
			        detalleContratoComision.IdCobroClasificacion
			        ]
				},
			};
		}	
	});
	
	detalleContratoComision.preparaDatos = function(){
	
		detalleContratoComision.subdetalles = [];
		
		detalleContratoComision.configuracionGrillaSubdetalle.dataSource.data().map(function(value) {
			detalleContratoComision.subdetalles.push({
												IdDetalleContratoComision		: value.IdDetalleContratoComision, 
												IdSubdetalleContratoComision	: value.IdSubdetalleContratoComision, 
												Monto 							: value.Monto,
												Porcentaje 						: value.Porcentaje,
												MontoMaximo						: value.MontoMaximo,
												Estado							: 'add'
												});
		});
		
		detalleContratoComision.subdetalles = detalleContratoComision.subdetalles.concat(detalleContratoComision.subdetallesEliminados);
		
		const datosGuardar = {
			p_IdEmpresa 						: $scope.SesionIdEmpresa,
			p_IdUsuario 						: $scope.SesionIdUsuario,
			p_IdModulo							: "PLA",
			p_UsaReglasNegocio					: "N",
			p_DetallesContratoComision 			: [{
													IdContratoComision				: detalleContratoComision.data.IdContratoComision,
													IdDetalleContratoComision		: detalleContratoComision.dataOriginal.IdDetalleContratoComision || null,
													IdCobroClasificacion			: detalleContratoComision.data.IdCobroClasificacion,
													IdCobro							: detalleContratoComision.data.IdCobro,
													EsPorRangos						: (detalleContratoComision.data.EsPorRangos) ? 'S' : 'N',
													IdConcepto						: detalleContratoComision.data.IdConcepto,
													MontoMinimo						: detalleContratoComision.data.MontoMinimo,
													MontoMaximo						: detalleContratoComision.data.MontoMaximo,
													SubdetallesContratoComision		: detalleContratoComision.subdetalles
													}],
			
			
		};
		return datosGuardar;
	};
	
	detalleContratoComision.agregarSubdetalle = function() {
		
		if(!detalleContratoComision.data.IdCobro) return;		
		detalleContratoComision.data.IdMoneda = detalleContratoComision.dataOriginal.IdMoneda;	
		
		
		if(detalleContratoComision.totalSubdetallesPorRangos > 0 && !detalleContratoComision.data.EsPorRangos){
			bcsComunes.msg("warning", "warning", $translate.instant('UI_ERROR_RANGO_AGREGAR_SUBDETALLE'));
			return;
		} 
		
		const detalle = {
			EsPorRangos: detalleContratoComision.data.EsPorRangos,
			IdMoneda:	 detalleContratoComision.data.IdMoneda,
			IdCobro:	 detalleContratoComision.data.IdCobro,
		
		}					
		const id = bcsComunes.ventanaAbrir("agregarModificarSubdetalleContratoComision",detalle, false, false, idVentana);  

		bcsComunes.ventanaPromesa(id).then(function(resp){
			if(Object.entries(resp).length){
				detalleContratoComision.puedeEditarDetalle = false;
				detalleContratoComision.configuracionGrillaSubdetalle.dataSource.add({
														IdDetalleContratoComision		: detalleContratoComision.dataOriginal.IdDetalleContratoComision || null, 
														IdSubdetalleContratoComision	: null, 
														Monto 							: resp.Monto,
														MontoMaximo						: resp.MontoMaximo,
														Porcentaje						: resp.Porcentaje,
														Estado							: 'add'
														});
			}
			
         });  
  	};
  	
  	detalleContratoComision.quitarSubdetalle = function() {
  		
  		if(detalleContratoComision.subdetalle.datos.kNgModel.IdSubdetalleContratoComision){
  			
  			const subdetalleEliminado = {
  							IdDetalleContratoComision		: detalleContratoComision.subdetalle.datos.kNgModel.IdDetalleContratoComision, 
							IdSubdetalleContratoComision	: detalleContratoComision.subdetalle.datos.kNgModel.IdSubdetalleContratoComision, 
							Monto 							: detalleContratoComision.subdetalle.datos.kNgModel.Monto,
							Porcentaje 						: detalleContratoComision.subdetalle.datos.kNgModel.Porcentaje,
							MontoMaximo						: detalleContratoComision.subdetalle.datos.kNgModel.MontoMaximo,
							Estado							: 'eliminado'
  			}
  			detalleContratoComision.subdetallesEliminados.push(subdetalleEliminado);
  		}

  		detalleContratoComision.configuracionGrillaSubdetalle.dataSource.remove(detalleContratoComision.subdetalle.datos.kNgModel);
  	}
  	
  	
  	detalleContratoComision.cancelar = function () {
		bcsComunes.ventanaData(idVentana, false);
		bcsComunes.ventanaCerrar(idVentana);
	};	
	
	
	$scope.$watchGroup(['detalleContratoComision.data.EsPorRangos','detalleContratoComision.data.IdCobro'],function(newval,oldval){
		if(newval){
			if(detalleContratoComision.data.formModo === 'add')detalleContratoComision.configuracionGrillaSubdetalle.dataSource.removeAll();
		}
	});	
	
	detalleContratoComision.esValidoMontoMinimo = function() {
		if(detalleContratoComision.data.MontoMaximo > 0) {
			if(detalleContratoComision.data.MontoMinimo > detalleContratoComision.data.MontoMaximo)	return false;  
		}
		return true;	   	
	}
	


	detalleContratoComision.cargaDatosPantalla();
			
			
});