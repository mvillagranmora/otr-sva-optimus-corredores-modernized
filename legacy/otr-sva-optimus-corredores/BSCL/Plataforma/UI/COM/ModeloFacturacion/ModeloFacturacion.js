/*********************************************************
 * Purpose	: Controlador para administrar 
 *            Modelos de Facturacion (Plataforma)
 * Author	: JGarces - BCS
 * Date		: 16/03/2016
 ********************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('ModeloFacturacionCtrl',
	function($state,$q,$scope,$translate,bcsComunes,servicioModeloFacturacion,$uibModal,bcsComunes,bcsAlerta){ 
	
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var modFact 		= this; 
		modFact.busq 		= {};
		modFact.datosAntes 	= {};
		modFact.datosDespues= {};
		modFact.cmbbx 		= {};
		modFact.fn 			= {};
		modFact.win 		= {};				
		modFact.win.modoLectura 	= true;
		modFact.win.modoEditar 		= false;
		modFact.win.muestraEditar 	= false;
		
/*********************************************
 * MODALIDADES DE PANTALLA
 ********************************************/
		
		modFact.win.ModoEdicion = function(){
			modFact.win.modoLectura   = false;
			modFact.win.modoEditar 	  = true;
			modFact.win.muestraEditar = false;
		};
		
		modFact.win.ModoCreacion = function(){
			modFact.win.modoLectura = false;
			modFact.win.modoEditar 	= false;
			modFact.win.muestraEditar = false;
		};

		modFact.win.ModoLectura = function(){
			modFact.win.modoLectura = true;
			modFact.win.modoEditar 	= false;
		};

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
		
		modFact.win.crear = function(){
			modFact.datosAntes  = {};
			modFact.datosDespues.IdModoFacturacion = 'NORMAL';
			modFact.datosDespues.IdEstado = true;
			modFact.win.ModoCreacion();
		};
		
		modFact.win.editar = function(){
			modFact.win.ModoEdicion();
		};

		modFact.win.guardar = function(){
			var dataModelo = modFact.fn.preparaDatosGuardar();
			if(modFact.win.modoEditar){ //editar
				servicioModeloFacturacion.updateModeloFacturacion(dataModelo,function(result){
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0){
						bcsComunes.msg("info","MODIFICAR MODELO FACTURACION",'Operación realizada');
						modFact.fn.postGuardar();
					}
					else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
						bcsComunes.msg("warning","MODIFICAR MODELO FACTURACION",'Modificación de Modelo Facturación tiene adventencias');
						modFact.win.ModoLectura();
						modFact.datosDespues = _.clone(modFact.datosAntes);
					}
					else bcsComunes.msg("error","MODIFICAR MODELO FACTURACION","Error al guardar");
				});
			}
			else { //crear
				servicioModeloFacturacion.saveModeloFacturacion(dataModelo,function(result){
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0){
						bcsComunes.msg("info","CREAR MODELO FACTURACION",'Operación realizada');
						modFact.fn.postGuardar();
					}
					else bcsComunes.msg("error","CREAR MODELO FACTURACION","Error al guardar");
				});
			}
		};

		modFact.win.cancelar = function(){
			modFact.datosAntes = {};
			modFact.datosDespues = {};
			modFact.busq.IdModeloFacturacion = undefined;
			modFact.win.ModoLectura();
		};

/*********************************************
 * DATASOURCES
 *********************************************/

/*********************************************
 * COMBOBOX
 *********************************************/
		modFact.cmbbx.modeloFacturacion = {
			filtros : { 
				//XX: { "field" : "XX", "operator": "eq","value" : XX }
			}
		};

/*********************************************
 * FUNCIONES
 *********************************************/

		modFact.fn.preparaDatosGuardar = function(){
			var dataModelo = {};
			dataModelo.p_IdEmpresa 							= $scope.$root.SesionIdEmpresa;
			dataModelo.p_IdUsuario 							= $scope.$root.SesionIdUsuario;
			dataModelo.p_IdModulo 							= 'PLA';
			dataModelo.p_UsaReglasNegocio 					= 'N',
			dataModelo.p_simularOperacion					= 'N',
			dataModelo.p_IdModeloFacturacion				= modFact.datosDespues.IdModeloFacturacion		
			dataModelo.p_DescripcionModeloFacturacion		= modFact.datosDespues.DescripcionModeloFacturacion||''
			dataModelo.p_SeparaLineaFacturaPorTransaccion	= ( modFact.datosDespues.SeparaLineaFacturaPorTransaccion && !modFact.datosDespues.FusionaLineaFacturaPorPrecio)?'S':'N';	
			dataModelo.p_SeparaFacturaPorInstrumento		= ( modFact.datosDespues.SeparaFacturaPorInstrumento)?'S':'N';	
			dataModelo.p_FusionaLineaFacturaPorPrecio		= (!modFact.datosDespues.SeparaLineaFacturaPorTransaccion &&  modFact.datosDespues.FusionaLineaFacturaPorPrecio)?'S':'N';
			dataModelo.p_IdModoFacturacion					= modFact.datosDespues.IdModoFacturacion||'NORMAL';	
			dataModelo.p_IdEntidadModoFacturacion			= 0;
			dataModelo.p_OperaContraCuentaInversion			= (modFact.datosDespues.OperaContraCuentaInversion)?'S':'N';
			dataModelo.p_NeteaIngresosEgresosTesoreria		= (modFact.datosDespues.NeteaIngresosEgresosTesoreria)?'S':'N';
			dataModelo.p_IdEstado					   		= (modFact.datosDespues.IdEstado)?'ACT':'INA';
			dataModelo.p_PorDefecto							= (modFact.datosDespues.PorDefecto)?'S':'N';
			return dataModelo;
		};
		
		modFact.fn.cargarDatos = function(modeloFacturacion){
			if(modeloFacturacion)  {
				modFact.datosAntes = _.clone(modeloFacturacion);
				modFact.datosAntes.PorDefecto						= (modeloFacturacion.PorDefecto==='S')?true:false;
				modFact.datosAntes.IdEstado							= (modeloFacturacion.IdEstado==='ACT')?true:false;
				modFact.datosAntes.SeparaLineaFacturaPorTransaccion	= (modeloFacturacion.SeparaLineaFacturaPorTransaccion==='S' && modeloFacturacion.FusionaLineaFacturaPorPrecio==='N')?true:false;
				modFact.datosAntes.SeparaFacturaPorInstrumento		= (modeloFacturacion.SeparaFacturaPorInstrumento==='S')?true:false;
				modFact.datosAntes.FusionaLineaFacturaPorPrecio		= (modeloFacturacion.SeparaLineaFacturaPorTransaccion==='N' && modeloFacturacion.FusionaLineaFacturaPorPrecio==='S')?true:false;
				modFact.datosAntes.IdModoFacturacion				= modFact.datosDespues.IdModoFacturacion||'NORMAL';
				modFact.datosAntes.OperaContraCuentaInversion		= (modeloFacturacion.OperaContraCuentaInversion==='S')?true:false;
				modFact.datosAntes.NeteaIngresosEgresosTesoreria	= (modeloFacturacion.NeteaIngresosEgresosTesoreria==='S')?true:false;
				modFact.datosDespues = _.clone(modFact.datosAntes);
				modFact.win.muestraEditar = true;
			}
		};
		
		modFact.fn.postGuardar = function() {
			modFact.win.ModoLectura();
			modFact.cmbbx.modeloFacturacion.filtros.read = true;
			modFact.busq.IdModeloFacturacion 	= modFact.datosDespues.IdModeloFacturacion;
			modFact.busq.IdModeloFacturacionDef = modFact.datosDespues.IdModeloFacturacion;
			modFact.datosAntes = {}; modFact.datosDespues = {};
			//modFact.datosAntes = _.clone(modFact.datosDespues); modFact.win.muestraEditar = true;
		};
		
		
		// El campo Separa linea factura por trans y campo Fusiona linea factura por precio NO pueden ir juntos
		$scope.$watch('modFact.datosDespues.SeparaLineaFacturaPorTransaccion', function (newValue, oldValue){
			if(modFact.datosDespues.SeparaLineaFacturaPorTransaccion){
				modFact.datosDespues.FusionaLineaFacturaPorPrecio = false;
			}
			//else{pPortPF.datosDespues.FusionaLineaFacturaPorPrecio = true;}
		});
		$scope.$watch('modFact.datosDespues.FusionaLineaFacturaPorPrecio', function (newValue, oldValue){
			if(modFact.datosDespues.FusionaLineaFacturaPorPrecio){
				modFact.datosDespues.SeparaLineaFacturaPorTransaccion = false;
			}
			//else{modFact.datosDespues.SeparaLineaFacturaPorTransaccion = true;}
		});
		
		// El campo Neteo ingresos y egresos en tesorería SOLO puede ser seleccionado si Opera contra cta inversion NO es marcado
		$scope.$watchGroup(['modFact.datosDespues.OperaContraCuentaInversion','modFact.datosDespues.NeteaIngresosEgresosTesoreria'], function (newValue, oldValue){
			if(modFact.datosDespues.OperaContraCuentaInversion){
				modFact.datosDespues.NeteaIngresosEgresosTesoreria = false;
				modFact.win.disableNeteaIngEgrTes = true;
			}
			else{modFact.win.disableNeteaIngEgrTes = false;}
		});
		
	});
}());
//------------------------- FIN CONTROLLER --------------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioModeloFacturacion',['$resource',function($resource) {
		var urlSaveModeloFacturacion 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=247";
		var urlUpdateModeloFacturacion 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=248";
		return $resource("", {},
		{
	  	  'saveModeloFacturacion'	: { method: "POST",	url: urlSaveModeloFacturacion},
	  	  'updateModeloFacturacion'	: { method: "POST",	url: urlUpdateModeloFacturacion}
	     });
	}]);
}());