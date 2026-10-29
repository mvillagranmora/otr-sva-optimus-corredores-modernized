angular.module('ModuloContabilidad').controller('InterfazContableCtrl',
  function ($rootScope, $scope, $window, $resource, $http, $timeout, bcsComunes, bcsAlerta, $translate, OPCIONES_BCSALERTA) {

    $scope.winID = bcsComunes.ventanaWinId($scope);
    $scope.data = {};
    $scope.IdVentana = bcsComunes.ventanaWinId($scope);
    bcsComunes.ventanaAutoOverlay($scope.winID,false, 3000);
    const fechaSistema = util.dateDS($rootScope.SesionFechaSistema, "FechaSistema", "CALL");
    $scope.data.kngFechaConsulta = fechaSistema;
    $scope.data.FechaConsultaInf = { max: fechaSistema };

    $scope.data.portafolioReporteSeleccionado = null;
    $scope.data.portafolioReporteIdActual = 0;
    $scope.data.portafolioReporteIdDefecto = sesion.getPreferencias("ClienteInformeLeasing");
    $scope.data.portafolioReporteFiltros = {
      IdCliente: { field: "IdCliente", operator: "eq", value: 0 }
    };

    var ConsultaTemplate = new bcs.DataSource({
      type: "odata",
      definition: {
        serverFiltering: true,
        transport: {
          read: {
            url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010155",
            dataType: "json",
          }
        },
        schema: {
          model: {
            fields: {
              IdTemplateContabilidad: { type: "string" },
              IdReporte: { type: "string" },
              NombreReporte: { type: "string" },
              IdServicio: { type: "number" },
              Descripcion: { type: "string" },
              IdEmpresa: { type: "number" },
              IdEstado: { type: "string" },
              ReportName: { type: "string" },
              MasterReportId: { type: "string" },
              ReportFile: { type: "string" },
              ReportVersion: { type: "number" },
              FormatoSalida: { type: "string" },
            }
          }
        }
      }
    });

    $scope.data.Generar = function () {
      sesion.setPreferencias("ClienteInformeLeasing", $scope.data.portafolioReporteIdActual);
      bcsComunes.ventanaOverlay($scope.winID, true);
      var subReports = [];
      var template = {};
      var filterTemplate = [
        { field: "NombreReporte", operator: "eq", value: 'INFORME_CARTERA_LEASING' },
        { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
        { field: "IdEstado", operator: "eq", value: 'ACT' }
      ];

      const ServiceDataReporte = {
        p_IdEmpresa: Number($scope.data.portafolioReporteSeleccionado.IdEmpresa),
        p_IdUsuario: Number($rootScope.SesionIdUsuario),
        p_FechaConsulta: util.dateSPdate($scope.data.kngFechaConsulta),  // formato DD-MM-YYYY
        p_IdCliente: Number($scope.data.portafolioReporteSeleccionado.IdCliente),
        p_IdPortafolio: Number($scope.data.portafolioReporteSeleccionado.IdPortafolio),
        "$schema"               : [{ "field" : "LogoCorredora", "type" : "blob" }]
      };

      ConsultaTemplate.query({ filter: filterTemplate }).then(function () {
        var data = ConsultaTemplate.data();

        for (let i = 0; i < data.length; i++) {
          if (data[i].IdTemplateContabilidad === 'CartLg') {
            template = {
              TipoReporte: data[i].FormatoSalida,
              IdReporte: data[i].IdReporte,
              NombreReporte: $translate.instant(data[i].NombreReporte),
              data: {
                serviceId: data[i].IdServicio,
                serviceData: ServiceDataReporte
              },
              idReporte: '64B301409BEFA0771900D20004486D9C'
            };
          } else {
            subReports.push({
              reportName: data[i].NombreSubReporte,
              parameters: {},
              data: {
                serviceId: data[i].ServicioReporte,
                serviceData: ServiceDataReporte
              }
            });
          }
        }

        template.subReports = subReports;
        $scope.getReport(template);
      });
    };

    $scope.getReport = function(dataTemplate) {
      var parameters = {
        "exportFormat": dataTemplate.TipoReporte,
        "reportId": dataTemplate.IdReporte,
        "parameters": {},
        "configuration": {
          "mainReport": {
            "reportName": dataTemplate.NombreReporte,
            "data": dataTemplate.data,
            "parameters": {}
          },
          "subReports": []
        }
      };
  
      $http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, { "responseType": "arraybuffer" })
        .success(function(response) {
          var file = new Blob([response], { type: 'application/pdf' });
          var nombreArchivo = dataTemplate.NombreReporte + '_' + util.dateSPdate($scope.data.kngFechaConsulta).replace(/-/g, '');
          saveAs(file, nombreArchivo + '.' + dataTemplate.TipoReporte);
  
          // Al generar PDF, llama al Excel
          excelReport();
          bcsComunes.ventanaOverlay($scope.winID, false);
  
        })
        .error(function() {
          bcsComunes.msg("error", 'Error', $translate.instant("UI_NO_HAY_DATOS"));
          bcsComunes.ventanaOverlay($scope.winID, false);
        });
  
        var excelReport = function(){
          /** Excel */
          var parameters = {
              "exportFormat"	: 'excel',
              "reportId"		: dataTemplate.IdReporte,
              "parameters"	: "",
              "configuration"	: {
                "mainReport": {
                  "reportName": dataTemplate.NombreReporte,
                  "data"		: dataTemplate.data,
                  "parameters": {}
                },
                "subReports": []
              }
            };
  
            $http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
              .success(function (response) {
                var file = new Blob([response], {type: 'application/xls'});
                              var nombreArchivo = dataTemplate.NombreReporte+'_'+util.dateSPdate($scope.data.kngFechaConsulta).replace(/-/g,'')
                 
                saveAs(file, nombreArchivo+"."+'xls');
              //	bcsComunes.ventanaOverlay(idVentana,false);
              })
              .error(function (response) {				
                bcsComunes.ventanaMsg({
                  mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
                  conflictos:util.parseServerResponse(response)
                },idVentana);
              });
    
        }
    };
  
  });
