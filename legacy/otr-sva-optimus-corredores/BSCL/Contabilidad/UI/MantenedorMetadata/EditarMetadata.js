angular.module('ModuloContabilidad').controller('EditarMetadataContabilidadCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource,bcsServicioContabilidad,bcsMantenedorEventosGrid) {
	
	$scope.data = {};
	$scope.formInfo = {};
	$scope.inputs = {};
	$scope.data.condicion = '=';
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(idVentana);
	
	var parametros = $scope.parametros;
	$scope.data.valorMetadata = parametros.Metadata.CodigoMetadata;
	$scope.data.descripcionMetadata = parametros.Metadata.DescripcionMetadata;
	
	var conf = bcsMantenedorEventosGrid.getDataSourceMetadataFiltros();
	var arrCuentas = [];
	
	conf.query({filter:{field: "IdMetadataCuentaContable", operator: "eq", value: parametros.Metadata.IdMetadataCuentaContable}}).then(function(){
		if(conf.data().length > 0){
			var datos = conf.data();
			$.each(datos,function(key,value){
				arrCuentas.push({
					IdFiltro : arrCuentas.length,
					CuentaOptimus :  value.CuentaOptimus,
					IdTipoDatoEvento : value.IdTipoDatoEvento,
					Condicion : value.Condicion,
					Valor : value.Valor
				});
			});
			$scope.tbcuentas.confDatos.dataSource.data(arrCuentas);
		}
	});
	
	
	$scope.oValor = {
			filtros : {
				IdTipoDatoEvento :  { "field": "IdTipoDatoEvento","operator": "eq", "value"   :  undefined }
			}
	}
	
	var dsPreCuentasFiltros = new bcs.DataSource({
		type : "array",
		definition : {							
			schema : bcsMantenedorEventosGrid.ModeloPreCuentasFiltros(),
		}
	});
	
	$scope.tbcuentas = {
			confDatos : {
				dataSource			: dsPreCuentasFiltros,
				columns				: bcsMantenedorEventosGrid.columnasPreCuentasFiltros(),		
				filterable			: false,
				autoBind			: true,  //comentar
				height				: 100,
			    selectable 			: "row"
			}
	}
	
	
	
	$scope.formInfo.agregarMetadataContabilidad = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		arrCuentas.push({
			IdFiltro : arrCuentas.length,
			CuentaOptimus :  $scope.data.numeroCuentaActual,
			IdTipoDatoEvento : $scope.data.eventoTipoDato,
			Condicion : $scope.data.condicion,
			Valor : $scope.data.selecionadoEventoTipoDato.DatosPreDefinidos === 'S' ? $scope.data.eventoValorTipoDato : $scope.data.valorTexto
		});
		$scope.tbcuentas.confDatos.dataSource.data(arrCuentas);
		bcsComunes.ventanaOverlay(idVentana,false);
	}
	
	$scope.formInfo.eliminarMetadataContabilidad = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		if ($scope.grid.seleccionadoPreCuentasFiltros === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			arrCuentas = arrCuentas.filter(function(item){
				return item.IdFiltro !== $scope.grid.seleccionadoPreCuentasFiltros.IdFiltro
			})
			$scope.tbcuentas.confDatos.dataSource.data(arrCuentas);
		};
		
		bcsComunes.ventanaOverlay(idVentana,false);
		
	}
	
	$scope.formInfo.cargarCuentasMetadata = function(){
		var datosArchivo = {codigoArchivo: 'CARGA_CUENTAS_METADATA', titulo: 'Carga Cuentas Metadata'};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
		bcsComunes.ventanaPromesa(id1).then(function(result){
			if(!result || !result.fileData){
				 bcsComunes.ventanaOverlay(idVentana,false);
				 return;
			}
			
			$.each(result.fileData,function(key,value){
				if(value.MENSAJEVALIDACION === 'OK'){
					arrCuentas.push({
						IdFiltro : arrCuentas.length,
						CuentaOptimus :  value.CCMD_NUMEROCUENTA,
						IdTipoDatoEvento : value.CCMD_TIPODATO,
						Condicion : value.CCMD_CONDICION,
						Valor : value.CCMD_VALOR
					});
				}
			}); 
			$scope.tbcuentas.confDatos.dataSource.data(arrCuentas);
			
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			console.log(result);
		})
	}
	
	$scope.formInfo.guardarMetadataContabilidad = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'CON',
				p_IdMetadataCuentaContable : parametros.Metadata.IdMetadataCuentaContable,
				p_DescripcionMetadata: $scope.data.descripcionMetadata,
				p_Filtros : []
		}
		for(var i=0;i<arrCuentas.length;i++){
			datos.p_Filtros.push({
				CuentaOptimus : arrCuentas[i].CuentaOptimus,
				IdTipoDatoEvento : arrCuentas[i].IdTipoDatoEvento,
				Condicion : arrCuentas[i].Condicion,
				Valor : arrCuentas[i].Valor
			})
		}
		bcsServicioContabilidad.EditarMetadata(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result ){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, idVentana);
						if(result.P_OUT_ERRORESCONTADOR === 0) {
							if(id){
								var promesa=bcsComunes.ventanaPromesa(id);                               
							    $.when(promesa).then(function(data){
							    	 $timeout(function() {
							    		 bcsComunes.ventanaCerrar(idVentana);
							    	 },20);
							     }); 
							}else{
								 bcsComunes.ventanaCerrar(idVentana); 
							 }
						}
					}

				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
				});
	}
	
	$scope.$watch('inputs.igualB',function(newVal){
		if(newVal){
			$scope.data.condicion = '=';
			$scope.inputs.distintoDisabledB = true;
			$scope.inputs.mayorEstrictoDisabledB = true;
			$scope.inputs.menorEstrictoDisabledB = true;
		}else{
			limipiarCheck();
		}
	});
	
	$scope.$watch('inputs.distintoB',function(newVal){
		if(newVal){
			$scope.data.condicion = '<>';
			$scope.inputs.igualDisabledB = true;
			$scope.inputs.mayorEstrictoDisabledB = true;
			$scope.inputs.menorEstrictoDisabledB = true;
		}else{
			limipiarCheck();
		}
	});
	
	$scope.$watch('inputs.mayorEstrictoB',function(newVal){
		if(newVal){
			$scope.data.condicion = '>';
			$scope.inputs.igualDisabledB = true;
			$scope.inputs.distintoDisabledB = true;
			$scope.inputs.menorEstrictoDisabledB = true;
		}else{
			limipiarCheck();
		}
	});
	
	$scope.$watch('inputs.menorEstrictoB',function(newVal){
		if(newVal){
			$scope.data.condicion = '<';
			$scope.inputs.igualDisabledB = true;
			$scope.inputs.mayorEstrictoDisabledB = true;
			$scope.inputs.distintoDisabledB = true;
		}else{
			limipiarCheck();
		}
	});
	
	var limipiarCheck = function(){
		$scope.inputs.igualDisabledB = false;
		$scope.inputs.distintoDisabledB = false;
		$scope.inputs.mayorEstrictoDisabledB = false;
		$scope.inputs.menorEstrictoDisabledB = false;
	}
	
})