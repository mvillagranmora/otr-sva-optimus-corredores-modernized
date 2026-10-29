angular.module('ModuloFFM').controller('MantenedorFondosIndustriaCtrl',function($scope,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos){
	
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	
	var conf = bcsConfiguracionFFM.getConsultaFondosIndustria();
	$scope.defaultOptions = conf.options;
	$scope.defaultOptions.dataSource.filter({ "field"   : "IdEstado","operator": "eq", "value"   :  "ACT"});
	$scope.buscarInstrumentosFMM = conf.buscar;	
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.activaBotones = $scope.datos.seleccionFondosIndustria==null?false:true;
	
	
	
	$scope.agregarFondo = function (){		
		
		let parametro={}
		parametro.accion='ADD'
		$scope.abrirVentana(parametro);
	}
		
	$scope.modificarFondo = function (){		
		
		let parametro={}
		parametro.accion='MOD'
		parametro.fondo=$scope.datos.seleccionFondosIndustria;
		$scope.abrirVentana(parametro);
	}

	$scope.eliminarFondo = function (){		
		
		
		let objFondo = {
					p_IdEmpresa:$scope.SesionIdEmpresa,
					p_IdUsuario:$scope.SesionIdUsuario,
					p_IdModulo:'FMM',
					p_Accion:'MOD',
					p_FondosIndustria:[
						{
							IdFondoIndustria: $scope.datos.seleccionFondosIndustria.IdFondoIndustria,
							RunFFMM:$scope.datos.seleccionFondosIndustria.RunFFMM,
							NombreFFMM:$scope.datos.seleccionFondosIndustria.NombreFFMM,
							SociedadAdmFFMM:$scope.datos.seleccionFondosIndustria.SociedadAdmFFMM,
							RutSociedadAdmFFMM:$scope.datos.seleccionFondosIndustria.RutSociedadAdmFFMM,
							IdEstado:'INA',
							IdMoneda: $scope.datos.seleccionFondosIndustria.IdMoneda
						}
					]
				};
				
		bcsComunes.ventanaOverlay($scope.winID,true);	
		bcsServicioFondosMutuos.ActualizaFondosIndustria(objFondo).$promise.then(function(respuesta){
		    var mensajeCarga = new Array();	
	 	 	if(respuesta.P_OUT_MENSAJES.length > 0){
				
				$.each(respuesta.P_OUT_MENSAJES,function(key,value){
					 var objCarga = {IdInterno : value.id,tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,codigo : value.codigo,descripcion : value.descripcion};
					 mensajeCarga.push(objCarga);
				});
			}
			$scope.refrescarGrilla();						
			bcsComunes.ventanaOverlay($scope.winID,false);	
			bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeCarga} ,false,false,$scope.winID);
	 	});
	}
	
	$scope.refrescarGrilla =function (){
		$scope.defaultOptions.dataSource.filter({ "field"   : "IdEstado","operator": "eq", "value"   :  "ACT"});
	}
	
	$scope.abrirVentana = function (parametro){				
		
		let id = bcsComunes.ventanaAbrir("AgregarModificarFondoIndustria",parametro,false,false,$scope.winID);
		let promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){
			$scope.refrescarGrilla();
        }); 		
	}
	
	$scope.$watch('datos.seleccionFondosIndustria',function(newval,oldval){
		$scope.activaBotones = $scope.datos.seleccionFondosIndustria==null?false:true;		
	},true);


	
	
});
