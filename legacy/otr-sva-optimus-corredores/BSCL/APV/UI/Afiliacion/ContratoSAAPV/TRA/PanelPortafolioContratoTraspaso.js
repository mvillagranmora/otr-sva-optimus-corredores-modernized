/*************************************************************
 * Purpose	: Panel Portafolio Contrato
 * Author	: TEAM PRONET IT SPA - BCS
 * Date		: 15/01/2018
 **************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelPortafolioContratoTraspasoCtrl',
function($scope,$translate,$rootScope,bcsComunes,$timeout,$state,$modal,$q,bcsConsultasAPV,bcsServiciosAPV,bcsAlerta,OPCIONES_BCSALERTA)
{
		/******************************************************/
		/*** INICIO DE VARIABLES Y PRECARGA					***/
		/******************************************************/

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var ptrasp = this;
		var custodia = [];
		
		ptrasp.HabilitoCampoTraspaso = false;
		ptrasp.datosForm ={};
		ptrasp.oPortafolio = {};
		$scope.csaapv.ptrasp = ptrasp;
		ptrasp.habilitarAgregar = true;
		
		$scope.filtroIdCliente = { "field": "IdCliente","operator": "eq", "value"   :  $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente ? $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente : 0 };
		$scope.filtroIdProductoAgrupador = { "field": "IdRegimenTributario","operator": "neq", "value"   :  9999 };
		$scope.filtroIdRegimenTributario = { "field": "IdRegimenTributario","operator": "neq", "value"   :  0 };
		
		$scope.ptrasp.oPortafolio = {
			filtros: {
				complejo: [
					$scope.filtroIdCliente,
					$scope.filtroIdProductoAgrupador
				],
				logic: "and",
				filters: [
					{
						 logic: "or",
						 filters: [
						 	$scope.filtroIdRegimenTributario
						 ]	
					},
					$scope.filtroIdCliente,
					$scope.filtroIdProductoAgrupador
					
				]
			}
		};
		
		$scope.ptrasp.EntidadRecaudadora = {
				idActual: 0,
				seleccionado:false,
				filtros : {
    	    		IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
    	    } 
		};
		
		$scope.ptrasp.RazonRetiro = {
				filtros : {
    	    		Codigo1 : { "field"   : "Codigo","operator": "neq", "value"   : "RES" },
    	    		Codigo2 : { "field"   : "Codigo","operator": "neq", "value"   : "REI" }
    	    } 
		};
	   	
		$scope.ptrasp.RegimenAgrupador = {
				filtros: {
					CodigoRegimenTipoAhorro : { "field": "CodigoRegimenTipoAhorro","operator": "eq", "value"   :  undefined },
					Codigo : { "field": "Codigo","operator": "neq", "value"   :  "BF" },
					IdEstadoRegimen : { "field": "IdEstadoRegimen","operator": "eq", "value"   :  "VIG" }
				},
			};
		
		$scope.$watch('csaapv.datosForm.SocioNegocioSeleccionado.IdCliente',function(newval,oldval){
			if (newval && ptrasp.RegimenAgrupadorSeleccionado){
				$scope.ptrasp.oPortafolio.filtros.complejo[0].value = $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente;
				$scope.ptrasp.oPortafolio.filtros.filters[1].value = $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente;
				if($scope.ptrasp.RegimenAgrupadorSeleccionado.Codigo == " TODOS"){
					$scope.fnFiltrarPortafolios(newval.Codigo, 0);
				}
			} 
		});
		
		$scope.$watch('ptrasp.RegimenAgrupadorSeleccionado',function(newval,oldval){
			if($scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente){
				if (newval && newval.Codigo != ' TODOS'){
					$scope.fnFiltrarPortafolios(newval.Codigo, $scope.ptrasp.RegimenAgrupadorSeleccionado.IdRegimenTributario);
					return;
				}
				
				if (newval && newval.Codigo == ' TODOS'){
					$scope.fnFiltrarPortafolios(newval.Codigo, 0);
				}
			}
		});
		
		$scope.fnFiltrarPortafolios = function (codigoRegimen, idRegimenTributario){
			if(typeof codigoRegimen !== 'undefined' && $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente){
					$scope.ptrasp.oPortafolio.filtros.filters[0].filters = [];
					$scope.fnReiniciarComplejoFiltroPortafolio();
					$scope.fnReiniciarFiltersFiltroPortafolio();
					$scope.ptrasp.oPortafolio.filtros.complejo[0].value = $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente;
					$scope.ptrasp.oPortafolio.filtros.filters[1].value = $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente;
					$scope.ptrasp.oPortafolio.filtros.filters[0].filters[0] = { "field": "IdRegimenTributario","operator": codigoRegimen == ' TODOS' ? "neq": "eq", "value"   :  idRegimenTributario };
					if(ptrasp.TipoAhorroSeleccionado.Codigo === "DC" ){
						$scope.ptrasp.oPortafolio.filtros.complejo.push({ "field": "CodigoRegimenTipoAhorro","operator": "eq", "value"   :  ptrasp.TipoAhorroSeleccionado.Codigo});
						$scope.ptrasp.oPortafolio.filtros.filters.push({ "field": "CodigoRegimenTipoAhorro","operator": "eq", "value"   :  ptrasp.TipoAhorroSeleccionado.Codigo});
						return;
					}
					if(ptrasp.TipoAhorroSeleccionado.Codigo === "APV" ){
						$scope.ptrasp.oPortafolio.filtros.complejo.push({ "field": "CodigoRegimenTipoAhorro","operator": "neq", "value"   : "DC"});
						$scope.ptrasp.oPortafolio.filtros.filters.push({ "field": "CodigoRegimenTipoAhorro","operator": "neq", "value"   : "DC"});
						if(codigoRegimen == 'APV-A' || (codigoRegimen == ' TODOS' )){
							$scope.ptrasp.oPortafolio.filtros.filters[0].filters.push({ "field": "IdRegimenTributario","operator": "eq", "value"   :  7 });
												
						}
						return;
					}
					if(codigoRegimen == 'APV-A'){
						$scope.ptrasp.oPortafolio.filtros.filters[0].filters.push({ "field": "IdRegimenTributario","operator": "eq", "value"   :  7 });											
					}
			}
		}
		
		$scope.fnReiniciarComplejoFiltroPortafolio = function(){
			$scope.ptrasp.oPortafolio.filtros.complejo = [
				$scope.filtroIdCliente,
				$scope.filtroIdProductoAgrupador
			]
		};
		
		$scope.fnReiniciarFiltersFiltroPortafolio = function(){
			$scope.ptrasp.oPortafolio.filtros.filters = [
				{
					 logic: "or",
					 filters: [
					 	$scope.filtroIdRegimenTributario
					 ]	
				},
				$scope.filtroIdCliente,
				$scope.filtroIdProductoAgrupador
			]
		};
		
		
		/******************************************************/
		/*** FUNCIONES DE CARGA DE TABLAS					***/
		/******************************************************/
		var defOrigenTraspaso = {
			type: "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
		        serverPaging    : true, // Paginado de los datos en el servidor.
		        serverSorting   : true, // Ordenamiento de los datos en el servidor.
		        pageSize        : 100,
		        sort 			: { field: "IdDetalleOrigenTraspaso", dir: "DESC" },
		        pageSize        : 10,
		        transport		: {
					read: {
						url 		: bcsConsultasAPV('ConsultarOrigenTraspaso'),
						dataType	: "json"
				    }
				},
				schema: {
					model: {
						fields: {
							   IdTraspaso				: { type: "number"}
							 , IdContratoSAAPV			: { type: "number"}
							 , TipoMovimiento			: { type: "string"}
							 , RazonRetiro				: { type: "string"}
							 , FechaIngreso				: { type: "sysdate"}
							 , CodigoTipoTraspaso		: { type: "string"}
							 , IdCliente				: { type: "number"}
							 , IdPersona				: { type: "number"}
							 , Descripcion				: { type: "string"}
							 , IdSocioNegocioCli		: { type: "number"}
							 , IdSocioNegocioEMP		: { type: "number"}
							 , IdDetalleOrigenTraspaso	: { type: "number"}
							 , CodigoTipoAhorro			: { type: "string"}
							 , IdEntidadRecaudadora		: { type: "number"}
							 , NombreEntidad			: { type: "string"}
							 , DocumentoEntidad			: { type: "string"}
							 , Fondo					: { type: "string"}
							 , MontoTraspaso			: { type: "number"}
							 , UFTraspaso				: { type: "number"}
							 , PorcentajeTraspaso		: { type: "number"}
							 , IdEstado					: { type: "string"}
							 , CodigoPropositoPortafolio : { type: "string"}
							 , IdPortafolio				: { type: "number"}
							 , IdInstrumento			: { type: "number"}
							 , Nemotecnico				: { type: "string"}
							 , CodigoRegimenTributario  : { type: "string"}
						}
					}
				}
			}	
		};
			
		/******************************************************/
		/*** DATASOURCES									***/
		/******************************************************/		
		ptrasp.dsOrigenTraspaso = new bcs.DataSource(defOrigenTraspaso);
		ptrasp.origenTraspaso = 
		{
			dataSource: ptrasp.dsOrigenTraspaso,
			columns: 
			[
				{field: "CodigoTipoTraspaso",title: $translate.instant('UI_TIPO_TRASPASO')		, width: "80px", align: "rigth" , sortable : true, editable: false , hidden: true},
				{field: "CodigoRegimenTributario"	,title: $translate.instant('UI_REGIMEN')	, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "IdPortafolio"				,title: $translate.instant('UI_PORTAFOLIO') , width: "80px", align: "rigth" , sortable : true, editable: false , hidden: true},
				{field: "CodigoPropositoPortafolio"	,title: $translate.instant('UI_PORTAFOLIO')	, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "IdInstrumento"		,title: $translate.instant('UI_INSTRUMENTO')	, width: "80px", align: "rigth" , sortable : true, editable: false , hidden: true},
				{field: "Nemotecnico"		,title: $translate.instant('UI_INSTRUMENTO')		, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "CodigoTipoAhorro"	,title: $translate.instant('UI_TIPO_AHORRO')		, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "NombreEntidad"		,title: $translate.instant('UI_NOMBRE')				, width: "80px", align: "rigth" , sortable : true, editable: false , hidden: true},
				{field: "DocumentoEntidad"	,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "80px", align: "rigth" , sortable : true, editable: false , hidden: true},
				{field: "MontoTraspaso"		,title: $translate.instant('UI_MONTO_PESOS')		, width: "80px", align: "left"  , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoTraspaso,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "UFTraspaso"		,title: $translate.instant('UI_MONTO_UF')			, width: "80px", align: "left"  , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(UFTraspaso,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "PorcentajeTraspaso",title: $translate.instant('UI_PORCENTAJE')			, width: "80px", align: "rigth" , sortable : true, editable: false}
			],	
			sortable		: false,
			pageable		: true,
			autoBind		: false,
			height			: 100,
			selectable 		: "row",
			userSettings	: true,
			navigatable		: true,
			pageSize		: 100,
			gridCreated		: function(grid){
				ptrasp.gridOrigenTraspaso = grid;
				if(parametrosVentana.modoVentana === 'M'){
					if(parametrosVentana.tipoTraspaso ==='EGR'){
						var filtroTraspaso = Array(); 
						filtroTraspaso.push({field: "IdTraspaso" , operator: "eq", value: Number($scope.csaapv.IdTraspaso)});
						ptrasp.gridOrigenTraspaso.dataSource.filter(filtroTraspaso);
						ptrasp.EntidadRecaudadora.idActual = 1005;
						$scope.buscarCustodia();
					}else{
						var filtroTraspaso = Array(); 
						filtroTraspaso.push({field: "IdContratoSAAPV" , operator: "eq", value: Number($scope.csaapv.IdContratoSAAPV)})
						ptrasp.gridOrigenTraspaso.dataSource.filter(filtroTraspaso);
						ptrasp.EntidadRecaudadora.idActual = 1005;
					}
				}
			}
		};
		
		/******************************************************/
		/*** FUNCIONES										***/
		/******************************************************/
		var tablaDestino = [];
		var tablaDestinoInstrumento=[];
		ptrasp.fnAgregoOrigenTraspaso = function()
		{
			var idVentana = bcsComunes.ventanaWinId($scope);
			bcsComunes.ventanaOverlay(idVentana,true);

			if(ptrasp.EntidadRecaudadora.idActual === false || ptrasp.EntidadRecaudadora.idActual === undefined || ptrasp.EntidadRecaudadora.idActual === ""){
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_ENTIDAD_RECAUDADORA"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			};
			
			if(ptrasp.TipoTraspasoIdActual === false || ptrasp.TipoTraspasoIdActual === undefined || ptrasp.TipoTraspasoIdActual === ""){
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_TIPO_TRASPASO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			};
			
			if(ptrasp.TipoAhorroIdActual === false || ptrasp.TipoAhorroIdActual === undefined || ptrasp.TipoAhorroIdActual === ""){
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_TIPO_AHORRO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			};
			
			//if($scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'INGRESO'){
			//	if(ptrasp.TipoAhorroSeleccionado.Codigo === 'APV' && ptrasp.EntidadRecaudadoraSeleccionado.IdTipoEntidadRecaudadora === 1){
			//		if(ptrasp.FondoOrigenIdActual === false || ptrasp.FondoOrigenIdActual === undefined || ptrasp.FondoOrigenIdActual === ""){
			//			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_FONDO_ORIGEN"));
			//			bcsComunes.ventanaOverlay(idVentana,false);
			//			return;
			//		}	
			//	}
			//}else 
			if($scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){
				if(ptrasp.RazonRetiroIdActual === false || ptrasp.RazonRetiroIdActual === undefined || ptrasp.RazonRetiroIdActual === ""){
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_RAZON_RETIRO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			}
			if(ptrasp.TipoTraspasoIdActual === 'PAR'){
				if((ptrasp.kngMontoTraspaso === undefined || ptrasp.kngMontoTraspaso === 0) 
						&& (ptrasp.kngUFTraspaso === undefined || ptrasp.kngUFTraspaso === 0) 
							&& (ptrasp.kngPorcentajeTraspaso === undefined || ptrasp.kngPorcentajeTraspaso === 0 ))
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_INGRESE_MONTO_MONTOUF_PORCENTAJE"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			};
			if(ptrasp.TipoTraspasoIdActual === 'PAR'){
				
				tablaDestino.push({
					"PorcentajeTraspaso" 	:	ptrasp.kngPorcentajeTraspaso   
				});
						
				var PorcentajeTotal=0;
				$.each(tablaDestino,function(key,value){
					PorcentajeTotal=Number(PorcentajeTotal)+ Number(value.PorcentajeTraspaso);
				});
				
				if (ptrasp.kngPorcentajeTraspaso > 100){
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("PORCENTAJE NO DEBE SER MAYOR A 100 %"));
					bcsComunes.ventanaOverlay(idVentana,false);
			 		return;
			 	}
			}else{
				tablaDestino.push({
					"PorcentajeTraspaso" 	:	100   
				});
				ptrasp.kngPorcentajeTraspaso  =100;
			};
			
			
			if($scope.formData === undefined && $scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){ 
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("SELECCIONE LOS INSTRUMENTOS A AGREGAR"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			if($scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){	
				let listaPaso = [];
				if (tablaDestinoInstrumento.length>0){
					var error=false;
					$.each($scope.formData.custodia,function(key,value) {
						if(tablaDestinoInstrumento.find(instrumento => instrumento.IdInstrumento == value.IdInstrumento && instrumento.IdPortafolio == value.IdPortafolio)){
							bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("Instrumento ya ha sido agregado"));
							$scope.formData.custodia = $scope.formData.custodia.filter(custodia => custodia.IdInstrumento != value.IdInstrumento && instrumento.IdPortafolio != value.IdPortafolio);
						} else {
							listaPaso.push({"Nemotecnico": value.Nemotecnico, "CantidadCustodia":value.CantidadCustodia, "IdInstrumento":value.IdInstrumento, "IdPortafolio":value.IdPortafolio});
						}
					});
					
					if(error==true){
						bcsComunes.ventanaOverlay(idVentana,false);
						return;
					}
					
				}else{
					$.each($scope.formData.custodia,function(key,value) {
						listaPaso.push({"Nemotecnico": value.Nemotecnico, "CantidadCustodia":value.CantidadCustodia, "IdInstrumento":value.IdInstrumento, "IdPortafolio":value.IdPortafolio});
					});
				}
				
				if(listaPaso.length == 0){
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
				$.each(listaPaso,function(key,value) {
					tablaDestinoInstrumento.push({"Nemotecnico": value.Nemotecnico, "CantidadCustodia":value.CantidadCustodia, "IdInstrumento":value.IdInstrumento, "IdPortafolio":value.IdPortafolio});
				});
			}
			
			
			if($scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'INGRESO'){		
				var PorcentajeTraspasoTotal=0;
				var SumaPorcentajes = [];
				SumaPorcentajes =ptrasp.dsOrigenTraspaso.dataColumns(['PorcentajeTraspaso']);
				
				if(SumaPorcentajes !== undefined){
					for (i=0;i<SumaPorcentajes.length;i++){
					PorcentajeTraspasoTotal = PorcentajeTraspasoTotal + SumaPorcentajes[i].PorcentajeTraspaso;
					}
				
					if (PorcentajeTraspasoTotal >= 100){
						bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("PORCENTAJE NO DEBE SER MAYOR A 100 %"));
						bcsComunes.ventanaOverlay(idVentana,false);
				 		return;
				 	}
				
				}
			}
			
			fnAgregoOrigenTraspaso();

			bcsComunes.ventanaOverlay(idVentana,false);
		};
		
		ptrasp.LimpioVariablesOrigen = function()
		{	
			ptrasp.kngPorcentajeTraspaso 			= undefined;
			ptrasp.kngUFTraspaso 					= undefined;
			ptrasp.kngMontoTraspaso 				= undefined;
			ptrasp.FondoOrigenIdActual 				= undefined;
			
			if(ptrasp.dsOrigenTraspaso === undefined || ptrasp.dsOrigenTraspaso.data().length === 0)
			{
				ptrasp.DocumentoIdentidadEntidadOrigen	= undefined;
				ptrasp.EntidadRecaudadora.idActual		= undefined;
				ptrasp.TipoTraspasoIdActual				= undefined;
				ptrasp.habilitoDocumentoIdentidad 		= true;
				ptrasp.habilitoTipoTraspaso				= true;
				tablaDestinoInstrumento					= [];
				$scope.custodiaOptions.dataSource.removeAll();

			}else{
				ptrasp.habilitoDocumentoIdentidad	 	= false;
				ptrasp.habilitoTipoTraspaso				= false;
			}
		};
		
		fnAgregoOrigenTraspaso = function ()
		{
			if($scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){
				$.each($scope.formData.custodia,function(key,value) {
					ptrasp.dsOrigenTraspaso.add({
						//IdEntidadRecaudadora		: ptrasp.EntidadRecaudadora.idActual,
						IdEntidadRecaudadora		: ptrasp.EntidadRecaudadoraSeleccionado.IdEntidadRecaudadora,
						//NombreEntidad				: ptrasp.EntidadRecaudadoraSeleccionado,
						NombreEntidad				: ptrasp.EntidadRecaudadoraSeleccionado.Nombre,
						DocumentoEntidad			: ptrasp.DocumentoIdentidadEntidadOrigen,
						CodigoTipoTraspaso			: ptrasp.TipoTraspasoSeleccionado.Nombre,
						CodigoTipoAhorro			: ptrasp.TipoAhorroSeleccionado.Codigo,
						Fondo						: ptrasp.FondoOrigenIdActual === false ? "" : ptrasp.FondoOrigenIdActual,
						MontoTraspaso				: ptrasp.kngMontoTraspaso,
						UFTraspaso					: ptrasp.kngUFTraspaso,
						PorcentajeTraspaso			: ptrasp.kngPorcentajeTraspaso,
						CodigoRegimenTributario		: value.Regimen,
						IdPortafolio				: value.IdPortafolio,
						CodigoPropositoPortafolio	: value.CodigoPortafolio,
						IdInstrumento				: value.IdInstrumento,
						Nemotecnico					: value.Nemotecnico,
						CantidadCustodia			: value.CantidadCustodia
					});	
				});
			} else {
				ptrasp.dsOrigenTraspaso.add({
					//IdEntidadRecaudadora		: ptrasp.EntidadRecaudadoraSeleccionado.IdEntidadRecaudadora,
					IdEntidadRecaudadora		: ptrasp.EntidadRecaudadoraSeleccionado.IdEntidadRecaudadora,
					//NombreEntidad				: ptrasp.EntidadRecaudadoraSeleccionado,
					NombreEntidad				: ptrasp.EntidadRecaudadoraSeleccionado.Nombre,
					DocumentoEntidad			: ptrasp.DocumentoIdentidadEntidadOrigen,
					CodigoTipoTraspaso			: ptrasp.TipoTraspasoSeleccionado.Nombre,
					CodigoTipoAhorro			: ptrasp.TipoAhorroSeleccionado.Codigo,
					Fondo						: ptrasp.FondoOrigenIdActual === false ? "" : ptrasp.FondoOrigenIdActual,
					MontoTraspaso				: ptrasp.kngMontoTraspaso,
					UFTraspaso					: ptrasp.kngUFTraspaso,
					PorcentajeTraspaso			: ptrasp.kngPorcentajeTraspaso,
					CodigoRegimenTributario		: 0,
					IdPortafolio				: 0,
					CodigoPropositoPortafolio	: 0,
					IdInstrumento				: 0,
					Nemotecnico					: '',
					CantidadCustodia			:0
				});	
			}
			
			ptrasp.LimpioVariablesOrigen();
		};
			
		ptrasp.fnEliminoOrigenTraspaso = function(){
			if(ptrasp.origenTraspasoSeleccionado === undefined)
				return;
			
			ptrasp.dsOrigenTraspaso.remove(ptrasp.origenTraspasoSeleccionado);
			
			if (tablaDestinoInstrumento.length>0){
				
				for(var i = 0; i < tablaDestinoInstrumento.length ; i++) {
					if (tablaDestinoInstrumento[i].IdInstrumento === ptrasp.origenTraspasoSeleccionado.IdInstrumento && tablaDestinoInstrumento[i].IdPortafolio === ptrasp.origenTraspasoSeleccionado.IdPortafolio && tablaDestinoInstrumento[i].CantidadCustodia=== ptrasp.origenTraspasoSeleccionado.CantidadCustodia){
						console.log('value',tablaDestinoInstrumento[i].Nemotecnico)
						tablaDestinoInstrumento.splice(i,1)
					}
				}
			}
			
		};
		
		ptrasp.LimpiarTraspaso = function(){
			ptrasp.dsOrigenTraspaso.removeAll();
			ptrasp.LimpioVariablesOrigen();
		};
		
		ptrasp.Requerido = function(tipo)
		{
			if(tipo === 'MONTO')
			{
				if(ptrasp.montoTraspaso !== null)
				{
					ptrasp.UFTraspasoRequerido 				= false;
					ptrasp.MontoUFDeshabilitado				= false;
					ptrasp.PorcentajeTraspasoRequerido 		= false;
					ptrasp.PorcentajeDeshabilitado			= false;
				}
			}
			else if(tipo === 'UF')
			{
				if(ptrasp.uFTraspaso !== null)
				{
					ptrasp.MontoTraspasoRequerido			= false;
					ptrasp.MontoPesoDeshabilitado			= false;
					ptrasp.PorcentajeTraspasoRequerido 		= false;
					ptrasp.PorcentajeDeshabilitado			= false;
				}
			}
			
			if(ptrasp.montoTraspaso === null || ptrasp.montoTraspaso === undefined)
			{
				if(ptrasp.uFTraspaso === null || ptrasp.uFTraspaso == undefined)
				{
					if(ptrasp.porcentajeTraspaso === null || ptrasp.porcentajeTraspaso === undefined)
					{
						ptrasp.MontoTraspasoRequerido 		= true;
						ptrasp.MontoPesoDeshabilitado		= true;
						ptrasp.UFTraspasoRequerido 			= true;
						ptrasp.MontoUFDeshabilitado			= true;
						ptrasp.PorcentajeTraspasoRequerido 	= true;
						ptrasp.PorcentajeDeshabilitado		= true;
					}
				}
			}
		};
		
		/******************************************************/
		/*** WATCH											***/
		/******************************************************/				
		$scope.$watch('ptrasp.TipoTraspasoSeleccionado',function(newval,oldval){
			if(ptrasp.TipoTraspasoSeleccionado !== undefined){
				if(ptrasp.TipoTraspasoSeleccionado.Codigo === 'PAR'){
					ptrasp.MontoTraspasoRequerido 		= true;
					ptrasp.MontoPesoDeshabilitado		= true;
					ptrasp.UFTraspasoRequerido 			= true;
					ptrasp.MontoUFDeshabilitado			= true;
					ptrasp.PorcentajeTraspasoRequerido 	= true;
					ptrasp.PorcentajeDeshabilitado		= true;
				}
				else{
					ptrasp.MontoTraspasoRequerido 		= false;
					ptrasp.MontoPesoDeshabilitado		= ($scope.csaapv.OrigenTraspasoSeleccionado && $scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO') ? false : true;
					ptrasp.UFTraspasoRequerido 			= false;
					ptrasp.MontoUFDeshabilitado			= false;
					ptrasp.PorcentajeTraspasoRequerido 	= false;
					ptrasp.PorcentajeDeshabilitado		= false;
				}
					
			}
			ptrasp.kngMontoTraspaso = 0;
			ptrasp.kngUFTraspaso = 0;
			ptrasp.kngPorcentajeTraspaso = 0;
		});
		
		$scope.$watch('ptrasp.porcentajeTraspaso',function(newval,oldval){
			if(ptrasp.porcentajeTraspaso !== null){
				ptrasp.MontoTraspasoRequerido 		= false;
				ptrasp.MontoPesoDeshabilitado		= false;
				ptrasp.UFTraspasoRequerido 			= false;
				ptrasp.MontoUFDeshabilitado			= false;
			}	
			if(ptrasp.montoTraspaso === null || ptrasp.montoTraspaso === undefined)
			{
				if(ptrasp.uFTraspaso === null || ptrasp.uFTraspaso == undefined)
				{
					if(ptrasp.porcentajeTraspaso === null || ptrasp.porcentajeTraspaso === undefined)
					{
						ptrasp.MontoTraspasoRequerido 		= true;
						ptrasp.MontoPesoDeshabilitado		= true;
						ptrasp.UFTraspasoRequerido 			= true;
						ptrasp.MontoUFDeshabilitado			= true;
						ptrasp.PorcentajeTraspasoRequerido 	= true;
						ptrasp.PorcentajeDeshabilitado		= true;
					}
				}
			}
		});
	
		$scope.$watch('ptrasp.TipoAhorroSeleccionado',function(newval,oldval){
			if(typeof newval !== 'undefined' && newval){	
				if(ptrasp.TipoAhorroSeleccionado.Codigo !== 'APV'){
					ptrasp.FondoOrigenSeleccionado = undefined;
					ptrasp.FondoOrigenIdActual = false;
				}
			}	
		});
		
		$scope.$watch('ptrasp.EntidadRecaudadoraSeleccionado',function(newval,oldval){
			if(ptrasp.EntidadRecaudadoraSeleccionado != undefined){
				ptrasp.DocumentoIdentidadEntidadOrigen = ptrasp.EntidadRecaudadoraSeleccionado.DocumentoIdentidad; 
			}
		});

		$scope.$watch('ptrasp.gridOrigenTraspaso.dataSource.ds._data',function(newval,oldval){
			if(newval && parametrosVentana.modoVentana === 'M'){
				if(ptrasp.gridOrigenTraspaso.dataSource.view().length > 0){
					ptrasp.EntidadRecaudadora.idActual = ptrasp.gridOrigenTraspaso.dataSource.view()[0].CodigoEntidadRecaudadora;
					ptrasp.EntidadRecaudadoraIdActualDefecto = ptrasp.gridOrigenTraspaso.dataSource.view()[0].CodigoEntidadRecaudadora;
					ptrasp.TipoTraspasoIdActual = ptrasp.gridOrigenTraspaso.dataSource.view()[0].CodigoTipoTraspaso;
					ptrasp.TipoTraspasoIdActualDefecto = ptrasp.gridOrigenTraspaso.dataSource.view()[0].CodigoTipoTraspaso;
					//$scope.csaapv.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'
					ptrasp.RazonRetiroIdActual = ptrasp.gridOrigenTraspaso.dataSource.view()[0].RazonRetiro;
					ptrasp.habilitoDocumentoIdentidad = true;
					ptrasp.habilitoTipoTraspaso = true;
				}
			}
		});
		
		$scope.configuracionColumnas = function() {
			return {
				Instrumento				:{field: "Nemotecnico",				title: $translate.instant('UI_INSTRUMENTO'),			    		width: "75px",	align: "left"},
				Cantidad				:{field: "CantidadCustodia",		title: $translate.instant('UI_CANTIDAD'),							width: "100px",	align: "left"},
				PrecioMercadoCustodia	:{field: "PrecioMercadoCustodia",	title: $translate.instant('UI_PRECIO'),								width: "75px",	align: "left", template : " <label> #:  (util.bcs_formato(PrecioMercadoCustodia,'FMM','Precio','"+$rootScope.SesionIdMonedaSistema+"', '','')).valor #</label>"},
				Regimen					:{field: "Regimen",					title: $translate.instant('UI_REGIMEN'),							width: "75px",	align: "left"},
				CodigoPortafolio		:{field: "CodigoPortafolio",		title: $translate.instant('UI_PORTAFOLIO'),							width: "75px",	align: "left"},
			}     
		};
		
		$scope.getConfiguracionColumna = function(campo,extras) {
			if (!$scope.configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
			return _.extend( {}, $scope.configuracionColumnas()[campo], extras);
		};
		
		$scope.getSchemaCustodia = function(){
			return {
				model : {
					fields : {
						Nemotecnico			  : { type : "string"},
						CantidadCustodia	  : { type : "number"},
						IdInstrumento		  : { type : "number"},
						PrecioMercadoCustodia : { type : "number"},
						Regimen				  : { type : "string"},
						CodigoPortafolio	  : { type : "string"},
						IdPortafolio          : { type : "number"}
					}
				}
			}
		};
		
		$scope.getDataSourceCustodia = function () {
			return new bcs.DataSource({
				type : "array",
				definition : {
					schema : $scope.getSchemaCustodia()			
				}
			})
		};
		
		$scope.getCustodia = function(){
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
		
						dataSource: $scope.getDataSourceCustodia(),
					
						columns: [
								   $scope.getConfiguracionColumna("Instrumento")
						          ,$scope.getConfiguracionColumna("Cantidad")
						          ,$scope.getConfiguracionColumna("PrecioMercadoCustodia")
						          ,$scope.getConfiguracionColumna("Regimen")
						          ,$scope.getConfiguracionColumna("CodigoPortafolio")
						]
					}
			};
			return config;
		};
		
		var dataSourceCustodia = $scope.getCustodia();
		$scope.custodiaOptions = dataSourceCustodia.options;
		
		$scope.buscarCustodia = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			var filtersCustodia = { 
					p_IdEmpresa 						: $scope.SesionIdEmpresa,				
					p_IdUsuario         				: $scope.SesionIdUsuario,
					p_IdModulo							: 'APV',
					p_IdCliente							: $scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente,
					p_IdPortafolio						: $scope.ptrasp.oPortafolio.seleccionado.IdPortafolio,
					p_IdClasificacionTipoInstrumento	: '',
					p_IdTipoInstrumento					: '',
					p_IdInstrumento						: 0,
					p_IdBoveda							: '',
					p_AgrupaPorBoveda					: 'S',
					p_AgrupaPorRelacion					: 'N',
					p_FechaConsulta						: '0001-01-01',
					p_TipoFechaConsulta					: 'LIQ',
					p_TipoAhorro						: $scope.ptrasp.TipoAhorroSeleccionado.Codigo,
					p_IdRegimen							: ($scope.ptrasp.RegimenAgrupadorSeleccionado.IdRegimenTributario) ? $scope.ptrasp.RegimenAgrupadorSeleccionado.IdRegimenTributario : 0, 
					p_IdProducto						: 0,
					p_IdPlan							: 0
				};	
			
			if($scope.csaapv.datosForm.SocioNegocioSeleccionado.IdCliente != undefined){
				bcsServiciosAPV.ConsultarCustodiaPortafolioAPV(filtersCustodia).$promise.then(function(respuesta){
					custodia = Array();
					if (respuesta.P_OUT_CUSTODIA.length > 0){
						for(var i=0; i < respuesta.P_OUT_CUSTODIA.length ; i++){
							custodia.push(respuesta.P_OUT_CUSTODIA[i]);	
						}
					} 
					$scope.custodiaOptions.dataSource.data(custodia);
					
					bcsComunes.ventanaOverlay(idVentana,false);
				});
			}else{
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_CLIENTE"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
		};
		
		$scope.$watch('$viewContentLoaded', function(){
			if(parametrosVentana.modoVentana === 'M'){
				
				ptrasp.EntidadRecaudadora.idActual = parametrosVentana.DocumentoIdentidadOrigen;
				ptrasp.EntidadRecaudadoraIdActualDefecto = parametrosVentana.DocumentoIdentidadOrigen;
				ptrasp.habilitoDocumentoIdentidad = false;
				ptrasp.habilitoTipoTraspaso = false;
				
			}
		});
		
		/******************************************************/
		/*** FINAL											***/
		/******************************************************/
	});
}());