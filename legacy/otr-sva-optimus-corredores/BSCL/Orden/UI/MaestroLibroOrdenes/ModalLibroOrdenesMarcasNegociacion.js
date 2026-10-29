angular.module('ModuloOrdenes').controller('ModalLibroOrdenesMarcasNegociacionCtrl',function($scope,$translate,bcsComunes,$uibModal,$resource) {
	
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
	
	var mloMN = this;
	mloMN.idVentana = bcsComunes.ventanaWinId($scope);
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	mloMN.LibroOrdenesMarcasNegociacion			= $.extend({},parametros.TipoLibSel);
	mloMN.LibroOrdenesMarcasNegociacionOriginal	= $.extend({},parametros.TipoLibSel);
	
	mloMN.IdBotonAceptar = 'ModalLibroOrdenesMarcasNegociacion_aceptar' + parametros.AccionBoton;
	
	mloMN.LibroOrdenesMarcasNegociacion.modoCrear = true;
	
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
	
	mloMN.aceptar = function(){
		var datos = {
			p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario			: $scope.$root.SesionIdUsuario,
			p_IdModulo			: 'ORD',
			p_UsaReglasNegocio	: 'N',
			
			p_IdLibroOrdenes	: parametros.TipoLibSel.IdLibroOrdenes,
			p_MarcaNegociacion	: mloMN.LibroOrdenesMarcasNegociacion.MarcaNegociacion
		};
		
		bcsComunes.ventanaOverlay(mloMN.idVentana,true);
		agregarLibroOrdenesMarcasNegociacion(datos).then(
			function(result) {
				console.log(result);
				var mensaje="";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					mensaje = "Advertencia","La operacion tiene advertencias";
				}
				else if (result.P_OUT_ERRORESCONTADOR){
					mensaje = "Error al agregar Marca Negociación a Libro de Órdenes :: " + result.P_OUT_ERRORESDETALLE[0].Codigo;
				}
				else{
					bcsComunes.msg("info","info","Marca Negociación agregada a Libro de Órdenes");
				}
				
				if(mensaje!="")
					bcsComunes.ventanaMsg({
						mensaje: mensaje,
						conflictos:util.parseServerResponse(result)
					},idVentana);
				
				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.ventanaOverlay(mloMN.idVentana,false);
			},
			function(result){
				console.log(result);
				bcsComunes.msg("error","error","Error al agregar Marca Negociación a Libro de Órdenes");
				bcsComunes.ventanaData(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.ventanaOverlay(mloMN.idVentana,false);
			});
	};
	
	mloMN.cancelar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
/*********************************************
 * DATASOURCES
 *********************************************/

	
/*********************************************
 * COMBOBOX
 *********************************************/
	
			
/*********************************************
 * GRILLAS
 *********************************************/
	
	
/*********************************************
 * FUNCIONES
 ********************************************/
	
	var agregarLibroOrdenesMarcasNegociacion = function(p_datos){
		var urlAgregarLibroOrdenesMarcasNegociacion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=222";
		var r = $resource("", {}, {'urlAgregarLibroOrdenesMarcasNegociacion': {method:"POST", url:urlAgregarLibroOrdenesMarcasNegociacion}});
		var post = r.urlAgregarLibroOrdenesMarcasNegociacion(p_datos);
		
		return post.$promise;
    };
	
//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
});