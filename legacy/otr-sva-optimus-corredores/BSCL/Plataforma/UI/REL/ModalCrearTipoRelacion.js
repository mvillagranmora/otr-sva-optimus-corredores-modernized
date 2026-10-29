angular.module('ModuloSocioNegocio').controller('ModalRelacionCtrl',function($scope,servicioSociodeNegocio,bcsComunes) {
 
	
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
	
	var mr = this;
	var overlay = bcsComunes.overlay;
	
	mr.crearNuevaPersona = true; 
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	mr.IdPersona		= parametros.IdPersona;
	mr.Relacion			= $.extend({},parametros.RelSel);
	mr.RelacionOriginal	= $.extend({},parametros.RelSel);
	
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
	
	if(parametros.RelSel){
		//modificacion
		mr.titulo = 'UI_MODIFICAR_RELACION';
		
		mr.Relacion = {};
		mr.Relacion.IdRelacionPersona	= parametros.RelSel.IdRelacionPersona;
		mr.Relacion.TipoRelacion		= parametros.RelSel.NombreTipoRelacion;
		mr.Relacion.DocIdeRelacionado	= util.formatRut(parametros.RelSel.DocumentoIdentidadDestino);
		mr.Relacion.NomComRelacionado	= parametros.RelSel.NombreCompletoDestino;
		mr.Relacion.EsIndefinida		= parametros.RelSel.EsIndefinida;
		mr.Relacion.FechaInicioRelacion	= parametros.RelSel.FechaInicioRelacion; //rev fecha
		mr.Relacion.FechaFinRelacion	= parametros.RelSel.FechaFinRelacion; //rev fecha
		
		mr.RelacionOriginal	= $.extend({},mr.Relacion);
		mr.Relacion.modoCrear = false;
	}
	else{
		mr.titulo = 'UI_AGREGAR_RELACION';
		mr.Relacion = {};
		mr.Relacion.IdEstado			= 'ACT';
		mr.Relacion.FechaInicioRelacion	= new Date();
		mr.Relacion.FechaFinRelacion	= new Date();
		
		mr.Relacion.modoCrear = true;
	}
	
	mr.aceptar = function(){
		//modificacion
		if (mr.Relacion.IdRelacionPersona){
			mr.Relacion.FechaInicioRelacion	= mr.Relacion.EsIndefinida === 'N' ? mr.Relacion.FechaInicioRelacion : '0001-01-01 00:00:00';
			mr.Relacion.FechaFinRelacion	= mr.Relacion.EsIndefinida === 'N' ? mr.Relacion.FechaFinRelacion : '0001-01-01 00:00:00'
				
			var campos = [
						{ nombre: "FechaInicioRelacion"	,tipo : "fecha"},
						{ nombre: "FechaFinRelacion"	,tipo : "fecha"}];
				
			var registroCambio = [];
			
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (mr.Relacion[value.nombre] !== mr.RelacionOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?mr.Relacion[value.nombre]:"")});
				}
				if (value.tipo === 'combo') {
					if (!(mr.Relacion[value.nombre] === false && mr.RelacionOriginal[value.nombre] === null) && (mr.Relacion[value.nombre] != mr.RelacionOriginal[value.nombre]))
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?mr.Relacion[value.nombre]:"")});
				}
				if (value.tipo === 'fecha') {
					if (mr.Relacion[value.nombre] !== mr.RelacionOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?mr.Relacion[value.nombre]:"")});
				}
				if (value.tipo === 'bool') {
					if (mr.Relacion[value.nombre] !== mr.RelacionOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?"S":"N")});
				}			
			});
			
			if (registroCambio.length === 0){
				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaCerrar(idVentana);
				return;
			}
			
			var datos = {
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_IdModulo			: 'SOC',
				p_UsaReglasNegocio	: 'S',
				
				p_IdRelacionPersona	: mr.Relacion.IdRelacionPersona,
				p_RegistroCambio	: registroCambio
			};
			
			overlay.encendido = true;
			servicioSociodeNegocio.modificarPersonaRelacion(datos).$promise.then(
				function(result) {
					console.log(result);
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = "La operacion tiene advertencias";
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						msg = "Error al modificar Relación :: " + result.P_OUT_ERRORESDETALLE[0].Codigo;
					}
					else{
						bcsComunes.msg("info","info","Relación agregada");
					}	
					
					if(msg!="")
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						},idVentana);
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					overlay.encendido = false;
				},
				function(result){
					console.log(result);
					bcsComunes.msg("error","error","Error al modificar Relación");
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					overlay.encendido = false;
				});
		}
		//creacion
		else{
			var datos = {
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_IdModulo			: 'SOC',
				p_UsaReglasNegocio	: 'S',
				
				p_IdTipoRelacion		: mr.TipoRelacionObj.IdTipoRelacion,
				p_IdPersonaOrigen		: Number(mr.IdPersona),
				p_IdPersonaDestino		: mr.RelacionadoObj.IdPersona,
				p_IdEstado				: mr.Relacion.IdEstado,
				p_FechaInicioRelacion	: mr.TipoRelacionObj.EsIndefinida === 'N' ? mr.Relacion.FechaInicioRelacion	: '0001-01-01 00:00:00',
				p_FechaFinRelacion		: mr.TipoRelacionObj.EsIndefinida === 'N' ? mr.Relacion.FechaFinRelacion	: '0001-01-01 00:00:00'
			};
			
			overlay.encendido = true;
			servicioSociodeNegocio.crearPersonaRelacion(datos).$promise.then(
				function(result) {
					console.log(result);
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = "La operacion tiene advertencias";
					}
					else if (result.P_OUT_ERRORESCONTADOR){
						msg = "Error al agregar Relación :: " + result.P_OUT_ERRORESDETALLE[0].Codigo;
					}
					else
						bcsComunes.msg("info","info","Relación agregada");
					

					if(msg!="")
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						},idVentana);
					
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					overlay.encendido = false;
				},
				function(result){
					console.log(result);
					bcsComunes.msg("error","error","Error al agregar Relación");
					bcsComunes.ventanaData(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
					overlay.encendido = false;
				});
		}
	};
	
	mr.cancelar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
/*********************************************
 * DATASOURCES
 *********************************************/

	
/*********************************************
 * COMBOBOX
 *********************************************/
	
	mr.conf = {};
	mr.conf.oConfiguracionTipoRelacion={
		tipo : "combobox-tipo-relacion",
		configuracion : {},
		filtros : {
			Administracion : {"field" : "Administracion", "operator" : "eq", "value" : 'RELACIONADOS'}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	mr.conf.oConfiguracionRelacionado={
		tipo : "combobox-persona-tipo-relacion",
		configuracion : {},
		filtros : {
			IdTipoRelacion : {field: "IdTipoRelacion", operator: "eq", value: ""}
		},
		seleccionado : false,
		idActual  : false,
		idDefecto : ''
	};
	
	mr.conf.oConfiguracionSocioNegocio={
		filtros: {
			IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa}
			//EstadoCliente : { "field"   : "EstadoCliente","operator": "eq", "value"   :  'ACT' },
			//EsCliente : {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
		},
	};
	
/*********************************************
 * GRILLAS
 *********************************************/
	
	
/*********************************************
 * FUNCIONES
 ********************************************/
	
	mr.crearPersona = function(){
		var id1 = bcsComunes.ventanaAbrir("CrearPersona",{ },false,true); 
        var promesa=bcsComunes.ventanaPromesa(id1);                               
        promesa.then(function(data){ 
        	if (data === true){
        		mr.oConfiguracionRelacionado.filtros.read = true;
			}
    	}); 
	};

//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
});