angular.module('ModuloPlataforma').controller('AperturaCierreModulosCtrl',function($scope,$translate,bcsComunes,bcsAlerta,bcsConfiguracionPauta,bcsServicioPlataforma,OPCIONES_BCSALERTA,$timeout){
	
	$scope.datosModulo = {};
	$scope.datosProceso = {};
	$scope.filtrosModulo = {};
	var confModulo = bcsConfiguracionPauta.getConsultaModuloDetalle();
	var apiGridModulo = confModulo.options.dataSource;
	$scope.defaultOptionsModulo = confModulo.options;
	$scope.buscarModulos = confModulo.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	var confControl = bcsConfiguracionPauta.getConsultaControlAperturaCierre();
	var dsProcesos = confControl.options.dataSource;
	
		var ConsultaEstadoMesa = new bcs.newDataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering: true,
			transport: {
				read: {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1880",
					dataType : "json",
				}
			},
			schema: {
				model: {
					fields: {
						Codigo: {type: "string"},
						IdEstadoMesa: {type: "string"}		
					}
				}
			}
		},
	});
	
	
	$scope.defaultOptionsProcesos = confControl.options;
	
	
	
	$scope.datosProceso.FechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.actualizarGrillaProcesos = function(datos){
		$scope.filtrosProceso.CodigoModulo = datos[0].CodigoModulo;
		$scope.buscarProcesos();
	}
	
	$scope.abrirVentanaPauta = function()
	{
		bcsComunes.ventanaAbrir("MantenedorPauta", {} );
	};
	
	$scope.buscarProcesos = function(){
		dsProcesos.setCallParameters($scope.preparaParametros());
		//dsProcesos.read();
	}
	
	$scope.preparaParametros = function(){
		parametrosConsulta = {};
		parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
		parametrosConsulta.p_IdModulo = "PLA";
		parametrosConsulta.p_FechaSistema = util.dateSPdate($scope.datosProceso.FechaSistema);
		parametrosConsulta.p_CodigoModulo = $scope.filtrosProceso.CodigoModulo == 0 ? '' : $scope.filtrosProceso.CodigoModulo;
		parametrosConsulta.p_TipoProceso = $scope.filtrosProceso.TipoProceso == 0 ? '' : $scope.filtrosProceso.TipoProceso;
		console.log(parametrosConsulta);
		return parametrosConsulta;
	};
	
	$scope.buscarEstadoSistema = function() {
		
		var filterTemplate = [ { field: "CodigoModulo", operator:"eq", value: 'SIS' },
		                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa }];
		bcsConfiguracionPauta.ConsultaEstadoSistema.query({ filter: filterTemplate }).then(function(){
			if(bcsConfiguracionPauta.ConsultaEstadoSistema.view().length > 0) {
				$scope.datosModulo.EstadoSistema	= bcsConfiguracionPauta.ConsultaEstadoSistema.view()[0].NombreEstado;
			}
		});
    };
    
    $scope.buscarEstadoSistema();
    
    $scope.entregarSistema = function()
	{
    	bcsComunes.ventanaOverlay($scope.winID,true);
		var p = bcsComunes.ventanaAbrir("EntregarSistema", {} );
		var promesa=bcsComunes.ventanaPromesa(p);   
		$.when(promesa).then(function(data){
			bcsComunes.ventanaOverlay($scope.winID,false);
        }); 
	};
	
	$scope.abrirModulo = function(modulos) {	
		if (!modulos || modulos.length == 0) return;
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
			    p_IdUsuario 		: $scope.SesionIdUsuario,
			    p_IdModulo       	: 'PLA',
				p_MesaAbierta		: 'N',
				p_Modulo : [ {
					CodigoModulo 			: modulos[0].CodigoModulo,
					Estado					: 'ABT',
					CodigoUsuario			: $scope.$root.SesionUsuario,
				}]
		};
		var filterTemplate	= [{ field: "Codigo", operator: "eq", value: 'MESA_SPOT' }];
		ConsultaEstadoMesa.query({ filter: filterTemplate }).then(function(){
		if(ConsultaEstadoMesa.view().length > 0 && ConsultaEstadoMesa.view()[0].IdEstadoMesa !='C' && modulos[0].CodigoModulo=='MSP') {
			var id1 = bcsComunes.ventanaAbrir("AbrirModuloMesa",{CodigoModulo: modulos[0].CodigoModulo});
			var promesa=bcsComunes.ventanaPromesa(id1); 
			$.when(promesa).then(function(data){
				bcsComunes.ventanaOverlay($scope.winID,false);
				$scope.buscarModulos($scope.apiGridModulo,$scope.filtrosModulo);
			});
		}
		else{
			bcsComunes.ventanaOverlay($scope.winID,true);
			bcsServicioPlataforma.Servicios().AbrirModulo(datos).$promise.then(function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			if (result.P_OUT_ERRORESCONTADOR === 0) {
				var p = bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_REAPERTURA_MODULO_OK").replace("::NombreModulo",result.P_OUT_RESULTADO[0].NombreModulo),
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(p);   
				$.when(promesa).then(function(data){
					$scope.buscarModulos($scope.apiGridModulo,$scope.filtrosModulo)
		        }); 
			} else {
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
						mensaje:"UI_ERROR_REAPERTURA_MODULO",
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
			}
			},function(result){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error", "UI_ERROR", "UI_ERROR_REAPERTURA_MODULO");
			});}
			});

	}
	
	$scope.cerrarModulo = function(modulos) {	
		if (!modulos || modulos.length == 0) return;
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
			    p_IdUsuario 		: $scope.SesionIdUsuario,
			    p_IdModulo       	: 'PLA',
			    p_simularOperacion	: 'N',
				p_UsaReglasNegocio	: 'S',
				p_Modulo : [ {
					CodigoModulo 			: modulos[0].CodigoModulo,
					Estado					: 'CER',
					CodigoUsuario			: $scope.$root.SesionUsuario,
				}]
		};
		bcsComunes.ventanaOverlay($scope.winID,true);
		bcsServicioPlataforma.Servicios().CerrarModulo(datos).$promise.then(function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			if (result.P_OUT_ERRORESCONTADOR === 0) {
				var p = bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_CERRAR_MODULO_OK").replace("::NombreModulo",result.P_OUT_RESULTADO[0].NombreModulo),
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(p);   
				$.when(promesa).then(function(data){
					$scope.buscarModulos($scope.apiGridModulo,$scope.filtrosModulo)
		        }); 

			} else {
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
						mensaje:"UI_ERROR_CERRAR_MODULO",
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
			}
		},function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.msg("error","UI_ERROR","UI_ERROR_CERRAR_MODULO");
		});
	}
});