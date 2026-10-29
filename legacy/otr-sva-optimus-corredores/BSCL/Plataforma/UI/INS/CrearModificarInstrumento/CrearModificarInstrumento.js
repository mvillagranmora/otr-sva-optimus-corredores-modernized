angular.module('ModuloPlataforma').controller('CrearModificarInstrumentoCtrl',function($scope,bcsComunes,bcsServicioPlataforma,$translate){

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.modo = 'INGRESAR';
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	
	$scope.formData = {};
	$scope.formData.filtros = {
		tipoInstrumento : {
			IdClasificacionTipoInstrumento : { field : "IdClasificacionTipoInstrumento", operator: "eq", value : "" }
		},
		valorClasificacionRiesgo : {
			IdTipoInstrumento : { field : "IdTipoInstrumento", operator: "eq", value : "" }
		},
		entidadClasificadoraRiesgo : {
			IdTipoInstrumento : { field : "IdTipoInstrumento", operator: "eq", value : "" }
		},

	};
	
	$scope.formData.agregarValorRiesgo = function() {
		var data = $scope.formData.apiValoresRiesgo.dataSource.data();
		var a = {};
		a.ValorClasificacionRiesgo = $scope.formData.valorClasificacionRiesgoSeleccionada.ClasificacionRiesgo;
		a.IdEntidadClasificadoraRiesgo  = $scope.formData.entidadClasificadoraRiesgoSeleccionada.IdEntidadClasificadoraRiesgo;
		
		var found = false;
		$.each(data,function(key,value){
			if (a.IdEntidadClasificadoraRiesgo === value.IdEntidadClasificadoraRiesgo) {
				found = true;
			}
		});
		
		if (found) {
			bcsComunes.msg("error","UI_ERROR",$translate.instant("UI_VALOR_YA_EXISTE"));
		}
		else {
			$scope.formData.apiValoresRiesgo.dataSource.add(a);
			$scope.formData.apiValoresRiesgo.refresh();
		}
		
	};
	
	$scope.formData.eliminarValorRiesgo = function() {
		
		$scope.formData.apiValoresRiesgo.dataSource.remove($scope.formData.valorSeleccionado);
		$scope.formData.apiValoresRiesgo.refresh();
		$scope.formData.valorSeleccionado = false;
		
	};

	$scope.instrumento = {};
	$scope.instrumento.valoresRiesgo = [];
	$scope.instrumentoDef = {};
	if (parametros.instrumento) {
		bcsComunes.ventanaTitulo($scope.idVentana, $translate.instant("UI_MODIFICAR_INSTRUMENTO"));
		$scope.modo = 'MODIFICAR';
		$scope.instrumentoDef = $.extend({}, parametros.instrumento);  
		$scope.instrumento = $.extend({}, parametros.instrumento);
		
		$scope.instrumento.EstaInscrito = $scope.instrumento.EstaInscrito === 'S'?true:false;		
	}
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.aceptar = function() {
		if ($scope.modo === 'INGRESAR') {

			var valores = $scope.formData.apiValoresRiesgo.dataSource.data();
			var p_valores = [];
			$.each(valores,function(key,value){
				var t = {
						IdInterno : 0,
						IdEntidadClasificadoraRiesgo : value.IdEntidadClasificadoraRiesgo,
						ValorClasificacionRiesgo : value.ValorClasificacionRiesgo,
				}
				p_valores.push(t);
				
			});
				
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
				    p_IdUsuario 		: $scope.SesionIdUsuario,
				    p_IdModulo       	: 'PLA',
				    p_simularOperacion	: 'N',
					p_UsaReglasNegocio	: 'N',
					p_Instrumentos : [ {
						IdInterno : 0,
						Nemotecnico: $scope.instrumento.Nemotecnico || null,
						ISIN: $scope.instrumento.ISIN || null,
						CorteMinimo: $scope.instrumento.CorteMinimo || null,
						EstaInscrito: $scope.instrumento.EstaInscrito?'S':'N',
						RazonSocial: $scope.instrumento.RazonSocial || null,
						FechaEmision: $scope.instrumento.FechaEmision? util.dateSPdate($scope.instrumento.FechaEmision) : null,
						FechaVencimiento: $scope.instrumento.FechaVencimiento? util.dateSPdate($scope.instrumento.FechaVencimiento) : null, 
						IdEmisor: $scope.instrumento.IdEmisor || null,
						IdTipoReajuste: $scope.instrumento.IdTipoReajuste || null,
						IdMoneda: $scope.instrumento.IdMoneda || null,
						IdTipoInstrumento: $scope.instrumento.IdTipoInstrumento || null,
						IdEstado: $scope.instrumento.IdEstado || null,
						Tamano: $scope.instrumento.Tamano || null,
						IdEntidadClasificadoraRiesgo: $scope.instrumento.IdEntidadClasificadoraRiesgo || null,
						ValorClasificacionRiesgo: $scope.instrumento.ValorClasificacionRiesgo || null,
						PrecioBase : $scope.instrumento.Precio || null,
						PrecioReferencia : $scope.instrumento.PrecioReferencia || null,
						FechaReferencia : $scope.instrumento.FechaReferencia? util.dateSPdate($scope.instrumento.FechaReferencia) : null
					}],
					p_ValoresRiesgo : p_valores
			};
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			bcsServicioPlataforma.Servicios().CrearInstrumento(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					var p = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_INSTRUMENTO_INGRESADO") + " : " + result.P_OUT_RESULTADO[0].Nemotecnico,
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
	
					bcsComunes.ventanaPromesa(p).then(function(){
	
						bcsComunes.ventanaData($scope.idVentana,{guardar : true});
						bcsComunes.ventanaCerrar($scope.idVentana);
					});
	
				} else {
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					bcsComunes.ventanaMsg({
							mensaje:"UI_ERROR_INGRESAR_INSTRUMENTO",
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.msg("error","UI_ERROR","UI_ERROR_INGRESAR_INSTRUMENTO");
			});
		}
		else if ($scope.modo === 'MODIFICAR') {
			var valores = $scope.formData.apiValoresRiesgo.dataSource.data();
			var p_valores = [];
			$.each(valores,function(key,value){
				var t = {
						IdInstrumento : $scope.instrumento.IdInstrumento,
						IdEntidadClasificadoraRiesgo : value.IdEntidadClasificadoraRiesgo,
						ValorClasificacionRiesgo : value.ValorClasificacionRiesgo,
				}
				p_valores.push(t);
				
			});
			
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
				    p_IdUsuario 		: $scope.SesionIdUsuario,
				    p_IdModulo       	: 'PLA',
				    p_simularOperacion	: 'N',
					p_UsaReglasNegocio	: 'N',
					p_Instrumentos : [ {
						IdInstrumento : $scope.instrumento.IdInstrumento,
						ISIN: $scope.instrumento.ISIN || null,
						CorteMinimo: $scope.instrumento.CorteMinimo || null,
						EstaInscrito: $scope.instrumento.EstaInscrito?'S':'N',
						RazonSocial: $scope.instrumento.RazonSocial || null,
						FechaEmision: $scope.instrumento.FechaEmision? util.dateSPdate($scope.instrumento.FechaEmision) : null,
						FechaVencimiento: $scope.instrumento.FechaVencimiento? util.dateSPdate($scope.instrumento.FechaVencimiento) : null, 
						IdEmisor: $scope.instrumento.IdEmisor || null,
						IdTipoReajuste: $scope.instrumento.IdTipoReajuste || null,
						IdMoneda: $scope.instrumento.IdMoneda || null,
						IdEstado: $scope.instrumento.IdEstado || null,
						Tamano: $scope.instrumento.Tamano || null,
						PrecioBase : $scope.instrumento.Precio || null,
						PrecioReferencia : $scope.instrumento.PrecioReferencia || null,
						FechaReferencia : util.dateSPdate($scope.instrumento.FechaReferencia) || null,
					}],
					p_ValoresRiesgo : p_valores
			};
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			bcsServicioPlataforma.Servicios().ModificarInstrumento(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					var p = bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_INSTRUMENTO_MODIFICADO"),
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
	
					bcsComunes.ventanaPromesa(p).then(function(){
	
						bcsComunes.ventanaData($scope.idVentana,{guardar : true});
						bcsComunes.ventanaCerrar($scope.idVentana);
					});
	
				} else {
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					bcsComunes.ventanaMsg({
							mensaje:"UI_ERROR_MODIFICAR_INSTRUMENTO",
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.msg("error","UI_ERROR","UI_ERROR_MODIFICAR_INSTRUMENTO");
			});
		}
	}
	
	//riesgo
	$scope.formData.apiValoresRiesgo = undefined;
	
	$scope.formData.configuracionValoresRiesgo = {
		gridCreated : function(e) {
			$scope.formData.apiValoresRiesgo = e;
			if ($scope.modo === 'MODIFICAR') {
				var datasss = [];
				var ds = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=3232",
								dataType: "json"
							}
						},
		    		    filter: { field: "IdInstrumento", operator: "eq", value: Number($scope.instrumento.IdInstrumento) },
		    		    schema: {
		    		    	model : {
		    		    		fields: {
		    		    			IdInstrumento			 	: {type : "number"},
		    		    			ValorClasificacionRiesgo 	: {type : "string"},
									IdEntidadClasificadoraRiesgo : {type : "string"}
		    		    		}
		    		    	}
		    		    }
		    		}});
				ds.fetch().then(function(){
					datasss = ds.data();
					e.dataSource.data(datasss);
				});
				
				
			}
		},
		autoBind: true,
		height : 150,
		selectable : "row",
		dataSource : new bcs.DataSource({
			type : "array",
			definition : {
				data : [],
				schema : {
					model : {
						fields : {	
							ValorClasificacionRiesgo 	: {type : "string"},
							IdEntidadClasificadoraRiesgo : {type : "string"}
						}
					}	
				}
			}
		}),
		columns : [
			{field: "IdEntidadClasificadoraRiesgo",		title: $translate.instant('UI_ENTIDAD_CLASIFICADORA'),	width: "100px"},
			{field: "ValorClasificacionRiesgo",			title: $translate.instant('UI_VALOR'),					width: "100px"},
			
		]
	};
	
});
