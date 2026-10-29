angular.module('ModuloNormativa').controller(
        'QuadController',
        [ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils', 'FileUpload',
                QuadController ]);

function QuadController($scope, $translate, bcsComunes, Model, Utils) {

	var windowParams = Utils.getWindowParams($scope);
    if(windowParams.Model){
		Model = windowParams.Model;
	}
	
    Utils.setWindowTitle($scope, $translate.instant('UI_CUADRAR') + ' '
            + Model.getEsquema() + '-' + Model.getVersion());

    $scope.fechaDesde = null;
    $scope.fechaHasta = null;

    $scope.botonCuadrarHabilitado = true;
    $scope.cuadrar = function() {
        $scope.botonCuadrarHabilitado = false;

        try {
            if (listTiposCuadraturas.value() === 'VALIDACION') {
                submitirValidacion();
            } else {
                submitirCuadratura();
            }
        } catch (err) {
            bcsComunes.msg('error', 'Cuadratura Datos', err);
        }

        $scope.botonCuadrarHabilitado = true;
    }

    // DataSource de las cuadraturas especificadas.
    var listTiposCuadraturas = null;
    var esquema = Model.getEsquema();
    var version = Model.getVersion();
    var dataSourceCuadratura = Model.getDataSourceCuadratura(esquema, version);
    dataSourceCuadratura.fetch().then(function() {
        var list = [];
        list.push({
            Tipo : 'VALIDACION',
            Descripcion : 'Validacion'
        });
        list.push({
            Tipo : 'INDIVIDUAL',
            Descripcion : 'Individual'
        });

        var cuadraturas = dataSourceCuadratura.data();
        for (var i = 0; i < cuadraturas.length; i++) {
            var cuadratura = cuadraturas[i];
            list.push({
                Tipo : cuadratura.Tipo,
                Descripcion : cuadratura.Descripcion
            });
        }

        $('#tipoCuadratura').kendoDropDownList({
            dataTextField : 'Descripcion',
            dataValueField : 'Tipo',
            dataSource : list,
            autobind : true,
            index : 0
        });
        listTiposCuadraturas = $('#tipoCuadratura').data('kendoDropDownList');
    });

    // DataSource de los clasificadores especificados.
    var listTiposClasificadores = null;
    var esquema = Model.getEsquema();
    var version = Model.getVersion();
    var dataSourceClasificador = Model.getDataSourceClasificador(esquema,
            version);
    dataSourceClasificador.fetch().then(
            function() {
                var list = [];
                list.push({
                    Tipo : '',
                    Descripcion : 'Ninguno'
                });

                var clasificadores = dataSourceClasificador.data();
                for (var i = 0; i < clasificadores.length; i++) {
                    var clasificador = clasificadores[i];
                    list.push({
                        Tipo : clasificador.Tipo,
                        Descripcion : clasificador.Descripcion
                    });
                }

                $('#tipoClasificador').kendoDropDownList({
                    dataTextField : 'Descripcion',
                    dataValueField : 'Tipo',
                    dataSource : list,
                    autobind : true,
                    index : 0
                });
                listTiposClasificadores = $('#tipoClasificador').data(
                        'kendoDropDownList');
            });

    function submitirValidacion() {
        var params = {
            p_IdEmpresa : $scope.SesionIdEmpresa,
            p_IdUsuario : $scope.SesionIdUsuario,
            p_IdModulo : Model.getModulo(),

            p_IdAgenda : 0,
            p_IdTipoTarea : 'ValidacionDJ',
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

        bcsComunes.msg('info', 'Validacion', 'En proceso...');
        Model.agregarPrograma(params, function(reply) {
            bcsComunes.msg('info', 'Agregar Programa', 'OK');
            var winid = bcsComunes.ventanaWinId($scope);
            bcsComunes.ventanaCerrar(winid);
        }, function(error) {
            bcsComunes.msg('error', 'Agregar Programa', error.message);
        });
    }

    function submitirCuadratura() {
        var params = {
            p_IdEmpresa : $scope.SesionIdEmpresa,
            p_IdUsuario : $scope.SesionIdUsuario,
            p_IdModulo : Model.getModulo(),

            p_IdAgenda : 0,
            p_IdTipoTarea : 'CuadraturaDJ',
            p_Segundos : 0,
            p_CreaParam : 'S',

            p_TablaParametros : [ {
                NombreParametro : 'tipoCuadratura',
                Valor : listTiposCuadraturas.value()
            }, {
                NombreParametro : 'tipoClasificador',
                Valor : listTiposClasificadores.value()
            }, {
                NombreParametro : 'idDocumento',
                Valor : Model.getDocumento().IdDocumento
            }, {
                NombreParametro : 'idUsuario',
                Valor : $scope.SesionIdUsuario
            }, {
                NombreParametro : 'schema',
                Valor : Model.getSchema()
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

        bcsComunes.msg('info', 'Cuadratura', 'En proceso...');
        Model.agregarPrograma(params, function(reply) {
            bcsComunes.msg('info', 'Agregar Programa', 'OK');
            var winid = bcsComunes.ventanaWinId($scope);
            bcsComunes.ventanaCerrar(winid);
        }, function(error) {
            bcsComunes.msg('error', 'Agregar Programa', error.message);
        });
    }
}
