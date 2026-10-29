angular.module('ModuloCuentaInversion').controller('ConsultaAnexo7Ctrl',function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope,bcsAlerta,OPCIONES_BCSALERTA,
		bcsServiciosCuentaInversion, bcsCuentaInversionGrid, bcsServicioPlataforma,bcsFormatoDocumentosCuentaInversion){
	var anexo7 = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var datosGuardar = {};
	var datosPorGuardar = false;
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	
	anexo7.filtros = {
			periodo : util.dateInicioDia($scope.SesionFechaSistema.substring(0,7) + '-01')
	};
			
	anexo7.generar = function(){

		if (parametrosVentana.data.length > 0)
		{
			bcsComunes.ventanaOverlay(idVentana,true);
			
			if(anexo7.filtros.tipoIndice === 'ME'){
				for(var campo in parametrosVentana.data){
					if(parametrosVentana.data[campo].NombreParametro === 'FechaDesde')
						parametrosVentana.data[campo].Valor = util.dateSPdate(new Date(anexo7.filtros.periodo.getFullYear(), anexo7.filtros.periodo.getMonth(), 1));
					if(parametrosVentana.data[campo].NombreParametro === 'FechaHasta')
						parametrosVentana.data[campo].Valor = util.dateSPdate( new Date(anexo7.filtros.periodo.getFullYear(), anexo7.filtros.periodo.getMonth() + 1, 0));
				}
			}
			
			if(anexo7.filtros.tipoIndice === 'AN'){
				for(var campo in parametrosVentana.data){
					if(parametrosVentana.data[campo].NombreParametro === 'FechaDesde')
						parametrosVentana.data[campo].Valor = util.dateSPdate(new Date(anexo7.filtros.periodo.getFullYear(), 0, 1));
					if(parametrosVentana.data[campo].NombreParametro === 'FechaHasta')
						parametrosVentana.data[campo].Valor = util.dateSPdate( new Date(anexo7.filtros.periodo.getFullYear(), 11, 31));
				}
			}
			
			parametrosVentana.data.push({NombreParametro : 'TipoInformacion', Valor : anexo7.filtros.tipoIndice === 'AN' ? '01' : '03'});
			
			var datos = {};
			datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario               = $scope.SesionIdUsuario; 
			datos.p_IdModulo                = 'CTA';
			datos.p_UsaReglasNegocio        = 'S';
			datos.p_IdTipoPrograma		    = 'ANEXO7_SALDOS_DE_CLIENTES';		
			datos.p_ParametrosPrograma	    = parametrosVentana.data;
			bcsComunes.log().info(JSON.stringify(datos));
			
			bcsServiciosCuentaInversion.SolicitudGenerarArchivo(datos).$promise.then(
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(result)
					},idVentana);
					bcsComunes.ventanaCerrar(idVentana);
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);					
					bcsComunes.msg("error","Error",$translate.instant('UI_SOLICITUD_GENERACION_ARCHIVO_ERROR'));
				}
			);
		}
	}
	
	$scope.$watch('anexo7.filtros.tipoIndice',function(newVal,oldVal){
		if(newVal && anexo7.filtros.periodo){
			if(newVal==='ME') {
				anexo7.filtros.periodo = util.dateInicioDia($scope.SesionFechaSistema.substring(0,7) + '-01');
				anexo7.configuracionPeriodo = {
					start: "year",
					depth: "year",
					format: "MM-yyyy"
				}
			} else {
				anexo7.configuracionPeriodo = {
					start: "decade",
					depth: "decade",
					format: "yyyy"
				}
			}
		}
	});
});