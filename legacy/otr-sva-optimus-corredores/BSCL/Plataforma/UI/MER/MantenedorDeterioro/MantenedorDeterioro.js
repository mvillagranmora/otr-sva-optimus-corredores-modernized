angular.module('ModuloPlataforma').controller
('MantenedorDeterioroCtrl', function($scope, bcsComunes, bcsServicioPlataforma, $translate, bcsAlerta, OPCIONES_BCSALERTA) {
  
    $scope.data = {};
    $scope.showDetalle = false;
    $scope.filtros = {};
    $scope.accionBoton = $scope.data.AccionBoton;
    $scope.emisores = [];

    var idVentana = bcsComunes.ventanaWinId($scope);
    $scope.winID=bcsComunes.ventanaWinId($scope);
    var overlay = bcsComunes.overlay;

     ModeloFlags = function() {
        return {
            model: {
                fields:{
					IdDeterioroEmisor: {type : "number"},
                    Emisor: { type : "string"},
                    FlagDeterioro: { type : "string"},
                    PorcentajeDeterioro: { type : "number"},
                    Fase: { type : "number"}
                } 
            }
        };
    };
     dataSourceFlags = function() {
        return new bcs.DataSource({
            type : "odata", 
            definition : {
                serverFiltering : true, 
                serverPaging : true, 
                serverSorting : true, 
                pageSize : 50,
                transport : {
                    read : {
                        url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=55669900",
                        dataType : "json"
                    }
                },
                schema : ModeloFlags(),
            }
        });
    };

		var columnasFlags = function() {
            return [ 
                { field: "Emisor", title: "Emisor", width: "150px", attributes: { style: "text-align:center;" } },
                { field: "FlagDeterioro", title: "Flag Deterioro", width: "150px", attributes: { style: "text-align:center;" } },
                { field: "PorcentajeDeterioro", title: "% Deterioro", width: "75px", attributes: { style: "text-align:center;" } },
                { field: "Fase", title: "Fase", width: "100px", attributes: { style: "text-align:center;" } }
            ];
	};

	$scope.flags = {
        dataSource: dataSourceFlags(),
        columns: columnasFlags(),      
        sortable: true,
        pageable: true,
        autoBind: true,
        height: 280,
        selectable : "row",
        userSettings: true,
        navigatable: true,
        excel : {
            allPages: true,
            },
        filterable: true,
	};

	$scope.data.grilla = $scope.flags;

    $scope.emisoresDataSource = dataSourceFlags();
    $scope.emisoresDataSource.fetch(function(){
        let data = this.data();
        $scope.$apply(function(){
            $scope.emisores = data;
        });
    });
    
	$scope.existeEmisor = function(emisor) {
        const emisorExistente = $scope.emisores.find(function(e) {
            return e.Emisor === emisor;
        });
        return emisorExistente ? emisorExistente.IdDeterioroEmisor : null;
    };
	
    $scope.CrearDeterioro = function() {
        $scope.accionBoton = false;
        const ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarDeterioro", { AccionBoton :'CREAR'}, true, false, idVentana);      
        bcsComunes.ventanaPromesa(ventanaModalId).then(function(){
            $scope.data.flagSeleccionado = undefined;
            $scope.data.grilla.dataSource.read();
        });
    };

	$scope.ModificarDeterioro = function() {
        $scope.accionBoton = false;
        const ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarDeterioro", 
        {
            IdDeterioroEmisor: $scope.data.flagSeleccionado.IdDeterioroEmisor,
            Emisor: $scope.data.flagSeleccionado.Emisor,
            FlagDeterioro: $scope.data.flagSeleccionado.FlagDeterioro === 'S' ? true : false,
            PorcentajeDeterioro: $scope.data.flagSeleccionado.PorcentajeDeterioro,
            Fase: $scope.data.flagSeleccionado.Fase,
            AccionBoton: 'MODIFICAR'
        }, true, false, idVentana);

        bcsComunes.ventanaPromesa(ventanaModalId).then(function(){
            $scope.data.flagSeleccionado = undefined;
            $scope.data.grilla.dataSource.read();
        });
	};
	
    $scope.cancelar = function() {
        bcsComunes.ventanaCerrar($scope.idVentana);
    };

    const successCrearModificarDeterioro = function (respuesta) {
     
        let mensaje = ''
       if(respuesta.P_OUT_ERRORESCONTADOR === 0){
            mensaje = "Acción realizada exitosamente"		
       }else{
            mensaje = $translate.instant("UI_ERROR");
            let mensajeTotal = new Array();
            $.each(respuesta.P_OUT_MENSAJES,function(key,value){
                let objMensaje = {
                        Id : value.id,
                        tipo : value.tipo,
                        codigo : value.codigo,
                        descripcion : $translate.instant(value.descripcion)
                };
                mensajeTotal.push(objMensaje);
            });
            respuesta.P_OUT_MENSAJES = mensajeTotal;
       }

        let idModal = bcsComunes.ventanaMsg({
            mensaje: mensaje,
            conflictos:util.parseServerResponse(respuesta)
        },idVentana);

        bcsComunes.ventanaPromesa(idModal).then(function(){
            bcsComunes.ventanaOverlay(idVentana,false);
            $scope.data.grilla.dataSource.read();
         });

    };
    
    const errorCrearModificarDeterioro = function () {
        bcsComunes.ventanaOverlay(idVentana,false);
		bcsComunes.msg("error","error",$translate.instant("UI_ERROR"));
    };

    $scope.EliminarDeterioro = function(flagSeleccionado) {

        const t = bcsAlerta.crear('PREGUNTA', $translate.instant('UI_ELIMINAR'));
		t.result.then(function (result) {
			if (result === OPCIONES_BCSALERTA.ACEPTAR) {
                bcsComunes.ventanaOverlay(idVentana, true);
                $scope.AccionBoton = 'MODIFICAR';
                var Flag = [{
                    IdDeterioroEmisor: flagSeleccionado.IdDeterioroEmisor,
                    Emisor: flagSeleccionado.Emisor,
                    FlagDeterioro: flagSeleccionado.FlagDeterioro ? 'S' : 'N',
                    PorcentajeDeterioro: flagSeleccionado.PorcentajeDeterioro,
                    Fase: flagSeleccionado.Fase,
                    Accion: 'MODIFICAR',
                    IdEstado: 'INA'
                }];
                
                var datos = {
                    p_DeteriorosEmisores: Flag,
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: "PLA",
                    p_UsaReglasNegocio: "N"
                };

                bcsComunes.ventanaOverlay(idVentana, true);
                bcsServicioPlataforma.Servicios().ModificarDeterioroEmisor(datos, successCrearModificarDeterioro, errorCrearModificarDeterioro);
            }else{
                bcsComunes.ventanaOverlay(idVentana, false); 
            }
        })
    };

    $scope.cargarArchivoDeterioroEmisores = function () {
        const datosArchivo = { codigoArchivo: 'CARGA_DETERIORO_EMISORES', formatoArchivoDefecto: '.txt' };
        const id = bcsComunes.ventanaAbrir("CargarDeterioro", datosArchivo, true, true);
        
        const promesa = bcsComunes.ventanaPromesa(id);
        promesa.then(function (data) {
            if (!data || !data.fileData) {
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}
            $scope.datosCarga = data.fileData;
            const datos = $scope.datosCarga;
            guardarCargaExterna(datos);
        });
    };

    const guardarCargaExterna = (data) => {

        let accion;
        const deteriorosEmisores = data.map(function(dato) {

            let idEmisor = $scope.existeEmisor(dato["EMISOR"]);
            accion = (idEmisor) ? 'MODIFICAR' : 'CREAR'
            return {
                IdDeterioroEmisor: (idEmisor) ? idEmisor : 0,
                Emisor: dato["EMISOR"],
                Fase: dato["FASE"],
                FlagDeterioro: dato["FLAGDETERIORO"],
                PorcentajeDeterioro: dato["PORCENTAJEDETERIORO"],
                Accion: accion
            };
        });

       const datos = {
            p_DeteriorosEmisores: deteriorosEmisores,                    
            p_IdEmpresa: $scope.SesionIdEmpresa,
            p_IdUsuario: $scope.SesionIdUsuario,
            p_IdModulo: "PLA",
            p_UsaReglasNegocio: "N"
        };

        bcsComunes.ventanaOverlay(idVentana, true);
        bcsServicioPlataforma.Servicios().CargarArchivoDeterioroEmisor(datos, successCrearModificarDeterioro, errorCrearModificarDeterioro);
    }


    $scope.data.buscar = function(filtros) {
    if ($scope.data.grilla && $scope.data.grilla.dataSource) {
        $scope.data.grilla.dataSource.read();
    } else {
        console.log('grilla or grilla.dataSource is not defined');
    }
	};

	$scope.data.mostrarVentanaDeCarga = function() {
    var ventanaModalId = bcsComunes.ventanaAbrir("CargarDeterioro", {}, true, false);
    var promesa = bcsComunes.ventanaPromesa(ventanaModalId);
    };
}); 