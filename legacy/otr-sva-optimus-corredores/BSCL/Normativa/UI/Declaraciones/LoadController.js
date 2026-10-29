angular.module('ModuloNormativa').controller(
        'UploadController',
        [ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils', 'FileUpload',
                UploadController ]);

function UploadController($scope, $translate, bcsComunes, Model, Utils,
        FileUpload) {
	
	var windowParams = Utils.getWindowParams($scope);
	
	if(windowParams.Model){
		Model = windowParams.Model;
	}

    Utils.setWindowTitle($scope, $translate.instant('UI_CARGAR') + ' '
            + Model.getEsquema() + '-' + Model.getVersion());

    var uploadUrl =  '/BSCL/Plataforma/GWSERVICES/files/upload';    
    $scope.fuenteDatos = 'EXTERNA';
    $scope.reemplazar = 'false';
    $scope.grabar = 'false';
    $scope.aceptarFechasNull = 'false';
    $scope.separadorListas = ';';
    $scope.simboloDecimal = ',';
    $scope.tipoArchivoDJ1 = 'FX';
    $scope.fechaDesde = null;
    $scope.fechaHasta = null;
    $scope.Es1720 = (Model.getEsquema() === 'CI1720') ? true : false;
    $scope.EsDJ1 = (Model.getEsquema() === 'DJ1') ? true : false;
    $scope.winID=bcsComunes.ventanaWinId($scope);
    $scope.botonCargarHabilitado = true;
    $scope.cargar = function() {
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
        var args = {
            idDocumento : Model.getDocumento().IdDocumento,
            idUsuario : $scope.SesionIdUsuario,
            schema : Model.getSchema(),
            grabar : $scope.EsDJ1? true : $scope.grabar,
            reemplazar :$scope.EsDJ1? true : $scope.reemplazar,
            aceptarFechasNull : $scope.aceptarFechasNull,
            tipoCarga : $scope.fuenteDatos,
            tipoArchivo : 'CSV',
            separadorListas : $scope.separadorListas,
            simboloDecimal : $scope.simboloDecimal,
            tipoArchivoDJ1: $scope.tipoArchivoDJ1,
            fechaDesde : Utils.formatDate($scope.fechaDesde),
            fechaHasta : Utils.formatDate($scope.fechaHasta)
        };

        bcsComunes.msg('info', 'Subir Archivo', 'En proceso...');
        FileUpload.uploadFileToUrl($scope.loadFile, uploadUrl, args, function(
                reply) {
            if (reply.code === 'OK') {
                bcsComunes.msg('info', 'Subir Archivo', 'OK');
                var winid = bcsComunes.ventanaWinId($scope);
                bcsComunes.ventanaCerrar(winid);
            } else {
                bcsComunes.msg('error', 'Subir Archivo', reply.message);
            }
        }, function(http, status, fnc, httpObj) {
            bcsComunes.msg('error', 'Subir Archivo', 'ERROR');
        });
    }

    function submitirCarga() {
        var params = {
            p_IdEmpresa : $scope.SesionIdEmpresa,
            p_IdUsuario : $scope.SesionIdUsuario,
            p_IdModulo : Model.getModulo(),

            p_IdAgenda : 0,
            p_IdTipoTarea : 'CargaDJ',
            p_Segundos : 0,
            p_CreaParam : 'S',

            p_TablaParametros : [ {
                NombreParametro : 'idDocumento',
                Valor : Model.getDocumento().IdDocumento
            }, {
                NombreParametro : 'idUsuario',
                Valor : $scope.SesionIdUsuario
            }, {
                NombreParametro : 'schema',
                Valor : Model.getSchema()
            }, {
                NombreParametro : 'grabar',
                Valor : $scope.EsDJ1? true : $scope.grabar
            }, {
                NombreParametro : 'reemplazar',
                Valor : $scope.EsDJ1? true : $scope.reemplazar
            }, {
                NombreParametro : 'aceptarFechasNull',
                Valor : 'false'
            }, {
                NombreParametro : 'tipoCarga',
                Valor : $scope.fuenteDatos
            } ]
        };

        var fd = Utils.formatDate($scope.fechaDesde);
        if (fd != null) {
        	params.p_TablaParametros.push({
                NombreParametro : 'fechaDesde',
                Valor : fd
            });
        }

        var fh = Utils.formatDate($scope.fechaHasta);
        if (fh != null) {
        	params.p_TablaParametros.push({
                NombreParametro : 'fechaHasta',
                Valor : fh
            });
        }

        bcsComunes.msg('info', 'Carga', 'En proceso...');
        Model.agregarPrograma(params, function(reply) {
            bcsComunes.msg('info', 'Agregar Programa', 'OK');
            var winid = bcsComunes.ventanaWinId($scope);
            bcsComunes.ventanaCerrar(winid);
        }, function(error) {
            bcsComunes.msg('error', 'Agregar Programa', error.message);
        });
    }
    
    var consultaConfiguracion = new bcs.DataSource({
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
    	var valorParametroCargaExterna = 'S';
    	var filterTemplate = [ { field: "IdNormativa",      operator: "eq", value: Model.getEsquema() },
		                       { field: "IdEmpresa",	    operator: "eq", value: $scope.$root.SesionIdEmpresa },
    						   { field: "CodigoParametro",	operator: "eq", value: Model.getEsquema() + '_PERMITE_CARGA_EXTERNA' }];
    	consultaConfiguracion.query({ filter: filterTemplate }).then(function(){
			if(consultaConfiguracion.view().length > 0) {
				valorParametroCargaExterna	= consultaConfiguracion.view()[0].Valor;
			}
			if (valorParametroCargaExterna == 'N') {
				$scope.fuenteDatos = 'INICIAL';
				$scope.deshabilitaFuenteDatos = true;
			}
			bcsComunes.ventanaOverlay($scope.winID,false);
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
}
