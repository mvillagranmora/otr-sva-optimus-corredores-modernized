/********************************************************************
 * Purpose	: Controlador para administrar Portafolios de un cliente
 * Author	: JGarces - BCS
 * Date		: 25/11/2015
 *******************************************************************/

(function() {
	angular.module('ModuloPortafolio').controller('AdministrarPortafolioCtrl',
		function ($state, $q, $scope, $compile, bcsComunes, $translate, $timeout, bcsAlerta, OPCIONES_BCSALERTA){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var admPort = this;
		admPort.datos = {};
		admPort.datosAPV = {};
		admPort.ds = {};
		admPort.cmbbx = {};
		admPort.idVentana = bcsComunes.ventanaWinId($scope); 
		admPort.params = bcsComunes.ventanaParams(admPort.idVentana);
		admPort.domVentana = $("[ventanaid='"+admPort.idVentana+"']");
		admPort.params.IdCliente = (admPort.params.IdCliente) ? admPort.params.IdCliente : 0 ; 
		admPort.params.IdPortafolio = (admPort.params.IdPortafolio) ? admPort.params.IdPortafolio : 0;
		admPort.socioNegocioPromise = admPort.params.socioNegocioPromise;
		admPort.dataSocioNegocio = admPort.params.dataSocioNegocio;
		bcsComunes.ventanaFrente(admPort.idVentana);
		//PRONET-IT
		//12-02-2018
		var filterVentana = Array();
		//FIN PRONET-IT
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
		admPort.tabs = {
			Portafolio: {
				PanelPortafolioDatosGenerales: { id: 'PanelPortafolioDatosGenerales', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioDatosGenerales.html\'" ></div>', active: true, cargado: false, datosCambiados: false },
				PanelPortafolioPreferenciasDeNegocio: { id: 'PanelPortafolioPreferenciasDeNegocio', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPreferencias.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioIndicadores: { id: 'PanelPortafolioIndicadores', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioIndicadores.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioNotificaciones: { id: 'PanelPortafolioNotificaciones', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioNotificaciones.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioRequisitos: { id: 'PanelPortafolioRequisitos', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioRequisitos.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioAutorizaciones: { id: 'PanelPortafolioAutorizaciones', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioAutorizaciones.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioAnotaciones: { id: 'PanelPortafolioAnotaciones', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioAnotaciones.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioPreferenciasCartola: { id: 'PanelPortafolioPreferenciasCartola', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPreferenciasCartola.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioCuentaCustodio: { id: 'PanelPortafolioCuentaCustodio', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioCuentaCustodio.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioLineaSobregiro: { id: 'PanelPortafolioLineaSobregiro', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioLineaSobregiro.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
			},
			PerfilComercial: {
				PanelPortafolioPerfilComercial: { id: 'PanelPortafolioPerfilComercial', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPerfilComercial.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioPerfilProductos: { id: 'PanelPortafolioPerfilProductos', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaProductos.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioPerfilCobros: { id: 'PanelPortafolioPerfilCobros', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaCobros.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioPerfilCobrosPeru: { id: 'PanelPortafolioPerfilCobrosPeru', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaCobrosPeru.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioPlantillaLimites: { id: 'PanelPortafolioPlantillaLimites', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPlantillaLimites.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
			},
			PrefPago: {
				PanelPortafolioPrefPagoRecibe: { id: 'PanelPortafolioPrefPagoRecibe', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPreferenciasPagoRecibe.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				PanelPortafolioPrefPagoPaga: { id: 'PanelPortafolioPrefPagoPaga', url: '<div ng-include="\'../../Portafolio/UI/AdministrarPortafolio/PanelPortafolioPreferenciasPagoPaga.html\'" ></div>', active: false, cargado: false, datosCambiados: false },
				
			}
		};
		
		admPort.fnTabSelect = function (ambito, nombreTab) {
			if (admPort.tabs[ambito][nombreTab].cargado !== true) {
				admPort.tabs[ambito][nombreTab].cargado = true;
				admPort.domVentana.find('#'+admPort.tabs[ambito][nombreTab].id).append($compile(admPort.tabs[ambito][nombreTab].url)($scope));
			}
		};
		
		admPort.revisarCambiosOtrosTabs = function () {
			_.each(admPort.tabs,function (tabs, ambito, ) {
				_.each(admPort.tabs[ambito],function (value, nombreTab, ) {
					if (value.datosCambiados) {
						admPort.fnPreguntaCambioDeTab(ambito, nombreTab)
					}
				})
			})
		};
		
		admPort.MaintabSelect = function(ambito) {
			switch (ambito) {
				case 'Portafolio':
					admPort.tabs['Portafolio']['PanelClienteDatosGenerales'].active = true;
					admPort.fnTabSelect('Portafolio', 'PanelPortafolioDatosGenerales');
					break;
				case 'PerfilComercial':
					admPort.tabs['PerfilComercial']['PanelPortafolioPerfilComercial'].active = true;
					admPort.fnTabSelect('PerfilComercial', 'PanelPortafolioPerfilComercial');
					break;
				case 'PrefPago':
					admPort.tabs['PrefPago']['PanelPortafolioPrefPagoRecibe'].active = true;
					admPort.fnTabSelect('PrefPago', 'PanelPortafolioPrefPagoRecibe');
					break;
			}

		};
		
		admPort.fnTabDeselect = function (ambito, nombreTab) {
			if (admPort.tabs[ambito][nombreTab].datosCambiados === true) admPort.fnPreguntaCambioDeTab(ambito, nombreTab);
		};
		admPort.fnPreguntaCambioDeTab = function (ambito, nombreTab) {
			var t = bcsAlerta.crear('PREGUNTA_SI_NO', $translate.instant('UI_PREGUNTA_GUARDAR'));
			t.result.then(function (result) {
				if (result === OPCIONES_BCSALERTA.NO) {
					admPort.fnCambioDeTab(ambito, nombreTab);
				} else {
					$scope.$emit('DatosCambiadosGuardados', { ambito: ambito, nombreTab: nombreTab });
				}
			});
		};
		
		admPort.fnCambioDeTab = function (ambito, nombreTab) {
		admPort.tabs[ambito][nombreTab].active=true; 
			$timeout(function(e) {
				$.each(admPort.tabs[ambito],function(key,value) { value.active=false; });
				admPort.tabs[ambito][nombreTab].active=true; 
			},0);
		}
     	
		$scope.$on('HayDatosSinGuardar', function (evt, data) {
			admPort.tabs[data.ambito][data.nombreTab].datosCambiados = true;
			console.log(`ON DATOS sin guardar ${data.ambito} ${data.nombreTab} datosCambiados: ${admPort.tabs[data.ambito][data.nombreTab].datosCambiados}`);

		});

		$scope.$on('DatosCambiadosGuardados', function (evt, data) {
			admPort.tabs[data.ambito][data.nombreTab].datosCambiados = false;
			console.log(`ON DATOS GUARDADOS ${data.ambito} ${data.nombreTab} datosCambiados: ${admPort.tabs[data.ambito][data.nombreTab].datosCambiados}`);
		});
     	
/*********************************************
 * DATASOURCES
 *********************************************/

     	admPort.ds.PortafolioDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3518",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPortafolio 	: { type: "number"},
							IdCliente		: { type: "number"},
							IdPersona		: { type: "number"},	
						}
					}
				}
			}
		});
     	
/*********************************************
 * COMBOBOX
 *********************************************/
     	admPort.cmbbx.oConfiguracionCategoriaInversionista={
			tipo : "combobox-categoria-inversionista",
			configuracion : {},
			filtros : { },
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		     	
/*********************************************
 * FUNCIONES
 ********************************************/
 
 		admPort.iniciar = function(){
 			if(admPort.params.IdCliente !== undefined && (admPort.params.IdPortafolio===undefined || String(admPort.params.IdPortafolio)==='0')) bcsComunes.ventanaCerrar(admPort.idVentana);
 			
 			/*admPort.datos.IdCategoriaInversionista  	= 'PROPGENERALES';
 			admPort.datos.IdCategoriaInversionistaDef 	= 'PROPGENERALES';*/
			
			if(admPort.params.IdCliente !== undefined && admPort.params.IdPortafolio !== undefined) {
				var filtroConsultaPortafolio = [ { field: "IdPortafolio", operator: "eq", value: Number(admPort.params.IdPortafolio) } ];
				admPort.portafolioPromise = admPort.ds.PortafolioDataSource.query({filter:filtroConsultaPortafolio}).then(function(){
					if(admPort.ds.PortafolioDataSource.data().length>0){
						admPort.datos = _.clone(admPort.ds.PortafolioDataSource.data()[0]);
						admPort.datos.IdCategoriaInversionistaDef = admPort.ds.PortafolioDataSource.data()[0].IdCategoriaInversionista;
						admPort.datos.IdCategoriaInversionista = admPort.ds.PortafolioDataSource.data()[0].IdCategoriaInversionista;
						admPort.datos.IdTipoClienteDef = admPort.ds.PortafolioDataSource.data()[0].IdTipoCliente;
						
						//DocumentoIdentidadFormateado:
						admPort.datos.DocumentoIdentidadFormateado = admPort.datos.DocumentoIdentidad;
						if(admPort.datos.TipoDocumentoIdentidad==='R')
							admPort.datos.DocumentoIdentidadFormateado = util.formatRut(admPort.datos.DocumentoIdentidadFormateado);
						
						/*Busca si existe un proposito de tipo APV, hasta que se regularize el Nombre*/
						admPort.datos.APV = 'NOAPV';
						if(admPort.datos.Proposito.indexOf('APV') !== -1){
							admPort.datos.APV = 'APV';
						}					
						/**/
						
						bcsComunes.ventanaTitulo(admPort.idVentana, admPort.datos.NombreCompleto + ' - ' + $translate.instant('UI_PORTAFOLIO') + ' ' + admPort.params.CodigoPortafolio );
					}
					else {
						bcsComunes.msg("error","PORTAFOLIO",'Portafolio No existe');
						bcsComunes.ventanaCerrar(admPort.idVentana);
					}
					return true;
				});
			}
			else {
				bcsComunes.msg("error","PORTAFOLIO",'Datos no existe');
				bcsComunes.ventanaCerrar(admPort.idVentana);
			}
		};
		$scope.verFichaCliente = function(){
			bcsComunes.ventanaAbrir("administrarSocioNegocio", {IdCliente : admPort.params.IdCliente, Entidad: "CLI"},false);
		}
		$scope.verResumenIneversiones = function(){
			bcsComunes.ventanaAbrir("ResumenInversionCliente", {IdCliente : admPort.params.IdCliente, IdPortafolio: admPort.params.IdPortafolio},false);
		}
		//PRONET-IT
		//18-02-2018
		$scope.verContrato = function(){
			
			if ($scope.admPort.pPortDG.FolioContratoSeleccionado.IdContratoSAAPV === undefined){
				bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBE SELECCIONAR_CONTRATO'));
				return;
			}else{
				filterVentana.IdContratoSAAPV 		= $scope.admPort.pPortDG.FolioContratoSeleccionado.IdContratoSAAPV;
				filterVentana.DocumentoIdentidad	= admPort.datos.DocumentoIdentidadFormateado;
				filterVentana.modoVentana 			= 'M';
				var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, admPort.idVentana);
			}
		}
		//FIN PRONET-IT
/*********************************************
 * GOOOO!!!!
 ********************************************/
		admPort.iniciar();
		
	});
}());
