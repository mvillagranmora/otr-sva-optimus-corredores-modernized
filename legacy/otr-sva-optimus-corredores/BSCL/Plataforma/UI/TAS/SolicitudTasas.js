(function() {
	angular.module('ModuloPlataforma').controller('SolicitudTasasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosTasas,$http,$timeout){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var sTas 		= this;
		var fuentesTree = [];
		sTas.idVentana	= bcsComunes.ventanaWinId($scope);
		sTas.cmbbx		= {};
		sTas.filtro		= {};
		sTas.fn			= {};
		
		sTas.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		var fuentesSeleccionadas = new Set();
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		sTas.cmbbx.Fuente={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'},
				NombreEntidad:{field: "NombreEntidad",	operator: "eq", value: 'MANTENEDOR'}
			}
		};

		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		 //generar archivo
		sTas.fn.generarArchivo = function(){
			var fuentesSeleccionadasArray = [...fuentesSeleccionadas]; 
			if(fuentesSeleccionadasArray == ''){
				console.log("Las fuentes estan vacias...");
				return;
			}
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'PLA';
	        parametrosSP.p_UsaReglasNegocio	= 'N';
	        
	        parametrosSP.p_Fecha		= util.dateSPdate(sTas.filtro.Fecha);
	        parametrosSP.p_Fuentes	= fuentesSeleccionadasArray;
	        
	        if(!parametrosSP.p_Fecha || !parametrosSP.p_Fuentes) return;
	        
        	bcsComunes.ventanaOverlay(sTas.idVentana,true);
        	bcsServiciosTasas.solicitudArchivoTasas(parametrosSP).$promise.then(function(result) {
				var errores = {
				    "ERROR_NO_CIERRE_MESARF": $translate.instant("ERROR_NO_CIERRE_MESARF"),
				    "ERROR_CONSULTAR_FUENTE": $translate.instant("ERROR_CONSULTAR_FUENTE"),
				    "ERROR_NO_CONFIGURACION_TASAS": $translate.instant("ERROR_NO_CONFIGURACION_TASAS"),
				    "ERROR_DOCUMENTO_IDENTIDAD": $translate.instant("ERROR_DOCUMENTO_IDENTIDAD"),
				    "ERROR_SELECCIONAR_INSTRUMENTO": $translate.instant("ERROR_SELECCIONAR_INSTRUMENTO"),
				    "ERROR_SELECT_INSERT_INSTRUMENTOS_TASAS": $translate.instant("ERROR_SELECT_INSERT_INSTRUMENTOS_TASAS"),
				};
				
				if (result.P_OUT_ERRORESCONTADOR !== 0) {
				    for (const mensaje of result.P_OUT_MENSAJES) {
				        const resultado = mensaje.codigo;
				        const mensajeError = errores[resultado];
				        if (mensajeError) {
				            bcsComunes.msg("error", $translate.instant("UI_SOLICITUD_TASAS"), mensajeError);
				            bcsComunes.ventanaOverlay(sTas.idVentana, false);
				            return;
				        }
				    }
				}
				else{
					function calcularTiempoTotal(arr) {
					    var tiempoPorArchivo = 2000;
					    return arr.length * tiempoPorArchivo;
					}
					
					var tiempoTotal = calcularTiempoTotal(result.nombreArchivo);
					
					for (var i = 0; i < result.nombreArchivo.length; i++) {
					    (function (indice) {
					        var datos = result.nombreArchivo[indice];
					        console.log("Mis datos son: ", datos);
					        var ruta = 'tas/';
					        $timeout(function () {
					            var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
					            var data = { file: ruta + datos };
					            $http({
					                method: 'POST',
					                url: url,
					                params: data
					            }).success(function (data) {
					                var file = new File([data], datos);
					                saveAs(file);
					                bcsComunes.msg("info", $translate.instant("UI_SOLICITUD_TASAS"), $translate.instant("UI_ARCHIVO_TASAS_OK"));
					                bcsComunes.ventanaOverlay(sTas.idVentana, false);
					            }).error(function (err) {
					                bcsComunes.msg("error", $translate.instant("UI_SOLICITUD_TASAS"), $translate.instant('UI_ARCHIVO_TASAS_ERROR'));
					                bcsComunes.ventanaOverlay(sTas.idVentana, false);
					            });
					        }, tiempoTotal);
					    })(i);
					}
					
				}
    			
        	},
	        	function(result){
	        		bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_TASAS"),$translate.instant("UI_ARCHIVO_TASAS_ERROR"));
	        		bcsComunes.ventanaOverlay(sTas.idVentana,false);
	        	}
        	);
		};
		
		//creación del arbol
	    function onCheck(e) {
			var dataItem = $scope.tree.dataItem(e.node); //elemento checkeado
			if (dataItem.checked) {
			    fuentesSeleccionadas.add(dataItem.idPerfil);
			} else {
			    fuentesSeleccionadas.delete(dataItem.idPerfil);
			}
	    }
	    
	   	$scope.options = {
	      checkboxes: true,
	      dataSource: fuentesTree,
	      check: onCheck
    	};
    	
    	var FuenteDataSource;
    	
    	let Constructor_TreeList = function(){
			fuentesTree = [];
			bcsComunes.ventanaOverlay(sTas.idVentana,true);
			FuenteDataSource = GetData();
			FuenteDataSource.query().then(function() {	
				$.each(FuenteDataSource.view(),function(key,value){
					fuentesTree.push({idPerfil: value.IdFuente, text: $translate.instant(value.CodigoDescripcion), expanded: false, items:[]});
				});
			}).then(function(){
				$scope.tree.setDataSource(fuentesTree);
				isTreeComplete();
			});
		};
	
		Constructor_TreeList();
		
		function GetData(){
			return new kendo.data.DataSource({
				type: "odata",
				serverFiltering: true,
				serverSorting : true,
				transport: {
					read: {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=600",
						dataType: "json"
				    }
				},
				filter: [
					{field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa},
					{field: "IdEstado",	operator: "eq", value: 'ACT'},
					{field: "NombreEntidad",	operator: "eq", value: 'MANTENEDOR'}
				],
				schema: {
		        	model:
		    		{
		        		fields : {
		        			IdFuente: {type: "number"},
							CodigoDescripcion: {type: "string"},
							NombreEntidad: {type:"string"}
						}
		    		}
		    	},
		    	sort : [{ field: "CodigoDescripcion", dir: "asc"}],
			});
		}
		
		function isTreeComplete(){
			if($scope.tree==undefined){
				setTimeout(isTreeComplete, 1000);
			}else{
				bcsComunes.ventanaOverlay(sTas.idVentana,false);
			}
		}
		
		bcsComunes.ventanaAutoHeight(sTas.idVentana);
	});
}());