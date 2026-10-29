/********************************************************************
 * Purpose	: Controlador para generar Reportes SICAM texto plano
 * Author	: AJARA - BCS
 * Date		: 10/07/2025
 *******************************************************************/

(function() {
	angular.module('ModuloNormativa').controller('GenerarRepoSicamCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http, bcsServiciosNormativa,Model, Utils){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var solRepo = this;
		var windowParams = Utils.getWindowParams($scope);
		solRepo.idVentana = bcsComunes.ventanaWinId($scope); 
		solRepo.params = bcsComunes.ventanaParams(solRepo.idVentana);
		solRepo.domVentana = $("[ventanaid='"+solRepo.idVentana+"']");
		bcsComunes.ventanaAutoOverlay(solRepo.idVentana,false,3000);
		
		solRepo.AnchoVentana=bcsComunes.getAncho(solRepo.idVentana);
		
		solRepo.cmbbx		= {};
		

		solRepo.modulo = 'NOR';
		solRepo.IdEstadoDocumento='PEN';
		solRepo.esquema=solRepo.params.esquema;
		solRepo.version=solRepo.params.version;
		solRepo.isREPOFechasBloqueadas = (solRepo.esquema === 'REPO3P1' || solRepo.esquema === 'REPO2P2');
		solRepo.fechaInicio = solRepo.params.fechaDesde;
		solRepo.fechaFin = solRepo.params.fechaHasta;
		
		if(windowParams.Model){
			Model = windowParams.Model
		}
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
	
    	solRepo.fnTabINFSICAM = function() {

     			solRepo.domVentana.find('#PanelINFSICAM').append($compile('<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/BCCH/GenerarREPO/Paneles/PanelGenerarREPO.html\'" ></div>')($scope));

     	};
   	     	
     	
/*********************************************
 * DATASOURCES
 *********************************************/
	// --------------------------------------------------
	// IMPRIMIR REPORTE
	// --------------------------------------------------
	solRepo.ConsultaArchivoNormativa = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1674",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
					    IdArchivoNormativa		: {type: "string"},
						VersionArchivo			: {type: "string"},
						Esquema					: {type: "string"},
						Version 				: {type: "string"},
						IdTipoDocumento 		: {type: "string"},
						Descripcion				: {type: "string"},
						NombreArchivo			: {type: "string"},
						IdArchivo				: {type: "string"},
						TipoArchivo				: {type: "string"},
						ServicioArchivo			: {type: "string"},
						IdEmpresa				: {type: "number"},
						IdEstado				: {type: "string"}						
					}
				}
			}
		},
	});		

/*********************************************
 * FUNCIONES
 ********************************************/
	solRepo.fnAjustaPantalla = function () {
		bcsComunes.ventanaCambiarTamano(solRepo.idVentana,300,280);
		//ancho, alto
	};

	solRepo.ajustarFecha = function(fecha){
		if (!fecha) return null;
		if (typeof fecha === "string") {
			return fecha;
		}
		if (fecha instanceof Date) {
			const yyyy = fecha.getFullYear();
			const mm = String(fecha.getMonth() + 1).padStart(2, '0');
			const dd = String(fecha.getDate()).padStart(2, '0');
			return `${yyyy}-${mm}-${dd}`;
		}
		if (typeof fecha === "string" && fecha.includes("T")) {
			return fecha.split("T")[0];
		}
		return null;
	}

	solRepo.solicitar = function() {
		var params = {
			p_IdEmpresa     :   $scope.SesionIdEmpresa,
			p_IdUsuario     :   $scope.SesionIdUsuario,
			p_IdModulo      :   Model.getModulo(),
			p_IdEsquema     :   solRepo.esquema, 
			p_Version       :   solRepo.version,
			p_FechaInicio	:	solRepo.ajustarFecha(solRepo.fechaInicio),
			p_FechaFin		:   solRepo.ajustarFecha(solRepo.fechaFin)
		};
		bcsServiciosNormativa.GenerarREPO(params).$promise.then(function(respuesta){
			if(respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.msg('info', 'Agregar Programa', 'OK');
				var winid = bcsComunes.ventanaWinId($scope);
				bcsComunes.ventanaCerrar(winid);
			}else{
				bcsComunes.msg('error', 'Agregar Programa', error.message);
			}
		})
	}	
	});										
}())