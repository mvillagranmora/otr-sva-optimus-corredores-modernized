/*************************************************************
 * Purpose	: Modal Administrar Tipo de Origen de Fondo.
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('ModalTipoOrigenFondoCtrl',
		function ($scope,bcsComunes,servicioTipoOrigenFondo) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var modTOF 			= this;
			modTOF.idVentana 	= bcsComunes.ventanaWinId($scope);
			modTOF.parametros 	= bcsComunes.ventanaParams(modTOF.idVentana);
			modTOF.tipoOrigenFondo	= _.clone(modTOF.parametros.tipoOrigenFondo);
			modTOF.cmbbx		= {};
			modTOF.respuesta 	= {};
			modTOF.modoEditar 	= false;
			

			/*********************************************
			 * FUNCIONES
			 *********************************************/
			modTOF.cargaDatosPantalla = function(){
				if(modTOF.tipoOrigenFondo && modTOF.tipoOrigenFondo.IdTipoOrigenFondo){
					modTOF.modoEditar = true;
					modTOF.tipoOrigenFondo.IdEstadoDef 	= modTOF.tipoOrigenFondo.IdEstado;
				}
				else {
					modTOF.tipoOrigenFondo = {};
					modTOF.tipoOrigenFondo.IdEstadoDef = 'ACT';
					modTOF.tipoOrigenFondo.ValorDefecto = 0;
				}
			};		
	
			modTOF.cancelar = function () {
				modTOF.respuesta.aceptar = false;
				bcsComunes.ventanaData(modTOF.idVentana, modTOF.respuesta);
				bcsComunes.ventanaCerrar(modTOF.idVentana);
			};
			
			modTOF.preparaDatosGuardar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 				= "PLA";
				datosGuardar.p_IdTipoOrigenFondo	 	= modTOF.tipoOrigenFondo.IdTipoOrigenFondo;
				datosGuardar.p_Nombre					= modTOF.tipoOrigenFondo.Nombre;
				datosGuardar.p_ValorDefecto				= modTOF.tipoOrigenFondo.ValorDefecto || 0;
				datosGuardar.p_IdEstado					= modTOF.tipoOrigenFondo.IdEstado;
				return datosGuardar;
			};

			modTOF.aceptar = function () {
				var datosGuardar = modTOF.preparaDatosGuardar();
				if(datosGuardar === undefined) {
					bcsComunes.msg("warning","TIPO ORIGEN FONDO",'No hay datos para guardar'); return;
				}
				bcsComunes.ventanaOverlay(modTOF.idVentana,true);
				if(modTOF.modoEditar) {
					servicioTipoOrigenFondo.updateTipoOrigenFondo(datosGuardar,function (result){
						modTOF.procesaCallback(result,false);
					}, function(resError){
						modTOF.procesaCallback(resError,true);
					});
				}
				else {
					servicioTipoOrigenFondo.saveTipoOrigenFondo(datosGuardar,function (result){
						modTOF.procesaCallback(result,false);
					}, function(resError){
						modTOF.procesaCallback(resError,true);
					});
				}
			};
			
			modTOF.procesaCallback = function(result,esError){
				if(esError){
					bcsComunes.msg("error","TIPO ORIGEN FONDO",'Revise Log');
					console.log(result);
				}
				else {
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
						bcsComunes.msg("info","TIPO ORIGEN FONDO",'Guardar OK');
						modTOF.respuesta.aceptar = true;
						bcsComunes.ventanaData(modTOF.idVentana, modTOF.respuesta);
						bcsComunes.ventanaCerrar(modTOF.idVentana);
					}
					else {
						bcsComunes.msg("error","TIPO ORIGEN FONDO",result.CODIGO + ' : ' + result.PILA);
						console.log(result);
					}
				}
				bcsComunes.ventanaOverlay(modTOF.idVentana,false);
			};

			/*********************************************
			 * COMBOBOX
			 *********************************************/
			modTOF.cmbbx.EstadoPerfilComercial={
				filtros : {},
			};

			$scope.$watch('modTOF.tipoOrigenFondo.IdTipoOrigenFondo',function(newVal){
				if(newVal) modTOF.tipoOrigenFondo.IdTipoOrigenFondo = modTOF.tipoOrigenFondo.IdTipoOrigenFondo.replace(' ','').toUpperCase();
			});
			
			/*********************************************
			 * GO!!!
			 *********************************************/
			modTOF.cargaDatosPantalla();	
	});
}());
//------------------------- FIN CONTROLLER --------------------------