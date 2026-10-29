/***************************************************************************
 * Purpose	: Controlador para creación/edición Homologaciones de Operador.
 * Author	: JGarces - BCS
 * Date		: 05/02/2016
 **************************************************************************/

(function() {
	angular.module('ModuloPlataforma').controller('administrarModHomologOperCtrl',
		function($window,$scope,$translate,bcsComunes,$state,$filter,servicioTransacciones){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var modHOper = this;
		modHOper.datosAntes 	= {};
		modHOper.datosDespues	= {};
		modHOper.fn 			= {};
		modHOper.ds				= {};
		modHOper.cbbx			= {};
		modHOper.win			= {};
		modHOper.winId 			= bcsComunes.ventanaWinId($scope);
		modHOper.param 			= bcsComunes.ventanaParams(modHOper.winId);
		 
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
		
		modHOper.win.guardar = function() {
			var datosGuardar = modHOper.fn.genDatosGuardar();
			console.log(datosGuardar);
			if(modHOper.param.operador && modHOper.param.homologacion){ //modificar
				servicioTransacciones.updateHomologacionOperador(datosGuardar,function (result){
					var datos = {};
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
						bcsComunes.msg('info','HOMOLOGACION OPERADOR','Homologacion Modificada');
						console.log(result);
					}
					else {
						datos.message = result.CODIGO + ' : ' + result.PILA;
						bcsComunes.msg('error','HOMOLOGACION OPERADOR','Error al modificar homologación. ' + datos.message);
						console.log(result);
					}
					bcsComunes.ventanaCerrar(modHOper.winId);
				});	
			}
			else{ //crear
				servicioTransacciones.saveHomologacionOperador(datosGuardar,function (result){
					var datos = {};
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
						bcsComunes.msg('info','HOMOLOGACION OPERADOR','Homologacion Creada');
						console.log(result);
					}
					else {
						datos.message = result.CODIGO + ' : ' + result.PILA;
						bcsComunes.msg('error','HOMOLOGACION OPERADOR','Error al crear homologación. ' + datos.message);
						console.log(result);
					}	
					bcsComunes.ventanaCerrar(modHOper.winId);
				});	
			}
		};
		
		modHOper.win.cancelar = function() {
			bcsComunes.ventanaCerrar(modHOper.winId);
		};
		
		
/*********************************************
 * DATASOURCES
 *********************************************/
		
		
/*********************************************
 * COMBOBOX
 *********************************************/
		modHOper.cbbx.bolsa={
			idActual : false,
			seleccionado : false,
			filtros: {
				//IdEmpresa : { "field"   : "IdEmpresa","operator": "eq", "value"   :  $scope.$root.SesionIdEmpresa },
			},
		};
	
/*********************************************
 * GRILLAS
 *********************************************/
		
		
/*********************************************
 * FUNCIONES
 *********************************************/
		
		modHOper.fn.cargaDatos = function(datos) {
			console.log("lo que llega!",datos);
			modHOper.datosAntes = _.clone(datos);
			modHOper.datosDespues = _.clone(datos);
			modHOper.datosDespues.IdBolsaDefecto = datos.IdBolsa;
			modHOper.datosDespues.IdEstado = (datos.IdEstado==='ACT')?true:false;
		};
		
		modHOper.fn.genDatosGuardar = function() {
			var dg = {};
			dg.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			dg.p_IdUsuario = $scope.$root.SesionIdUsuario;
			dg.p_IdModulo = 'PLA',
			dg.p_simularOperacion = 'N',
			dg.p_UsaReglasNegocio = 'N',
			dg.p_CodigoOperador = modHOper.datosDespues.CodigoOperador;
			dg.p_IdBolsa = modHOper.datosDespues.IdBolsa;
			dg.p_CodigoHomologacion = modHOper.datosDespues.CodigoHomologacion;
			dg.p_IdEstado = (modHOper.datosDespues.IdEstado) ? 'ACT':'INA';
			if(modHOper.param.operador && modHOper.param.homologacion) dg.p_IdHomologacionOperador = modHOper.datosDespues.IdHomologacionOperador;
			return dg;
		};

		
		
/*********************************************
 * FUNCIONES DE POPUP
 *********************************************/	
		

		
/*********************************************
 * FUNCIONES WATCH 
 *********************************************/	
		

		
/*********************************************
 * GOOOOO
 *********************************************/	
		if(modHOper.param.operador){
			if(modHOper.param.homologacion) {
				modHOper.fn.cargaDatos(modHOper.param.homologacion);
			}
			else {
				modHOper.datosDespues.CodigoOperador = modHOper.param.operador.CodigoOperador;
				modHOper.datosDespues.IdEstado=true;
			}
		}
		else bcsComunes.ventanaCerrar(modHOper.winId); //No permitido.
			
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------

