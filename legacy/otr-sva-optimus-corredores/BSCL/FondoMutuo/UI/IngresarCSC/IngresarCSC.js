
angular.module('ModuloFFM').controller('IngresoCSCCtrl',function($scope,$translate,$timeout,bcsComunes,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionFFM){

    /************************************************
     * VARIABLES 
     ************************************************/
	var IngresoCSC = this; 
	$scope.formData = {};
	$scope.formData.Fecha  = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros = {};
	$scope.ingresarCSC = {};
	$scope.oClientePortafolio = {};
	$scope.oConfiguracionBanco = {};
	ingresarCSC = {};
	
	var estado_banco = 0;
	
	$scope.ingresarCSC.filtrosRegimen = {};
	var winID = bcsComunes.ventanaWinId($scope);
	
	IngresoCSC.params = bcsComunes.ventanaParams(winID);
	$scope.formData.matriz = {};
	$scope.formData.check = {};
	$scope.formData.checkDisable = {}
	$scope.formData.AceptoCondiciones = true;
	$scope.formData.AceptoMLT = false;
	$scope.formData.habilitarAgregar = true;
	$scope.formData.FirmaFuncionario = true;
	$scope.filMonUSD	= { "field"   : "IdMoneda","operator": "eq", "value" : "USD"  };
	$scope.filMonCLP	= { "field"   : "IdMoneda","operator": "eq", "value" : "CLP"};
	$scope.filMonUF		= { "field"   : "IdMoneda","operator": "eq", "value" : "UF"};
	
	$scope.formData.filtroMonedaPago = {
			complejo : [$scope.filMonCLP],
			logic : "or",
			filters : [
				$scope.filMonUSD,$scope.filMonCLP,$scope.filMonUF
			]
	};

	var inversiones = [];
	var conf_medios_pago = bcsConfiguracionFFM.getDataSourceMetodosDePagoCSC();
	var pago_check = {}
	$scope.oConfiguracionDiaDescuento = {};
	$scope.oConfiguracionDiaDescuento.conf = {"min" : 1, "max" : 31};
	
    
    /************************************************
     * FILTROS  PARA EL CHECK REGIMEN SERIE FONDO
     ************************************************/
	ingresarCSC.filtrosRegimenSerieFondo = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
	
    
    /************************************************
     * FILTROS  MEDIOS DE PAGO
     ************************************************/
	conf_medios_pago.query({filter:{field: "IdEstado", operator: "eq", value: 'ACT'}, sort: { field: "Orden", dir: "asc" }}).then(function(){
		if(conf_medios_pago.data().length > 0){
			var datos = conf_medios_pago.data();
			$.each(datos,function(key,value){
				$scope.formData.matriz[value.IdCSCPago] = value;
				$scope.formData.check[value.IdCSCPago] = false;
				$scope.formData.checkDisable[value.IdCSCPago] = $scope.formData.matriz[key] || false;
				
				if (IngresoCSC.params.IdTipoSolicitud == 'Editar'){
					if(IngresoCSC.params.ObjetoCSC.TipoPago === value.IdCSCPago){
						$scope.formData.check[value.IdCSCPago] = true;
					}
				}
				
				bcsComunes.ventanaAutoHeight(winID);
			});
		}
	});
	
	$scope.defaultOptions = {
			dataSource: new bcs.DataSource({
			    type : "array",
				definition : {
					sort: { field: "RazonSocial", dir: "asc" },
					schema : {
						model : {
							fields : {
								RegimenTributacion	 : { type : "string"},
								RazonSocial			 : { type : "string"},
								Serie		 		 : { type : "number"},
								Monto		     	 : { type : "number"},
								Porcentaje		     : { type : "number"},				
							}
						}
					}			
				}
			}),
            columns: [  {field : "RegimenTributacion",  title:'UI_REGIMEN_TRIBUTACION', 	width :"150px"},
                        {field : "RazonSocial",         title:'UI_RAZON_SOCIAL', 	        width :"150px"},
                        {field : "Serie", 		        title:'UI_SERIE', 		            width :"80px"},
                        {field : "Monto", 		        title:"UI_MONTO", 		            width: "60px"},
                        {field : "Porcentaje",          title:'UI_PORCENTAJE', 	            width :"50px",  editable: true}
				    ],	
			sortable		: false,
			pageable		: true,
			autoBind		: true,
			height			: 160,
			selectable		: "row",
			userSettings	: false,
			navigatable		: true,
			save: function(e) 	{calculoMonto(e.values, e.model);},
			gridCreated     : function(g){
				$scope.defaultOptions.dataSource.data(inversiones);
				$scope.ApiInversiones = g;
			}
		};
	
	$scope.formData.DescripcionCanal = 'OPTIMUS';
	if (IngresoCSC.params.IdTipoSolicitud == 'Editar'){
		bcsComunes.ventanaTitulo(winID, $translate.instant('UI_MODIFICAR_CSC'));
		$scope.formData.IdFolio = IngresoCSC.params.ObjetoCSC.FolioCSC;
		$scope.formData.FolioRelacionado = IngresoCSC.params.ObjetoCSC.FolioCSC;
		$scope.formData.MontoPesos = IngresoCSC.params.ObjetoCSC.MontoPesos;
		$scope.formData.MontoUF = IngresoCSC.params.ObjetoCSC.MontoUF;
		$scope.formData.NombreFuncionario = IngresoCSC.params.ObjetoCSC.NombreFuncionario;
		$scope.formData.RutFuncionario = IngresoCSC.params.ObjetoCSC.DocumentoIdentificacionFuncionario;
		$scope.formData.DomicilioSucursal = IngresoCSC.params.ObjetoCSC.DomicilioSucursal;
		$scope.formData.CodigoSucursal = IngresoCSC.params.ObjetoCSC.CodigoSucursal;
		$scope.formData.DiaDescuento = IngresoCSC.params.ObjetoCSC.DiaDescuento;
        $scope.formData.Hora = IngresoCSC.params.ObjetoCSC.Hora;
        $scope.formData.IdEstado = IngresoCSC.params.ObjetoCSC.IdEstado;
        
        $scope.formData.Canal = IngresoCSC.params.ObjetoCSC.Canal;
        $scope.formData.IdFolioCSC = IngresoCSC.params.ObjetoCSC.IdFolioCSC;
        $scope.formData.DescripcionCanal = IngresoCSC.params.ObjetoCSC.DescripcionCanal;
        // CARGAR EL CANAL
        
        if(IngresoCSC.params.ObjetoCSC.IndicadorMLT === "S"){
        	$scope.formData.AceptoMLT = $scope.formData.AceptoMLT = true;
        }

		if(IngresoCSC.params.ObjetoCSC.AceptoCondiciones === "true"){
			$scope.formData.AceptoCondiciones =$scope.formData.AceptoCondiciones = true;
		}
		
		if(IngresoCSC.params.ObjetoCSC.FirmaFuncionario === "true"){
			$scope.formData.FirmaFuncionario =$scope.formData.FirmaFuncionario = true;
		}
        
        //  consultamos el detalle 
			
		var defDestinos = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : false, // Paginado de los datos en el servidor.
	                serverSorting   : false, // Ordenamiento de los datos en el servidor.
	                pageSize        : 6,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=600086",
							dataType: "json"
						}
					},
					
					sort : { field: "NombreSerieFondo", dir: "asc" }, 
					schema: {
						model: {
							fields: {
								Regimen	 			 : { type : "string"},
								IdSerieFondo		 : { type : "string"},
								NombreSerieFondo	 : { type : "string"},
								Monto		     	 : { type : "number"},
								Porcentaje		     : { type : "number"},
							}
						}
					}
				}	
			};
        
        
		var dsDestinos = new bcs.DataSource(defDestinos);
		var filter = { field: "IdFolioCSC", operator: "eq",  value: $scope.formData.IdFolioCSC};
		dsDestinos.query({filter:filter}).then(function(){
			inversiones = [];
			$.each(dsDestinos.data(),function(key,value) {
				inversiones.push({RegimenTributacion: value.Regimen , RazonSocial : value.NombreSerieFondo, Serie: value.IdSerieFondo, Monto : value.Monto, Porcentaje : value.Porcentaje });
			});
			$scope.defaultOptions.dataSource.data(inversiones);
			console.log($scope.defaultOptions.dataSource.data());
		})
		
		
		$scope.$watch('$viewContentLoaded', function(){
			if(IngresoCSC.params.ObjetoCSC.IdPortafolio){
				bcsComunes.ventanaTitulo(IngresoCSC.idVentana, $translate.instant("UI_EDICION_DE_CONTRATO"));
				$timeout(function() {
					var promesa = bcsComunes.ventanaPromesa(IngresoCSC.domVentana);
						$.when(promesa).then(function(){
							$scope.oClientePortafolio.idActual =  IngresoCSC.params.ObjetoCSC.IdPortafolio || -1;
							$scope.oClientePortafolio.idDefecto =  IngresoCSC.params.ObjetoCSC.IdPortafolio;
						});
				},50);
			}else{
				bcsComunes.ventanaTitulo(IngresoCSC.idVentana, $translate.instant("UI_INGRESO_DE_CONTRATO"));
				pc1.modoPantalla="INGRESO";
			}
		});
	
				
		
    } // FIN EDITAR
	

	
	$scope.formData.LabelBanco = false;
	$scope.formData.DiasDescuento = false;
	$scope.formData.Banco = false;
	$scope.formData.LabelDiasDescuento = false;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.dataTemplate = {};
	$scope.formData.Deshabilitado = false;
	$scope.formData.Canal = 'OPT';
	
	
	$scope.limpiarFiltros = function(){
		$scope.formData.Fecha  = util.dateInicioDia($scope.SesionFechaSistema);
	};
	
	$scope.ingresarCSC.filtrosRegimen = {
		filtros: {
			CodigoRegimenTipoAhorro : {"field": "CodigoRegimenTipoAhorro","operator": "neq", "value"   :  'APV'},
		}
	};
	
	$scope.Instrumento ={
			tipo : "combobox-serie-fondo-regimen",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdRegimenFondoMutuo : {"field": "IdRegimenFondoMutuo","operator": "eq", "value"   :  0 },
			},
	};
	
	
	$scope.GrupoRegimen = {"field": "GrupoRegimen","operator": "eq", "value"   :  "APV" };
	$scope.GrupoRegimen2 = {"field": "GrupoRegimen","operator": "eq", "value"  :  "DC"};
	
	$scope.ingresarCSC.filtrosRegimenSerieFondo = { 
				IdSerieFondo : {"field": "IdSerieFondo","operator": "eq", "value"   :  undefined },
				GrupoRegimen1 :  {"field": "GrupoRegimen","operator": "neq", "value"  :  "APV" },
				GrupoRegimen2 :  {"field": "GrupoRegimen","operator": "neq", "value"  :  "DC" }
	};
	
		
	
	$scope.limpiarFiltros();
	
	$scope.ajustaVentana = function(){
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	$scope.ajustaVentana();
		
	$scope.formData.agregarItem = function (){
		
		if ($scope.formData && $scope.Instrumento.seleccionado){
			inversiones = [];
			
			inversiones.push({RegimenTributacion: $scope.formData.IdRegimenSerieFondo , RazonSocial : $scope.Instrumento.seleccionado.NombreSerieFondo, Serie: $scope.Instrumento.seleccionado.IdSerieFondo, Monto : 0, Porcentaje : 0 });
			
			$.each($scope.defaultOptions.dataSource.data(),function(key,value) {
				inversiones.push(value);
			});
			
			$scope.defaultOptions.dataSource.data(inversiones);
		} else {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INSTRUMENTO_VACIO"));
		}
		
	}
	

	
	$scope.formData.quitarItem = function (){
		$scope.defaultOptions.dataSource.remove($scope.formData.ItemSeleccionado);
		inversiones = [];
		$.each($scope.defaultOptions.dataSource.data(),function(key,value) {
			inversiones.push(value);
		});
	}
	

	
	$scope.oClientePortafolio = {
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				Proposito : {"field": "Proposito","operator": "eq", "value"   :  'FFMM'}
			},
	};
	
	estado_banco = IngresoCSC.params.IdTipoSolicitud == "Editar" ? IngresoCSC.params.ObjetoCSC.IdBanco : '';
	
	$scope.oConfiguracionBanco={
			tipo          : "combobox-banco",
			configuracion : {},
			filtros       : { 
				IdEmpresa : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
				IdEstado  : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
			},
			seleccionado  : false,
			idDefecto     : estado_banco		
	};
	

	$scope.formData.IngresarCSC = function() {	
		bcsComunes.ventanaOverlay(winID,true);
		 
		var monto_final=0;
		var idMedioPago = "";
		var diaDescuento = 0
		var idBancoFormulario = 0;
		var idFolioCSC = 0;
		
		$.each($scope.defaultOptions.dataSource.ds._view,function(key,value) {
			monto_final = value.Porcentaje + monto_final
		});
		
		if (monto_final > 100){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_PORCENTAJE_INVALIDO"));
		}
		
		$scope.formData.MostrarDI = $scope.formData.MontoPesos ? true : false;
		
		$.each($scope.formData.check,function(key,value){
			if(value){
				idMedioPago = key;
			}
		})
		 		 
		
		 
		diaDescuento 		= $scope.formData.DiaDescuento === undefined ? 0 : $scope.formData.DiaDescuento;
		idBancoFormulario 	= $scope.oConfiguracionBanco.idActual === false ? 0 : $scope.oConfiguracionBanco.idActual;
		
	    idFolioCSC 			= IngresoCSC.params.IdTipoSolicitud == 'Editar'? IngresoCSC.params.ObjetoCSC.IdFolioCSC : 0;
	    IndicadorMLT 		= ($scope.formData.AceptoMLT === true) ? "S" : "N";
	    
	    
		
	   if(IngresoCSC.params.IdTipoSolicitud == 'Editar'){
		   var datosIngresoCSC = {
					p_IdEmpresa : $scope.SesionIdEmpresa, 
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'FMM',
					p_simularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_ModificarCSC : [{
						 	IdFolioCSC					:   idFolioCSC,
						 	FolioCSC		      		:	$scope.formData.IdFolio || $scope.formData.IdFolioEdit,
						 	IdCliente 	          		:	$scope.oClientePortafolio.seleccionado.IdCliente,
						 	IdFolioEdit					:	$scope.formData.IdFolioEdit,
						 	FolioRelacionado			:	$scope.formData.FolioRelacionado,
					    	IdPortafolio          		:	$scope.oClientePortafolio.seleccionado.IdPortafolio,
					    	Hora          				:	'12:00:00',
					    	Fecha         				:	$scope.formData.Fecha,
					    	MontoPesos         			:	$scope.formData.MontoPesos,
					    	MontoUF  					:	$scope.formData.MontoUF,
					    	TipoPago					:   idMedioPago, 
					    	IdBanco						:	idBancoFormulario,
					    	DomicilioSucursal			:   $scope.formData.DomicilioSucursal,
					    	CodigoSucursal				:   $scope.formData.CodigoSucursal,
					    	NombreFuncionario			:   $scope.formData.NombreFuncionario,
					    	DocumentoIdentificacionFuncionario	:   $scope.formData.RutFuncionario,
					    	FirmaFuncionario			:   $scope.formData.FirmaFuncionario || false,
					    	DiaDescuento				:	diaDescuento,
					    	AceptoCondiciones			:	$scope.formData.AceptoCondiciones || false,
					    	Canal						:  	$scope.formData.Canal,
					    	IdEstado					:	$scope.formData.IdEstado || '',
					    	MLT							:	IndicadorMLT,					    	

					    	p_ModificarCSCDetalle : []
					 }]		 
			}
	   }else{
		   var monto = 0;
		   $scope.formData.MontoPesos = 0;
		   $scope.formData.MontoUF = 0;
		   $scope.formData.MontoUSD = 0;
		   if ($scope.formData.MonedaSeleccionado.IdMoneda === 'CLP') $scope.formData.MontoPesos = $scope.formData.Monto;
		   if ($scope.formData.MonedaSeleccionado.IdMoneda === 'UF') $scope.formData.MontoUF = $scope.formData.Monto;
		   if ($scope.formData.MonedaSeleccionado.IdMoneda === 'USD') $scope.formData.MontoUSD = $scope.formData.Monto;
		   if (idMedioPago === 'PAC' && $scope.formData.MonedaSeleccionado.IdMoneda === 'USD'){
			   bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_PAC_MONEDA_NO_VALIDA"));
			   bcsComunes.ventanaOverlay(winID,false);
			   return;
		   }
		   var datosIngresoCSC = {
					p_IdEmpresa : $scope.SesionIdEmpresa, 
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'FMM',
					p_simularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_IngresoCSC : [{
						 	IdFolioCSC					:   idFolioCSC,
						 	FolioCSC		      		:	$scope.formData.IdFolio || $scope.formData.IdFolioEdit,
						 	IdCliente 	          		:	$scope.oClientePortafolio.seleccionado.IdCliente,
						 	IdFolioEdit					:	$scope.formData.IdFolioEdit,
						 	FolioRelacionado			:	$scope.formData.FolioRelacionado,
					    	IdPortafolio          		:	$scope.oClientePortafolio.seleccionado.IdPortafolio,
					    	Hora          				:	'12:00:00',
					    	Fecha         				:	$scope.formData.Fecha,
					    	MontoPesos         			:	$scope.formData.MontoPesos,
					    	MontoUF  					:	$scope.formData.MontoUF,
					    	MontoUSD  					:	$scope.formData.MontoUSD,
					    	TipoPago					:   idMedioPago, 
					    	IdBanco						:	idBancoFormulario,
					    	DomicilioSucursal			:   $scope.formData.DomicilioSucursal,
					    	CodigoSucursal				:   $scope.formData.CodigoSucursal,
					    	NombreFuncionario			:   $scope.formData.NombreFuncionario,
					    	DocumentoIdentificacionFuncionario	:   $scope.formData.RutFuncionario,
					    	FirmaFuncionario			:   $scope.formData.FirmaFuncionario || false,
					    	DiaDescuento				:	diaDescuento,
					    	AceptoCondiciones			:	$scope.formData.AceptoCondiciones || false,
					    	Canal						:  	$scope.formData.Canal,
					    	IdEstado					:	$scope.formData.IdEstado || '',
					    	MLT							:	IndicadorMLT,					    	

					    	p_IngresoCSCDetalle : []
					 }]		 
			}
	   }
		 
		
		
		$.each($scope.defaultOptions.dataSource.ds._view,function(key,value) {
			if (IngresoCSC.params.IdTipoSolicitud == 'Editar'){
					var agregar_regimen=value.NombreSerieFondo;
					datosIngresoCSC.p_ModificarCSC[0].p_ModificarCSCDetalle.push({Serie : value.Serie, Monto : value.Monto, Porcentaje : value.Porcentaje, Regimen :value.RegimenTributacion  });
			}else{
				var agregar_regimen=value.RegimenTributacion;
				datosIngresoCSC.p_IngresoCSC[0].p_IngresoCSCDetalle.push({Serie : value.Serie, Monto : value.Monto, Porcentaje : value.Porcentaje, Regimen :value.RegimenTributacion  });
			}
	    });
		
		 
		if (monto_final > 100){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_PORCENTAJE_INVALIDO"));
				bcsComunes.ventanaOverlay(winID,false);
				return;		
		}
		 
		var obtieneDigitoVerificador = function dv(T) {
			var M=0,S=1;for(;T;T=Math.floor(T/10))
				S=(S+T%10*(9-M++%6))%11;return S?S-1:'K';
		};
		
		if($scope.formData.RutFuncionario){
			if(!($scope.formData.RutFuncionario.indexOf('-') > 0 &&
					$scope.formData.RutFuncionario.length > ($scope.formData.RutFuncionario.indexOf('-') + 1) &&
					$scope.formData.RutFuncionario[$scope.formData.RutFuncionario.indexOf('-') + 1] &&
					$scope.formData.RutFuncionario.length >= 3 &&
					obtieneDigitoVerificador($scope.formData.RutFuncionario.substr(0,$scope.formData.RutFuncionario.length-2)) == $scope.formData.RutFuncionario[$scope.formData.RutFuncionario.indexOf('-') + 1]))
					{
						bcsComunes.msg("error",'Error','UI_RUT_INVALIDO');
						bcsComunes.ventanaOverlay(winID,false);
						return;
					}
			
		}else{
			bcsComunes.msg("error",'Error','UI_RUT_VACIO');
			bcsComunes.ventanaOverlay(winID,false);
			return;
		}
		
		
		
		if(IngresoCSC.params.IdTipoSolicitud == 'Editar'){
			bcsServicioFondosMutuos.ModificarCSC(datosIngresoCSC).$promise.then(function(respuesta){
			
                if (respuesta.P_OUT_ERRORESCONTADOR === 0){ 
                    var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_EDICION_CONTRATO"),conflictos:util.parseServerResponse(respuesta)},winID);
                    bcsComunes.ventanaOverlay(winID,false);
                    bcsConfiguracionFFM.ingresarCerrarVentana(id, winID); 
                    
                }else{
                    bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_FOLIO_YA_EXISTE"),conflictos:util.parseServerResponse(respuesta)}, winID);
                    bcsComunes.ventanaOverlay(winID,false);
                }
			},function(respuesta){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
				bcsComunes.ventanaOverlay(winID,false);
			});
			
		}else{ 

			
			bcsServicioFondosMutuos.IngresarCSC(datosIngresoCSC).$promise.then(function(respuesta){
		     	 
		     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){ 
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREACION_CONTRATO"),conflictos:util.parseServerResponse(respuesta)},winID);
		     		bcsComunes.ventanaOverlay(winID,false);
		     		bcsConfiguracionFFM.ingresarCerrarVentana(id, winID); 
		     		
		     	}else{
		 			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_FOLIO_YA_EXISTE"),conflictos:util.parseServerResponse(respuesta)}, winID);
		     		bcsComunes.ventanaOverlay(winID,false);	
		     	}
			},function(respuesta){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
				bcsComunes.ventanaOverlay(winID,false);
			});
		}
		
    	
	    
		
	}
	
	$scope.$watch('formData.PAC.EsHabilitado',function(newval,oldval){
			$scope.formData.LabelBanco =$scope.formData.LabelBanco ? false : true;
			$scope.formData.Banco =$scope.formData.Banco ? false : true;
			$scope.formData.DiasDescuento =$scope.formData.DiasDescuento ? false : true;
			$scope.formData.LabelDiaDescuento =$scope.formData.LabelDiaDescuento ? false : true;
			
	});
	
	$scope.$watch('defaultOptions.dataSource.data().length',function(newval,oldval){
		
		if ($scope.formData.AceptoMLT && newval == 1) {
			$scope.formData.habilitarAgregar = false;
		}else if($scope.formData.AceptoMLT && newval < 1){
			$scope.formData.habilitarAgregar = true;
		}
			
	});
	
	$scope.$watch('formData.AceptoMLT',function(newval,oldval){
		var numeroRegistros = $scope.defaultOptions.dataSource.data().length;
		
		if(newval && numeroRegistros == 1){
			$scope.formData.habilitarAgregar = false;
		}else if (!newval) {
			$scope.formData.habilitarAgregar = true;
		}else if(newval && numeroRegistros > 1){
			
			var confirmacion = confirm("Para activar MLT solo debe poseer un Instrumento asignado. �Desea limpiar la Grilla?");
			if (confirmacion) { 
				$scope.defaultOptions.dataSource.data([]);	
			}else{
				$scope.formData.AceptoMLT = false;
			}
		}
		
	});
	
	
	$scope.$watch('formData.Monto',function(newval,oldval){
		
		inversiones = [];
		
		$.each($scope.defaultOptions.dataSource.data(),function(key,value) {
			inversiones.push({RegimenTributacion: value.RegimenTributacion , RazonSocial : value.RazonSocial, Serie: value.Serie, Monto : (newval*value.Porcentaje)/100, Porcentaje : value.Porcentaje });
		});	
		
		$scope.defaultOptions.dataSource.data(inversiones);
		
	});


	
	var calculoMonto = function(valor, model){
		if($scope.formData.Monto && $scope.formData.Monto != 0){
			model.Monto = ($scope.formData.Monto * valor.Porcentaje) / 100;
		}
	}
	
	
	$scope.formData.Requerido = function(tipo)  {
		if(tipo === 'Pesos' ){
			$scope.formData.MontoUF = 0;	
		}else if (tipo === 'UF'){
            $scope.formData.MontoPesos = 0;	
        }			
	}
	
    $scope.$watchCollection('formData.check',function(newValue, oldValue){
        
        if(newValue && oldValue && JSON.stringify(newValue)!=JSON.stringify(oldValue)){
		    var existeMedioPago = false;
            
            $.each(newValue,function(key,value){
					 
			    if(key === 'PAC' && value === true){
                    $scope.formData.LabelBanco =$scope.formData.LabelBanco = true;
                    $scope.formData.Banco =$scope.formData.Banco = true;
                    $scope.formData.DiasDescuento =$scope.formData.DiasDescuento = true;
                    $scope.formData.LabelDiaDescuento =$scope.formData.LabelDiaDescuento = true;
				
				}else if (key === 'PAC' && value === false){
                    $scope.formData.LabelBanco =$scope.formData.LabelBanco = false;
                    $scope.formData.Banco =$scope.formData.Banco = false;
                    $scope.formData.DiasDescuento =$scope.formData.DiasDescuento = false;
                    $scope.formData.LabelDiaDescuento =$scope.formData.LabelDiaDescuento = false;	 
				}
					 
				if(value){
					existeMedioPago = true;
                    
                    $.each($scope.formData.check,function(key2,value2){
						if(key2 !== key){
							$scope.formData.check[key2] = false;
							$scope.formData.checkDisable[key2] = true;
						}
					})
				}
			});
                 
            if(!existeMedioPago){
                $.each($scope.formData.check,function(key,value){
					$scope.formData.check[key] = false;
					$scope.formData.checkDisable[key] = false;
				})
			}	
		}
	});
		
		
	$scope.$watch('Instrumento.seleccionado',function(newval,oldval){
		if (newval && newval.IdFondo) {
			$scope.ingresarCSC.filtrosRegimenSerieFondo.IdSerieFondo.value = newval.IdSerieFondo;
		};
    });
    
});