angular.module('ModuloNormativa').controller(
        'GenController',
        [ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils', 'FileUpload',
                GenController ]);

function GenController($scope, $translate, bcsComunes, Model, Utils) {

	var windowParams = Utils.getWindowParams($scope);

	if(windowParams.Model){
		Model = windowParams.Model
	}

    Utils.setWindowTitle($scope, $translate.instant('UI_GENERAR') + ' '
            + Model.getEsquema() + '-' + Model.getVersion());

    $scope.botonGenerarHabilitado = true;

    $scope.mostrarFormNormal =  Model.getEsquema() !== 'DJ1' ? true : false;

 	$scope.generar = function() {
        $scope.botonGenerarHabilitado = false;

        try {
            submitirGeneracion();
        } catch (err) {
            bcsComunes.msg('error', 'Generacion Documento', err);
        }

        $scope.botonGenerarHabilitado = true;
    }
    
	var datosResumenGeneracion = 
	{
		type : "odata",
		definition : 
		{
			pageSize        : 10,
			transport		:
			{
				read:
				{
					url 		: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700018",
					dataType	: "json"
				}
			},
            schema:
			{ 
				model: 
                {
					fields: 
					{
						"IdDocumento"		: {type: "number"},
						"UltimoCorrelativo"	: {type: "number"}
					}
				}
			}
		}
	};
	
	var filtro = Array();
	filtro.push({ field: "IdDocumento" , operator: "eq", value: Number(Model.getDocumento().IdDocumento) });
	
	var ConsultaResumenGeneracion = new bcs.DataSource(datosResumenGeneracion);
	ConsultaResumenGeneracion.query({ filter: filtro }).then(function(){
		if(ConsultaResumenGeneracion.view().length > 0)
		{	
		    $('#numeroPresentacion').kendoNumericTextBox({
		        format : 'n0',
		        value : ConsultaResumenGeneracion.view()[0].UltimoCorrelativo + 1
		    });
		}
		else
		{
		    $('#numeroPresentacion').kendoNumericTextBox({
		        format : 'n0',
		        value : 1
		    });
		}
	});

    $scope.modoPresentacion = 'INTERNET';

    $scope.rectificador = 'N'; 

    function submitirGeneracion() {
		
		var FechaDesde = '0001-01-01';
		if (windowParams.FechaDesde!=null && Model.getEsquema()=='DJ1') {
			if (windowParams.FechaDesde instanceof Date) {
				FechaDesde = util.dateSPdate(windowParams.FechaDesde);
			} else {
				FechaDesde = windowParams.FechaDesde;
			}
		}

        var params = {
            p_IdEmpresa : $scope.SesionIdEmpresa,
            p_IdUsuario : $scope.SesionIdUsuario,
            p_IdModulo : Model.getModulo(),
            p_IdAgenda : 0,
            p_IdTipoTarea : 'GeneracionDJ',
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
                NombreParametro : 'modoPresentacion',
                Valor : $scope.modoPresentacion
            }, {
                NombreParametro : 'numeroPresentacion',
                Valor : $('#numeroPresentacion')[0].value
            }, {
                NombreParametro : 'periodicidad',
                Valor : windowParams.Periodicidad
            }, {
                NombreParametro : 'periodoComercial',
                Valor : FechaDesde
            }, {
                NombreParametro : 'rectifica',
                Valor : windowParams.Rectifica
            }]
        };
        if ($scope.mostrarFormNormal) {
            params.p_TablaParametros.push({
                NombreParametro: 'rectificador',
                Valor: $scope.rectificador
            })};

        bcsComunes.msg('info', 'Generacion', 'En proceso...');
        Model.agregarPrograma(params, function(reply) {
            bcsComunes.msg('info', 'Agregar Programa', 'OK');
        }, function(error) {
            bcsComunes.msg('error', 'Agregar Programa', error.message);
        });
    }

}
