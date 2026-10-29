angular.module('ModuloOrdenes').controller('ModalLibroOrdenesOperadoresCtrl',function($scope,$translate,bcsComunes,$uibModal,$resource) {
	
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
	
	var mloOpe = this;
	mloOpe.idVentana = bcsComunes.ventanaWinId($scope);
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	mloOpe.LibroOrdenesOperadores			= $.extend({},parametros.TipoLibSel);
	mloOpe.LibroOrdenesOperadoresOriginal	= $.extend({},parametros.TipoLibSel);
	
	mloOpe.IdBotonAceptar = 'ModalLibroOrdenesOperadores_aceptar' + parametros.AccionBoton;
	
	mloOpe.LibroOrdenesOperadores.modoCrear = true;
	
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
	
	mloOpe.aceptar = function(){
		var datos = {
			p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario			: $scope.$root.SesionIdUsuario,
			p_IdModulo			: 'ORD',
			p_UsaReglasNegocio	: 'N',
			
			p_IdLibroOrdenes	: parametros.TipoLibSel.IdLibroOrdenes,
			p_CodigoOperador	: mloOpe.LibroOrdenesOperadores.CodigoOperador
		};
		
		bcsComunes.ventanaOverlay(mloOpe.idVentana,true);
		agregarLibroOrdenesOperadores(datos).then(
			function(result) {
				console.log(result);
				var mensaje = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					mensaje = "Advertencia","La operacion tiene advertencias";
				}
				else if (result.P_OUT_ERRORESCONTADOR){
					mensaje = "Error al agregar Operador a Libro de Órdenes :: " + result.P_OUT_ERRORESDETALLE[0].Codigo;
				}
				else{
					bcsComunes.msg("info","info","Operador a Libro de Órdenes agregado");
				}
				
				if(mensaje!="")
					bcsComunes.ventanaMsg({
						mensaje: mensaje,
						conflictos:util.parseServerResponse(result)
					},idVentana);

				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.ventanaOverlay(mloOpe.idVentana,false);
			},
			function(result){
				console.log(result);
				bcsComunes.msg("error","error","Error al agregar Operador a Libro de Órdenes");
				bcsComunes.ventanaData(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.ventanaOverlay(mloOpe.idVentana,false);
			});
	};
	
	mloOpe.cancelar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
/*********************************************
 * DATASOURCES
 *********************************************/

	
/*********************************************
 * COMBOBOX
 *********************************************/
	
	mloOpe.conf = {};
	mloOpe.conf.oConfiguracionOperadores={
		filtros : {
			EstadoOperador : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
			
/*********************************************
 * GRILLAS
 *********************************************/
	
	
/*********************************************
 * FUNCIONES
 ********************************************/
	
	var agregarLibroOrdenesOperadores = function(p_datos){
		var urlAgregarLibroOrdenesOperadores = "/BSCL/Plataforma/GWJAVA/PLA/call?t=217";
		var r = $resource("", {}, {'urlAgregarLibroOrdenesOperadores': {method:"POST", url:urlAgregarLibroOrdenesOperadores}});
		var post = r.urlAgregarLibroOrdenesOperadores(p_datos);
		
		return post.$promise;
    };
	
//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
});