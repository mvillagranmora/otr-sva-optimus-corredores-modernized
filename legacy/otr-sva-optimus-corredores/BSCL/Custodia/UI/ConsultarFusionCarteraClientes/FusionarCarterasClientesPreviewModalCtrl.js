angular.module('ModuloCustodia').controller('FusionarCarterasClientesPreviewModalCtrl',
function($scope, $timeout, $rootScope, $translate, bcsComunes, bcsServiciosCustodia)
{
/******************************************************/
/*** INICIO ***/
/******************************************************/
  var idVentana = bcsComunes.ventanaWinId($scope);
  var parametros   = bcsComunes.ventanaParams(idVentana);
  var idVentanaPadre = parametros.idVentanaPadre;
  $scope.filtros       = parametros.filtros;
  $scope.cargando      = true;
  $scope.sinResultados = false;
  $scope.habilitadoFusion = false;
  $scope.esMercadoRV = $scope.filtros.p_IdClasificacionTipoInstrumento === 'RV';
  $scope.ajustesManuales = [];
  $scope.$watchGroup(['cargando','sinResultados'],function(valores){
  $scope.habilitadoFusion = !valores[0] && !valores[1];
    });
  var gridPreview = null;
/******************************************************/
/*** MODELOS / COLUMNAS ***/
/******************************************************/
   var modeloPreviewFusion = function() {
       return {
           model: { fields: {
               IdGeneracionMovimiento           : { type: "number" }, // clave para enlazar con el detalle
               IdPortafolio                     : { type: "number" },
               IdClasificacionTipoInstrumento   : { type: "string" },
               Nemotecnico                      : { type: "string" },
               NemoSVS                          : { type: "string" },
               IdEmisor                         : { type: "string" },
               CarterasFusionadas               : { type: "number" },
               INGCantidad                      : { type: "number" },
               MontoCompraMonedaInstrumento     : { type: "number" },
               ValorCalculadoFus                : { type: "number" },
               ValorizacionExitosa              : { type: "boolean" }, // false = este grupo NO se fusionará realmente (no logró valorizarse)
               Precio                           : { type: "number" },
               Tasa                             : { type: "number" },
               AjustadoManualmente              : { type: "boolean" },
               AjusteActivo                     : { type: "string", editable: false } // 'PRECIO' | 'TASA' | null — control de exclusión, no editable directamente
           }}
       };
   };
   var modeloDetallePreview = function() {
       return {
           model: { fields: {
               IdGeneracionMovimiento  : { type: "number" },
               FolioCartera        : { type: "string" },
               Cantidad             : { type: "number" },
               ValorRescate         : { type: "number" },
               FechaCompra          : { type: "sysdate" },
               FechaLiquidacion     : { type: "sysdate" },
               MontoCompra          : { type: "number" },
               TasaCompra           : { type: "number" }
           }}
       };
   };
   var columnasPreviewFusion = function() {
       var columnas = [
           {field: "IdPortafolio",                    title: 'UI_PORTAFOLIO_ABR',      width: "70px" },
           {field: "IdClasificacionTipoInstrumento",  title: 'UI_MERCADO',             width: "60px" },
           {field: "Nemotecnico",                     title: 'UI_INSTRUMENTO',         width: "100px" },
           {field: "NemoSVS",                         title: 'UI_NEMO_SVS',            width: "80px" },
           {field: "IdEmisor",                        title: 'UI_EMISOR',              width: "80px" },
           {field: "CarterasFusionadas",              title: 'UI_CARTERA_FUSIONADA',   width: "60px" },
           {field: "INGCantidad", title: 'UI_CANTIDAD_TOTAL',
               template: " <label> #: (util.bcs_formato(INGCantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", width: "90px" },
           {field: "MontoCompraMonedaInstrumento", title: 'UI_MONTO_COMPRA',
               template: " <label> #: (util.bcs_formato(MontoCompraMonedaInstrumento,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>",
               width: "110px", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
           {field: "Precio", title: 'UI_PRECIO', width: "90px", editable: true,
               template: " <label> #: (util.bcs_formato(Precio,'GEN','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"},
           {field: "ValorCalculadoFus", title: 'UI_VALOR_PRESENTE',
               template: " <label> #: (util.bcs_formato(ValorCalculadoFus,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>",
               width: "110px" }
       ];
       if (!$scope.esMercadoRV) {
           columnas.splice(6, 0, {field: "Tasa", title: 'UI_TASA', width: "70px", editable: true,
               template: " <label> #: (util.bcs_formato(Tasa,'GEN','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"});
       }
       return columnas;
   };
   console.log(util.bcs_formato(0.005, 'GEN', 'Precio', 'CLP', '', ''));
	// revisa .cantidadDecimales y .trucaRedondea en el resultado
   var columnasDetallePreview = function() {
       return [
           {field: "FolioCartera",     title: "UI_FOLIO_CARTERA_ABR",     width: "70px" },
           {field: "Cantidad",         title: "UI_CANTIDAD",
               template: " <label> #: (util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", width: "80px" },
           {field: "ValorRescate",     title: "UI_VALOR_RESCATE",
               template: " <label> #: (util.bcs_formato(ValorRescate,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", width: "80px" },
           {field: "FechaCompra",      title: "UI_FECHA_COMPROBANTE",     width: "80px" },
           {field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION_ABR", width: "80px" },
           {field: "MontoCompra",      title: "UI_MONTO_COMPRA",
               template: " <label> #: (util.bcs_formato(MontoCompra,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','IdTipoInstrumento','')).valor #</label>", width: "90px" },
           {field: "TasaCompra",       title: "UI_TASA_COMPRA",
               template: " <label> #: (util.bcs_formato(TasaCompra,'GEN','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", width: "70px" }
       ];
   };
/******************************************************/
/*** PAYLOAD BASE (igual composición que la función real) ***/
/******************************************************/
   var construirDatosFC = function(simular){
       return {
           p_IdEmpresa : $rootScope.SesionIdEmpresa,
           p_IdUsuario : $rootScope.SesionIdUsuario,
           p_IdPais    : $rootScope.SesionIdPaisSistema,
           p_IdModulo  : "CUS",
       	   p_IdCliente : $scope.filtros.p_IdCliente,
           p_IdPortafolio                    : $scope.filtros.p_IdPortafolio,
           p_IdClasificacionTipoInstrumento  : $scope.filtros.p_IdClasificacionTipoInstrumento,
           p_IdTipoInstrumento               : $scope.filtros.p_IdTipoInstrumento,
           p_Nemotecnico                     : $scope.filtros.p_Nemotecnico,
           // "S" = solo simula y retorna el resultado sin persistir, "N" = ejecuta y persiste
           p_simularOperacion : simular ? "S" : "N",
           p_AjustesManuales : $scope.ajustesManuales
       };
   };

   // Título fijo del encabezado de ventanaMsg, segun haya o no errores. El detalle real de
   // los mensajes (P_OUT_MENSAJES) SIEMPRE va en 'conflictos' (util.parseServerResponse), que
   // ya se renderiza como lista abajo del encabezado. Nunca se debe repetir el mismo texto en
   // 'mensaje', o queda duplicado (una vez como encabezado, otra vez en el detalle).
   var construirTituloMensaje = function(result) {
       var hayErrores = result && result.P_OUT_ERRORESCONTADOR > 0;
       return hayErrores
           ? $translate.instant("UI_ERROR")
           : $translate.instant("UI_INFORMACION_CARTERAS");
   };
/******************************************************/
/*** DATASOURCE DE CABECERA ***/
/*** Se crea UNA sola vez, vacío, de forma SÍNCRONA (igual que  ***/
/*** el patrón de la pantalla de consulta, donde el dataSource  ***/
/*** existe desde el inicio y solo se le hace .read()/.data()   ***/
/*** después).
/******************************************************/
   var detalles = [];
   var dsCabecera = new bcs.DataSource({
    type : "array",
    definition : {
        data : [],
        schema : modeloPreviewFusion(),
        pageSize : 25
    }
  });
  function actualizarCabecera(data) {
 
      if (!angular.isArray(data))
          data = [];
  
      if (dsCabecera.ds) {
          dsCabecera.ds.data(data);
      }
 
      if (angular.isFunction(dsCabecera.data)) {
          dsCabecera.data(data);
      }
 
      if (gridPreview && gridPreview.refresh) {
          gridPreview.refresh();
      }
  }
      $scope.confBasicaPreviewFusion = {
        dataSource  : dsCabecera,
        columns     : columnasPreviewFusion(),
        sortable    : true,
        pageable    : true,
        filterable  : true,
        navigatable : true,
        height      : 300,
        selectable  : "row",
       notEditableColumns: function(dataItem) {
           if (dataItem.AjusteActivo === 'PRECIO') return ['Tasa'];
           if (dataItem.AjusteActivo === 'TASA') return ['Precio'];
           return [];
       },
       save: function(e) {
           var camposEditados = Object.keys(e.values || {});
           if (camposEditados.length === 0) return;
           var campo = camposEditados[0]; // en edicion incell siempre llega un solo campo
           if (campo !== 'Precio' && campo !== 'Tasa') return;

           var valor = e.values[campo];
           if (valor === null || valor === undefined || valor === '') return;

           var tipoAjuste = campo === 'Precio' ? 'PRECIO' : 'TASA';
           e.model.AjusteActivo = tipoAjuste;

           var folios = obtenerFoliosDeGrupo(e.model.IdGeneracionMovimiento);
           if (!folios.length) return;

           upsertAjuste(folios, tipoAjuste, valor);
           cargarPreview(); // recalcula todo el preview contra el back, incluyendo este ajuste
       },
        gridCreated: function (grid, kendoGrid) {    
         gridPreview = kendoGrid || grid;
         if (dsCabecera.ds && gridPreview && gridPreview.dataSource) {
             gridPreview.dataSource.data(dsCabecera.ds.data());
             gridPreview.refresh();
         }
     }
    };
   
   
   $scope.confDetallePreviewFusion = function(dataItem) {
       var detalleFiltrado = detalles.filter(function(d){
           return d.IdGeneracionMovimiento === dataItem.IdGeneracionMovimiento;
       });
       var dsDetalle = new bcs.DataSource({
        type : "array",
        definition : {
            data : detalleFiltrado,
            schema : modeloDetallePreview(),
            pageSize : 10
        }
    });
       return {
           dataSource  : dsDetalle,
           columns     : columnasDetallePreview(),
           sortable    : true,
           pageable    : true,
           height      : 150,
           width       : 300,
           selectable  : "multiple row",
           autoBind    : true,
           navigatable : true,
           allowCopy   : true
       };
   };
  /******************************************************/
/*** SIMULACIÓN (p_simularOperacion="S") ***/
/******************************************************/
     var cargarPreview = function() {
 
      $scope.cargando = true;
 
      var datosSimulacion = construirDatosFC(true);
 
      bcsServiciosCustodia.FusionarCarterasClientes(
          datosSimulacion
      ).$promise.then(
 
          function(result) {
 
              var cabeceras =
                  (result && result.P_OUT_RESULTADO)
                      ? result.P_OUT_RESULTADO
                      : [];
 
              var hayErrores =
                  result &&
                  result.P_OUT_ERRORESCONTADOR > 0;
 
              var sinDatos =
                  cabeceras.length === 0;
 
            /*
             * No hay información para mostrar o existe un error.
             */
              if (hayErrores || sinDatos) {
 
                  $scope.cargando = false;
 
                  var idMensaje = bcsComunes.ventanaMsg(
                      {
                          mensaje: construirTituloMensaje(result),
                          conflictos: util.parseServerResponse(result)
                      },
                      idVentanaPadre
                  );
 
                /*
                 * La ventana Preview se cierra DESPUÉS
                 * de cerrar/terminar el mensaje.
                 */
                  if (idMensaje) {
 
                      var promesaMensaje =
                          bcsComunes.ventanaPromesa(idMensaje);
 
                      promesaMensaje.then(function() {
 
                          $timeout(function() {
 
                              bcsComunes.ventanaCerrar(
                                  idVentana
                              );
 
                          }, 20);
 
                      });
 
                  } else {
 
                      bcsComunes.ventanaCerrar(
                          idVentana
                      );
                  }
 
                  return;
              }
 
            /*
             * Preview correcto.
             */
              detalles =
                  (result && result.P_OUT_DETALLE)
                      ? result.P_OUT_DETALLE
                      : [];
 
              actualizarCabecera(cabeceras);
 
              $scope.cargando = false;
 
              $timeout(function() {
 
                  bcsComunes.ventanaAutoHeight(
                      idVentana
                  );
 
                  if (gridPreview && gridPreview.resize) {
                      gridPreview.resize(true);
                  }
 
              }, 0);
          },
 
          function(error) {
 
              $scope.cargando = false;
 
              $scope.ErrorManager(error);
          }
      );
  };
   cargarPreview();
   
/******************************************************/
/*** AJUSTE MANUAL DE PRECIO/TASA ***/
/******************************************************/
   function obtenerFoliosDeGrupo(idGeneracionMovimiento) {
       return detalles
           .filter(function(d){ return d.IdGeneracionMovimiento === idGeneracionMovimiento; })
           .map(function(d){ return d.FolioCartera; });
   }

   function upsertAjuste(folios, tipoAjuste, valor) {
       var idx = -1;
       for (var i = 0; i < $scope.ajustesManuales.length; i++) {
           if (angular.equals($scope.ajustesManuales[i].Folios.slice().sort(), folios.slice().sort())) {
               idx = i; break;
           }
       }
       var ajuste = { Folios: folios, TipoAjuste: tipoAjuste, Valor: valor };
       if (idx >= 0) { $scope.ajustesManuales[idx] = ajuste; }
       else { $scope.ajustesManuales.push(ajuste); }
   }

   function onAjusteCeldaPreview(dataItem, campo) {
       var valor = campo === 'Precio' ? dataItem.Precio : dataItem.Tasa;
       if (valor === null || valor === undefined || valor === '') return;

       var tipoAjuste = campo === 'Precio' ? 'PRECIO' : 'TASA';
       dataItem.AjusteActivo = tipoAjuste; // bloquea el campo hermano en la próxima edición

       var folios = obtenerFoliosDeGrupo(dataItem.IdGeneracionMovimiento);
       if (!folios.length) return;

       upsertAjuste(folios, tipoAjuste, valor);
       cargarPreview(); // recalcula todo el preview contra el back, incluyendo este ajuste
   }

   $scope.revertirAjuste = function(dataItem) {
       var folios = obtenerFoliosDeGrupo(dataItem.IdGeneracionMovimiento);
       $scope.ajustesManuales = $scope.ajustesManuales.filter(function(a){
           return !angular.equals(a.Folios.slice().sort(), folios.slice().sort());
       });
       cargarPreview();
   };  
   
/******************************************************/
/*** 2 EJECUCIÓN REAL (p_simularOperacion="N")     ***/
/******************************************************/
  $scope.ejecutarFusion = function () {
      bcsComunes.ventanaOverlay(idVentanaPadre, true);
 
      var datosFC = construirDatosFC(false);
 
      bcsServiciosCustodia.FusionarCarterasClientes(datosFC).$promise.then(
          function (result) {
 
              if (!result) {
                  bcsComunes.ventanaOverlay(idVentanaPadre, false);
 
                  bcsComunes.msg(
                      'error',
                      $translate.instant("UI_ERROR"),
                      $translate.instant("UI_ERROR")
                  );
 
                  return;
              }
 
              if (result.P_OUT_ERRORESCONTADOR === 0) {
 
                /*
                 * Primero quitamos el overlay.
                 * Después, en el siguiente ciclo, abrimos ventanaMsg.
                 */
                  bcsComunes.ventanaOverlay(
                      idVentanaPadre,
                      false
                  );
 
                  $timeout(function () {
 
                      var id = bcsComunes.ventanaMsg(
                          {
                              mensaje: $translate.instant("UI_PROCESO_EJECUTADO_EXITO"),
                              conflictos: util.parseServerResponse(result)
                          },
                          idVentanaPadre
                      );
 
                      if (id) {
 
                          var promesa =
                              bcsComunes.ventanaPromesa(id);
 
                          promesa.then(function () {
 
                              $timeout(function () {
 
                                  bcsComunes.ventanaData(
                                      idVentana,
                                      true
                                  );
 
                                  bcsComunes.ventanaCerrar(
                                      idVentana
                                  );
 
                              }, 20);
 
                          });
 
                      } else {
 
                          bcsComunes.ventanaData(
                              idVentana,
                              true
                          );
 
                          bcsComunes.ventanaCerrar(
                              idVentana
                          );
                      }
 
                  }, 0);
 
              } else {
 
                  bcsComunes.ventanaOverlay(
                      idVentanaPadre,
                      false
                  );
 
                  $timeout(function () {
 
                      var idError = bcsComunes.ventanaMsg(
                          {
                              mensaje: construirTituloMensaje(result),
                              conflictos: util.parseServerResponse(result)
                          },
                          idVentanaPadre
                      );
 
                      if (!idError) {
 
                          bcsComunes.msg(
                              'error',
                              $translate.instant("UI_ERROR"),
                              result.P_OUT_ERRORESDETALLE &&
                              result.P_OUT_ERRORESDETALLE.length > 0
                                  ? result.P_OUT_ERRORESDETALLE[0].Codigo
                                  : $translate.instant("UI_ERROR")
                          );
                      }
 
                  }, 0);
              }
          },
          function (error) {
 
              bcsComunes.ventanaOverlay(
                  idVentanaPadre,
                  false
              );
 
              bcsComunes.msg(
                  "error",
                  $translate.instant("UI_ERROR"),
                  $translate.instant("UI_ERROR")
              );
          }
      );
  };
 
   $scope.cerrar = function () {
      bcsComunes.ventanaCerrar(idVentana);
  };
 
   $scope.ErrorManager = function (errorcatched) {
       bcsComunes.msg('error', $translate.instant("UI_ERROR"), errorcatched);
   }; 
});