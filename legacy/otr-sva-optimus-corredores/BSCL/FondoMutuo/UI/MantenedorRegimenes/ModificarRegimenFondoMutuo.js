angular.module('ModuloFFM').controller('ModificarRegimenFondoMutuoCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,$timeout){
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.formData = {};
	
	$scope.formData.datosAntes = $scope.parametros;
	
	$scope.limpiar = function(){
		$scope.formData.datosDespues = angular.copy($scope.formData.datosAntes);
	}
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.modificarRegimen = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		regimen = [];
		$.each($scope.confRegimen.dataSource.data(),function(key,value) {
			regimen.push({IdRegimenFondoMutuo : value.IdRegimenFondoMutuoDestino});
		});
		proposito = [];
		$.each($scope.confPropositoPortafolio.dataSource.data(),function(key,value) {
			proposito.push({IdPropositoPortafolio : value.IdPropositoPortafolio});
		});
		if ($scope.formData.datosDespues.HomologacionNCG365 === ""){
			$scope.formData.datosDespues.HomologacionNCG365 = $scope.formData.datosDespues.IdRegimenFondoMutuo
		}
		var datosModificarFondoMutuo = {
				p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo : 'FMM',
				p_UsaReglasNegocio : 'S',
				p_RegimenFondoMutuo : [{
					IdInterno					:	0,
					IdRegimenFondoMutuo			:	$scope.formData.datosDespues.IdRegimenFondoMutuo,
					Descripcion					:	$scope.formData.datosDespues.Descripcion,
					PropositoSerieRelacionado	:	$scope.formData.datosDespues.PropositoSerieRelacionado,
					PermiteOtrosGrupos			:	'S',
					ValidaPortafolioCliente		:	$scope.formData.datosDespues.ValidaPortafolioCliente,
					ValidaPortafolioAGF			:	$scope.formData.datosDespues.ValidaPortafolioAGF,
					PermiteRescate				:	$scope.formData.datosDespues.PermiteRescate,
					PermiteAporte				:	$scope.formData.datosDespues.PermiteAporte,
					PermiteTraspaso				:	$scope.formData.datosDespues.PermiteTraspaso,
					ValidaMontoMinimoInstrumento: 	$scope.formData.datosDespues.ValidaMontoMinimoInstrumento,
					PermiteCambioGrupo			:	'S',
					RescateConMandato			:	$scope.formData.datosDespues.RescateConMandato,
					AceptaMandatoEnReinversion	:	$scope.formData.datosDespues.AceptaMandatoEnReinversion,
					IdEstado					:	$scope.formData.datosDespues.IdEstado,
					CertificaSinAsociado        :   $scope.formData.datosDespues.CertificaSinAsociado,
					HomologacionNCG365			:	$scope.formData.datosDespues.HomologacionNCG365,
				}],
				p_Regimen : regimen,
				p_PropositoPortafolio : proposito,
		};
		bcsServicioFondosMutuos.ModificarRegimenFondoMutuo(datosModificarFondoMutuo).$promise.then(function(respuesta){
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_REGIMEN_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsConfiguracionFFM.ingresarCerrarVentana(id, $scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_REGIMEN_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
 		 },function(respuesta){
 		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_REGIMEN_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
 		 });
	}
	
	$scope.getSchemaConfiguracionRegimen = function(){
		return {
			model : {
				fields : {
					IdRegimenFondoMutuoDestino : { type : "string"},
					DescripcionRegimenFondoMutuoDestino	: { type : "string"}
				}
			}
		}
	};
	
	$scope.getDataSourceConfiguracionRegimen = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				schema : $scope.getSchemaConfiguracionRegimen()			
			}
		})
	};
	
	$scope.getConfiguracionRegimen = function(){
		var config = {};
		config = {
				options : {
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: false,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: true,
		      		scrollable: true,
		      		height:150,
	
					dataSource: $scope.getDataSourceConfiguracionRegimen(),
				
					columns: [
							   bcsConfiguracionFFM.getConfiguracionColumna("IdRegimenFondoMutuoDestino")
					],
					gridCreated : function(grid){
						$scope.ajustaVentana();
					}
				}
		};
		return config;
	};
	$scope.limpiar();
	var dataSourceRegimen = $scope.getConfiguracionRegimen();
	$scope.confRegimen = dataSourceRegimen.options;
	
	$scope.ConsultaConfigRegimen = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=601109",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdRegimenFondoMutuoOrigen 	  		 		: { type : "string"},
						IdRegimenFondoMutuoDestino 	  	  		 	: { type : "string"},
						DescripcionRegimenFondoMutuoOrigen   		: { type : "string"},
						DescripcionRegimenFondoMutuoDestino   	  	: { type : "string"},				
					}
				}
			}
		},
	});	
	
	
	$scope.ajustaVentana();
	
	/* */
	
	$scope.getSchemaConfiguracionPropositoPortafolio = function(){
		return {
			model : {
				fields : {
					IdRegimenFondoMutuo : { type : "string"},
					IdPropositoPortafolio	: { type : "string"}
				}
			}
		}
	};
	
	$scope.getDataSourcePropositoPortafolio = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				schema : $scope.getSchemaConfiguracionPropositoPortafolio()			
			}
		})
	};
	
	$scope.getConfiguracionPropositoPortafolio = function(){
		var config = {};
		config = {
				options : {
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: false,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: true,
		      		scrollable: true,
		      		height:150,
	
					dataSource: $scope.getDataSourcePropositoPortafolio(),
				
					columns: [
							   bcsConfiguracionFFM.getConfiguracionColumna("IdPropositoDestino")
					],
					gridCreated : function(grid){
						$scope.ajustaVentana();
					}
				}
		};
		return config;
	};
	
	var dataSourcePropositoPortafolio = $scope.getConfiguracionPropositoPortafolio();
	$scope.confPropositoPortafolio = dataSourcePropositoPortafolio.options;
	
	$scope.ConsultaConfigPropositoPortafolio = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=601110",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdRegimenFondoMutuo 	  		 				: { type : "string"},
						IdPropositoPortafolio 	  	  		 			: { type : "string"},
						DescripcionPropositoSerieRelacionadoOrigen   	: { type : "string"},
						DescripcionPropositoSerieRelacionadoDestino   	: { type : "string"},				
					}
				}
			}
		},
	});	
	
	$scope.ConsultaConfigPropositoPortafolio.query({filter:{field: "IdRegimenFondoMutuo", operator: "eq", value: $scope.formData.datosAntes.IdRegimenFondoMutuo}}).then(function(){
		if($scope.ConsultaConfigPropositoPortafolio.view().length > 0) {
			proposito = Array();
			for(var i=0; i < $scope.ConsultaConfigPropositoPortafolio.view().length ; i++){
				proposito.push($scope.ConsultaConfigPropositoPortafolio.view()[i]);	
			}
			$scope.confPropositoPortafolio.dataSource.data(proposito);
			
		}
	});
	
	$scope.ConsultaConfigRegimen.query({filter:{field: "IdRegimenFondoMutuoOrigen", operator: "eq", value: $scope.formData.datosAntes.IdRegimenFondoMutuo}}).then(function(){
		if($scope.ConsultaConfigRegimen.view().length > 0) {
			regimen = Array();
			for(var i=0; i < $scope.ConsultaConfigRegimen.view().length ; i++){
				regimen.push($scope.ConsultaConfigRegimen.view()[i]);	
			}
			$scope.confRegimen.dataSource.data(regimen);
			
		}
	});
	
	$scope.ajustaVentana();
	
	$scope.agregarProposito = function(){
		if($scope.formData.datosDespues.PropositoSeleccionado){
			var proposito = undefined;
			$.each($scope.confPropositoPortafolio.dataSource.data(),function(key,value) {
				if(value.IdPropositoPortafolio === $scope.formData.datosDespues.PropositoSeleccionado.IdPropositoPortafolio) proposito = value.IdPropositoPortafolio
			});
			if (!proposito && $scope.formData.datosDespues.PropositoSeleccionado.IdPropositoPortafolio)
				$scope.confPropositoPortafolio.dataSource.add({IdPropositoPortafolio : $scope.formData.datosDespues.PropositoSeleccionado.IdPropositoPortafolio})
		}
		
	};
	
	$scope.quitarProposito = function(){
			$scope.confPropositoPortafolio.dataSource.remove($scope.datos.propositoPortafoliokNgModel);
	};
	
	$scope.agregarRegimen = function(){
		if($scope.formData.datosDespues.RegimenSeleccionado){
			var regimen = undefined;
			$.each($scope.confRegimen.dataSource.data(),function(key,value) {
				if(value.IdRegimenFondoMutuoDestino === $scope.formData.datosDespues.RegimenSeleccionado.IdRegimenFondoMutuo) regimen = value.IdRegimenFondoMutuoDestino
			});
			if (!regimen && $scope.formData.datosDespues.RegimenSeleccionado.IdRegimenFondoMutuo)
				$scope.confRegimen.dataSource.add({IdRegimenFondoMutuoDestino : $scope.formData.datosDespues.RegimenSeleccionado.IdRegimenFondoMutuo})
		}
	};
	
	$scope.quitarRegimen = function(){
			$scope.confRegimen.dataSource.remove($scope.datos.kNgModel);
	};
	
	$scope.limpiar();
});