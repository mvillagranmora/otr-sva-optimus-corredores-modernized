angular.module('ModuloNormativa').controller('GenControllerRMO', function GenControllerRMO($scope, $translate, bcsComunes, Model, Utils) {
    
    const windowParams = Utils.getWindowParams($scope);
    const datos = windowParams.datos;
    const idVentana = bcsComunes.ventanaWinId($scope);
    Utils.setWindowTitle($scope, $translate.instant('UI_GENERAR') + ' RMO')
    
    $scope.botonGenerarHabilitado = true;
    $scope.fechaDesde = new Date(windowParams.fechaDesde);
    $scope.fechaHasta = new Date(windowParams.fechaHasta);
    $scope.chkFormatoTXT = true;
    
    let year = datos.version.substring(0,4);
	let month = datos.version.substring(5,7).replace(/^0+/,"") - 1;
	let startDate = new Date(year, month, 1);
	let endDate = new Date(year, month + 1, 0);
	
	$scope.dateOptions = { min: startDate, max: endDate};

 	$scope.generarRMO = function() {
        $scope.botonGenerarHabilitado = false;

        try {
            submitirGeneracion();
        } catch (err) {
            bcsComunes.msg('error', 'Generacion Documento', err);
        }

        $scope.botonGenerarHabilitado = true;
    }
    
	let datosResumenGeneracion = 
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
	
	let filtro = Array();
	filtro.push({ field: "IdDocumento" , operator: "eq", value: Number(datos.idDocumento) });
	
	let ConsultaResumenGeneracion = new bcs.DataSource(datosResumenGeneracion);
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

    function submitirGeneracion() {
		let formatoArchivo = $scope.chkFormatoTXT ? 'txt' : 'csv';
        let params = {
            p_IdEmpresa : $scope.SesionIdEmpresa,
            p_IdUsuario : $scope.SesionIdUsuario,
            p_IdModulo : datos.modulo,

            p_IdAgenda : 0,
            p_IdTipoTarea : 'GeneracionDJ',
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
                NombreParametro : 'modoPresentacion',
                Valor : $scope.modoPresentacion
            }, {
                NombreParametro : 'numeroPresentacion',
                Valor : 1
            }, {
                NombreParametro : 'periodicidad',
                Valor : windowParams.periodicidad
            }, {
                NombreParametro : 'extensionArchivo',
                Valor : formatoArchivo
            }]
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

        bcsComunes.msg('info', 'Generacion', 'En proceso...');
        Model.agregarPrograma(params, function(reply) {
            bcsComunes.msg('info', 'Agregar Programa', 'OK');
            bcsComunes.ventanaCerrar(idVentana);
        }, function(error) {
            bcsComunes.msg('error', 'Agregar Programa', error.message);
        });
    }

   /*********************************************
	* WATCHES
	*********************************************/

	$scope.$watch('chkFormatoTXT',function(newval){
		switch (newval){
			case true:  
				$scope.chkFormatoCSV = false;
				break;
			case false: 
				$scope.chkFormatoCSV = true;				
				break;
		}					
	});
		
	$scope.$watch('chkFormatoCSV',function(newval){
		switch (newval){
			case true:  
				$scope.chkFormatoTXT = false;
				break;
			case false: 
				$scope.chkFormatoTXT = true;				
				break;
		}		
	});
});
