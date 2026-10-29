angular.module('ModuloNormativa').controller('LoadControllerRMO',function LoadControllerRMO($scope, $translate, bcsComunes, Model, Utils, FileUpload) {

    const uploadUrl =  '/BSCL/Plataforma/GWSERVICES/files/upload';
    const windowParams = Utils.getWindowParams($scope);
    const datos = windowParams.datos;
    const idVentana = bcsComunes.ventanaWinId($scope);
    
    Utils.setWindowTitle($scope, $translate.instant('UI_CARGAR') + ' RMO');
      
    $scope.fuenteDatos = 'EXTERNA';
    $scope.reemplazar = 'false';
    $scope.grabar = 'false';
    $scope.aceptarFechasNull = 'false';
    $scope.separadorListas = ';';
    $scope.simboloDecimal = ',';
    $scope.fechaDesde = new Date(windowParams.fechaDesde);
    $scope.fechaHasta = new Date(windowParams.fechaHasta);
    $scope.EsDJ999 = (datos.esquema === 'DJ999') ? true : false;
    $scope.winID=bcsComunes.ventanaWinId($scope);
    $scope.botonCargarHabilitado = true;
    
    $scope.cargarRMO = function() {
        $scope.botonCargarHabilitado = false;

        try {
            if ($scope.fuenteDatos === 'EXTERNA') {
                subirArchivo();
            } else if ($scope.fuenteDatos === 'INICIAL') {
                submitirCarga();
            }
        } catch (err) {
            bcsComunes.msg('error', 'Cargar Archivo', err);
        }

        $scope.botonCargarHabilitado = true;
    }

    $scope.loadFile = null;
    function subirArchivo() {
        let args = {
            idDocumento : datos.idDocumento,
            idUsuario : $scope.SesionIdUsuario,
            schema : datos.esquemaDatos,
            grabar : 'true',
            reemplazar : 'true',
            aceptarFechasNull : $scope.aceptarFechasNull,
            tipoCarga : $scope.fuenteDatos,
            tipoArchivo : 'CSV',
            separadorListas : $scope.separadorListas,
            simboloDecimal : $scope.simboloDecimal,
            fechaDesde : Utils.formatDate($scope.fechaDesde),
            fechaHasta : Utils.formatDate($scope.fechaHasta)
        };

        bcsComunes.msg('info', 'Subir Archivo', 'En proceso...');
        FileUpload.uploadFileToUrl($scope.loadFile, uploadUrl, args, function(
                reply) {
            if (reply.code === 'OK') {
                bcsComunes.msg('info', 'Subir Archivo', 'OK');
                bcsComunes.ventanaCerrar(idVentana);
            } else {
                bcsComunes.msg('error', 'Subir Archivo', reply.message);
            }
        }, function() {
            bcsComunes.msg('error', 'Subir Archivo', 'ERROR');
        });
    }

    function submitirCarga() {
        let params = {
            p_IdEmpresa : $scope.SesionIdEmpresa,
            p_IdUsuario : $scope.SesionIdUsuario,
            p_IdModulo : datos.modulo,

            p_IdAgenda : 0,
            p_IdTipoTarea : 'CargaDJ',
            p_Segundos : 0,
            p_CreaParam : 'S',

            p_TablaParametros : [ {
                NombreParametro : 'idDocumento',
                Valor : datos.idDocumento
            }, {
                NombreParametro : 'idUsuario',
                Valor : $scope.SesionIdUsuario
            }, {
                NombreParametro : 'schema',
                Valor : datos.esquemaDatos
            }, {
                NombreParametro : 'grabar',
                Valor : 'true'
            }, {
                NombreParametro : 'reemplazar',
                Valor : 'true'
            }, {
                NombreParametro : 'aceptarFechasNull',
                Valor : 'false'
            }, {
                NombreParametro : 'tipoCarga',
                Valor : $scope.fuenteDatos
            } ]
        };

        let fechaDesde = Utils.formatDate($scope.fechaDesde);
        if (fechaDesde != null) {
        	params.p_TablaParametros.push({
                NombreParametro : 'fechaDesde',
                Valor : fechaDesde
            });
        }

        let fechaHasta = Utils.formatDate($scope.fechaHasta);
        if (fechaHasta != null) {
        	params.p_TablaParametros.push({
                NombreParametro : 'fechaHasta',
                Valor : fechaHasta
            });
        }

        bcsComunes.msg('info', 'Carga', 'En proceso...');
        Model.agregarPrograma(params, function() {
            bcsComunes.msg('info', 'Agregar Programa', 'OK');
            bcsComunes.ventanaCerrar(idVentana);
        }, function(error) {
            bcsComunes.msg('error', 'Agregar Programa', error.message);
        });
    }
    
    let consultaConfiguracion = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4172",
					dataType : "json",
				}
			},
			schema : {
				model: {
					fields : {
					    IdEmpresa		    : {type: "string"},
						CodigoParametro		: {type: "string"},
						Descripcion			: {type: "string"},
						Valor 				: {type: "string"},
						IdNormativa 		: {type: "string"}				
					}
				}
			}
		},
	});	
    
    function consultarCargaExterna() {
    	bcsComunes.ventanaOverlay($scope.winID,true);
    	let valorParametroCargaExterna = 'S';
    	let filterTemplate = [ { field: "IdNormativa",      operator: "eq", value: datos.esquema },
		                       { field: "IdEmpresa",	    operator: "eq", value: $scope.$root.SesionIdEmpresa },
    						   { field: "CodigoParametro",	operator: "eq", value: 'PERMITE_CARGA_EXTERNA' }];
    	consultaConfiguracion.query({ filter: filterTemplate }).then(function(){
			if(consultaConfiguracion.view().length > 0) {
				valorParametroCargaExterna	= consultaConfiguracion.view()[0].Valor;
			}
			if (valorParametroCargaExterna == 'N') {
				$scope.fuenteDatos = 'INICIAL';
				$scope.deshabilitaFuenteDatos = true;
			}
			bcsComunes.ventanaOverlay(idVentana,false);
		});
    }
    consultarCargaExterna();

    $scope.fileChanged = function(input) {
        $scope.nombreArchivo = '';
        if(input.files[0]){
            $scope.fileName = input.files[0].name;
            if($scope.fileName){
                    $scope.nombreArchivo = $scope.fileName;
            }
        }
    };
});
