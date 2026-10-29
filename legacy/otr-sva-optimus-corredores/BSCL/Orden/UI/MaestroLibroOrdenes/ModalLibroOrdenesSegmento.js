angular.module('ModuloOrdenes').controller('ModalLibroOrdenesSegmentoCtrl',function($scope,$translate,bcsComunes,$uibModal,$resource) {
	
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
	
	var mloSeg = this;
	mloSeg.idVentana = bcsComunes.ventanaWinId($scope);
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	mloSeg.LibroOrdenesSegmento			= $.extend({},parametros.TipoLibSel);
	mloSeg.LibroOrdenesSegmentoOriginal	= $.extend({},parametros.TipoLibSel);
	
	mloSeg.IdBotonAceptar = 'ModalLibroOrdenesSegmento_aceptar' + parametros.AccionBoton;
	
	mloSeg.LibroOrdenesSegmento.modoCrear = true;
	
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
	
	mloSeg.aceptar = function(){
		var datos = {
			p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario			: $scope.$root.SesionIdUsuario,
			p_IdModulo			: 'ORD',
			p_UsaReglasNegocio	: 'N',
			
			p_IdLibroOrdenes	: parametros.TipoLibSel.IdLibroOrdenes,
			p_IdSegmentoCliente	: mloSeg.LibroOrdenesSegmento.IdSegmentoCliente
		};
		
		bcsComunes.ventanaOverlay(mloSeg.idVentana,true);
		agregarLibroOrdenesSegmento(datos).then(
			function(result) {
				console.log(result);
				var mensaje = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					mensaje = "Advertencia","La operacion tiene advertencias";
				}
				else if (result.P_OUT_ERRORESCONTADOR){
					mensaje = "Error al agregar Segmento a Libro de Órdenes :: " + result.P_OUT_ERRORESDETALLE[0].Codigo;
				}
				else{
					bcsComunes.msg("info","info","Segmento a Libro de Órdenes agregado");
				}
				
				if(mensaje!="")
					bcsComunes.ventanaMsg({
						mensaje: mensaje,
						conflictos:util.parseServerResponse(result)
					},idVentana);
				
				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.ventanaOverlay(mloSeg.idVentana,false);
			},
			function(result){
				console.log(result);
				bcsComunes.msg("error","error","Error al agregar Segmento a Libro de Órdenes");
				bcsComunes.ventanaData(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.ventanaOverlay(mloSeg.idVentana,false);
			});
	};
	
	mloSeg.cancelar = function() {
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
	
	var agregarLibroOrdenesSegmento = function(p_datos){
		var urlAgregarLibroOrdenesSegmento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=221";
		var r = $resource("", {}, {'urlAgregarLibroOrdenesSegmento': {method:"POST", url:urlAgregarLibroOrdenesSegmento}});
		var post = r.urlAgregarLibroOrdenesSegmento(p_datos);
		
		return post.$promise;
    };
	
//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
});