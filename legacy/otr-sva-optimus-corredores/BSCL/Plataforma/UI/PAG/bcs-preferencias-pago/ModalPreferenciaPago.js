/********************************************************************
 * Purpose	: Controlador para crear/modificar Preferencia de Pago
 * 			  en el portafolio.
 * Author	: JGarces - BCS
 * Date		: 21/03/2016
 *******************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('ModalPreferenciaPagoCtrl',
	function($state,$q,$scope,$translate,bcsComunes,servicioPreferenciasPago,bcsComunes){
	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var mPrefPago 				= this;
		mPrefPago.idVentana 		= bcsComunes.ventanaWinId($scope);
		mPrefPago.parametros 		= bcsComunes.ventanaParams(mPrefPago.idVentana);
		mPrefPago.domVentana 		= $("[ventanaid='"+mPrefPago.idVentana+"']");	
		mPrefPago.win 				= {};
		mPrefPago.datos 			= {};
		mPrefPago.cmbbx 			= {};
		mPrefPago.ds 				= {};
		mPrefPago.ktv 				= {};
		mPrefPago.fn 				= {};
		mPrefPago.datosAntes 		= {};
		mPrefPago.datosDespues		= {};
		var overlay 				= bcsComunes.overlay;
		mPrefPago.win.modoEdicion 	= false;
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/
		
		if(!mPrefPago.parametros.RecibePagaPortafolio) {
			bcsComunes.msg("error","Abrir Ventana",'No se reciben parámetros iniciales');
			bcsComunes.ventanaCerrar(mPrefPago.idVentana);
		}

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/

		mPrefPago.win.guardar = function(){
			var dataPreferenciaPago = mPrefPago.fn.datosGuardar();
			if(mPrefPago.win.modoEdicion) { // EDITAR
				if(dataPreferenciaPago) {
					servicioPreferenciasPago.updatePreferenciaPago(dataPreferenciaPago,function(result){
						var salida = {};
						console.log(JSON.stringify(result));
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0) {
							bcsComunes.msg("info",'Modificar Preferencia Pago','Preferencia creada');
							bcsComunes.ventanaCerrar(mPrefPago.idVentana);
						}
						else {
							bcsComunes.msg("error",'Modificar Preferencia Pago','error al modificar preferencia');
							bcsComunes.ventanaCerrar(mPrefPago.idVentana);
						};
					},
					function(error){
						bcsComunes.msg("error",'Modificar Preferencia Pago','Error');
						console.log(error);
					});
				}
			}
			else { // CREAR
				if(dataPreferenciaPago) {
					servicioPreferenciasPago.savePreferenciaPago(dataPreferenciaPago,function(result){
						var salida = {};
						console.log(JSON.stringify(result));
						bcsComunes.ventanaCerrar(mPrefPago.idVentana);
						bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_PREFERENCIA_PAGO'),
							conflictos : util.parseServerResponse(result)}, mPrefPago.idVentana 	);
						
						bcsComunes.setConflictos(util.parseServerResponse(result));
						
					},
					function(error){
						bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_PREFERENCIA_PAGO'),
							conflictos : util.parseServerResponse(error)}, mPrefPago.idVentana 	);
						
						bcsComunes.setConflictos(util.parseServerResponse(error));
						//bcsComunes.msg("error",'Crear Preferencia Pago','Error');
						console.log(error);
					});
				}
			}
		};
		
		mPrefPago.win.cancelar = function(){
			bcsComunes.ventanaCerrar(mPrefPago.idVentana);
		};

		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		mPrefPago.ds.listaOperaciones = new bcs.DataSource({
			type			: "odata",
			definition 		: {
			  	serverFiltering	: true,
				serverSorting	: true,
				transport	: {
					read	: {
						url 		:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1050",
				      	dataType	: "json"
				   	}
				},
				sort: [
					 { field : "IdTipoOperacion", 	dir: "asc" },
				],
				schema: {
					model: {
						fields: {
							IdPreferenciaPago		: { type: "string"},
							IdTipoOperacion			: { type: "string"},
							IdMedioDePago			: { type: "string"},
							IdMoneda				: { type: "string"},
							UsaPersonaRelacionada	: { type: "string"},
							FechaCreacion			: { type: "date"  },
							FechaModificacion		: { type: "date"  },
							IdUsuarioCreador		: { type: "number"},
							IdEstado				: { type: "string"},
							IdEmpresa 				: { type: "number"},
							UsaCuentaBancaria		: { type: "string"},
							UsaTarjetaBancaria		: { type: "string"},
							NombreTipoOperacion		: { type: "string"},
							IdTipoProducto			: { type: "string"},
							NombreTipoProducto		: { type: "string"},
							RecibePagaPortafolio 	: { type: "string"},
							NombreMedioDePago		: { type: "string"},
							NombreMoneda			: { type: "string"},										
						}
					}
				}
			}
		});
			
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		mPrefPago.cmbbx.TipoOperacion = {
			filtros : {  
				RecibePagaPortafolio : {
					"field"   : "RecibePagaPortafolio",
					"operator": "eq",
					"value"   : "0" 
				},
				/*IdEstado : {
					"field"   : "IdEstado",
					"operator": "eq",
					"value"   : "ACT" 
				}*/
			},
		};

		mPrefPago.cmbbx.Moneda = {
			filtros : { 
				IdEstado : {
					"field"   : "IdEstado",
					"operator": "eq",
					"value"   : "ACT" 
				}
			},
		};
		
		mPrefPago.cmbbx.MedioPago = { 
			filtros : { 
				/*RecibePagaPortafolio : {
					"field"   : "RecibePagaPortafolio",
					"operator": "eq",
					"value"   : "0" 
				},*/
				IdEstado : {
					"field"   : "IdEstado",
					"operator": "eq",
					"value"   : "ACT" 
				}
			},
			read : false,
		};
		
		mPrefPago.cmbbx.estado = { 
			filtros : {},
		};

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		mPrefPago.fn.datosGuardar = function(){
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 			= $scope.$root.SesionIdEmpresa;
			datosGuardar.p_IdUsuario 			= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 			= 'SOC';
			datosGuardar.p_UsaReglasNegocio 	= 'N';
			if (mPrefPago.win.modoEdicion) {
				datosGuardar.p_IdPreferenciaPago = mPrefPago.datosDespues.IdPreferenciaPago;
			}
			else {
				var idGenerado=mPrefPago.datosDespues.IdTipoOperacion+'_'+mPrefPago.datosDespues.IdMedioDePago+'_'+mPrefPago.datosDespues.IdMoneda;
				datosGuardar.p_IdPreferenciaPago = idGenerado;
			}
			datosGuardar.p_IdTipoOperacion 		= mPrefPago.datosDespues.IdTipoOperacion;
			datosGuardar.p_IdMedioDePago		= mPrefPago.datosDespues.IdMedioDePago;
			datosGuardar.p_IdMoneda 			= mPrefPago.datosDespues.IdMoneda;
			datosGuardar.p_UsaPersonaRelacionada= (mPrefPago.datosDespues.UsaPersonaRelacionada)?'S':'N';
			datosGuardar.p_IdEstado				= mPrefPago.datosDespues.IdEstado;
			return datosGuardar;
		};
				
		mPrefPago.fn.iniciar = function() {
			
			//título ventana:
			if(!mPrefPago.win.modoEdicion && mPrefPago.parametros.RecibePagaPortafolio === 'P')
				bcsComunes.ventanaTitulo(mPrefPago.idVentana, $translate.instant('UI_CREAR_FORMA_DE_PAGO_RECIBIMOS'));
			if(mPrefPago.win.modoEdicion && mPrefPago.parametros.RecibePagaPortafolio === 'P')
				bcsComunes.ventanaTitulo(mPrefPago.idVentana, $translate.instant('UI_EDITAR_FORMA_DE_PAGO_RECIBIMOS'));
			if(!mPrefPago.win.modoEdicion && mPrefPago.parametros.RecibePagaPortafolio === 'R')
				bcsComunes.ventanaTitulo(mPrefPago.idVentana, $translate.instant('UI_CREAR_FORMA_DE_PAGO_PAGAMOS'));
			if(mPrefPago.win.modoEdicion && mPrefPago.parametros.RecibePagaPortafolio === 'R')
				bcsComunes.ventanaTitulo(mPrefPago.idVentana, $translate.instant('UI_EDITAR_FORMA_DE_PAGO_PAGAMOS'));
			
			mPrefPago.datosAntes.IdEstado = 'ACT';
			mPrefPago.datosAntes.IdMoneda = $scope.$root.SesionIdMonedaSistema;
			
			//Filtros generales
			mPrefPago.cmbbx.TipoOperacion.filtros.RecibePagaPortafolio.value = mPrefPago.parametros.RecibePagaPortafolio;	
			
			//Carga datos modo Edicion
			if(mPrefPago.parametros.PreferenciaPagoObj) {
				mPrefPago.win.modoEdicion = true;
				mPrefPago.datosAntes = _.clone(mPrefPago.parametros.PreferenciaPagoObj);
				mPrefPago.datosDespues = _.clone(mPrefPago.parametros.PreferenciaPagoObj);
				mPrefPago.datosAntes.IdEstado = mPrefPago.parametros.PreferenciaPagoObj.IdEstado;
				mPrefPago.datosDespues.UsaPersonaRelacionada = (mPrefPago.parametros.PreferenciaPagoObj.UsaPersonaRelacionada==='S')?true:false;
			}
		}
		
		/*********************************************
		 * GO!!!
		 *********************************************/
		mPrefPago.fn.iniciar();
	});
}());