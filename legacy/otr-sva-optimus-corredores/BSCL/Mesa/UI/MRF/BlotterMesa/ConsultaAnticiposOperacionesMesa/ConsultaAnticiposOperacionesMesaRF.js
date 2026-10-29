angular.module('ModuloMesa').controller('ConsultarAnticiposOperacionesMesaRFCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa,$http,bcsServiciosGrillasMesaRF,bcsAlerta,OPCIONES_BCSALERTA)
{	
	var conAnticipo = this;
	$scope.show = false;
	$scope.dataAnticipo = {};
	$scope.dataTemplate	= {};
	$scope.dataTemplate2= {};

	//$scope.Estado = '';
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
	// --------------------------------------------------
	// CONFIGURACION DE GRILLAS
	// --------------------------------------------------
	var promise = Promise.resolve(bcsServiciosGrillasMesaRF.getConfiguracionOperacionesMesaRF_Anticipos());
	promise.then(function(confGridOperaciones){
		$scope.dataAnticipo.confBasicaOperacionesMesaRF = confGridOperaciones.options;	
		$scope.dataAnticipo.buscarOperacionesMesaRF = confGridOperaciones.buscar;
		$scope.show = true;
	});
	
	var dataSourceConsultarEstadoMesa = bcsServiciosGrillasMesaRF.getDataSourceConsultarEstadoMesa();	
	var fechaAnticipos = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	
	$scope.NombreArchivo = "ConsultaAnticiposOperacionesMesaRF_"+$scope.SesionFechaSistema;
	$scope.dataAnticipo.SesionIdEmpresa = $scope.SesionIdEmpresa;

	$scope.dataAnticipo.HabilitaBotones = true;
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.dataAnticipo.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};
	
	$scope.dataAnticipo.Modo = 'ANT';
	
	
	$scope.blotterOpMesa.ANTBUSCA = function(){	
		$scope.dataAnticipo.buscarOperacionesMesaRF($scope.dataAnticipo.gridApiOperacionesMesaRF,$scope.dataAnticipo);		
	};
	
	$scope.dataAnticipo.cargaGrillas = function(){
		$scope.dataAnticipo.buscarOperacionesMesaRF($scope.dataAnticipo.gridApiOperacionesMesaRF,$scope.dataAnticipo);
		$scope.blotterOpMesa.OPEBUSCA();
		$scope.blotterOpMesa.VENBUSCA();
	};

	$scope.dataAnticipo.btnBuscar = function(){
		if( $scope.dataAnticipo.kngFechaOperacionFin < $scope.dataAnticipo.kngFechaOperacion ){
			bcsComunes.msg("error","error", 'Rango de fecha INVALIDO');
		} else {
			$scope.dataAnticipo.buscarOperacionesMesaRF($scope.dataAnticipo.gridApiOperacionesMesaRF,$scope.dataAnticipo);
		}
	};
	
	//DETALLE OPERACION VENTANA
	$scope.dataAnticipo.detalleOperacion = function(datos){
		bcsComunes.ventanaAbrir("DetalleOperacionMesaRF", {
					IdOperacion: $scope.dataAnticipo.seleccionOperacionesMesaRF[0].IdOperacion,
					IdTipoProducto : $scope.dataAnticipo.seleccionOperacionesMesaRF[0].IdTipoProducto,
				},false);
	};
	

	//ASIGNACION DE FECHAS A DATAPICKER
	$scope.dataAnticipo.kngFechaOperacion = fechaAnticipos;
	$scope.dataAnticipo.kngFechaOperacionFin = util.dateDS(util.dateAddMonths(util.date($scope.SesionFechaSistema),1),"FechaSistema","CALL");

	$scope.dataAnticipo.oConfiguracionFechaDesde = {
			min: util.dateInicioDia($scope.SesionFechaSistema)
	};
	$scope.dataAnticipo.oConfiguracionFechaHasta = {
			min: $scope.dataAnticipo.kngFechaOperacion
	};
	
	$scope.$watch('dataAnticipo.kngFechaOperacion',function(newval,oldval) {    
		if(newval && newval !== oldval){
			if( newval > $scope.dataAnticipo.kngFechaOperacionFin ){
				$scope.dataAnticipo.kngFechaOperacionFin = $scope.dataAnticipo.kngFechaOperacion;
			}
			$scope.dataAnticipo.oConfiguracionFechaHasta = {
				min: $scope.dataAnticipo.kngFechaOperacion
			};
		}
    });

	$scope.$watch('dataAnticipo.kngFechaOperacionFin',function(newval,oldval) {    
		if(newval && newval !== oldval){
			if( newval < $scope.dataAnticipo.kngFechaOperacion ){
				$scope.dataAnticipo.oConfiguracionFechaDesde = {
						min: $scope.dataAnticipo.kngFechaOperacionFin
				};
				$scope.dataAnticipo.kngFechaOperacion = $scope.dataAnticipo.kngFechaOperacionFin;
			}
		}
    });
	/*
	 *  WATCH CAMBIO DE MESA
	 * */

	$scope.$watch('dataAnticipo.oConfiguracionMesas.seleccionado', function(newval,oldval ){
		if ($scope.dataAnticipo.oConfiguracionMesas.seleccionado) {
			$scope.blotterOpMesa.IdMesa = $scope.dataAnticipo.oConfiguracionMesas.seleccionado.IdMesa; 
			$scope.blotterOpMesa.ANTBUSCA();
		}
		
	});
	$scope.$watch('blotterOpMesa.IdMesa', function(newval,oldval ){
		if ($scope.blotterOpMesa.IdMesa) {
			$scope.dataAnticipo.oConfiguracionMesas.idActual = $scope.blotterOpMesa.IdMesa;
		}
		
	});
	
	
	// --------------------------------------------------
	// INGRESO DE ANTICIPO OPERACION
	// --------------------------------------------------
	$scope.crearAnticipoOperacion = function() {
		var datos = {};
		var fechaOperacion = '';
		if ($scope.dataAnticipo.seleccionOperacionesMesaRF === undefined || $scope.dataAnticipo.seleccionOperacionesMesaRF.length != 1 )
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_OPERACION'));		
		}
		else
		{	
			$scope.Estado = $scope.dataAnticipo.seleccionOperacionesMesaRF[0].IdEstado;
			fechaOperacion = util.dateSPdate($scope.dataAnticipo.seleccionOperacionesMesaRF[0].FechaOperacion,"FechaSistema","CALL");
			if ( $scope.Estado !== "VIG" ) {
				bcsComunes.msg("warning","info",$translate.instant('UI_OPERACION_INVALIDA'));
			}
			else
			{
				dataSourceConsultarEstadoMesa.query({filter : {field:"IdMesa", operator: "eq", value: Number($scope.dataAnticipo.oConfiguracionMesas.idActual)}}).then(function(){
					var data = dataSourceConsultarEstadoMesa.data();
					if (data.length > 0){
						if (data[0].TipoEstadoModulo === 'C'){
							bcsComunes.msg("warning","info",$translate.instant('UI_MODULO_CERRADO'));
							return;
						}
						if (data[0].IdEstadoMesa === 'C'){
							bcsComunes.msg("warning","info",$translate.instant('MESA_RF_CERRADA'));
							return;
						}
						if (data[0].FechaActualSistema !== $scope.SesionFechaSistema){
							
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse({P_OUT_ERRORESDETALLE : [{Codigo:'FECHA_PROCESO_INVALIDA', Descripcion:$translate.instant('FECHA_PROCESO_INVALIDA')}]})
							},$scope.winID);
							return;
						}
						$scope.dataAnticipo.HabilitaBotones = false;
						var id1=bcsComunes.ventanaAbrir("ModificaOperacionMesaRF", 
								{Operacion : $scope.dataAnticipo.seleccionOperacionesMesaRF[0],
								 Accion: "ANTICIPAR"
								},false,false,null,false, {title : $translate.instant('UI_ANTICIPAR_OPERACION')})
						var promesa=bcsComunes.ventanaPromesa(id1);
								
				   		$.when(promesa).then(function(data){
				   			$scope.dataAnticipo.HabilitaBotones = true;
				   			$scope.dataAnticipo.cargaGrillas();
				      	});
					}
				});
			
			};
		};
		
	};
	var ConsultaTemplate = new bcs.newDataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=450",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdTemplateMesa	: {type: "string"},
						IdReporte		: {type: "string"},
						NombreReporte	: {type: "string"},
						TipoReporte		: {type: "string"},
						ServicioReporte	: {type: "string"},
						IdEmpresa		: {type: "number"},
						IdEstado		: {type: "string"}
					}
				}
			}
		},
	});
	
	$scope.generarReporteAnticipoOperacion = function(){
		
		var subReports = [];
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'PAPELETA_ANTICIPO_PACTOS' },//Guardar con este nombre el reporte en la tabla template
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		
		
		var ServiceDataReporte= {
	       	 	"p_IdEmpresa"			: $scope.SesionIdEmpresa
			    ,"p_IdModulo"			: 'MRF'
				,"p_IdPais"				: $scope.$root.SesionIdPaisSistema
				,"p_IdOperacion"		: $scope.dataAnticipo.seleccionOperacionesMesaRF[0].IdOperacion
				,"$schema"				: [{ "field" : "LogoCorredora", "type" : "blob" }]
	       };

		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
				template = {
							TipoReporte		: data[0].TipoReporte//'pdf' 
							,IdReporte		: data[0].IdReporte
							,NombreReporte	: $translate.instant(data[0].NombreReporte)
							,data			: { "serviceId" 	: data[0].ServicioReporte,
												"serviceData" 	: ServiceDataReporte
												}
				};
			/*	subReports.push({ "reportName" 	: data.IdTemplateMesaSubreporte
         			 ,"data" 		: { "serviceId": data.ServicioReporte, "serviceData": ServiceDataReporte }
				});*/
				
			
			
			
		template.subReports = subReports;
		console.log(template);
		$scope.getReport(template);
		
		
			
		});
	};
	
	$scope.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": {}
				},
				"subReports": []
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				//var fileURL = URL.createObjectURL(file);
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'')
				nombreArchivo = !dataTemplate.idReporte ? nombreArchivo+'_'+$scope.dataAnticipo.seleccionOperacionesMesaRF[0].IdOperacion:nombreArchivo;
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				//bcsComunes.ventanaMsg({
				//                     conflictos:util.parseServerResponse(response)
				//                   },$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
		
	};

var array = [
		{ Estado: 'Vigente', Color: 'black' },
		{ Estado: 'Anticipado', Color: 'light-green-font' }

	];
	
		bcsComunes.tootipGlosa('toolConsultarAnticiposOperacionesMesaRF', array)	

	
});