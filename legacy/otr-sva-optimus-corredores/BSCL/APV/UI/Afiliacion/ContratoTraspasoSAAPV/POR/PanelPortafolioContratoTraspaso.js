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
		
		ptrasp.HabilitoCampoTraspaso = false;
		ptrasp.MontoPesoOrigenRequerido = true;
		ptrasp.MontoUFOrigenRequerido = true;
		ptrasp.PorcentajeOrigenRequerido = true;
		ptrasp.datosForm ={};
		$scope.ctrasp.ptrasp = ptrasp;
		ptrasp.habilitarAgregar = true;
		
		/******************************************************/
		/*** FUNCIONES DE CARGA DE TABLAS					***/
		/******************************************************/
		var defOrigenTraspaso = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 100,
                sort 			: false,
				schema: {
					model: {
						fields: {
							NombreEntidad				: { type: "string"},
							DocumentoIdentidad			: { type: "string"},
							TipoTraspaso				: { type: "string"},
							TipoAhorro					: { type: "string"},
							FondoOrigen					: { type: "string"},
							MontoTraspaso				: { type: "number"},
							UFTraspaso					: { type: "number"},
							PorcentajeTraspaso			: { type: "number"}
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
				{field: "TipoTraspaso"		,title: $translate.instant('UI_TIPO_TRASPASO')		, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "TipoAhorro"		,title: $translate.instant('UI_TIPO_AHORRO')		, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "NombreEntidad"		,title: $translate.instant('UI_NOMBRE')				, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "DocumentoIdentidad",title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "FondoOrigen"		,title: $translate.instant('UI_FONDO_ORIGEN')		, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "MontoTraspaso"		,title: $translate.instant('UI_MONTO_PESOS')		, width: "80px", align: "left"  , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoTraspaso,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "UFTraspaso"		,title: $translate.instant('UI_MONTO_UF')			, width: "80px", align: "left"  , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(UFTraspaso,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "PorcentajeTraspaso",title: $translate.instant('UI_PORCENTAJE')			, width: "80px", align: "rigth" , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(PorcentajeTraspaso,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
			],	
			sortable		: false,
			pageable		: true,
			autoBind		: true,
			height			: 100,
			selectable 		: "row",
			userSettings	: true,
			navigatable		: true,
			gridCreated		: function(grid){
				gridOrigenTraspaso = grid;
			}
		};
		
		/******************************************************/
		/*** FUNCIONES										***/
		/******************************************************/
		ptrasp.fnAgregoOrigenTraspaso = function()
		{
			var idVentana = bcsComunes.ventanaWinId($scope);
			bcsComunes.ventanaOverlay(idVentana,true);

			if(ptrasp.IdentidadRecaudadoraIdActual === false || ptrasp.IdentidadRecaudadoraIdActual === undefined){
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_ENTIDAD_RECAUDADORA"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			};
			
			if(ptrasp.TipoTraspasoIdActual === false || ptrasp.TipoTraspasoIdActual === undefined){
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_TIPO_TRASPASO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			};
			
			if(ptrasp.TipoAhorroIdActual === false || ptrasp.TipoAhorroIdActual === undefined){
				bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_TIPO_AHORRO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			};
			
			if($scope.ctrasp.OrigenTraspasoSeleccionado.Nombre === 'INGRESO'){
				if(ptrasp.FondoOrigenIdActual === false || ptrasp.FondoOrigenIdActual === undefined){
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_FONDO_ORIGEN"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				};	
			}else if($scope.ctrasp.OrigenTraspasoSeleccionado.Nombre === 'EGRESO'){
				if(ptrasp.RazonRetiroIdActual === false || ptrasp.RazonRetiroIdActual === undefined){
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_SELECCION_RAZON_RETIRO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				};
			}
			
			
			if((ptrasp.TipoTraspasoIdActual !== false || ptrasp.TipoTraspasoIdActual !== undefined) && ptrasp.TipoTraspasoIdActual === 'PARCIAL'){
				if(ptrasp.kngMontoTraspaso === undefined && ptrasp.kngUFTraspaso === undefined && ptrasp.kngPorcentajeTraspaso === undefined)
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_INGRESE_MONTO"));
					bcsComunes.ventanaOverlay(idVentana,false);
					return;
				}
			};
			
			fnAgregoOrigenTraspaso();

			bcsComunes.ventanaOverlay(idVentana,false);
		};
		
		ptrasp.LimpioVariablesOrigen = function()
		{	
			ptrasp.kngPorcentajeTraspaso 			= undefined;
			ptrasp.kngUFTraspaso 					= undefined;
			ptrasp.kngMontoTraspaso 				= undefined;
			ptrasp.FondoOrigenIdActual 				= undefined;
			ptrasp.TipoAhorroIdActual 				= undefined;
			
			if(ptrasp.dsOrigenTraspaso === undefined || ptrasp.dsOrigenTraspaso.data().length === 0)
			{
				ptrasp.DocumentoIdentidadEntidadOrigen	= undefined;
				ptrasp.IdentidadRecaudadoraIdActual		= undefined;
				ptrasp.TipoTraspasoIdActual				= undefined;
				ptrasp.habilitoDocumentoIdentidad 		= true;
				ptrasp.habilitoTipoTraspaso				= true;
			}else{
				ptrasp.habilitoDocumentoIdentidad	 	= false;
				ptrasp.habilitoTipoTraspaso				= false;
			}
		};
		
		fnAgregoOrigenTraspaso = function ()
		{
			ptrasp.dsOrigenTraspaso.add({
				NombreEntidad				: ptrasp.EntidadRecaudadoraSeleccionado.Nombre,
				DocumentoIdentidad			: ptrasp.DocumentoIdentidadEntidadOrigen,
				TipoTraspaso				: ptrasp.TipoTraspasoSeleccionado.Nombre,
				TipoAhorro					: ptrasp.TipoAhorroSeleccionado.Codigo,
				FondoOrigen					: ptrasp.FondoOrigenIdActual,
				MontoTraspaso				: ptrasp.kngMontoTraspaso,
				UFTraspaso					: ptrasp.kngUFTraspaso,
				PorcentajeTraspaso			: ptrasp.kngPorcentajeTraspaso
			});	
			
			ptrasp.LimpioVariablesOrigen();
		};
			
		ptrasp.fnEliminoOrigenTraspaso = function(){
			if(ptrasp.origenTraspasoSeleccionado === undefined)
				return;
			
			ptrasp.dsOrigenTraspaso.remove(ptrasp.origenTraspasoSeleccionado);
			if(ptrasp.dsOrigenTraspaso === undefined || ptrasp.dsOrigenTraspaso.data().length === 0)
			{
				ptrasp.LimpioVariablesOrigen();
			}
		};
				
		ptrasp.Requerido = function(tipo)
		{
			if(tipo === 'MONTO')
			{
				if(ptrasp.kngMontoTraspaso !== null)
				{
					ptrasp.UFTraspasoRequerido = false;
					ptrasp.kngUFTraspaso = 0;
					ptrasp.PorcentajeTraspasoRequerido = false;
					ptrasp.kngPorcentajeTraspaso = 0;
				}
			}
			else if(tipo === 'UF')
			{
				if(ptrasp.kngUFTraspaso !== null)
				{
					ptrasp.MontoTraspasoRequerido = false;
					ptrasp.kngMontoTraspaso = 0;
					ptrasp.PorcentajeTraspasoRequerido = false;
					ptrasp.kngPorcentajeTraspaso = 0;
				}
			}else{
				if(ptrasp.kngPorcentajeTraspaso !== null){
					if(ptrasp.kngPorcentajeTraspaso > 100) ptrasp.kngPorcentajeTraspaso = 100;
					ptrasp.MontoTraspasoRequerido = false;
					ptrasp.kngMontoTraspaso = 0;
					ptrasp.UFTraspasoRequerido = false;
					ptrasp.kngUFTraspaso = 0;
				}
			}	
			
			if(ptrasp.kngMontoTraspaso === null || ptrasp.kngMontoTraspaso === undefined)
			{
				if(ptrasp.kngUFTraspaso === null || ptrasp.kngUFTraspaso == undefined)
				{
					if(ptrasp.kngPorcentajeTraspaso === null || ptrasp.kngPorcentajeTraspaso === undefined)
					{
						ptrasp.MontoTraspasoRequerido = true;
						ptrasp.UFTraspasoRequerido = true;
						ptrasp.PorcentajeTraspasoRequerido = true;
					}
				}
			}
		};
		
		/******************************************************/
		/*** WATCH											***/
		/******************************************************/				
		$scope.$watch('ptrasp.TipoTraspasoSeleccionado',function(newval,oldval){
			if(ptrasp.TipoTraspasoSeleccionado != undefined)
				if(ptrasp.TipoTraspasoSeleccionado.Codigo === 'PAR')
					ptrasp.HabilitoCampoTraspaso = true;
				else
					ptrasp.HabilitoCampoTraspaso = false;
					
			ptrasp.kngMontoTraspaso = 0;
			ptrasp.kngUFTraspaso = 0;
			ptrasp.kngPorcentajeTraspaso = 0;
		});
		
		$scope.$watch('ptrasp.EntidadRecaudadoraSeleccionado',function(newval,oldval){
			if(ptrasp.EntidadRecaudadoraSeleccionado != undefined){
				ptrasp.DocumentoIdentidadEntidadOrigen = ptrasp.EntidadRecaudadoraSeleccionado.DocumentoIdentidad; 
			}
		});
		
		/******************************************************/
		/*** FINAL											***/
		/******************************************************/
	});
}());