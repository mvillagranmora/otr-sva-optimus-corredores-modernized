/*************************************************************
 * Purpose	: Modal Administrar Valor de Cobro.
 * Author	: JG - BCS
 * Date		: 30/06/2016
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('ModalAdministrarValorCobroCtrl',
	function ($scope,bcsComunes,$translate,servicioPerfilCobros) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var mAdmVC 			= this;
		mAdmVC.idVentana 	= bcsComunes.ventanaWinId($scope);
		mAdmVC.parametros 	= bcsComunes.ventanaParams(mAdmVC.idVentana);
		mAdmVC.valorCobro	= _.clone(mAdmVC.parametros.valorCobro);
		mAdmVC.cmbbx		= {};
		mAdmVC.respuesta 	= {};
		mAdmVC.modoEditar 	= false;
		mAdmVC.validaEditar = false;
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		mAdmVC.cargaDatosPantalla = function(){
			if(mAdmVC.valorCobro && mAdmVC.valorCobro.IdValorCobro){
				mAdmVC.modoEditar = true;
				mAdmVC.valorCobro.IdTipoProductoDef 		= mAdmVC.parametros.valorCobro.IdTipoProducto;
				mAdmVC.valorCobro.IdCobroClasificacionDef 	= mAdmVC.parametros.valorCobro.IdCobroClasificacion;
				mAdmVC.valorCobro.IdCobroDef				= mAdmVC.parametros.valorCobro.IdCobro;
				mAdmVC.valorCobro.ValorCobro				= mAdmVC.valorCobro.ValorCobro;
				mAdmVC.valorCobro.IdMonedaDef 				= mAdmVC.valorCobro.IdMoneda;
				mAdmVC.valorCobro.IdEstadoDef 				= mAdmVC.valorCobro.IdEstado;
				mAdmVC.valorCobro.ValorTopeMinimoOrden	    = mAdmVC.valorCobro.ValorTopeMinimoOrden;
			}
			else {
				mAdmVC.valorCobro = {};
				mAdmVC.valorCobro.IdEstadoDef = 'ACT';
				mAdmVC.valorCobro.ValorTopeMinimoOrden = 0;
				mAdmVC.valorCobro.IdMonedaDef = 'CLP';
				mAdmVC.inicializado = true;
			}
		};		

		mAdmVC.cancelar = function () {
			mAdmVC.respuesta.aceptar = false;
			bcsComunes.ventanaData(mAdmVC.idVentana, mAdmVC.respuesta);
			bcsComunes.ventanaCerrar(mAdmVC.idVentana);
		};
		
		mAdmVC.preparaDatosGuardar = function(){
			var datosGuardar = {};
			
			if (mAdmVC.valorCobro.CobroSeleccionado.UnidadDeCobro === "POR" && mAdmVC.valorCobro.ValorCobro > 100){
				mAdmVC.validaEditar = false
			}else{
				   mAdmVC.validaEditar = true
			     }
			
			datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 				= "PLA";
			datosGuardar.p_UsaReglasNegocio 		= "N";
			datosGuardar.p_IdValorCobro	 			= mAdmVC.valorCobro.IdValorCobro;
			datosGuardar.p_DescripcionValorCobro	= mAdmVC.valorCobro.IdValorCobro;
			datosGuardar.p_IdTipoProducto			= mAdmVC.valorCobro.IdTipoProducto;
			datosGuardar.p_IdCobro					= mAdmVC.valorCobro.IdCobro;
			datosGuardar.p_ValorCobro				= mAdmVC.valorCobro.ValorCobro;
			datosGuardar.p_ValorTopeMinimoOrden		= mAdmVC.valorCobro.ValorTopeMinimoOrden || 0;
			datosGuardar.p_IdMoneda					= mAdmVC.valorCobro.IdMoneda;
			datosGuardar.p_IdEstado					= mAdmVC.valorCobro.IdEstado;
			return datosGuardar;
		};

		mAdmVC.aceptar = function () {
			var datosGuardar = mAdmVC.preparaDatosGuardar();
			if(mAdmVC.modoEditar && mAdmVC.validaEditar) { //EDITAR
				if(datosGuardar) {
					servicioPerfilCobros.updateValorCobro(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","VALOR COBRO",'Valor Actualizado');
							mAdmVC.respuesta.aceptar = true;
							bcsComunes.ventanaData(mAdmVC.idVentana, mAdmVC.respuesta);
							bcsComunes.ventanaCerrar(mAdmVC.idVentana);
						}else {
							bcsComunes.msg("error","VALOR COBRO",result.CODIGO + ' : ' + result.PILA);
							console.log(result);
						}
					}, function(resError){
						bcsComunes.msg("error","VALOR COBRO",'Revise Log');
						console.log(resError);
					});
				} else bcsComunes.msg("warning","VALOR COBRO",'No hay datos para guardar');
			}
			else { // GUARDAR
				if(datosGuardar && mAdmVC.validaEditar) {
					servicioPerfilCobros.saveValorCobro(datosGuardar,function (result){
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
							bcsComunes.msg("info","VALOR COBRO",'Valor Actualizado');
							mAdmVC.respuesta.aceptar = true;
							bcsComunes.ventanaData(mAdmVC.idVentana, mAdmVC.respuesta);
							bcsComunes.ventanaCerrar(mAdmVC.idVentana);
						}else {
							bcsComunes.msg("error",result.P_OUT_ERRORESDETALLE[0].Codigo, $translate.instant(result.P_OUT_ERRORESDETALLE[0].Descripcion));
							console.log(result);
						}
					}, function(resError){
						bcsComunes.msg("error","VALOR COBRO",'Revise Log');
						console.log(resError);
					});
					
				} else
					if (mAdmVC.validaEditar){
						bcsComunes.msg("warning","VALOR COBRO",'No hay datos para guardar');		
					}
					else
					{
						bcsComunes.msg("error","VALOR COBRO",'valor variable no puede superar el 100 %');		
						
					}
				 
			}
		};
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		mAdmVC.cmbbx.Bolsa={
			filtros : {},
		};

		mAdmVC.cmbbx.TipoProducto={
			filtros : {},
		};
		
		mAdmVC.cmbbx.ClasificacionCobro={
			filtros : {},
		};
		
		mAdmVC.cmbbx.Cobro={
			filtros : {
				IdCobroClasificacion : { "field" : "IdCobroClasificacion", "operator": "eq","value" : '-1' },
			PermiteAsignarPortafolio : { "field" : "PermiteAsignarPortafolio", "operator": "eq","value" : "S" },
			IdEmpresa : { "field" : "IdEmpresa", "operator": "eq","value" : $scope.$root.SesionIdEmpresa}
			},
		};
		
		mAdmVC.cmbbx.Moneda={
			filtros : {},
		};

		mAdmVC.cmbbx.EstadoPerfilComercial={
			filtros : {},
		};

		$scope.$watch('mAdmVC.valorCobro.IdValorCobro',function(newVal){
			if(newVal) {
				mAdmVC.valorCobro.IdValorCobro		    = mAdmVC.valorCobro.IdValorCobro.replace(' ','').toUpperCase();
				if( mAdmVC.inicializado) {
					if(mAdmVC.modoEditar){
						mAdmVC.valorCobro.ValorCobro 			= mAdmVC.valorCobro.ValorCobro;
						mAdmVC.valorCobro.ValorTopeMinimoOrden 	= mAdmVC.valorCobro.ValorTopeMinimoOrden;
					}else{
						mAdmVC.valorCobro.ValorCobro 			= mAdmVC.valorCobro.CobroSeleccionado.ValorCobro;
						mAdmVC.valorCobro.ValorTopeMinimoOrden 	= mAdmVC.valorCobro.CobroSeleccionado.ValorTopeMinimoOrden;
					}
				}
			}
		});
		
		$scope.$watch('mAdmVC.valorCobro.IdCobro',function(newVal, oldVal){
				if(newVal && oldVal && mAdmVC.inicializado && oldVal !== newVal) {
						mAdmVC.valorCobro.ValorCobro 			= mAdmVC.valorCobro.CobroSeleccionado.ValorCobro;
						mAdmVC.valorCobro.ValorTopeMinimoOrden 	= mAdmVC.valorCobro.CobroSeleccionado.ValorTopeMinimoOrden;
						if (newVal == "IVA") {
							mAdmVC.valorCobro.IdMonedaDef = 'CLP';
						}
				}
		});
		
		$scope.$watch('mAdmVC.valorCobro.IdCobroClasificacion',function(newVal, oldVal){
				if(oldVal && newVal && newVal !== oldVal){
					mAdmVC.valorCobro.ValorTopeMinimoOrden 	= 0;
				}
		});
		
		$scope.$watch('mAdmVC.valorCobro.MonedaSeleccionado', function(newVal, oldVal) {
			if (newVal) {
				$scope.mAdmVC.valorCobro.CantidadDecimales = (util.bcs_formato(0, 'GEN', 'Monto', $scope.mAdmVC.valorCobro.MonedaSeleccionado.IdMoneda, '', '')).cantidadDecimales;
			}
		}, true);

		$scope.$watch('mAdmVC.valorCobro.CobroSeleccionado.PermiteCambiarValor',function(newVal){
			if(newVal==='N' || newVal==='S'){
				if(mAdmVC.inicializado) {
					if(mAdmVC.modoEditar){
						mAdmVC.valorCobro.ValorCobro 			= mAdmVC.valorCobro.ValorCobro;
						mAdmVC.valorCobro.ValorTopeMinimoOrden 	= mAdmVC.valorCobro.ValorTopeMinimoOrden;
					}else{
						mAdmVC.valorCobro.ValorCobro 			= mAdmVC.valorCobro.CobroSeleccionado.ValorCobro;
						mAdmVC.valorCobro.ValorTopeMinimoOrden 	= mAdmVC.valorCobro.CobroSeleccionado.ValorTopeMinimoOrden;
					}
				}else {
					mAdmVC.inicializado = true;
				}
			}
		});
		/*********************************************
		 * GO!!!
		 *********************************************/
		mAdmVC.cargaDatosPantalla();
	});
}());